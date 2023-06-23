package com.example.asanmobile.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.asanmobile.sensor.AppDatabase
import com.example.asanmobile.R

class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //==== 초기화가 필요한 인스턴스
        db = AppDatabase.getInstance(applicationContext)!!
        //====

//        // 로그인페이지로 전환
        intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

        // 테스트용 바로 센서액티비티
//        val intent = Intent(this, SensorActivity::class.java)
//        startActivity(intent)

    }

    // 뒤로가기 버튼을 누를 때 액티비티 종료
    override fun onBackPressed() {
        finish()
    }
}