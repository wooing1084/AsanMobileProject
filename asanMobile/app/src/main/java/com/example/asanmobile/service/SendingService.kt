package com.example.asanmobile.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import java.io.File
import kotlin.concurrent.timer
import androidx.core.app.NotificationCompat
import com.example.asanmobile.R
import java.io.IOException


class SendingService : Service() {
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "ForegroundServiceChannel"
    val tag = "Sending Sevice"
    val second = 20



    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification: Notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        Log.d( tag, "start service")
        val t = timer(period = (second * 1000).toLong(),
            initialDelay = (second * 1000).toLong()){
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
        //HeartRate
        val hrFileName = getExistFileName(this, "HeartRate")
        val hrSrcPath = getExternalPath(this,"sensor") + "/" +hrFileName
        val hrDestPath = getExternalPath(this,"sensor/sended") + "/"+ hrFileName

        //파일 이동 후 삭제
        moveFile(hrSrcPath,hrDestPath)
        val hrSrcFile = getFile(hrSrcPath)
        hrSrcFile?.delete()

        val heartFile = getFile(hrDestPath)

        //PpgGreen
        val ppgFileName = getExistFileName(this, "PpgGreen")
        val ppgSrcPath = getExternalPath(this,"sensor") + "/" +ppgFileName
        val ppgDestPath = getExternalPath(this,"sensor/sended") + "/"+ ppgFileName

        //파일 이동 후 삭제
        moveFile(ppgSrcPath,ppgDestPath)
        val ppgSrcFile = getFile(ppgSrcPath)
        ppgSrcFile?.delete()

        val ppgFile = getFile(ppgDestPath)

        if (ppgFile != null) {
//            val token = ppgFileName!!.split('_')
//            val ppgTime = token[1].split('.')[0]
//
//            ServerConnection.postFile(ppgFile, DeviceInfo._uID, "100", ppgTime)
            Log.d(tag, "PPG Green sensor file sending!")
        }
        if(heartFile != null)
        {
//            val token = hrFileName!!.split('_')
//            val hrTime = token[1].split('.')[0]
//
//            ServerConnection.postFile(heartFile, DeviceInfo._uID, "100", hrTime)
            Log.d(tag, "Heartrate sensor file sending!")
        }

    }

    private fun getFile(fileName : String): File? {
        val file = File(fileName)
        if(!file.exists())
        {
            Log.d(tag, fileName + " File does not found")
            return null
        }

        return file
    }

    //파일 디렉토리 옮기기
    //soure -> dest로
    fun moveFile(sourcePath: String, destinationPath: String) {
        val sourceFile = File(sourcePath)
        val destinationFile = File(destinationPath)

        try {
            // 파일을 이동합니다.
            sourceFile.renameTo(destinationFile)
            Log.d(tag, "파일 이동 성공")
        } catch (e: IOException) {
            println("파일 이동 실패: ${e.message}")
        }
    }

    //디바이스의 센서_unixtime.csv파일명 가져오기
    //unixtime은 알 수 없기 때문에 파일명을 알아내기 위해 사용
    fun getExistFileName(context: Context, name: String): String? {
        val path: String = getExternalPath(context, "sensor")
        val directory: File = File(path)

        if (directory.exists()) {
            val files: Array<out File>? = directory.listFiles()

            for (file in files!!) {
                if (file.name.contains(name)) {
                    return file.name
                }
            }
        }
        return null
    }

    public fun getExternalPath(context: Context, dirName : String): String{
        val dir: File? = context.getExternalFilesDir(null)
        val path = dir?.absolutePath + File.separator + dirName

        // 외부 저장소 경로가 있는지 확인, 없으면 생성
        val file: File = File(path)
        if (!file.exists()) {
            file.mkdir()
        }
        return path
    }
}