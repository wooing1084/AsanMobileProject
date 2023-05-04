package com.example.asanmobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val login = findViewById<Button>(R.id.login_btn)
        login.setOnClickListener{
            // 로그인 처리
            //Toast.makeText(this, ServerConnection.login("HCkDYUYGQW2jDUPMLV1mDFR6SUQ="), Toast.LENGTH_LONG).show()

            val id = findViewById<EditText>(R.id.id)
            val intent = Intent(this, SendingActivity::class.java)
            intent.putExtra("ID", id.text.toString())

            startActivity(intent)

            finish()
        }

    }
}