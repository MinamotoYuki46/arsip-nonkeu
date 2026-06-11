package com.bpkpad.arsip.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bpkpad.arsip.core.data.local.entity.TempDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TempDocumentDao {
    @Query("SELECT * FROM temp_documents ORDER BY createdAt DESC")
    fun getAllStaging(): Flow<List<TempDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(doc: TempDocumentEntity)

    @Update
    suspend fun update(doc: TempDocumentEntity)

    @Query("DELETE FROM temp_documents WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM temp_documents WHERE id = :id")
    suspend fun getById(id: String): TempDocumentEntity?
}