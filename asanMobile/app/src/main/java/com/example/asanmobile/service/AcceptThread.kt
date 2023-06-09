package com.example.asanmobile.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.asanmobile.activity.SensorChartActivity
import com.example.asanmobile.common.*
import com.example.asanmobile.sensor.controller.SensorController
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.util.*

import java.nio.ByteBuffer
import java.nio.ByteOrder

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
            Log.e(TAG, e.printStackTrace().toString())
            EventBus.getDefault().post(ThreadStateEvent(ThreadState.STOP))
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
                Log.e(TAG, e.printStackTrace().toString())
                EventBus.getDefault().post(ThreadStateEvent(ThreadState.STOP))
                return
            }

            socket?.let {
                val inputStream = it.inputStream
                var buffer = ByteArray(990)

                Log.d(this.toString(), buffer.toString())
                EventBus.getDefault().post(SocketStateEvent(SocketState.CONNECT))

                while (true) {
                    try {
                        val receivedData = buffer.copyOf(inputStream.read(buffer))
//                        val receivedData = buffer.copyOf(inputStream.read(buffer))
//                        val buffer: ByteArray = inputStream.readNBytes(3200)
//                        val byteBuffer = ByteBuffer.wrap(receivedData)
                        val byteBuffer = ByteBuffer.wrap(receivedData)
                        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                        Log.i("size", byteBuffer.array().size.toString())
                        if (byteBuffer.array().size != 964) continue
                        val reconstructedData = StringBuilder()

                        val battery = byteBuffer.int
                        DeviceInfo.setBattery(battery.toString())
                        Log.i("battery", battery.toString())

                        while (byteBuffer.hasRemaining()) {
                            when ((byteBuffer.position() - 4) % 16) {
                                // 0 <= ppgGreen OR heartRate 구분하는 곳
                                // byteBuffer1.int <= buffer에서 int만큼 읽겠다는 뜻
                                // reconstructedData할 필요없이 여기서 바로 String으로 바꾸고 DB에 넣으면 됨
                                0 -> {
                                    reconstructedData.append(byteBuffer.int).append("|")
                                }
                                // 여긴 타임스탬프
                                4 -> reconstructedData.append(byteBuffer.long).append(":")
                                // 여긴 값
                                12 -> reconstructedData.append(byteBuffer.float).append("-")
                            }
                        }

                        val str = reconstructedData.toString()
                        Log.d(this.toString(), str.toString())
                        val intent = Intent(Intent.ACTION_ATTACH_DATA)
                        intent.putExtra("data", str)

                        CoroutineScope(Dispatchers.IO).launch {
                            // 소켓에서 데이터를 받아올 때 전송하는 것으로 작성
                            context.sendBroadcast(intent)
                            sensorController.dataAccept(str)
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
