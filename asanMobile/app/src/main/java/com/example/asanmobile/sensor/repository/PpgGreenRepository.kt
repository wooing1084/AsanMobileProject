package com.example.asanmobile.sensor.repository

import android.content.Context
import com.example.asanmobile.AppDatabase
import com.example.asanmobile.sensor.dao.PpgGreenDao
import com.example.asanmobile.sensor.model.PpgGreen
import com.example.asanmobile.sensor.model.Sensor
import java.util.concurrent.LinkedBlockingQueue

class PpgGreenRepository(context: Context) {

    private val ppgGreenDao: PpgGreenDao
    private val db: AppDatabase

    init {
        db = AppDatabase.getInstance(context)!!
        ppgGreenDao = db.ppgGreenDao()
    }

    // 싱글톤 구현
    companion object {

        @Volatile
        private var INSTANCE: PpgGreenRepository? = null
        fun getInstance(_context: Context): PpgGreenRepository {
            return INSTANCE ?: synchronized(PpgGreenRepository::class) {
                val instance = PpgGreenRepository(_context)
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

//    fun delete(id: Long) {
//        ppgGreenDao.delete(id)
//    }
}