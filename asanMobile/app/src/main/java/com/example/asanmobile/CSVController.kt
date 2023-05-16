package com.example.asanmobile

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Environment
import android.util.Log
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime


object CSVController {
    // csv Writer, Reader 이름을 주입시켜야 하는 과정 필요
    fun getCSVReader(path: String, fileName: String): CSVReader{
        return CSVReader(FileReader("$path/$fileName"))
    }

    fun getCSVWriter(path: String, fileName: String): CSVWriter{
        return CSVWriter(FileWriter("$path/$fileName"))
    }

    // 파일이 저장될 외부 저장소 path 불러오는 함수
    // 파일이 저장될 path 리턴
    private fun getExternalPath(): String{
        var dir: File? = Environment.getExternalStorageDirectory()
        var abPath: String? = dir?.absolutePath
//        val packageName: String = context.packageName
//        var path = abPath + dir!!.absolutePath + packageName
        val path = "$abPath/sensor"

        // 외부 저장소 경로가 있는지 확인, 없으면 생성
        val file: File = File(path)
        if (!file.exists()) {
            file.mkdir()
        }

        return path
    }

    // 파일 존재 확인 함수
    fun fileExist(name: String): Boolean {
        val path: String = getExternalPath()
        val directory: File = File(path)
        val files: Array<out File>? = directory.listFiles()

        for (file in files!!) {
            if (file.name.equals(name)) {
                return true
            }
        }
        return false
    }

    // 현재 날짜 시간을 리턴하는 메소드
    // 형태: "yyyy-MM-dd_HH:mm:ss"
    private fun getTime(): String {
        val dateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now()
        } else {
            // "VERSION.SDK_INT < O"
            val timeStamp = System.currentTimeMillis()
        }
        return SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(dateTime).toString()
    }

    private fun getFileName(sensorName: String): String {
        return getTime() + "_" + sensorName
    }

    fun csvFirst(sensorName: String) {
        val path: String = getExternalPath()
        val name = getFileName(sensorName)
        val file = File(path, name)

        val headerData = "time,data"
        val bw = BufferedWriter(FileWriter(file))
        try {
            bw.write("$headerData")
            bw.flush()
            bw.close()
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

//    fun csvSave(sensorName: String, 데이터배열) {
//        val path: String = getExternalPath()
//        val name = getFileName(sensorName)
//
//        val csvWriter = getCSVWriter(path, name)
//        try {
//            for (data in dataList) {
//                csvWriter.use {
//                    it.writeNext(data)
//                }
//            }
//        } catch (e: Exception) {
//            Log.d(TAG, e.toString())
//        }
//    }

    fun fileRename(path:String, origin: String, change: String) {
        try {
            val file = File(path, origin)
            if (!file.exists()) {
//                throw NoSuchFileException("Source file doesn't exist")
            }

            val dest = File(path, change)
            if (dest.exists()) {
//                throw FileAlreadyExistsException("Destination file already exist")
            }
            val success = file.renameTo(dest)
            if (success) {
                println("Renaming succeeded")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}