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

                    // Log the intercepted notification securely in local Room Database via Repository
                    repo.logNotification(
                        packageName = packageName,
                        appName = appName,
                        title = title,
                        body = text,
                        category = category,
                        wasBatched = batchingEnabled || focusActive
                    )
                    repo.saveSetting("latest_blocked_notification", "$appName: $title - $text")
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
