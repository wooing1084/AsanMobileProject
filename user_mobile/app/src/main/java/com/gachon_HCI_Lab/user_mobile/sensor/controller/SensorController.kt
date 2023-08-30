package com.gachon_HCI_Lab.user_mobile.sensor.controller

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.gachon_HCI_Lab.user_mobile.common.CsvController
import com.gachon_HCI_Lab.user_mobile.common.RegexManager
import com.gachon_HCI_Lab.user_mobile.sensor.model.*
import com.gachon_HCI_Lab.user_mobile.sensor.service.OneAxisDataService
import com.gachon_HCI_Lab.user_mobile.sensor.service.ThreeAxisDataService
import kotlinx.coroutines.*
import java.io.IOException

/**
 * 센서데이터 관리하는 클래스
 * */
class SensorController(context: Context) {
    private val oneAxisDataService: OneAxisDataService = OneAxisDataService.getInstance(context)
    private val threeAxisDataService: ThreeAxisDataService =
        ThreeAxisDataService.getInstance(context)
    private val prefManager: SharePreferenceManager = SharePreferenceManager.getInstance(context)
    private val regexManager: RegexManager = RegexManager.getInstance(context)

    private val oneAxisList = listOf(
        SensorEnum.HEART_RATE.value,
        SensorEnum.LIGHT.value,
        SensorEnum.PPG_GREEN.value
//            SensorEnum.STEP_COUNT.value
    )

    private val threeAxisList = listOf(
        SensorEnum.ACCELEROMETER.value,
        SensorEnum.GRAVITY.value,
        SensorEnum.GYROSCOPE.value
    )

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

    /**
     * 소켓으로부터 수신받은 데이터를 버퍼에 저장하고 플러시하는 메소드
     * data: 소켓으로부터 받은 String 데이터
     * */
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

    /**
     * 호출 시 로컬 DB에 저장된 센서 데이터를 가져온다
     * sensorName: PpgGreen,HeartRate (현재 코드에선 SensorEnum 사용)
     * List<AbstractSensor>: 센서데이터 리스트
     * */
    private suspend fun dataExport(axisType: String, name: String): List<AbstractSensor> =
        withContext(Dispatchers.IO) {
            // 센서의 값을 불러온 후, 그 리스트의 사이즈 값을 커서로 저장
            // 다음 호출시 그 커서부터 다시 데이터 호출
            val abstractSensorSet: List<AbstractSensor> = when (axisType) {
                "OneAxis" -> {
                    oneAxisDataExport()
                }
                "ThreeAxis" -> {
                    threeAxisDataExport()
                }
                else -> throw IllegalArgumentException("Invalid sensor name: $axisType")
            }
            return@withContext abstractSensorSet
        }

    private fun oneAxisDataExport(): List<AbstractSensor> {
        val oneAxisCursor = prefManager.getCursor("oneAxis")
        Log.d(TAG, "Start One_Axis cursor: $oneAxisCursor")
        val oneAxisSet = oneAxisDataService.getAll(oneAxisCursor)
        val oneAxisSetSize = oneAxisSet.size
        prefManager.putCursor("oneAxis", oneAxisCursor + oneAxisSetSize)
        return oneAxisSet
    }

    private fun threeAxisDataExport(): List<AbstractSensor> {
        val threeAxisCursor = prefManager.getCursor("threeAxis")
        Log.d(TAG, "Start Three_Axis cursor: $threeAxisCursor")
        val threeAxisSet = oneAxisDataService.getAll(threeAxisCursor)
        val threeAxisSetSize = threeAxisSet.size
        prefManager.putCursor("threeAxis", threeAxisCursor + threeAxisSetSize)
        return threeAxisSet
    }

    /**
     * 버퍼 내용을 처리하는 메서드
     */
    private suspend fun flushBuffer(buffer: MutableList<String>) {
        // bufferData를 SensorRepository에 작성
        Log.d(this.toString(), buffer.toString())
        writeSensorRepo(buffer)
        buffer.clear() // 버퍼 내용을 비움
    }

