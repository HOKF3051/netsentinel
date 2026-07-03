package com.example.worker

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.NetSentinelApplication
import java.net.InetAddress

class NetworkMonitorWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("NetworkMonitorWorker", "Running background network monitoring...")
        
        val app = applicationContext as? NetSentinelApplication ?: return Result.failure()
        val repository = app.container.repository

        try {
            // 1. Get current network details
            val details = repository.getNetworkDetails()
            val ssid = details["SSID"] ?: "Not Connected"
            val signalLevel = details["Signal Level"]?.toIntOrNull() ?: 100
            val gateway = details["Gateway"] ?: "192.168.1.1"

            // 2. Perform connection checks
            val isGatewayReachable = try {
                val addr = InetAddress.getByName(gateway)
                addr.isReachable(2000)
            } catch (e: Exception) {
                false
            }

            // 3. Log events and send notifications based on findings
            if (ssid == "Not Connected" || !isGatewayReachable) {
                repository.addEvent(
                    eventType = "Connection Lost",
                    title = "Network Connection Interrupted",
                    description = "Gateway $gateway is unreachable or Wi-Fi is disconnected."
                )
                showNotification(
                    id = 101,
                    title = "Connection Lost",
                    message = "Your on-device gateway is currently unreachable. Check your Wi-Fi settings."
                )
            } else if (signalLevel < 35) { // e.g. level < 35 out of 100
                repository.addEvent(
                    eventType = "Signal Drops",
                    title = "Weak Wi-Fi Signal",
                    description = "Wi-Fi signal dropped below acceptable levels ($signalLevel%)."
                )
                showNotification(
                    id = 102,
                    title = "Weak Wi-Fi Signal",
                    message = "Your connection is weak ($signalLevel%). You may experience high latency."
                )
            }

            // 4. Background check for new devices
            // To be efficient, we scan the subset and check if there's any device change
            // But since subnet scan takes a few seconds, we can run it, or log a simple heartbeat.
            Log.d("NetworkMonitorWorker", "Background network health check successful for SSID: $ssid")
            return Result.success()
        } catch (e: Exception) {
            Log.e("NetworkMonitorWorker", "Error in background monitor worker", e)
            return Result.retry()
        }
    }

    private fun showNotification(id: Int, title: String, message: String) {
        // Check permission if on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (applicationContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("NetworkMonitorWorker", "Missing notification permission, skipping notification show.")
                return
            }
        }

        val builder = NotificationCompat.Builder(applicationContext, "netsentinel_alerts")
            .setSmallIcon(android.R.drawable.stat_notify_chat) // Standard android fallback icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(applicationContext)
            notificationManager.notify(id, builder.build())
        } catch (e: SecurityException) {
            Log.e("NetworkMonitorWorker", "Security exception posting notification", e)
        }
    }
}
