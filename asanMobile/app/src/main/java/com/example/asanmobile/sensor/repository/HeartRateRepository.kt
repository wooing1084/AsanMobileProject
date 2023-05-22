package com.example.asanmobile.sensor.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.asanmobile.AppDatabase
import com.example.asanmobile.sensor.dao.HeartRateDao
import com.example.asanmobile.sensor.model.HeartRate
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.LinkedBlockingQueue

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

    fun getAll(): List<HeartRate> {
        return heartRateDao.getAll()
    }

    fun delete(id: Long) {
        heartRateDao.delete(id)
    }
}