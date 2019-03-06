package com.example.husgo.firebasekotlin.Dialog


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.SupportActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.husgo.firebasekotlin.LoginActivity
import com.example.husgo.firebasekotlin.MainActivity
import com.example.husgo.firebasekotlin.Model.Users
import com.example.husgo.firebasekotlin.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import es.dmoral.toasty.Toasty


class ReSendEmailDialogFragment : DialogFragment() {

    lateinit var fragmentContext: Context
    lateinit var etMail: EditText
    lateinit var etPassword: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        fragmentContext = activity!!.applicationContext
        var view = inflater.inflate(R.layout.fragment_re_send_email_dialog, container, false)
        etMail = view.findViewById(R.id.etMail)
        etPassword = view.findViewById(R.id.etPassword)

        var btnVazgec = view.findViewById<Button>(R.id.btnCancel)

        btnVazgec.setOnClickListener {
            dialog.dismiss()
        }

        var btnGonder = view.findViewById<Button>(R.id.btnReSendVerificationMail)

        btnGonder.setOnClickListener {
            controlUser(etMail.text.toString(), etPassword.text.toString())
            dismiss()


        }
        return view
    }


    private fun controlUser(mail: String, password: String) {
        var myCredential = EmailAuthProvider.getCredential(mail, password)

        FirebaseAuth.getInstance().signInWithCredential(myCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reSendVerificationMail()




            } else {
                Toasty.error(fragmentContext, "Hata :" + task.exception?.message, Toast.LENGTH_SHORT, true).show();

            }
        }

    }

    private fun reSendVerificationMail() {


        var user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            if (!user.isEmailVerified) {
                user.sendEmailVerification().addOnCompleteListener(object : OnCompleteListener<Void> {
                    override fun onComplete(p0: Task<Void>) {
                        if (p0.isSuccessful) {
                            Toasty.success(fragmentContext, "Aktivasyon Maili " + user.email + " Adresinize Gönderildi.", Toast.LENGTH_SHORT, true).show();



                        } else {
                            Toasty.error(fragmentContext, "Hata :" + p0.exception?.message, Toast.LENGTH_SHORT, true).show()


                        }

                    }

                })
            } else {

                Toasty.info(fragmentContext, "Hesabınız zaten aktive edilmiş.", Toast.LENGTH_SHORT).show()

            }

        }
    }


}
