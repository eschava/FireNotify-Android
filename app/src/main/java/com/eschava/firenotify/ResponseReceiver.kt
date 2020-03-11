package com.eschava.firenotify

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import android.widget.Toast
import okhttp3.*
import org.json.JSONObject


class ResponseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.getIntExtra("id", 0)
        val dismiss = intent?.getBooleanExtra("dismiss", false)
        val data = intent?.getStringExtra("data")
        val to = intent?.getStringExtra("to")

        if (dismiss!!) {
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(id!!)
        }

        if (to != null && data != null) {
            ResponseTask(context, context.getString(R.string.fcm_key), to, JSONObject(data)).execute()
        }
    }

    class ResponseTask(private var context: Context, private var fcmKey: String, private var to: String, private var data: JSONObject) : AsyncTask<Void, Void, String>() {
        private val jsonMediaType: MediaType = MediaType.parse("application/json; charset=utf-8")!!

        override fun doInBackground(vararg params: Void?): String? {
            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger()

            try {
                val json = JSONObject()
                json.put("data", data)
                json.put("to", to)

                val requestBody = RequestBody.create(jsonMediaType, json.toString())
                val request: Request = Request.Builder()
                        .header("Authorization", "key=$fcmKey")
                        .url("https://fcm.googleapis.com/fcm/send")
                        .post(requestBody)
                        .build()

                val client = OkHttpClient()
                val response: Response = client.newCall(request).execute()
                val responseBody = response.body()!!.string()

                if (!response.isSuccessful) {
                    showToast("""${response.code()} / $responseBody""")
                } else {
                    val jsonResponse = JSONObject(responseBody)
                    val failure = jsonResponse.getInt("failure")
                    if (failure > 0)
                        showToast("Error: $responseBody")
                }

            } catch (e: Exception) {
                showToast(e.message)
                Log.d("Exception", e.toString())
            }
            return null
        }

        private fun showToast(text: String?) {
            Handler(context.mainLooper).post {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            }
        }
    }
}


