package com.bpkpad.arsipnonkeu.ui.screen.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsipnonkeu.di.ArchiveModule
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentFilter
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveDocumentsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ArchiveListUiState(
    val isLoading: Boolean = false,
    val selectedYear: Int? = null,
    val documents: List<ArchiveDocument> = emptyList(),
    val filter: ArchiveDocumentFilter? = null,
    val errorMessage: String? = null
)

class ArchiveViewModel(
    private val getArchiveDocumentsUseCase: GetArchiveDocumentsUseCase =
        ArchiveModule.getArchiveDocumentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveListUiState())
    val uiState: StateFlow<ArchiveListUiState> = _uiState.asStateFlow()

    fun loadDocumentsByYear(year: Int) {
        val filter = ArchiveDocumentFilter(year = year)
        loadDocuments(filter)
    }

    fun applyFilter(
        documentType: DocumentType? = null,
        status: DocumentStatus? = null,
        physicalForm: PhysicalForm? = null,
        condition: DocumentCondition? = null,
        originInstance: String? = null,
        keyword: String? = null
    ) {
        val currentYear = _uiState.value.selectedYear ?: return

        val filter = ArchiveDocumentFilter(
            year = currentYear,
            documentType = documentType,
            status = status,
            physicalForm = physicalForm,
            condition = condition,
            originInstance = originInstance,
            keyword = keyword
        )

        loadDocuments(filter)
    }

    fun resetFilter() {
        val currentYear = _uiState.value.selectedYear ?: return
        loadDocumentsByYear(currentYear)
    }

    private fun loadDocuments(filter: ArchiveDocumentFilter) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedYear = filter.year,
                filter = filter,
                errorMessage = null
            )

            try {
                val documents = getArchiveDocumentsUseCase(filter)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    documents = documents
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Gagal memuat dokumen arsip"
                )
            }
        }
    }
}