package com.gachon_HCI_Lab.user_mobile.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.gachon_HCI_Lab.user_mobile.R
import com.gachon_HCI_Lab.user_mobile.activity.SensorActivity
import com.gachon_HCI_Lab.user_mobile.common.CsvController.getExistFileName
import com.gachon_HCI_Lab.user_mobile.common.CsvController.getExternalPath
import com.gachon_HCI_Lab.user_mobile.common.CsvController.getFile
import com.gachon_HCI_Lab.user_mobile.common.CsvController.moveFile
import com.gachon_HCI_Lab.user_mobile.common.DeviceInfo
import com.gachon_HCI_Lab.user_mobile.common.ServerConnection
import com.gachon_HCI_Lab.user_mobile.common.SocketState
import com.gachon_HCI_Lab.user_mobile.common.SocketStateEvent
import com.gachon_HCI_Lab.user_mobile.common.ThreadState
import com.gachon_HCI_Lab.user_mobile.common.ThreadStateEvent
import com.gachon_HCI_Lab.user_mobile.sensor.controller.SensorController
import com.gachon_HCI_Lab.user_mobile.sensor.model.SensorEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.reflect.Method
import java.util.Timer
import java.util.TimerTask

/**
 * 포그라운드 서비스
 * 웨어러블을 통해 수집된 데이터를 폰을 통해 수집
 * 백그라운드에서 수집 + csv 생성 + 서버로 csv 전송
 * */
class AcceptService : Service() {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var acceptThread: AcceptThread
    private val sensorController: SensorController = SensorController.getInstance(this@AcceptService)
    private var timer: Timer? = null

    //From Sending Service
    private val tag = "Sending Service"
    private var isConnected: Boolean = false

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // 블루투스를 지원하지 않는 경우
        if (!isBluetoothSupport(bluetoothAdapter)) {
            onDestroy()
        }

        // Bluetooth 비활성화 상태인 경우
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!bluetoothAdapter.isEnabled) {
                Toast.makeText(this@AcceptService, "블루투스를 활성화해주세요", Toast.LENGTH_SHORT).show()
                onDestroy()
                return START_NOT_STICKY
            }
            if (!pairingBluetoothConnected()) {
                Toast.makeText(this@AcceptService, "기기를 연결해주세요", Toast.LENGTH_SHORT).show()
                onDestroy()
                return START_NOT_STICKY
            }
        }
        setForeground()
        startForeground()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::acceptThread.isInitialized) {
            acceptThread.clear()
        }
        Log.d("Accept Service", "onDestroy")
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
        EventBus.getDefault().post(SocketStateEvent(SocketState.CLOSE))

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this)
    }

    private fun startForeground() {
        BluetoothConnect.createBluetoothAdapter(bluetoothAdapter)
        acceptThread = AcceptThread(this)
        createEventBus()
        acceptThread.start()
    }

    private fun createEventBus(){
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    private fun setForeground() {
        Log.d("setForegroundNotification", "notification")
        val channelId = "com.user_mobile.AcceptService"
        val channelName = "accept data service channel"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, SensorActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(this, channelId).apply {
            setContentTitle("Asan Service")
            setContentText("센서 데이터 감지중 입니다")
            setSmallIcon(R.mipmap.ic_launcher)
            setContentIntent(pendingIntent)
        }

        val notificationID = 1
        startForeground(notificationID, notification.build())
    }

    /**
     * 블루투스 지원 여부 판별 메소드
     * */
    private fun isBluetoothSupport(bluetoothAdapter: BluetoothAdapter): Boolean {
        return if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth 지원을 하지 않는 기기입니다.", Toast.LENGTH_SHORT).show()
            false
        } else true
    }

    /**
     * 블루투스 연결 여부 판별 메소드
     * */
    private fun isConnected(device: BluetoothDevice): Boolean {
        return try {
            val method: Method = device.javaClass.getMethod("isConnected")
            val connected: Boolean = method.invoke(device) as Boolean
            connected
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    /**
     * 페어링 된 기기와 연결 확인 메소드
     * */
    private fun pairingBluetoothConnected(): Boolean {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val bluetoothDevices: Set<BluetoothDevice> =
                    bluetoothAdapter.bondedDevices
                for (bluetoothDevice in bluetoothDevices) {
                    if (isConnected(bluetoothDevice)) {
                        // 연결 중이 아닌 상태
                        return true
                    }
                }
            }
        } catch (e: NullPointerException) {
            // 블루투스 서비스 사용 불가인 경우 처리
        }
        return false
    }

    /**
     * csv를 작성하는 메소드
     * 타이머가 있어 입력받은 시간마다 동작
     * count는 주기를 의미
     * 주기마다 csv를 서버로 전송하는 메소드인 sendCSV 호출
     * input: 시간(단위: unixTime)
     * ex) csvWrite(60000) -> 1분마다 동작
     * */

    private fun csvWrite(time: Long) {
        var count = 0
        if (timer != null) {
            Log.d("Accept Service", "timer is already running")
            return
        }
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                Log.d("Accept Service", "CSV Write method called")
                CoroutineScope(Dispatchers.IO).launch {
                    if (isConnected) {
                        sensorController.writeCsv(this@AcceptService, "OneAxis")
                        sensorController.writeCsv(this@AcceptService, "ThreeAxis")
                        count++
                        if (count == 6) {
                            sendCSV()
                            count %= 6
                        }
                    }
                }
            }
        }, 0, time)
    }


    //From Sending Service
    /**
     * CSV를 서버로 전송하는 메소드
     * */
    private fun sendCSV() {

        for (sensorName in SensorEnum.values()) {
            val fileName = getExistFileName(this, sensorName.value) ?: continue

            val srcPath = getExternalPath(this, "sensor") + "/" + fileName
            val destPath = getExternalPath(this, "sensor/sended") + "/" + fileName

            moveFile(srcPath, destPath)
            val srcFile = getFile(srcPath)
            srcFile?.delete()

            val file = getFile(destPath)

            if (file != null) {
                val token = fileName.split('_')
                val ppgTime = token[1].split('.')[0]

                ServerConnection.postFile(file, DeviceInfo._uID, DeviceInfo._battery, ppgTime)
                Log.d(tag, sensorName.name + " sensor file sending!")
            }

        }

    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun listenSocketState(event: ThreadStateEvent) {
        isConnected = when (event.state) {
            ThreadState.RUN -> {
                Log.d(this.tag, "SOCKET_CONNECT!")
                csvWrite(1000 * 60 * 1)
                true
            }
            else -> {
                Log.d(this.tag, "SOCKET_CLOSE!")
                false
            }
        }
    }
}
