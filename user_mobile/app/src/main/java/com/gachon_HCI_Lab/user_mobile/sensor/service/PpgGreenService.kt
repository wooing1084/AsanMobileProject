package com.gachon_HCI_Lab.user_mobile.sensor.service

import android.content.Context
import com.gachon_HCI_Lab.user_mobile.sensor.AppDatabase
import com.gachon_HCI_Lab.user_mobile.sensor.dao.PpgGreenDao
import com.gachon_HCI_Lab.user_mobile.sensor.model.PpgGreen
import com.gachon_HCI_Lab.user_mobile.sensor.model.AbstractSensor

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

    fun getAll(cursor: Int): List<AbstractSensor> {
        return ppgGreenDao.getAll(cursor)
    }

    fun getFromNow(period: Long): List<AbstractSensor> {
        val now = System.currentTimeMillis()
        return ppgGreenDao.getFromNow(now, period)
    }

    fun deleteAll() {
        ppgGreenDao.deleteAll()
    }

//    fun delete(id: Long) {
//        ppgGreenDao.delete(id)
//    }
}