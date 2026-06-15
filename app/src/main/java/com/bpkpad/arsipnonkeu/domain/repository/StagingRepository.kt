package com.bpkpad.arsipnonkeu.domain.repository

import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingDocument

interface StagingRepository {
    suspend fun getStagingDocuments(year: Int): List<StagingDocument>
    suspend fun upsertStagingDocument(document: StagingDocument)
    suspend fun deleteStagingDocument(id: String)
}
