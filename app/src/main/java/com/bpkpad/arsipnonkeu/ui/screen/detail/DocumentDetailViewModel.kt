package com.bpkpad.arsipnonkeu.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsipnonkeu.di.ArchiveModule
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveDocumentDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ArchiveDetailUiState(
    val isLoading: Boolean = false,
    val document: ArchiveDocument? = null,
    val errorMessage: String? = null
)


class DocumentDetailViewModel(
    private val getArchiveDocumentDetailUseCase: GetArchiveDocumentDetailUseCase =
        ArchiveModule.getArchiveDocumentDetailUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArchiveDetailUiState())
    val uiState: StateFlow<ArchiveDetailUiState> = _uiState.asStateFlow()

    fun loadDocument(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val document = getArchiveDocumentDetailUseCase(id)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    document = document,
                    errorMessage = if (document == null) {
                        "Dokumen tidak ditemukan"
                    } else {
                        null
                    }
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Gagal memuat detail dokumen"
                )
            }
        }
    }
}