package com.example.asanmobile.activity

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.asanmobile.common.CsvController
import com.example.asanmobile.common.RegexManager
import com.example.asanmobile.databinding.ActivityChartBinding
import com.example.asanmobile.sensor.controller.SensorController
import com.example.asanmobile.sensor.model.SensorEnum
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.opencsv.CSVReader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.lang.Math.abs
import java.sql.Date
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

//TODO
//값이 없는부분은 0으로 지정해야한다.
//현재는 숫자로 나오지만, 시간으로 나오게 해야함
//선택이나 줌 안되게 막아야함

// 여기에 수신해서 받는 거로 적용해보려 했으나, 수신 안됨
class SensorChartActivity : AppCompatActivity() {
    private var timer: Timer? = null

    private lateinit var binding: ActivityChartBinding
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // 뒤로가기 클릭 시 종료
            finish()
        }
    }
    private val regexManager: RegexManager = RegexManager.getInstance(this)

    private lateinit var ppgGreenChart: LineChart
    private lateinit var heartRateChart: LineChart
    private lateinit var ppgGreenBufferChart : LineChart
    private lateinit var heartRateBufferChart : LineChart



    // 데이터 담을 리스트
//    private var heartIndex: Float = 0F
//    private var ppgGIndex: Float = 0F

    protected override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    class LineChartXAxisValueFormatter : IndexAxisValueFormatter() {
        override fun getFormattedValue(value: Float): String {


            // Show time in local version
            val time = Date(value.toLong())
            val dateTimeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
            return dateTimeFormat.format(time)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ppgGreenChart = binding.chartPpgGreen
        heartRateChart = binding.chartHeart

//        ppgGreenChart.xAxis.setValueFormatter(LineChartXAxisValueFormatter())
//        heartRateChart.xAxis.setValueFormatter(LineChartXAxisValueFormatter())

//       makeStatisticsChart()
//
//        val fileObserver: FileObserver = object : FileObserver(CsvController.getExternalPath(this,"sensor") + "/statistics") {
//            override fun onEvent(event: Int, path: String?) {
//                when (event) {
//                    MODIFY -> {
//                        makeStatisticsChart()
//                    }
//                }
//            }
//        }
//        fileObserver.startWatching()

        makeBufferChart()

        this.onBackPressedDispatcher.addCallback(this, callback)
    }


    /**
    sensorName : 요약 센서명
    bin : 데이터 사이의 간격(초)
    len : 총 데이터의 시간 길이(ms)
    */
    private suspend fun sumDatas(sensorName : String, bin : Int, len : Long): ArrayDeque<Entry> {
        val startTime = System.currentTimeMillis()

        val bufferQueue = ArrayDeque<Entry>()
        val sensorTemp = arrayOfNulls<Pair<Long, Float>>((len/ (bin * 1000)).toInt())
        val count = arrayOfNulls<Int>((len/ (bin * 1000)).toInt())

        SensorController.getInstance(this@SensorChartActivity).getDataFromNow(sensorName,len)?.let {

            for ((iter, data) in it.withIndex()) {
                val time = data.time
                val value = data.value

                val dif = (startTime - time) / 1000

                var idx = (dif / bin).toInt()

                if(idx >= sensorTemp.size){
                    idx = sensorTemp.size - 1
                }

                if(sensorTemp[idx] == null){
//                    sensorTemp[idx] = Pair(time,value)
                    sensorTemp[idx] = Pair(idx.toLong(),value)
                    count[idx] = 1
                }
                else{
//                    sensorTemp[idx] = Pair(sensorTemp[idx]!!.first, sensorTemp[idx]!!.second + value)
                    sensorTemp[idx] = Pair(idx.toLong(), sensorTemp[idx]!!.second + value)
                    count[idx] = count[idx]!! + 1
                }
            }
        }

        for (i in sensorTemp.indices){
            if(sensorTemp[i] == null){
                continue
            }
            bufferQueue.add(Entry(sensorTemp[i]!!.first.toFloat(),sensorTemp[i]!!.second / count[i]!!))
        }
        return bufferQueue

    }


//    if(sensorTemp.size == 0)
//    {
//        val pair: Pair<Long,Float> = Pair(time,value)
//        sensorTemp.add(pair)
//
//        continue
//    }
//
//    val nowDate = Date(time)
//    val nowMinute = SimpleDateFormat("hh:mm:ss").format(nowDate).split(":")[1].toInt()
//
//    val firstDate = Date(sensorTemp.get(0).first)
//    val firstMinute = SimpleDateFormat("hh:mm:ss").format(firstDate).split(":")[1].toInt()
//
//    if(nowMinute == firstMinute){
//        var sum = 0.0
//        for(i in sensorTemp){
//            sum += i.second
//        }
//        val mean = sum / sensorTemp.size
//        bufferQueue.add(Entry(sensorTemp.get(0).first.toFloat(),mean.toFloat()))
//        sensorTemp.clear()
//
//        continue
//
//    }
//    else{
//        val pair: Pair<Long,Float> = Pair(time,value)
//        sensorTemp.add(pair)
//    }
//
//    if(it.size - 1 == iter){
//        if(sensorTemp.size == 0)
//            continue
//        var sum = 0.0
//        for(i in sensorTemp){
//            sum += i.second
//        }
//        val mean = sum / sensorTemp.size
//        bufferQueue.add(Entry(sensorTemp.get(0).first.toFloat(),mean.toFloat()))
//        sensorTemp.clear()
//    }

    private fun makeBufferChart(){
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                Log.d("Chart Activity", "Timer called!")
                GlobalScope.launch {
                    ppgGreenChart.clear()
                    heartRateChart.clear()



                    val ppgGreenBufferQueue = sumDatas(SensorEnum.PPG_GREEN.value,60,10 * 60 * 1000)
                    val heartBufferQueue = sumDatas(SensorEnum.HEART_RATE.value,60, 10 * 60 * 1000)


                    val ppgGreenDataSet = LineDataSet(ppgGreenBufferQueue.toList(), "ppgGreen")

                    val heartDataSet = LineDataSet(heartBufferQueue.toList(), "heartRate")

                    val ppgGreenData = LineData(ppgGreenDataSet)
                    val heartData = LineData(heartDataSet)

                    ppgGreenChart.data = ppgGreenData
                    heartRateChart.data = heartData

                    ppgGreenChart.invalidate()
                    heartRateChart.invalidate()
                }
            }
        }, 0, 10000)
    }

    private fun makeStatisticsChart(){
        ppgGreenChart.clear()
        heartRateChart.clear()

        val ppgFile = CsvController.getFile(CsvController.getExternalPath(this,"sensor") + "/statistics/PpgGreen_mean.csv" )
        val heartFile = CsvController.getFile(CsvController.getExternalPath(this,"sensor") + "/statistics/HeartRate_mean.csv")

        if(ppgFile == null || heartFile == null) {
            Log.d(TAG, "No statistics file.")
            return
        }

        val ppgReader = CSVReader(InputStreamReader(ppgFile?.inputStream()))
        val heartReader =CSVReader(InputStreamReader(heartFile?.inputStream()))

        val ppgList = ppgReader.readAll()
        val heartList = heartReader.readAll()

        val ppgGreenQueue = ArrayDeque<Entry>()
        val heartQueue = ArrayDeque<Entry>()

        var i = 0
        for (ppg in ppgList) {
            i++
            val time = ppg[0].toFloat()
            val data = ppg[1].toFloat()
            ppgGreenQueue.add(Entry(i.toFloat(), data))
//            ppgGreenQueue.add(Entry(time, data))

        }

        i = 0
        for (heart in heartList) {
            i++
            val time = heart[0].toFloat()
            val data = heart[1].toFloat()
            heartQueue.add(Entry(i.toFloat(), data))
//            ppgGreenQueue.add(Entry(time, data))
        }

        val ppgGreenDataSet = LineDataSet(ppgGreenQueue.toList(), "ppgGreen")
        val heartDataSet = LineDataSet(heartQueue.toList(), "heartRate")

        val ppgGreenData = LineData(ppgGreenDataSet)
        val heartData = LineData(heartDataSet)

        ppgGreenChart.data = ppgGreenData
        heartRateChart.data = heartData

        ppgGreenChart.invalidate()
        heartRateChart.invalidate()
    }

    //들어오는 즉시 그래프를 그리기 위한 현석이형 코드
    // service와 통신하기 위한 BroadcastReceiver
