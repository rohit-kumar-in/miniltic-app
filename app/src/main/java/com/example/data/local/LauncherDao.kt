package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDao {

    // --- App Config Queries ---
    @Query("SELECT * FROM app_configs")
    fun getAllAppConfigs(): Flow<List<AppConfigEntity>>

    @Query("SELECT * FROM app_configs WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppConfig(packageName: String): AppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppConfig(config: AppConfigEntity)

    @Update
    suspend fun updateAppConfig(config: AppConfigEntity)

    // --- Launch Logs Queries ---
    @Query("SELECT * FROM app_launch_logs ORDER BY timestamp DESC")
    fun getAllLaunchLogs(): Flow<List<AppLaunchLogEntity>>

    @Query("SELECT * FROM app_launch_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getLaunchLogsSince(sinceTimestamp: Long): Flow<List<AppLaunchLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaunchLog(log: AppLaunchLogEntity)

    @Query("DELETE FROM app_launch_logs")
    suspend fun clearLaunchLogs()

    // --- Focus Sessions Queries ---
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity)

    // --- System Settings Queries ---
    @Query("SELECT * FROM system_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): SystemSettingsEntity?

    @Query("SELECT * FROM system_settings")
    fun getAllSettings(): Flow<List<SystemSettingsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SystemSettingsEntity)
}
