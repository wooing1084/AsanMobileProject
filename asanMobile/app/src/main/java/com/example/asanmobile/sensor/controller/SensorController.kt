package com.example.asanmobile.sensor.controller

import android.content.Context
import android.util.Log
import com.example.asanmobile.CsvController
import com.example.asanmobile.sensor.model.HeartRate
import com.example.asanmobile.sensor.model.PpgGreen
import com.example.asanmobile.sensor.model.Sensor
import com.example.asanmobile.sensor.repository.HeartRateRepository
import com.example.asanmobile.sensor.repository.PpgGreenRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// 전역 객체
class SensorController(context: Context) {
    private val heartRateRepository: HeartRateRepository = HeartRateRepository.getInstance(context)
    private val ppgGreenRepository: PpgGreenRepository = PpgGreenRepository.getInstance(context)

    companion object {
        private var INSTANCE: SensorController? = null

        fun getInstance(_context: Context): SensorController {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SensorController(_context).also {
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

    // sensor 재활용성을 높이기 위해 제네릭 타입에 * 사용
//    suspend fun dataExport(sensorName: String): List<Sensor> = withContext(Dispatchers.IO){
//        var sensorFlow: Flow<List<Sensor>>? = null
//        val collectedQueue = mutableListOf<Sensor>()
//
//        if (sensorName == "HeartRate") {
//            sensorFlow = heartRateRepository.getAll()
//            Log.d(this.toString(), "도킹!")
//        } else if (sensorName == "PpgGreen") {
////            sensorFlow =
//        }
//
//        val collectedList = sensorFlow?.flatMapConcat { it.asFlow() }?.toList() ?: emptyList()
//        Log.d(this.toString(), collectedQueue.toString())
//        return@withContext collectedList
//    }

    private suspend fun dataExport(sensorName: String): List<Sensor> = withContext(Dispatchers.IO) {
        val sensorSet: List<Sensor> = when (sensorName) {
            "HeartRate" -> heartRateRepository.getAll()
             "PpgGreen" -> ppgGreenRepository.getAll()
            else -> throw IllegalArgumentException("Invalid sensor name: $sensorName")
        }
        return@withContext sensorSet
    }

    // 버퍼 내용을 처리하는 함수
    private suspend fun flushBuffer(buffer: MutableList<String>) {
        // bufferData를 SensorRepository에 작성
        Log.d(this.toString(), buffer.toString())
        writeSensorRepo(buffer)
        buffer.clear() // 버퍼 내용을 비움

//        // 복사한 버퍼 데이터를 처리
//        println("Processing: $buffer")
    }

    private suspend fun writeSensorRepo(bufferData: List<String>) = coroutineScope {
        // 심장박동수 정규표현식
        val heartRegex = "\\d{12,}:\\d{1,4}[.]\\d|\\d{12,}:\\d{1,4}-".toRegex()

        // ppgGreen 정규표현식
        // 처음오는 숫자가 12 이상이 오고, '['로 시작하고 안에는 어떤 문장이 와도 괜찮고, ']'로 끝나야 한다
        val ppgGreen = "(\\d{12,}): \\[[^\\]]*\\]".toRegex()
        for (str in bufferData) {
            val hrStr = heartRegex.find(str)
            val pgStr = ppgGreen.find(str)

            // 데이터 레포에 넣는 코루틴
            launch {
                do {
                    // 방어 코드 필요
                    var hrValue = hrStr?.value
                    val hrRes = hrValue.toString().split(":")
                    val time = hrRes[0].trim()
                    val data = hrRes[1].trim().toFloat()

                    // data = 0은 스킵
                    if (data.toInt() == (0.0).toInt()) {
                        continue
                    } else {
                        heartRateRepository.insert(HeartRate(time, data))
                    }
//                        Log.d(TAG, "SAVED: $time, $data")
                } while(hrStr?.next() != null)
            }

            launch {
                if (pgStr != null) {
                    try {
                        val ppgGreenValue = pgStr.value
                        val pgRes = ppgGreenValue.split(":")
                        val pgTime = pgRes[0].trim()
                        val innerPpg = pgRes[1].trim()
                        val innerRex = "^[+-]?\\d*(\\.?\\d*)?$".toRegex()
                        do {
                            val pgStr = innerRex.find(innerPpg)
                            val pgValue = pgStr.toString().toFloat()

                            if (pgValue.toInt() == (0.0).toInt()) {
                                continue
                            } else {
                                Log.d(this.toString(), "SAVED: $pgTime, $pgValue")
                                ppgGreenRepository.insert(PpgGreen(pgTime, pgValue))
                            }
                        } while (pgStr?.next() != null)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

//                    val res = toString().split(":")
//                    val time = res[0].trim()
//                    val dataSet = res[1].split(",")
//
//                    for (data in dataSet) {
//                        if (data.contains('[')) {
//                            data.replace("[", "")
//                        } else if (data.contains(']')) {
//                            data.replace("]", "")
//                        }
//                    }
            }
        }
    }

    // 커서랑 개수 정해야 함
    suspend fun writeCsv(context: Context, sensorName: String) = coroutineScope {
        // sensorName 적절하게 들어왔는지, 네임을 적절하게 넣어야 함
        Log.d(this.toString(), "csv 시작")
        val sensorSet = dataExport(sensorName)
        if (CsvController.fileExist(context, sensorName) == null) {
            CsvController.csvFirst(context, sensorName)
        }
        val fileName = CsvController.fileExist(context, sensorName)
        CsvController.csvSave(context, fileName!!, sensorSet)
        Log.d(this.toString(), "csv 생성")
    }
}