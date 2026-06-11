package com.bpkpad.arsipnonkeu.domain.usecase

import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository


class GetArchiveDocumentDetailUseCase(
    private val repository: ArchiveRepository
) {
    suspend operator fun invoke(id: String): ArchiveDocument? {
        return repository.getArchiveDocumentById(id)
    }
}