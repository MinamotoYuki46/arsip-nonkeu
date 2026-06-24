package com.bpkpad.arsipnonkeu.data.local.dao

import androidx.room.*
import com.bpkpad.arsipnonkeu.data.local.entity.ArchiveDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchiveDocumentDao {
    @Query("SELECT * FROM archive_documents WHERE year = :year AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun getArchiveDocumentsByYear(year: Int): Flow<List<ArchiveDocumentEntity>>

    @Query("SELECT * FROM archive_documents WHERE id = :id")
    suspend fun getArchiveDocumentById(id: String): ArchiveDocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchiveDocuments(documents: List<ArchiveDocumentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchiveDocument(document: ArchiveDocumentEntity)

    @Delete
    suspend fun deleteArchiveDocument(document: ArchiveDocumentEntity)

    @Query("DELETE FROM archive_documents WHERE year = :year")
    suspend fun clearArchiveDocumentsByYear(year: Int)

    // Untuk ArchiveYearSummary, kita bisa agregasi di Room
    @Query("SELECT year, COUNT(*) as count FROM archive_documents WHERE deletedAt IS NULL GROUP BY year ORDER BY year DESC")
    fun getArchiveYearSummaries(): Flow<List<YearSummaryProjection>>
}

data class YearSummaryProjection(
    val year: Int,
    val count: Int
)
