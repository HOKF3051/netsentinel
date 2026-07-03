package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.DeviceEntity
import com.example.data.database.NetworkEventEntity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCoral
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberSapphire
import com.example.ui.theme.CyberIndigo
import com.example.ui.theme.TextGray
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DeviceFilter
import com.example.ui.viewmodel.DeviceSort
import com.example.ui.viewmodel.NetworkViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// -------------------------------------------------------------
// BILINGUAL TRANSLATION MAP
// -------------------------------------------------------------
fun t(key: String, lang: String): String {
    val mapAr = mapOf(
        "title" to "صافي الحارس (NetSentinel)",
        "dashboard" to "الرئيسية",
        "devices" to "الأجهزة",
        "tools" to "الأدوات",
        "monitor" to "مراقب حي",
        "logs" to "التقارير",
        "settings" to "الإعدادات",
        "network_details" to "تفاصيل الشبكة",
        "local_ip" to "عنوان IP المحلي",
        "gateway" to "البوابة",
        "dns_servers" to "خوادم DNS",
        "wifi_frequency" to "تردد الواي فاي",
        "wifi_standard" to "معيار الواي فاي",
        "signal_strength" to "قوة الإشارة",
        "download_speed" to "سرعة التنزيل",
        "upload_speed" to "سرعة الرفع",
        "ping" to "البنج (Ping)",
        "jitter" to "التشتت (Jitter)",
        "packet_loss" to "فقدان الحزم",
        "connected_devices" to "الأجهزة المتصلة",
        "scan_subnet" to "فحص الشبكة",
        "scanning" to "جاري الفحص المباشر للشبكة...",
        "search" to "بحث عن الأجهزة والسجلات والتقارير...",
        "sort_by" to "ترتيب حسب",
        "filter_by" to "تصفية حسب",
        "no_devices" to "لا توجد أجهزة متصلة بالشبكة حالياً",
        "favorite" to "المفضلة",
        "device_type" to "نوع الجهاز",
        "online" to "متصل",
        "offline" to "غير متصل",
        "last_seen" to "آخر ظهور",
        "response_time" to "زمن الاستجابة",
        "edit_device" to "تعديل تفاصيل الجهاز",
        "save" to "حفظ التغييرات",
        "cancel" to "إلغاء",
        "nickname" to "الاسم المستعار للجهاز",
        "notes" to "ملاحظات إضافية",
        "diagnostics" to "الأدوات والتشخيصات",
        "ping_diagnostic" to "تشخيص زمن الاستجابة (Ping)",
        "dns_lookup" to "بحث سجلات DNS",
        "run_test" to "بدء الاختبار",
        "speed_test" to "اختبار سرعة الإنترنت",
        "bandwidth" to "مراقب عرض النطاق الترددي المباشر",
        "history_logs" to "سجل الأحداث والتقارير",
        "clear_all" to "مسح كل السجلات",
        "export_as" to "تصدير التقرير بصيغة",
        "theme" to "وضع السمات",
        "amoled" to "وضع الشاشات الفائقة (AMOLED)",
        "language" to "لغة التطبيق",
        "notifications" to "تنبيهات الخلفية",
        "interval" to "فترة المراقبة بالخلفية",
        "accent" to "تخصيص لون التأكيد",
        "global_search" to "البحث الشامل للشبكة",
        "all_devices" to "كل الأجهزة",
        "routers" to "أجهزة الراوتر",
        "phones" to "الهواتف المحمولة",
        "laptops" to "أجهزة اللابتوب",
        "tvs" to "الشاشات الذكية",
        "printers" to "الطابعات",
        "favorites_only" to "المفضلة فقط",
        "online_only" to "المتصلة فقط"
    )

    if (lang == "AR") {
        return mapAr[key] ?: key
    }
    return when (key) {
        "title" -> "NetSentinel"
        "dashboard" -> "Dashboard"
        "devices" -> "Devices"
        "tools" -> "Tools"
        "monitor" -> "Live Monitor"
        "logs" -> "Reports"
        "settings" -> "Settings"
        "network_details" -> "Network Details"
        "local_ip" -> "Local IP"
        "gateway" -> "Gateway"
        "dns_servers" -> "DNS Servers"
        "wifi_frequency" -> "Wi-Fi Frequency"
        "wifi_standard" -> "Wi-Fi Standard"
        "signal_strength" -> "Signal Strength"
        "download_speed" -> "Download Speed"
        "upload_speed" -> "Upload Speed"
        "ping" -> "Ping"
        "jitter" -> "Jitter"
        "packet_loss" -> "Packet Loss"
        "connected_devices" -> "Connected Devices"
        "scan_subnet" -> "Scan Subnet"
        "scanning" -> "Scanning Network Subnet..."
        "search" -> "Global search devices, reports, history..."
        "sort_by" -> "Sort By"
        "filter_by" -> "Filter By"
        "no_devices" -> "No devices registered in local subnet"
        "favorite" -> "Favorites"
        "device_type" -> "Device Type"
        "online" -> "Online"
        "offline" -> "Offline"
        "last_seen" -> "Last Seen"
        "response_time" -> "Response Time"
        "edit_device" -> "Edit Device Profile"
        "save" -> "Save Changes"
        "cancel" -> "Cancel"
        "nickname" -> "Device Nickname / Custom Name"
        "notes" -> "Notes"
        "diagnostics" -> "Diagnostics"
        "ping_diagnostic" -> "Ping Diagnostic tool"
        "dns_lookup" -> "DNS Domain Lookup"
        "run_test" -> "Run Test"
        "speed_test" -> "Internet Speed Test"
        "bandwidth" -> "Real-time Telemetry Monitor"
        "history_logs" -> "Event History & Reporting"
        "clear_all" -> "Clear Logs"
        "export_as" -> "Export Report as"
        "theme" -> "Display Mode"
        "amoled" -> "AMOLED Blackout Mode"
        "language" -> "App Language"
        "notifications" -> "Background Alerts"
        "interval" -> "Auto-Scan Frequency"
        "accent" -> "System Accent Color"
        "global_search" -> "NetSentinel Global Search"
        "all_devices" -> "All Devices"
        "routers" -> "Routers"
        "phones" -> "Phones"
        "laptops" -> "Laptops"
        "tvs" -> "Smart TVs"
        "printers" -> "Printers"
        "favorites_only" -> "Favorites Only"
        "online_only" -> "Online Only"
        else -> key.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

// -------------------------------------------------------------
// MAIN ENTRY COMPOSE SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: NetworkViewModel) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val amoledMode = themeMode == "AMOLED"
    val isDark = themeMode == "DARK" || themeMode == "AMOLED"
    val accentName by viewModel.themeAccent.collectAsStateWithLifecycle()
    val currentLang by viewModel.language.collectAsStateWithLifecycle()

    val isRtl = currentLang == "AR"

    MyApplicationTheme(
        darkTheme = isDark,
        amoledMode = amoledMode,
        accentColorName = accentName
    ) {
        val accentColor = MaterialTheme.colorScheme.primary

        CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides if (isRtl) {
                androidx.compose.ui.unit.LayoutDirection.Rtl
            } else {
                androidx.compose.ui.unit.LayoutDirection.Ltr
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                    .drawBehind {
                        // Ambient glowing lights for standard glassmorphism blur representation
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(accentColor.copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(size.width * 0.85f, size.height * 0.15f),
                                radius = size.width * 0.7f
                            ),
                            center = Offset(size.width * 0.85f, size.height * 0.15f),
                            radius = size.width * 0.7f
                        )
                        
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(CyberIndigo.copy(alpha = 0.18f), Color.Transparent),
                                center = Offset(size.width * 0.15f, size.height * 0.55f),
                                radius = size.width * 0.6f
                            ),
                            center = Offset(size.width * 0.15f, size.height * 0.55f),
                            radius = size.width * 0.6f
                        )

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(CyberCoral.copy(alpha = 0.12f), Color.Transparent),
                                center = Offset(size.width * 0.8f, size.height * 0.85f),
                                radius = size.width * 0.6f
                            ),
                            center = Offset(size.width * 0.8f, size.height * 0.85f),
                            radius = size.width * 0.6f
                        )
                    }
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Shield logo",
                                        tint = accentColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = t("title", currentLang),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF0F172A).copy(alpha = 0.40f),
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            actions = {
                                IconButton(onClick = { viewModel.refreshNetworkDetails() }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh details",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            modifier = Modifier.border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                                ),
                                shape = RoundedCornerShape(0.dp)
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF0F172A).copy(alpha = 0.45f),
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.10f), Color.Transparent)
                                    ),
                                    shape = RoundedCornerShape(0.dp)
                                )
                        ) {
                            val tabs = listOf(
                                Triple("DASHBOARD", Icons.Default.Dashboard, "dashboard"),
                                Triple("DISCOVERY", Icons.Default.Devices, "devices"),
                                Triple("TOOLS", Icons.Default.Build, "tools"),
                                Triple("LIVE_MONITOR", Icons.Default.Timeline, "monitor"),
                                Triple("REPORTS", Icons.Default.Assessment, "logs"),
                                Triple("SETTINGS", Icons.Default.Settings, "settings")
                            )

                            tabs.forEach { (tabId, icon, labelKey) ->
                                NavigationBarItem(
                                    selected = activeTab == tabId,
                                    onClick = { viewModel.setActiveTab(tabId) },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = tabId
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = t(labelKey, currentLang),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 10.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = accentColor,
                                        indicatorColor = accentColor,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    ),
                                    modifier = Modifier.testTag("nav_item_${tabId.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (activeTab) {
                            "DASHBOARD" -> DashboardTab(viewModel, currentLang, accentColor)
                            "DISCOVERY" -> DiscoveryTab(viewModel, currentLang, accentColor)
                            "TOOLS" -> ToolsTab(viewModel, currentLang, accentColor)
                            "LIVE_MONITOR" -> LiveMonitorTab(viewModel, currentLang, accentColor)
                            "REPORTS" -> ReportsTab(viewModel, currentLang, accentColor)
                            "SETTINGS" -> SettingsTab(viewModel, currentLang, accentColor)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: HOME DASHBOARD
// -------------------------------------------------------------
@Composable
fun DashboardTab(viewModel: NetworkViewModel, lang: String, accentColor: Color) {
    val networkDetails by viewModel.networkDetails.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    val devices by viewModel.devices.collectAsStateWithLifecycle()

    val onlineCount = devices.count { it.isOnline }

    val ssid = networkDetails["SSID"] ?: "Secure Wi-Fi"
    val localIp = networkDetails["Local IP Address"] ?: "192.168.1.102"
    val gateway = networkDetails["Gateway"] ?: "192.168.1.1"
    val dns = networkDetails["DNS"] ?: "8.8.8.8"
    val frequency = networkDetails["Wi-Fi Frequency"] ?: "5745 MHz"
    val band = networkDetails["Band"] ?: "5 GHz"
    val standard = networkDetails["Wi-Fi Standard"] ?: "Wi-Fi 6 (802.11ax)"
    val signal = networkDetails["Signal Strength"] ?: "-42 dBm"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Glassmorphism Hero Status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = accentColor.copy(alpha = 0.22f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(40.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Wifi",
                        tint = accentColor,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = ssid,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "${t("local_ip", lang)}: $localIp",
                    color = TextGray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Scan Action Button
                Button(
                    onClick = { viewModel.triggerSubnetScan() },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("subnet_scan_button"),
                    enabled = !isScanning
                ) {
                    if (isScanning) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${t("scanning", lang)} ($scanProgress%)",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.YoutubeSearchedFor,
                                contentDescription = "Scan icon",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = t("scan_subnet", lang),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Live Scanning Progress Bar
        if (isScanning) {
            LinearProgressIndicator(
                progress = { scanProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.2f),
            )
        }

        // 2x2 Grid of Animated Telemetry Cards
        Text(
            text = t("network_details", lang),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 380.dp)
        ) {
            val telemetryItems = listOf(
                Quadruple(t("gateway", lang), gateway, Icons.Default.Router, "Gateway IP"),
                Quadruple(t("dns_servers", lang), dns.split(",").firstOrNull() ?: "8.8.8.8", Icons.Default.Dns, "DNS IP"),
                Quadruple(t("wifi_frequency", lang), "$frequency ($band)", Icons.Default.Waves, "Frequency"),
                Quadruple(t("wifi_standard", lang), standard.substringBefore(" ("), Icons.Default.SettingsInputAntenna, "Standard"),
                Quadruple(t("signal_strength", lang), signal, Icons.Default.SignalCellularAlt, "RSSI"),
                Quadruple(t("connected_devices", lang), "$onlineCount ${t("online", lang)}", Icons.Default.People, "Devices")
            )

            items(telemetryItems) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = item.third,
                                contentDescription = item.fourth,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.first,
                                fontSize = 11.sp,
                                color = TextGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.second,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Live Simulated Download/Upload speeds cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, CyberEmerald.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Download",
                        tint = CyberEmerald,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = t("download_speed", lang), fontSize = 11.sp, color = TextGray)
                        Text(text = "84.2 Mbps", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CyberEmerald)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, CyberCyan.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Upload",
                        tint = CyberCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = t("upload_speed", lang), fontSize = 11.sp, color = TextGray)
                        Text(text = "24.5 Mbps", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CyberCyan)
                    }
                }
            }
        }
    }
}

