package com.example.asanmobile.sensor.controller

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.asanmobile.CsvController
import com.example.asanmobile.sensor.model.HeartRate
import com.example.asanmobile.sensor.model.PpgGreen
import com.example.asanmobile.sensor.model.Sensor
import com.example.asanmobile.sensor.repository.HeartRateRepository
import com.example.asanmobile.sensor.repository.PpgGreenRepository
import kotlinx.coroutines.*
import java.io.IOException
import java.nio.ByteBuffer

// 전역 객체
class SensorController(context: Context) {
    private val heartRateRepository: HeartRateRepository = HeartRateRepository.getInstance(context)
    private val ppgGreenRepository: PpgGreenRepository = PpgGreenRepository.getInstance(context)
    private val prefManager: SharePreferenceManager = SharePreferenceManager.getInstance(context)

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

    private suspend fun dataExport(sensorName: String): List<Sensor> = withContext(Dispatchers.IO) {

        // 센서의 값을 불러온 후, 그 리스트의 사이즈 값을 커서로 저장
        // 다음 호출시 그 커서부터 다시 데이터 호출
        val sensorSet: List<Sensor> = when (sensorName) {
            "HeartRate" -> {
                val heartCursor = prefManager.getCursor("HeartRate")
                Log.d(TAG, "Start cursor: $heartCursor")
                val heartRateSet = heartRateRepository.getAll(heartCursor)
                val heartRateSize = heartRateSet.size
                prefManager.putCursor("HeartRate", heartCursor + heartRateSize)
                heartRateSet
            }

            "PpgGreen" -> {
                val ppgGreenCursor = prefManager.getCursor("PpgGreen")
                val ppgGreenSet = ppgGreenRepository.getAll(ppgGreenCursor)
                val ppgGreenSize = ppgGreenSet.size
                prefManager.putCursor("PpgGreen", ppgGreenCursor + ppgGreenSize)
                ppgGreenSet
            }

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
    }

    private suspend fun writeSensorRepo(bufferList: List<String>) = coroutineScope {
        // 심장박동수 정규표현식
//        val heartRegex = "\\d{12,}:\\d{1,4}[.]\\d|\\d{12,}:\\d{1,4}-".toRegex()
        val hrRegex = "0\\|\\d{12,}:(-?\\d+(\\.\\d+)?)-".toRegex()

        // ppgGreen 정규표현식
//        val ppgGreen = "(\\d{12,}): \\[[^\\]]*\\]".toRegex()
        val pgRegex = "1\\|\\d{12,}:(-?\\d+(\\.\\d+)?)-".toRegex()

        // value 정규표현식
        val valueRegex = "\\d{12,}:(-?\\d+(\\.\\d+)?)".toRegex()

        for (buffer in bufferList) {
            Log.d(TAG, "str: $buffer")
            println("str: $buffer")
            val hrList = hrRegex.findAll(buffer)
            Log.d(TAG, "hr: $hrList")
            val pgList = pgRegex.findAll(buffer)
            Log.d(TAG, "pg: $pgList")

            // 데이터 레포에 넣는 코루틴
            launch {
                try {
                    for (hrPattern in hrList) {
                        val hrVal = hrPattern.value
                        val resRex = valueRegex.find(hrVal)
                        val res = resRex?.value
                        if (res != null) {
                            val str = res.split(":")
                            val time = str[0]
                            val data = str[1].toFloat()

                            // data = 0은 스킵
                            if (data == (0.0).toFloat()) {
                                continue
                            } else {
                                Log.d(TAG, "SAVED: $time, $data")
                                heartRateRepository.insert(HeartRate(time, data.toFloat()))
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            launch {
                try {
                    for (pgPattern in pgList) {
                        val pgVal = pgPattern.value
                        val resRex = valueRegex.find(pgVal)
                        val res = resRex?.value

                        if (res != null) {
                            val str = res.split(":")
                            val time = str[0]
                            val data = str[1].toFloat()

                            if (data == (0.0).toFloat()) {
                                continue
                            } else {
                                Log.d(this.toString(), "SAVED: $time, $data")
                                ppgGreenRepository.insert(PpgGreen(time, data))
                            }
                        }
                    }
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


    suspend fun writeCsv(context: Context, sensorName: String) = coroutineScope {
        // sensorName 적절하게 들어왔는지, 네임을 적절하게 넣어야 함
        Log.d(this.toString(), "csv 시작")
        val sensorSet = dataExport(sensorName)
        if (CsvController.fileExist(context, sensorName) == null) {
            CsvController.csvFirst(context, sensorName)
        }
        val fileName = CsvController.fileExist(context, sensorName)
        // 작성시 이름이 없을 때
        if (fileName == null) {
            CsvController.csvFirst(context, sensorName)
        }
        try {
            CsvController.csvSave(context, fileName!!, sensorSet)
        } catch (e: IOException) {
            // 혹여나 delay로 인해 터질때 대비
            delay(1000)
            // 여기서도 이름이 없을까봐
            CsvController.csvFirst(context, sensorName)
            CsvController.csvSave(context, fileName!!, sensorSet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d(this.toString(), "csv 생성")
    }

    // sharedPreference 싱글톤 객체
    class SharePreferenceManager private constructor(private val context: Context) {
        companion object {
            private lateinit var pref: SharedPreferences
            private lateinit var editor: SharedPreferences.Editor
            private var instance: SharePreferenceManager? = null

            fun getInstance(_context: Context): SharePreferenceManager {
                if (instance == null) {
                    instance = SharePreferenceManager(_context)
                    initialize(_context)
                }
                return instance!!
            }

            private fun initialize(context: Context) {
                pref = context.getSharedPreferences("pref", Activity.MODE_PRIVATE)
                editor = pref.edit()
            }
        }

        fun getCursor(sensorName: String): Int {
            return pref.getInt(sensorName + "Cursor", 0);
        }

        fun putCursor(sensorName: String, lastCursor: Int) {
            editor.putInt(sensorName + "Cursor", lastCursor)
            editor.apply()
        }
    }
}