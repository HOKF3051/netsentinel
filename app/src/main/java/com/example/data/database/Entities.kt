package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val macAddress: String,
    val ipAddress: String,
    val name: String,
    val nickname: String = "",
    val manufacturer: String = "Unknown",
    val deviceType: String = "Phone", // Phone, Laptop, Router, TV, Camera, Printer, etc.
    val isOnline: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis(),
    val responseTime: Long = 0,
    val isFavorite: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "network_events")
data class NetworkEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventType: String, // Device Connected, Device Disconnected, Network Changed, Wi-Fi Changed, Signal Drops, Connection Lost
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val description: String
)
