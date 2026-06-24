package com.bpkpad.arsipnonkeu.data.local.datasource

import com.bpkpad.arsipnonkeu.data.local.dao.ArchiveDocumentDao
import com.bpkpad.arsipnonkeu.data.local.mapper.toDomain
import com.bpkpad.arsipnonkeu.data.local.mapper.toEntity
import com.bpkpad.arsipnonkeu.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ArchiveLocalDataSource(private val archiveDocumentDao: ArchiveDocumentDao) {
    fun observeArchiveDocumentListItems(year: Int): Flow<List<ArchiveDocumentListItem>> {
        return archiveDocumentDao.getArchiveDocumentsByYear(year).map { entities ->
            entities.map { entity ->
                val domain = entity.toDomain()
                ArchiveDocumentListItem(
                    document = domain,
                    currentPlacement = null,
                    storageLocation = domain.room?.let { room ->
                        StorageLocation(
                            id = "", // ID is not strictly needed for export display
                            room = room,
                            shelf = domain.shelf,
                            boxNumber = domain.boxNumber
                        )
                    }
                )
            }
        }
    }

    fun observeArchiveYearSummaries(): Flow<List<ArchiveYearSummary>> {
        return archiveDocumentDao.getArchiveYearSummaries().map { projections ->
            projections.map { projection ->
                ArchiveYearSummary(
                    year = projection.year,
                    documentCount = projection.count
                )
            }
        }
    }

    suspend fun getArchiveDocumentById(id: String): ArchiveDocument? {
        return archiveDocumentDao.getArchiveDocumentById(id)?.toDomain()
    }

    suspend fun saveArchiveDocuments(documents: List<ArchiveDocument>) {
        archiveDocumentDao.insertArchiveDocuments(documents.map { it.toEntity() })
    }

    suspend fun saveArchiveDocument(document: ArchiveDocument) {
        archiveDocumentDao.insertArchiveDocument(document.toEntity())
    }

    suspend fun deleteArchiveDocument(document: ArchiveDocument) {
        archiveDocumentDao.deleteArchiveDocument(document.toEntity())
    }

    suspend fun clearArchiveDocumentsByYear(year: Int) {
        archiveDocumentDao.clearArchiveDocumentsByYear(year)
    }
}
