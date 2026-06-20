package com.example.domain.repository

import android.content.Context
import com.example.data.local.NotificationLogEntity
import com.example.domain.model.AppInfo
import com.example.domain.model.AppUsage
import com.example.domain.model.FocusSession
import com.example.domain.model.LaunchLog
import kotlinx.coroutines.flow.Flow

interface LauncherRepository {

    // --- App Fetching & Operations ---
    fun getInstalledAppsFlow(context: Context): Flow<List<AppInfo>>
    suspend fun getAppConfig(packageName: String): AppInfo?
    suspend fun setFavorite(packageName: String, isFavorite: Boolean)
    suspend fun setWhitelisted(packageName: String, isWhitelisted: Boolean)
    suspend fun setHidden(packageName: String, isHidden: Boolean)
    suspend fun setOpeningDelay(packageName: String, delaySeconds: Int)
    suspend fun renameApp(packageName: String, customLabel: String?)
    suspend fun setTimeLimit(packageName: String, limitMinutes: Int)
    
    // --- Launch Tracking ---
    fun observeLaunchLogs(): Flow<List<LaunchLog>>
    suspend fun logLaunch(packageName: String, appLabel: String)
    suspend fun clearLaunchLogs()

    // --- Focus Deep Work ---
    fun observeFocusSessions(): Flow<List<FocusSession>>
    suspend fun logFocusSession(sessionName: String, durationMinutes: Int, completed: Boolean)

    // --- Settings Persistence ---
    fun observeSettings(): Flow<Map<String, String>>
    suspend fun getSetting(key: String): String?
    suspend fun saveSetting(key: String, value: String)

    // --- Notification Logs Management ---
    fun observeNotificationLogs(): Flow<List<NotificationLogEntity>>
    suspend fun logNotification(packageName: String, appName: String, title: String, body: String, category: String, wasBatched: Boolean)
    suspend fun clearNotificationLogs()

    // --- Analytics API (Official UsageStatsManager Adapter) ---
    fun getRealAppUsage(context: Context, periodDays: Int): List<AppUsage>
    fun hasUsagePermission(context: Context): Boolean
}
