package com.kk.secretchat.chat

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.kk.secretchat.AppPref
import com.kk.secretchat.R
import com.kk.secretchat.Utils
import com.kk.secretchat.db.ChatDao
import com.kk.secretchat.db.ChatDatabase
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jivesoftware.smack.*
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.filter.StanzaTypeFilter
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.roster.RosterEntry
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smackx.chatstates.ChatState
import org.jivesoftware.smackx.chatstates.ChatStateManager
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension
import org.jivesoftware.smackx.offline.OfflineMessageManager
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest
import org.json.JSONObject
import org.jxmpp.jid.impl.JidCreate
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import kotlin.collections.ArrayList


class ChatService : Service() {

    private lateinit var mChatStateManager: ChatStateManager
    private var roster: Roster? = null
    private var chatDao: ChatDao? = null
    private var chat: Chat? = null
    private var mConnection: XMPPTCPConnection? = null
    private var senderID = ""
    private var fromID = ""
    private lateinit var pref: AppPref
    private var msgCount = 1
    private val NOTIFICATION_ID = 10014
    private val NOTIFICATION_CHANNEL_ID = "SecretChat"
    private var mNotificationManager: NotificationManager? = null

    //    private var chatIdIncommingList  = ArrayList<String>()
    private var chatIdIncommingList = ""

    var mUsername = ""
    var mPassword = ""
    var mServiceName = ""

