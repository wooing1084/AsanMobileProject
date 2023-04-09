package com.example.asanmobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //====초기화가 필요한 인스턴스

        //====

        // 로그인페이지로 전환
        intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

        //액티비티 종료
        finish()
    }

}