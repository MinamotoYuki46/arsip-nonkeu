package com.bpkpad.arsipnonkeu.ui.screen.scan

import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ScanViewModelTest : BehaviorSpec({
    val testDispatcher = StandardTestDispatcher()
    lateinit var viewModel: ScanViewModel

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    beforeTest {
        viewModel = ScanViewModel()
    }

    Given("ScanViewModel") {
        When("onDocumentTypeChange is called") {
            viewModel.onDocumentTypeChange(DocumentType.PERDA)
            
            Then("selected document type should be updated") {
                viewModel.uiState.value.selectedDocumentType shouldBe DocumentType.PERDA
            }
        }

        When("clearMessage is called") {
            viewModel.clearMessage()
            
            Then("errorMessage in uiState should be null") {
                viewModel.uiState.value.errorMessage shouldBe null
            }
        }
    }
})
