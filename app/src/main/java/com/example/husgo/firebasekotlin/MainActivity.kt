package com.example.husgo.firebasekotlin

import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.support.v7.widget.ToolbarWidgetWrapper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.example.husgo.firebasekotlin.Model.Users
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.messaging.FirebaseMessagingService
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    lateinit var userAuthStateListener: FirebaseAuth.AuthStateListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Ekranı tam ekran yapıyor
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        var  ab = supportActionBar;
        ab?.setTitle("Anasayfa");
        setContentView(R.layout.activity_main)
        initAuthStateListener()

        insertTokenValue()
        getIntenttoFirebaseMessagingService()
    }

    private fun getIntenttoFirebaseMessagingService() {
        var getNotifyIntent=intent
        if (getNotifyIntent.hasExtra("curr_room_id")){
            var intent=Intent(this,ChatRoomActivity::class.java)
            intent.putExtra("roomID",getNotifyIntent.getStringExtra("curr_room_id"))
            startActivity(intent)
        }
    }

    private fun insertTokenValue() {

        var token=FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(object :OnSuccessListener<InstanceIdResult>{
            override fun onSuccess(p0: InstanceIdResult?) {

                        FirebaseDatabase.getInstance().reference
                                .child("users")
                                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                .child("token_key")
                                .setValue(p0?.token)




            }

        }).addOnFailureListener(object :OnFailureListener{
            override fun onFailure(p0: Exception) {
                Toasty.error(this@MainActivity,"Hata :${p0.message}",Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setUserInfo() {
        var user=FirebaseAuth.getInstance().currentUser
        if(user!=null){
            tvEmail.text=user.email
            var query=FirebaseDatabase.getInstance().reference.child("users").orderByKey().equalTo(user.uid).addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (readUser in p0.children){
                        var singleUser=readUser.getValue(Users::class.java)
                        tvUsername.text=singleUser?.isim
                        tvUID.text=singleUser?.user_id
                    }
                }

            })

        }


    }


    private fun initAuthStateListener() {
        userAuthStateListener = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var user = p0.currentUser
                if (user != null) {

                }else{
                    var intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
            }

        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.maimmenu, menu)

        return true
    }

    override fun onResume() {
        super.onResume()
        userAuthControl()
        setUserInfo()
    }

    private fun userAuthControl() {
        var user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            var intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.btnExit -> {
                var dialog=AlertDialog.Builder(this@MainActivity)
                        .setTitle("Çıkış Yapmak İstediğinizden")
                        .setMessage("Emin misiniz?")
                        .setPositiveButton("Evet",object :DialogInterface.OnClickListener{
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                exitUser()
                            }

                        }).setNegativeButton("Hayır",object :DialogInterface.OnClickListener{
                            override fun onClick(dialog: DialogInterface?, which: Int) {

                            }

                        }).setCancelable(false)
                        .show()



                return true
            }
            R.id.btnAccountSettings->{
                var intent=Intent(this@MainActivity,AccountSettingsActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.btnNewChatRoom->{
                var intent=Intent(this@MainActivity,ChatRoomListActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun exitUser() {
        FirebaseAuth.getInstance().signOut()
        //userAuthControl()
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(userAuthStateListener)
    }

    override fun onStop() {
        super.onStop()
        if (userAuthStateListener!=null)
        FirebaseAuth.getInstance().removeAuthStateListener(userAuthStateListener)
    }


}
