package com.example.husgo.firebasekotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.husgo.firebasekotlin.Dialog.PasswordResetFragment
import com.example.husgo.firebasekotlin.Dialog.ReSendEmailDialogFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    lateinit var myAuthStateListener: FirebaseAuth.AuthStateListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        var ab = supportActionBar;
        ab?.setTitle("Giriş Yap");
        initAuthStateListener()

        tvRegister.setOnClickListener {
            var intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        tvReSendVerificationMail.setOnClickListener {
            var dialog = ReSendEmailDialogFragment()
            dialog.show(supportFragmentManager, "gonder")

        }

        btnLogin.setOnClickListener {

            if (etEmail.text.isNotEmpty() && etPassword.text.isNotEmpty()) {

                loginUser(etEmail.text.toString(), etPassword.text.toString())


            } else {

                Toasty.info(this, "Form alanı boş bırakılmamalıdır.", Toast.LENGTH_SHORT, true).show();
            }


        }


        tvForgotPassword.setOnClickListener {
            var forgotPassDialog = PasswordResetFragment()
            forgotPassDialog.show(supportFragmentManager, "forgotpassword")

        }

    }


    private fun loginUser(mail: String, password: String) {
        progresVisible()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(mail, password)
                .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                    override fun onComplete(p0: Task<AuthResult>) {

                        if (p0.isSuccessful) {
                            if (!p0.result.user.isEmailVerified) {
                                FirebaseAuth.getInstance().signOut()
                            }
//                            Toasty.success(this@LoginActivity, "Giriş Başarılı " + mail, Toast.LENGTH_SHORT, true).show();
//                            var intent = Intent(this@LoginActivity, MainActivity::class.java)
//                            startActivity(intent)
//                            finish()
                        } else {
                            Toasty.error(this@LoginActivity, "Giriş İşlemi Yapılırken Hata :" + p0.exception?.message, Toast.LENGTH_SHORT, true).show();

                        }


                        progresInvisible()
                    }
                })


    }

     fun initAuthStateListener() {

        myAuthStateListener = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var user = p0.currentUser
                if (user != null) {
                    if (user.isEmailVerified) {


                                var intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                                Toasty.success(this@LoginActivity, "Giriş Başarılı " + user.email, Toast.LENGTH_SHORT, true).show();




                    } else {


                                Toasty.info(this@LoginActivity, "Email Aktivasyon işleminizi yapınız.", Toast.LENGTH_SHORT, true).show();




                    }
                }

            }
        }

        }


        override fun onStart() {
            super.onStart()
            FirebaseAuth.getInstance().addAuthStateListener(myAuthStateListener)
        }

        override fun onStop() {
            super.onStop()
            FirebaseAuth.getInstance().removeAuthStateListener(myAuthStateListener)
        }

        private fun progresVisible() {
            progressBarLogin.visibility = View.VISIBLE
        }

        private fun progresInvisible() {
            progressBarLogin.visibility = View.INVISIBLE
        }


    }
