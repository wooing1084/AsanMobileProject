package com.example.asanmobile.sensor.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heartRate")
data class HeartRate(
    @ColumnInfo(name = "time") override val time: Long,
    @ColumnInfo(name = "value") override val value: Float
): AbstractSensor() {
    // AutoIncrement 구현
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo
    var id: Long = 0
}
