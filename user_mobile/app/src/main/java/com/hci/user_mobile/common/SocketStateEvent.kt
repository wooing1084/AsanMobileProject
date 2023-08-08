package com.hci.user_mobile.common

import com.hci.user_mobile.common.SocketState

data class SocketStateEvent(val state: SocketState) {
    val stateName: String = state.name
}