package com.kk.secretchat.chat

import org.jivesoftware.smackx.chatstates.ChatState

data class MessageEvent(
    var type: Int = 0,
    var modelChat: ModelChat? = null,
    var message: String = "",
    var chatState: ChatState? = null,
    var other: Any? = null,
    var ids: List<String>?  = null
)
