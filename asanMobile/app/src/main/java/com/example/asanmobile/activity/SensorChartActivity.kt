package com.example.asanmobile.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.asanmobile.adapter.TextAdapter
import com.example.asanmobile.databinding.ActivityCsvPopupBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry

class SensorChartActivity: AppCompatActivity() {

    private lateinit var binding: ActivityCsvPopupBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TextAdapter
    private lateinit var itemList: MutableList<String>

    private lateinit var ppgGreenChart: LineChart
    private lateinit var heartRateChart: LineChart

    // 데이터 담을 리스트
    private var ppgGreenArr = ArrayList<Entry>()
    private var heartArr = ArrayList<Entry>()
    private var heartIndex: Float = 0F
    private var ppgGIndex: Float = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCsvPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

//        val filter = IntentFilter("my-event")
//        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

    }

    //    val receiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if (intent?.action == "my-event") {
//                val msg = intent.getStringExtra("message")
//                val data = msg?.split(":")
//                var extractData = data?.get(1)?.trim()?.toFloat()
//
//                if (msg != null) {
//                    println(msg)
////                    addItem(msg)
//
//                    if (data?.get(0).equals("heart")) {
//                        heartArr.add(Entry(heartIndex, extractData!!))
//
//                        val heartSet: LineDataSet = LineDataSet(heartArr, "Heart") // 데이터 넣기 // LineDataSet 변환
//
//                        val heartData = LineData() // 차트에 담길 데이터
//                        heartData.addDataSet(heartSet)
//
//                        // 차트에 데이터 추가
//                        heartRateChart?.setData(heartData)
//                        heartRateChart?.invalidate() // 차트 업데이트
//                        heartRateChart?.setTouchEnabled(false) // 차트 터치 disable
//                        heartIndex++
//                    }
//                    else if (data?.get(0).equals("ppgGreen")) {
//                        ppgGreenArr.add(Entry(ppgGIndex, extractData!!))
//
//                        val ppgGreenSet: LineDataSet // 데이터 넣기
//                        ppgGreenSet = LineDataSet(ppgGreenArr, "ppgGreenArr") // LineDataSet 변환
//
//                        val ppgGreenData = LineData() // 차트에 담길 데이터
//                        ppgGreenData.addDataSet(ppgGreenSet)
//
//                        // 차트에 데이터 추가
//                        ppgGreenChart?.setData(ppgGreenData)
//                        ppgGreenChart?.invalidate() // 차트 업데이트
//                        ppgGreenChart?.setTouchEnabled(false) // 차트 터치 disable
//                        ppgGIndex++
//                    }
//                }
//            }
//        }
//    }

//    fun sendBroadcast(msg: String) {
//        val intent = Intent("my-event")
//        intent.putExtra("message", msg)
//        localBroadcastManager.sendBroadcast(intent)
//    }

    // 리사이클러 뷰에 데이터 추가
//    fun addItem(item: String) {
//        itemList.add(item)
//        adapter.notifyItemInserted(itemList.size - 1)
//        recyclerView.scrollToPosition(itemList.size - 1)
//    }
}

