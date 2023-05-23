package com.example.asanmobile.sensor.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.asanmobile.sensor.model.HeartRate
import com.example.asanmobile.sensor.model.Sensor
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.LinkedBlockingQueue

@Dao
interface HeartRateDao {
//    @Query("SELECT * FROM heartRate LIMIT :limit")
//    fun getAll(limit: Int): LinkedBlockingQueue<HeartRate>

    @Query("SELECT * FROM heartRate WHERE id > :cursor ORDER BY id ASC")
    fun getAll(cursor: Int): LinkedBlockingQueue<Sensor>

//    @Query("SELECT * FROM heartRate where id = id")
//    suspend fun get(id: Long): List<HeartRate>

//    @Query("SELECT * FROM heartRate WHERE id IN (:sensorIds)")
//    fun loadAllByIds(sensorIds: IntArray): Flow<LinkedBlockingQueue<Sensor>>

    @Insert
    fun insertAll(vararg sensor: HeartRate)

    @Query("DELETE FROM heartRate WHERE id = :id")
    fun delete(id: Long)
}