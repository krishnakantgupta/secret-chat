package com.kk.secretchat

import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kk.secretchat.chat.*
import com.kk.secretchat.databinding.ActivityXabberChatBinding
import com.kk.secretchat.db.ChatDatabase
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jivesoftware.smackx.chatstates.ChatState
import java.util.*




class XabberChatActivity : AppCompatActivity(), ChatView {
    private lateinit var binding: ActivityXabberChatBinding
    private lateinit var txtUsername: EditText
    private lateinit var loginView: View
    private lateinit var mainView: View
    private lateinit var txtPassword: EditText
    private lateinit var btnLogin: View
    private lateinit var btnSend: View
    private lateinit var txtMessage: EditText
    private lateinit var presenter: ChatPresenter
    private lateinit var recylerView: RecyclerView
    private lateinit var pref: AppPref
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarMessage: TextView
    private lateinit var ownStatus: TextView
    private lateinit var lastSeenView: TextView
    private lateinit var minuteDialog: View
    private lateinit var txtMinut: EditText
    private lateinit var replyView: RelativeLayout
    private lateinit var tvReplyMsg: TextView
    private var adapter: ChatRecyclerAdapter? = null
    private var replyChatId: String? = null
    private var replyChatMsg: String? = null

    var senderID: String? = null
    var loginID = ""
    var isLogin = false
    var isMinutDialogVisible = false
    val lastSeanText = "Last seen : "
    var SERVER = "@xabber.org"

    var dd: Long = 0L
    private fun sendStatus(chatstate: ChatState) {
        presenter.sendChatStatus(chatstate)
    }

    override fun onResume() {
        var notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.cancel(10014)
        if (isLogin) {
            recylerView.visibility = View.VISIBLE
            sendStatus(ChatState.active)
        }
        super.onResume()
    }

    override fun onPause() {
        if (isLogin) {
            recylerView.visibility = View.GONE
            sendStatus(ChatState.inactive)
        }
//        if (pref.calHideFor > -1) {
//            var diff = Utils.getTimeDifferenceInMinutes(AppPref.lastOpenDate, Date().time)
//            if (diff >= pref.calHideFor) {
//                finish()
//            }
//        } else {
//            finish()
//        }
        finish()

        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xabber_chat)
        AppPref.lastOpenDate = Date().time
        pref = AppPref.getInstance(applicationContext)
        toolbar = findViewById(R.id.toolbar)
        txtUsername = findViewById(R.id.txtUserName)
        txtPassword = findViewById(R.id.txtPassword)
        txtMessage = findViewById(R.id.txtMessage)
        btnSend = findViewById(R.id.btnSend)
        btnLogin = findViewById(R.id.btnLogin)
        loginView = findViewById(R.id.loginView)
        mainView = findViewById(R.id.mainView)
        recylerView = findViewById(R.id.recyclerView)
        toolbarMessage = findViewById(R.id.toolbarMessage)
        ownStatus = findViewById(R.id.ownStatus)
        minuteDialog = findViewById(R.id.minuteDialog)
        txtMinut = findViewById(R.id.txtMinutes)
        replyView = findViewById<RelativeLayout>(R.id.reply_view)
        replyView.visibility = View.GONE
        findViewById<View>(R.id.btnSetTime).setOnClickListener {
            setTime()
        }
        findViewById<View>(R.id.close_ReplyView).setOnClickListener {
            replyView.visibility = View.GONE
            replyChatId = null
            replyChatMsg = null
        }
        tvReplyMsg = findViewById(R.id.tvReplyMsg)
        toolbar.title = ""
        toolbar.setTitleTextColor(Color.WHITE)
//        senderID = findViewById<EditText>(R.id.txtSender).text.toString()
        val username = "" + SERVER
        val password = ""

        findViewById<View>(R.id.btnReverse).setOnClickListener { v ->
//            txtUsername.setText(senderID)
//            txtPassword.setText("")
//            findViewById<EditText>(R.id.txtSender).setText(username)
//            senderID = username
        }


