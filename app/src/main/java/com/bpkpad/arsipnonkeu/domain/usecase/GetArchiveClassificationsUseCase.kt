package com.bpkpad.arsipnonkeu.domain.usecase

import com.bpkpad.arsipnonkeu.data.repository.FakeArchiveClassificationRepository
import com.bpkpad.arsipnonkeu.domain.model.ArchiveClassification

class GetArchiveClassificationsUseCase(
    private val repository: FakeArchiveClassificationRepository
) {

    suspend operator fun invoke(
        keyword: String = ""
    ): List<ArchiveClassification> {
        return if (keyword.isBlank()) {
            repository.getAll()
        } else {
            repository.search(keyword)
        }
    }

    suspend fun findByCode(
        code: String?
    ): ArchiveClassification? {
        return repository.findByCode(code)
    }

    suspend fun isValidCode(
        code: String?
    ): Boolean {
        return repository.isValidCode(code)
    }

    suspend fun getChildren(
        parentCode: String?
    ): List<ArchiveClassification> {
        return repository.getChildren(parentCode)
    }

    suspend fun getTopLevel(): List<ArchiveClassification> {
        return repository.getTopLevel()
    }
}
