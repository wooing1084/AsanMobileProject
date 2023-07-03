package com.example.asanmobile.sensor.controller

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.asanmobile.common.CsvController
import com.example.asanmobile.common.RegexManager
import com.example.asanmobile.sensor.model.HeartRate
import com.example.asanmobile.sensor.model.PpgGreen
import com.example.asanmobile.sensor.model.AbstractSensor
import com.example.asanmobile.sensor.model.SensorEnum
import com.example.asanmobile.sensor.service.HeartRateService
import com.example.asanmobile.sensor.service.PpgGreenService
import kotlinx.coroutines.*
import java.io.IOException

// 전역 객체
class SensorController(context: Context) {
    private val heartRateService: HeartRateService = HeartRateService.getInstance(context)
    private val ppgGreenService: PpgGreenService = PpgGreenService.getInstance(context)
    private val prefManager: SharePreferenceManager = SharePreferenceManager.getInstance(context)
    private val regexManager: RegexManager = RegexManager.getInstance(context)

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

    private suspend fun dataExport(sensorName: String): List<AbstractSensor> = withContext(Dispatchers.IO) {

        // 센서의 값을 불러온 후, 그 리스트의 사이즈 값을 커서로 저장
        // 다음 호출시 그 커서부터 다시 데이터 호출
        val abstractSensorSet: List<AbstractSensor> = when (sensorName) {
            SensorEnum.HEART_RATE.value -> {
                val heartCursor = prefManager.getCursor("HeartRate")
                Log.d(TAG, "Start cursor: $heartCursor")
                val heartRateSet = heartRateService.getAll(heartCursor)
                val heartRateSize = heartRateSet.size
                prefManager.putCursor("HeartRate", heartCursor + heartRateSize)
                heartRateSet
            }

            SensorEnum.PPG_GREEN.value -> {
                val ppgGreenCursor = prefManager.getCursor("PpgGreen")
                val ppgGreenSet = ppgGreenService.getAll(ppgGreenCursor)
                val ppgGreenSize = ppgGreenSet.size
                prefManager.putCursor("PpgGreen", ppgGreenCursor + ppgGreenSize)
                ppgGreenSet
            }

            else -> throw IllegalArgumentException("Invalid sensor name: $sensorName")
        }
        return@withContext abstractSensorSet
    }

    // 버퍼 내용을 처리하는 함수
    private suspend fun flushBuffer(buffer: MutableList<String>) {
        // bufferData를 SensorRepository에 작성
        Log.d(this.toString(), buffer.toString())
        writeSensorRepo(buffer)
        buffer.clear() // 버퍼 내용을 비움
    }

    private suspend fun writeSensorRepo(bufferList: List<String>) = coroutineScope {

        for (buffer in bufferList) {
            println("str: $buffer")
            Log.d(TAG, "str: $buffer")

            val hrList = regexManager.hrRegex.findAll(buffer)
            Log.d(TAG, "hr: $hrList")
            val pgList = regexManager.pgRegex.findAll(buffer)
            Log.d(TAG, "pg: $pgList")

            // 데이터 레포에 넣는 코루틴
            launch {
                try {
                    for (hrPattern in hrList) {
                        val hrVal = hrPattern.value
                        val resRex = regexManager.valueRegex.find(hrVal)
                        val res = resRex?.value

                        val dataMap = regexManager.dataExtract(res!!)
                        heartRateService.insert(HeartRate(dataMap.time, dataMap.data))
                        Log.d(TAG, "SAVED: $dataMap.time, $dataMap.data")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            launch {
                try {
                    for (pgPattern in pgList) {
                        val pgVal = pgPattern.value
                        val resRex = regexManager.valueRegex.find(pgVal)
                        val res = resRex?.value

                        val dataMap = regexManager.dataExtract(res!!)
                        ppgGreenService.insert(PpgGreen(dataMap.time, dataMap.data))
                        Log.d(TAG, "SAVED: $dataMap.time, $dataMap.data")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    suspend fun writeCsv(context: Context, sensorName: String) = coroutineScope {
        // sensorName 적절하게 들어왔는지, 네임을 적절하게 넣어야 함
        Log.d(this.toString(), sensorName + ".csv 시작")
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
            val regenFile = CsvController.fileExist(context, sensorName)
            CsvController.csvSave(context, regenFile!!, sensorSet)
        } catch (e: NullPointerException) {
            // 혹여나 delay로 인해 터질때 대비
            delay(1000)
            // 여기서도 이름이 없을까봐
            CsvController.csvFirst(context, sensorName)
            val regenFile = CsvController.fileExist(context, sensorName)
            CsvController.csvSave(context, regenFile!!, sensorSet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d(this.toString(), sensorName+"csv 생성")
    }

    /**
     * 지금으로부터 원하는 시간만큼 데이터를 추출하는 메소드. 코루틴 메소드
     * parameter
     * sensorName: "HeartRate" or "PpgGreen" ENUM 적용
     * time: unixtime 기준
     */

    suspend fun getDataFromNow(sensorName: String, time: Long): List<AbstractSensor> = withContext(Dispatchers.IO) {
        val abstractSensorSet: List<AbstractSensor> = when (sensorName) {
            SensorEnum.HEART_RATE.value -> {
                val heartRateSet = heartRateService.getFromNow(time)
                heartRateSet
            }

            SensorEnum.PPG_GREEN.value -> {
                val ppgGreenSet = ppgGreenService.getFromNow(time)
                ppgGreenSet
            }
            else -> throw IllegalArgumentException("Invalid sensor name: $sensorName")
        }
        Log.d("GET DATA FROM NOW", abstractSensorSet.size.toString())
        return@withContext abstractSensorSet
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