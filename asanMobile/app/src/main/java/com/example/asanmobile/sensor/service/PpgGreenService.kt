package com.example.asanmobile.sensor.service

import android.content.Context
import com.example.asanmobile.sensor.AppDatabase
import com.example.asanmobile.sensor.dao.PpgGreenDao
import com.example.asanmobile.sensor.model.PpgGreen
import com.example.asanmobile.sensor.model.Sensor

class PpgGreenService(context: Context) {

    private val ppgGreenDao: PpgGreenDao
    private val db: AppDatabase

    init {
        db = AppDatabase.getInstance(context)!!
        ppgGreenDao = db.ppgGreenDao()
    }

    // 싱글톤 구현
    companion object {

        @Volatile
        private var INSTANCE: PpgGreenService? = null
        fun getInstance(_context: Context): PpgGreenService {
            return INSTANCE ?: synchronized(PpgGreenService::class) {
                val instance = PpgGreenService(_context)
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun insert(ppgGreen: PpgGreen) {
        ppgGreenDao.insertAll(ppgGreen)
    }

    fun getAll(cursor: Int): List<Sensor> {
        return ppgGreenDao.getAll(cursor)
    }

    fun getFromNow(period: Long): List<Sensor> {
        val now = System.currentTimeMillis()
        return ppgGreenDao.getFromNow(now, period)
    }

//    fun delete(id: Long) {
//        ppgGreenDao.delete(id)
//    }
}