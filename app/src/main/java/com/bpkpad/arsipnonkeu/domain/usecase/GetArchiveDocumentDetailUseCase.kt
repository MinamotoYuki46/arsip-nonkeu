package com.bpkpad.arsipnonkeu.domain.usecase

import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentListItem
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository

class GetArchiveDocumentDetailUseCase(
    private val repository: ArchiveRepository
) {
    suspend operator fun invoke(
        id: String
    ): ArchiveDocumentListItem? {
        return repository.getArchiveDocumentListItemById(id)
    }
}