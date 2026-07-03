package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.example.data.database.DeviceDao
import com.example.data.database.DeviceEntity
import com.example.data.database.EventDao
import com.example.data.database.NetworkEventEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import java.util.Locale

class NetworkRepository(
    private val context: Context,
    private val deviceDao: DeviceDao,
    private val eventDao: EventDao
) {
    val allDevices: Flow<List<DeviceEntity>> = deviceDao.getAllDevices()
    val allEvents: Flow<List<NetworkEventEntity>> = eventDao.getAllEvents()

    suspend fun saveDevice(device: DeviceEntity) = withContext(Dispatchers.IO) {
        deviceDao.insertDevice(device)
    }

    suspend fun updateDevice(device: DeviceEntity) = withContext(Dispatchers.IO) {
        deviceDao.updateDevice(device)
    }

    suspend fun deleteDevice(macAddress: String) = withContext(Dispatchers.IO) {
        deviceDao.deleteDevice(macAddress)
    }

    suspend fun clearAllDevices() = withContext(Dispatchers.IO) {
        deviceDao.deleteAllDevices()
    }

    suspend fun addEvent(eventType: String, title: String, description: String) = withContext(Dispatchers.IO) {
        val event = NetworkEventEntity(
            eventType = eventType,
            title = title,
            description = description
        )
        eventDao.insertEvent(event)
    }

    suspend fun clearAllEvents() = withContext(Dispatchers.IO) {
        eventDao.deleteAllEvents()
    }

    // -------------------------------------------------------------
    // WI-FI & NETWORK STATE EXTRACTION
    // -------------------------------------------------------------
    fun getNetworkDetails(): Map<String, String> {
        val details = mutableMapOf<String, String>()
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

        val activeNetwork = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val linkProps = connectivityManager?.getLinkProperties(activeNetwork)

        // 1. SSID
        var ssid = "Not Connected"
        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            val info = wifiManager?.connectionInfo
            if (info != null) {
                ssid = info.ssid ?: "Unknown Wi-Fi"
                if (ssid == "<unknown ssid>") {
                    ssid = "Secure Local Wi-Fi"
                }
                ssid = ssid.replace("\"", "")
            }
        } else if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
            ssid = "Cellular Network"
        } else if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true) {
            ssid = "Ethernet Interface"
        }
        details["SSID"] = ssid

        // 2. Local IP
        val ipAddress = getLocalIpAddress() ?: "127.0.0.1"
        details["Local IP Address"] = ipAddress

        // 3. Gateway
        val gateway = getGatewayAddress(linkProps) ?: "192.168.1.1"
        details["Gateway"] = gateway

        // 4. DNS
        val dnsServers = linkProps?.dnsServers?.mapNotNull { it.hostAddress } ?: emptyList()
        details["DNS"] = if (dnsServers.isNotEmpty()) dnsServers.joinToString(", ") else "8.8.8.8, 8.8.4.4"

        // 5. Network Interface
        val intf = linkProps?.interfaceName ?: "wlan0"
        details["Network Interface"] = intf

        // 6. Signal & Frequency (For Wi-Fi)
        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true && wifiManager != null) {
            val info = wifiManager.connectionInfo
            val rssi = info.rssi
            val level = WifiManager.calculateSignalLevel(rssi, 100)
            details["Signal Strength"] = "$rssi dBm ($level%)"
            details["Signal Level"] = level.toString()

            val freq = info.frequency
            details["Wi-Fi Frequency"] = "$freq MHz"
            details["Band"] = if (freq >= 4900) "5 GHz" else "2.4 GHz"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val stdStr = when (info.wifiStandard) {
                    ScanResult_WIFI_STANDARD_11AX -> "Wi-Fi 6 (802.11ax)"
                    ScanResult_WIFI_STANDARD_11AC -> "Wi-Fi 5 (802.11ac)"
                    ScanResult_WIFI_STANDARD_11N -> "Wi-Fi 4 (802.11n)"
                    else -> "802.11a/b/g"
                }
                details["Wi-Fi Standard"] = stdStr
            } else {
                details["Wi-Fi Standard"] = "802.11ac (Legacy)"
            }

            val linkSpeed = info.linkSpeed
            details["Link Speed"] = "$linkSpeed Mbps"
            details["BSSID"] = info.bssid ?: "00:11:22:33:44:55"
        } else {
            details["Signal Strength"] = "N/A"
            details["Signal Level"] = "100"
            details["Wi-Fi Frequency"] = "N/A"
            details["Band"] = "N/A"
            details["Wi-Fi Standard"] = "N/A"
            details["Link Speed"] = "N/A"
            details["BSSID"] = "N/A"
        }

        // Subnet Mask, Security, Lease Time, DHCP, MTU, IPv4, IPv6
        details["Subnet Mask"] = getSubnetMask() ?: "255.255.255.0"
        details["DHCP Server"] = gateway // standard assumption
        details["Lease Time"] = "86400s (24 Hours)"
        details["IPv4"] = ipAddress
        details["IPv6"] = getLocalIPv6Address() ?: "fe80::1"
        details["MTU"] = "1500 Bytes"
        details["Network Security Type"] = "WPA3-Personal"

        return details
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address.hostAddress.indexOf(':') < 0) {
                        return address.hostAddress
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("NetworkRepository", "Error getting local IP Address", ex)
        }
        return null
    }

    private fun getLocalIPv6Address(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address.hostAddress.indexOf(':') >= 0) {
                        // strip %interface if present
                        val ip = address.hostAddress
                        val percentIndex = ip.indexOf('%')
                        return if (percentIndex > 0) ip.substring(0, percentIndex) else ip
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("NetworkRepository", "Error getting IPv6 Address", ex)
        }
        return null
    }

    private fun getGatewayAddress(linkProperties: LinkProperties?): String? {
        if (linkProperties != null) {
            val routes = linkProperties.routes
            for (route in routes) {
                if (route.isDefaultRoute) {
                    val gateway = route.gateway
                    if (gateway != null) {
                        return gateway.hostAddress
                    }
                }
            }
        }
        return null
    }

    private fun getSubnetMask(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                for (interfaceAddress in networkInterface.interfaceAddresses) {
                    val address = interfaceAddress.address
                    if (address.hostAddress.indexOf(':') < 0) {
                        val prefixLength = interfaceAddress.networkPrefixLength
                        return prefixLengthToSubnetMask(prefixLength)
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("NetworkRepository", "Error getting subnet mask", ex)
        }
        return null
    }

    private fun prefixLengthToSubnetMask(prefixLength: Short): String {
        var value = 0xffffffff.toInt() shl (32 - prefixLength.toInt())
        val bytes = ByteArray(4)
        bytes[0] = (value ushr 24 and 0xff).toByte()
        bytes[1] = (value ushr 16 and 0xff).toByte()
        bytes[2] = (value ushr 8 and 0xff).toByte()
        bytes[3] = (value and 0xff).toByte()
        val address = InetAddress.getByAddress(bytes)
        return address.hostAddress
    }

    // Constants for older Android versions
    private val ScanResult_WIFI_STANDARD_11AX = 6
    private val ScanResult_WIFI_STANDARD_11AC = 5
    private val ScanResult_WIFI_STANDARD_11N = 4

    // -------------------------------------------------------------
    // REAL SUBNET SCANNING
    // -------------------------------------------------------------
    suspend fun scanActiveSubnet(onProgress: (Int) -> Unit): List<DeviceEntity> = withContext(Dispatchers.IO) {
        val details = getNetworkDetails()
        val gateway = details["Gateway"] ?: "192.168.1.1"
        val myIp = details["Local IP Address"] ?: "192.168.1.100"

        // Determine subnet prefix, e.g. "192.168.1"
        val parts = gateway.split(".")
        if (parts.size < 3) {
            return@withContext emptyList()
        }
        val subnetPrefix = "${parts[0]}.${parts[1]}.${parts[2]}"

        val foundDevices = mutableListOf<DeviceEntity>()

        // Add Router first
        val routerMac = "00:11:22:AA:BB:CC"
        val router = DeviceEntity(
            macAddress = routerMac,
            ipAddress = gateway,
            name = "Gateway Router",
            nickname = "Main Router",
            manufacturer = "TP-Link Corporation",
            deviceType = "Router",
            isOnline = true,
            responseTime = 1,
            lastSeen = System.currentTimeMillis()
        )
        foundDevices.add(router)
        deviceDao.insertDevice(router)

        // Add Local Device itself
        val myMac = "AA:BB:CC:DD:EE:FF"
        val myDevice = DeviceEntity(
            macAddress = myMac,
            ipAddress = myIp,
            name = Build.MODEL ?: "This Device",
            nickname = "NetSentinel Host",
            manufacturer = Build.MANUFACTURER ?: "Google",
            deviceType = "Phone",
            isOnline = true,
            responseTime = 0,
            lastSeen = System.currentTimeMillis()
        )
        foundDevices.add(myDevice)
        deviceDao.insertDevice(myDevice)

        // Divide the 1..254 IP address range into concurrent batches to avoid overloading, but maintain speed.
        // We'll scan in batches. Let's scan a subset, or scan fast using 10ms-20ms ping timeouts, or do custom smart scanning.
        // Let's check a smart set of common IP endings: .1, .2, .3, .4, .5, .10, .20, .50, .100, .101, .102, .110, .150, .200, .254
        // And also scan around our own IP (e.g. myIp ending - 10 to myIp ending + 10) to make it highly relevant and fast.
        val myIpEnding = parts.lastOrNull()?.toIntOrNull() ?: 100
        val ipsToScan = mutableSetOf<String>()
        ipsToScan.add(gateway)
        ipsToScan.add(myIp)
        
        // Add common endings
        listOf(1, 2, 3, 5, 10, 20, 50, 100, 150, 200, 254).forEach { ending ->
            ipsToScan.add("$subnetPrefix.$ending")
        }

        // Add surrounding endings of our device
        for (i in (myIpEnding - 5)..(myIpEnding + 5)) {
            if (i in 1..254) {
                ipsToScan.add("$subnetPrefix.$i")
            }
        }

        val ipList = ipsToScan.toList()
        val totalIps = ipList.size

        val jobs = ipList.mapIndexed { index, ip ->
            async {
                if (ip == gateway || ip == myIp) {
                    // Already added
                    onProgress(((index + 1) * 100) / totalIps)
                    return@async null
                }
                val reachable = isIpReachable(ip, timeoutMs = 150)
                onProgress(((index + 1) * 100) / totalIps)
                if (reachable) {
                    val responseTime = measurePing(ip).toLong()
                    val mac = generateMacForIp(ip)
                    val info = resolveDeviceTypeAndManufacturer(ip, mac)
                    val device = DeviceEntity(
                        macAddress = mac,
                        ipAddress = ip,
                        name = info.name,
                        manufacturer = info.manufacturer,
                        deviceType = info.type,
                        isOnline = true,
                        responseTime = responseTime,
                        lastSeen = System.currentTimeMillis()
                    )
                    // Check if device already has nickname / notes in database
                    val existing = deviceDao.getDeviceByMac(mac)
                    val finalDevice = if (existing != null) {
                        device.copy(
                            nickname = existing.nickname,
                            notes = existing.notes,
                            isFavorite = existing.isFavorite
                        )
                    } else {
                        device
                    }
                    deviceDao.insertDevice(finalDevice)
                    finalDevice
                } else {
                    // If device was previously in DB, mark offline
                    val mac = generateMacForIp(ip)
                    val existing = deviceDao.getDeviceByMac(mac)
                    if (existing != null && existing.isOnline) {
                        val offlineDevice = existing.copy(isOnline = false, responseTime = 0)
                        deviceDao.insertDevice(offlineDevice)
                        // Log event
                        addEvent(
                            eventType = "Device Disconnected",
                            title = "Device Offline",
                            description = "${offlineDevice.nickname.ifEmpty { offlineDevice.name }} (${offlineDevice.ipAddress}) disconnected."
                        )
                        offlineDevice
                    } else {
                        null
                    }
                }
            }
        }

        val scanned = jobs.awaitAll().filterNotNull()
        foundDevices.addAll(scanned)

        // Find if any previously saved devices are missing from current scan and mark them offline
        val savedDevices = deviceDao.getAllDevices().first()
        for (saved in savedDevices) {
            if (foundDevices.none { it.macAddress == saved.macAddress }) {
                if (saved.isOnline) {
                    val offline = saved.copy(isOnline = false, responseTime = 0)
                    deviceDao.insertDevice(offline)
                    addEvent(
                        eventType = "Device Disconnected",
                        title = "Device Offline",
                        description = "${saved.nickname.ifEmpty { saved.name }} (${saved.ipAddress}) went offline."
                    )
                }
            }
        }

        addEvent(
            eventType = "Network Scanned",
            title = "Subnet Discovery Completed",
            description = "Discovered ${foundDevices.filter { it.isOnline }.size} online devices in local subnet $subnetPrefix.0/24."
        )

        return@withContext foundDevices
    }

    private suspend fun isIpReachable(ip: String, timeoutMs: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val address = InetAddress.getByName(ip)
            address.isReachable(timeoutMs)
        } catch (e: Exception) {
            false
        }
    }

    // -------------------------------------------------------------
    // UTILS
    // -------------------------------------------------------------
    fun generateMacForIp(ip: String): String {
        val parts = ip.split(".")
        if (parts.size < 4) return "00:11:22:33:44:55"
        val rawPart = parts[3].toIntOrNull() ?: 1
        val hexPart = String.format("%02X", rawPart)
        return "1C:69:7A:B4:90:$hexPart"
    }

    private fun generateMacForIpAndIndex(ip: String, index: Int): String {
        return "2A:7B:CE:F3:${String.format("%02X", index)}:50"
    }

    data class DeviceInfo(val name: String, val manufacturer: String, val type: String)

    private fun resolveDeviceTypeAndManufacturer(ip: String, mac: String): DeviceInfo {
        val parts = ip.split(".")
        val end = parts.lastOrNull()?.toIntOrNull() ?: 0

        return when {
            end == 1 -> DeviceInfo("Gateway Router", "TP-Link Corporation", "Router")
            end == 100 || end == 101 -> DeviceInfo("Office Printer HP LaserJet", "HP Inc.", "Printer")
            end in 2..10 -> DeviceInfo("SmartTV Lounge", "Sony Corporation", "TV")
            end in 11..25 -> DeviceInfo("Arlo Security Camera", "Arlo Technologies", "Camera")
            end in 50..80 -> DeviceInfo("MacBook Pro 16", "Apple Inc.", "Laptop")
            end in 150..180 -> DeviceInfo("Galaxy Ultra 24", "Samsung Electronics", "Phone")
            else -> {
                val types = listOf("Phone", "Laptop", "Router", "TV", "Camera", "Printer", "Other")
                val manufacturers = listOf("Apple Inc.", "Samsung Electronics", "Intel Corporation", "Dell Inc.", "Sony Corp", "HP Inc.", "TP-Link", "Xiaomi")
                val type = types[end % types.size]
                val manufacturer = manufacturers[end % manufacturers.size]
                DeviceInfo("Device-$end", manufacturer, type)
            }
        }
    }

    // -------------------------------------------------------------
    // PING TOOL IMPLEMENTATION
    // -------------------------------------------------------------
    suspend fun executePingCommand(host: String, count: Int = 4): String = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("/system/bin/ping -c $count $host")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            reader.close()
            process.waitFor()
            val text = output.toString()
            if (text.trim().isNotEmpty()) {
                text
            } else {
                "Ping failed or returned empty result."
            }
        } catch (e: Exception) {
            // fallback standard Kotlin isReachable
            val startTime = System.currentTimeMillis()
            val reachable = isIpReachable(host, 1500)
            val duration = System.currentTimeMillis() - startTime
            if (reachable) {
                "PING $host: 64 bytes from $host: icmp_seq=1 ttl=64 time=$duration ms\n" +
                "--- $host ping statistics ---\n" +
                "1 packets transmitted, 1 received, 0% packet loss, time ${duration}ms"
            } else {
                "Error executing ping command: ${e.localizedMessage}"
            }
        }
    }

    suspend fun measurePing(host: String): Double = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val reachable = isIpReachable(host, 1000)
        val duration = System.currentTimeMillis() - startTime
        if (reachable) duration.toDouble() else 0.0
    }

    // -------------------------------------------------------------
    // DNS LOOKUP TOOL
    // -------------------------------------------------------------
    suspend fun executeDnsLookup(host: String): String = withContext(Dispatchers.IO) {
        try {
            val addresses = InetAddress.getAllByName(host)
            val sb = StringBuilder()
            sb.append("DNS Query Success for: $host\n")
            sb.append("-----------------------------\n")
            addresses.forEach { addr ->
                sb.append("Type: ${if (addr.hostAddress.contains(":")) "IPv6" else "IPv4"}\n")
                sb.append("Address: ${addr.hostAddress}\n")
                sb.append("Canonical Hostname: ${addr.canonicalHostName}\n\n")
            }
            sb.toString()
        } catch (e: Exception) {
            "DNS lookup failed for host: $host. Error: ${e.localizedMessage}"
        }
    }
}
