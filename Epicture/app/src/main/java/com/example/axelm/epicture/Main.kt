package com.example.axelm.epicture

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.support.v4.app.FragmentActivity
import android.util.Base64
import android.util.Log
import android.view.View
import android.util.Base64.NO_WRAP
//import com.sun.org.apache.xalan.internal.xsltc.compiler.Constants.REDIRECT_URI
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


//import com.sun.org.apache.xalan.internal.xsltc.compiler.Constants.REDIRECT_URI



class Main : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private val AUTH_URL = "https://api.imgur.com/oauth2/authorize?client_id=%s" +
    "&response_type=token&state=%s"

    private val CLIENT_ID = "9931a029cdb6487"

    private val STATE = "MY_RANDOM_STRING_1"

    private val REDIRECT_URI = "http://www.scumgang.com/my_redirect"

    private val ACCESS_TOKEN_URL = "https://api.imgur.com/oauth2/token"

    private val TAG = "MainActivity"

    fun startSignIn(view: View) {
        val url = String.format(AUTH_URL, CLIENT_ID, STATE)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        if (intent != null && intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri!!.getQueryParameter("error") != null) {
                val error = uri.getQueryParameter("error")
                Log.e(TAG, "An error has occurred : " + error!!)
            } else {
                val state = uri.getQueryParameter("state")
                if (state == STATE) {
                    System.out.println(uri)
                    val token = uri.fragment.substringAfter("access_token=").split("&").first()
                    val userName = uri.fragment.substringAfter("account_username=").split("&").first()
                    val intent = Intent(this, LoggedActivity::class.java)
                    intent.putExtra("access_token", token)
                    intent.putExtra("user_name", userName)
                    startActivity(intent)
                }
            }
        }
    }

/*    private fun getAccessToken(code: String) {
        val client = OkHttpClient()
        val authString = "$CLIENT_ID:"
        val encodedAuthString = Base64.encodeToString(
            authString.toByteArray(),
            Base64.NO_WRAP)

        val request = Request.Builder()
            .addHeader("User-Agent", "Sample App")
            .addHeader("Authorization", "Basic $encodedAuthString")
            .url(ACCESS_TOKEN_URL)
            .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                "grant_type=authorization_code&code=" + code +
                        "&redirect_uri=" + REDIRECT_URI))
            .build()

            client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "ERROR: $e")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val json = response.body().string()

                var data: JSONObject? = null
                try {
                    data = JSONObject(json)
                    val accessToken = data.optString("access_token")
                    val refreshToken = data.optString("refresh_token")

                    Log.d(TAG, "Access Token = $accessToken")
                    Log.d(TAG, "Refresh Token = $refreshToken")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }*/
}