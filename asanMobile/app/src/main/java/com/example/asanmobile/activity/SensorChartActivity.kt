package com.example.asanmobile.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.asanmobile.databinding.ActivityChartBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlin.collections.ArrayDeque

class SensorChartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChartBinding

    private lateinit var ppgGreenChart: LineChart
    private lateinit var heartRateChart: LineChart

    // 데이터 담을 리스트
    private var ppgGreenQueue = ArrayDeque<Entry>()
    private var heartQueue = ArrayDeque<Entry>()
    private var heartIndex: Float = 0F
    private var ppgGIndex: Float = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ppgGreenChart = binding.chartPpgGreen
        heartRateChart = binding.chartHeart
        registerReceiver(receiver, IntentFilter())
    }

    // service와 통신하기 위한 BroadcastReceiver
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "heart") {
                val msg = intent.getStringExtra("message")
                val data = msg?.split(":")
                var extractData = data?.get(1)?.trim()?.toFloat()

                if (msg != null) {
                    println(msg)

                    if (data?.get(0).equals("heart")) {
                        heartQueue.add(Entry(heartIndex, extractData!!))

                        val heartSet: LineDataSet =
                            LineDataSet(heartQueue, "Heart") // 데이터 넣기 // LineDataSet 변환

                        val heartData = LineData() // 차트에 담길 데이터
                        heartData.addDataSet(heartSet)

                        // 차트에 데이터 추가
                        heartRateChart?.setData(heartData)
                        heartRateChart?.invalidate() // 차트 업데이트
                        heartRateChart?.setTouchEnabled(false) // 차트 터치 disable
                        heartIndex++
                    }
                }
            }
//            } else if (intent?.action == "ppgGreen") {
//                if (data?.get(0).equals("ppgGreen")) {
//                    ppgGreenQueue.add(Entry(ppgGIndex, extractData!!))
//
//                    val ppgGreenSet: LineDataSet // 데이터 넣기
//                    ppgGreenSet = LineDataSet(ppgGreenQueue, "ppgGreenArr") // LineDataSet 변환
//
//                    val ppgGreenData = LineData() // 차트에 담길 데이터
//                    ppgGreenData.addDataSet(ppgGreenSet)
//
//                    // 차트에 데이터 추가
//                    ppgGreenChart?.setData(ppgGreenData)
//                    ppgGreenChart?.invalidate() // 차트 업데이트
//                    ppgGreenChart?.setTouchEnabled(false) // 차트 터치 disable
//                    ppgGIndex++
//                }
//            }
        }
    }
}

