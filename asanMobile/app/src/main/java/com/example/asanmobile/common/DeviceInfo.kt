package com.example.asanmobile.common

class DeviceInfo private constructor(){
    companion object{
        lateinit var _deviceID : String
        lateinit var _uID : String

        fun init(deviceID : String = "", uID : String = ""){
            _deviceID = deviceID
            _uID = uID
        }

        fun setDeviceID(deviceID: String){
            _deviceID = deviceID
        }

        fun setUID(uID: String){
            _uID = uID
        }
    }
}