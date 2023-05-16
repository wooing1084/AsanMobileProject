package com.example.asanmobile

import java.util.concurrent.LinkedBlockingQueue

// SensorRepository 의 경우 싱글톤을 위해 Object로 구현
object SensorRepository {
//    private val heartRateQueue: LinkedBlockingQueue<Pair<String, Float>> = LinkedBlockingQueue<Pair<String, Float>>()
//    private val ppgGreenQueue: LinkedBlockingQueue<Pair<String, Float>> = LinkedBlockingQueue<Pair<String, Float>>()

//    fun writeSensor(time:String, data: Float) {
//        val pair = Pair(time, data)
//
//        if (data in 1.0..400.0) {
//            heartRateQueue.add(pair)
//        } else {
//            ppgGreenQueue.add(pair)
//        }
//    }
//    fun popAllSensor(queue: LinkedBlockingQueue<Pair<String, Int>>) {
//        repeat(queue.size) {
//            queue
//        }
//    }

    fun save (time:String, data:Float) {

    }


}