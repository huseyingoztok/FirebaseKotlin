package com.example.husgo.firebasekotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.request.RequestOptions
import com.example.husgo.firebasekotlin.Dialog.UserPhotoFragment
import com.example.husgo.firebasekotlin.Dialog.onGetPhoto
import com.example.husgo.firebasekotlin.Model.Users
import com.google.android.gms.tasks.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_account_settings.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

class AccountSettingsActivity : AppCompatActivity(), onGetPhoto {
    var user = FirebaseAuth.getInstance().currentUser!!
    var permissions = false
    var getCameraImage: Bitmap? = null
    var getGalleryImage: Uri? = null
    val MEGABYTE = 1000000

    override fun getCamera(bitmap: Bitmap?) {
        getCameraImage = bitmap
        imgUserPhoto.maxWidth = 100
        imgUserPhoto.maxHeight = 100
        imgUserPhoto.setImageBitmap(getCameraImage)
    }

    override fun getGallery(imageUri: Uri?) {
        getGalleryImage = imageUri
        Picasso.get().load(getGalleryImage).into(imgUserPhoto)
    }

    inner class ImageUploadProcess : AsyncTask<Uri, Double, ByteArray> {
        var myBitmap: Bitmap? = null

        constructor() {}

        constructor(bm: Bitmap) {

            this.myBitmap = bm

        }

        override fun onPreExecute() {

            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Uri?): ByteArray {

            if (myBitmap == null) {
                myBitmap = MediaStore.Images.Media.getBitmap(this@AccountSettingsActivity.contentResolver, params[0])
                Log.e("BASARILI", "" + myBitmap!!.byteCount.toDouble() / MEGABYTE.toDouble())
            }


            var imageBytes: ByteArray? = null
            for (i in 1..10) {
                imageBytes = imageToByteArray(myBitmap, 100 / i)
                publishProgress(imageBytes?.size?.toDouble())
            }

            return imageBytes!!
        }

        private fun imageToByteArray(image: Bitmap?, compressedQuality: Int): ByteArray {
            var stream = ByteArrayOutputStream()
            image?.compress(Bitmap.CompressFormat.JPEG, compressedQuality, stream)

            return stream.toByteArray()
        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)

            uploadResimtoFirebase(result)

        }

