package com.bpkpad.arsipnonkeu.domain.repository

import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentFilter
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentListItem
import com.bpkpad.arsipnonkeu.domain.model.ArchiveYearSummary

interface ArchiveRepository {
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

    suspend fun createArchiveDocument(
        document: ArchiveDocument
    )

    suspend fun updateArchiveDocument(
        document: ArchiveDocument
    )

    suspend fun deleteArchiveDocument(
        id: String
    )
}