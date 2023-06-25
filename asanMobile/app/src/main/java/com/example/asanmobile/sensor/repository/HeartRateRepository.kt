package com.example.asanmobile.sensor.repository

import android.content.Context
import com.example.asanmobile.sensor.AppDatabase
import com.example.asanmobile.sensor.dao.HeartRateDao
import com.example.asanmobile.sensor.model.HeartRate
import com.example.asanmobile.sensor.model.Sensor

class HeartRateRepository(context: Context) {

    private val heartRateDao: HeartRateDao
    private val db: AppDatabase

    init {
        db = AppDatabase.getInstance(context)!!
        heartRateDao = db.heartRateDao()
    }

    // 싱글톤 구현
    companion object {

        @Volatile
        private var INSTANCE: HeartRateRepository? = null
        fun getInstance(_context: Context): HeartRateRepository {
            return INSTANCE ?: synchronized(HeartRateRepository::class) {
                val instance = HeartRateRepository(_context)
                INSTANCE = instance
                instance
            }
        }
    }

    fun insert(heartRate: HeartRate) {
        heartRateDao.insertAll(heartRate)
    }

    fun getAll(cursor: Int): List<Sensor> {
        return heartRateDao.getAll(cursor)
    }

    fun delete(id: Long) {
        heartRateDao.delete(id)
    }
}