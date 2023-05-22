package com.example.asanmobile.sensor.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.asanmobile.sensor.model.PpgGreen
import com.example.asanmobile.sensor.model.Sensor
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.LinkedBlockingQueue

@Dao
interface PpgGreenDao {

    @Query("SELECT * FROM ppgGreen")
    fun getAll(): List<PpgGreen>

//    @Query("SELECT * FROM ppgGreen where id = id")
//    suspend fun get(id: Long): PpgGreen

//    @Query("SELECT * FROM ppgGreen WHERE id IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): Flow<LinkedBlockingQueue<Sensor>>

    @Insert
    suspend fun insertAll(vararg users: PpgGreen)

    @Query("DELETE FROM ppgGreen WHERE id = :id")
    suspend fun deleteById(id: Long)
}