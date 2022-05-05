package com.kk.secretchat.chat

import android.content.Context
import org.jivesoftware.smackx.chatstates.ChatState

interface ChatView {
    fun loginSuccess()
    fun loginFailed(message: String)
    fun appendMessage(message: String)
    fun fetchChatSuccess(modelList: List<ModelChat>)
    fun newMessageAdd(modelChat: ModelChat)
    fun deleteAll()
    fun delivered(id:String)
    fun chatState(chatStates:ChatState)
    fun getAppContext() : Context
}