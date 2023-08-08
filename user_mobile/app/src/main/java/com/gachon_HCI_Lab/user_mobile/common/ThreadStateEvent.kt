package com.gachon_HCI_Lab.user_mobile.common

import com.gachon_HCI_Lab.user_mobile.common.ThreadState

data class ThreadStateEvent(val state: ThreadState) {
    val stateName: String = state.name
}