    /**
     * 버퍼의 데이터를 알맞은 센서 테이블에 저장하는 메소드
     * */
    private suspend fun writeSensorRepo(bufferList: List<String>) = coroutineScope {
        for (buffer in bufferList) {
            Log.d(TAG, "str: $buffer")
            val oneAxisList = regexManager.oneAxisRegex.findAll(buffer)
            Log.d(TAG, "1_Ax: $oneAxisList")
            val threeAxisList = regexManager.threeAxisRegex.findAll(buffer)
            Log.d(TAG, "3_Ax: $threeAxisList")

            /**
             * 1축 데이터 저장하는 구간
             * 1축 데이터 정규표현식에 맞는 경우 데이터 저장
             * */
            launch {
                try {
                    for (oneAxisPattern in oneAxisList) {
                        val oneAxisVal = oneAxisPattern.value
                        val type =
                            SensorEnum.getValueByType(regexManager.typeRegex.find(oneAxisVal)!!.value.toInt())
                        val resRex = regexManager.oneAxisValueRegex.find(oneAxisVal)
                        val res = resRex?.value
                        val dataMap = regexManager.oneAxisDataExtract(type, res!!)
                        oneAxisDataService.insert(
                            OneAxisData(
                                dataMap.time,
                                dataMap.type,
                                dataMap.data
                            )
                        )
                        Log.d(
                            TAG,
                            "SAVED: type: ${dataMap.type} time: ${dataMap.time}, data: ${dataMap.data}"
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            /**
             * PpgGreen 저장하는 구간
             * ppgGreen 정규표현식의 맞는 경우 데이터 저장
             * */
            launch {
                try {
                    for (threeAxisPattern in threeAxisList) {
                        val threeAxisVal = threeAxisPattern.value
                        val type = SensorEnum.getValueByType(
                            regexManager.typeRegex.find(threeAxisVal)!!.value.toInt()
                        )
                        val resRex = regexManager.threeAxisValueRegex.find(threeAxisVal)
                        val res = resRex?.value
                        val dataMap = regexManager.threeAxisDataExtract(type, res!!)
                        threeAxisDataService.insert(
                            ThreeAxisData(
                                dataMap.time,
                                dataMap.type,
                                dataMap.xData,
                                dataMap.yData,
                                dataMap.zData
                            )
                        )
                        Log.d(
                            TAG,
                            "SAVED: " +
                                    "type: ${dataMap.type} " +
                                    "time: ${dataMap.time}, " +
                                    "data: ${dataMap.xData}, ${dataMap.yData}, ${dataMap.zData}"
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 로컬 DB에서 가져온 센서 데이터를 csv에 저장하는 메소드
     * sensorName: PpgGreen,HeartRate (현재 코드에선 SensorEnum 사용)
     * */
    suspend fun writeCsv(context: Context, type: String) = coroutineScope {
        // sensorName 적절하게 들어왔는지, 네임을 적절하게 넣어야 함
        var sensorSet: List<AbstractSensor> = dataExport(type, "OneAxis")
        val sensorArr: Array<SensorEnum> = SensorEnum.values()

        for (enum in sensorArr) {
            val sensorName = enum.value
            if (SensorEnum.isThreeAxisData(enum.type)) {
                sensorSet = dataExport(type, "ThreeAxis")
            }

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
                CsvController.csvFirst(context, sensorName)
                val regenFile = CsvController.fileExist(context, sensorName)
                CsvController.csvSave(context, regenFile!!, sensorSet)
            } catch (e: NullPointerException) {
                delay(1000)
                CsvController.csvFirst(context, sensorName)
                val regenFile = CsvController.fileExist(context, sensorName)
                CsvController.csvSave(context, regenFile!!, sensorSet)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.d(this.toString(), sensorName + "csv 생성")
        }

    }

    /**
     * 지금으로부터 원하는 시간만큼 데이터를 추출하는 메소드. 코루틴 메소드
     * sensorName: PpgGreen,HeartRate (현재 코드에선 SensorEnum 사용)
     * time: unixtime 기준
     */
    suspend fun getDataFromNow(sensorName: String, time: Long): List<AbstractSensor> =
        withContext(Dispatchers.IO) {
            val abstractSensorSet: List<AbstractSensor> = when (sensorName) {
                "OneAxis" -> {
                    val oneAxisDataSet = oneAxisDataService.getFromNow(time)
                    oneAxisDataSet
                }
                "ThreeAxis" -> {
                    val threeAxisDataSet = threeAxisDataService.getFromNow(time)
                    threeAxisDataSet
                }
                else -> throw IllegalArgumentException("Invalid sensor name: $sensorName")
            }
            Log.d("GET DATA FROM NOW", abstractSensorSet.size.toString())
            return@withContext abstractSensorSet
        }

    /**
     * RoomDB에 저장된 센서 데이터 모두 삭제
     */
    fun deleteAll(){
        oneAxisDataService.deleteAll()
        threeAxisDataService.deleteAll()
    }


    /**
     * sharedPreference 싱글톤 객체
     * 데이터 조회 및 수집시(페이징 활용) 필요한 키 값 저장을 위해 사용
     */
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

        /**
         * 페이지네이션에 사용되는 키값 가져오는 메소드
         * */
        fun getCursor(sensorName: String): Int {
            return pref.getInt(sensorName + "Cursor", 0);
        }

        /**
         * 페이지네이션에 사용되는 키값 저장하는 메소드
         * */
        fun putCursor(sensorName: String, lastCursor: Int) {
            editor.putInt(sensorName + "Cursor", lastCursor)
            editor.apply()
        }
    }


}