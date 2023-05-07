package com.example.asanmobile

import android.Manifest
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

class SensorActivity : AppCompatActivity() {

    private lateinit var serviceIntent : Intent

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TextAdapter
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var itemList: MutableList<String>

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
            stopService(intent)
        })

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        itemList = mutableListOf()

        adapter = TextAdapter(itemList)
        recyclerView.adapter = adapter

        val filter = IntentFilter("my-event")
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "my-event") {
                val msg = intent.getStringExtra("message")

                // 리사이클러뷰에 메시지 추가
                if (msg != null) {
                    println(msg)
                    addItem(msg)
                }
            }
        }
    }

    // 리사이클러 뷰에 데이터 추가
    fun addItem(item: String) {
        itemList.add(item)
        adapter.notifyItemInserted(itemList.size - 1)
        recyclerView.scrollToPosition(itemList.size - 1)
    }

    fun serviceStart() {
        serviceIntent = Intent(this, AcceptService::class.java)
//        serviceIntent.putExtra("ID", ID)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        }
        else {
            startService(serviceIntent)
        }
    }

}