package com.bpkpad.arsipnonkeu.ui.screen.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScanUiState(
    val selectedDocumentType: DocumentType = DocumentType.SURAT,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val scanCompleted: Boolean = false
)

class ScanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun onDocumentTypeChange(documentType: DocumentType) {
        _uiState.value = _uiState.value.copy(
            selectedDocumentType = documentType,
            errorMessage = null
        )
    }

    fun processCapturedImage(
        imageBytes: ByteArray,
        onParsed: (DocumentType) -> Unit
    ) {
        if (_uiState.value.isProcessing) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                errorMessage = null,
                scanCompleted = false
            )

            try {
                // TODO nanti:
                // 1. kirim imageBytes ke ML Kit OCR
                // 2. ambil raw text OCR
                // 3. enhance / parse dengan AI berdasarkan selectedDocumentType
                // 4. hasil parsing dikonversi jadi StagingDocument
                delay(1200)

                onParsed(_uiState.value.selectedDocumentType)

                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    scanCompleted = true
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = exception.message ?: "Gagal memproses hasil scan"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}