package com.example.asanmobile.sensor.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.asanmobile.sensor.model.PpgGreen

@Dao
interface PpgGreenDao {

    @Query("SELECT * FROM sensor")
    fun getAll(): List<PpgGreen>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<PpgGreen>

    @Insert
    fun insertAll(vararg users: PpgGreen)

    @Delete
    fun delete(sensor: PpgGreen)
}