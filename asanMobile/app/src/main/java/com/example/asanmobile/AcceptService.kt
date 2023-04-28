package com.example.asanmobile

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class AcceptService: Service() {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var acceptThread: AcceptThread
    private lateinit var localBroadcastManager: LocalBroadcastManager

    // 블루투스 기능 on/off 확인
//    /**
//     * System Bluetooth On Check
//     */
//    val isOn: Boolean
//        get() = bluetoothAdapter.isEnabled
//
//    /**
//     * System Bluetooth On
//     */
//    fun on(activity: AppCompatActivity) {
//        if (!bluetoothAdapter.isEnabled) {
//            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            if (ActivityCompat.checkSelfPermission(
//                    applicationContext,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                activity.startActivityForResult(intent, INTENT_REQUEST_BLUETOOTH_ENABLE)
//            }
//        }
//    }
//
//    /**
//     * System Bluetooth On Result
//     */
//    fun onServiceResult(requestCode: Int, resultCode: Int): Boolean {
//        return (requestCode == INTENT_REQUEST_BLUETOOTH_ENABLE
//                && Activity.RESULT_OK == resultCode)
//    }
//
//    /**
//     * System Bluetooth Off
//     */
//    fun off() {
//        if (ActivityCompat.checkSelfPermission(
//                applicationContext,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            if (bluetoothAdapter.isEnabled) bluetoothAdapter.disable()
//        }
//    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // 서비스에서 시작시 블루투스가 켜져있는지 확인하는 기능 추가중...
//        val mIntent: Intent? = intent
//        val bundle: Bundle = Bundle()
//        val receiver: MainActivity? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            mIntent?.getParcelableExtra("RECEIVER", MainActivity::class.java)
//        } else {
//            TODO("VERSION.SDK_INT < TIRAMISU")
//            mIntent?.getParcelableExtra("RECEIVER")
//        }

//        // 로그인시 받아온 ID 넘겨받기
//        if (intent != null) {
////            ID = intent.getStringExtra("ID").toString()
//        }

        val channelId = "com.asanMobile.AcceptService"
        val channelName = "accept data service channel"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            var manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val notification = NotificationCompat.Builder(this, channelId).apply {
            setContentText("센서 데이터 감지중 입니다")
        }

        val notificationID = 12345
        startForeground(notificationID, notification.build())
        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        acceptThread = AcceptThread(bluetoothAdapter, this)
        acceptThread.start()

        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        return START_STICKY
    }

    fun sendBroadcast(msg: String) {
        val intent = Intent("my-event")
        intent.putExtra("message", msg)
        localBroadcastManager.sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}