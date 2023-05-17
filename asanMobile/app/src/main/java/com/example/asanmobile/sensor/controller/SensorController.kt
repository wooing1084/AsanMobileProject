package com.example.asanmobile.sensor.controller

import android.content.Context
import com.example.asanmobile.AcceptThread
import com.example.asanmobile.AppDatabase
import com.example.asanmobile.sensor.model.HeartRate
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SensorController(context: Context) {
    private var db: AppDatabase = AppDatabase.getInstance(context)!!

    companion object {
        private var INSTANCE: SensorController? = null
//        private lateinit var context: Context

        fun getInstance(_context: Context): SensorController {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SensorController(_context).also {
//                    context = _context
                    INSTANCE = it
                }
            }
        }
    }

    suspend fun dataAccept(data: String) = coroutineScope {
        val bufferSize = 1024 // 버퍼 사이즈 설정
        val bufferTime = 2000L // 버퍼링 주기 설정 (2.0 초)
        val buffer = mutableListOf<String>() // 버퍼 선언

        // 데이터를 전달하는 채널 선언
//            val channel = Channel<String>(bufferSize)

        // 데이터를 받아서 버퍼에 저장하는 코루틴
        launch {
            buffer.add(data) // 데이터를 버퍼에 추가
//                println("Added: $data")
//                channel.consumeEach { data ->
//
//                }
            if (buffer.size >= bufferSize) { // 버퍼가 가득 찼을 경우
                flushBuffer(buffer) // 버퍼 내용을 처리
                buffer.clear()
            }
        }

        // 일정 주기마다 버퍼 내용을 처리하는 코루틴
        launch {
            delay(bufferTime)
            if (buffer.isNotEmpty()) { // 버퍼에 내용이 있을 경우
                flushBuffer(buffer) // 버퍼 내용을 처리
                buffer.clear()
            }
        }
    }

    // 버퍼 내용을 처리하는 함수
    suspend fun flushBuffer(buffer: MutableList<String>) {
        // bufferData를 SensorRepository에 작성
        writeSensorRepo(buffer)
        buffer.clear() // 버퍼 내용을 비움

        // 복사한 버퍼 데이터를 처리
        println("Processing: $buffer")
    }

    suspend fun writeSensorRepo(bufferData: List<String>) = coroutineScope {
        // 심장박동수 정규표현식
        val heartRegex = "\\d{12,}:\\d{1,4}[.]\\d|\\d{12,}:\\d{1,4}-".toRegex()

        // ppgGreen 정규표현식
        // 처음오는 숫자가 12 이상이 오고, '['로 시작하고 안에는 어떤 문장이 와도 괜찮고, ']'로 끝나야 한다
        val ppgGreen = "(\\d{12,}): \\[[^\\]]*\\]".toRegex()
        for (str in bufferData) {
            val heartStr = heartRegex.find(str)
            val ppgGreenStr = ppgGreen.find(str)

            // 데이터 레포에 넣는 코루틴
            launch {
                do {
                    // 방어 코드 필요
                    var value = heartStr?.value
                    val res = value.toString().split(":")
                    val time = res[0].trim()
                    val data = res[1].trim().toFloat()

//                        Log.d(TAG, "SAVED: $time, $data")
                    db.heartRateDao().insertAll(HeartRate(time,data))
                } while(heartStr?.next() != null)


//                    ppgGreenStr.forEach {
//                        val res = it.toString().split(":")
//                        val time = res[0].trim()
//                        val dataSet = res[1].split(",")
//
//                        for (data in dataSet) {
//                            if (data.contains('[')) {
//                                data.replace("[", "")
//                            } else if (data.contains(']')) {
//                                data.replace("]", "")
//                            }
//
//                            print("$time, $data")
//                            SensorRepository.writeSensor(time, data.toInt())
//                        }
//                    }
            }

        }
    }
}