        setSupportActionBar(toolbar)//        supportActionBar.
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_product_details)

        presenter = ChatPresenter(this)
        var userData = pref.userData
        senderID = pref.senderID
        if (senderID == null) {
            senderID = findViewById<EditText>(R.id.txtSender).text.toString()
            pref.saveSenderID(senderID)
        }

        btnLogin.setOnClickListener {
            loginID = txtUsername.text.toString().split("@".toRegex()).toTypedArray()[0]
            senderID = findViewById<EditText>(R.id.txtSender).text.toString()
            pref.saveSenderID(senderID)
            presenter.doLogin(txtUsername.text.toString(), txtPassword.text.toString(), senderID!!)
            loginView.visibility = View.GONE
            mainView.visibility = View.VISIBLE
        }
        btnSend.setOnClickListener {
            sendMessage()
        }
        recylerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)

        txtMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (!s.toString().isNullOrEmpty()) {
                    if (dd == 0L) {
                        dd = Date().time
                        presenter.sendChatStatus(ChatState.composing)
                    } else {
                        var diff = Utils.getTimeDifferenceInSec(dd, Date().time)
                        if (diff >= 5) {
                            dd = Date().time
                            presenter.sendChatStatus(ChatState.composing)
                        }
                    }
                } else {
                    presenter.sendChatStatus(ChatState.paused)
                }
            }
        })
        EventBus.getDefault().register(this)
        if (userData == null) {
            loginView.visibility = View.VISIBLE
            mainView.visibility = View.GONE
        } else {
            ownStatus.text = "Login - please wait..."
            loginID = userData[0].substring(0, userData[0].indexOf("@"))
            loginView.visibility = View.GONE
            mainView.visibility = View.VISIBLE
            presenter.doLogin(userData[0], userData[1], senderID!!)
        }

        registerForContextMenu(recylerView)
    }


    private fun setTime() {
        if (txtMinut.text != null && txtMinut.text.toString().trim().length > 0) {
            var value = txtMinut.text.toString().trim().toInt()
            if (value > 10 || value <= 0) {
                Toast.makeText(
                    this@XabberChatActivity,
                    "Enter Less than 15 minute",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                pref.saveCalHideFor(value)
                isMinutDialogVisible = false
                minuteDialog.visibility = View.GONE
            }
        }
    }

    fun fetchOldChat() {
        presenter.setChatDao(ChatDatabase.getInstance(this.applicationContext)?.chatDao())
        if (senderID != null) {
            presenter.setSender(pref.senderID)
            adapter = ChatRecyclerAdapter(this)
            recylerView.adapter = adapter
            presenter.fetchOldChat()
        }
    }

    override fun loginSuccess() {
        isLogin = true
        ownStatus.text = loginID + "- Connected"
        loginView.visibility = View.GONE
        mainView.visibility = View.VISIBLE
        if (pref.userData == null) {
            pref.saveUserData(txtUsername.text.toString(), txtPassword.text.toString())
            pref.saveSenderID(senderID!!)
        }
        fetchOldChat()
        presenter.sendChatStatus(ChatState.active)
    }


    override fun appendMessage(message: String) {
        runOnUiThread(object : Runnable {
            override fun run() {
            }
        })
    }

    override fun fetchChatSuccess(modelList: List<ModelChat>) {
        adapter?.setData(modelList as ArrayList<ModelChat>)
    }

    override fun loginFailed(message: String) {
        ownStatus.text = message
    }


    override fun newMessageAdd(modelChat: ModelChat) {
        runOnUiThread {
            adapter?.addNew(modelChat)
            adapter?.list?.size?.let {
                recylerView.scrollToPosition(it)
            }

            if (!modelChat.isIncomming) {
                txtMessage.setText("")
                btnSend.isEnabled = true
            }
        }
    }

    private fun sendMessage() {
        var message = txtMessage.text
        if (message != null && message.toString().trim().isNotEmpty()) {
            btnSend.isEnabled = false
            var message = message.toString().trim()
            replyView.visibility = View.GONE
            var rId = replyChatId
            presenter.sendMessage(message, rId, replyChatMsg)
            replyChatId = null
        }
    }

    override fun onDestroy() {
        presenter.sendChatStatus(ChatState.gone)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> presenter.deleteAll()
            R.id.alert -> openDailog()
            R.id.call -> makeCall()
        }
        return true
    }
    private fun makeCall(){

    }

    private fun openDailog() {
        isMinutDialogVisible = true
        minuteDialog.visibility = View.VISIBLE

    }

    override fun onBackPressed() {
        if (!isMinutDialogVisible) {
            super.onBackPressed()
        } else {
            isMinutDialogVisible = false
            minuteDialog.visibility = View.GONE
        }
    }

    override fun deleteAll() {
        adapter?.list?.clear()
        adapter?.setData(ArrayList())
    }

    override fun delivered(id: String) {
        var position = -1
        var size = adapter?.list?.size ?: 0
        var count = 0
        while (count < size) {
            if (adapter?.list?.get(count)?.chatID.equals(id)) {
                position = count
                break
            }
        }
        if (position != -1) {
            var model = adapter?.list?.get(count)!!
            model.isDeliver = true
            adapter?.updatePosition(position, model)
            presenter.updateChat(model)
        }
    }

    override fun chatState(chatStates: ChatState) {
        runOnUiThread {
            when (chatStates) {
                ChatState.composing -> toolbar.title =
                    "typing..."//toolbarMessage.setText("typing...")
                ChatState.active -> toolbar.title = "Online"//toolbarMessage.setText("Online")
                ChatState.paused -> toolbar.title = "Online"//toolbarMessage.setText("Online")
                ChatState.gone -> toolbar.title = "Offline"//toolbarMessage.setText("Offline")
                else -> toolbar.title = "Offline"//toolbarMessage.setText("Offline")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageEventType.AFTER_LOGIN_SERVER_CONNECT -> {
                loginSuccess()
            }
            MessageEventType.CONNECTED -> {
                ownStatus.text = loginID + "- Connected"
            }

            MessageEventType.ALREADY_CONNECTED -> {
                ownStatus.text = loginID + "- Connected"
                loginSuccess()
            }
            MessageEventType.DELIVERED -> {
                var id = event.message
                delivered(id)
            }
            MessageEventType.LOGIN_FAILED -> {
                ownStatus.text = event.message
            }
            MessageEventType.NEW_OUTGOING_MESSAGE -> {
                event.modelChat?.let { newMessageAdd(it) }
            }
            MessageEventType.NEW_INCOMMING_MESSAGE -> {
                event.modelChat?.let { newMessageAdd(it) }
            }
            MessageEventType.OTHER_USER_CHAT_STATE -> {
                event.chatState?.let { chatState(it) }
            }
            MessageEventType.READ_CHAT_IDS ->{
                event.ids?.let {
                    readChat(it)
                }
            }
        }
    }

    private fun readChat(ids: List<String>) {
        adapter?.readChat(ids)
    }

    override fun getAppContext(): Context {
        return applicationContext
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        var position = adapter?.position ?: -1
        when (item.itemId) {
            R.id.copy -> {
                doCopy()
            }
            R.id.reply -> {
                doReply()
            }
            else -> {
                Toast.makeText(this, "positon " + position, Toast.LENGTH_SHORT).show()
            }

        }
        return super.onContextItemSelected(item)
    }

    private fun doReply() {

        var position = adapter?.position ?: -1
        if (position >= 0) {
            var modelChat = adapter!!.list[position]
            tvReplyMsg.text = modelChat.message
            replyView.visibility = View.VISIBLE
            replyChatId = modelChat.chatID
            replyChatMsg = modelChat.message
        }
    }

    private fun doCopy() {
        var position = adapter?.position ?: -1
        if (position >= 0) {
            var modelChat = adapter!!.list[position]
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Message", modelChat.message)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Message Copied!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?) {
        val inflater: MenuInflater = getMenuInflater()
        inflater.inflate(R.menu.chat_context_menu, menu)
    }
}