package com.example.asanmobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val login = findViewById<Button>(R.id.login_btn)
        login.setOnClickListener{

            val intent = Intent(this, SendingActivity::class.java)
            intent.putExtra("ID", "1234")

            startActivity(intent)

            finish()
        }

    }
}