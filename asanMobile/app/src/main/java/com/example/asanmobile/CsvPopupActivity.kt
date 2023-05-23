package com.example.asanmobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class CsvPopupActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TextAdapter
    private lateinit var itemList: MutableList<String>

    private lateinit var closeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_csv_popup)

        closeButton = findViewById<Button>(R.id.btnClose)
        closeButton.setOnClickListener {
            finish() // 액티비티 종료
        }

        // 리사이클러 뷰 관련 코드
        recyclerView = findViewById(R.id.csv_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        itemList = mutableListOf()

        adapter = TextAdapter(itemList)
        recyclerView.adapter = adapter

        // CSV 파일 개수 가져오기
        val csvFolderPath = CsvController.getExternalPath(applicationContext)// CSV 파일들이 있는 폴더 경로 설정
        val csvFolder = File(csvFolderPath)
        if (csvFolder.exists() && csvFolder.isDirectory) {
            val csvFiles = csvFolder.listFiles { file ->
                file.extension.equals("csv", ignoreCase = true) // 확장자가 .csv인 파일들만 가져옴
            }
            csvFiles?.let {
                itemList.clear()
                itemList.addAll(csvFiles.map { file ->
                    val fileSize = file.length() // 파일 크기
                    val fileName = "${file.name}   (${fileSize} bytes)" // 파일명과 크기 조합하여 표시
                    fileName
                })
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onBackPressed() {
        finish() // 뒤로가기 버튼을 누를 때 액티비티 종료
    }
}