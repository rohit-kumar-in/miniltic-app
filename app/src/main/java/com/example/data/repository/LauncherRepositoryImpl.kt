package com.example.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import com.example.data.local.*
import com.example.domain.model.AppInfo
import com.example.domain.model.AppUsage
import com.example.domain.model.FocusSession
import com.example.domain.model.LaunchLog
import com.example.domain.repository.LauncherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

class LauncherRepositoryImpl(
    private val dao: LauncherDao
) : LauncherRepository {

    override fun getInstalledAppsFlow(context: Context): Flow<List<AppInfo>> {
        val packageManager = context.packageManager
        
        // Flow that yields the static packages we resolve
        val pmAppsFlow = flow {
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val launchables = packageManager.queryIntentActivities(intent, 0)
            val pmApps = launchables.mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(packageManager).toString()
                if (packageName == context.packageName) null else packageName to label
            }.distinctBy { it.first }
            emit(pmApps)
        }

        // Live config settings flow from database
        val configsFlow = dao.getAllAppConfigs()

        // Combine package descriptions with database configurations
        return combine(pmAppsFlow, configsFlow) { pmApps, configs ->
            val configMap = configs.associateBy { it.packageName }
            pmApps.map { (packageName, label) ->
                val config = configMap[packageName]
                AppInfo(
                    packageName = packageName,
                    label = label,
                    isFavorite = config?.isFavorite ?: false,
                    isWhitelisted = config?.isWhitelisted ?: false,
                    isHidden = config?.isHidden ?: false,
                    openingDelaySeconds = config?.openingDelaySeconds ?: 0
                )
            }.sortedBy { it.label.lowercase() }
        }
    }

    override suspend fun getAppConfig(packageName: String): AppInfo? {
        val config = dao.getAppConfig(packageName) ?: return null
        return AppInfo(
            packageName = config.packageName,
            label = "", // label will be populated dynamically or locally
            isFavorite = config.isFavorite,
            isWhitelisted = config.isWhitelisted,
            isHidden = config.isHidden,
            openingDelaySeconds = config.openingDelaySeconds
        )
    }

    override suspend fun setFavorite(packageName: String, isFavorite: Boolean) {
        val existing = dao.getAppConfig(packageName)
        val config = existing?.copy(isFavorite = isFavorite) ?: AppConfigEntity(packageName, isFavorite = isFavorite)
        dao.insertAppConfig(config)
    }

    override suspend fun setWhitelisted(packageName: String, isWhitelisted: Boolean) {
        val existing = dao.getAppConfig(packageName)
        val config = existing?.copy(isWhitelisted = isWhitelisted) ?: AppConfigEntity(packageName, isWhitelisted = isWhitelisted)
        dao.insertAppConfig(config)
    }

    override suspend fun setHidden(packageName: String, isHidden: Boolean) {
        val existing = dao.getAppConfig(packageName)
        val config = existing?.copy(isHidden = isHidden) ?: AppConfigEntity(packageName, isHidden = isHidden)
        dao.insertAppConfig(config)
    }

    override suspend fun setOpeningDelay(packageName: String, delaySeconds: Int) {
        val existing = dao.getAppConfig(packageName)
        val config = existing?.copy(openingDelaySeconds = delaySeconds) ?: AppConfigEntity(packageName, openingDelaySeconds = delaySeconds)
        dao.insertAppConfig(config)
    }

    // --- Launch Tracking ---
    override fun observeLaunchLogs(): Flow<List<LaunchLog>> {
        return dao.getAllLaunchLogs().map { entities ->
            entities.map { LaunchLog(it.id, it.packageName, it.appName, it.timestamp, it.durationMs) }
        }
    }

    override suspend fun logLaunch(packageName: String, appLabel: String) {
        val log = AppLaunchLogEntity(
            packageName = packageName,
            appName = appLabel,
            timestamp = System.currentTimeMillis()
        )
        dao.insertLaunchLog(log)
    }

    override suspend fun clearLaunchLogs() {
        dao.clearLaunchLogs()
    }

    // --- Focus Deep Work ---
    override fun observeFocusSessions(): Flow<List<FocusSession>> {
        return dao.getAllFocusSessions().map { entities ->
            entities.map { FocusSession(it.id, it.sessionName, it.durationMinutes, it.timestamp, it.completed) }
        }
    }

    override suspend fun logFocusSession(sessionName: String, durationMinutes: Int, completed: Boolean) {
        val session = FocusSessionEntity(
            sessionName = sessionName,
            durationMinutes = durationMinutes,
            timestamp = System.currentTimeMillis(),
            completed = completed
        )
        dao.insertFocusSession(session)
    }

    // --- Settings Persistence ---
    override fun observeSettings(): Flow<Map<String, String>> {
        return dao.getAllSettings().map { list ->
            list.associate { it.key to it.value }
        }
    }

    override suspend fun getSetting(key: String): String? {
        return dao.getSetting(key)?.value
    }

    override suspend fun saveSetting(key: String, value: String) {
        dao.insertSetting(SystemSettingsEntity(key, value))
    }

    // --- Analytics Adapter ---
    override fun hasUsagePermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun getRealAppUsage(context: Context, periodDays: Int): List<AppUsage> {
        if (!hasUsagePermission(context)) return emptyList()
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyList()
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (periodDays * 24 * 60 * 60 * 1000L)

        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return emptyList()

        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val appLabels = pm.queryIntentActivities(intent, 0).associate {
            it.activityInfo.packageName to it.loadLabel(pm).toString()
        }

        val usageMap = mutableMapOf<String, Long>()
        statsList.forEach { stats ->
            val totalTime = stats.totalTimeInForeground
            if (totalTime > 0) {
                usageMap[stats.packageName] = (usageMap[stats.packageName] ?: 0L) + totalTime
            }
        }

        return usageMap.entries
            .map { entry ->
                val label = appLabels[entry.key] ?: entry.key.substringAfterLast('.')
                AppUsage(
                    packageName = entry.key,
                    appLabel = label,
                    screenTimeMs = entry.value,
                    launchCount = 0
                )
            }
            .filter { it.packageName != context.packageName && appLabels.containsKey(it.packageName) }
            .sortedByDescending { it.screenTimeMs }
    }
}
