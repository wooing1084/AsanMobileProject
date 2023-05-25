package com.example.asanmobile

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.example.asanmobile.sensor.controller.SensorController
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(MockitoJUnitRunner::class)
class ExampleUnitTest {

    @Mock
    lateinit var context: Context
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun regex_test() {
        val trueString: String = "0|1234509093636:0.0-0|1234509093636:1.0-0|1234509093636:2.0-"
        val falseString: String = "10|123453636:0.0-"

        val hrRegex = "0\\|\\d{12,}:(-?\\d+(\\.\\d+)?)-".toRegex()
        val pgRegex = "1\\|\\d{13,}:\\d{7,}-\$".toRegex()
        val valueRegex = "\\d{12,}:(-?\\d+(\\.\\d+)?)".toRegex()

        val hrList = hrRegex.find(trueString)
        val pgList = pgRegex.find(trueString)

        println(hrList?.value)
        do {
            val hrVal = hrList?.next();
            println(hrVal?.value)
            val hrRes = valueRegex.find(hrVal?.value.toString())
            val res = hrRes?.value.toString().split(":")
            println(res[0])
            println(res[1])

        } while (hrVal?.next() != null)

//            val time = hrRes[0].trim()
//            val data = hrRes[1].trim().toFloat()
//            println("time: $time, data: $data")
//            assertEquals(arrayOf(time, data), arrayOf(1234509093636, 0.0))


//            for (str in value) {
//                print(str)
//            }
//
//            println(hrRes?.value)
//

//        val hrRes = valueRegex.find(hrList.toString()).toString().split(":")
//        val time = hrRes[0].trim()
//        val data = hrRes[1].trim().toFloat()
//
//        assertEquals(arrayOf(time, data), arrayOf(1234509093636, 0.0))
    }
}