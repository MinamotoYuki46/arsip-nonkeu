package com.bpkpad.arsip.presentation.home

import app.cash.turbine.test
import com.bpkpad.arsip.domain.usecase.ExcelExportUseCase
import com.bpkpad.arsip.domain.usecase.GetArchivesUseCase
import com.bpkpad.arsip.utils.ResultState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getArchivesUseCase: GetArchivesUseCase
    private lateinit var excelExportUseCase: ExcelExportUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getArchivesUseCase = mockk()
        excelExportUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getArchives should update uiState with success when usecase returns success`() = runTest {
        // Given
        val archives = emptyList<com.bpkpad.arsip.domain.model.ArchiveDocument>()
        every { getArchivesUseCase() } returns flowOf(ResultState.Success(archives))
        
        // When
        viewModel = HomeViewModel(getArchivesUseCase, excelExportUseCase)
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isLoading)
            assertEquals(archives, state.archives)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
