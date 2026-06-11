package com.bpkpad.arsip.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bpkpad.arsip.core.data.local.dao.*
import com.bpkpad.arsip.core.data.local.entity.*

@Database(
    entities = [
        TempDocumentEntity::class,
        UserEntity::class,
        StorageLocationEntity::class,
        ArchiveDocumentEntity::class,
        StoringEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tempDocumentDao(): TempDocumentDao
    abstract fun userDao(): UserDao
    abstract fun storageLocationDao(): StorageLocationDao
    abstract fun archiveDocumentDao(): ArchiveDocumentDao
    abstract fun storingDao(): StoringDao
}
