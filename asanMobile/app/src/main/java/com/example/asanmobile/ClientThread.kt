package com.example.asanmobile

import com.opencsv.CSVWriter
import okhttp3.*
import java.io.*
import java.net.HttpURLConnection
import kotlin.concurrent.thread


class ClientThread{
    val urlText = "http://10.0.2.2:8000/csv"
    lateinit var netConn: HttpURLConnection


    public fun sendCSV(){
        val data = arrayOf(arrayOf("Name", "Age", "Email"), arrayOf("John", "30", "john@example.com"), arrayOf("Jane", "25", "jane@example.com"))

        // 파일 생성 및 쓰기
        val dir = "data/data/com.example.asanmobile/"
        var file = File(dir,"data.csv")
        FileWriter(file)
        val writer = CSVWriter(FileWriter(file))

        writer.writeAll(data.toMutableList())

        writer.close()

        uploadFile(file, urlText)
    }

    // 서버에 Post로 파일 전송하는 부분
    fun uploadFile(file: File, url: String) {
        //Post에 붙일 요청 body생성부분
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("application/octet-stream"), file))
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

}