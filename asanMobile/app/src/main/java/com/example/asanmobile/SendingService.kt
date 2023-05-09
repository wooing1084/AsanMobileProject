package com.example.asanmobile

import android.app.Service
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.IBinder
import android.util.Log
import java.io.File
import java.sql.Date
import kotlin.concurrent.timer

class SendingService : Service() {
    val tag = "Sending Sevice"
    val second = 10

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d( tag, "start service")
        val t = timer(period = (second * 1000).toLong()){
            Log.d("Sending Service", "Send!")
            //sendCSV()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    //전송양식
    //[파일 이름]
    //센서명_unixtime.csv
    //[데이터 전송시 timestamp]
    //dd_MM_yyyy
    private fun sendCSV(){
        //Unix time 생성
        var unixtime = System.currentTimeMillis()

        val date = Date(unixtime)
        // SimpleDateFormat을 사용하여 날짜와 시간을 포맷팅하기
        val formatter = SimpleDateFormat("dd_MM_yyyy")
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val formattedDate = formatter.format(date)

        val ppg = getFile("ppg", unixtime.toString())
        val hrm = getFile("hrm", unixtime.toString())
        val acc = getFile("acc", unixtime.toString())


        // 서버에 Post전송(테스트를 위해 하나만 전송하는 중)
        if (ppg != null) {
            ServerConnection.postFile(ppg, "gachon_test", "100", formattedDate.toString())
        }
    }

    private fun getFile(name : String, uTime : String): File? {
        val context = applicationContext
        val path = context.filesDir.toString()

        val src = File(path, name+".csv")
        if(!src.exists())
        {
            Log.d(tag, name + " File does not found")
            return null
        }

        val dest = File(name + "_"+uTime+".csv")
        val result = src.renameTo(dest)

        return src
    }
}