package com.example.axelm.epicture

import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import android.util.Base64
import android.util.Log
import okhttp3.*


class UploadImage : AppCompatActivity() {

    private var img: Bitmap? = null
    private var btn: Button? = null
    private var imageview: ImageView? = null
    private val GALLERY = 1
    private val CAMERA = 2
    private var button: Button? = null
    private val CLIENT_ID = "9931a029cdb6487"
    private var ACCESS_TOKEN : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_image)
        val intent = intent
        ACCESS_TOKEN = intent.getStringExtra("ACCESS_TOKEN")
        System.out.println(ACCESS_TOKEN)
        btn = findViewById<View>(R.id.btn) as Button
        imageview = findViewById<View>(R.id.iv) as ImageView
        button = findViewById<View>(R.id.button) as Button
        button!!.visibility = View.GONE
        btn!!.setOnClickListener { showPictureDialog() }
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallary()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }

    public override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        /* if (resultCode == this.RESULT_CANCELED)
         {
         return
         }*/
        if (requestCode == GALLERY)
        {
            if (data != null)
            {
                val contentURI = data!!.data
                try
                {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    img = bitmap
                    Toast.makeText(this@UploadImage, "Image Selected!", Toast.LENGTH_SHORT).show()

                }
                catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@UploadImage, "Failed!", Toast.LENGTH_SHORT).show()
                }

            }

        }
        else if (requestCode == CAMERA)
        {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            img = thumbnail
            Toast.makeText(this@UploadImage, "Image Selected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (img != null) {
            imageview!!.setImageBitmap(img)
            button!!.visibility = View.VISIBLE
            btn!!.visibility = View.GONE
        }
    }

    fun startUpload(view: View) {
        val stream = ByteArrayOutputStream()
        img?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        img!!.recycle()
        uploadToAPI(byteArray)
    }

    private fun uploadToAPI(byteArray: ByteArray) {
        val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", encoded)
            .build()

        val request = Request.Builder()
            .addHeader("Authorization", "Bearer $ACCESS_TOKEN")
            .addHeader("content-type", "multipart/form-data")
            .url("https://api.imgur.com/3/image")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json = response.body()!!.string()
                Log.d("UPLOAD_IMAGE", json)
                val intent = Intent(this@UploadImage, LoggedActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivityIfNeeded(intent, 0)
            }

            @Throws(IOException::class)
            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("UPLOAD_IMAGE", "Error: $e")
            }
        })
    }
}
