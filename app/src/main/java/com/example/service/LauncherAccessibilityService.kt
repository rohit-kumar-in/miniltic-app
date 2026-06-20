package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.LauncherApplication
import kotlinx.coroutines.*

class LauncherAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        
        val targetPackage = event.packageName?.toString() ?: return
        val currentPackage = packageName // our launcher's package

        if (targetPackage == currentPackage || targetPackage.isEmpty()) return

        // Access the app's database via LauncherRepository
        val app = application as? LauncherApplication ?: return
        val repo = app.container.repository

        serviceScope.launch {
            try {
                // Check if Focus Mode / Deep Work / Blocking schedules are active
                val settings = repo.getSetting("focus_mode_active")?.toBoolean() ?: false
                val blockAllNonWhitelisted = settings || repo.getSetting("block_all_apps")?.toBoolean() ?: false
                
                // Get app-specific config
                val appConfig = repo.getAppConfig(targetPackage)
                val isWhitelisted = appConfig?.isWhitelisted ?: false
                val isHidden = appConfig?.isHidden ?: false

                var shouldBlock = false

                // 1. Check direct blocking conditions
                if (isHidden) {
                    shouldBlock = true
                } else if (blockAllNonWhitelisted && !isWhitelisted) {
                    shouldBlock = true
                }

                // 2. Check general schedules (e.g., Sleep mode or Work boundaries)
                val sleepModeActive = repo.getSetting("sleep_mode_active")?.toBoolean() ?: false
                val workModeActive = repo.getSetting("work_mode_active")?.toBoolean() ?: false
                if ((sleepModeActive || workModeActive) && !isWhitelisted) {
                    shouldBlock = true
                }

                // 3. Check time limit
                val limitMinutesStr = repo.getSetting("limit_$targetPackage")
                if (limitMinutesStr != null && limitMinutesStr.toIntOrNull() != null) {
                    val limitMinutes = limitMinutesStr.toInt()
                    // Fetch usage for today
                    val usages = repo.getRealAppUsage(applicationContext, 1)
                    val targetUsage = usages.find { it.packageName == targetPackage }
                    val currentUsageMinutes = (targetUsage?.screenTimeMs ?: 0L) / 60000L
                    if (currentUsageMinutes >= limitMinutes && !isWhitelisted) {
                        shouldBlock = true
                    }
                }

                if (shouldBlock) {
                    withContext(Dispatchers.Main) {
                        redirectHome()
                    }
                }
            } catch (e: Exception) {
                Log.e("AccessibilityService", "Error filtering window transition event: ${e.message}")
            }
        }
    }

    private fun redirectHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }

    override fun onInterrupt() {
        Log.d("AccessibilityService", "Service disrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
