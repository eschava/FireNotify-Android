package com.eschava.firenotify.ui.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.eschava.firenotify.DeleteNotificationReceiver
import com.eschava.firenotify.R
import kotlinx.android.synthetic.main.fragment_notifications.*


class NotificationsFragment : Fragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel::class.java)
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationViewAdapter = NotificationViewAdapter(context)
        notificationViewAdapter.setNotifications(getNotifications(notificationManager))
        notificationsView.adapter = notificationViewAdapter

        val touchListener = RecyclerTouchListener(activity, notificationsView)
        touchListener
//                .setClickable(object : OnRowClickListener {
//                    override fun onRowClicked(position: Int) {
//                        Toast.makeText(context, taskList.get(position).getName(), Toast.LENGTH_SHORT).show()
//                    }
//
//                    override fun onIndependentViewClicked(independentViewID: Int, position: Int) {}
//                })
                .setSwipeOptionViews(R.id.delete_notification)
                .setSwipeable(R.id.rowFG, R.id.rowBG) { viewID, position ->
                    when (viewID) {
                        R.id.delete_notification -> {
                            val notifications = getNotifications(notificationManager)
                            notificationManager.cancel(notifications[position].id)
                            DeleteNotificationReceiver.deleteOrphanedGroups(notificationManager)

                            notificationViewAdapter.setNotifications(getNotifications(notificationManager))
                        }
        //                        R.id.edit_task -> Toast.makeText(ApplicationProvider.getApplicationContext(), "Edit Not Available", Toast.LENGTH_SHORT).show()
                    }
                }
        notificationsView.addOnItemTouchListener(touchListener)
    }

    private fun getNotifications(notificationManager: NotificationManager) =
            notificationManager.activeNotifications.toList().filter { n -> (n.notification.flags and Notification.FLAG_GROUP_SUMMARY) == 0 }
}