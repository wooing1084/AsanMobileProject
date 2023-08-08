package com.hci.user_mobile.common

import com.hci.user_mobile.common.ThreadState

data class ThreadStateEvent(val state: ThreadState) {
    val stateName: String = state.name
}
