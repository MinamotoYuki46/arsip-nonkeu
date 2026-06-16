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
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArchiveListUiState(
    val isLoading: Boolean = false,
    val selectedYear: Int? = null,
    val documents: List<ArchiveDocumentListItem> = emptyList(),
    val filter: ArchiveDocumentFilter? = null,
    val errorMessage: String? = null
)

class ArchiveViewModel(
    private val repository: ArchiveRepository = ArchiveModule.archiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveListUiState())
    val uiState: StateFlow<ArchiveListUiState> = _uiState.asStateFlow()

    private var observationJob: Job? = null

    fun loadDocumentsByYear(year: Int) {
        val currentFilter = _uiState.value.filter
        val filter = if (currentFilter?.year == year) {
            currentFilter
        } else {
            ArchiveDocumentFilter(year = year)
        }
        
        _uiState.update { it.copy(selectedYear = year, filter = filter) }
        
        // Start observing from local Room
        observeDocuments(year)
        
        // Trigger refresh from Supabase
        refreshDocuments(year)
    }

    private fun observeDocuments(year: Int) {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            repository.observeArchiveDocumentListItems(year).collectLatest { documents ->
                _uiState.update { state ->
                    // Apply local keyword filter if needed, 
                    // though repository should ideally handle basic filter.
                    // For now, simple local filter for keywords if present.
                    val filteredDocs = if (!state.filter?.keyword.isNullOrBlank()) {
                        val kw = state.filter?.keyword?.lowercase() ?: ""
                        documents.filter { 
                            it.document.title.lowercase().contains(kw) || 
                            it.document.documentNumber?.lowercase()?.contains(kw) == true 
                        }
                    } else {
                        documents
                    }

                    state.copy(documents = filteredDocs)
                }
            }
        }
    }

    private fun refreshDocuments(year: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.refreshArchiveDocuments(year)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = e.message ?: "Gagal memperbarui data dari server" 
                    ) 
                }
            }
        }
    }

    fun updateKeyword(keyword: String) {
        val currentFilter = currentFilterOrNull() ?: return
        val newFilter = currentFilter.copy(keyword = keyword.takeIf { it.isNotBlank() })
        _uiState.update { it.copy(filter = newFilter) }
        
        // For keyword, we can just re-filter the current list in UI state
        // or re-observe if repository supports keyword filtering in Flow.
        // For now, our observation block handles keyword.
    }

    fun updateDocumentType(documentType: DocumentType?) {
        val currentFilter = currentFilterOrNull() ?: return
        // Ideally we'd refresh with filter, but for now we refresh the whole year
        _uiState.update { it.copy(filter = currentFilter.copy(documentType = documentType)) }
    }

    fun updateStatus(status: DocumentStatus?) {
        val currentFilter = currentFilterOrNull() ?: return
        _uiState.update { it.copy(filter = currentFilter.copy(status = status)) }
    }

    fun updatePhysicalForm(physicalForm: PhysicalForm?) {
        val currentFilter = currentFilterOrNull() ?: return
        _uiState.update { it.copy(filter = currentFilter.copy(physicalForm = physicalForm)) }
    }

    fun updateCondition(condition: DocumentCondition?) {
        val currentFilter = currentFilterOrNull() ?: return
        _uiState.update { it.copy(filter = currentFilter.copy(condition = condition)) }
    }

    fun updateOriginInstance(originInstance: String?) {
        val currentFilter = currentFilterOrNull() ?: return
        _uiState.update { it.copy(filter = currentFilter.copy(originInstance = originInstance?.takeIf { it.isNotBlank() })) }
    }

    fun resetFilter() {
        val currentYear = _uiState.value.selectedYear ?: return
        _uiState.update { it.copy(filter = ArchiveDocumentFilter(year = currentYear)) }
    }

    private fun currentFilterOrNull(): ArchiveDocumentFilter? {
        val currentState = _uiState.value
        return currentState.filter ?: currentState.selectedYear?.let { year ->
            ArchiveDocumentFilter(year = year)
        }
    }
}
