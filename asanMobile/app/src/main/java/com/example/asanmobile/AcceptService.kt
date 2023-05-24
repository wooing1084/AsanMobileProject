package com.example.asanmobile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.asanmobile.sensor.controller.SensorController
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.Timer
import java.util.TimerTask

class AcceptService: Service() {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var acceptThread: AcceptThread
    private val sensorController: SensorController = SensorController.getInstance(this)
    private val context: Context = this

    //From Sending Service
    val tag = "Sending Sevice"

//    fun isBluetoothSupport(): Boolean {
//        return if (bluetoothAdapter == null) {
//            Toast.makeText(this, "Bluetooth 지원을 하지 않는 기기입니다.", Toast.LENGTH_SHORT).show()
//            false
//        } else {
//            true
//        }
//    }
//    fun isBluetoothEnabled(): Boolean {
//        return if (!bluetoothAdapter.isEnabled) {
//            // 블루투스를 지원하지만 비활성 상태인 경우
//            // 블루투스를 활성 상태로 바꾸기 위해 사용자 동의 요청
//            Toast.makeText(this, "Bluetooth를 활성화 해 주세요.", Toast.LENGTH_SHORT).show()
//            false
//        } else {
//            true
//        }
//    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "com.asanMobile.AcceptService"
        val channelName = "accept data service channel"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

//        if (isBluetoothSupport()) {}

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val notification = NotificationCompat.Builder(this, channelId).apply {
            setContentText("센서 데이터 감지중 입니다")
        }

        val notificationID = 12345
        startForeground(notificationID, notification.build())
        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        acceptThread = AcceptThread(bluetoothAdapter, applicationContext)
        acceptThread.start()

        csvWrite(60000 * 7) // 1분 * n
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

     private fun csvWrite(time: Long) {
         var i = 0
        val timer = Timer()
            val timerTask = object: TimerTask() {
                override fun run() {
                    Log.d("Accept Service", "CSV Write method called")
                    GlobalScope.launch {
                        sensorController.writeCsv(this@AcceptService, "HeartRate")
                        sensorController.writeCsv(this@AcceptService, "PpgGreen")
                    }

                    i ++
                    if(i == 6){
                        sendCSV()
                        i %= 6
                    }
                }
            }
        timer.schedule(timerTask, 0, time)

    }


    //From Sending Service
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