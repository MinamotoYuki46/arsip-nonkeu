package com.bpkpad.arsipnonkeu.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsipnonkeu.di.ArchiveModule
import com.bpkpad.arsipnonkeu.domain.model.ArchiveYearSummary
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


data class ArchiveYearDashboardUiState(
    val isLoading: Boolean = false,
    val years: List<ArchiveYearSummary> = emptyList(),
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val archiveRepository: ArchiveRepository =
        ArchiveModule.archiveRepositoryInstance
) : ViewModel(){
    private val _uiState = MutableStateFlow(ArchiveYearDashboardUiState())
    val uiState: StateFlow<ArchiveYearDashboardUiState> = _uiState.asStateFlow()

    init {
        observeYears()
    }

    private fun observeYears() {
        viewModelScope.launch {
            archiveRepository.observeArchiveYearSummaries().collectLatest { years ->
                _uiState.value = _uiState.value.copy(
                    years = years,
                    isLoading = false
                )
            }
        }
    }

    fun loadYears() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                // Trigger remote refresh
                archiveRepository.refreshArchiveYearSummaries()
                // Flow will automatically update the UI
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Gagal menyegarkan data dari server"
                )
            }
        }
    }

    fun addNewYear(year: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Check if year already exists locally
                if (_uiState.value.years.any { it.year == year }) {
                    throw Exception("Tahun $year sudah ada.")
                }

                val placeholder = ArchiveDocument(
                    id = "", 
                    documentType = DocumentType.SURAT,
                    documentNumber = null,
                    classificationCode = null,
                    title = "Placeholder $year",
                    description = "Dokumen pembuka untuk tahun $year.",
                    year = year,
                    physicalForm = PhysicalForm.SHEET,
                    condition = null,
                    copyCount = 1,
                    isCopy = null,
                    status = DocumentStatus.AVAILABLE,
                    originInstance = null,
                    createdBy = null,
                    updatedBy = null,
                    createdAt = null,
                    updatedAt = null,
                    deletedAt = null 
                )

                archiveRepository.createArchiveDocument(placeholder)
                // Refresh to ensure everything is in sync
                loadYears()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Gagal menambah tahun baru"
                )
            }
        }
    }
}
