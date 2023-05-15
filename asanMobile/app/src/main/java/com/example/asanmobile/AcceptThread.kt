package com.example.asanmobile

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

@SuppressLint("MissingPermission")
class AcceptThread(private val bluetoothAdapter: BluetoothAdapter, context: Context) : Thread() {
    private lateinit var serverSocket: BluetoothServerSocket
    private val handler = Handler(Looper.getMainLooper())
    private val context: Context = context

    companion object {
        private const val TAG = "ACCEPT_THREAD"
        private const val SOCKET_NAME = "server"
        private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    }

    init {
        try {
            Toast.makeText(context, "Start Service", Toast.LENGTH_LONG).show()
            // 서버 소켓
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(SOCKET_NAME, MY_UUID)
        } catch (e: Exception) {
            Log.d(TAG, e.message.toString())
        }
    }

    override fun run() {
        var socket: BluetoothSocket? = null

        while (true) {
            try {
                // 클라이언트 소켓
                socket = serverSocket?.accept()
                Log.d("success", socket.toString())
                sleep(300)
            } catch (e: IOException) {
                Log.d(TAG, e.message.toString())
            }

            socket?.let {
                val mInputputStream = socket.inputStream
                val buffer = ByteArray(1024)
                var bytes: Int

                while (true) {
                    try {
                        bytes = mInputputStream.read(buffer)
                        val msg = String(buffer, 0, bytes, Charsets.UTF_8)

                        // 대대적인 수정 필요
                        // Synchronized 필요 -> 데이터가 끊겨서 들어올 수 있기 때문에
//                        Thread(Runnable {
//                            // UI를 업데이트하는 작업 수행
//                            if (csvController.fileExist()) {
//                                val intent = Intent("my-event")
//                                intent.putExtra("message", msg)
//                                handler.post {
//                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
//                                }
//                            } else {
//                                csvController.csvFirst()
//                                val intent = Intent("my-event")
//                                intent.putExtra("message", msg)
//                                handler.post {
//                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
//                                }
//                            }
//                            // csv 작성
//                            csvController.csvSave(msg)
//                        }).start()
                        // 오류 발생시 소켓 close
                    } catch (e: IOException) {
                        Log.e(TAG, "unable to read message form socket", e)
                        socket?.close()
                        break
                    }
                }
            }
        }
    }

    object AcceptController {

        private lateinit var splitData: String

        suspend fun dataAccept(data: String) = coroutineScope {
            val bufferSize = 1024 // 버퍼 사이즈 설정
            val bufferTime = 500L // 버퍼링 주기 설정 (0.5초)
            val buffer = mutableListOf<String>() // 버퍼 선언

            // 데이터를 전달하는 채널 선언
            val channel = Channel<String>(bufferSize)

            // 데이터를 받아서 버퍼에 저장하는 코루틴
            launch {
                channel.consumeEach { data ->
                    buffer.add(data) // 데이터를 버퍼에 추가
                    println("Added: $data")

                    if (buffer.size >= bufferSize) { // 버퍼가 가득 찼을 경우
                        flushBuffer(buffer) // 버퍼 내용을 처리
                        buffer.clear()
                    }
                }
            }

            // 일정 주기마다 버퍼 내용을 처리하는 코루틴
            launch {
                while (true) {
                    delay(bufferTime) // 일정 시간 대기

                    if (buffer.isNotEmpty()) { // 버퍼에 내용이 있을 경우
                        flushBuffer(buffer) // 버퍼 내용을 처리
                        buffer.clear()
                    }
                }
            }
        }

        // 버퍼 내용을 처리하는 함수
        suspend fun flushBuffer(buffer: MutableList<String>) {
            // 심장박동수 정규표현식
            val heartRegex = "\\d{10,}:\\d{1,4}[.]\\d|\\d{10,}:\\d{1,4}-".toRegex()

            // ppgGreen 정규표현식
            // 처음오는 숫자가 10이상이 오고, '['로 시작하고 안에는 어떤 문장이 와도 괜찮고, ']'로 끝나야 한다
            val ppgGreen = "(\\d{10,}): \\[[^\\]]*\\]".toRegex()
            val bufferData = buffer.toList() // 버퍼 데이터를 리스트로 복사

            // bufferData를 SensorRepository에 작성해야함
            bufferData
            buffer.clear() // 버퍼 내용을 비움

            // 복사한 버퍼 데이터를 처리
            println("Processing: $bufferData")
        }
        
        suspend fun writeSensorRepo() {
            
        }
    }
}

