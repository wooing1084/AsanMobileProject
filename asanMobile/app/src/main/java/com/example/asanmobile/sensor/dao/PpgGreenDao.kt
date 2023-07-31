package com.example.asanmobile.sensor.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.asanmobile.sensor.model.PpgGreen

/**
 * PpgGreen DAO 클래스
 * */
@Dao
interface PpgGreenDao {

    /**
     * 데이터 조회 메소드
     * */
    @Query("SELECT * FROM ppgGreen WHERE id > :cursor ORDER BY id ASC")
    fun getAll(cursor: Int): List<PpgGreen>

    /**
     * 데이터 insert 메소드
     * */
    @Insert
    suspend fun insertAll(vararg users: PpgGreen)

    @Query("DELETE FROM ppgGreen WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 현재 시간(now)에서 기준(period) 만큼의 수집된 데이터를 조회하는 메소드
     * */
    @Query("SELECT * FROM ppgGreen WHERE time BETWEEN :now - :period AND :now")
    fun getFromNow(now: Long, period: Long): List<PpgGreen>
}