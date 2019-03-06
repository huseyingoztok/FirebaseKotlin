package com.example.husgo.firebasekotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.example.husgo.firebasekotlin.Dialog.ChatSettingsFragment
import com.example.husgo.firebasekotlin.Model.ChatRoomMessages
import com.example.husgo.firebasekotlin.Model.Room
import com.example.husgo.firebasekotlin.RecyclerViewAdapter.MRecyclerViewAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_chat_room_list.*

class ChatRoomListActivity : AppCompatActivity() {
    var allRooms: ArrayList<Room>? = null
    lateinit var userAuthListener: FirebaseAuth.AuthStateListener
    var roomIdList: HashSet<String>? = null

    var mAdapter: MRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room_list)
        var  ab = supportActionBar;
        ab?.setTitle("Sohbet Odaları");

        Init()

    }

    var chatRoomsControlEventListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {

        }

        override fun onDataChange(p0: DataSnapshot) {
            getAllChatRoomsAndMessages()
        }

    }


    fun Init() {
        userAuthControl()
        changeInRoomsControl()
        getAllChatRoomsAndMessages()



        fabCreateNewChatRoom.setOnClickListener {
            var dialog = ChatSettingsFragment()
            dialog.show(supportFragmentManager, "showcreatechatsettingsdialog")
        }


    }



    private fun changeInRoomsControl() {
        var dbRef = FirebaseDatabase.getInstance().reference
        getAllChatRoomsAndMessages()
        dbRef.child("chat_room").addValueEventListener(chatRoomsControlEventListener)


    }


    private fun userAuthControl() {
        userAuthListener = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var user = p0.currentUser

                if (user == null) {
                    returnToLogin()
                }
            }

        }
    }


    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(userAuthListener)

    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(userAuthListener)

    }

    override fun onResume() {
        super.onResume()
        if (FirebaseAuth.getInstance().currentUser == null) {
            returnToLogin()
        }
        //getAllChatRoomsAndMessages()
        //Init()

        if (mAdapter != null) {
            roomIdList = null
            allRooms = null
            Init()
        }
    }

    private fun returnToLogin() {
        var intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getAllChatRoomsAndMessages() {
        if (allRooms == null) {
            allRooms = ArrayList<Room>()
        }

        if (roomIdList == null) {
            roomIdList = HashSet<String>()
        }


        var dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("chat_room").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (currRoom in p0.children) {
                    var currRoomId = currRoom.getValue(Room::class.java)?.id
                    if (!roomIdList?.contains(currRoomId)!!) {
                        roomIdList?.add(currRoomId.toString())

                    if (currRoom.getValue(Room::class.java)?.isactive!!){
                        var readRoom = currRoom.getValue(Room::class.java)
                        var allMessages = ArrayList<ChatRoomMessages>()
                        for (currMessages in currRoom.child("all_messages").children) {
                            var messages = currMessages.getValue(ChatRoomMessages::class.java)
                            allMessages.add(messages!!)

                        }
                        readRoom?.message_list = allMessages
                        allRooms!!.add(readRoom!!)
                    }

                    }

                }

                chatRoomsAdapterSettings()

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }


    private fun chatRoomsAdapterSettings() {

        mAdapter = MRecyclerViewAdapter(this@ChatRoomListActivity, allRooms)
        mAdapter!!.notifyDataSetChanged()
        rvChatList.adapter = mAdapter

        var lManager = LinearLayoutManager(this@ChatRoomListActivity, LinearLayoutManager.VERTICAL, false)
        rvChatList.layoutManager = lManager
    }

    fun roomDelete(roomId: String) {
        var dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("chat_room")
                .child(roomId)
                .child("isactive")
                .setValue(false)
        Toasty.info(this@ChatRoomListActivity, "Sohbet Odasını Sildiniz ...", Toast.LENGTH_SHORT, true).show();

        if (mAdapter != null) {
            roomIdList = null
            allRooms = null
            Init()
        }
    }
}
