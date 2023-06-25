package com.example.asanmobile.activity

import android.content.*
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.asanmobile.common.RegexManager
import com.example.asanmobile.databinding.ActivityChartBinding
import com.example.asanmobile.sensor.model.HeartRate
import com.example.asanmobile.sensor.model.PpgGreen
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.ArrayDeque


// 여기에 수신해서 받는 거로 적용해보려 했으나, 수신 안됨
class SensorChartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChartBinding
    private val regexManager: RegexManager = RegexManager.getInstance(this)

    private lateinit var ppgGreenChart: LineChart
    private lateinit var heartRateChart: LineChart

    // 데이터 담을 리스트
    private var ppgGreenQueue = ArrayDeque<Entry>()
    private var heartQueue = ArrayDeque<Entry>()
//    private var heartIndex: Float = 0F
//    private var ppgGIndex: Float = 0F

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
            if (Intent.ACTION_ATTACH_DATA.equals(intent?.action)) {
                val receivedString = intent?.getStringExtra("data")
                Log.d(TAG, "received: " + receivedString.toString())

                val hrList = regexManager.hrRegex.findAll(receivedString.toString())
                val pgList = regexManager.pgRegex.findAll(receivedString.toString())

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        for (hrPattern in hrList) {
                            val hrVal = hrPattern.value
                            val resRex = regexManager.valueRegex.find(hrVal)
                            val res = resRex?.value

                            val dataMap = regexManager.dataExtract(res!!)
                            heartQueue.add(Entry(dataMap.time.toFloat(), dataMap.data))

                            val heartSet: LineDataSet =
                                LineDataSet(heartQueue, "Heart") // 데이터 넣기 // LineDataSet 변환

                            val heartData = LineData() // 차트에 담길 데이터
                            heartData.addDataSet(heartSet)

                            // 차트에 데이터 추가
                            heartRateChart.data = heartData
                            heartRateChart.invalidate() // 차트 업데이트
                            heartRateChart.setTouchEnabled(false) // 차트 터치 disable
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        for (pgPattern in pgList) {
                            val pgVal = pgPattern.value
                            val resRex = regexManager.valueRegex.find(pgVal)
                            val res = resRex?.value

                            val dataMap = regexManager.dataExtract(res!!)
                            ppgGreenQueue.add(Entry(dataMap.time.toFloat(), dataMap.data))

                            val ppgGreenSet: LineDataSet =
                                LineDataSet(
                                    ppgGreenQueue,
                                    "ppgGreenArr"
                                ) // 데이터 넣기 // LineDataSet 변환

                            val ppgGreenData = LineData() // 차트에 담길 데이터
                            ppgGreenData.addDataSet(ppgGreenSet)

                            // 차트에 데이터 추가
                            ppgGreenChart.data = ppgGreenData
                            ppgGreenChart.invalidate() // 차트 업데이트
                            ppgGreenChart.setTouchEnabled(false) // 차트 터치 disable

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}

