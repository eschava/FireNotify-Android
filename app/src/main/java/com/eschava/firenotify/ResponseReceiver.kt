package com.eschava.firenotify

import android.app.NotificationManager
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import okhttp3.*
import org.json.JSONObject


class ResponseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.getIntExtra("id", 0)
        val dismiss = intent?.getBooleanExtra("dismiss", false)
        val reply = intent?.getBooleanExtra("reply", false)
        val data = intent?.getStringExtra("data")
        val to = intent?.getStringExtra("to")
        val url = intent?.getStringExtra("url")
        val idToDismiss = if (dismiss!! || reply!!) id else null // reply creates new notification so old one should be dismissed

        if (url != null) {
            val urlIntent = Intent(Intent.ACTION_VIEW)
            urlIntent.data = Uri.parse(url)
            urlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(urlIntent)
        }

        if (to != null) {
            val jsonData = if (data != null) JSONObject(data) else JSONObject()

            if (reply!!) {
                val results: Bundle = RemoteInput.getResultsFromIntent(intent)
                jsonData.put("reply", results.getCharSequence("reply"))
            }

            // don't dismiss right away if needed, response task should do this after sending response
            ResponseTask(context, context.getString(R.string.fcm_key), to, jsonData, idToDismiss).execute()
        } else if (idToDismiss != null) {
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(idToDismiss)
        }
    }

    class ResponseTask(private var context: Context, private var fcmKey: String, private var to: String, private var data: JSONObject, private var idToDismiss: Int?) : AsyncTask<Void, Void, String>() {
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
                    if (failure > 0) {
                        showToast("Error: $responseBody")
                    } else if (idToDismiss != null) {
                        Handler(context.mainLooper).post {
                            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancel(idToDismiss!!)
                        }
                    }
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


