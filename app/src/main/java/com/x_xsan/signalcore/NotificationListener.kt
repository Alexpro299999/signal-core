package com.x_xsan.signalcore

import SignalManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class NotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(applicationContext)
    }
    private val signalManager: SignalManager by lazy {
        SignalManager(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn != null && sbn.packageName == "org.telegram.messenger") {
            val title = sbn.notification.extras.getString("android.title")
            if (title != null) {
                serviceScope.launch {
                    val priorityContacts = userPreferencesRepository.priorityContacts.first()

                    Log.d("NotificationListener", "Checking notification. Title: '$title'. Priority list: $priorityContacts")

                    if (title in priorityContacts) {
                        Log.d("NotificationListener", "!!! MATCH FOUND: $title !!!")
                        signalManager.startSignal()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}