package com.example.asanmobile.sensor.model

// 센서 추상 클래스, 각 센서들은 이 클래스를 상속받아야 한다
abstract class Sensor {
    abstract val time: String
    abstract val value: Float
}