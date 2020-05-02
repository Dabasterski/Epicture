package com.example.axelm.epicture

import android.app.SearchManager
import android.content.Intent
import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class SearchResult : AppCompatActivity() {

    private val TAG = "SEARCH_ACTIVITY"
    private val CLIENT_ID = "9931a029cdb6487"
    private var ACCESS_TOKEN : String? = null

    //lateinit var httpClient: OkHttpClient

    class Photo {
        internal var id:String? = null
        internal var title:String? = null
    }

    private class PhotoVH internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var photo: ImageView? = null
        internal var title: TextView? = null
    }

    internal val photos: MutableList<Photo>  = ArrayList()

    lateinit var rv: RecyclerView

    private fun render(photos: List<Photo>) {
        rv = findViewById<RecyclerView>(R.id.rv_of_photos)
        rv.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val query = intent.getStringExtra("Query")
        supportActionBar!!.title = "Search Results $query"
        getSearchResults(query)
        }

    private fun getSearchResults(query : String) {
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .addHeader("Authorization", "Client-ID $CLIENT_ID")
            .url("https://api.imgur.com/3/gallery/search/?q=$query")
            .get()
            .build()

        System.out.println("search request built")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
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
                        holder.title?.text = photos[position].title
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
}
