package com.gachon_HCI_Lab.user_mobile.common

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import java.lang.reflect.Method

/**
 * [BTRManager]
 * 블루투스 관련 기능을 담당하는 싱글톤 클래스
 * BluetoothDevice의 모든 데이터들은 접근시에 permission check가 필요하기 때문에
 * 코드양이 늘어날 것을 고려하여 몇몇 기능들은 해당 클래스에 모아둘 예정이다.
 */
class BTManager {
    companion object{
        private val tag = "BluetoothManager"

        val uuidFixed = arrayOf("00001101", "0000", "1000", "8000", "00805F9B34FB")

        /**
         * [connectedDevices]
         * 블루투스 페어링 되어있는 모든 기기를 가져오는 메소드
         * context : 액티비티의 context
         * return : 페어링 되어있는 모든 기기 리스트
         */
        fun connectedDevices(context: Context): MutableSet<BluetoothDevice>? {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ){
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    1
                )
            }

            val pairedDevices = bluetoothAdapter.bondedDevices

            return pairedDevices
        }

        /**
         * [isConnected]
         * 해당 기기가 연결되어있는지 확인하는 메소드. 워치가 연결되어있는지 확인할 때 사용
         * device : 연결되어있는지 확인할 기기
         */
        fun isConnected(device: BluetoothDevice): Any? {
            val m: Method = device.javaClass.getMethod("isConnected")
            return m.invoke(device)
        }

        /**
         * [getUUID]
         * 해당 기기의 UUID를 가져오는 메소드
         * context : 액티비티의 context
         * device : UUID를 가져올 기기
         */
        fun getUUID(context : Context, device: BluetoothDevice?): String {
            if (device == null)
                return ""
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ){
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    1
                )
            }

            var dUuid = ""

            for (uuid in device.uuids) {
                val splitted = uuid.toString().split("-")

                if(splitted[1].compareTo(uuidFixed[1]) == 0)
                    continue
                if(splitted[2].compareTo(uuidFixed[2]) == 0)
                    continue
                if(splitted[3].compareTo(uuidFixed[3]) == 0)
                    continue
                if(splitted[4].compareTo(uuidFixed[4]) == 0)
                    continue

                dUuid = uuid.toString()
                break;
            }

           return dUuid
        }

        /**
         * [getConnectedDevice]
         * 연결되어있는 기기를 가져오는 메소드. 페어링 된 기기 리스트에서 블루투스 연결되어있는지 확인한다.
         * context : 액티비티의 context
         * devices : 페어링 된 기기 리스트
         * return : 연결되어있는 기기
         */
        fun getConnectedDevice(context: Context, devices : MutableSet<BluetoothDevice>?): BluetoothDevice? {
            /*
            수정 고려 사항: 모바일 기기에 에어팟과 같은 블루투스 기기를 동시에 사용중인 경우 워치가 아닌 다른 기기가 검색될수도 있을거같음
             */

            if (devices == null)
                return null

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ){
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    1
                )
            }

            for (device in devices){
                if (isConnected(device) == true){
                    Log.d(tag, "Connected Device : ${device.name}")
                    return device
                }
            }

            return null
        }
    }
}