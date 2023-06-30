package com.example.asanmobile.sensor.service

import android.content.Context
import com.example.asanmobile.sensor.AppDatabase
import com.example.asanmobile.sensor.dao.HeartRateDao
import com.example.asanmobile.sensor.model.HeartRate
import com.example.asanmobile.sensor.model.Sensor

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

    fun getAll(cursor: Int): List<Sensor> {
        return heartRateDao.getAll(cursor)
    }

//    fun getFromNow(time: Int): List<Sensor> {
//        val now = System.currentTimeMillis()
//        return heartRateDao.getFromNow(now, time)
//    }

    fun delete(id: Long) {
        heartRateDao.delete(id)
    }
}