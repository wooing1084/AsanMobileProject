package com.example.asanmobile.common

import android.content.Context
import com.example.asanmobile.common.CsvController.getExternalPath
import java.io.File

object CsvStastics {

    fun GetMean(context: Context, fileName : String) {
        val path: String = getExternalPath(context)
        var file = File(fileName)

    }
}