package com.example.husgo.firebasekotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.example.husgo.firebasekotlin.Model.*
import com.example.husgo.firebasekotlin.RecyclerViewAdapter.ChatRoomRecyclerViewAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_chat_room.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class ChatRoomActivity : AppCompatActivity() {
    companion object {
        var isUserIntheChatRoom: Boolean = false
    }

    lateinit var userAuthListener: FirebaseAuth.AuthStateListener
    var dbRef = FirebaseDatabase.getInstance().reference
    lateinit var roomId: String
    var allMessageList: ArrayList<ChatRoomMessages>? = null
    var myAdapter: ChatRoomRecyclerViewAdapter? = null
    var messageIdHashSet: HashSet<String>? = null
    val BASE_URL = "https://fcm.googleapis.com/fcm/"
    var SERVER_KEY: String? = null
    var ab :ActionBar?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        ab=supportActionBar


        userAuthControl()
        readServerKey()
        getRoomId()

        getAllMessagesInChatRoom()
        initSendMessage()


    }

    private fun readServerKey() {
        FirebaseDatabase.getInstance().reference
                .child("server")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        var singleSnapshot = p0.children.iterator().next().getValue().toString()
                        SERVER_KEY = singleSnapshot

                    }

                })
    }

    private fun initSendMessage() {
        etSendMessageText.setOnClickListener {
            rvChatRoomMessages.smoothScrollToPosition(allMessageList?.size!! - 1)
        }
        etSendMessageText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!etSendMessageText.text.trim().isNullOrEmpty()) {
                    imgSendMessageButton.visibility = View.VISIBLE
                    imgSendMessageButton.setOnClickListener {
                        var uniqId = dbRef.push().key
                        var sendMessageText = etSendMessageText.text.toString()
                        var messageObj = ChatRoomMessages()
                        messageObj.content = sendMessageText
                        messageObj.send_user_id = FirebaseAuth.getInstance().currentUser!!.uid
                        messageObj.id = uniqId.toString()
                        messageObj.timestamp = getCurrentTimestamp()
                        dbRef.child("chat_room")
                                .child(roomId)
                                .child("all_messages")
                                .child(uniqId.toString())
                                .setValue(messageObj)

                        var retrofit = Retrofit.Builder()
                                .baseUrl(BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build()

                        var myInterface = retrofit.create(FCMInterface::class.java)
                        var headers = HashMap<String, String>()
                        headers.put("Authorization", "key=" + SERVER_KEY!!)
                        headers.put("Content-Type", "application/json")

                        dbRef.child("chat_room")
                                .child(roomId)
                                .child("room_users")
                                .orderByKey()
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {

                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        for (i in p0.children) {
                                            var id = i.key
                                            if (!id.equals(FirebaseAuth.getInstance().currentUser?.uid)) {


                                                dbRef.child("users")
                                                        .orderByKey()
                                                        .equalTo(id)
                                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                                            override fun onCancelled(p0: DatabaseError) {

                                                            }

                                                            override fun onDataChange(p0: DataSnapshot) {
                                                                var singleUser = p0.children.iterator().next()
                                                                var userToken = singleUser.getValue(Users::class.java)?.token_key
                                                                var sendData = SendNotify.Data(sendMessageText, "Sohbet", "Sohbet Bildirimi", roomId)

                                                                var notifyData = SendNotify(userToken, sendData)
                                                                var request = myInterface.sendNotify(headers, notifyData)
                                                                request.enqueue(object : retrofit2.Callback<Response<SendNotify>> {
                                                                    override fun onFailure(call: Call<Response<SendNotify>>?, t: Throwable?) {

                                                                    }

                                                                    override fun onResponse(call: Call<Response<SendNotify>>?, response: Response<Response<SendNotify>>?) {

                                                                    }

                                                                })


                                                            }

                                                        })
                                                etSendMessageText.setText("")
                                            }
                                            etSendMessageText.setText("")
                                        }


                                    }

                                })


                    }
                } else {
                    imgSendMessageButton.visibility = View.INVISIBLE
                }
            }

        })


    }


    private fun getRoomId() {
        roomId = intent.getStringExtra("roomID")
        getChatRoomName(roomId)
        chatMessageChangeControl()
    }

    private fun getChatRoomName(roomId: String?) {

        dbRef.child("chat_room")
                .orderByKey()
                .equalTo(roomId.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        var roomName = p0.children.iterator().next().getValue(Room::class.java)?.name!!
                        ab?.setTitle(""+roomName);
                    }

                })

    }

    var chatRoomMessagesChangeListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(p0: DataSnapshot) {
            getAllMessagesInChatRoom()
            if (isUserIntheChatRoom)
                updateSawMessageCount(p0.childrenCount.toInt())
        }

        override fun onCancelled(p0: DatabaseError) {

        }

    }

    private fun updateSawMessageCount(messageCount: Int) {
        dbRef.child("chat_room")
                .child(roomId)
                .child("room_users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("message_count")
                .setValue(messageCount)
    }

    private fun getCurrentTimestamp(): String? {

        var currentDate = SimpleDateFormat("HH:mm a", Locale("en"))


        return currentDate.format(Date())
    }

    private fun getAllMessagesInChatRoom() {

        if (allMessageList == null) {
            allMessageList = ArrayList<ChatRoomMessages>()
        }

        if (messageIdHashSet == null) {
            messageIdHashSet = HashSet<String>()
        }


        dbRef.child("chat_room")
                .child(roomId)
                .child("all_messages")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        for (i in p0.children) {
                            var tempMessageObj = ChatRoomMessages()

                            var senduid: String = i.getValue(ChatRoomMessages::class.java)?.send_user_id.toString()

                            if (!messageIdHashSet?.contains(i.key.toString())!!) {
                                messageIdHashSet?.add(i.key.toString())
                                if (senduid != null) {

                                    tempMessageObj.timestamp = i.getValue(ChatRoomMessages::class.java)?.timestamp.toString()
                                    tempMessageObj.content = i.getValue(ChatRoomMessages::class.java)?.content.toString()
                                    tempMessageObj.id = i.getValue(ChatRoomMessages::class.java)?.id.toString()
                                    tempMessageObj.send_user_id = senduid



                                    allMessageList?.add(tempMessageObj)
                                    myAdapter?.notifyDataSetChanged()

                                } else {
                                    tempMessageObj.timestamp = i.getValue(ChatRoomMessages::class.java)?.timestamp.toString()
                                    tempMessageObj.content = i.getValue(ChatRoomMessages::class.java)?.content.toString()
                                    tempMessageObj.id = i.getValue(ChatRoomMessages::class.java)?.id.toString()

                                    allMessageList?.add(tempMessageObj)
                                }
                                rvChatRoomMessages.scrollToPosition(allMessageList?.size!! - 1)
                            }


                        }
                    }

                })

        myAdapter = ChatRoomRecyclerViewAdapter(this, allMessageList!!)

        var myLayoutManager = LinearLayoutManager(this)
        myLayoutManager.stackFromEnd = true
        myLayoutManager.orientation = LinearLayoutManager.VERTICAL
        myLayoutManager.reverseLayout = false
        rvChatRoomMessages.layoutManager = myLayoutManager
        rvChatRoomMessages.adapter = myAdapter

    }


    private fun chatMessageChangeControl() {
        dbRef.child("chat_room")
                .child(roomId)
                .child("all_messages")
                .addValueEventListener(chatRoomMessagesChangeListener)
    }

    private fun userAuthControl() {
        userAuthListener = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var currUser = p0.currentUser
                if (currUser == null) {
                    returnLogin()
                }
            }

        }
    }

    private fun returnLogin() {
        var intent = Intent(this@ChatRoomActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        isUserIntheChatRoom = true
        FirebaseAuth.getInstance().addAuthStateListener(userAuthListener)
    }

    override fun onStop() {
        super.onStop()
        isUserIntheChatRoom = false
        if (FirebaseAuth.getInstance().currentUser != null)
            FirebaseAuth.getInstance().removeAuthStateListener(userAuthListener)
    }

    override fun onResume() {
        super.onResume()
        var user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            returnLogin()
        }
        getAllMessagesInChatRoom()
    }


}

