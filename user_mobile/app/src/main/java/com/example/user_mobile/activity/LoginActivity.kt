package com.example.user_mobile.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.user_mobile.common.ServerConnection
import com.example.user_mobile.common.CacheManager
import com.example.user_mobile.databinding.ActivityLoginBinding
import com.example.user_mobile.sensor.AppDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //==== 초기화가 필요한 인스턴스
        db = AppDatabase.getInstance(applicationContext)!!
        //====


        val cache = CacheManager.loadCacheFile(this, "login.txt")
        if (cache != null) {
            ServerConnection.login(cache, context = this)
        }
        else {
            binding.loginBtn.setOnClickListener {
                ServerConnection.login(binding.id.text.toString(), context = this)
            }
        }
    }
}