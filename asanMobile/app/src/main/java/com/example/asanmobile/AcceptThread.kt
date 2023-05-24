package com.example.asanmobile

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.asanmobile.sensor.controller.SensorController
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.util.*

import com.example.asanmobile.SocketState


@SuppressLint("MissingPermission")
class AcceptThread(private val bluetoothAdapter: BluetoothAdapter, context: Context) : Thread() {
    private lateinit var serverSocket: BluetoothServerSocket
    private lateinit var sensorController: SensorController
//    private val serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(SOCKET_NAME, MY_UUID)
//    private val handler = Handler(Looper.getMainLooper())
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
            sensorController = SensorController.getInstance(context)
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
                val buffer = ByteArray(125000)
                var bytes: Int
                EventBus.getDefault().post(SocketStateEvent(SocketState.CONNECT))

                while (true) {
                    try {
                        bytes = mInputputStream.read(buffer)
                        val msg = String(buffer, 0, bytes, Charsets.UTF_8)
                        Log.d(this.toString(), msg)

                        CoroutineScope(Dispatchers.IO).launch {
                            sensorController.dataAccept(msg)
                        }
                        // 오류 발생시 소켓 close
                    } catch (e: IOException) {
                        Log.e(TAG, "unable to read message form socket", e)
                        socket.close()
                        EventBus.getDefault().post(SocketStateEvent(SocketState.CLOSE))
                        break
                    } finally {
                        try {
                            if (!socket.isConnected) {
                                socket.use {
                                    socket.close()
                                    EventBus.getDefault().post(SocketStateEvent(SocketState.CLOSE))
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

}