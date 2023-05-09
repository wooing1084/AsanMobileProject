package com.example.asanmobile

import android.content.Intent
import android.hardware.Sensor
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

            val id = findViewById<EditText>(R.id.id).text.toString()
            val result = ServerConnection.login(id)
            // 로그인 처리
            if(result){

                val intent = Intent(this, SensorActivity::class.java)
                intent.putExtra("ID", id)
                startActivity(intent)

                finish()
            }
            else{
                Toast.makeText(this,"Login fail", Toast.LENGTH_LONG).show()
            }

//
//            startActivity(intent)

            // 로그인 성공 시, SensorAcitivity로 접속
//            val id = findViewById<EditText>(R.id.id)
//            val intent = Intent(this, SensorActivity::class.java)
//            intent.putExtra("ID", id.text.toString())


        }

    }
}