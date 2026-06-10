package com.example.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.data.NotificationItem
import com.example.data.SyncRepository
import java.util.UUID

class ParentNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val packageName = it.packageName
            val extras = it.notification.extras
            val title = extras.getString("android.title") ?: "No Title"
            val text = extras.getCharSequence("android.text")?.toString() ?: "No Text"
            
            // Filter out system notifications if desired
            if (packageName != "android" && packageName != "com.android.systemui") {
                val item = NotificationItem(
                    id = UUID.randomUUID().toString(),
                    appName = packageName,
                    title = title,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )
                SyncRepository.addNotification(item)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Optionally track removed notifications
    }
}
