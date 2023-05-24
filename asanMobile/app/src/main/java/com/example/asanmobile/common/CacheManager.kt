package com.example.asanmobile.common

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class CacheManager {
    companion object {
        val CACHE_DIRECTORY = "cache/"

        // 캐시 파일을 저장하는 함수
        fun saveCacheFile(context: Context, data: String, fileName: String) {
            val cacheDir = File(context.cacheDir, CACHE_DIRECTORY)
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val cacheFile = File(cacheDir, fileName)
            if (!cacheFile.exists()) {
                cacheFile.createNewFile()
            }

            val fos = FileOutputStream(cacheFile)
            fos.write(data.toByteArray())
            fos.close()
        }

        // 캐시 파일을 로드하는 함수
        fun loadCacheFile(context: Context, fileName: String): String? {
            val cacheDir = File(context.cacheDir, CACHE_DIRECTORY)
            if (!cacheDir.exists()) {
                return null
            }

            val cacheFile = File(cacheDir, fileName)
            if (!cacheFile.exists()) {
                return null
            }

            val fis = FileInputStream(cacheFile)
            val data = fis.readBytes().toString(Charsets.UTF_8)
            fis.close()

            return data
        }


    }
}