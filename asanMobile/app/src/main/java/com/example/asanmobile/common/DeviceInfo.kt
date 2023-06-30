package com.example.asanmobile.common

class DeviceInfo private constructor() {
    companion object{
        lateinit var _deviceID : String
        lateinit var _uID : String
        lateinit var _battery : String

        fun init(deviceID : String = "", uID : String = "", battery: String = "100") {
            _deviceID = deviceID
            _uID = uID
            _battery = battery
        }

        fun setDeviceID(deviceID: String) {
            _deviceID = deviceID
        }

        fun setUID(uID: String) {
            _uID = uID
        }

        fun setBattery(battery: String) {
            this._battery = battery
        }
    }
}