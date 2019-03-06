package com.example.husgo.firebasekotlin.Dialog

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.husgo.firebasekotlin.R
import com.google.firebase.auth.FirebaseAuth
import es.dmoral.toasty.Toasty





class PasswordResetFragment : DialogFragment() {
    lateinit var etMail: EditText
    lateinit var fragmentContext:Context
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        fragmentContext= activity!!

        var view = inflater.inflate(R.layout.fragment_password_reset, container, false)
        etMail = view.findViewById(R.id.etForgotPasswordEmail)

        var btnCancel = view.findViewById<Button>(R.id.btnCancelFP)
        var btnGonder = view.findViewById<Button>(R.id.btnSendFP)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }


        btnGonder.setOnClickListener {
            resetPassword(etMail.text.toString())
        }


        return view
    }

    private fun resetPassword(mail: String) {

        FirebaseAuth.getInstance().sendPasswordResetEmail(mail)
                .addOnCompleteListener {task ->

                    if (task.isSuccessful){
                        Toasty.success(fragmentContext, "Şifre Sıfırlama Maili E-postanıza Gönderildi", Toast.LENGTH_SHORT, true).show();

                    }else{
                        Toasty.error(fragmentContext, "Şifre Sıfırlama Maili Gönderilirken Hata :"+task.exception?.message, Toast.LENGTH_SHORT, true).show();

                    }

                    dialog.dismiss()

                }
    }

}
