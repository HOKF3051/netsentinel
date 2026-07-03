package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DeviceEntity::class, NetworkEventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NetSentinelDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: NetSentinelDatabase? = null

        fun getDatabase(context: Context): NetSentinelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NetSentinelDatabase::class.java,
                    "netsentinel_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