    override fun onCreate() {
        super.onCreate()
        pref = AppPref.getInstance(applicationContext)
        EventBus.getDefault().register(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (chatDao == null) {
            chatDao = ChatDatabase.getInstance(this.applicationContext)?.chatDao()
        }
        msgCount = 1
        mUsername = intent.getStringExtra("mUsername") ?: ""
        mPassword = intent.getStringExtra("mPassword") ?: ""
        mServiceName = intent.getStringExtra("mServiceName") ?: ""
        fromID = mUsername
        senderID = pref.senderID
        if (mConnection != null && mConnection!!.isConnected && mConnection!!.isAuthenticated) {
            if (chat == null) {
                EventBus.getDefault()
                    .post(MessageEvent(MessageEventType.AFTER_LOGIN_SERVER_CONNECT))
            } else {
                EventBus.getDefault().post(MessageEvent(MessageEventType.ALREADY_CONNECTED))
                sendReadStatus()
//                chatIdIncommingList = ""

            }
        } else {
            if (mUsername != null && mPassword != null && mServiceName != null) {
                Thread { connect(mUsername, mPassword, mServiceName) }.start()
            }
        }
        generateForegroundNotification()
        return START_STICKY
    }

    private fun connect(mUsername: String, mPassword: String, mServiceName: String) {
        EventBus.getDefault()
            .post(MessageEvent(MessageEventType.LOGIN_FAILED, null, "start Connecting..."))
        appendMessage("Call Connect")
        val sc = SSLContext.getInstance("TLS")
        sc.init(null, arrayOf<X509TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(
                chain: Array<X509Certificate>,
                authType: String
            ) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(
                chain: Array<X509Certificate>,
                authType: String
            ) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                return arrayOfNulls(0)
            }
        }), SecureRandom())


        val builder = XMPPTCPConnectionConfiguration.builder()
            .setUsernameAndPassword(mUsername, mPassword)
            .setHost(mServiceName) //                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
            .setServiceName(JidCreate.serverBareFrom(mServiceName))
            .setCustomSSLContext(sc)
            .setSendPresence(true)
            .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible) //                .setHostnameVerifier(verifier)
            .setDebuggerEnabled(true) //                .setKeystorePath(null)
            //                .setKeystoreType("AndroidCAStore")
            .build() // to view what's happening in detail

        mConnection = XMPPTCPConnection(builder)

        mConnection!!.addConnectionListener(object : ConnectionListener {
            override fun connected(connection: XMPPConnection) {
                appendMessage("CONNECTED")
                EventBus.getDefault()
                    .post(MessageEvent(MessageEventType.LOGIN_FAILED, null, "connecting..."))
                if (!mConnection!!.isAuthenticated)
                    mConnection!!.login()
            }

            override fun authenticated(connection: XMPPConnection, resumed: Boolean) {
                val presence = Presence(Presence.Type.available)
                presence.setStatus("Online and ready to chat")
                connection.sendStanza(presence)
                appendMessage("authenticated")
                sendChatStatus(ChatState.active)
                var mStanzaListener = object : StanzaListener {
                    override fun processStanza(packet: Stanza?) {
                        if (packet is Message && !packet.from.contains(connection.user)) {
                            var msg = packet as Stanza
                            if (!msg.extensions.isEmpty() && msg.extensions.get(0) is ChatStateExtension) {
                                var chatState =
                                    (msg.extensions.get(0) as ChatStateExtension).chatState
//                                Handler(Looper.getMainLooper()).post { view.chatState(chatState) }
                                when (chatState) {
                                    ChatState.inactive, ChatState.gone -> Handler(Looper.getMainLooper()).post { lastSeanUpdate() }
                                    else -> Handler(Looper.getMainLooper()).post {
                                        pref.saveLastSeen(
                                            Date().time
                                        )
                                    }
                                }
                                EventBus.getDefault().post(
                                    MessageEvent(
                                        MessageEventType.OTHER_USER_CHAT_STATE,
                                        null,
                                        "",
                                        chatState
                                    )
                                )
                            }
                        }
                    }
                }
                connection.addSyncStanzaListener(mStanzaListener, StanzaTypeFilter.MESSAGE)
                Handler(Looper.getMainLooper()).post { loginSuccess() }
            }

            override fun connectionClosed() {
                appendMessage("connectionClosed")
                EventBus.getDefault()
                    .post(MessageEvent(MessageEventType.LOGIN_FAILED, null, "connectionClosed"))
                reConnect()
            }

            override fun connectionClosedOnError(e: Exception) {
                appendMessage("connectionClosedOnError " + e.message)
                EventBus.getDefault().post(
                    MessageEvent(
                        MessageEventType.LOGIN_FAILED, null, e.message
                            ?: "Login failed"
                    )
                )
                reConnect()
//                Handler(Looper.getMainLooper()).post { view.loginFailed("Failed :" + e.message) }
            }

            override fun reconnectionSuccessful() {
                appendMessage("reconnectionSuccessful ")
                EventBus.getDefault().post(MessageEvent(MessageEventType.CONNECTED))
            }

            override fun reconnectingIn(seconds: Int) {
                appendMessage("reconnectingIn $seconds")
                reConnect()
                EventBus.getDefault().post(
                    MessageEvent(
                        MessageEventType.LOGIN_FAILED,
                        null,
                        "reconnectingIn $seconds"
                    )
                )
            }

            override fun reconnectionFailed(e: Exception) {
                appendMessage("reconnectionFailed " + e.message)
//                Handler(Looper.getMainLooper()).post { view.loginFailed("Reconnect Failed :" + e.message) }
                EventBus.getDefault().post(
                    MessageEvent(
                        MessageEventType.LOGIN_FAILED,
                        null,
                        "Reconnect Failed " + e.message
                    )
                )
            }

        })
        mConnection!!.connect()

        val reconnectionManager = ReconnectionManager.getInstanceFor(mConnection)
        ReconnectionManager.setEnabledPerDefault(true)
        reconnectionManager.enableAutomaticReconnection()
    }

    private fun lastSeanUpdate() {
        val lastSeenTime = pref.lastSean
        if (lastSeenTime != 0L) {
            var lastSeen = Utils.getLastSeenTime(lastSeenTime)
        } else {

        }
    }

    fun appendMessage(message: String) {
        Log.v("KK---", message)
//        view.appendMessage(message)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageEventType.AFTER_LOGIN_SERVER_CONNECT -> {
                serverConnected()
            }
            MessageEventType.NEW_MESSAGE -> {
                sendMessage(event.message, event.modelChat)
            }
            MessageEventType.OWN_CHAT_STATE -> {
                event.chatState?.let { sendChatStatus(it) }
            }
        }
    }


    fun serverConnected() {
//        mChatStateManager = ChatStateManager.getInstance(mConnection!!)
        val chatManager = ChatManager.getInstanceFor(mConnection!!)
        val jid = JidCreate.entityBareFrom(senderID)
        chat = chatManager.chatWith(jid)
        sendReadStatus()
//        chatManager.createChat("kkcyberlinks@xabber.org", messageListener);
        roster = Roster.getInstanceFor(mConnection!!)
        chatManager.addIncomingListener { from, message, chat ->
            println("New message from " + from + ": " + message.getBody())
            var xml = message.toXML()
            parseIncommingMessage(xml.toString(), message.getBody())
        }
        chatManager.addOutgoingListener { to, message, chat ->
            println("Sending message from " + to + ": " + message.getBody())
            parseOutGoingMessage(message.toXML().toString(), message.getBody())
        }
        val entries: Set<RosterEntry> = roster!!.getEntries()
        for (entry in entries) {
            println("kk--roster:" + entry)
        }
        var offlineMessageManager = OfflineMessageManager(mConnection!!)
        val size: Int = offlineMessageManager.getMessageCount()
        val messages: List<Message> = offlineMessageManager.getMessages()
        println("OFFLINE message $size")
        for (msg in messages) {
            println("KK--" + msg.toXML())
        }
    }

    private var gson = Gson()
    private fun parseIncommingMessage(xml: String?, message: String) {
        try {
            var jsonString = getJSON(xml)
            println(jsonString)
            var jsonObject = JSONObject(jsonString)
            jsonString = jsonObject.getString("message").toString()
            var incommingMessageModal = gson.fromJson(jsonString, IncommingMessageModal::class.java)
            incommingMessageModal.body = message
            println(incommingMessageModal)
            if (incommingMessageModal.isReadSubject) {
                AsyncTask.execute {
                    incommingMessageModal.readIds.forEach { id ->
                        if (id.isNotEmpty()) {
                            chatDao?.updateRead(id)
                        }
                    }
                    EventBus.getDefault()
                        .post(
                            MessageEvent(
                                MessageEventType.READ_CHAT_IDS,
                                null,
                                "",
                                null,
                                null,
                                incommingMessageModal.readIds.toList()
                            )
                        )
                }
            } else {
                var modelChat = ModelChat(incommingMessageModal)
                AsyncTask.execute { chatDao?.insert(modelChat) }
//            view.newMessageAdd(modelChat)
                EventBus.getDefault()
                    .post(MessageEvent(MessageEventType.NEW_INCOMMING_MESSAGE, modelChat))
                sendNotification(incommingMessageModal.id)
            }
        } catch (e: Exception) {
            println(e.printStackTrace())
        }
    }

    fun getJSON(xml: String?): String {
        if (xml != null) {
            val xmlToJson = XmlToJson.Builder(xml!!).build()
            var json = xmlToJson.toFormattedString()
            return json.replace("\n", "", false)
        }
        return ""
    }

    fun sendReadStatus() {
        AsyncTask.execute {
            var ids = chatDao?.selectAllUnreadIds()
            var idsData = ""
            ids?.let {id->
                if (!id.isEmpty()) {
                    idsData = Constant.READ_PREFIX
                    id.forEach {
                        idsData = "$idsData$it#"
                        chatDao?.updateRead(it)
                    }
                    println("kk unread Ids:$idsData")
                    chat?.let {
                        var message = Message()
                        message.body = ""
                        message.type = Message.Type.chat
                        message.subject = idsData
                        chat?.send(message)
                    }
                }
            }
        }
    }

    fun sendMessage(text: String, modelChat: ModelChat?) {
//        chat.send(text)
        chat?.let {
            var message = Message()
            message.body = text
            message.type = Message.Type.chat
            modelChat?.let {
                message.subject = modelChat.repliedID
            }
            DeliveryReceiptRequest.addTo(message)
            chat?.send(message)
            val dm = DeliveryReceiptManager.getInstanceFor(mConnection!!)
            dm.autoReceiptMode = DeliveryReceiptManager.AutoReceiptMode.always
            dm.autoAddDeliveryReceiptRequests()
            dm.addReceiptReceivedListener { fromJid, toJid, receiptId, receipt ->
                println("addReceiptReceivedListener")
                println(fromJid)
                println(toJid)
                println(receiptId)
                println(receipt)
                println("-------------------")
                EventBus.getDefault()
                    .post(MessageEvent(MessageEventType.DELIVERED, null, receiptId))
                AsyncTask.execute { chatDao?.updateDelivery(receiptId) }
            }
        }
    }


    private fun parseOutGoingMessage(xml: String?, message: String) {
        try {
            var jsonString = getJSON(xml)
            println(jsonString)
            var jsonObject = JSONObject(jsonString)
            jsonString = jsonObject.getString("message").toString()
            var outGoingMessageModel = gson.fromJson(jsonString, OutGoingMessageModel::class.java)
            if (!outGoingMessageModel.isReadSubject) {
                outGoingMessageModel.body = message
                outGoingMessageModel.from = fromID
                var modelChat = ModelChat(outGoingMessageModel)
                modelChat.isSend = true
                println(outGoingMessageModel)
                AsyncTask.execute { chatDao?.insert(modelChat) }
                EventBus.getDefault()
                    .post(MessageEvent(MessageEventType.NEW_OUTGOING_MESSAGE, modelChat))
            }
        } catch (e: Exception) {
            println(e.printStackTrace())
        }
    }


    private fun loginSuccess() {
        senderID = pref.senderID
        EventBus.getDefault().post(MessageEvent(MessageEventType.AFTER_LOGIN_SERVER_CONNECT))
    }

    private fun initConnection() {

    }

    private var notifManager: NotificationManager? = null

    private fun generateForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val intentMainLanding = Intent(this, MainActivity::class.java)
