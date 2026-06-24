package com.bpkpad.arsipnonkeu.domain.usecase

import com.bpkpad.arsipnonkeu.data.repository.FakeArchiveClassificationRepository
import com.bpkpad.arsipnonkeu.domain.model.ArchiveClassification
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

class GetArchiveClassificationsUseCaseTest : StringSpec({
    val repository = mockk<FakeArchiveClassificationRepository>()
    val useCase = GetArchiveClassificationsUseCase(repository)

    "invoke should return all classifications when keyword is blank" {
        val expected = listOf(ArchiveClassification("1", "Test", null, 1))
        coEvery { repository.getAll() } returns expected
        
        val result = useCase("")
        
        result shouldBe expected
    }

    "invoke should search classifications when keyword is not blank" {
        val expected = listOf(ArchiveClassification("1", "Test", null, 1))
        coEvery { repository.search("query") } returns expected
        
        val result = useCase("query")
        
        result shouldBe expected
    }

    "isValidCode should call repository" {
        coEvery { repository.isValidCode("100") } returns true
        
        useCase.isValidCode("100") shouldBe true
    }
})
