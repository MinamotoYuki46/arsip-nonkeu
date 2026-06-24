package com.bpkpad.arsipnonkeu.ui.screen.archive

import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentFilter
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveViewModelTest : BehaviorSpec({

    val testDispatcher = StandardTestDispatcher()
    val repository = mockk<ArchiveRepository>()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    given("ArchiveViewModel") {
        
        every { repository.observeArchiveDocumentListItems(any()) } returns flowOf(emptyList())
        coEvery { repository.refreshArchiveDocuments(any()) } just Runs
        
        val viewModel = ArchiveViewModel(repository)
        viewModel.loadDocumentsByYear(2025)

        `when`("updating keyword") {
            val keyword = "Surat Keputusan"
            viewModel.updateKeyword(keyword)
            
            then("filter in uiState should contain keyword") {
                viewModel.uiState.value.filter?.keyword shouldBe keyword
            }
        }

        `when`("updating document type") {
            val type = DocumentType.PERDA
            viewModel.updateDocumentType(type)
            
            then("filter in uiState should contain document type") {
                viewModel.uiState.value.filter?.documentType shouldBe type
            }
        }

        `when`("resetting filter") {
            viewModel.updateKeyword("Search")
            viewModel.resetFilter()
            
            then("filter in uiState should be default for the year") {
                viewModel.uiState.value.filter?.year shouldBe 2025
                viewModel.uiState.value.filter?.keyword shouldBe null
            }
        }
    }
})
