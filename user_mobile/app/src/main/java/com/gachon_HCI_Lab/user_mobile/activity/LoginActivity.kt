package com.gachon_HCI_Lab.user_mobile.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.gachon_HCI_Lab.user_mobile.common.BTManager
import com.gachon_HCI_Lab.user_mobile.common.ServerConnection
import com.gachon_HCI_Lab.user_mobile.common.CacheManager
import com.gachon_HCI_Lab.user_mobile.sensor.AppDatabase
import com.gachon_HCI_Lab.user_mobile.databinding.ActivityLoginBinding

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

        val deviceID = BTManager.getUUID(this, BTManager.getConnectedDevice(this, BTManager.connectedDevices(this)))

        Log.d("LoginActivity", "deviceID: $deviceID")

        val cache = CacheManager.loadCacheFile(this, "login.txt")
        if (cache != null) {
            ServerConnection.login(cache,deviceID = deviceID, context = this)
        }
        else {
            binding.loginBtn.setOnClickListener {
                ServerConnection.login(binding.id.text.toString(),deviceID = deviceID, context = this)
            }
        }
    }
}