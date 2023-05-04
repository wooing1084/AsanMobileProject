package com.example.asanmobile

import android.util.Log
import android.widget.Toast
import okhttp3.*
import java.io.*


abstract class ServerConnection{
    companion object{
        //val urlText = "http://172.16.226.109:8000/csv"
        //val urlText = "http://10.0.2.2:8000/csv"
        val requestUrl = "http://220.149.46.249:7778/tmp_get/"
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
                    Log.e("Client",e.printStackTrace().toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    println(response.body()?.string())
                }
            })
        }

        fun login(watchid : String): String {
            val client = OkHttpClient()

            val httpBuilder = HttpUrl.parse(requestUrl)?.newBuilder()
            if (httpBuilder != null) {
                httpBuilder.addQueryParameter("userID", watchid)
            }

            val request = Request.Builder()
                .url(httpBuilder?.build())
                .build()
            val responseBody:String
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                responseBody = response.body().toString()
                // response 값을 가지고 작업을 수행합니다
            }
            return responseBody
        }

    }


}