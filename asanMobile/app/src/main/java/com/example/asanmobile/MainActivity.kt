package com.example.asanmobile

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class MainActivity : AppCompatActivity() {
    lateinit var cThread: ClientThread
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cThread  = ClientThread()

        val sendBtn = findViewById<Button>(R.id.sendButton)
        sendBtn.setOnClickListener{

            cThread.sendCSV()
        }

    }


    override fun onDestroy() {
        super.onDestroy()
    }




    private fun readServerMessage(){

    }

}