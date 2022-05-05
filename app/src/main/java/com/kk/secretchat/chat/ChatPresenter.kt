package com.kk.secretchat.chat

import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.kk.secretchat.db.ChatDao
import org.greenrobot.eventbus.EventBus
import org.jivesoftware.smack.MessageListener
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smackx.chatstates.ChatState
import org.jivesoftware.smackx.chatstates.ChatStateListener
import java.util.*


class ChatPresenter(private val view: ChatView) {

    private var chatDao: ChatDao? = null
    private var roster: Roster? = null
    private var senderID = ""
    private var fromID = ""

    val STATUS_ONLINE = 0
    val STATUS_OFFLINE = 1
    val STATUS_TYPING = 2

    fun setChatDao(chatDao: ChatDao?) {
        this.chatDao = chatDao
    }

    fun doLogin(jid: String, mPassword: String, senderID: String) {
        fromID = jid
        var mUsername = jid.split("@".toRegex()).toTypedArray()[0]
        var mServiceName = jid.split("@".toRegex()).toTypedArray()[1]
        var intent = Intent(view.getAppContext(), ChatService::class.java)
        intent.putExtra("mUsername", mUsername)
        intent.putExtra("mPassword", mPassword)
        intent.putExtra("mServiceName", mServiceName)
        intent.putExtra("senderID", senderID)
        view.getAppContext().startService(intent)
    }


    fun sendChatStatus(chatState: ChatState) {
        EventBus.getDefault().post(MessageEvent(MessageEventType.OWN_CHAT_STATE, null, "", chatState))
//        try {
//            if (mConnection?.isConnected == true && mConnection?.isAuthenticated == true) {
//                var message = Message()
//                message.type = Message.Type.chat
//                message.to = JidCreate.bareFrom(senderID)
//                message.addExtension(ChatStateExtension(chatState))
//                mConnection?.sendStanza(message)
//            }
//        } catch (e: Exception) {
//            Log.e("m/service", "ChatState Not Sent: " + e.message);
//            e.printStackTrace();
//        }
    }


    fun updateChat(chatModel: ModelChat) {
        AsyncTask.execute { chatDao?.update(chatModel) }
    }

    fun disconnect() {
//        Thread {
//            if (mConnection?.isConnected == true) {
//                mConnection?.disconnect()
//            }
//        }.start()
    }

    fun setSender(senderID: String) {
//        this.senderID = senderID
//        serverConnected()
    }

    fun fetchOldChat() {
        AsyncTask.execute {
            var before12hours = (Date().time) - 1000 * 60 * 60 * 24
//            var chatList = chatDao?.selectAllFromTime(Date(before12hours).time) ?: listOf()
            var chatList = chatDao?.selectAllFromTime(before12hours) ?: listOf()
            Handler(Looper.getMainLooper()).post { view.fetchChatSuccess(chatList) }
        }
    }

    fun deleteAll() {
        AsyncTask.execute {
            chatDao?.deleteAll()
            Handler(Looper.getMainLooper()).post { view.deleteAll() }
        }
    }

    class MessageListenerImpl : MessageListener, ChatStateListener {

        override fun processMessage(chat: org.jivesoftware.smack.chat.Chat?, message: Message?) {
            TODO("Not yet implemented")
            Log.v("KK---Received message: ", "" + message)

        }

        override fun stateChanged(chat: org.jivesoftware.smack.chat.Chat?, state: ChatState?, message: Message?) {
//            if (ChatState.composing.equals(state)) {
//                Log.d("Chat State",state.getParticipant() + " is typing..");
//            } else if (ChatState.gone.equals(state)) {
//                Log.d("Chat State",state.getParticipant() + " has left the conversation.");
//            } else {
//                Log.d("Chat State",state.getParticipant() + ": " + arg1.name());
//            }
            when (state) {
                ChatState.active -> Log.v("state", "active")
                ChatState.composing -> Log.v("state", "composing")
                ChatState.paused -> Log.v("state", "paused")
                ChatState.inactive -> Log.v("state", "inactive")
                ChatState.gone -> Log.v("state", "gone")
            }
        }

        override fun processMessage(message: Message?) {
            Log.v("KK---processMessage: ", "" + message)
        }
    }

    fun sendMessage(text: String, replyID:String?, replyChatMsg: String?) {
        var  model  = ModelChat()
        if(replyID!=null && !replyID.equals("null")) {
            model.repliedID = replyID.let { ";#;reply$it##$replyChatMsg" }
        }
        EventBus.getDefault().post(MessageEvent(MessageEventType.NEW_MESSAGE, model , text))
    }


}