//    private val receiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if (Intent.ACTION_ATTACH_DATA.equals(intent?.action)) {
//                val receivedString = intent?.getStringExtra("data")
//                Log.d(TAG, "received: " + receivedString.toString())
//
//                val hrList = regexManager.hrRegex.findAll(receivedString.toString())
//                val pgList = regexManager.pgRegex.findAll(receivedString.toString())
//
//                CoroutineScope(Dispatchers.IO).launch {
//                    try {
//                        for (hrPattern in hrList) {
//                            val hrVal = hrPattern.value
//                            val resRex = regexManager.valueRegex.find(hrVal)
//                            val res = resRex?.value
//
//                            val dataMap = regexManager.dataExtract(res!!)
//                            heartBufferQueue.add(Entry(dataMap.time.toFloat(), dataMap.data))
//
//                            val heartSet: LineDataSet =
//                                LineDataSet(heartBufferQueue, "Heart") // 데이터 넣기 // LineDataSet 변환
//
//                            val heartData = LineData() // 차트에 담길 데이터
//                            heartData.addDataSet(heartSet)
//
//                            // 차트에 데이터 추가
//                            heartRateChart.data = heartData
//                            heartRateChart.setTouchEnabled(false) // 차트 터치 disable
//                            heartRateChart.invalidate() // 차트 업데이트
//                        }
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//
//                CoroutineScope(Dispatchers.IO).launch {
//                    try {
//                        for (pgPattern in pgList) {
//                            val pgVal = pgPattern.value
//                            val resRex = regexManager.valueRegex.find(pgVal)
//                            val res = resRex?.value
//
//                            val dataMap = regexManager.dataExtract(res!!)
//                            ppgGreenBufferQueue.add(Entry(dataMap.time.toFloat(), dataMap.data))
//
//                            val ppgGreenSet = LineDataSet(
//                                ppgGreenBufferQueue,
//                                    "ppgGreenArr"
//                                ) // 데이터 넣기 // LineDataSet 변환
//
//                            val ppgGreenData = LineData() // 차트에 담길 데이터
//                            ppgGreenData.addDataSet(ppgGreenSet)
//
//                            // 차트에 데이터 추가
//                            ppgGreenChart.data = ppgGreenData
//                            ppgGreenChart.setTouchEnabled(false) // 차트 터치 disable
//                            ppgGreenChart.invalidate() // 차트 업데이트
//
//                        }
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//            }
//        }
//    }
}

