package com.bpkpad.arsipnonkeu.domain.usecase

import com.bpkpad.arsipnonkeu.domain.model.ArchiveYearSummary
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository

class GetArchiveYearSummariesUseCase(
    private val repository: ArchiveRepository
) {
    suspend operator fun invoke(): List<ArchiveYearSummary> {
        return repository.getArchiveYearSummaries()
    }
}