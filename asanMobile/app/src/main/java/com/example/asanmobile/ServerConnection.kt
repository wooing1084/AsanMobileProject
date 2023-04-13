package com.example.asanmobile

import com.opencsv.CSVWriter
import okhttp3.*
import java.io.*
import java.net.HttpURLConnection
import java.sql.Timestamp


class ServerConnection{
    //val urlText = "http://172.16.226.109:8000/csv"
    //val urlText = "http://10.0.2.2:8000/csv"
    val urlText = "http://220.149.46.249:7778/tmp_get/"
    // 서버에 Post로 파일 전송하는 부분
    fun postFile(file: File, deviceID: String, battery: String, timestamp: String, url: String = urlText) {
        //Post에 붙일 요청 body생성부분
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("application/octet-stream"), file))
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
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                println(response.body()?.string())
            }
        })
    }

    fun get(){

    }

}