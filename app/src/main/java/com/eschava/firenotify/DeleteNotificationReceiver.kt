package com.eschava.firenotify

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.service.notification.StatusBarNotification

class DeleteNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("Notification", "Received delete notification intent " + intent?.toUri(0))
//        val group = intent?.getStringExtra("group") // group is incorrect for some reason

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        deleteOrphanedGroups(notificationManager)
    }

    companion object {
        fun deleteOrphanedGroups(notificationManager: NotificationManager) {
            val groupCountMap: MutableMap<String?, Int?> = HashMap()
            for (notification: StatusBarNotification in notificationManager.activeNotifications) {
                if (notification.isGroup) {
                    var count = groupCountMap[notification.groupKey]
                    if (count == null) count = 0
                    groupCountMap[notification.groupKey] = count + 1
                }
            }

            for (entry: Map.Entry<String?, Int?> in groupCountMap.entries) {
                if (entry.value == 1) {
                    val groupKey = entry.key

                    for (notification: StatusBarNotification in notificationManager.activeNotifications) {
                        if (notification.isGroup && notification.groupKey == groupKey) {
                            notificationManager.cancel(notification.id)
                            Log.d("OrphanedNotification", "delete orphaned notification for group $groupKey")
                        }
                    }
                }
            }
        }
    }
}