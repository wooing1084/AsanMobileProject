package com.example.asanmobile.activity

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.asanmobile.*
import com.example.asanmobile.common.DeviceInfo
import com.example.asanmobile.common.SocketState
import com.example.asanmobile.common.SocketStateEvent
import com.example.asanmobile.sensor.controller.SensorController
import com.example.asanmobile.service.AcceptService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class SensorActivity() : AppCompatActivity() {
    private lateinit var serviceIntent : Intent
    private lateinit var sensorController: SensorController

    private lateinit var stateLabel: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnRename: Button
    private lateinit var btnCsvCheck: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //로그인 페이지에서 온 id들 가져오기
        DeviceInfo.init(intent.getStringExtra("DeviceID").toString(),
            intent.getStringExtra("ID").toString())


        setContentView(R.layout.activity_sensor)
        sensorController = SensorController.getInstance(this)

        // 권한 허가
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                1
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH
                ),
                1
            )
        }

        // 화면
        stateLabel = findViewById(R.id.stateLabel)
        stateLabel.setText(SocketState.NONE.toString())

        btnStart = findViewById<Button>(R.id.BtnStart)
        btnStart.setOnClickListener(View.OnClickListener {
            serviceStart()
        })
        btnStop = findViewById<Button>(R.id.BtnStop)
        btnStop.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, AcceptService::class.java)
            if(intent != null)
                stopService(intent)
//            val sendIntent = Intent(this, SendingService::class.java)
//            if(sendIntent != null)
//                stopService(sendIntent)
        })

//        btnCsv = findViewById<Button>(R.id.sameleButton)
//        btnCsv.setOnClickListener(View.OnClickListener {
//            GlobalScope.launch {
//                sensorController.writeCsv(this@SensorActivity, "HeartRate")
//            }
//        })

        btnCsvCheck = findViewById<Button>(R.id.BtnCsvCheck)
        btnCsvCheck.setOnClickListener {
            val intent = Intent(this, CsvPopupActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this)
    }

    fun serviceStart() {
//        val str = this.packageName + ".AcceptService"
        val manager = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (service.service.className.contains("AcceptService")) {
                Log.d("SensorActivity", "Service Already Running")
                return
            }
        }

        serviceIntent = Intent(this, AcceptService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        }
        else {
            startService(serviceIntent)
        }

//        val sendIntent = Intent(this, SendingService::class.java)
//        startService(sendIntent)
    }

    @Subscribe
    fun listenSocketState(event: SocketStateEvent) {
        val state = event.state.name
        runOnUiThread {
            stateLabel.text = state
        }
    }

    // 뒤로가기 버튼을 누를 때 액티비티 종료
    override fun onBackPressed() {
        finish()
    }
}