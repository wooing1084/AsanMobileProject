package com.example.asanmobile.sensor.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.asanmobile.sensor.model.PpgGreen

@Dao
interface PpgGreenDao {

    @Query("SELECT * FROM ppgGreen WHERE id > :cursor ORDER BY id ASC")
    fun getAll(cursor: Int): List<PpgGreen>

    @Insert
    suspend fun insertAll(vararg users: PpgGreen)

    @Query("DELETE FROM ppgGreen WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM ppgGreen WHERE time BETWEEN :now - :period AND :now")
    fun getFromNow(now: Long, period: Long): List<PpgGreen>
}