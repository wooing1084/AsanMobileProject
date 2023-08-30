package com.gachon_HCI_Lab.user_mobile.sensor.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "OneAxis_TB")
data class OneAxisData(
    @ColumnInfo override val time: Long,
    @ColumnInfo override val type: String,
    @ColumnInfo val value: Double
) : AbstractSensor() {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "one_id")
    var id: Long = 0
}
