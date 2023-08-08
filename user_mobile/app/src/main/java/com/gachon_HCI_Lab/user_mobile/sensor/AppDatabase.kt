package com.gachon_HCI_Lab.user_mobile.sensor

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gachon_HCI_Lab.user_mobile.sensor.dao.HeartRateDao
import com.gachon_HCI_Lab.user_mobile.sensor.dao.PpgGreenDao
import com.gachon_HCI_Lab.user_mobile.sensor.model.HeartRate
import com.gachon_HCI_Lab.user_mobile.sensor.model.PpgGreen

/**
 * 앱 내 센서 데이터 저장용 로컬 DB 접근용 추상 클래스
 * */
@Database(entities = [HeartRate::class, PpgGreen::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun heartRateDao(): HeartRateDao
    abstract fun ppgGreenDao(): PpgGreenDao

    // 싱글톤 구현
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "sensor"
                    ).build()
                }
            }
            INSTANCE!!.openHelper.readableDatabase
            return INSTANCE
        }
    }
}