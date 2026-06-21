package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.LauncherApplication
import kotlinx.coroutines.*

class LauncherAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentForegroundApp: String? = null
    private var currentForegroundStartTime: Long = 0L
    private var promptTrackingJob: Job? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        
        val targetPackage = event.packageName?.toString() ?: return
        val currentPackage = packageName // our launcher's package

        if (targetPackage == currentForegroundApp) return

        // Save previous app usage
        val previousApp = currentForegroundApp
        val previousAppTimeMs = if (currentForegroundStartTime > 0) System.currentTimeMillis() - currentForegroundStartTime else 0L

        currentForegroundApp = targetPackage
        currentForegroundStartTime = System.currentTimeMillis()
        
        val app = application as? LauncherApplication ?: return
        val repo = app.container.repository

        // Trigger Life Value Reflection if previous app used for long time
        if (previousApp != null && previousApp != currentPackage && previousAppTimeMs > 5 * 60 * 1000L) { // > 5 minutes
            serviceScope.launch {
                val intent = Intent(applicationContext, com.example.presentation.launcher.LifeValueReflectionActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("package_name", previousApp)
                    putExtra("duration_ms", previousAppTimeMs)
                }
                startActivity(intent)
            }
        }

        if (targetPackage == currentPackage || targetPackage.isEmpty()) {
            promptTrackingJob?.cancel()
            return
        }

        promptTrackingJob?.cancel()
        // Wait for configurable interval to prompt during usage? 
        // The prompt says "Trigger reflection prompts after configurable intervals"
        promptTrackingJob = serviceScope.launch {
            delay(15 * 60 * 1000L) // 15 mins
            if (currentForegroundApp == targetPackage) {
                val intent = Intent(applicationContext, com.example.presentation.launcher.LifeValueReflectionActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("package_name", targetPackage)
                    putExtra("duration_ms", 15 * 60 * 1000L)
                }
                startActivity(intent)
            }
        }

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

                // 2. Check general schedules (Sleep schedule, Work schedule, and Weekend rules)
                if (!isWhitelisted) {
                    val sleepActive = repo.getSetting("sleep_active")?.toBoolean() ?: false
                    if (sleepActive) {
                        val sleepStart = repo.getSetting("sleep_start") ?: "22:00"
                        val sleepEnd = repo.getSetting("sleep_end") ?: "07:00"
                        val nowStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                        if (isTimeBetween(nowStr, sleepStart, sleepEnd)) {
                            shouldBlock = true
                        }
                    }

                    val workActive = repo.getSetting("work_active")?.toBoolean() ?: false
                    if (workActive) {
                        val workStart = repo.getSetting("work_start") ?: "09:00"
                        val workEnd = repo.getSetting("work_end") ?: "17:00"
                        val nowStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                        if (isTimeBetween(nowStr, workStart, workEnd)) {
                            shouldBlock = true
                        }
                    }

                    val weekendActive = repo.getSetting("weekend_active")?.toBoolean() ?: false
                    if (weekendActive) {
                        val dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
                        val isWeekend = dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY
                        if (isWeekend) {
                            shouldBlock = true
                        }
                    }
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

    private fun isTimeBetween(nowStr: String, startStr: String, endStr: String): Boolean {
        try {
            val nowParts = nowStr.split(":")
            val nowMins = (nowParts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (nowParts.getOrNull(1)?.toIntOrNull() ?: 0)
            
            val startParts = startStr.split(":")
            val startMins = (startParts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (startParts.getOrNull(1)?.toIntOrNull() ?: 0)
            
            val endParts = endStr.split(":")
            val endMins = (endParts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (endParts.getOrNull(1)?.toIntOrNull() ?: 0)
            
            return if (startMins <= endMins) {
                nowMins in startMins..endMins
            } else {
                nowMins >= startMins || nowMins <= endMins
            }
        } catch (e: Exception) {
            return false
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
