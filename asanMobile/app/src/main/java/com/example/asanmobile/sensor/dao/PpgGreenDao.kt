package com.example.asanmobile.sensor.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.asanmobile.sensor.model.HeartRate
import com.example.asanmobile.sensor.model.PpgGreen
import com.example.asanmobile.sensor.model.Sensor

@Dao
interface PpgGreenDao {

    @Query("SELECT * FROM ppgGreen WHERE id > :cursor ORDER BY id ASC")
    fun getAll(cursor: Int): List<PpgGreen>

//    @Query("SELECT * FROM ppgGreen where id = id")
//    suspend fun get(id: Long): PpgGreen

//    @Query("SELECT * FROM ppgGreen WHERE id IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): Flow<LinkedBlockingQueue<Sensor>>

    @Insert
    suspend fun insertAll(vararg users: PpgGreen)

    @Query("DELETE FROM ppgGreen WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM ppgGreen WHERE time BETWEEN time - :period AND :now")
    fun getFromNow(now: Long, period: Long): List<PpgGreen>
}