package com.gachon_HCI_Lab.user_mobile.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.gachon_HCI_Lab.user_mobile.common.BTManager
import com.gachon_HCI_Lab.user_mobile.common.ServerConnection
import com.gachon_HCI_Lab.user_mobile.common.CacheManager
import com.gachon_HCI_Lab.user_mobile.common.CsvController
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

        // Sended폴더에 일정 시간 지난 파일들 삭제 (하루)
        CsvController.getExternalPath(this, "Sensor").let {
            CsvController.deleteOldfiles(it, 60 * 60 * 24 * 1000)
        }
        CsvController.getExternalPath(this, "Sensor/Sended").let {
            CsvController.deleteOldfiles(it, 60 * 60 * 24 * 1000)
        }


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