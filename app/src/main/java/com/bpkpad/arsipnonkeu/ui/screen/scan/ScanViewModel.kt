package com.bpkpad.arsipnonkeu.ui.screen.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.util.AiOcrParserService
import com.bpkpad.arsipnonkeu.util.MlKitOcrService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ScanUiState(
    val selectedDocumentType: DocumentType = DocumentType.SURAT,
    val isProcessing: Boolean = false,
    val rawOcrText: String = "",
    val errorMessage: String? = null,
    val scanCompleted: Boolean = false
)

class ScanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun onDocumentTypeChange(
        documentType: DocumentType
    ) {
        _uiState.value = _uiState.value.copy(
            selectedDocumentType = documentType,
            errorMessage = null,
            scanCompleted = false
        )
    }

    fun processCapturedImage(
        imageBytes: ByteArray,
        onParsed: (ParsedOcrDocument) -> Unit
    ) {
        if (_uiState.value.isProcessing) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                rawOcrText = "",
                errorMessage = null,
                scanCompleted = false
            )

            try {
                val selectedType = _uiState.value.selectedDocumentType

                val rawOcrText = withContext(Dispatchers.IO) {
                    MlKitOcrService.recognizeText(imageBytes)
                }

                if (rawOcrText.isBlank()) {
                    error("OCR tidak menemukan teks. Coba foto ulang dengan pencahayaan lebih baik.")
                }

                _uiState.value = _uiState.value.copy(
                    rawOcrText = rawOcrText
                )

                val parsedDocument = try {
                    withContext(Dispatchers.IO) {
                        AiOcrParserService.parseOcrText(
                            documentType = selectedType,
                            rawOcrText = rawOcrText
                        )
                    }
                } catch (_: Throwable) {
                    AiOcrParserService.createFallbackDocument(
                        documentType = selectedType,
                        rawOcrText = rawOcrText
                    )
                }

                onParsed(parsedDocument)

                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    scanCompleted = true
                )
            } catch (throwable: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = throwable.message ?: "Gagal memproses hasil scan"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }
}