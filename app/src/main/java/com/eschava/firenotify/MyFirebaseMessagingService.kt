package com.eschava.firenotify

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.webkit.URLUtil
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.function.Consumer
import kotlin.math.min

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val dataJSON = JSONObject(remoteMessage.data as Map<*, *>)

        Log.d("Message", dataJSON.toString())

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var id = (353..37930).random()
        val group = getString(dataJSON, "group", "")

        if (dataJSON.has("id")) {
            id = try {
                dataJSON.getInt("id")
            } catch (e: JSONException) {
                dataJSON.getString("id").hashCode()
            }

            if (getBoolean(dataJSON,"dismiss")) {
                notificationManager.cancel(id)
                DeleteNotificationReceiver.deleteOrphanedGroups(notificationManager)
                return
            }

            // if token is specified - ignore messages having outdated tokens
            if (dataJSON.has("token") && dataJSON.getString("token").isNotBlank()) {
                try {
                    val lastIdTokenPreferences = applicationContext.getSharedPreferences("last_id_token", Context.MODE_PRIVATE)
                    val token = dataJSON.getLong("token")

                    val key = "" + id
                    val prevToken = lastIdTokenPreferences.getLong(key, -1)
                    if (prevToken > token) {
                        Log.d("Outdated token", "Skipping message because its token less than last of $prevToken")
                        return
                    }

                    val editor = lastIdTokenPreferences.edit()
                    editor.putLong(key, token)
                    editor.apply()
                } catch (e: Exception) {
                    Log.e("Exception", e.message, e)
                }
            }
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val silent = getBoolean(dataJSON, "silent")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

            if (group!!.isNotEmpty()) {
                notificationBuilder.setGroup(group)

                val deleteIntent = Intent(this, DeleteNotificationReceiver::class.java)
                deleteIntent.putExtra("group", group)
                val pendingDeleteIntent = PendingIntent.getBroadcast(this, (353..37930).random(), deleteIntent, 0)
                notificationBuilder.setDeleteIntent(pendingDeleteIntent)
            }

            applyString(dataJSON, "text", Consumer { text ->
                notificationBuilder.style = Notification.BigTextStyle().bigText(text)
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
                        notificationBuilder.style = Notification.BigPictureStyle().bigPicture(imageBmp).bigLargeIcon(Icon.createWithResource(this, R.drawable.blank_icon))
                        //notificationBuilder.setLargeIcon(imageBmp)
                    }
                }
            })

            applyString(dataJSON, "icon", Consumer { icon ->
                if (URLUtil.isValidUrl(icon)) {
                    val iconBmp = getBitmapFromURL(icon)
                    if (iconBmp != null) {
                        notificationBuilder.setLargeIcon(Icon.createWithBitmap(iconBmp))
                    }
                }
            })

            val commonTo = getString(dataJSON, "to", null)
            applyString(dataJSON, "actions", Consumer { actionsStr ->
                val actions = JSONArray(actionsStr)

                for (i in 0 until min(3, actions.length())) {
                    try {
                        val actionJSON = actions.getJSONObject(i)
                        val title = actionJSON.getString("title")
                        val data = if (actionJSON.has("data")) actionJSON.getJSONObject("data").toString() else null
                        val to = getString(actionJSON, "to", commonTo)
                        val dismiss = getBoolean(actionJSON, "dismiss")
                        val reply = getBoolean(actionJSON, "reply")
                        val replyText = getString(actionJSON, "replyText", "Text")
                        val url = getString(actionJSON, "url", null)

                        val broadcastIntent = Intent(this, ResponseReceiver::class.java)
                        broadcastIntent.putExtra("id", id)
                        broadcastIntent.putExtra("data", data)
                        broadcastIntent.putExtra("to", to)
                        broadcastIntent.putExtra("dismiss", dismiss)
                        broadcastIntent.putExtra("reply", reply)
                        broadcastIntent.putExtra("url", url)

                        val actionIntent = PendingIntent.getBroadcast(this, (353..37930).random(), broadcastIntent, 0)
                        val notificationActionBuilder = Notification.Action.Builder(Icon.createWithResource(this, R.drawable.application_icon), title, actionIntent)
                        if (reply)
                            notificationActionBuilder.addRemoteInput(RemoteInput.Builder("reply").setLabel(replyText).build())
                        notificationBuilder.addAction(notificationActionBuilder.build())
                    } catch (e: JSONException) {
                        Log.e("Exception", e.message, e)
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
        } else { // VERSION.SDK_INT < O
            val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_icon_vector)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.application_icon))

            if (silent)
                notificationBuilder.setNotificationSilent()

            if (group!!.isNotEmpty()) {
                notificationBuilder.setGroup(group)

                val deleteIntent = Intent(this, DeleteNotificationReceiver::class.java)
                deleteIntent.putExtra("group", group)
                val pendingDeleteIntent = PendingIntent.getBroadcast(this, (353..37930).random(), deleteIntent, 0)
                notificationBuilder.setDeleteIntent(pendingDeleteIntent)
            }

            applyString(dataJSON, "text", Consumer { text ->
                notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
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
            applyString(dataJSON, "color", Consumer { color -> notificationBuilder.color = Color.parseColor(color) })
            applyBoolean(dataJSON, "colorized", Consumer { colorized -> notificationBuilder.setColorized(colorized) })
            applyBoolean(dataJSON, "persistent", Consumer { persistent -> notificationBuilder.setOngoing(persistent) })

            applyString(dataJSON, "image", Consumer { image ->
                if (URLUtil.isValidUrl(image)) {
                    val imageBmp = getBitmapFromURL(image)
                    if (imageBmp != null) {
                        notificationBuilder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(imageBmp).bigLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.blank_icon)))
                        //notificationBuilder.setLargeIcon(imageBmp)
                    }
                }
            })

            applyString(dataJSON, "icon", Consumer { icon ->
                if (URLUtil.isValidUrl(icon)) {
                    val iconBmp = getBitmapFromURL(icon)
                    if (iconBmp != null) {
                        notificationBuilder.setLargeIcon(iconBmp)
                    }
                }
            })

            val commonTo = getString(dataJSON, "to", null)
            applyString(dataJSON, "actions", Consumer { actionsStr ->
                val actions = JSONArray(actionsStr)

                for (i in 0 until min(3, actions.length())) {
                    try {
                        val actionJSON = actions.getJSONObject(i)
                        val title = actionJSON.getString("title")
                        val data = if (actionJSON.has("data")) actionJSON.getJSONObject("data").toString() else null
                        val to = getString(actionJSON, "to", commonTo)
                        val dismiss = getBoolean(actionJSON, "dismiss")
                        val reply = getBoolean(actionJSON, "reply")
                        val replyText = getString(actionJSON, "replyText", "Text")
                        val url = getString(actionJSON, "url", null)

                        val broadcastIntent = Intent(this, ResponseReceiver::class.java)
                        broadcastIntent.putExtra("id", id)
                        broadcastIntent.putExtra("data", data)
                        broadcastIntent.putExtra("to", to)
                        broadcastIntent.putExtra("dismiss", dismiss)
                        broadcastIntent.putExtra("reply", reply)
                        broadcastIntent.putExtra("url", url)

                        val actionIntent = PendingIntent.getBroadcast(this, (353..37930).random(), broadcastIntent, 0)
                        val notificationActionBuilder = NotificationCompat.Action.Builder(R.drawable.application_icon, title, actionIntent)
                        if (reply)
                            notificationActionBuilder.addRemoteInput(androidx.core.app.RemoteInput.Builder("reply").setLabel(replyText).build())
                        notificationBuilder.addAction(notificationActionBuilder.build())
                    } catch (e: JSONException) {
                        Log.e("Exception", e.message, e)
                    }
                }
            })

            val notification = notificationBuilder.build()
            notificationManager.notify(id /* ID of notification */, notification)

            if (group.isNotEmpty()) {
                val summaryNotification = NotificationCompat.Builder(this)
//                        .setContentTitle("Your summary message")
                        .setSmallIcon(R.drawable.notification_icon_vector)
//                .setLargeIcon(largeIcon)
                        .setStyle(NotificationCompat.InboxStyle()
                                .addLine("Details about your first notification")
                                .addLine("Details about your second notification")
                                .setBigContentTitle("5 new notifications"))
                        .setGroup(group)
                        .setGroupSummary(true)

                if (silent)
                    summaryNotification.setNotificationSilent()

                notificationManager.notify(group.hashCode(), summaryNotification.build())
            }
        }
    }

    private fun getString(json: JSONObject, attribute: String, defaultValue: String?): String? {
        if (json.has(attribute)) {
            try {
                return json.getString(attribute)
            } catch (e: JSONException) {
                Log.e("Exception", e.message, e)
            }
        }

        return defaultValue
    }

    private fun applyString(json: JSONObject, attribute: String, consumer: Consumer<String>) {
        if (json.has(attribute)) {
            try {
                val value = json.getString(attribute)
                consumer.accept(value)
            } catch (e: JSONException) {
                Log.e("Exception", e.message, e)
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
                Log.e("Exception", e.message, e)
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
                Log.e("Exception", e.message, e)
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
                Log.e("Exception", e.message, e)
            }
        }

        consumer.accept(defaultValue)
    }

    private fun getBoolean(json: JSONObject, attribute: String): Boolean {
        if (json.has(attribute)) {
            try {
                return json.getBoolean(attribute)
            } catch (e: JSONException) {
                Log.e("Exception", e.message, e)
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
            Log.e("Exception", e.message, e)
            null
        }
    }
}


