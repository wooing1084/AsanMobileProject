package com.gachon_HCI_Lab.user_mobile.sensor.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gachon_HCI_Lab.user_mobile.sensor.model.HeartRate

/**
 * HeartRate DAO 클래스
 * */
@Dao
interface HeartRateDao {

    /**
     * 데이터 조회 메소드
     * */
    @Query("SELECT * FROM heartRate WHERE id > :cursor ORDER BY id ASC")
    fun getAll(cursor: Int): List<HeartRate>

    /**
     * 데이터 insert 메소드
     * */
    @Insert
    fun insertAll(vararg sensor: HeartRate)

    @Query("DELETE FROM heartRate WHERE id = :id")
    fun delete(id: Long)

    @Query("DELETE FROM heartRate")
    fun deleteAll()

    /**
     * 현재 시간(now)에서 기준(period) 만큼의 수집된 데이터를 조회하는 메소드
     * */
    @Query("SELECT * FROM heartRate WHERE time BETWEEN :now - :period AND :now")
    fun getFromNow(now: Long, period: Long): List<HeartRate>

}