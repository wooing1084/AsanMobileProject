package com.gachon_HCI_Lab.user_mobile.service

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.gachon_HCI_Lab.user_mobile.common.SocketState
import com.gachon_HCI_Lab.user_mobile.common.SocketStateEvent
import com.gachon_HCI_Lab.user_mobile.common.ThreadState
import com.gachon_HCI_Lab.user_mobile.common.ThreadStateEvent
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.io.InputStream
import java.util.*

object BluetoothConnect {
    private lateinit var serverSocket: BluetoothServerSocket
    private lateinit var socket: BluetoothSocket
    private lateinit var inputStream: InputStream
    private var isRunning: Boolean = true

    private val SOCKET_NAME = "server"
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    @SuppressLint("MissingPermission")
    fun createSeverSocket(bluetoothAdapter: BluetoothAdapter){
        serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(SOCKET_NAME, MY_UUID)
    }

    @Throws(IOException::class)
    fun createBluetoothSocket(): BluetoothSocket {
        try {
            isRunning = true
            socket = serverSocket.accept()
            Thread.sleep(300)
        } catch (e: IOException) {
            EventBus.getDefault().post(ThreadStateEvent(ThreadState.STOP))
            throw IOException()
        }
        return socket
    }

    fun createInputStream(): InputStream{
        inputStream = socket.inputStream
        EventBus.getDefault().post(SocketStateEvent(SocketState.CONNECT))
        return inputStream
    }

    fun clear(){
        isRunning = false
        socket.close()
        serverSocket.close()
    }

    fun isBluetoothRunning(): Boolean{
        return isRunning
    }

    fun disconnectRunning(){
        isRunning = false
    }
}
