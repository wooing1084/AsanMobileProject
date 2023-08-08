package com.gachon_HCI_Lab.user_mobile.common

import android.content.Context
import com.gachon_HCI_Lab.user_mobile.sensor.model.SensorData

/**
 * 정규표현식 클래스
 * 소켓을 통해 수신한 데이터 수집을 위해 사용 
 * */
class RegexManager private constructor(context: Context) {

    // 싱글톤 구현
    companion object {
        @Volatile
        private var INSTANCE: RegexManager? = null
        fun getInstance(_context: Context): RegexManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RegexManager(_context).also { INSTANCE = it }
            }
        }
    }

    // 심장박동수 정규표현식
    val hrRegex = "0\\|\\d{12,}:(-?\\d+(\\.\\d+)?)-".toRegex()

    // ppgGreen 정규표현식
    val pgRegex = "1\\|\\d{12,}:(-?\\d+(\\.\\d+)?)-".toRegex()

    // value 정규표현식
    val valueRegex = "\\d{12,}:(-?\\d+(\\.\\d+)?)".toRegex()


    /**
     * 데이터 추출 시 사용 메소드
     * */
    fun dataExtract(res: String): SensorData {
        val str = res.split(":")
        val time = str[0].toLong()
        val data = str[1].toFloat()

        return SensorData(time, data)
    }

}