package com.bpkpad.arsipnonkeu.domain.usecase

import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentFilter
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

class GetArchiveDocumentListItemsUseCaseTest : StringSpec({
    val repository = mockk<ArchiveRepository>()
    val useCase = GetArchiveDocumentListItemsUseCase(repository)

    "invoke should return list from repository" {
        val filter = ArchiveDocumentFilter(year = 2024)
        coEvery { repository.getArchiveDocumentListItems(filter) } returns emptyList()
        
        val result = useCase(filter)
        
        result shouldBe emptyList()
    }
})
