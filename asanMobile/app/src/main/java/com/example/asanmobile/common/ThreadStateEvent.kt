package com.example.asanmobile.common

data class ThreadStateEvent(val state: ThreadState) {
    val stateName: String = state.name
}
