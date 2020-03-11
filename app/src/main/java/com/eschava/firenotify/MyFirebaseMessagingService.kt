package com.eschava.firenotify

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Icon
import android.util.Log
import android.webkit.URLUtil
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.function.Consumer

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val dataJSON = JSONObject(remoteMessage.data)

        Log.d("Message", dataJSON.toString())

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var id = (353..37930).random()
        val group = if (dataJSON.has("group")) dataJSON.getString("group") else ""

        if (dataJSON.has("id")) {
            id = try {
                dataJSON.getInt("id")
            } catch (e: JSONException) {
                dataJSON.getString("id").hashCode()
            }

            if (getBoolean(dataJSON,"dismiss")) {
                notificationManager.cancel(id)
                return
            }
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val silent = getBoolean(dataJSON, "silent")
        val channelId = if (silent) "silent" else "default"
        val channelName = if (silent) "Silent" else "Default"
        val importance = if (silent) NotificationManager.IMPORTANCE_DEFAULT else NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(channelId, channelName, importance)
        if (silent)
            channel.setSound(null, null)

        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = Notification.Builder(this, channelId)
                .setSmallIcon(R.drawable.notification_icon_vector)
                .setLargeIcon(Icon.createWithResource(this, R.drawable.application_icon))

        if (group.isNotEmpty())
            notificationBuilder.setGroup(group)

        applyString(dataJSON, "text", Consumer { text ->
            notificationBuilder.setStyle(Notification.BigTextStyle().bigText(text))
            notificationBuilder.setContentText(text)
        })

        applyString(dataJSON, "title", Consumer { title -> notificationBuilder.setContentTitle(title) })
        applyString(dataJSON, "subText", Consumer { subText -> notificationBuilder.setSubText(subText) })
        applyInteger(dataJSON, "number", Consumer { number -> notificationBuilder.setNumber(number) })
        applyLong(dataJSON, "time", System.currentTimeMillis(), Consumer { time -> notificationBuilder.setWhen(time) })
        applyBoolean(dataJSON, "showTime", true, Consumer { showTime -> notificationBuilder.setShowWhen(showTime) })
        applyBoolean(dataJSON, "autoCancel", true, Consumer { autoCancel -> notificationBuilder.setAutoCancel(autoCancel) })
        applyBoolean(dataJSON, "onlyOnce", Consumer { onlyOnce -> notificationBuilder.setOnlyAlertOnce(onlyOnce) })
        applyBoolean(dataJSON, "local", Consumer { local -> notificationBuilder.setLocalOnly(local) })
        applyString(dataJSON, "color", Consumer { color -> notificationBuilder.setColor(Color.parseColor(color)) })
        applyBoolean(dataJSON, "colorized", Consumer { colorized -> notificationBuilder.setColorized(colorized) })
        applyBoolean(dataJSON, "persistent", Consumer { persistent -> notificationBuilder.setOngoing(persistent) })

        applyString(dataJSON, "image", Consumer { image ->
            if (URLUtil.isValidUrl(image)) {
                val imageBmp = getBitmapFromURL(image)
                if (imageBmp != null) {
                    notificationBuilder.setStyle(Notification.BigPictureStyle().bigPicture(imageBmp).bigLargeIcon(Icon.createWithResource(this, R.drawable.blank_icon)))
                    notificationBuilder.setLargeIcon(imageBmp)
                }
            }
        })

        applyString(dataJSON, "icon", Consumer { icon ->
            if (URLUtil.isValidUrl(icon)) {
                val iconBmp = getBitmapFromURL(icon)
                if (iconBmp != null) {
                    notificationBuilder.setSmallIcon(Icon.createWithBitmap(iconBmp))
                }
            }
        })

        applyString(dataJSON, "actions", Consumer { actionsStr ->
            val actions = JSONArray(actionsStr)

            for (i in 0 until 3) {
                try {
                    val actionJSON = actions.getJSONObject(i)
                    val title = actionJSON.getString("title")
                    val action = actionJSON.getString("action")
                    val broadcastIntent = Intent(this, ResponseReceiver::class.java)
                    broadcastIntent.putExtra("id", id)
                    broadcastIntent.putExtra("action", action)
                    val actionIntent = PendingIntent.getBroadcast(this, (353..37930).random(), broadcastIntent, 0)
                    val notificationAction = Notification.Action.Builder(Icon.createWithResource(this, R.drawable.application_icon), title, actionIntent).build()
                    notificationBuilder.addAction(notificationAction)

                } catch (e: JSONException) {
                    Log.d("Exception", e.toString())
                }
            }
        })

        val notification = notificationBuilder.build()
        notificationManager.notify(id /* ID of notification */, notification)

        if (group.isNotEmpty()) {
            val summaryNotification = Notification.Builder(this, channelId)
//                        .setContentTitle("Your summary message")
                    .setSmallIcon(R.drawable.notification_icon_vector)
//                .setLargeIcon(largeIcon)
                    .setStyle(Notification.InboxStyle()
                            .addLine("Details about your first notification")
                            .addLine("Details about your second notification")
                            .setBigContentTitle("5 new notifications"))
                    .setGroup(group)
                    .setGroupSummary(true)
                    .build()

            notificationManager.notify(group.hashCode(), summaryNotification)
        }
    }

    private fun applyString(json: JSONObject, attribute: String, consumer: Consumer<String>) {
        if (json.has(attribute)) {
            try {
                val value = json.getString(attribute)
                consumer.accept(value)
            } catch (e: JSONException) {
                Log.d("Exception", e.toString())
            }
        }
    }

    private fun applyBoolean(json: JSONObject, attribute: String, consumer: Consumer<Boolean>): Boolean {
        if (json.has(attribute)) {
            try {
                val value = json.getBoolean(attribute)
                consumer.accept(value)
                return true
            } catch (e: JSONException) {
                Log.d("Exception", e.toString())
            }
        }
        return false
    }

    @Suppress("SameParameterValue")
    private fun applyBoolean(json: JSONObject, attribute: String, defaultValue: Boolean, consumer: Consumer<Boolean>) {
        if (!applyBoolean(json, attribute, consumer))
            consumer.accept(defaultValue)
    }

    @Suppress("SameParameterValue")
    private fun applyInteger(json: JSONObject, attribute: String, consumer: Consumer<Int>) {
        if (json.has(attribute)) {
            try {
                val value = json.getInt(attribute)
                consumer.accept(value)
            } catch (e: JSONException) {
                Log.d("Exception", e.toString())
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun applyLong(json: JSONObject, attribute: String, defaultValue: Long, consumer: Consumer<Long>) {
        if (json.has(attribute)) {
            try {
                val value = json.getLong(attribute)
                consumer.accept(value)
                return
            } catch (e: JSONException) {
                Log.d("Exception", e.toString())
            }
        }

        consumer.accept(defaultValue)
    }

    private fun getBoolean(json: JSONObject, attribute: String): Boolean {
        if (json.has(attribute)) {
            try {
                return json.getBoolean(attribute)
            } catch (e: JSONException) {
                Log.d("Exception", e.toString())
            }
        }

        return false
    }

    private fun getBitmapFromURL(image_url: String): Bitmap? {
        return try {
            val url = URL(image_url)
            val connection = url.openConnection()
            connection.doInput = true
            connection.connect()
            val input = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            null
        }
    }
}


