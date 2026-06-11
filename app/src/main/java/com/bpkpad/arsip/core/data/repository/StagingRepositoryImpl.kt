package com.bpkpad.arsip.core.data.repository

import com.bpkpad.arsip.core.data.local.dao.ArchiveDocumentDao
import com.bpkpad.arsip.core.data.local.dao.TempDocumentDao
import com.bpkpad.arsip.core.data.local.entity.ArchiveDocumentEntity
import com.bpkpad.arsip.core.data.local.entity.TempDocumentEntity
import com.bpkpad.arsip.core.domain.model.DocumentType
import com.bpkpad.arsip.core.domain.model.StagingDocument
import com.bpkpad.arsip.core.domain.model.StagingStatus
import com.bpkpad.arsip.core.domain.repository.StagingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StagingRepositoryImpl @Inject constructor(
    private val tempDocumentDao: TempDocumentDao,
    private val archiveDao: ArchiveDocumentDao
) : StagingRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getAllStaging(): Flow<List<StagingDocument>> {
        return tempDocumentDao.getAllStaging().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveToStaging(doc: StagingDocument): Result<Unit> {
        return try {
            tempDocumentDao.insert(doc.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStaging(doc: StagingDocument): Result<Unit> {
        return try {
            tempDocumentDao.update(doc.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFromStaging(id: String) {
        tempDocumentDao.deleteById(id)
    }

    override suspend fun pushAllToCloud(locationId: String, userId: String): Result<Unit> {
        return try {
            val stagingDocs = tempDocumentDao.getAllStaging().first()
            stagingDocs.forEach { stagingEntity ->
                val archiveEntity = ArchiveDocumentEntity(
                    id = stagingEntity.id,
                    type = stagingEntity.type,
                    title = stagingEntity.title,
                    year = stagingEntity.year,
                    condition = stagingEntity.condition,
                    instance = stagingEntity.instance,
                    metadata = stagingEntity.metadata,
                    coverUrl = stagingEntity.coverLocalPath,
                    timestampUserId = userId
                )
                archiveDao.insert(archiveEntity)
                tempDocumentDao.deleteById(stagingEntity.id)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun TempDocumentEntity.toDomain(): StagingDocument {
        return StagingDocument(
            id = id,
            type = DocumentType.valueOf(type),
            title = title,
            year = year,
            metadata = json.decodeFromString(metadata),
            coverLocalPath = coverLocalPath,
            status = StagingStatus.valueOf(status)
        )
    }

    private fun StagingDocument.toEntity(): TempDocumentEntity {
        return TempDocumentEntity(
            id = id,
            type = type.name,
            title = title,
            year = year,
            condition = "BAIK", // Placeholder
            instance = "BPKPAD", // Placeholder
            metadata = json.encodeToString(metadata),
            coverLocalPath = coverLocalPath,
            status = status.name
        )
    }
}