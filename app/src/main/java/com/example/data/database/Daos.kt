package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY lastSeen DESC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE macAddress = :macAddress LIMIT 1")
    suspend fun getDeviceByMac(macAddress: String): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<DeviceEntity>)

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE macAddress = :macAddress")
    suspend fun deleteDevice(macAddress: String)

    @Query("DELETE FROM devices")
    suspend fun deleteAllDevices()
}

@Dao
interface EventDao {
    @Query("SELECT * FROM network_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<NetworkEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: NetworkEventEntity)

    @Query("DELETE FROM network_events")
    suspend fun deleteAllEvents()
}
