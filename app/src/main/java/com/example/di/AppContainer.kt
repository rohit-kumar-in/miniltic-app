package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.LauncherDatabase
import com.example.data.repository.LauncherRepositoryImpl
import com.example.domain.repository.LauncherRepository

class AppContainer(context: Context) {
    
    private val database: LauncherDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            LauncherDatabase::class.java,
            "minilauncher_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    val repository: LauncherRepository by lazy {
        LauncherRepositoryImpl(database.dao)
    }
}
