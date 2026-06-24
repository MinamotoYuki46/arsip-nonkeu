package com.bpkpad.arsipnonkeu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bpkpad.arsipnonkeu.data.local.entity.ArchiveClassificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchiveClassificationDao {
    @Query("SELECT * FROM archive_classifications")
    fun getAllClassifications(): Flow<List<ArchiveClassificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassifications(classifications: List<ArchiveClassificationEntity>)

    @Query("DELETE FROM archive_classifications")
    suspend fun clearClassifications()
}
