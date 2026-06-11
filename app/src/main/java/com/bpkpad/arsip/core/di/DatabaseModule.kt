package com.bpkpad.arsip.core.di

import android.content.Context
import androidx.room.Room
import com.bpkpad.arsip.core.data.local.AppDatabase
import com.bpkpad.arsip.core.data.local.dao.TempDocumentDao
import com.bpkpad.arsip.core.data.local.dao.UserDao
import com.bpkpad.arsip.core.data.local.dao.ArchiveDocumentDao
import com.bpkpad.arsip.core.data.local.DatabaseSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        databaseProvider: Provider<AppDatabase>
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "arsip_core_db"
        ).addCallback(DatabaseSeeder(databaseProvider))
            .fallbackToDestructiveMigration(true) // Fixed deprecation
            .build()
    }

    @Provides
    fun provideTempDocumentDao(database: AppDatabase): TempDocumentDao {
        return database.tempDocumentDao()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideArchiveDocumentDao(database: AppDatabase): ArchiveDocumentDao {
        return database.archiveDocumentDao()
    }
}