package com.bpkpad.arsip.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsip.core.domain.model.DocumentType
import com.bpkpad.arsip.domain.usecase.ExtractTextWithMlKitUseCase
import com.bpkpad.arsip.domain.usecase.ParseMetadataWithAiUseCase
import com.bpkpad.arsip.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val extractTextUseCase: ExtractTextWithMlKitUseCase,
    private val parseMetadataUseCase: ParseMetadataWithAiUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun onImageCaptured(imageBytes: ByteArray, type: DocumentType) {
        viewModelScope.launch {
            extractTextUseCase(imageBytes).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is ResultState.Success -> {
                        parseMetadata(result.data, type)
                    }
                    is ResultState.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }

    private fun parseMetadata(rawText: String, type: DocumentType) {
        viewModelScope.launch {
            parseMetadataUseCase(rawText, type).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ResultState.Success -> {
                        _uiState.update { it.copy(isLoading = false, parsedDocument = result.data) }
                    }
                    is ResultState.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }
    
    fun clearState() {
        _uiState.update { ScanUiState() }
    }
}
