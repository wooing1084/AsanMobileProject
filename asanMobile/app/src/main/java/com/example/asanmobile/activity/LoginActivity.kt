package com.example.asanmobile.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.asanmobile.R
import com.example.asanmobile.ServerConnection
import com.example.asanmobile.common.CacheManager
import com.example.asanmobile.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cache = CacheManager.loadCacheFile(this, "login.txt")
        if (cache != null) {
            ServerConnection.login(cache, context = this)
        }
        else {
//            val login = findViewById<Button>(R.id.login_btn)
//            login.setOnClickListener {
//                val id = findViewById<EditText>(R.id.id).text.toString()
//                ServerConnection.login(id, context = this)
//            }

            binding.loginBtn.setOnClickListener {
                ServerConnection.login(binding.id.text.toString(), context = this)
            }
        }
    }
}