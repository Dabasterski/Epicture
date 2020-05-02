package com.example.axelm.epicture

import android.app.SearchManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import okhttp3.*
import java.io.IOException
import android.content.Intent
import android.graphics.Rect
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject


class LoggedActivity : AppCompatActivity() {

    private val TAG = "LOGGED_ACTIVITY"
    private val CLIENT_ID = "9931a029cdb6487"
    private var ACCESS_TOKEN : String? = null

    lateinit var httpClient: OkHttpClient
    internal val photos: MutableList<Photo>  = ArrayList()

    class Photo {
        internal var id:String? = null
        internal var title:String? = null
    }

    private class PhotoVH internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var photo: ImageView? = null
        internal var title: TextView? = null
    }

    lateinit var rv: RecyclerView

    private fun render(photos: List<Photo>) {
        rv = findViewById<RecyclerView>(R.id.rv_of_photos)
        rv.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logged)
        val intent = intent
        val accountName = intent.getStringExtra("user_name")
        val token = intent.getStringExtra("access_token")
        supportActionBar!!.title = accountName
        System.out.println(token)
        System.out.println(accountName)
        getImages(token)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menus, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }
        val searchmenu = menu.findItem(R.id.search).actionView as SearchView
        searchmenu.queryHint = "Enter Search"
        searchmenu.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val intent = Intent(this@LoggedActivity, SearchResult::class.java)
                intent.putExtra("Query", query)
                startActivity(intent)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
        return true
    }

    private fun getImages(token: String) {
        httpClient = OkHttpClient.Builder().build()
        //val client = OkHttpClient()
        val request = Request.Builder()
            .addHeader("Authorization", "Bearer $token")
            .url("https://api.imgur.com/3/account/me/images") //modify request
            .get()
            .build()
        ACCESS_TOKEN = token
        httpClient.newCall(request).enqueue(object : Callback {
            //client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "ERROR: $e")
            }
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                //System.out.println(json)
                try {
                    val data = JSONObject(response.body()!!.string())
                    val items = data.getJSONArray("data")
                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        val photo = Photo()
                        photo.id = item.getString("id")
                        photo.title = item.getString("title")
                        if (photo.title == "null")
                            photo.title = ""
                        photos.add(photo)
                        /*System.out.println(jsonObj.getJSONObject(i).getString("name"))
                    System.out.println(jsonObj.getJSONObject(i).getString("link"))*/
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                runOnUiThread { render(photos) }

                val adapter = object : RecyclerView.Adapter<PhotoVH>() {

                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoVH {
                        val vh = PhotoVH(layoutInflater.inflate(R.layout.item, null))
                        vh.photo = vh.itemView.findViewById(R.id.photo)
                        vh.title = vh.itemView.findViewById(R.id.title)
                        return vh
                    }

                    override fun onBindViewHolder(holder: PhotoVH, position: Int) {
                        Picasso.get().load("https://i.imgur.com/" + photos[position].id + ".jpg").into(holder.photo)
                        holder.title?.setText(photos[position].title)
                        holder.photo!!.setOnClickListener { showPictureDialog(photos[position].id) }
                    }

                    override fun getItemCount(): Int {return photos.size}
                }
                runOnUiThread{
                    rv.addItemDecoration(object: RecyclerView.ItemDecoration() {
                        override fun getItemOffsets(
                            outRect: Rect,
                            view: View,
                              parent: RecyclerView,
                            state: RecyclerView.State
                        ) {
                            outRect.bottom = 16 //padding texte
                        }

                    })
                    rv.adapter = adapter
                }
            }
        })
    }

    private fun showPictureDialog(id: String?) {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Add to favorites")
        val pictureDialogItems = arrayOf("Yes")
        pictureDialog.setItems(pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> MakeFavorite(id)
            }
        }
        pictureDialog.show()
    }

    private fun MakeFavorite(id: String?) {
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", "toto")
            .build()

        val request = Request.Builder()
            .addHeader("Authorization", "Bearer $ACCESS_TOKEN")
            .url("https://api.imgur.com/3/image/$id/favorite  ")
            .post(requestBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json = response.body()!!.string()
                Log.d("UPLOAD_IMAGE", json)
            }

            @Throws(IOException::class)
            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("UPLOAD_IMAGE", "Error: $e")
            }
        })
    }

    fun startUpload(view: View) {
        val intent = Intent(this@LoggedActivity, UploadImage::class.java)
        System.out.println("Access_token = $ACCESS_TOKEN")
        intent.putExtra("ACCESS_TOKEN", ACCESS_TOKEN)
        startActivity(intent)
    }
}