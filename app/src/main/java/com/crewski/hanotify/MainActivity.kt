package com.crewski.hanotify

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val token = FirebaseInstanceId.getInstance().token
        ha_url.setText(token)
//        val sharedPref = getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)
//        val url = sharedPref.getString(getString(R.string.home_assistant_url), "")
//        ha_url.setText(url)
//        val api_password = sharedPref.getString(getString(R.string.api_password), "")
//        ha_passwrod.setText(api_password)

//    register_btn.setOnClickListener{
//        var url = ha_url.text.toString()
//        val password = ha_passwrod.text.toString()
//
//        if (!URLUtil.isValidUrl(url)){
//            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
//            return@setOnClickListener
//        }
//        url = url.removeSuffix("/")

//        with (sharedPref.edit()) {
//            putString(getString(R.string.home_assistant_url), url)
//            putString(getString(R.string.api_password), password)
//            commit()
//        }


//        FirebaseInstanceId.getInstance().getToken()
//        FirebaseInstanceId.getInstance().gM


//        val dataJSON = JSONObject()
//        dataJSON.put("token", token)
//        val que = Volley.newRequestQueue(this)
//        val url_suffix = getString(R.string.url_suffix)
//        val req = object : JsonObjectRequest(Request.Method.POST, "$url$url_suffix", dataJSON,
//                Response.Listener { response ->
//                    val resJSON = JSONObject(response.toString())
//                    Toast.makeText(this, resJSON.getString("message"), Toast.LENGTH_SHORT).show()
//
//                }, Response.ErrorListener { error ->  Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
//
//        }) {
//            @Throws(AuthFailureError::class)
//            override fun getHeaders(): Map<String, String> {
//                val headers = HashMap<String, String>()
//                headers.put("Content-Type", "application/json");
//                headers.put("x-ha-access", password)
//                return headers
//            }
//        }
//
//        que.add(req)
//    }


    }



//    fun btnConnectHandler(){
//
//    }
}
