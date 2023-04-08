package com.example.asanmobile

import com.opencsv.CSVWriter
import okhttp3.*
import java.io.*
import java.net.HttpURLConnection
import kotlin.concurrent.thread


class ClientThread{
    //val urlText = "http://172.16.226.109:8000/csv"
    val urlText = "http://10.0.2.2:8000/csv"
    lateinit var netConn: HttpURLConnection



    public fun makeCSV(name: String, data: String): File {
        // 파일 생성 및 쓰기
        val dir = "data/data/com.example.asanmobile/"
        val file = File(dir,name)
        FileWriter(file)
        val writer = CSVWriter(FileWriter(file))

        writer.writeNext(arrayOf(data))

        writer.close()
        return file
    }

    public fun sendCSV(){
        val data = "This is test data!!"
        val file1 = makeCSV("ppg_lastSaveTime.csv", data)
        val file2 = makeCSV("hrm_lastSaveTime.csv", data)
        val file3 = makeCSV("acc_lastSaveTime.csv", data)


        uploadFile(file1, urlText)
        uploadFile(file2, urlText)
        uploadFile(file3, urlText)

    }

    // 서버에 Post로 파일 전송하는 부분
    fun uploadFile(file: File, url: String) {
        //Post에 붙일 요청 body생성부분
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("application/octet-stream"), file))
            .addFormDataPart("deviceID", "1234")
            .addFormDataPart("battery", "100")
            .addFormDataPart("timestamp", "2023-03-27")
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