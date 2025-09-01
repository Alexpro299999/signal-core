package com.x_xsan.signalcore

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn != null) {
            sbn.notification.extras.getString("android.title")
            val packageName = sbn.packageName
            val title = sbn.notification.extras.getString("android.title")

            Log.d("NotificationListener", "Notification Posted! Package: $packageName, Title: $title")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}