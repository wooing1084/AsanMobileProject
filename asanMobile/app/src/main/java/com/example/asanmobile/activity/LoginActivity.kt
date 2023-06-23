package com.example.asanmobile.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.asanmobile.R
import com.example.asanmobile.ServerConnection
import com.example.asanmobile.common.CacheManager

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val cache = CacheManager.loadCacheFile(this, "loggin.txt")
        if (cache != null) {
            ServerConnection.login(cache, context = this)
        }
        else{
            val login = findViewById<Button>(R.id.login_btn)
            login.setOnClickListener {
                val id = findViewById<EditText>(R.id.id).text.toString()
                ServerConnection.login(id, context = this)
            }
        }




    }
}