        override fun onProgressUpdate(vararg values: Double?) {
            super.onProgressUpdate(*values)
        }
    }

    private fun uploadResimtoFirebase(result: ByteArray?) {

        var storageRef = FirebaseStorage.getInstance().reference
        var imagePath = storageRef.child("images/users/" + FirebaseAuth.getInstance().currentUser?.uid + "/profil_resmi")
        var uploadTask = imagePath.putBytes(result!!)
        var dbRef = FirebaseDatabase.getInstance().reference


        uploadTask.addOnFailureListener(object : OnFailureListener {
            override fun onFailure(p0: Exception) {

                Toasty.error(this@AccountSettingsActivity, "Hata" + p0.message, Toast.LENGTH_SHORT, true).show();
            }

        }).addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
            override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                Toasty.success(this@AccountSettingsActivity, "Resim yüklendi", Toast.LENGTH_SHORT, true).show();
            }

        })


        var urlTask = uploadTask.continueWithTask(object : Continuation<UploadTask.TaskSnapshot, Task<Uri>> {
            override fun then(p0: Task<UploadTask.TaskSnapshot>): Task<Uri> {
                if (!p0.isSuccessful) {
                    progresPhotoInVisible()
                    throw p0.exception!!
                }

                return imagePath.downloadUrl
            }
        }).addOnCompleteListener { task ->

            dbRef.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("profile_image").setValue(task.result.toString()).addOnSuccessListener(object : OnSuccessListener<Void> {
                override fun onSuccess(p0: Void?) {
                    Toasty.success(this@AccountSettingsActivity, "Resim veritabanına eklendi", Toast.LENGTH_SHORT, true).show();
                }

            }).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(p0: Exception) {
                    Toasty.error(this@AccountSettingsActivity, "Hata" + p0.message, Toast.LENGTH_SHORT, true).show();

                }


            })
        }
        progresPhotoInVisible()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)
        var  ab = supportActionBar;
        ab?.setTitle("Hesap Ayarları");
        setAccountInfo()

        imgUserPhoto.setOnClickListener {

            if (permissions) {
                progresPhotoVisible()
                var dialog = UserPhotoFragment()
                dialog.show(supportFragmentManager, "selectimage")
            } else {
                takePermission()
            }

        }

        tvMailPasswordUpdate.setOnClickListener {
            if (etPassword.text.toString().isNotEmpty()) {
                var updatePasswordUser = EmailAuthProvider.getCredential(user.email!!, etPassword.text.toString())
                user.reauthenticate(updatePasswordUser).addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        updateLayout.visibility = View.VISIBLE
                        btnNewPasswordUpdate.setOnClickListener {
                            userUpdatePassword()
                        }
                        btnNewMailUpdate.setOnClickListener {
                            userUpdateMail()
                        }
                    } else {
                        updateLayout.visibility = View.INVISIBLE
                        Toasty.error(this@AccountSettingsActivity,"Authenticate işleminde hata :" + task.exception?.message, Toast.LENGTH_SHORT, true).show();
                    }


                }
            } else {
                Toasty.info(this@AccountSettingsActivity, "Şifre alanı boş geçilmemelidir.", Toast.LENGTH_SHORT, true).show();
            }
            etPassword.setText("")



        }


        tvForgotPassword.setOnClickListener {
            FirebaseAuth.getInstance().sendPasswordResetEmail(user.email!!)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {
                            Toasty.success(this@AccountSettingsActivity, "Şifre Sıfırlama Maili E-postanıza Gönderildi", Toast.LENGTH_SHORT, true).show();
                        } else {
                            Toasty.error(this@AccountSettingsActivity, "Şifre Sıfırlama Maili Gönderilirken Hata :" + task.exception?.message, Toast.LENGTH_SHORT, true).show();
                        }

                    }
        }



        btnSaveChanged.setOnClickListener {
            if (etUsername.text.toString().isNotEmpty()) {

                var isim = etUsername.text.toString()
                var phone = etPhone.text.toString()
                var query = FirebaseDatabase.getInstance().reference.child("users").orderByKey().equalTo(user.uid)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        for (readUser in p0.children) {
                            var singleUser = readUser.getValue(Users::class.java)
                            if (!singleUser?.isim.equals(isim)) {
                                FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser?.uid!!).child("isim").setValue(isim).addOnCompleteListener { task ->

                                    if (task.isSuccessful) {
                                        Toasty.success(this@AccountSettingsActivity, "İsim Güncelleme İşlemi Başarılı", Toast.LENGTH_SHORT, true).show();

                                    } else {
                                        Toasty.error(this@AccountSettingsActivity, "Hata :" + task.exception?.message, Toast.LENGTH_SHORT, true).show();

                                    }


                                }
                            }

                            if (!singleUser?.phone.equals(phone)) {
                                FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser?.uid!!).child("phone").setValue(phone).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toasty.success(this@AccountSettingsActivity, "Telefon Güncelleme İşlemi Başarılı", Toast.LENGTH_SHORT, true).show();

                                    } else {
                                        Toasty.error(this@AccountSettingsActivity, "Hata :" + task.exception?.message, Toast.LENGTH_SHORT, true).show();

                                    }
                                }
                            }
                        }
                    }
                })
            } else {
                Toasty.info(this@AccountSettingsActivity, "Kullanıcı adı boş olmamalıdır.", Toast.LENGTH_SHORT, true).show();
            }


            if (getGalleryImage != null) {
                compressedImage(getGalleryImage)
            } else if (getCameraImage != null) {
                compressedImage(getCameraImage)
            }

        }

    }

    private fun compressedImage(galleryImage: Uri?) {

        var imageUploadObj = ImageUploadProcess()
        imageUploadObj.execute(galleryImage)

    }

    private fun compressedImage(cameraImage: Bitmap?) {
        var uri: Uri? = null

        var imageUploadObj = ImageUploadProcess(cameraImage!!)
        imageUploadObj.execute(uri)

    }

    private fun takePermission() {
        var permissionList = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (ContextCompat.checkSelfPermission(this@AccountSettingsActivity, permissionList[0]) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this@AccountSettingsActivity, permissionList[0]) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this@AccountSettingsActivity, permissionList[0]) == PackageManager.PERMISSION_GRANTED) {
            permissions = true
        } else {
            ActivityCompat.requestPermissions(this, permissionList, 150)

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 150) {
            if (grantResults.get(0) == PackageManager.PERMISSION_GRANTED && grantResults.get(1) == PackageManager.PERMISSION_GRANTED && grantResults.get(2) == PackageManager.PERMISSION_GRANTED) {
                var dialog = UserPhotoFragment()
                dialog.show(supportFragmentManager, "selectimage")
            } else {
                Toasty.info(this@AccountSettingsActivity, "Gerekli izinleri vermelisiniz...", Toast.LENGTH_SHORT, true).show();

            }
        }
    }

    private fun userUpdateMail() {
        var user = FirebaseAuth.getInstance().currentUser!!
        if (etNewMail.text.toString().isNotEmpty()) {
            if (user != null) {

                FirebaseAuth.getInstance().fetchSignInMethodsForEmail(etNewMail.text.toString()).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result.signInMethods?.size!! > 0) {
                            Toasty.warning(this@AccountSettingsActivity, "Yazdığınız mail başka bir kullanıcı tarafından kullanılıyor", Toast.LENGTH_SHORT, true).show();
                        } else {
                            user.updateEmail(etNewMail.text.toString()).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toasty.success(this@AccountSettingsActivity, "Emailiniz Başarıyla değiştirildi", Toast.LENGTH_SHORT, true).show();

                                    etNewMail.setText("")
                                    updateLayout.visibility = View.INVISIBLE
                                    returLoginPage()
                                    FirebaseAuth.getInstance().signOut()
                                } else
                                Toasty.error(this@AccountSettingsActivity, "Email  Değiştirilirken Hata :" + task.exception?.message, Toast.LENGTH_SHORT, true).show();

                            }
                        }
                    } else {
                        Toasty.error(this@AccountSettingsActivity, "Mail Günceleme işleminde Hata" + task.exception?.message, Toast.LENGTH_SHORT, true).show();

                    }

                }


            }
        } else {
            Toasty.info(this@AccountSettingsActivity, "Mail Alanı Boş Geçilemez", Toast.LENGTH_SHORT, true).show();
        }


    }

    private fun userUpdatePassword() {

        if (etNewPassword.text.toString().isNotEmpty()) {
            if (user != null) {
                user.updatePassword(etNewPassword.text.toString()).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toasty.success(this@AccountSettingsActivity, "Şifreniz Başarıyla değiştirildi", Toast.LENGTH_SHORT, true).show();
                        returLoginPage()
                        FirebaseAuth.getInstance().signOut()
                        etNewPassword.setText("")
                        updateLayout.visibility = View.INVISIBLE
                    } else
                    Toasty.error(this@AccountSettingsActivity, "Şifre  Değiştirilirken Hata :" + task.exception?.message, Toast.LENGTH_SHORT, true).show();

                }
            }
        } else {
            Toasty.info(this@AccountSettingsActivity, "Şifre Alanı Boş Geçilemez.", Toast.LENGTH_SHORT, true).show();

        }


    }

    private fun setAccountInfo() {
        var user = FirebaseAuth.getInstance().currentUser!!

        if (user != null) {

            tvMail.text = user.email

            var reference = FirebaseDatabase.getInstance().reference
            var query = reference.child("users").orderByKey().equalTo(user.uid)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (readUser in p0.children) {

                        var singleUser = readUser.getValue(Users::class.java)
                        etUsername.setText(singleUser?.isim.toString())
                        etPhone.setText(singleUser?.phone.toString())
                        Log.e("SETACCOUNTINFON", "" + singleUser?.profile_image)
                        var optionsReq=RequestOptions()

                        Picasso.get().load(Uri.parse(singleUser?.profile_image)).error(R.drawable.ic_action_user).into(imgUserPhoto)


                    }
                }

            })


        }


    }

    private fun returLoginPage() {
        var intent = Intent(this@AccountSettingsActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun progresPhotoVisible() {
        progressBarPhoto.visibility = View.VISIBLE
    }

    private fun progresPhotoInVisible() {
        progressBarPhoto.visibility = View.INVISIBLE
    }


}
