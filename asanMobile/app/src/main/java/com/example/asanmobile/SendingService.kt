package com.example.asanmobile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Build
import android.os.IBinder
import android.util.Log
import java.io.File
import java.sql.Date
import kotlin.concurrent.timer
import androidx.core.app.NotificationCompat


class SendingService : Service() {
    companion object {
        private val NOTIFICATION_ID = 1
        private val CHANNEL_ID = "ForegroundServiceChannel"
        val tag = "Sending Sevice"
        val second = 10
    }



    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification: Notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        Log.d( tag, "start service")
        val t = timer(period = (second * 1000).toLong()){
            Log.d("Sending Service", "Send!")
            sendCSV()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Foreground Service is running...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)

        return builder.build()
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
        val data = getFile("data", unixtime.toString())

        if(data != null)
        {
            //ServerConnection.postFile(data, "gachon_test", "100", formattedDate.toString())
            Log.e(tag, "Data sensor file sending!")
        }

        if (ppg != null) {
            //ServerConnection.postFile(ppg, "gachon_test", "100", formattedDate.toString())
            Log.e(tag, "PPG sensor file sending!")
        }
        if(hrm != null)
        {
            Log.e(tag, "PPG sensor file sending!")
        }
        if(acc != null)
        {
            Log.e(tag, "PPG sensor file sending!")
        }
    }

    //워치에서 데이터 받아서 저장된 후 테스트 필요
    private fun getFile(name : String, uTime : String): File? {
        val context = applicationContext
        val path = context.filesDir.toString()

        var src = File(path, name +".csv")
        if(!src.exists())
        {
            Log.d(tag, name + " File does not found")
            return null
        }

        val dest = File(path, name + "_"+uTime+".csv")
        if(src.renameTo(dest))
        {
            src = File(path, name + "_"+uTime+".csv")
        }

        return src
    }
}