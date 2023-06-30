package com.example.asanmobile.common

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.example.asanmobile.sensor.model.Sensor
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.*


object CsvController {

    // csv Writer, Reader 이름을 주입시켜야 하는 과정 필요
    fun getCSVReader(path: String, fileName: String): CSVReader{
        return CSVReader(FileReader("$path/$fileName"))
    }

    private fun getCsvWriter(path: String, fileName: String): CSVWriter{
        return CSVWriter(FileWriter("$path/$fileName"))
    }

    // 파일이 저장될 외부 저장소 path 불러오는 함수
    // 파일이 저장될 path 리턴
    public fun getExternalPath(context: Context): String{
        val dir: File? = context.getExternalFilesDir(null)
        val path = dir?.absolutePath + File.separator + "sensor"

        // 외부 저장소 경로가 있는지 확인, 없으면 생성
        val file: File = File(path)
        if (!file.exists()) {
            file.mkdir()
        }
        return path
    }

    fun getExternalPath(context: Context, dirName: String): String {
        val dir: File? = context.getExternalFilesDir(null)
        val path = dir?.absolutePath + File.separator + dirName

        // 외부 저장소 경로가 있는지 확인, 없으면 생성
        val file: File = File(path)
        if (!file.exists()) {
            file.mkdir()
        }
        return path
    }

    // 파일 존재 확인 함수
    // 센서명을 인풋으로 넣는다
    // 존재하면 파일명, 없으면 null 리턴
    fun fileExist(context: Context, name: String): String? {
        val path: String = getExternalPath(context)
        val directory: File = File(path)

        if (directory.exists()) {
            val files: Array<out File>? = directory.listFiles()

            for (file in files!!) {
                if (file.name.contains(name)) {
                    return file.name
                }
            }
        }
        return null
    }

    // 현재 날짜 시간을 리턴하는 메소드
    // 형태: "yyyy-MM-dd_HH:mm:ss"
    private fun getTime(): String {
        return (System.currentTimeMillis() / 1000L).toString()
    }

    private fun setFileName(sensorName: String): String {
        return sensorName + "_" + getTime() + ".csv"
    }

    //디바이스의 센서_unixtime.csv파일명 가져오기
    //unixtime은 알 수 없기 때문에 파일명을 알아내기 위해 사용
    fun getExistFileName(context: Context, name: String): String? {
        val path: String = getExternalPath(context, "sensor")
        val directory: File = File(path)

        if (directory.exists()) {
            val files: Array<out File>? = directory.listFiles()

            for (file in files!!) {
                if (file.name.contains(name)) {
                    return file.name
                }
            }
        }
        return null
    }

    fun csvFirst(context: Context, sensorName: String) {
        val path: String = getExternalPath(context)
        val name = setFileName(sensorName)
        val file: File = File(path, name)
//        val csvWriter = getCsvWriter(path, name)
        
//        val headerData = "time,value"
        val bw = BufferedWriter(FileWriter(file))
        try {
            bw.write("")
            bw.flush()
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        } finally {
            bw.close()
        }
        Log.d(this.toString(), "csv 생성")
    }

    fun csvSave(context: Context, sensorName: String, sensorSet: List<Sensor>) {
        val path: String = getExternalPath(context)
        val name = fileExist(context, sensorName)

        Log.d(this.toString(), "csv 작성")
        val headerData = arrayOf("time","value")
        val csvWriter = getCsvWriter(path, name!!)
        try {
            csvWriter.writeNext(headerData)
            for (sensor in sensorSet) {
                val data: Array<String> = convertSensorToStringArray(sensor)
                csvWriter.writeNext(data)
            }
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        } finally {
            csvWriter.close()
        }
    }

    private fun convertSensorToStringArray(sensor: Sensor): Array<String> {
        val time = sensor.time.toString()
        val value = sensor.value.toString()
        return arrayOf(time, value)
    }

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


     fun getFile(fileName: String): File? {
        val file = File(fileName)
        if (!file.exists()) {
            Log.d("csv controller", fileName + " File does not found")
            return null
        }

        return file
    }

    //파일 디렉토리 옮기기
    //soure -> dest로
    fun moveFile(sourcePath: String, destinationPath: String) {
        val sourceFile = File(sourcePath)
        val destinationFile = File(destinationPath)

        try {
            // 파일을 이동합니다.
            sourceFile.renameTo(destinationFile)
            Log.d("csv controller", "파일 이동 성공")
        } catch (e: IOException) {
            println("파일 이동 실패: ${e.message}")
        }
    }
}