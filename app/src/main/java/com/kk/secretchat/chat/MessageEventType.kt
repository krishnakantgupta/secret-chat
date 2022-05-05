package com.kk.secretchat.chat

object MessageEventType {
    const val NEW_MESSAGE = 1
    const val NEW_INCOMMING_MESSAGE = 2
    const val NEW_OUTGOING_MESSAGE = 3
    const val AFTER_LOGIN_SERVER_CONNECT = 4
    const val LOGIN_FAILED = 5
    const val CONNECTED = 6
    const val DELIVERED = 7
    const val ALREADY_CONNECTED = 8
    const val OTHER_USER_CHAT_STATE = 9
    const val OWN_CHAT_STATE = 10
    const val READ_CHAT_IDS = 11
}