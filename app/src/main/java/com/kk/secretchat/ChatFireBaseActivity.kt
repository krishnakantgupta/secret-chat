package com.kk.secretchat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kk.secretchat.chat.ChatPresenter
import com.kk.secretchat.chat.ModelChat
import java.util.*


class ChatFireBaseActivity : AppCompatActivity() {

    var mUserDatabase: DatabaseReference? = null
    var mChatData: DatabaseReference? = null
    var mUserData: DatabaseReference? = null
    var mAuth: FirebaseAuth? = null
    private lateinit var mainView: View
    private lateinit var btnSend: View
    private lateinit var txtMessage: EditText
    private lateinit var presenter: ChatPresenter
    private lateinit var recylerView: RecyclerView
    private lateinit var pref: AppPref
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarMessage: TextView
    private lateinit var ownStatus: TextView

    var username = "null"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_fire_base)

        pref = AppPref.getInstance(applicationContext)
        toolbar = findViewById(R.id.toolbar)
        txtMessage = findViewById(R.id.txtMessage)
        btnSend = findViewById(R.id.btnSend)
        mainView = findViewById(R.id.mainView)
        recylerView = findViewById(R.id.recyclerView)
        toolbarMessage = findViewById(R.id.toolbarMessage)
        ownStatus = findViewById(R.id.ownStatus)

        mAuth = FirebaseAuth.getInstance();
        mChatData = FirebaseDatabase.getInstance().getReference().child("chats");
        mUserData = FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(mAuth!!.getUid())
            ?: "")

        mUserData!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                username = Objects.requireNonNull(dataSnapshot.child("username").value).toString()
                Log.v("KK---",username)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        btnSend.setOnClickListener(View.OnClickListener {
            sendMessage()
        })

//        var adapter = FirebaseListAdapter<ModelChat>(this, ModelChat.class, R.layout.list_msg, FirebaseDatabase.getInstance().getReference().child("chats").limitToLast(100)) {
//            @Override
//            protected void populateView(View v, Model_Chat model, int position) {
//
//                TextView messageText, messageUser, messageTime;
//                messageText = v.findViewById(R.id.message_text);
//                messageUser = v.findViewById(R.id.message_user);
//                messageTime = v.findViewById(R.id.message_time);
//
//                messageText.setText(model.getMessage());
//                messageUser.setText(model.getName());
//                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getTimestamp()));
//
//            }
//        };
//        listOfMessage.setAdapter(adapter);
//    }
    }

    private fun sendMessage() {
        var message = txtMessage.text
        if (message != null && message.toString().trim().isNotEmpty()) {
            mChatData!!.push().setValue(ModelChat(message.toString(), "", System.currentTimeMillis()))

//            val messageRef = FirebaseDatabase.getInstance()
//                .getReference(com.sun.org.apache.bcel.internal.classfile.Utility.DEBUG).child(com.sun.org.apache.bcel.internal.classfile.Utility.MESSAGES).child(chatMessageModel.chatId)
//            val key = messageRef.push().key
//            chatMessageModel.messageId = key
//            messageRef.child(key!!).setValue(chatMessageModel)
        }
    }
}