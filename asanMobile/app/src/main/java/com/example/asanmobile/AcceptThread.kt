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
import java.io.IOException
import java.util.*

@SuppressLint("MissingPermission")
class AcceptThread(private val bluetoothAdapter: BluetoothAdapter, context: Context) : Thread() {
    private lateinit var serverSocket: BluetoothServerSocket
    private val csvController: CSVController = CSVController(context)
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

                        Thread(Runnable {
                            // UI를 업데이트하는 작업 수행
                            if (csvController.fileExist()) {
                                val intent = Intent("my-event")
                                intent.putExtra("message", msg)
//                                handler.post {
//                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
//                                }
                            } else {
                                csvController.csvFirst()
                                val intent = Intent("my-event")
                                intent.putExtra("message", msg)
//                                handler.post {
//                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
//                                }
                            }
                            // csv 작성
                            Log.d("acceptThread", msg);
                            csvController.csvSave(msg)
                        }).start()
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
}