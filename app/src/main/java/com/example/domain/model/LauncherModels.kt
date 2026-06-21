package com.example.domain.model

data class AppInfo(
    val packageName: String,
    val label: String,
    val isFavorite: Boolean = false,
    val isWhitelisted: Boolean = false,
    val isHidden: Boolean = false,
    val openingDelaySeconds: Int = 0,
    val customLabel: String? = null,
    val limitMinutes: Int = 0
)

data class FocusSession(
    val id: Long = 0L,
    val sessionName: String,
    val durationMinutes: Int,
    val timestamp: Long,
    val completed: Boolean
)

data class LaunchLog(
    val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val timestamp: Long,
    val durationMs: Long = 0L
)

data class AppUsage(
    val packageName: String,
    val appLabel: String,
    val screenTimeMs: Long,
    val launchCount: Int
)

data class BatchNotificationSetting(
    val isEnabled: Boolean = false,
    val batchIntervalMinutes: Int = 15,
    val focusModeActive: Boolean = false
)

data class LifeValueLog(
    val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val timestamp: Long,
    val durationMs: Long,
    val valueCategory: String, // High Value, Medium Value, Low Value
    val response: String
)

enum class WellnessState {
    Focused, Balanced, Mindful, Distracted, Overloaded, Unknown
}
