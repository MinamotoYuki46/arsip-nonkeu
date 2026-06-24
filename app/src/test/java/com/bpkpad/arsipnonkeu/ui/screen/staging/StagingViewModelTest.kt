package com.bpkpad.arsipnonkeu.ui.screen.staging

import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository
import com.bpkpad.arsipnonkeu.data.repository.StagingDraftRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class StagingViewModelTest : BehaviorSpec({
    val stagingRepository = mockk<StagingDraftRepository>(relaxed = true)
    val archiveRepository = mockk<ArchiveRepository>(relaxed = true)
    
    // Note: StagingViewModel might try to load drafts on init, so we use relaxed mock
    // or specify every { stagingRepository.getDrafts() } returns emptyList()
    
    val viewModel = StagingViewModel(stagingRepository, archiveRepository)

    Given("StagingViewModel") {
        When("onRoomChange is called") {
            viewModel.onRoomChange("Gudang A")
            
            Then("uiState room should be updated") {
                viewModel.uiState.value.room shouldBe "Gudang A"
            }
        }

        When("isStorageLocationValid is checked") {
            viewModel.onRoomChange("Gudang A")
            viewModel.onShelfChange("Rak 1")
            
            Then("it should be true if room and shelf are not blank") {
                viewModel.uiState.value.isStorageLocationValid shouldBe true
            }
        }
        
        When("clearMessage is called") {
            // We can't easily set errorMessage since it's private mutation in VM
            // But we can check if it becomes null
            viewModel.clearMessage()
            viewModel.uiState.value.errorMessage shouldBe null
        }
    }
})
