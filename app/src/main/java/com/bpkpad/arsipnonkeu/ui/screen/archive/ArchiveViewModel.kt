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
import com.bpkpad.arsipnonkeu.utils.ErrorHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    private val repository: ArchiveRepository = ArchiveModule.archiveRepositoryInstance
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveListUiState())
    val uiState: StateFlow<ArchiveListUiState> = _uiState.asStateFlow()

    private val _filter = MutableStateFlow<ArchiveDocumentFilter?>(null)
    
    init {
        observeDocuments()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDocuments() {
        viewModelScope.launch {
            _filter.flatMapLatest { filter ->
                if (filter == null) {
                    flowOf(emptyList<ArchiveDocumentListItem>())
                } else {
                    combine(
                        repository.observeArchiveDocumentListItems(filter.year),
                        flowOf(filter)
                    ) { documents, currentFilter ->
                        filterDocuments(documents, currentFilter)
                    }
                }
            }.collectLatest { filteredDocuments ->
                _uiState.update { it.copy(documents = filteredDocuments) }
            }
        }
    }

    private fun filterDocuments(
        documents: List<ArchiveDocumentListItem>,
        filter: ArchiveDocumentFilter
    ): List<ArchiveDocumentListItem> {
        return documents.filter { item ->
            val doc = item.document
            
            val matchesKeyword = if (!filter.keyword.isNullOrBlank()) {
                val kw = filter.keyword.trim().lowercase()
                doc.title.lowercase().contains(kw) || 
                doc.documentNumber?.lowercase()?.contains(kw) == true ||
                doc.classificationCode?.lowercase()?.contains(kw) == true ||
                doc.originInstance?.lowercase()?.contains(kw) == true ||
                doc.description?.lowercase()?.contains(kw) == true
            } else true

            val matchesType = filter.documentType == null || doc.documentType == filter.documentType
            val matchesStatus = filter.status == null || doc.status == filter.status
            val matchesPhysicalForm = filter.physicalForm == null || doc.physicalForm == filter.physicalForm
            val matchesCondition = filter.condition == null || doc.condition == filter.condition
            val matchesOrigin = filter.originInstance == null || 
                               doc.originInstance?.equals(filter.originInstance, ignoreCase = true) == true

            matchesKeyword && matchesType && matchesStatus && matchesPhysicalForm && matchesCondition && matchesOrigin
        }
    }

    fun loadDocumentsByYear(year: Int) {
        val currentFilter = _filter.value
        val filter = if (currentFilter?.year == year) {
            currentFilter
        } else {
            ArchiveDocumentFilter(year = year)
        }
        
        _filter.value = filter
        _uiState.update { it.copy(selectedYear = year, filter = filter) }
        
        // Trigger refresh from Supabase
        refreshDocuments(year)
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
                        errorMessage = ErrorHandler.getErrorMessage(e)
                    ) 
                }
            }
        }
    }

    fun updateKeyword(keyword: String) {
        val currentFilter = currentFilterOrNull() ?: return
        val newFilter = currentFilter.copy(keyword = keyword.takeIf { it.isNotBlank() })
        _filter.value = newFilter
        _uiState.update { it.copy(filter = newFilter) }
    }

    fun updateDocumentType(documentType: DocumentType?) {
        val currentFilter = currentFilterOrNull() ?: return
        val newFilter = currentFilter.copy(documentType = documentType)
        _filter.value = newFilter
        _uiState.update { it.copy(filter = newFilter) }
    }

    fun updateStatus(status: DocumentStatus?) {
        val currentFilter = currentFilterOrNull() ?: return
        val newFilter = currentFilter.copy(status = status)
        _filter.value = newFilter
        _uiState.update { it.copy(filter = newFilter) }
    }

    fun updatePhysicalForm(physicalForm: PhysicalForm?) {
        val currentFilter = currentFilterOrNull() ?: return
        val newFilter = currentFilter.copy(physicalForm = physicalForm)
        _filter.value = newFilter
        _uiState.update { it.copy(filter = newFilter) }
    }

    fun updateCondition(condition: DocumentCondition?) {
        val currentFilter = currentFilterOrNull() ?: return
        val newFilter = currentFilter.copy(condition = condition)
        _filter.value = newFilter
        _uiState.update { it.copy(filter = newFilter) }
    }

    fun updateOriginInstance(originInstance: String?) {
        val currentFilter = currentFilterOrNull() ?: return
        val newFilter = currentFilter.copy(originInstance = originInstance?.takeIf { it.isNotBlank() })
        _filter.value = newFilter
        _uiState.update { it.copy(filter = newFilter) }
    }

    fun resetFilter() {
        val currentYear = _uiState.value.selectedYear ?: return
        val newFilter = ArchiveDocumentFilter(year = currentYear)
        _filter.value = newFilter
        _uiState.update { it.copy(filter = newFilter) }
    }

    private fun currentFilterOrNull(): ArchiveDocumentFilter? {
        return _filter.value ?: _uiState.value.selectedYear?.let { year ->
            ArchiveDocumentFilter(year = year)
        }
    }
}
