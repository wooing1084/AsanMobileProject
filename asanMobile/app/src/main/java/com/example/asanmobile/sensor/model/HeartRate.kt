package com.example.asanmobile.sensor.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heartRate")
data class HeartRate(
    @ColumnInfo(name = "time") val time: String,
    @ColumnInfo(name = "data") val data: Float
) {
    // AutoIncrement 구현
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0
}
