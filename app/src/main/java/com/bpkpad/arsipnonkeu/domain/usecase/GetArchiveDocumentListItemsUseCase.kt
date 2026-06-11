package com.bpkpad.arsipnonkeu.domain.usecase

import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentFilter
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentListItem
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository

class GetArchiveDocumentListItemsUseCase(
    private val repository: ArchiveRepository
) {
    suspend operator fun invoke(
        filter: ArchiveDocumentFilter
    ): List<ArchiveDocumentListItem> {
        return repository.getArchiveDocumentListItems(filter)
    }
}