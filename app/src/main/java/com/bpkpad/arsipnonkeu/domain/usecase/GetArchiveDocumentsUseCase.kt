package com.bpkpad.arsipnonkeu.domain.usecase

import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentFilter
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository

class GetArchiveDocumentsUseCase(
    private val repository: ArchiveRepository
) {
    suspend operator fun invoke(
        filter: ArchiveDocumentFilter
    ): List<ArchiveDocument> {
        return repository.getArchiveDocuments(filter)
    }
}