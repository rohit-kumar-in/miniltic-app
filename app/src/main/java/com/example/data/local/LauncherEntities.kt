package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_configs")
data class AppConfigEntity(
    @PrimaryKey val packageName: String,
    val isFavorite: Boolean = false,
    val isWhitelisted: Boolean = false, // whitelisted from opening delay / Focus Mode
    val isHidden: Boolean = false,
    val openingDelaySeconds: Int = 0 // 0 means use global delay or no delay
)

@Entity(tableName = "app_launch_logs")
data class AppLaunchLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val timestamp: Long,
    val durationMs: Long = 0L
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionName: String,
    val durationMinutes: Int,
    val timestamp: Long,
    val completed: Boolean
)

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)
