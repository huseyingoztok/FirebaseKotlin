package com.example.husgo.firebasekotlin.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.example.husgo.firebasekotlin.ChatRoomActivity
import com.example.husgo.firebasekotlin.MainActivity
import com.example.husgo.firebasekotlin.Model.Room
import com.example.husgo.firebasekotlin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class MyFirebaseMessagingService:FirebaseMessagingService() {
    var numberOfUnreadableMessages=0
    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
        var token=p0;
        Log.e("NEWTOKEN","TOKEN :$token")

        insertTokenDatabase(token)
    }
    private fun insertTokenDatabase(token: String?) {
        var currUser=FirebaseAuth.getInstance().currentUser
        if (currUser!=null){
            FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("token_key")
                    .setValue(token)
        }

    }
    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)

        if (!isUserinActivity()){
            var messageTitle=p0?.notification?.title
            var messageBody=p0?.notification?.body
            var messageData=p0?.data

            var title=messageData?.get("title").toString()
            var content=messageData?.get("content").toString()
            var notify_type=messageData?.get("notify_type").toString()
            var chat_room_id=messageData?.get("chat_room_id").toString()


            var dbRef=FirebaseDatabase.getInstance().reference
            dbRef.child("chat_room")
                    .orderByKey()
                    .equalTo(chat_room_id)
                    .addListenerForSingleValueEvent(object :ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            var singleRoom=p0.children.iterator().next()

                            var allMessageCount=singleRoom.child("all_messages")
                                    .childrenCount.toInt()
                            var readableMessageCount=singleRoom.child("room_users")
                                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .child("message_count").getValue().toString().toInt()
                            numberOfUnreadableMessages=allMessageCount-readableMessageCount

                            for (i in p0.children){
                                var currChatRoom=i.getValue(Room::class.java)
                                sendNotification(title,content,currChatRoom)


                            }



                        }

                    })
        }

    }

    private fun sendNotification(title: String, content: String, currChatRoom: Room?) {
        var notifyId=AtomicLong(0)

        Log.e("FCM100","NOTFYID"+notifyId)
        var pendingIntent=Intent(this,MainActivity::class.java)
        pendingIntent.putExtra("curr_room_id",currChatRoom!!.id)
        pendingIntent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        var notifyIntent=PendingIntent.getActivity(this,10,pendingIntent,PendingIntent.FLAG_UPDATE_CURRENT)



        var notificationObj=NotificationCompat.Builder(this,currChatRoom?.name.toString())
                .setSmallIcon(R.drawable.ic_action_user)
                .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.ic_action_user))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(currChatRoom?.name+" odasından yeni "+title)
                .setAutoCancel(true)
                .setSubText(numberOfUnreadableMessages.toString()+" yeni mesaj")
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setNumber(numberOfUnreadableMessages)
                .setOnlyAlertOnce(true)
                .setContentIntent(notifyIntent)

                notificationObj.color=resources.getColor(R.color.colorAccent)

        var notifyManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifyManager.notify(notifyId.incrementAndGet().toInt(),notificationObj.build())
    }


    private  fun isUserinActivity():Boolean{
        if (ChatRoomActivity.isUserIntheChatRoom){
            return true
        }else{
            return false
        }

    }


    private fun getNotifyId():String{
        return UUID.randomUUID().toString()
    }


}