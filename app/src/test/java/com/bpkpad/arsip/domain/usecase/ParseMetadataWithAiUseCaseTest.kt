package com.bpkpad.arsip.domain.usecase

import app.cash.turbine.test
import com.bpkpad.arsip.domain.model.ArchiveDocument
import com.bpkpad.arsip.domain.repository.ArchiveRepository
import com.bpkpad.arsip.utils.ResultState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class ParseMetadataWithAiUseCaseTest {

    private val repository: ArchiveRepository = mockk()
    private val useCase = ParseMetadataWithAiUseCase(repository)

    @Test
    fun `invoke should return result from repository`() = runTest {
        // Given
        val rawText = "some raw text"
        val expectedDoc = ArchiveDocument(
            id = "1",
            title = "Title",
            type = "Type",
            date = System.currentTimeMillis(),
            description = "Desc",
            boxId = "B1",
            locationId = "L1"
        )
        every { repository.parseArchiveFromText(rawText) } returns flowOf(ResultState.Success(expectedDoc))

        // When
        val result = useCase(rawText)

        // Then
        result.test {
            assertEquals(ResultState.Success(expectedDoc), awaitItem())
            awaitComplete()
        }
    }
}
