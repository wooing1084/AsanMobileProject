package com.example.user_mobile.sensor.service

import android.content.Context
import com.example.user_mobile.sensor.AppDatabase
import com.example.user_mobile.sensor.dao.HeartRateDao
import com.example.user_mobile.sensor.model.HeartRate
import com.example.user_mobile.sensor.model.AbstractSensor

class HeartRateService(context: Context) {

    private val heartRateDao: HeartRateDao
    private val db: AppDatabase

    init {
        db = AppDatabase.getInstance(context)!!
        heartRateDao = db.heartRateDao()
    }

    // 싱글톤 구현
    companion object {

        @Volatile
        private var INSTANCE: HeartRateService? = null
        fun getInstance(_context: Context): HeartRateService {
            return INSTANCE ?: synchronized(HeartRateService::class) {
                val instance = HeartRateService(_context)
                INSTANCE = instance
                instance
            }
        }
    }

    fun insert(heartRate: HeartRate) {
        heartRateDao.insertAll(heartRate)
    }

    fun getAll(cursor: Int): List<AbstractSensor> {
        return heartRateDao.getAll(cursor)
    }

    fun getFromNow(period: Long): List<AbstractSensor> {
        val now = System.currentTimeMillis()
        return heartRateDao.getFromNow(now, period)
    }

    fun delete(id: Long) {
        heartRateDao.delete(id)
    }
}