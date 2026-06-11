package com.bpkpad.arsipnonkeu.di

import com.bpkpad.arsipnonkeu.data.repository.FakeArchiveRepository
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveDocumentDetailUseCase
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveDocumentListItemsUseCase
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveYearSummariesUseCase

object ArchiveModule {
    private val archiveRepository: ArchiveRepository = FakeArchiveRepository()

    val getArchiveYearSummariesUseCase =
        GetArchiveYearSummariesUseCase(archiveRepository)

    val getArchiveDocumentListItemsUseCase =
        GetArchiveDocumentListItemsUseCase(archiveRepository)

    val getArchiveDocumentDetailUseCase =
        GetArchiveDocumentDetailUseCase(archiveRepository)
}