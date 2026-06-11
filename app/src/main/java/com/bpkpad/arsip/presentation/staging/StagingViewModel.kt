package com.bpkpad.arsip.presentation.staging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.bpkpad.arsip.core.domain.model.DocumentType
import com.bpkpad.arsip.core.domain.model.StagingDocument
import com.bpkpad.arsip.core.domain.model.StagingStatus
import com.bpkpad.arsip.core.domain.repository.StagingRepository
import com.bpkpad.arsip.domain.usecase.ExcelImportUseCase
import com.bpkpad.arsip.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class StagingViewModel @Inject constructor(
    private val stagingRepository: StagingRepository,
    private val excelImportUseCase: ExcelImportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StagingUiState())
    val uiState: StateFlow<StagingUiState> = _uiState.asStateFlow()

    init {
        loadStagingDocuments()
    }

    private fun loadStagingDocuments() {
        viewModelScope.launch {
            stagingRepository.getAllStaging().collect { docs ->
                _uiState.update { it.copy(documents = docs) }
            }
        }
    }

    fun addDocument(title: String, type: DocumentType, year: Int) {
        viewModelScope.launch {
            val doc = StagingDocument(
                id = UUID.randomUUID().toString(),
                title = title,
                type = type,
                year = year,
                metadata = emptyMap(),
                coverLocalPath = null,
                status = StagingStatus.LOCAL_ONLY
            )
            stagingRepository.saveToStaging(doc)
        }
    }

    fun onBoxIdChange(id: String) {
        _uiState.update { it.copy(boxId = id) }
    }

    fun onLocationIdChange(id: String) {
        _uiState.update { it.copy(locationId = id) }
    }

    fun importFromExcel(context: Context, uri: Uri) {
        viewModelScope.launch {
            excelImportUseCase(context, uri).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ResultState.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        // Repository should update Flow, UI updates via loadStagingDocuments
                    }
                    is ResultState.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            stagingRepository.deleteFromStaging(id)
        }
    }

    fun pushToCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = stagingRepository.pushAllToCloud(
                locationId = _uiState.value.locationId,
                userId = "admin" // Placeholder
            )
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
