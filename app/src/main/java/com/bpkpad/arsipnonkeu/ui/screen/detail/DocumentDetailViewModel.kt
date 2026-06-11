package com.bpkpad.arsipnonkeu.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsipnonkeu.di.ArchiveModule
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentListItem
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveDocumentDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DocumentDetailUiState(
    val isLoading: Boolean = false,
    val item: ArchiveDocumentListItem? = null,
    val errorMessage: String? = null,
    val isDeleted: Boolean = false,
    val successMessage: String? = null
)

class DocumentDetailViewModel(
    private val getArchiveDocumentDetailUseCase: GetArchiveDocumentDetailUseCase =
        ArchiveModule.getArchiveDocumentDetailUseCase,
    private val repository: ArchiveRepository =
        ArchiveModule.archiveRepositoryInstance
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentDetailUiState())
    val uiState: StateFlow<DocumentDetailUiState> = _uiState.asStateFlow()

    fun loadDocument(documentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                val item = getArchiveDocumentDetailUseCase(documentId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    item = item,
                    errorMessage = if (item == null) {
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

    fun updateDocument(
        updatedDocument: ArchiveDocument
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                repository.updateArchiveDocument(updatedDocument)

                val updatedItem = getArchiveDocumentDetailUseCase(updatedDocument.id)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    item = updatedItem,
                    successMessage = "Dokumen berhasil diperbarui"
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Gagal memperbarui dokumen"
                )
            }
        }
    }

    fun deleteDocument() {
        val documentId = _uiState.value.item?.document?.id ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                repository.deleteArchiveDocument(documentId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isDeleted = true,
                    successMessage = "Dokumen berhasil dihapus"
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Gagal menghapus dokumen"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}