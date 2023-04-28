package com.example.asanmobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter

class SendingActivity : AppCompatActivity() {
    lateinit var con: ServerConnection
    lateinit var deviceID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sending)

        val getIntent = intent
        deviceID = getIntent.getStringExtra("ID").toString()
        //deviceID = "fSUwYR8iSuCByyJ2+qJk3lR6SUQ="

        con  = ServerConnection()

        val sendBtn = findViewById<Button>(R.id.sendButton)
        sendBtn.setOnClickListener{

            sendCSV()
        }
    }

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

    private fun sendCSV(){
        //CSV파일 생성(현석이형이 워치로부터 값 받아서 파일 생성하면 됨)
        val data = "This is test data!!"
        val file1 = makeCSV("ppg_lastSaveTime.csv", data)
        val file2 = makeCSV("hrm_lastSaveTime.csv", data)
        val file3 = makeCSV("acc_lastSaveTime.csv", data)

        //서버에 Post전송(테스트를 위해 하나만 전송하는 중)
        con.postFile(file1, deviceID, "100", "2023-04-28")
//        con.postFile(file2, deviceID, "100", "2023-04-09")
//        con.postFile(file3, deviceID, "100", "2023-04-09")
    }
}