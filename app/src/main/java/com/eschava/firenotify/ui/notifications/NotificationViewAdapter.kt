package com.eschava.firenotify.ui.notifications

import android.content.Context
import android.service.notification.StatusBarNotification
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eschava.firenotify.R

class NotificationViewAdapter internal constructor(context: Context) : RecyclerView.Adapter<NotificationViewAdapter.MyViewHolder>() {
    private val mContext: Context = context
    private var notifications: List<StatusBarNotification> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewAdapter.MyViewHolder, position: Int) {
        val notification = notifications[position]
        val extras = notification.notification.extras
        holder.tvNotificationTitle.text = extras.getString(android.app.Notification.EXTRA_TITLE)
        holder.tvNotificationText.text = extras.getString(android.app.Notification.EXTRA_TEXT)
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    fun setNotifications(notifications: List<StatusBarNotification>) {
        this.notifications = notifications
        notifyDataSetChanged()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNotificationTitle: TextView = itemView.findViewById(R.id.notification_title)
        val tvNotificationText: TextView = itemView.findViewById(R.id.notification_text)
    }
}