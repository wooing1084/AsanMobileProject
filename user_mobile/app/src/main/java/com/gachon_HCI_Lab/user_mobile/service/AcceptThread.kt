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
    private var reconstructedOneAxisData = StringBuilder()
    private var reconstructedTreeAxisData = StringBuilder()

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

    /**
     * 오류 발생 시
     * 소켓 close
     * eventBus를 통해 socketState Close 전송
     * eventBus를 통해 ThreadState Stop 전송
     * */
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
                val buffer: ByteArray = ByteArray(964)

                Log.d(this.toString(), buffer.toString())
                // 소켓 연결시 EventBus를 통해 Connect 전송
                EventBus.getDefault().post(SocketStateEvent(SocketState.CONNECT))

                while (true) {
                    try {
                        val receivedData = buffer.copyOf(inputStream.read(buffer))
                        val byteBuffer = getByteBufferFrom(receivedData)
                        updateStringBuffer()
                        saveBatteryDataFrom(byteBuffer)
                        saveSensorDataToString(byteBuffer)
                        saveOneAxisDataToCsv()
                        saveThreeDataToCsv()
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

    private fun validateOneAxisDataType(dataType: Int): Boolean {
        if (dataType == 5 || dataType == 18 || dataType == 21 || dataType == 30)
            return true
        return false
    }

    private fun validateBufferSize(byteBuffer: ByteBuffer): Boolean {
        if ((byteBuffer.limit() - byteBuffer.position()) < 16)
            return false
        return true
    }

    private fun validateBufferSizeForOneAxisSensor(byteBuffer: ByteBuffer): Boolean {
        if ((byteBuffer.limit() - byteBuffer.position()) < 4)
            return false
        return true
    }

    private fun validateBufferSizeForThreeAxisSensor(byteBuffer: ByteBuffer): Boolean {
        if ((byteBuffer.limit() - byteBuffer.position()) < 12)
            return false
        return true
    }

    private fun getByteBufferFrom(receivedData: ByteArray): ByteBuffer {
        val byteBuffer = ByteBuffer.wrap(receivedData)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        return byteBuffer
    }

    private fun saveSensorDataToString(byteBuffer: ByteBuffer){
        while (byteBuffer.position() < byteBuffer.limit()) {
            if (!validateBufferSize(byteBuffer))
                break
            saveEachSensorDataToString(byteBuffer)
        }
    }

    private fun saveEachSensorDataToString(byteBuffer: ByteBuffer) {
        val dataType = byteBuffer.int
        val timestamp = byteBuffer.long
        addOneAxisData(byteBuffer, dataType, timestamp)
        addThreeAxisData(byteBuffer, dataType, timestamp)
    }

    private fun saveBatteryDataFrom(byteBuffer: ByteBuffer) {
        val battery = byteBuffer.int
        DeviceInfo.setBattery(battery.toString())
        Log.i("battery", battery.toString())
    }

    /**
     * 1축 데이터 - 5(light), 18(step count), 21(heart rate), 30(ppg)
     * 데이터번호|timestamp|value -> ex) 5|timestamp|value
     */
    private fun saveOneAxisDataToCsv() {
        val oneAxisData = reconstructedOneAxisData.toString()
        Log.d("OneAxisData", oneAxisData)
//        CoroutineScope(Dispatchers.IO).launch {
//            sensorController.dataAccept(oneAxisData)
//        }
    }

    /**
     * 3축 데이터 - 1(accelerometer), 4(gyroscope), 9(gravity)
     * 데이터번호|timestamp|x축value|y축value|z축value
     */
    private fun saveThreeDataToCsv() {
        val threeAxisData = reconstructedTreeAxisData.toString()
        Log.d("ThreeAxisData", threeAxisData)
//        CoroutineScope(Dispatchers.IO).launch {
//            sensorController.dataAccept(threeAxisData)
//        }
    }

    private fun updateStringBuffer() {
        reconstructedOneAxisData = StringBuilder()
        reconstructedTreeAxisData = StringBuilder()
    }

    private fun addOneAxisData(byteBuffer: ByteBuffer, dataType: Int, timestamp: Long) {
        if (!validateOneAxisDataType(dataType)) return
        if (!validateBufferSizeForOneAxisSensor(byteBuffer)) return
        val data = byteBuffer.float
        addOneAxisDataToString(dataType, timestamp, data)
    }

    private fun addThreeAxisData(byteBuffer: ByteBuffer, dataType: Int, timestamp: Long) {
        if (validateOneAxisDataType(dataType)) return
        if (!validateBufferSizeForThreeAxisSensor(byteBuffer)) return
        val xAxisData = byteBuffer.float
        val yAxisData = byteBuffer.float
        val zAxisData = byteBuffer.float
        addThreeAxisDataToString(dataType, timestamp, xAxisData, yAxisData, zAxisData)
    }

    private fun addOneAxisDataToString(
        dataType: Int,
        timestamp: Long,
        data: Float
    ) {
        reconstructedOneAxisData.append(dataType).append("|")
        reconstructedOneAxisData.append(timestamp).append("|")
        reconstructedOneAxisData.append(data).append(":")
    }

    private fun addThreeAxisDataToString(
        dataType: Int,
        timestamp: Long,
        xAxisData: Float,
        yAxisData: Float,
        zAxisData: Float
    ) {
        reconstructedTreeAxisData.append(dataType).append("|")
        reconstructedTreeAxisData.append(timestamp).append("|")
        reconstructedTreeAxisData.append(xAxisData).append("|")
        reconstructedTreeAxisData.append(yAxisData).append("|")
        reconstructedTreeAxisData.append(zAxisData).append(":")
    }

}
