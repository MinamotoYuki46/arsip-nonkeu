package com.bpkpad.arsipnonkeu.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bpkpad.arsipnonkeu.data.local.dao.ArchiveClassificationDao
import com.bpkpad.arsipnonkeu.data.local.dao.ArchiveDocumentDao
import com.bpkpad.arsipnonkeu.data.local.dao.UserProfileDao
import com.bpkpad.arsipnonkeu.data.local.entity.ArchiveClassificationEntity
import com.bpkpad.arsipnonkeu.data.local.entity.ArchiveDocumentEntity
import com.bpkpad.arsipnonkeu.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        ArchiveDocumentEntity::class,
        ArchiveClassificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun archiveDocumentDao(): ArchiveDocumentDao
    abstract fun archiveClassificationDao(): ArchiveClassificationDao
}
