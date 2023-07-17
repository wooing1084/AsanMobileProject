package com.example.asanmobile.sensor.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.asanmobile.sensor.model.HeartRate

@Dao
interface HeartRateDao {

    @Query("SELECT * FROM heartRate WHERE id > :cursor ORDER BY id ASC")
    fun getAll(cursor: Int): List<HeartRate>

    @Insert
    fun insertAll(vararg sensor: HeartRate)

    @Query("DELETE FROM heartRate WHERE id = :id")
    fun delete(id: Long)

    @Query("SELECT * FROM heartRate WHERE time BETWEEN :now - :period AND :now")
    fun getFromNow(now: Long, period: Long): List<HeartRate>

}