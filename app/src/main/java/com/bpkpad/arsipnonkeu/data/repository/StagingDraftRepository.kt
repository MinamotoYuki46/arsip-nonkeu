package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.data.local.StagingDraftLocalDataSource
import com.bpkpad.arsipnonkeu.domain.model.StagingDraft

class StagingDraftRepository(
    private val localDataSource: StagingDraftLocalDataSource
) {
    fun getDrafts(): List<StagingDraft> {
        return localDataSource.getAll()
    }

    fun saveDraft(draft: StagingDraft) {
        localDataSource.upsert(draft)
    }

    fun deleteDraft(id: String) {
        localDataSource.deleteById(id)
    }

    fun clearDrafts() {
        localDataSource.clearAll()
    }
}