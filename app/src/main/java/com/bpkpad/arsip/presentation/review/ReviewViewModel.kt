package com.bpkpad.arsip.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsip.core.domain.model.DocumentType
import com.bpkpad.arsip.core.domain.model.StagingDocument
import com.bpkpad.arsip.core.domain.model.StagingStatus
import com.bpkpad.arsip.core.domain.repository.StagingRepository
import com.bpkpad.arsip.domain.model.ArchiveDocument
import com.bpkpad.arsip.domain.usecase.GetArchiveByIdUseCase
import com.bpkpad.arsip.domain.usecase.SaveArchiveUseCase
import com.bpkpad.arsip.domain.usecase.UploadCoverImageUseCase
import com.bpkpad.arsip.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val uploadCoverImageUseCase: UploadCoverImageUseCase,
    private val saveArchiveUseCase: SaveArchiveUseCase,
    private val getArchiveByIdUseCase: GetArchiveByIdUseCase,
    private val stagingRepository: StagingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun setDocument(document: ArchiveDocument) {
        _uiState.update { it.copy(document = document) }
    }

    fun loadFromStaging(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val stagingDocs = stagingRepository.getAllStaging().first()
            val doc = stagingDocs.find { it.id == id }
            if (doc != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        document = ArchiveDocument(
                            id = doc.id,
                            title = doc.title,
                            type = doc.type.name,
                            date = System.currentTimeMillis(), // Placeholder
                            description = doc.metadata["description"] ?: "",
                            boxId = "", 
                            locationId = ""
                        )
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Document not found") }
            }
        }
    }

    fun loadFromArchive(id: String) {
        viewModelScope.launch {
            getArchiveByIdUseCase(id).collect { result ->
                when (result) {
                    is ResultState.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is ResultState.Success -> _uiState.update { it.copy(isLoading = false, document = result.data) }
                    is ResultState.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun deleteFromArchive() {
        // We'll need a delete usecase eventually, but for now we can use repo directly if available
        // or add a placeholder since we only have save/get in repo.
        // Assuming archiveDao.softDelete is in repository.
    }

    fun updateInStaging() {
        val currentDoc = _uiState.value.document ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val type = try {
                DocumentType.valueOf(currentDoc.type)
            } catch (e: Exception) {
                DocumentType.SURAT
            }

            val stagingDoc = StagingDocument(
                id = currentDoc.id,
                title = currentDoc.title,
                type = type,
                year = 2024, // Simplified
                metadata = mapOf("description" to currentDoc.description),
                coverLocalPath = null,
                status = StagingStatus.LOCAL_ONLY
            )

            stagingRepository.updateStaging(stagingDoc).onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteFromStaging() {
        val id = _uiState.value.document?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            stagingRepository.deleteFromStaging(id)
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { state ->
            state.copy(document = state.document?.copy(title = title))
        }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { state ->
            state.copy(document = state.document?.copy(description = description))
        }
    }

    fun onTypeChange(type: String) {
        _uiState.update { state ->
            state.copy(document = state.document?.copy(type = type))
        }
    }

    fun saveDocument(imageBytes: ByteArray?) {
        val currentDoc = _uiState.value.document ?: return
        
        viewModelScope.launch {
            if (imageBytes != null) {
                uploadImageAndSave(currentDoc, imageBytes)
            } else {
                saveDocumentToBackend(currentDoc)
            }
        }
    }

    private suspend fun uploadImageAndSave(document: ArchiveDocument, imageBytes: ByteArray) {
        uploadCoverImageUseCase(imageBytes).collect { result ->
            when (result) {
                is ResultState.Loading -> {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                is ResultState.Success -> {
                    saveDocumentToBackend(document.copy(imageUrl = result.data))
                }
                is ResultState.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    private suspend fun saveDocumentToBackend(document: ArchiveDocument) {
        saveArchiveUseCase(document).collect { result ->
            when (result) {
                is ResultState.Loading -> {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                is ResultState.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is ResultState.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
}
