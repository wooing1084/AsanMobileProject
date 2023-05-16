package com.example.asanmobile

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Sensor::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sensorDao(): SensorDAO
}