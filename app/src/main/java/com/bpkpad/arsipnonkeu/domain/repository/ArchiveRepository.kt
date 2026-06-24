package com.bpkpad.arsipnonkeu.domain.repository

import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentFilter
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentListItem
import com.bpkpad.arsipnonkeu.domain.model.ArchiveYearSummary
import kotlinx.coroutines.flow.Flow

interface ArchiveRepository {
    // Read operations (Local First via Flow)
    fun observeArchiveYearSummaries(): Flow<List<ArchiveYearSummary>>

    fun observeArchiveDocumentListItems(year: Int): Flow<List<ArchiveDocumentListItem>>

    // Refresh operations (Fetch from Remote to Local)
    suspend fun refreshArchiveYearSummaries()

    suspend fun refreshArchiveDocuments(year: Int)

    suspend fun refreshArchiveDocumentById(id: String)

    // Legacy/Sync operations
    suspend fun getArchiveYearSummaries(): List<ArchiveYearSummary>

    suspend fun getArchiveDocumentListItems(
        filter: ArchiveDocumentFilter
    ): List<ArchiveDocumentListItem>

    suspend fun getArchiveDocumentListItemById(
        id: String
    ): ArchiveDocumentListItem?

    suspend fun getArchiveDocumentById(
        id: String
    ): ArchiveDocument?

    // Write operations (Remote first then Local)
    suspend fun createArchiveDocument(
        document: ArchiveDocument
    )

    suspend fun updateArchiveDocument(
        document: ArchiveDocument
    )

    suspend fun deleteArchiveDocument(
        id: String
    )

    suspend fun saveStagingDocuments(
        documents: List<ArchiveDocument>,
        room: String,
        shelf: String,
        boxNumber: String?,
        actorId: String? = null
    )

    suspend fun checkStorageLocationExists(
        room: String,
        shelf: String,
        boxNumber: String?
    ): Boolean

    suspend fun checkDocumentDuplicate(
        title: String,
        documentNumber: String?,
        year: Int
    ): Boolean
}
