package com.example.asanmobile.sensor.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ppgGreen")
data class PpgGreen(
    @ColumnInfo(name = "time") val time: String,
    @ColumnInfo(name = "value") val value: Float
) {
    // AutoIncrement 구현
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo
    var id: Long = 0
}
