package com.example.asanmobile

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class CSVController {
    private var context: Context
    private val name = "data.csv"
    lateinit var path: String

    constructor(context: Context) {
        this.context = context
        path = context.filesDir.toString()
    }

    fun fileExist(): Boolean {
        val file = File(path, name)
        return file.exists()
    }

    fun csvFirst() {
        val file = File(path, name)
        if (!file.exists()) {
            val headerData = System.currentTimeMillis()
            val bw = BufferedWriter(FileWriter(file))
            try {
                bw.write("$headerData\n")
                bw.flush()
                bw.close()
            } catch(e: Exception) {
                Log.d(TAG, e.toString())
            }
        }
    }

    fun csvSave(str: String) {
        val file = File(path, name)
        val bw = BufferedWriter(FileWriter(file, true))
        try {
            bw.write("" + str + "\n")
            bw.flush()
            bw.close()
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }
}