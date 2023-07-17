package com.example.asanmobile.activity

import android.Manifest
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.asanmobile.R
import com.example.asanmobile.common.DeviceInfo
import com.example.asanmobile.common.SocketState
import com.example.asanmobile.common.SocketStateEvent
import com.example.asanmobile.common.ThreadState
import com.example.asanmobile.common.ThreadStateEvent
import com.example.asanmobile.databinding.ActivitySensorBinding
import com.example.asanmobile.sensor.controller.SensorController
import com.example.asanmobile.service.AcceptService
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class SensorActivity() : AppCompatActivity() {
    private lateinit var serviceIntent : Intent
    private lateinit var sensorController: SensorController
    private lateinit var binding: ActivitySensorBinding

    private val callback = object : OnBackPressedCallback(true) {
        var backPressedTime: Long = 0
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis()
                Toast.makeText(this@SensorActivity, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //로그인 페이지에서 온 id들 가져오기
        DeviceInfo.init(intent.getStringExtra("DeviceID").toString(),
            intent.getStringExtra("ID").toString())

//        setContentView(R.layout.activity_sensor)
        binding = ActivitySensorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sensorController = SensorController.getInstance(this)

        // 권한 허가
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                ), 1)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH), 1)
        }

        // 화면
        binding.stateLabel.text = SocketState.NONE.toString()
        binding.BtnStart.setOnClickListener {
            serviceStart()
        }
        binding.BtnStop.setOnClickListener {
            val intent = Intent(this, AcceptService::class.java)
            stopService(intent)
            EventBus.getDefault().post(SocketStateEvent(SocketState.CLOSE))
        }
        binding.BtnCsvCheck.setOnClickListener {
            val intent = Intent(this, CsvPopupActivity::class.java)
            startActivity(intent)
        }
        binding.BtnToChartActivity.setOnClickListener {
            val intent = Intent(this, SensorChartActivity::class.java)
            startActivity(intent)
        }

        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this)
    }

    private fun serviceStart() {
        serviceIntent = Intent(this, AcceptService::class.java)
        startService(serviceIntent)
    }

    @Subscribe
    fun listenSocketState(event: SocketStateEvent) {
        val state = event.state.name
        runOnUiThread {
            binding.stateLabel.text = state
        }
    }

    @Subscribe
    fun listenThreadState(event: ThreadStateEvent) {
        if (ThreadState.STOP == event.state) {
            Log.d("스레드 상태 감지", "서비스 종료")
            val intent = Intent(this, AcceptService::class.java)
            stopService(intent)
            EventBus.getDefault().post(SocketStateEvent(SocketState.NONE))
        }
    }

}