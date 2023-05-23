package com.example.asanmobile

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.asanmobile.sensor.controller.SensorController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SensorActivity() : AppCompatActivity() {
    private lateinit var serviceIntent : Intent
    private lateinit var sensorController: SensorController

    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnRename: Button
    private lateinit var btnCsvCheck: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        btnStart = findViewById<Button>(R.id.BtnStart)
        btnStart.setOnClickListener(View.OnClickListener {
            serviceStart()
            val sendIntent = Intent(this, SendingService::class.java)
            startService(sendIntent)
        })
        btnStop = findViewById<Button>(R.id.BtnStop)
        btnStop.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, AcceptService::class.java)
            stopService(intent)
            val sendIntent = Intent(this, SendingService::class.java)
            stopService(sendIntent)
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

    fun serviceStart() {
        serviceIntent = Intent(this, AcceptService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        }
        else {
            startService(serviceIntent)
        }
    }

    // 뒤로가기 버튼을 누를 때 액티비티 종료
    override fun onBackPressed() {
        finish()
    }
}