package com.bpkpad.arsip.core.domain.repository

import com.bpkpad.arsip.core.domain.model.StagingDocument
import kotlinx.coroutines.flow.Flow

interface StagingRepository {
    fun getAllStaging(): Flow<List<StagingDocument>>
    suspend fun saveToStaging(doc: StagingDocument): Result<Unit>
    suspend fun updateStaging(doc: StagingDocument): Result<Unit>
    suspend fun deleteFromStaging(id: String)
    suspend fun pushAllToCloud(locationId: String, userId: String): Result<Unit>
}