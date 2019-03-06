package com.example.husgo.firebasekotlin.Dialog


import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.husgo.firebasekotlin.ChatRoomListActivity
import com.example.husgo.firebasekotlin.Model.ChatRoomMessages
import com.example.husgo.firebasekotlin.Model.Room
import com.example.husgo.firebasekotlin.Model.Users
import com.example.husgo.firebasekotlin.R
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import es.dmoral.toasty.Toasty
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class ChatSettingsFragment : DialogFragment() {
    lateinit var etChatRoomName: EditText
    lateinit var seekBarChatLevel: SeekBar
    lateinit var tvChatLevel: TextView
    lateinit var btnCreatNewChatRoom: Button
    var userLevel = 0
    var getChangeSeekBarLevel = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var v = inflater.inflate(R.layout.fragment_chat_settings, container, false)

        etChatRoomName = v.findViewById(R.id.etChatRoomName)
        seekBarChatLevel = v.findViewById(R.id.seekBarChatLevel)
        tvChatLevel = v.findViewById(R.id.tvShowLevel)
        btnCreatNewChatRoom = v.findViewById(R.id.btnCreateNewChatRoom)

        tvChatLevel.text = getChangeSeekBarLevel.toString()
        seekBarChatLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                getChangeSeekBarLevel = progress
                tvChatLevel.text = getChangeSeekBarLevel.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
       getUserLevel()


        btnCreatNewChatRoom.setOnClickListener {

            if (!etChatRoomName.text.isNullOrEmpty()){

                if (seekBarChatLevel.progress<=userLevel){
                    var dbRef=FirebaseDatabase.getInstance().reference
                    var roomID=dbRef.push().key
                    var room= Room()

                    room.name=etChatRoomName.text.toString()
                    room.id=roomID.toString()
                    room.level=getChangeSeekBarLevel.toString()
                    room.create_user_id=FirebaseAuth.getInstance().currentUser!!.uid
                    room.isactive=true


                    dbRef.child("chat_room").child(roomID!!).setValue(room)
                            .addOnSuccessListener(object :OnSuccessListener<Void>{
                                override fun onSuccess(p0: Void?) {
                                    Toasty.success(activity?.applicationContext!!, "Oda Kaydedildi", Toast.LENGTH_SHORT, true).show();
                                    dialog.dismiss()

                                    var welcome_message=ChatRoomMessages()
                                    welcome_message.content="Sohbet Odasına Hoşgeldiniz ..."
                                    welcome_message.timestamp=getCurrentTimestamp()
                                    welcome_message.id
                                    var messageID=dbRef.child("chat_room").push().key
                                    dbRef.child("chat_room")
                                            .child(roomID)
                                            .child("all_messages")
                                            .child(messageID!!)
                                            .setValue(welcome_message)


                                    (activity as ChatRoomListActivity).Init()
                                }

                            }).addOnFailureListener(object :OnFailureListener{
                                override fun onFailure(p0: Exception) {
                                    Toasty.error(activity?.applicationContext!!, "Hata :"+p0?.message, Toast.LENGTH_SHORT, true).show();

                                    dialog.dismiss()
                                }

                            })
                }else{
                    Toasty.info(activity?.applicationContext!!, "Kendi seviyenizden ("+userLevel+") büyük oda açamazsınız ...", Toast.LENGTH_SHORT, true).show();

                }

            }else{
                Toasty.info(activity?.applicationContext!!, "Chat Odasının ismi girilmelidir...", Toast.LENGTH_SHORT, true).show();

            }

        }




        return v
    }

    private fun getCurrentTimestamp(): String? {

        var currentDate= SimpleDateFormat("dd-MM-yyyy HH:mm:ss",Locale("tr"))


        return currentDate.format(Date())
    }

    private fun getUserLevel() {

        var dbRef = FirebaseDatabase.getInstance().reference
        dbRef
                .child("users")
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().currentUser!!.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        for (i in p0.children) {
                            userLevel = i.getValue(Users::class.java)?.seviye!!.toInt()
                        }
                    }

                })

    }


}


