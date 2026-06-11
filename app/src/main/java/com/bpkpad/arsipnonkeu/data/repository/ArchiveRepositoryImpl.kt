package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentFilter
import com.bpkpad.arsipnonkeu.domain.model.ArchiveYearSummary
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository

class ArchiveRepositoryImpl : ArchiveRepository {

    override suspend fun getArchiveYearSummaries(): List<ArchiveYearSummary> {
        // TODO: implement database/API query later
        return emptyList()
    }

    override suspend fun getArchiveDocuments(
        filter: ArchiveDocumentFilter
    ): List<ArchiveDocument> {
        // TODO: implement database/API query later
        return emptyList()
    }

    override suspend fun getArchiveDocumentById(
        id: String
    ): ArchiveDocument? {
        // TODO: implement database/API query later
        return null
    }

    override suspend fun createArchiveDocument(
        document: ArchiveDocument
    ) {
        // TODO: implement insert later
    }

    override suspend fun updateArchiveDocument(
        document: ArchiveDocument
    ) {
        // TODO: implement update later
    }

    override suspend fun deleteArchiveDocument(
        id: String
    ) {
        // TODO: implement soft delete later
    }
}