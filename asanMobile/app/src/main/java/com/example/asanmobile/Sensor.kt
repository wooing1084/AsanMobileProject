package com.example.asanmobile

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Sensor(
    @PrimaryKey val uid: Int,
//    @ColumnInfo(time = "time") val time: String?,
//    @ColumnInfo(data = "data") val data: Float?
)
