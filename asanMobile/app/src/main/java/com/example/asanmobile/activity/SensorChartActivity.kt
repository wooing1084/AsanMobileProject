package com.example.asanmobile.activity

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.asanmobile.common.CsvController
import com.example.asanmobile.common.RegexManager
import com.example.asanmobile.databinding.ActivityChartBinding
import com.example.asanmobile.sensor.controller.SensorController
import com.example.asanmobile.sensor.model.SensorEnum
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

//TODO
//값이 없는부분은 0으로 지정해야한다.

//현재는 숫자로 나오지만, 시간으로 나오게 해야함
//처음에 시간을 fix해놓고 해당 시간에 값을 넣는다.

//선택이나 줌 안되게 막아야함

// 여기에 수신해서 받는 거로 적용해보려 했으나, 수신 안됨
class SensorChartActivity : AppCompatActivity() {
    private var timer: Timer? = null

    var startTime = 0L

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


    protected override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        timer = null
    }

    class LineChartXAxisValueFormatter : IndexAxisValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            // Show time in local version
            val unixTime = System.currentTimeMillis() / 1000L - value.toLong()
            val timestamp = Date(unixTime * 1000L)
            val dateTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return dateTimeFormat.format(timestamp)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ppgGreenChart = binding.chartPpgGreen
        heartRateChart = binding.chartHeart

        //차트 x라벨 하단에 위치
        ppgGreenChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        heartRateChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        ppgGreenChart.xAxis.valueFormatter = LineChartXAxisValueFormatter()
        heartRateChart.xAxis.valueFormatter = LineChartXAxisValueFormatter()

        makeBufferChart()

        this.onBackPressedDispatcher.addCallback(this, callback)
    }


    /**
    sensorName : 요약 센서명
    bin : 데이터 사이의 간격(초)
    len : 총 데이터의 시간 길이(초)
     */
    private suspend fun sumData(sensorName: String, bin: Int, len: Long): ArrayDeque<Entry> {
        startTime = System.currentTimeMillis() / 1000L

        val bufferQueue = ArrayDeque<Entry>()
        val sensorTemp = ArrayList<Pair<Int, Float>>()
        val count = ArrayList<Int>()

        for (i in 0 until (len / (bin)).toInt()) {
//            sensorTemp.add(Pair(startTime - (i * bin), 0f))
//            sensorTemp.add(Pair(i.toLong(), 0f))
            sensorTemp.add(Pair((i * bin), 0f))
            count.add(0)
        }

        SensorController.getInstance(this@SensorChartActivity).getDataFromNow(sensorName, len * 1000)
            .let {

                for ((iter, data) in it.withIndex()) {
                    val time = data.time / 1000L
                    val value = data.value

                    val dif = kotlin.math.abs(startTime - time)
                    val idx = (dif / bin).toInt()

                    if (idx < 0 ||idx >= sensorTemp.size) {
                        continue
                    }

                    sensorTemp[idx] = Pair(sensorTemp[idx].first, sensorTemp[idx].second + value)
    //                  sensorTemp[idx] = Pair(idx.toLong(), sensorTemp[idx]!!.second + value)
                    count[idx] = count[idx] + 1

                }
            }

        for (i in 0 until sensorTemp.size) {
            if (count[i] == 0) {
                bufferQueue.add(
                    Entry(
                        sensorTemp[i].first.toFloat(),
                        0f
                    )
                )
            }
            else{
                bufferQueue.add(
                    Entry(
                        sensorTemp[i].first.toFloat(),
                        sensorTemp[i].second / count[i]
                    )
                )
            }

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

    private fun makeBufferChart() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                Log.d("Chart Activity", "Timer called!")
                runOnUiThread {
                    GlobalScope.launch {
                        ppgGreenChart.clear()
                        heartRateChart.clear()

                        val ppgGreenBufferQueue = sumData(SensorEnum.PPG_GREEN.value, 60, 10 * 60)
                        val heartBufferQueue = sumData(SensorEnum.HEART_RATE.value, 60, 10 * 60)

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
            }
        }, 0, 10000)
    }


    private fun makeStatisticsChart() {
        ppgGreenChart.clear()
        heartRateChart.clear()

        val ppgFile = CsvController.getFile(
            CsvController.getExternalPath(
                this,
                "sensor"
            ) + "/statistics/PpgGreen_mean.csv"
        )
        val heartFile = CsvController.getFile(
            CsvController.getExternalPath(
                this,
                "sensor"
            ) + "/statistics/HeartRate_mean.csv"
        )

        if (ppgFile == null || heartFile == null) {
            Log.d(TAG, "No statistics file.")
            return
        }

        val ppgReader = CSVReader(InputStreamReader(ppgFile?.inputStream()))
        val heartReader = CSVReader(InputStreamReader(heartFile?.inputStream()))

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
}

