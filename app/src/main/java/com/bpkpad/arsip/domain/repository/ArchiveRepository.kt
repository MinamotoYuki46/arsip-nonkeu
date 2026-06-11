package com.bpkpad.arsip.domain.repository

import com.bpkpad.arsip.domain.model.ArchiveDocument
import com.bpkpad.arsip.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface ArchiveRepository {
    fun getArchives(): Flow<ResultState<List<ArchiveDocument>>>
    fun getArchiveById(id: String): Flow<ResultState<ArchiveDocument>>
    fun saveArchive(document: ArchiveDocument): Flow<ResultState<Unit>>
    fun parseArchiveFromText(rawText: String): Flow<ResultState<ArchiveDocument>>
}
