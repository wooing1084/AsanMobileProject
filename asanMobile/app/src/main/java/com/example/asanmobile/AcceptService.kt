package com.example.asanmobile

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.provider.Settings.Global
import androidx.core.app.NotificationCompat
import com.example.asanmobile.sensor.controller.SensorController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

class AcceptService: Service() {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var acceptThread: AcceptThread
    private val sensorController: SensorController = SensorController.getInstance(this)
    private val context: Context = this

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
        val timer = Timer()
            val timerTask = object: TimerTask() {
                override fun run() {
                    GlobalScope.launch {
                        sensorController.writeCsv(this@AcceptService, "HeartRate")
                        sensorController.writeCsv(this@AcceptService, "PpgGreen")
                    }
                }
            }
        timer.schedule(timerTask, 0, time)

    }

}