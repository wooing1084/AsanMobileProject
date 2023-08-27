package com.gachon_HCI_Lab.user_mobile.service

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.gachon_HCI_Lab.user_mobile.common.*
import com.gachon_HCI_Lab.user_mobile.sensor.controller.SensorController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * 서비스에서 소켓 연결을 담당하는 클래스
 * */
class AcceptThread(private val bluetoothAdapter: BluetoothAdapter, context: Context) : Thread() {
    private lateinit var serverSocket: BluetoothServerSocket
    private lateinit var sensorController: SensorController

    companion object {
        private const val TAG = "ACCEPT_THREAD"
        private const val SOCKET_NAME = "server"
        private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    }

    init {
        try {
            Toast.makeText(context, "Start Service", Toast.LENGTH_LONG).show()
            // 서버 소켓
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                serverSocket =
                    bluetoothAdapter.listenUsingRfcommWithServiceRecord(SOCKET_NAME, MY_UUID)
            }

            sensorController = SensorController.getInstance(context)
        } catch (e: Exception) {
            Log.e(TAG, e.printStackTrace().toString())
            EventBus.getDefault().post(ThreadStateEvent(ThreadState.STOP))
            e.printStackTrace()
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
            } catch (e: Exception) {
                Log.e(TAG, e.printStackTrace().toString())
                // 소켓 오류 시 EventBus를 통해 Thread Stop 전송
                EventBus.getDefault().post(ThreadStateEvent(ThreadState.STOP))
                e.printStackTrace()
                break
            }

            socket?.let {
                val inputStream = it.inputStream
                val buffer: ByteArray = ByteArray(990)

                Log.d(this.toString(), buffer.toString())
                // 소켓 연결시 EventBus를 통해 Connect 전송
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

                        // if (byteBuffer.array().size != 964) continue
                        val reconstructedOneAxisData = StringBuilder()
                        val reconstructedTreeAxisData = StringBuilder()

                        val battery = byteBuffer.int
                        DeviceInfo.setBattery(battery.toString())
                        Log.i("battery", battery.toString())

                        while (byteBuffer.hasRemaining()) {
                            val dataType = byteBuffer.int
                            val timestamp = byteBuffer.long
                            if (dataType == 5 || dataType == 18 || dataType == 21) {
                                val data = byteBuffer.float
                                addOneAxisData(reconstructedOneAxisData, dataType, timestamp, data)
                            } else {
                                val xAxisData = byteBuffer.float
                                val yAxisData = byteBuffer.float
                                val zAxisData = byteBuffer.float
                                addThreeAxisData(reconstructedTreeAxisData, dataType, timestamp, xAxisData, yAxisData, zAxisData)
                            }
                        }

                        val oneAxisData = reconstructedOneAxisData.toString()
                        val threeAxisData = reconstructedTreeAxisData.toString()
                        /**
                         * 1축 데이터 - 5(light), 18(step count), 21(heart rate), 30(ppg)
                         * 데이터번호|timestamp|value -> ex) 5|timestamp|value
                         *
                         * 3축 데이터 - 1(accelerometer), 4(gyroscope), 9(gravity)
                         * 데이터번호|timestamp|x축value|y축value|z축value
                         */

//                        CoroutineScope(Dispatchers.IO).launch {
//                            sensorController.dataAccept(oneAxisData)
//                        }
//                        CoroutineScope(Dispatchers.IO).launch {
//                            sensorController.dataAccept(threeAxisData)
//                        }
                        /**
                         * 오류 발생 시
                         * 소켓 close
                         * eventBus를 통해 socketState Close 전송
                         * eventBus를 통해 ThreadState Stop 전송
                         * */
                    } catch (e: IOException) {
                        Log.e(TAG, "unable to read message form socket", e)
                        socket.close()
                        serverSocket.close()
                        EventBus.getDefault().post(SocketStateEvent(SocketState.CLOSE))
                        EventBus.getDefault().post(ThreadStateEvent(ThreadState.STOP))
                        e.printStackTrace()
                        break
                    } finally {
                        try {
                            if (!socket.isConnected) {
                                socket.use {
                                    socket.close()
                                    serverSocket.close()
                                    EventBus.getDefault().post(SocketStateEvent(SocketState.CLOSE))
                                    EventBus.getDefault().post(ThreadStateEvent(ThreadState.STOP))
                                }
                            }
                        } catch (e: Exception) {
                            socket.close()
                            serverSocket.close()
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }



    private fun addOneAxisData(
        reconstructedOneAxisData: StringBuilder,
        dataType: Int,
        timestamp: Long,
        data: Float
    ) {
        reconstructedOneAxisData.append(dataType).append("|")
        reconstructedOneAxisData.append(timestamp).append("|")
        reconstructedOneAxisData.append(data).append("-")
    }

    private fun addThreeAxisData(
        reconstructedTreeAxisData: StringBuilder,
        dataType: Int,
        timestamp: Long,
        xAxisData: Float,
        yAxisData: Float,
        zAxisData: Float
    ){
        reconstructedTreeAxisData.append(dataType).append("|")
        reconstructedTreeAxisData.append(timestamp).append("|")
        reconstructedTreeAxisData.append(xAxisData).append("|")
        reconstructedTreeAxisData.append(yAxisData).append("|")
        reconstructedTreeAxisData.append(zAxisData).append("-")
    }

}
