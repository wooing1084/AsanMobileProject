package com.example.asanmobile.sensor.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heartRate")
data class HeartRate(
    @ColumnInfo(name = "time") override val time: String,
    @ColumnInfo(name = "value") override val value: Float
): Sensor(time, value) {
    // AutoIncrement 구현
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo
    var id: Long = 0
}