package com.example.asanmobile

import android.Manifest
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.asanmobile.sensor.controller.SensorController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet


class SensorActivity() : AppCompatActivity() {
    private lateinit var serviceIntent : Intent
    private var sensorController: SensorController = SensorController.getInstance(applicationContext)

//    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: TextAdapter
//    private lateinit var itemList: MutableList<String>

    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnRename: Button

    private lateinit var ppgGreenChart: LineChart
    private lateinit var heartRateChart: LineChart

    // 데이터 담을 리스트
    private var ppgGreenArr = ArrayList<Entry>()
    private var heartArr = ArrayList<Entry>()
    private var heartIndex: Float = 0F
    private var ppgGIndex: Float = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)

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
        })
        btnStop = findViewById<Button>(R.id.BtnStop)
        btnStop.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, AcceptService::class.java)
//            intent.putExtra("controller", sensorController)
            stopService(intent)
        })

        // 리사이클러 뷰 관련 코드
//        recyclerView = findViewById(R.id.recyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        itemList = mutableListOf()
//
//        adapter = TextAdapter(itemList)
//        recyclerView.adapter = adapter
//        adapter.notifyDataSetChanged()

//        ppgGreenChart = findViewById<LineChart>(R.id.chart_ppgGreen)
//        heartRateChart = findViewById<LineChart>(R.id.chart_heart)

        val filter = IntentFilter("my-event")
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "my-event") {
                val msg = intent.getStringExtra("message")
                val data = msg?.split(":")
                var extractData = data?.get(1)?.trim()?.toFloat()

                if (msg != null) {
                    println(msg)
//                    addItem(msg)

                    if (data?.get(0).equals("heart")) {
                        heartArr.add(Entry(heartIndex, extractData!!))

                        val heartSet: LineDataSet // 데이터 넣기
                        heartSet = LineDataSet(heartArr, "Heart") // LineDataSet 변환

                        val heartData = LineData() // 차트에 담길 데이터
                        heartData.addDataSet(heartSet)

                        // 차트에 데이터 추가
                        heartRateChart?.setData(heartData)
                        heartRateChart?.invalidate() // 차트 업데이트
                        heartRateChart?.setTouchEnabled(false) // 차트 터치 disable
                        heartIndex++
                    }
                    else if (data?.get(0).equals("ppgGreen")) {
                        ppgGreenArr.add(Entry(ppgGIndex, extractData!!))

                        val ppgGreenSet: LineDataSet // 데이터 넣기
                        ppgGreenSet = LineDataSet(ppgGreenArr, "ppgGreenArr") // LineDataSet 변환

                        val ppgGreenData = LineData() // 차트에 담길 데이터
                        ppgGreenData.addDataSet(ppgGreenSet)

                        // 차트에 데이터 추가
                        ppgGreenChart?.setData(ppgGreenData)
                        ppgGreenChart?.invalidate() // 차트 업데이트
                        ppgGreenChart?.setTouchEnabled(false) // 차트 터치 disable
                        ppgGIndex++
                    }
                }
            }
        }
    }

    // 리사이클러 뷰에 데이터 추가
//    fun addItem(item: String) {
//        itemList.add(item)
//        adapter.notifyItemInserted(itemList.size - 1)
//        recyclerView.scrollToPosition(itemList.size - 1)
//    }

    fun serviceStart() {
        serviceIntent = Intent(applicationContext, AcceptService::class.java)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        }
        else {
            startService(serviceIntent)
        }
    }

}