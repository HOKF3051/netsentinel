package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worker.NetworkMonitorWorker
import java.util.concurrent.TimeUnit

class NetSentinelApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        
        createNotificationChannels()
        setupBackgroundMonitoring()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "NetSentinel Alerts"
            val descriptionText = "Notifications for device changes, connection drops, and signal strength alerts."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("netsentinel_alerts", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupBackgroundMonitoring() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val monitorRequest = PeriodicWorkRequestBuilder<NetworkMonitorWorker>(
            15, TimeUnit.MINUTES // Minimum interval allowed by Android WorkManager
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "netsentinel_network_monitor",
            ExistingPeriodicWorkPolicy.KEEP,
            monitorRequest
        )
    }
}

class AppContainer(private val context: Context) {
    val database: com.example.data.database.NetSentinelDatabase by lazy {
        com.example.data.database.NetSentinelDatabase.getDatabase(context)
    }

    val repository: com.example.data.repository.NetworkRepository by lazy {
        com.example.data.repository.NetworkRepository(
            context = context,
            deviceDao = database.deviceDao(),
            eventDao = database.eventDao()
        )
    }
}
