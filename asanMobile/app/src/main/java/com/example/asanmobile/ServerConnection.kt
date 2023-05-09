package com.example.asanmobile

import android.util.Log
import okhttp3.*
import java.io.*


abstract class ServerConnection{
    companion object{
        val tag = "Server Connection"
        //val urlText = "http://172.16.226.109:8000/csv"
        //val urlText = "http://10.0.2.2:8000/csv"
        val requestUrl = "http://220.149.46.249:7778/tmp_get/"
        val loginURL = "http://220.149.46.249:7778/registUser/"
        // 서버에 Post로 파일 전송하는 부분
        fun postFile(file: File, deviceID: String, battery: String, timestamp: String, url: String = requestUrl) {
            //Post에 붙일 요청 body생성부분
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("csvfile", file.name, RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .addFormDataPart("deviceID", deviceID)
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
                    println(response.body()?.string())
                }
            })
        }

        fun login(authcode : String, deviceID : String = "device_id_test", regID: String = "reg_id_test"): Boolean {
            val client = OkHttpClient()

            val httpBuilder = HttpUrl.parse(loginURL)?.newBuilder()
            if (httpBuilder != null) {
                httpBuilder.addQueryParameter("userID", authcode)
                httpBuilder.addQueryParameter("deviceID", deviceID)
                httpBuilder.addQueryParameter("regID", regID)
            }

            val request = Request.Builder()
                .url(httpBuilder!!.build())
                .build()
            var responseBody = true

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(tag,e.printStackTrace().toString())
                    responseBody = false
                }

                override fun onResponse(call: Call, response: Response) {
                   Log.e(tag, response.body().toString())
                    responseBody = true

                }
            })
            return responseBody
        }

    }


}