// Helper Class
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// -------------------------------------------------------------
// TAB 2: DEVICE DISCOVERY
// -------------------------------------------------------------
@Composable
fun DiscoveryTab(viewModel: NetworkViewModel, lang: String, accentColor: Color) {
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.deviceSortBy.collectAsStateWithLifecycle()
    val filterOption by viewModel.deviceFilterBy.collectAsStateWithLifecycle()

    var activeEditingDevice by remember { mutableStateOf<DeviceEntity?>(null) }

    // Dynamic Filter & Sort Logic
    val processedDevices = remember(devices, searchQuery, sortOption, filterOption) {
        devices.filter { dev ->
            val matchSearch = dev.ipAddress.contains(searchQuery, ignoreCase = true) ||
                    dev.macAddress.contains(searchQuery, ignoreCase = true) ||
                    dev.name.contains(searchQuery, ignoreCase = true) ||
                    dev.nickname.contains(searchQuery, ignoreCase = true) ||
                    dev.manufacturer.contains(searchQuery, ignoreCase = true)
            
            val matchFilter = when (filterOption) {
                DeviceFilter.ALL -> true
                DeviceFilter.PHONES -> dev.deviceType.equals("Phone", ignoreCase = true)
                DeviceFilter.LAPTOPS -> dev.deviceType.equals("Laptop", ignoreCase = true)
                DeviceFilter.ROUTERS -> dev.deviceType.equals("Router", ignoreCase = true)
                DeviceFilter.TVS -> dev.deviceType.equals("TV", ignoreCase = true)
                DeviceFilter.PRINTERS -> dev.deviceType.equals("Printer", ignoreCase = true)
                DeviceFilter.FAVORITES -> dev.isFavorite
                DeviceFilter.ONLINE -> dev.isOnline
            }

            matchSearch && matchFilter
        }.sortedWith { d1, d2 ->
            when (sortOption) {
                DeviceSort.IP_ADDRESS -> {
                    val p1 = d1.ipAddress.split(".").mapNotNull { it.toIntOrNull() }
                    val p2 = d2.ipAddress.split(".").mapNotNull { it.toIntOrNull() }
                    if (p1.size == 4 && p2.size == 4) {
                        for (i in 0..3) {
                            if (p1[i] != p2[i]) return@sortedWith p1[i].compareTo(p2[i])
                        }
                    }
                    d1.ipAddress.compareTo(d2.ipAddress)
                }
                DeviceSort.NAME -> d1.nickname.ifEmpty { d1.name }.compareTo(d2.nickname.ifEmpty { d2.name }, ignoreCase = true)
                DeviceSort.RESPONSE_TIME -> d2.responseTime.compareTo(d1.responseTime) // Highest latency first
                DeviceSort.LAST_SEEN -> d2.lastSeen.compareTo(d1.lastSeen)
                DeviceSort.FAVORITE -> d2.isFavorite.compareTo(d1.isFavorite)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text(text = t("search", lang), fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("device_search_input"),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        // Sorting & Filtering Options Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sort Dropdown Trigger
            var sortMenuExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { sortMenuExpanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort icon", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = t("sort_by", lang),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    DeviceSort.entries.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt.name.replace("_", " ")) },
                            onClick = {
                                viewModel.setSortBy(opt)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Filter Dropdown Trigger
            var filterMenuExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { filterMenuExpanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter icon", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = t("filter_by", lang),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                DropdownMenu(
                    expanded = filterMenuExpanded,
                    onDismissRequest = { filterMenuExpanded = false }
                ) {
                    DeviceFilter.entries.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(t(opt.name.lowercase() + "s", lang)) },
                            onClick = {
                                viewModel.setFilterBy(opt)
                                filterMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Active filters list
        Text(
            text = "${t("devices", lang)} (${processedDevices.size})",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = TextGray
        )

        // Device Scroll List
        if (processedDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DeveloperMode,
                        contentDescription = "No device icon",
                        tint = TextGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = t("no_devices", lang),
                        color = TextGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(processedDevices, key = { it.macAddress }) { dev ->
                    DeviceRow(
                        device = dev,
                        accentColor = accentColor,
                        onEditClicked = { activeEditingDevice = dev },
                        onFavoriteToggled = { viewModel.toggleFavorite(dev.macAddress) }
                    )
                }
            }
        }
    }

    // Editing Dialog Overlay
    if (activeEditingDevice != null) {
        EditDeviceDialog(
            device = activeEditingDevice!!,
            lang = lang,
            accentColor = accentColor,
            onDismiss = { activeEditingDevice = null },
            onSave = { nick, notes, type, isFav ->
                viewModel.updateDeviceNick(activeEditingDevice!!.macAddress, nick, notes, type, isFav)
                activeEditingDevice = null
            }
        )
    }
}

@Composable
fun DeviceRow(
    device: DeviceEntity,
    accentColor: Color,
    onEditClicked: () -> Unit,
    onFavoriteToggled: () -> Unit
) {
    val indicatorColor by animateColorAsState(
        targetValue = if (device.isOnline) CyberEmerald else TextGray.copy(alpha = 0.5f),
        animationSpec = tween(300)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClicked() }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Online Status dot & Device Category Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(accentColor.copy(alpha = 0.08f), RoundedCornerShape(23.dp)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (device.deviceType.lowercase(Locale.ENGLISH)) {
                    "router" -> Icons.Default.Router
                    "tv" -> Icons.Default.Tv
                    "camera" -> Icons.Default.Videocam
                    "printer" -> Icons.Default.Print
                    "laptop" -> Icons.Default.Laptop
                    else -> Icons.Default.PhoneAndroid
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Device Category",
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
                
                // Status dot indicator overlay
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .background(Color.Black, RoundedCornerShape(5.5.dp))
                        .align(Alignment.BottomEnd)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(indicatorColor, RoundedCornerShape(4.dp))
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.nickname.ifEmpty { device.name },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (device.isFavorite) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite star",
                            tint = CyberCoral,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = device.ipAddress,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = accentColor
                )
                Text(
                    text = "${device.manufacturer} • ${device.macAddress}",
                    fontSize = 11.sp,
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Latency / Ping details or Favorite Icon button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (device.isOnline) {
                    Text(
                        text = "${device.responseTime} ms",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = CyberEmerald
                    )
                } else {
                    Text(
                        text = "Offline",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                IconButton(
                    onClick = onFavoriteToggled,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (device.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (device.isFavorite) CyberCoral else TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditDeviceDialog(
    device: DeviceEntity,
    lang: String,
    accentColor: Color,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean) -> Unit
) {
    var nickname by remember { mutableStateOf(device.nickname) }
    var notes by remember { mutableStateOf(device.notes) }
    var deviceType by remember { mutableStateOf(device.deviceType) }
    var isFav by remember { mutableStateOf(device.isFavorite) }

    val categories = listOf("Phone", "Laptop", "Router", "TV", "Camera", "Printer", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = t("edit_device", lang), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text(t("nickname", lang)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(t("notes", lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Category Selector
                Text(text = t("device_type", lang), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(4).forEach { cat ->
                        val isSelected = deviceType.equals(cat, ignoreCase = true)
                        SuggestionChip(
                            onClick = { deviceType = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
                                labelColor = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                // Favorite Checkbox Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFav = !isFav },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isFav, onCheckedChange = { isFav = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = t("favorite", lang))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nickname, notes, deviceType, isFav) },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(text = t("save", lang), color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = t("cancel", lang))
            }
        }
    )
}

// -------------------------------------------------------------
// TAB 3: DIAGNOSTICS & NETWORK TOOLS
// -------------------------------------------------------------
@Composable
fun ToolsTab(viewModel: NetworkViewModel, lang: String, accentColor: Color) {
    val pingResult by viewModel.pingResult.collectAsStateWithLifecycle()
    val isPingRunning by viewModel.isPingRunning.collectAsStateWithLifecycle()

    val dnsResult by viewModel.dnsLookupResult.collectAsStateWithLifecycle()
    val isDnsRunning by viewModel.isDnsRunning.collectAsStateWithLifecycle()

    val isSpeedTesting by viewModel.isSpeedTesting.collectAsStateWithLifecycle()
    val speedProgress by viewModel.speedTestProgress.collectAsStateWithLifecycle()
    val downloadSpeed by viewModel.downloadSpeed.collectAsStateWithLifecycle()
    val uploadSpeed by viewModel.uploadSpeed.collectAsStateWithLifecycle()

    var pingHost by remember { mutableStateOf("8.8.8.8") }
    var dnsDomain by remember { mutableStateOf("google.com") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = t("diagnostics", lang),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        // TOOL 1: SPEED TEST
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = "Speed test", tint = CyberEmerald, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = t("speed_test", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(14.dp))

                if (isSpeedTesting) {
                    LinearProgressIndicator(
                        progress = { speedProgress.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyberEmerald,
                        trackColor = CyberEmerald.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Download", fontSize = 12.sp, color = TextGray)
                        Text(text = "${String.format("%.1f", downloadSpeed)} Mbps", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = CyberEmerald)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Upload", fontSize = 12.sp, color = TextGray)
                        Text(text = "${String.format("%.1f", uploadSpeed)} Mbps", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = CyberCyan)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { viewModel.runSpeedTest() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSpeedTesting
                ) {
                    Text(text = t("run_test", lang), color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // TOOL 2: PING DIAGNOSTIC
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.NetworkPing, contentDescription = "Ping diagnostic", tint = accentColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = t("ping_diagnostic", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = pingHost,
                        onValueChange = { pingHost = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor)
                    )

                    Button(
                        onClick = { viewModel.runPing(pingHost) },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isPingRunning
                    ) {
                        if (isPingRunning) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(text = t("run_test", lang), color = Color.Black)
                        }
                    }
                }

                if (pingResult.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = pingResult,
                            color = CyberEmerald,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // TOOL 3: DNS LOOKUP
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = "DNS Lookup", tint = CyberSapphire, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = t("dns_lookup", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = dnsDomain,
                        onValueChange = { dnsDomain = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberSapphire)
                    )

                    Button(
                        onClick = { viewModel.runDnsLookup(dnsDomain) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSapphire),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isDnsRunning
                    ) {
                        if (isDnsRunning) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(text = t("run_test", lang), color = Color.White)
                        }
                    }
                }

                if (dnsResult.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = dnsResult,
                            color = CyberCyan,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: LIVE TELEMETRY GRAPHS
// -------------------------------------------------------------
@Composable
fun LiveMonitorTab(viewModel: NetworkViewModel, lang: String, accentColor: Color) {
    val downloadData by viewModel.telemetryDownload.collectAsStateWithLifecycle()
    val uploadData by viewModel.telemetryUpload.collectAsStateWithLifecycle()
    val latencyData by viewModel.telemetryLatency.collectAsStateWithLifecycle()
    val currentDownload by viewModel.currentDownloadSpeed.collectAsStateWithLifecycle()
    val currentUpload by viewModel.currentUploadSpeed.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = t("bandwidth", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            // Pulsing / Live TrafficStats Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(CyberEmerald, CircleShape)
                )
                Text(
                    text = "TRAFFICSTATS API ACTIVE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Graph 1: Download (Green wave)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = t("download_speed", lang), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        text = currentDownload,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = CyberEmerald,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                TelemetryLineChart(
                    dataPoints = downloadData,
                    accentColor = CyberEmerald,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }

        // Graph 2: Upload (Blue wave)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = t("upload_speed", lang), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        text = currentUpload,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                TelemetryLineChart(
                    dataPoints = uploadData,
                    accentColor = CyberCyan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }

        // Graph 3: Latency (Coral wave)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "${t("ping", lang)} (ms)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                TelemetryLineChart(
                    dataPoints = latencyData,
                    accentColor = CyberCoral,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// RESPONSIVE TELEMETRY LINE CHART WITH CANVAS
// -------------------------------------------------------------
@Composable
fun TelemetryLineChart(
    dataPoints: List<Float>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val points = dataPoints.takeLast(12)
    if (points.isEmpty()) return

    val maxVal = (points.maxOrNull() ?: 10f).coerceAtLeast(10f) * 1.15f

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (points.size - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value / maxVal) * height

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Draw Area Gradient Fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Draw Spline Stroke Line
        drawPath(
            path = path,
            color = accentColor,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw Grid Lines (Professional networking scope)
        val gridLines = 3
        for (i in 1..gridLines) {
            val y = (height / (gridLines + 1)) * i
            drawLine(
                color = Color.Gray.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

// -------------------------------------------------------------
// TAB 5: REPORTS & LOGS
// -------------------------------------------------------------
@Composable
fun ReportsTab(viewModel: NetworkViewModel, lang: String, accentColor: Color) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = t("history_logs", lang),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Export Action Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = t("export_as", lang),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("PDF", "CSV", "TXT").forEach { format ->
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val result = viewModel.generateReport(format)
                                    if (result.success) {
                                        Toast.makeText(context, "Exported successfully: ${result.filePath}", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Export Failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = format, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "System Events (${events.size})",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )

            TextButton(onClick = { viewModel.clearLogs() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = t("clear_all", lang), color = CyberCoral)
                }
            }
        }

        // Logs Scroll Area
        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No logs recorded yet.", color = TextGray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(events) { ev ->
                    LogEventRow(ev)
                }
            }
        }
    }
}

@Composable
fun LogEventRow(event: NetworkEventEntity) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val formattedTime = remember(event.timestamp) { formatter.format(Date(event.timestamp)) }

    val tintColor = when (event.eventType) {
        "Connection Lost", "Signal Drops" -> CyberCoral
        "Network Scanned", "Report Generated" -> CyberEmerald
        else -> CyberCyan
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(tintColor, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = event.description,
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formattedTime,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TextGray
            )
        }
    }
}

// -------------------------------------------------------------
// TAB 6: SETTINGS
// -------------------------------------------------------------
@Composable
fun SettingsTab(viewModel: NetworkViewModel, lang: String, accentColor: Color) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val notificationEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val interval by viewModel.scanInterval.collectAsStateWithLifecycle()
    val accentName by viewModel.themeAccent.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = t("settings", lang),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        // SECTION 1: DISPLAY MODE
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = t("theme", lang), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themeModes = listOf("DARK", "LIGHT", "AMOLED")
                    themeModes.forEach { mode ->
                        val isSelected = themeMode == mode
                        Button(
                            onClick = { viewModel.setThemeMode(mode) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = mode,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // SECTION 2: ACCENT CUSTOMIZATION
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = t("accent", lang), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val colors = listOf("Indigo", "Cyan", "Emerald", "Sapphire", "Coral")
                    colors.forEach { cName ->
                        val isSelected = accentName == cName
                        val colorVec = when (cName) {
                            "Indigo" -> CyberIndigo
                            "Cyan" -> CyberCyan
                            "Emerald" -> CyberEmerald
                            "Sapphire" -> CyberSapphire
                            "Coral" -> CyberCoral
                            else -> CyberIndigo
                        }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(21.dp))
                                .background(colorVec)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    shape = RoundedCornerShape(21.dp)
                                )
                                .clickable { viewModel.setThemeAccent(cName) }
                        )
                    }
                }
            }
        }

        // SECTION 3: APP LANGUAGE
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = t("language", lang), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val langs = listOf("EN", "AR")
                    langs.forEach { l ->
                        val isSelected = lang == l
                        Button(
                            onClick = { viewModel.setLanguage(l) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (l == "EN") "English" else "العربية",
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // SECTION 4: ALERT NOTIFICATIONS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = t("notifications", lang), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Alert on connection drop / changes", fontSize = 11.sp, color = TextGray)
                }
                Switch(
                    checked = notificationEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                )
            }
        }

        // SECTION 5: AUTO-SCAN FREQUENCY
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = t("interval", lang), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val options = listOf(15, 30, 60)
                    options.forEach { opt ->
                        val isSelected = interval == opt
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setScanInterval(opt) },
                            label = { Text("$opt Min") }
                        )
                    }
                }
            }
        }

        // SECTION 6: DATA ADMIN
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Data Management", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberCoral)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.resetDevices() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Reset Devices", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { viewModel.clearLogs() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Reset Events", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
