package com.example.asanmobile.sensor.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ppgGreen")
data class PpgGreen(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "time") val time: String,
    @ColumnInfo(name = "value") val value: Float
)
