package com.example.asanmobile.common

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.asanmobile.activity.SensorActivity
import okhttp3.*
import java.io.*
import java.util.Date
import java.util.Locale


abstract class ServerConnection{
    companion object{
        val tag = "Server Connection"
        //val urlText = "http://172.16.226.109:8000/csv"
        //val urlText = "http://10.0.2.2:8000/csv"
        val requestUrl = "http://220.149.46.249:7778/forUser/postCurrentData/"
        val loginURL = "http://220.149.46.249:7778/forUser/registUser/"
        // 서버에 Post로 파일 전송하는 부분
        fun postFile(file: File, userID: String, battery: String, timestamp: String, url: String = requestUrl) {
            //Post에 붙일 요청 body생성부분
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("csvfile", file.name, RequestBody.create(MediaType.parse("text/csv"), file))
                .addFormDataPart("userID", userID)
                .addFormDataPart("battery", battery)
                .addFormDataPart("timestamp", timestamp)
                .build()

            //Post요청 생성
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            //전송
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(tag,e.printStackTrace().toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.e(tag, "File Send response code: " + response.body()?.string().toString())
                }
            })
        }

        fun login(authcode : String, deviceID : String = "123456", regID: String = "1234567", context: Activity) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = Date()
            val strDate: String = dateFormat.format(date)


            val client = OkHttpClient()

            val httpBuilder = HttpUrl.parse(loginURL)?.newBuilder()
            if (httpBuilder != null) {
                httpBuilder.addQueryParameter("userID", authcode)
                httpBuilder.addQueryParameter("deviceID", deviceID)
                httpBuilder.addQueryParameter("regID", regID)
                httpBuilder.addQueryParameter("timestamp",strDate)
            }

            val request = Request.Builder()
                .url(httpBuilder!!.build())
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d(tag,e.printStackTrace().toString())
                    val handler = android.os.Handler(Looper.getMainLooper())
                    handler.postDelayed(Runnable {
                        Toast.makeText(
                            context,
                            "Login failed!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }, 0)
                }

                override fun onResponse(call: Call, response: Response) {
                   Log.d(tag, "Login response code: "+response.code().toString())
                    if(response.code().toString() == "200"){
                        CacheManager.saveCacheFile(context, authcode, "login.txt")

                        val intent = Intent(context, SensorActivity::class.java)
                        intent.putExtra("ID", authcode)
                        intent.putExtra("DeviceID", deviceID)
                        context.startActivity(intent)
                        context.finish()
                    }
                    else{
                        val handler = android.os.Handler(Looper.getMainLooper())
                        handler.postDelayed(Runnable {
                            Toast.makeText(
                                context,
                                "Login failed!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }, 0)
                    }

                }
            })
        }

    }


}