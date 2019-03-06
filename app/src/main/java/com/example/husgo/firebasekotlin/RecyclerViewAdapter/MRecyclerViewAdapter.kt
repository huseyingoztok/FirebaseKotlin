package com.example.husgo.firebasekotlin.RecyclerViewAdapter

import android.content.DialogInterface
import android.content.Intent
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.husgo.firebasekotlin.ChatRoomActivity
import com.example.husgo.firebasekotlin.ChatRoomListActivity
import com.example.husgo.firebasekotlin.Model.Room
import com.example.husgo.firebasekotlin.Model.Users
import com.example.husgo.firebasekotlin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.util.logging.Handler

class MRecyclerViewAdapter(var context: AppCompatActivity, var allRooms: ArrayList<Room>?) : RecyclerView.Adapter<MRecyclerViewAdapter.MViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        var mView = inflater.inflate(R.layout.single_line_room_info, parent, false)
        return MViewHolder(mView)
    }

    override fun getItemCount(): Int {
        return allRooms?.size!!
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {
        holder.setData(position)
    }


    inner class MViewHolder(var singleLine: View) : RecyclerView.ViewHolder(singleLine) {
        var imgUserPhoto: ImageView? = null
        var tvUserName: TextView? = null
        var imgDeleteRoom: ImageView? = null
        var tvMessageCount: TextView? = null
        var tvRoomName: TextView? = null
        var dbRef = FirebaseDatabase.getInstance().reference

        fun setData(position: Int) {
            var currRoom = allRooms?.get(position)



            imgUserPhoto = singleLine.findViewById(R.id.imgSingleLineUserPhoto)
            imgDeleteRoom = singleLine.findViewById(R.id.imgSingleLineDeleteRoom)
            tvMessageCount = singleLine.findViewById(R.id.tvMessageCount)
            tvUserName = singleLine.findViewById(R.id.tvSingleLineUsername)
            tvRoomName = singleLine.findViewById(R.id.tvSingleLineRoomName)



            singleLine.setOnClickListener {
                saveUserInChatRoom(currRoom)
                var intent = Intent(context, ChatRoomActivity::class.java)
                intent.putExtra("roomID", currRoom?.id)
                context.startActivity(intent)
            }



            if (!FirebaseAuth.getInstance().currentUser!!.uid.equals(currRoom?.create_user_id)) {
                imgDeleteRoom?.visibility = View.INVISIBLE
            }


            imgDeleteRoom?.setOnClickListener {
                var dialog = android.support.v7.app.AlertDialog.Builder(singleLine.context!!)
                dialog.setTitle("Emin misiniz ?")
                dialog.setMessage("Sohbet Odasını Silmek İstediğinizden")
                dialog.setCancelable(true)
                dialog.setPositiveButton("Evet", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        (context as ChatRoomListActivity).roomDelete(currRoom?.id.toString())

                    }

                })

                dialog.setNegativeButton("Hayır", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                    }

                })
                dialog.show()

            }

            dbRef.child("users")
                    .orderByKey()
                    .equalTo(currRoom?.create_user_id.toString())
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            for (readUser in p0.children) {
                                var currUser = readUser.getValue(Users::class.java)
                                Picasso.get().load(currUser?.profile_image).resize(200, 200).centerInside().into(imgUserPhoto)

                                tvUserName?.text = currUser?.isim
                                tvRoomName?.text = currRoom?.name


                            }
                        }

                    })



            tvMessageCount?.text = currRoom?.message_list?.size.toString()


        }

        private fun saveUserInChatRoom(currRoom: Room?) {
            dbRef.child("chat_room")
                    .child(currRoom?.id!!)
                    .child("room_users")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("message_count")
                    .setValue(currRoom.message_list!!.size)
        }

    }

}