package com.bpkpad.arsip.data.repository

import com.bpkpad.arsip.core.data.local.dao.ArchiveDocumentDao
import com.bpkpad.arsip.core.data.local.entity.ArchiveDocumentEntity
import com.bpkpad.arsip.core.data.local.entity.toDomain
import com.bpkpad.arsip.data.remote.ArchiveApiService
import com.bpkpad.arsip.data.remote.dto.toDomain
import com.bpkpad.arsip.domain.model.ArchiveDocument
import com.bpkpad.arsip.domain.repository.ArchiveRepository
import com.bpkpad.arsip.utils.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class ArchiveRepositoryImpl @Inject constructor(
    private val apiService: ArchiveApiService,
    private val archiveDao: ArchiveDocumentDao
) : ArchiveRepository {

    override fun getArchives(): Flow<ResultState<List<ArchiveDocument>>> = flow {
        emit(ResultState.Loading)
        
        archiveDao.getAllDocuments().collect { entities ->
            emit(ResultState.Success(entities.map { it.toDomain() }))
        }
    }

    override fun getArchiveById(id: String): Flow<ResultState<ArchiveDocument>> = flow {
        // Simplified for local dev seeder
        emit(ResultState.Loading)
        archiveDao.getAllDocuments().collect { entities ->
            val doc = entities.find { it.id == id }?.toDomain()
            if (doc != null) emit(ResultState.Success(doc))
        }
    }

    override fun saveArchive(document: ArchiveDocument): Flow<ResultState<Unit>> = flow {
        emit(ResultState.Loading)
        try {
            val entity = ArchiveDocumentEntity(
                id = if (document.id.isEmpty()) UUID.randomUUID().toString() else document.id,
                type = document.type,
                title = document.title,
                year = 2024, // Simplified
                condition = "BAIK",
                instance = "BPKPAD",
                metadata = "{\"description\":\"${document.description}\",\"box_id\":\"${document.boxId}\",\"location_id\":\"${document.locationId}\"}",
                coverUrl = document.imageUrl,
                timestampUserId = "admin" // Placeholder
            )
            archiveDao.insert(entity)
            emit(ResultState.Success(Unit))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Unknown Error"))
        }
    }

    override fun parseArchiveFromText(rawText: String): Flow<ResultState<ArchiveDocument>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiService.parseArchiveFromText(mapOf("text" to rawText))
            if (response.isSuccessful) {
                val data = response.body()?.toDomain()
                if (data != null) {
                    emit(ResultState.Success(data))
                } else {
                    emit(ResultState.Error("Failed to parse document"))
                }
            } else {
                emit(ResultState.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Unknown Error", e))
        }
    }
}
