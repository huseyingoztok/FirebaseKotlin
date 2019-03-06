package com.example.husgo.firebasekotlin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.husgo.firebasekotlin.Model.Users
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : Activity() {
    var user = FirebaseAuth.getInstance().currentUser
    //lateinit var myAuthStateListener:FirebaseAuth.AuthStateListener
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        var  ab = actionBar;
        ab?.setTitle("Üye Ol");

        btnRegister.setOnClickListener {

            if (etMail.text.isNotEmpty() && etRePassword.text.isNotEmpty() && etPassword.text.isNotEmpty()) {
                if (etRePassword.text.toString().equals(etPassword.text.toString())) {
                    registerUser(etMail.text.toString(), etPassword.text.toString())
                } else {

                    Toasty.error(this, "Şifre ile Şifre Tekrar uyuşmuyor.", Toast.LENGTH_SHORT, true).show();
                }
            } else {
                Toasty.info(this, "Form alanı boş bırakılmamalıdır.", Toast.LENGTH_SHORT, true).show();
            }

        }


    }

    private fun sendVerificationMail() {
        var user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(object : OnCompleteListener<Void> {
                override fun onComplete(p0: Task<Void>) {
                    if (p0.isSuccessful) {
                        Toasty.success(this@RegisterActivity, "Onay Maili " + user.email + " Adresinize Gönderildi.", Toast.LENGTH_SHORT, true).show();
                    } else {
                        Toasty.error(this@RegisterActivity, "Mail Gönderilirken Bir Hata Oluştu :" + p0.exception?.message, Toast.LENGTH_SHORT, true).show();
                    }
                }

            })
        }
    }

    private fun registerUser(mail: String, password: String) {
        progresbarVisible()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail, password)
                .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                    override fun onComplete(p0: Task<AuthResult>) {
                        if (p0.isSuccessful) {

                            var userObj = Users()
                            userObj.isim=mail.substring(0, mail.indexOf("@"))
                            userObj.phone="123"
                            userObj.profile_image=""
                            userObj.seviye="1"
                            userObj.user_id=FirebaseAuth.getInstance().currentUser?.uid
                            FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser?.uid!!).setValue(userObj).addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                Toasty.success(this@RegisterActivity, "Kullanıcı başarılı bir şekilde kaydedildi :"+p0.result.user.email, Toast.LENGTH_SHORT, true).show();
                                sendVerificationMail()
                                returLoginPage()
                                FirebaseAuth.getInstance().signOut()
                            } else{
                                Toasty.error(this@RegisterActivity, "Kayıt Eklenirken Hata :"+task.exception?.message, Toast.LENGTH_SHORT, true).show();
                            }
                            }
                        } else {
                            Toasty.error(this@RegisterActivity, "Kullanıcı kaydedilirken bir hata oluştu :" + p0.exception?.message, Toast.LENGTH_SHORT, true).show();
                        }
                        progressbarInvisible()

                    }

                })

    }


    fun progresbarVisible() {
        progressBar.visibility = View.VISIBLE
    }

    fun progressbarInvisible() {
        progressBar.visibility = View.INVISIBLE
    }
    private fun returLoginPage(){
        var intent=Intent(this@RegisterActivity,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}
