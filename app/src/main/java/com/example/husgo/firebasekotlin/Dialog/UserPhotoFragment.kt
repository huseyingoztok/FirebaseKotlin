package com.example.husgo.firebasekotlin.Dialog


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.husgo.firebasekotlin.R


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
interface onGetPhoto{
    fun getCamera(bitmap:Bitmap?)
    fun getGallery(imageUri:Uri?)
}
class UserPhotoFragment : DialogFragment() {
    lateinit var tvCamera:TextView
    lateinit var tvGallery:TextView
    lateinit var getPhotoListener: onGetPhoto
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var v=inflater.inflate(R.layout.fragment_user_photo, container, false)
        tvCamera=v.findViewById(R.id.tvTakeCamera)
        tvGallery=v.findViewById(R.id.tvSelectGalery)

        tvGallery.setOnClickListener{
            var intent=Intent(Intent.ACTION_GET_CONTENT)
            intent.type="image/*"
            startActivityForResult(intent,100)
        }

        tvCamera.setOnClickListener {
            var intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent,200)
        }

        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Galeriden
        if (requestCode==100&& resultCode==Activity.RESULT_OK && data!=null){
            getPhotoListener.getGallery(data.data)

        }
        //Kameradan
        else if(requestCode==200&&resultCode==Activity.RESULT_OK&& data!=null){
            getPhotoListener.getCamera(data.extras.get("data") as Bitmap)
        }
        dialog.dismiss()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        getPhotoListener=activity as onGetPhoto
    }

}
