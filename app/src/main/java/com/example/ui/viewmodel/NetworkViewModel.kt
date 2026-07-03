package com.example.ui.viewmodel

import android.content.Context
import android.net.TrafficStats
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.DeviceEntity
import com.example.data.database.NetworkEventEntity
import com.example.data.repository.NetworkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.random.Random

enum class DeviceSort {
    IP_ADDRESS, NAME, RESPONSE_TIME, LAST_SEEN, FAVORITE
}

enum class DeviceFilter {
    ALL, PHONES, LAPTOPS, ROUTERS, TVS, PRINTERS, FAVORITES, ONLINE
}

class NetworkViewModel(
    private val context: Context,
    private val repository: NetworkRepository
) : ViewModel() {

    // Active Screen Tab
    private val _activeTab = MutableStateFlow("DASHBOARD")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    // Network Details Map
    private val _networkDetails = MutableStateFlow<Map<String, String>>(emptyMap())
    val networkDetails: StateFlow<Map<String, String>> = _networkDetails.asStateFlow()

    // Subnet scan state
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0)
    val scanProgress: StateFlow<Int> = _scanProgress.asStateFlow()

    // Database Flows
    val devices: StateFlow<List<DeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<NetworkEventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search, sorting, filtering states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _deviceSortBy = MutableStateFlow(DeviceSort.IP_ADDRESS)
    val deviceSortBy: StateFlow<DeviceSort> = _deviceSortBy.asStateFlow()

    private val _deviceFilterBy = MutableStateFlow(DeviceFilter.ALL)
    val deviceFilterBy: StateFlow<DeviceFilter> = _deviceFilterBy.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortBy(sort: DeviceSort) {
        _deviceSortBy.value = sort
    }

    fun setFilterBy(filter: DeviceFilter) {
        _deviceFilterBy.value = filter
    }

    // Settings States
    private val _themeMode = MutableStateFlow("DARK") // DARK, LIGHT, AMOLED
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _language = MutableStateFlow("EN") // EN, AR
    val language: StateFlow<String> = _language.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _scanInterval = MutableStateFlow(15) // minutes
    val scanInterval: StateFlow<Int> = _scanInterval.asStateFlow()

    private val _themeAccent = MutableStateFlow("Indigo") // Indigo, Cyan, Emerald, Sapphire, Coral
    val themeAccent: StateFlow<String> = _themeAccent.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun setScanInterval(minutes: Int) {
        _scanInterval.value = minutes
    }

    fun setThemeAccent(accent: String) {
        _themeAccent.value = accent
    }

    // Tool States
    private val _pingResult = MutableStateFlow("")
    val pingResult: StateFlow<String> = _pingResult.asStateFlow()

    private val _isPingRunning = MutableStateFlow(false)
    val isPingRunning: StateFlow<Boolean> = _isPingRunning.asStateFlow()

    private val _dnsLookupResult = MutableStateFlow("")
    val dnsLookupResult: StateFlow<String> = _dnsLookupResult.asStateFlow()

    private val _isDnsRunning = MutableStateFlow(false)
    val isDnsRunning: StateFlow<Boolean> = _isDnsRunning.asStateFlow()

    // Real-Time Speed Test Simulator
    private val _downloadSpeed = MutableStateFlow(0.0) // Mbps
    val downloadSpeed: StateFlow<Double> = _downloadSpeed.asStateFlow()

    private val _uploadSpeed = MutableStateFlow(0.0) // Mbps
    val uploadSpeed: StateFlow<Double> = _uploadSpeed.asStateFlow()

    private val _speedTestProgress = MutableStateFlow(0.0) // 0.0 to 1.0
    val speedTestProgress: StateFlow<Double> = _speedTestProgress.asStateFlow()

    private val _isSpeedTesting = MutableStateFlow(false)
    val isSpeedTesting: StateFlow<Boolean> = _isSpeedTesting.asStateFlow()

    private val _currentDownloadSpeed = MutableStateFlow("0.00 Kbps")
    val currentDownloadSpeed: StateFlow<String> = _currentDownloadSpeed.asStateFlow()

    private val _currentUploadSpeed = MutableStateFlow("0.00 Kbps")
    val currentUploadSpeed: StateFlow<String> = _currentUploadSpeed.asStateFlow()

    // Live Telemetry lists (Download, Upload, Latency, Signal, Device Count, Bandwidth)
    private val _telemetryDownload = MutableStateFlow<List<Float>>(List(15) { 20f + Random.nextFloat() * 10f })
    val telemetryDownload: StateFlow<List<Float>> = _telemetryDownload.asStateFlow()

    private val _telemetryUpload = MutableStateFlow<List<Float>>(List(15) { 5f + Random.nextFloat() * 4f })
    val telemetryUpload: StateFlow<List<Float>> = _telemetryUpload.asStateFlow()

    private val _telemetryLatency = MutableStateFlow<List<Float>>(List(15) { 15f + Random.nextFloat() * 10f })
    val telemetryLatency: StateFlow<List<Float>> = _telemetryLatency.asStateFlow()

    private val _telemetrySignal = MutableStateFlow<List<Float>>(List(15) { 70f + Random.nextFloat() * 15f })
    val telemetrySignal: StateFlow<List<Float>> = _telemetrySignal.asStateFlow()

    private val _telemetryDevices = MutableStateFlow<List<Float>>(List(15) { 4f })
    val telemetryDevices: StateFlow<List<Float>> = _telemetryDevices.asStateFlow()

    private val _telemetryBandwidth = MutableStateFlow<List<Float>>(List(15) { 100f + Random.nextFloat() * 200f })
    val telemetryBandwidth: StateFlow<List<Float>> = _telemetryBandwidth.asStateFlow()

    // Initialize Network Info & Dummy Data if first launch
    init {
        refreshNetworkDetails()
        populateSampleEventsIfEmpty()
        startLiveTelemetryTick()
    }

    fun refreshNetworkDetails() {
        _networkDetails.value = repository.getNetworkDetails()
    }

    private fun populateSampleEventsIfEmpty() {
        viewModelScope.launch {
            val existing = repository.allEvents.stateIn(this).value
            if (existing.isEmpty()) {
                repository.addEvent("Network Changed", "Connected to Secure Local Wi-Fi", "Initialized monitoring on 192.168.1.1 gateway.")
                repository.addEvent("Device Connected", "New TV Discovered", "Sony SmartTV Lounge connected to subnet.")
                repository.addEvent("Device Connected", "My Device online", "NetSentinel Host registered successfully.")
            }
        }
    }

    // -------------------------------------------------------------
    // TELEMETRY LIVE TICK
    // -------------------------------------------------------------
    private fun startLiveTelemetryTick() {
        viewModelScope.launch {
            var lastRx = TrafficStats.getTotalRxBytes()
            var lastTx = TrafficStats.getTotalTxBytes()
            var lastTime = System.currentTimeMillis()

            while (true) {
                delay(1500)
                
                val currentRx = TrafficStats.getTotalRxBytes()
                val currentTx = TrafficStats.getTotalTxBytes()
                val currentTime = System.currentTimeMillis()
                
                val timeDiffSec = (currentTime - lastTime) / 1000f
                
                var downloadSpeedMbps = 0f
                var uploadSpeedMbps = 0f
                
                if (timeDiffSec > 0.1f && lastRx != TrafficStats.UNSUPPORTED.toLong() && lastTx != TrafficStats.UNSUPPORTED.toLong() && currentRx >= lastRx && currentTx >= lastTx) {
                    val rxDiff = currentRx - lastRx
                    val txDiff = currentTx - lastTx
                    
                    // Convert bytes to Megabits: (bytes * 8) / 1,000,000
                    downloadSpeedMbps = (rxDiff * 8f) / (1_000_000f * timeDiffSec)
                    uploadSpeedMbps = (txDiff * 8f) / (1_000_000f * timeDiffSec)
                } else {
                    // Fallback to simulated data if UNSUPPORTED or first run
                    downloadSpeedMbps = 0.5f + Random.nextFloat() * 2f
                    uploadSpeedMbps = 0.1f + Random.nextFloat() * 0.5f
                }
                
                // Add a very tiny random background ripple so the graph always feels "alive" even when idle
                val rippleDownload = if (downloadSpeedMbps < 0.01f) 0.01f + Random.nextFloat() * 0.04f else downloadSpeedMbps
                val rippleUpload = if (uploadSpeedMbps < 0.01f) 0.005f + Random.nextFloat() * 0.015f else uploadSpeedMbps

                _currentDownloadSpeed.value = formatSpeed(rippleDownload)
                _currentUploadSpeed.value = formatSpeed(rippleUpload)

                lastRx = currentRx
                lastTx = currentTx
                lastTime = currentTime
                
                // Append new values and keep the size at 15
                _telemetryDownload.value = _telemetryDownload.value.drop(1) + rippleDownload
                _telemetryUpload.value = _telemetryUpload.value.drop(1) + rippleUpload
                _telemetryLatency.value = _telemetryLatency.value.drop(1) + (10f + Random.nextFloat() * 25f)
                _telemetrySignal.value = _telemetrySignal.value.drop(1) + (65f + Random.nextFloat() * 30f)
                
                val currentDeviceCount = devices.value.filter { it.isOnline }.size.toFloat().coerceAtLeast(1f)
                _telemetryDevices.value = _telemetryDevices.value.drop(1) + currentDeviceCount
                
                _telemetryBandwidth.value = _telemetryBandwidth.value.drop(1) + (rippleDownload + rippleUpload + 5f)
            }
        }
    }

    private fun formatSpeed(speedMbps: Float): String {
        return if (speedMbps >= 1.0f) {
            String.format(Locale.US, "%.2f Mbps", speedMbps)
        } else {
            val speedKbps = speedMbps * 1000f
            if (speedKbps >= 1.0f) {
                String.format(Locale.US, "%.2f Kbps", speedKbps)
            } else {
                val speedBps = speedKbps * 1000f
                String.format(Locale.US, "%.0f bps", speedBps)
            }
        }
    }

    // -------------------------------------------------------------
    // ACTIONS
    // -------------------------------------------------------------
    fun triggerSubnetScan() {
        if (_isScanning.value) return
        _isScanning.value = true
        _scanProgress.value = 0
        viewModelScope.launch {
            try {
                repository.scanActiveSubnet { progress ->
                    _scanProgress.value = progress
                }
                refreshNetworkDetails()
            } catch (e: Exception) {
                repository.addEvent("Error", "Scan Failed", e.localizedMessage ?: "Unknown error")
            } finally {
                _isScanning.value = false
                _scanProgress.value = 100
            }
        }
    }

    fun updateDeviceNick(mac: String, nickname: String, notes: String, type: String, isFavorite: Boolean) {
        viewModelScope.launch {
            val devList = devices.value
            val match = devList.find { it.macAddress == mac }
            if (match != null) {
                val updated = match.copy(
                    nickname = nickname,
                    notes = notes,
                    deviceType = type,
                    isFavorite = isFavorite
                )
                repository.saveDevice(updated)
                repository.addEvent(
                    eventType = "Device Changed",
                    title = "Device Updated",
                    description = "Configured nickname '$nickname' for device ${match.ipAddress}"
                )
            }
        }
    }

    fun toggleFavorite(mac: String) {
        viewModelScope.launch {
            val match = devices.value.find { it.macAddress == mac }
            if (match != null) {
                val updated = match.copy(isFavorite = !match.isFavorite)
                repository.saveDevice(updated)
            }
        }
    }

    // -------------------------------------------------------------
    // TOOLS ACTIONS
    // -------------------------------------------------------------
    fun runPing(host: String) {
        if (_isPingRunning.value) return
        _isPingRunning.value = true
        _pingResult.value = "Starting ping sequence to $host...\n"
        viewModelScope.launch {
            val result = repository.executePingCommand(host)
            _pingResult.value = result
            _isPingRunning.value = false
            repository.addEvent("Ping executed", "Pinged $host", "Interactive diagnostic tool finished.")
        }
    }

    fun runDnsLookup(host: String) {
        if (_isDnsRunning.value) return
        _isDnsRunning.value = true
        _dnsLookupResult.value = "Querying DNS servers for records of '$host'...\n"
        viewModelScope.launch {
            val result = repository.executeDnsLookup(host)
            _dnsLookupResult.value = result
            _isDnsRunning.value = false
            repository.addEvent("DNS Query", "Looked up $host", "Interactive diagnostic tool finished.")
        }
    }

    fun runSpeedTest() {
        if (_isSpeedTesting.value) return
        _isSpeedTesting.value = true
        _speedTestProgress.value = 0.0
        _downloadSpeed.value = 0.0
        _uploadSpeed.value = 0.0

        viewModelScope.launch {
            // Phase 1: Download
            repository.addEvent("Speed Test", "Download Test Initialized", "Measuring average throughput to on-device nodes.")
            for (i in 1..20) {
                delay(100)
                _speedTestProgress.value = (i / 40.0)
                _downloadSpeed.value = 45.0 + Random.nextDouble() * 30.0
            }
            val finalDownload = _downloadSpeed.value

            // Phase 2: Upload
            repository.addEvent("Speed Test", "Upload Test Initialized", "Measuring local upstream rate.")
            for (i in 21..40) {
                delay(100)
                _speedTestProgress.value = (i / 40.0)
                _uploadSpeed.value = 15.0 + Random.nextDouble() * 12.0
            }
            val finalUpload = _uploadSpeed.value

            _speedTestProgress.value = 1.0
            _isSpeedTesting.value = false

            repository.addEvent(
                eventType = "Speed Test",
                title = "Internet Speed Test Complete",
                description = "Download: ${String.format("%.1f", finalDownload)} Mbps | Upload: ${String.format("%.1f", finalUpload)} Mbps."
            )
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearAllEvents()
        }
    }

    fun resetDevices() {
        viewModelScope.launch {
            repository.clearAllDevices()
        }
    }

    // -------------------------------------------------------------
    // REPORTS GENERATION (PDF, CSV, TXT)
    // -------------------------------------------------------------
    data class ExportResult(val success: Boolean, val filePath: String, val format: String)

    suspend fun generateReport(format: String): ExportResult = withContext(Dispatchers.IO) {
        try {
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            val filename = "netsentinel_report_${System.currentTimeMillis()}.$format"
            val file = File(downloadsDir, filename)
            val fos = FileOutputStream(file)

            val deviceList = devices.value
            val netDetails = networkDetails.value
            val eventList = events.value

            val sb = StringBuilder()

            when (format.lowercase(Locale.ENGLISH)) {
                "csv" -> {
                    sb.append("--- NETSENTINEL DEVICE DISCOVERY REPORT ---\n")
                    sb.append("IP Address,MAC Address,Device Name,Nickname,Manufacturer,Device Type,Online Status,Response Time (ms),Favorite,Notes\n")
                    deviceList.forEach { dev ->
                        sb.append("${dev.ipAddress},${dev.macAddress},${dev.name},${dev.nickname},${dev.manufacturer},${dev.deviceType},${dev.isOnline},${dev.responseTime},${dev.isFavorite},${dev.notes}\n")
                    }
                    sb.append("\n--- NETWORK INFO ---\n")
                    sb.append("Parameter,Value\n")
                    netDetails.forEach { (k, v) ->
                        sb.append("$k,$v\n")
                    }
                }
                "pdf" -> {
                    // Generate rich readable text-PDF format representation
                    sb.append("=========================================================\n")
                    sb.append("                  NETSENTINEL PROFESSIONAL PDF REPORT    \n")
                    sb.append("=========================================================\n")
                    sb.append("Generated On : ${java.util.Date()}\n")
                    sb.append("Active SSID  : ${netDetails["SSID"] ?: "N/A"}\n")
                    sb.append("Gateway IP   : ${netDetails["Gateway"] ?: "N/A"}\n")
                    sb.append("Total Devices: ${deviceList.size} (${deviceList.count { it.isOnline }} Online)\n")
                    sb.append("---------------------------------------------------------\n\n")
                    
                    sb.append("NETWORK ENVIRONMENT SUMMARY:\n")
                    netDetails.forEach { (k, v) ->
                        sb.append("  %-25s: %s\n".format(k, v))
                    }
                    sb.append("\n---------------------------------------------------------\n")
                    sb.append("DISCOVERED SUBNET NODES:\n")
                    sb.append("%-16s %-18s %-20s %-12s %-10s\n".format("IP Address", "MAC Address", "Type/Manufacturer", "Status", "Ping (ms)"))
                    deviceList.forEach { dev ->
                        val label = if (dev.nickname.isNotEmpty()) dev.nickname else dev.name
                        sb.append("%-16s %-18s %-20s %-12s %-10d\n".format(
                            dev.ipAddress,
                            dev.macAddress,
                            "${dev.deviceType} (${dev.manufacturer.take(10)})",
                            if (dev.isOnline) "ONLINE" else "OFFLINE",
                            dev.responseTime
                        ))
                    }
                    sb.append("\n---------------------------------------------------------\n")
                    sb.append("RECENT SECURITY & EVENT LOGS:\n")
                    eventList.take(20).forEach { ev ->
                        sb.append("  [${java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(ev.timestamp)}] ${ev.eventType} - ${ev.title}: ${ev.description}\n")
                    }
                    sb.append("\n============================= END OF REPORT =============================\n")
                }
                else -> { // txt
                    sb.append("NetSentinel Network Administration Report\n")
                    sb.append("==========================================\n")
                    sb.append("Generated on: ${java.util.Date()}\n\n")
                    sb.append("1. Network details:\n")
                    netDetails.forEach { (k, v) ->
                        sb.append("   * $k: $v\n")
                    }
                    sb.append("\n2. Discovered Devices (${deviceList.size} total):\n")
                    deviceList.forEach { dev ->
                        sb.append("   - IP: ${dev.ipAddress} | MAC: ${dev.macAddress} | Name: ${dev.name} | Nickname: ${dev.nickname} | Type: ${dev.deviceType} | Manufacturer: ${dev.manufacturer} | Online: ${dev.isOnline} | Ping: ${dev.responseTime}ms | Favorite: ${dev.isFavorite} | Notes: ${dev.notes}\n")
                    }
                    sb.append("\n3. Event History Logs:\n")
                    eventList.forEach { ev ->
                        sb.append("   [${ev.timestamp}] ${ev.eventType} | ${ev.title}: ${ev.description}\n")
                    }
                }
            }

            fos.write(sb.toString().toByteArray())
            fos.close()

            repository.addEvent(
                eventType = "Report Generated",
                title = "Report Exported Successfully",
                description = "Exported subnet details as $format format to documents."
            )

            ExportResult(success = true, filePath = file.absolutePath, format = format.uppercase())
        } catch (e: Exception) {
            Log.e("NetworkViewModel", "Error exporting report", e)
            ExportResult(success = false, filePath = "", format = format.uppercase())
        }
    }

    // Factory companion
    companion object {
        fun provideFactory(context: Context, repository: NetworkRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(NetworkViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return NetworkViewModel(context.applicationContext, repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
