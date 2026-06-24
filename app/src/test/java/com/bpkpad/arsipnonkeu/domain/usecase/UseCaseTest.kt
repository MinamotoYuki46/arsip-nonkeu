package com.bpkpad.arsipnonkeu.domain.usecase

import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentFilter
import com.bpkpad.arsipnonkeu.domain.model.ArchiveYearSummary
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

class UseCaseTest : BehaviorSpec({

    val repository = mockk<ArchiveRepository>()

    given("GetArchiveYearSummariesUseCase") {
        val useCase = GetArchiveYearSummariesUseCase(repository)
        
        `when`("invoked") {
            val dummyData = listOf(ArchiveYearSummary(2025, 10))
            coEvery { repository.getArchiveYearSummaries() } returns dummyData
            
            val result = useCase.invoke()
            
            then("it should return data from repository") {
                result shouldBe dummyData
                coVerify { repository.getArchiveYearSummaries() }
            }
        }
    }

    given("GetArchiveDocumentListItemsUseCase") {
        val useCase = GetArchiveDocumentListItemsUseCase(repository)
        
        `when`("invoked with filter") {
            val filter = ArchiveDocumentFilter(year = 2025)
            coEvery { repository.getArchiveDocumentListItems(filter) } returns emptyList()
            
            val result = useCase.invoke(filter)
            
            then("it should call repository with same filter") {
                result shouldBe emptyList()
                coVerify { repository.getArchiveDocumentListItems(filter) }
            }
        }
    }
})
