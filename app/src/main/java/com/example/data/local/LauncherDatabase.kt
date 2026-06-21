package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AppConfigEntity::class,
        AppLaunchLogEntity::class,
        FocusSessionEntity::class,
        SystemSettingsEntity::class,
        NotificationLogEntity::class,
        LifeValueLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract val dao: LauncherDao
}
