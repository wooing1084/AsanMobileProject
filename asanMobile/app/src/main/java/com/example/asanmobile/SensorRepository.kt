package com.example.asanmobile

import java.util.concurrent.LinkedBlockingQueue

// SensorRepository 의 경우 싱글톤을 위해 Object로 구현
object SensorRepository {
    private val heartRateQueue: LinkedBlockingQueue<Pair<String, Int>> = LinkedBlockingQueue<Pair<String, Int>>()
    private val ppgGreenQueue: LinkedBlockingQueue<Pair<String, Int>> = LinkedBlockingQueue<Pair<String, Int>>()


}