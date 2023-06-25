package com.example.asanmobile.sensor

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.asanmobile.sensor.dao.HeartRateDao
import com.example.asanmobile.sensor.dao.PpgGreenDao
import com.example.asanmobile.sensor.model.HeartRate
import com.example.asanmobile.sensor.model.PpgGreen

@Database(entities = [HeartRate::class, PpgGreen::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun heartRateDao(): HeartRateDao
    abstract fun ppgGreenDao(): PpgGreenDao

    // 싱글톤 구현
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "sensor"
                    ).build()
                }
            }
            INSTANCE!!.openHelper.readableDatabase
            return INSTANCE
        }
    }
}