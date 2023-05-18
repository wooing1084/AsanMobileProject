package com.example.asanmobile.sensor.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.asanmobile.sensor.model.HeartRate

@Dao
interface HeartRateDao {
//    @Query("SELECT * FROM sensor")
//    fun getAll(): List<HeartRate>
//    @Query("SELECT * FROM ")
//    fun get(id: Long):

    @Query("SELECT * FROM heartRate WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<HeartRate>

    @Insert
    fun insertAll(vararg users: HeartRate)

    @Delete
    fun delete(sensor: HeartRate)
}