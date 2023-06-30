package com.example.asanmobile.sensor.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.asanmobile.sensor.model.HeartRate
import com.example.asanmobile.sensor.model.Sensor

@Dao
interface HeartRateDao {
//    @Query("SELECT * FROM heartRate LIMIT :limit")
//    fun getAll(limit: Int): LinkedBlockingQueue<HeartRate>

    @Query("SELECT * FROM heartRate WHERE id > :cursor ORDER BY id ASC")
    fun getAll(cursor: Int): List<HeartRate>

//    @Query("SELECT * FROM heartRate where id = id")
//    suspend fun get(id: Long): List<HeartRate>

//    @Query("SELECT * FROM heartRate WHERE id IN (:sensorIds)")
//    fun loadAllByIds(sensorIds: IntArray): Flow<LinkedBlockingQueue<Sensor>>

    @Insert
    fun insertAll(vararg sensor: HeartRate)

    @Query("DELETE FROM heartRate WHERE id = :id")
    fun delete(id: Long)

//    @Query("SELECT * FROM heartRate WHERE ")
//    fun getFromNow(now: Long, time: Int): List<HeartRate>

}