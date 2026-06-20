package com.example.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.LauncherApplication
import com.example.data.local.NotificationLogEntity
import kotlinx.coroutines.*

class LauncherNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName ?: return
        if (packageName == this.packageName) return // Ignore notifications from our own app

        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        val app = application as? LauncherApplication ?: return
        val repo = app.container.repository

        serviceScope.launch {
            try {
                // Check if notification filtering or batching is active
                val filteringEnabled = repo.getSetting("notification_filtering_enabled")?.toBoolean() ?: false
                val batchingEnabled = repo.getSetting("notification_batching_enabled")?.toBoolean() ?: false
                val focusActive = repo.getSetting("focus_mode_active")?.toBoolean() ?: false

                val pm = packageManager
                val appName = try {
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    packageName.substringAfterLast('.')
                }

                // Auto-categorize based on package properties
                val category = when {
                    packageName.contains("chat") || packageName.contains("whatsapp") || packageName.contains("messenger") || packageName.contains("discord") || packageName.contains("telegram") -> "Social"
                    packageName.contains("email") || packageName.contains("gmail") || packageName.contains("slack") || packageName.contains("teams") -> "Work"
                    packageName.contains("sms") || packageName.contains("telephony") || packageName.contains("phone") -> "Personal"
                    else -> "General"
                }

                if (filteringEnabled || batchingEnabled || focusActive) {
                    // Suppress (Dismiss) the notification from status bar
                    try {
                        cancelNotification(sbn.key)
                    } catch (e: SecurityException) {
                        Log.e("NotificationListener", "Missing permission to dismiss notification: ${e.message}")
                    }

                    // Log the intercepted notification in local Room Database
                    val log = NotificationLogEntity(
                        packageName = packageName,
                        appName = appName,
                        title = title,
                        body = text,
                        timestamp = System.currentTimeMillis(),
                        category = category,
                        wasBatched = batchingEnabled || focusActive
                    )
                    // Insert to database directly using repo method (which we will define, or save it dynamically)
                    repo.saveSetting("latest_blocked_notification", "$appName: $title - $text")
                    
                    // We will append it to notification history logs in the dao
                    // Let's call our repo's insert helper or write directly to database dao
                    app.container.repository.let {
                        // We will dynamically expose simple logging for Notification digests
                        repo.saveSetting("notif_${System.currentTimeMillis()}", "$packageName|$appName|$title|$text|$category")
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationListener", "Error tracking incoming notification: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy();
        serviceScope.cancel()
    }
}
