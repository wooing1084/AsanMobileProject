package com.hci.user_mobile.activity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.hci.user_mobile.common.DeviceInfo
import com.hci.user_mobile.common.SocketState
import com.hci.user_mobile.common.SocketStateEvent
import com.hci.user_mobile.sensor.controller.SensorController
import com.hci.user_mobile.service.AcceptService
import com.hci.user_mobile.databinding.ActivitySensorBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class SensorActivity() : AppCompatActivity() {
    private lateinit var serviceIntent : Intent
    private lateinit var sensorController: SensorController
    private lateinit var binding: ActivitySensorBinding

    /**
     * 뒤로가기 연속으로 누르면 앱 종료
     **/
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

//    @Subscribe
//    fun listenThreadState(event: ThreadStateEvent) {
//        if (ThreadState.STOP == event.state) {
//            Log.d("스레드 상태 감지", "서비스 종료")
//            val intent = Intent(this, AcceptService::class.java)
//            stopService(intent)
//            EventBus.getDefault().post(SocketStateEvent(SocketState.NONE))
//        }
//    }

}