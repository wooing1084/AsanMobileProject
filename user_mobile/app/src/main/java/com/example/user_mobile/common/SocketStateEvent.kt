package com.example.user_mobile.common

data class SocketStateEvent(val state: SocketState) {
    val stateName: String = state.name
}