package com.eschava.firenotify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class ResponseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
//        val id = intent?.getIntExtra("id", 0)
//        val action = intent?.getStringExtra("action")
//
//        val notificationManager: NotificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.cancel(id!!)
//
//        val dataJSON = JSONObject()
//        dataJSON.put("action", action)
//        dataJSON.put("type", "clicked")
//        dataJSON.put("token", FirebaseInstanceId.getInstance().token)
    }
}


