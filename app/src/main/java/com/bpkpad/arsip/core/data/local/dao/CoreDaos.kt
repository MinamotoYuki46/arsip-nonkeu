package com.bpkpad.arsip.core.data.local.dao

import androidx.room.*
import com.bpkpad.arsip.core.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)
}

@Dao
interface StorageLocationDao {
    @Query("SELECT * FROM storage_locations")
    fun getAllLocations(): Flow<List<StorageLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: StorageLocationEntity)
}

@Dao
interface ArchiveDocumentDao {
    @Query("SELECT * FROM archive_documents WHERE deletedAt IS NULL")
    fun getAllDocuments(): Flow<List<ArchiveDocumentEntity>>

    @Query("SELECT * FROM archive_documents WHERE year = :year AND deletedAt IS NULL")
    fun getDocumentsByYear(year: Int): Flow<List<ArchiveDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: ArchiveDocumentEntity)

    @Query("UPDATE archive_documents SET deletedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface StoringDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(storing: StoringEntity)
}
