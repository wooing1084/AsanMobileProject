package com.example.asanmobile.common

data class SocketStateEvent(val state: SocketState) {
    val stateName: String = state.name
}