//            val pendingIntent =
//                PendingIntent.getActivity(this, 0, intentMainLanding, 0)
            var iconNotification = BitmapFactory.decodeResource(resources, R.drawable.ic_read)
            if (mNotificationManager == null) {
                mNotificationManager =
                    this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert(mNotificationManager != null)
                mNotificationManager?.createNotificationChannelGroup(
                    NotificationChannelGroup("chats_group", "Chats")
                )
                val notificationChannel =
                    NotificationChannel(
                        "service_channel", "Service Notifications",
                        NotificationManager.IMPORTANCE_MIN
                    )
                notificationChannel.enableLights(false)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                mNotificationManager?.createNotificationChannel(notificationChannel)
            }
            val builder = NotificationCompat.Builder(this, "service_channel")

            builder.setContentTitle("Junk file searching")
                .setTicker("Junk file searching")
                .setContentText("DB Cleaning...") //                    , swipe down for more options.
                .setSmallIcon(R.drawable.app_icon)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
            if (iconNotification != null) {
                builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
            }
            builder.color = resources.getColor(R.color.purple_200)
            startForeground(20004, builder.build())
        }

    }

    private fun sendNotification(chatId: String) {
        if (appOpen()) {
            chatIdIncommingList = ""
            msgCount = 1
            return
        }
        chatIdIncommingList = "$chatIdIncommingList$chatId#"
        var aTitle = "Junk File Found."
        var aMessage = "File Count: $msgCount"
        val description = "New Notification Description"
        val id = "New Notification id"
        val name = "New Notification Name"
        val builder: NotificationCompat.Builder
        if (notifManager == null) {
            notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            var mChannel: NotificationChannel? = notifManager!!.getNotificationChannel(id)
            if (mChannel == null) {
                mChannel = NotificationChannel(id, name, importance)
                mChannel.description = description
                mChannel.enableVibration(true)
                mChannel.setShowBadge(true)
                mChannel.vibrationPattern = longArrayOf(200, 400)
                notifManager!!.createNotificationChannel(mChannel)
            }
            builder = NotificationCompat.Builder(this, id)

            builder.setContentTitle(aMessage)  // required
                .setSmallIcon(R.drawable.ic_read) // required
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setNumber(msgCount)
                .setContentTitle(aTitle)
                .setTicker(aMessage)
                .setContentText(aMessage)
                .setVibrate(longArrayOf(200, 400))
        } else {

            builder = NotificationCompat.Builder(this)
            builder.setContentTitle(aMessage)                           // required
                .setSmallIcon(R.drawable.ic_read) // required
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setNumber(msgCount)
                .setContentTitle(aTitle)
                .setTicker(aMessage)
                .setContentText(aMessage)
                .setVibrate(longArrayOf(200, 400)).priority = Notification.PRIORITY_HIGH
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(alarmSound)
        }

        if (!appOpen()) {
            msgCount++
            notifManager!!.notify(NOTIFICATION_ID, builder.build())
        }
    }

    fun sendChatStatus(chatState: ChatState) {
        AsyncTask.execute {
            try {
                if (senderID.isNullOrEmpty()) {
                    senderID = pref.senderID
                }
                if (mConnection?.isConnected == true && mConnection?.isAuthenticated == true) {
                    var message = Message()
                    message.type = Message.Type.chat
                    message.from = JidCreate.bareFrom(fromID)
                    message.to = JidCreate.bareFrom(senderID)
                    message.addExtension(ChatStateExtension(chatState))
                    mConnection?.sendStanza(message)
                }
            } catch (e: Exception) {
                Log.e("m/service", "ChatState Not Sent: " + e.message);
                e.printStackTrace();
            }
        }
    }

    fun appOpen(): Boolean {
        val am = this
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager ?: return false

        val runningProcesses = am.runningAppProcesses

        for (processInfo in runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (activeProcess in processInfo.pkgList) {
                    if (activeProcess.equals("com.kk.secretchat", ignoreCase = true)) {
                        // App is in foreground
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun reConnect() {
        if (mUsername != null && mPassword != null && mServiceName != null) {
            disconnect()
            Thread { connect(mUsername, mPassword, mServiceName) }.start()
        }
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

    private fun disconnect() {
        Thread {
            if (mConnection?.isConnected == true) {
                mConnection?.disconnect()
            }
        }.start()
    }
}