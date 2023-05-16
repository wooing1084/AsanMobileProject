package com.example.asanmobile

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SensorDAO {
    @Query("SELECT * FROM sensor")
    fun getAll(): List<Sensor>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Sensor>

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): Sensor

    @Insert
    fun insertAll(vararg users: Sensor)

    @Delete
    fun delete(sensor: Sensor)
}