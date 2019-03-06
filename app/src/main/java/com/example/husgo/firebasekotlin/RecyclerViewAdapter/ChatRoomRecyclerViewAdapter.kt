package com.example.husgo.firebasekotlin.RecyclerViewAdapter

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.husgo.firebasekotlin.Model.ChatRoomMessages
import com.example.husgo.firebasekotlin.Model.Users
import com.example.husgo.firebasekotlin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.single_line_chat_room.view.*
import java.text.SimpleDateFormat

class ChatRoomRecyclerViewAdapter(var context: Context, var allMessages: ArrayList<ChatRoomMessages>) : RecyclerView.Adapter<ChatRoomRecyclerViewAdapter.ChatRoomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        var inflater = LayoutInflater.from(context)
        var view:View?=null

        if (viewType==1){
            view = inflater.inflate(R.layout.single_line_chat_room2, parent, false)
        }else if (viewType==2){
            view = inflater.inflate(R.layout.single_line_chat_room, parent, false)
        }else{
            view = inflater.inflate(R.layout.single_line_chat_room_welcome, parent, false)
        }

        return ChatRoomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return allMessages.size
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        //var currMessageList = allMessages.get(position)
        var currMessageList = allMessages.get(position)
        holder.setData(currMessageList)
    }

    override fun getItemViewType(position: Int): Int {
        if (position!=0){
            if (allMessages.get(position).send_user_id.equals(FirebaseAuth.getInstance().currentUser?.uid)){
                return 1
            }else{
                return 2
            }
        }
            return 0


    }



    inner class ChatRoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        var single_line = view as ConstraintLayout

        var tvAuthor = single_line.tvChatRoomAuthor
        var tvCurrentTimeStamp = single_line.tvChatRoomCurrentTimeStamp
        var imgUserPhoto = single_line.imgChatRoomUserPhoto
        var tvMessageContent = single_line.tvChatRoomMessageContent

        var dbRef = FirebaseDatabase.getInstance().reference
        var currUserName:String?=null
        var currUserImage:String?=null

        fun setData(currMessageList:ChatRoomMessages) {

            if (!currMessageList.send_user_id.isNullOrEmpty()) {


                dbRef.child("users")
                        .orderByKey()
                        .equalTo(currMessageList.send_user_id)
                        .addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {

                            }

                            override fun onDataChange(p0: DataSnapshot) {
                               for (i in p0.children){
                                   currUserName=i.getValue(Users::class.java)?.isim
                                   currUserImage=i.getValue(Users::class.java)?.profile_image

                                   tvAuthor.text = currUserName
                                   Picasso.get().load(currUserImage).resize(200, 200).centerInside().into(imgUserPhoto)
                               }
                            }

                        })



                tvCurrentTimeStamp.text = currMessageList.timestamp
                tvMessageContent.text = currMessageList.content

            }else{
                tvCurrentTimeStamp.text = currMessageList.timestamp
                tvMessageContent.text = currMessageList.content
            }




        }
    }
}