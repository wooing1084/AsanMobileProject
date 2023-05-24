package com.example.asanmobile

data class SocketStateEvent(val state: SocketState) {
    val stateName: String = state.name
}