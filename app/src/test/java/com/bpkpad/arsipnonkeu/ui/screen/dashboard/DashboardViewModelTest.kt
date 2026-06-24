package com.bpkpad.arsipnonkeu.ui.screen.dashboard

import com.bpkpad.arsipnonkeu.domain.model.ArchiveYearSummary
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveYearSummariesUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest : BehaviorSpec({
    val testDispatcher = StandardTestDispatcher()
    val useCase = mockk<GetArchiveYearSummariesUseCase>()
    lateinit var viewModel: DashboardViewModel

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    beforeTest {
        viewModel = DashboardViewModel(useCase)
    }

    Given("DashboardViewModel") {
        When("loadYears is called successfully") {
            val mockData = listOf(ArchiveYearSummary(2025, 5))
            coEvery { useCase() } returns mockData

            viewModel.loadYears()
            testDispatcher.scheduler.advanceUntilIdle()

            Then("uiState should contain the loaded years") {
                viewModel.uiState.value.years shouldBe mockData
                viewModel.uiState.value.isLoading shouldBe false
            }
        }

        When("loadYears fails with exception") {
            coEvery { useCase() } throws Exception("Network Error")

            viewModel.loadYears()
            testDispatcher.scheduler.advanceUntilIdle()

            Then("uiState should show error message") {
                viewModel.uiState.value.errorMessage shouldBe "Network Error"
                viewModel.uiState.value.isLoading shouldBe false
            }
        }
    }
})
