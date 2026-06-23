package com.bpkpad.arsipnonkeu

import android.app.Application
import androidx.room.Room
import com.bpkpad.arsipnonkeu.data.local.database.AppDatabase
import com.bpkpad.arsipnonkeu.di.ArchiveModule

class BpkpadApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Database
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "arsip_bpkpad.db"
        ).fallbackToDestructiveMigration()
            .build()
        
        // Initialize ArchiveModule with database
        ArchiveModule.initialize(database)
    }
}
