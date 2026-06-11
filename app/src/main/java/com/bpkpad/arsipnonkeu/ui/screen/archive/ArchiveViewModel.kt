package com.bpkpad.arsipnonkeu.ui.screen.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsipnonkeu.di.ArchiveModule
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentFilter
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentListItem
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveDocumentListItemsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ArchiveListUiState(
    val isLoading: Boolean = false,
    val selectedYear: Int? = null,
    val documents: List<ArchiveDocumentListItem> = emptyList(),
    val filter: ArchiveDocumentFilter? = null,
    val errorMessage: String? = null
)

class ArchiveViewModel(
    private val getArchiveDocumentListItemsUseCase: GetArchiveDocumentListItemsUseCase =
        ArchiveModule.getArchiveDocumentListItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveListUiState())
    val uiState: StateFlow<ArchiveListUiState> = _uiState.asStateFlow()

    fun loadDocumentsByYear(year: Int) {
        val currentFilter = _uiState.value.filter

        val filter = if (currentFilter?.year == year) {
            currentFilter
        } else {
            ArchiveDocumentFilter(year = year)
        }

        loadDocuments(filter)
    }

    fun updateKeyword(keyword: String) {
        val currentFilter = currentFilterOrNull() ?: return

        loadDocuments(
            currentFilter.copy(
                keyword = keyword.takeIf { it.isNotBlank() }
            )
        )
    }

    fun updateDocumentType(documentType: DocumentType?) {
        val currentFilter = currentFilterOrNull() ?: return

        loadDocuments(
            currentFilter.copy(
                documentType = documentType
            )
        )
    }

    fun updateStatus(status: DocumentStatus?) {
        val currentFilter = currentFilterOrNull() ?: return

        loadDocuments(
            currentFilter.copy(
                status = status
            )
        )
    }

    fun updatePhysicalForm(physicalForm: PhysicalForm?) {
        val currentFilter = currentFilterOrNull() ?: return

        loadDocuments(
            currentFilter.copy(
                physicalForm = physicalForm
            )
        )
    }

    fun updateCondition(condition: DocumentCondition?) {
        val currentFilter = currentFilterOrNull() ?: return

        loadDocuments(
            currentFilter.copy(
                condition = condition
            )
        )
    }

    fun updateOriginInstance(originInstance: String?) {
        val currentFilter = currentFilterOrNull() ?: return

        loadDocuments(
            currentFilter.copy(
                originInstance = originInstance?.takeIf { it.isNotBlank() }
            )
        )
    }

    fun resetFilter() {
        val currentYear = _uiState.value.selectedYear ?: return

        loadDocuments(
            ArchiveDocumentFilter(year = currentYear)
        )
    }

    private fun currentFilterOrNull(): ArchiveDocumentFilter? {
        val currentState = _uiState.value

        return currentState.filter
            ?: currentState.selectedYear?.let { year ->
                ArchiveDocumentFilter(year = year)
            }
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
                val documents = getArchiveDocumentListItemsUseCase(filter)

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