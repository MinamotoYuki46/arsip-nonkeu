package com.bpkpad.arsipnonkeu.ui.screen.staging

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsipnonkeu.di.ArchiveModule
import com.bpkpad.arsipnonkeu.domain.model.ArchiveClassification
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository
import com.bpkpad.arsipnonkeu.domain.repository.StagingRepository
import com.bpkpad.arsipnonkeu.ui.screen.scan.ParsedOcrDocument
import com.bpkpad.arsipnonkeu.util.ArchiveExcelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class StagingDocument(
    val id: String = "",
    val documentType: DocumentType,
    val documentNumber: String?,
    val classificationCode: String?,
    val title: String,
    val description: String?,
    val year: Int,
    val physicalForm: PhysicalForm,
    val condition: DocumentCondition?,
    val copyCount: Int,
    val isCopy: Boolean?,
    val status: DocumentStatus,
    val originInstance: String?,
    val source: StagingDocumentSource
) {
    fun toArchiveDocument(): ArchiveDocument {
        return ArchiveDocument(
            id = id,
            documentType = documentType,
            documentNumber = documentNumber,
            classificationCode = classificationCode,
            title = title,
            description = description,
            year = year,
            physicalForm = physicalForm,
            condition = condition,
            copyCount = copyCount,
            isCopy = isCopy,
            status = status,
            originInstance = originInstance,
            createdBy = "user-current",
            updatedBy = null,
            createdAt = null,
            updatedAt = null,
            deletedAt = null
        )
    }
}

enum class StagingDocumentSource(val label: String) {
    MANUAL("Manual"),
    SCAN("Scan"),
    IMPORT("Import")
}

data class StagingUiState(
    val documents: List<StagingDocument> = emptyList(),

    val room: String = "",
    val shelf: String = "",
    val boxNumber: String = "",

    val selectedDocument: StagingDocument? = null,

    val archiveClassifications: List<ArchiveClassification> = emptyList(),
    val classificationKeyword: String = "",

    val isLoading: Boolean = false,
    val isClassificationLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val isStorageLocationValid: Boolean
        get() = room.isNotBlank() && shelf.isNotBlank()

    val storageLocationLabel: String
        get() {
            val parts = buildList {
                if (room.isNotBlank()) add(room)
                if (shelf.isNotBlank()) add(shelf)
                if (boxNumber.isNotBlank()) add("Box $boxNumber")
            }

            return if (parts.isEmpty()) {
                "Belum ditentukan"
            } else {
                parts.joinToString(" / ")
            }
        }
}

class StagingViewModel(
    private val stagingRepository: StagingRepository,
    private val archiveRepository: ArchiveRepository
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val getArchiveClassificationsUseCase =
        ArchiveModule.getArchiveClassificationsUseCase

    private val _uiState = MutableStateFlow(StagingUiState())
    val uiState: StateFlow<StagingUiState> = _uiState.asStateFlow()

    init {
        loadArchiveClassifications()
        loadStagingDocuments()
    }

    private fun loadStagingDocuments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val docs = stagingRepository.getStagingDocuments()
                _uiState.value = _uiState.value.copy(
                    documents = docs,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Gagal memuat dokumen staging"
                )
            }
        }
    }

    fun onRoomChange(value: String) {
        _uiState.value = _uiState.value.copy(
            room = value,
            isSuccess = false
        )
    }

    fun onShelfChange(value: String) {
        _uiState.value = _uiState.value.copy(
            shelf = value,
            isSuccess = false
        )
    }

    fun onBoxNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(
            boxNumber = value,
            isSuccess = false
        )
    }

    fun selectDocument(documentId: String) {
        val document = _uiState.value.documents.firstOrNull { document ->
            document.id == documentId
        }

        _uiState.value = _uiState.value.copy(
            selectedDocument = document
        )
    }

    fun clearSelectedDocument() {
        _uiState.value = _uiState.value.copy(
            selectedDocument = null
        )
    }

    fun updateSelectedDocument(
        documentType: DocumentType,
        documentNumber: String?,
        classificationCode: String?,
        title: String,
        description: String?,
        year: Int,
        physicalForm: PhysicalForm,
        condition: DocumentCondition?,
        copyCount: Int,
        isCopy: Boolean?,
        status: DocumentStatus,
        originInstance: String?
    ) {
        val selectedDocument = _uiState.value.selectedDocument ?: return

        val updatedDocument = selectedDocument.copy(
            documentType = documentType,
            documentNumber = documentNumber.cleanTextOrNull(),
            classificationCode = classificationCode.cleanTextOrNull(),
            title = title.trim(),
            description = description.cleanTextOrNull(),
            year = year,
            physicalForm = physicalForm,
            condition = condition,
            copyCount = copyCount.coerceAtLeast(1),
            isCopy = isCopy,
            status = status,
            originInstance = originInstance.cleanTextOrNull()
        )

        viewModelScope.launch {
            try {
                stagingRepository.upsertStagingDocument(updatedDocument)
                _uiState.value = _uiState.value.copy(
                    documents = _uiState.value.documents.map { document ->
                        if (document.id == selectedDocument.id) updatedDocument else document
                    },
                    selectedDocument = updatedDocument,
                    errorMessage = null,
                    isSuccess = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Gagal memperbarui dokumen"
                )
            }
        }
    }

    fun deleteSelectedDocument() {
        val selectedDocument = _uiState.value.selectedDocument ?: return
        deleteDocument(selectedDocument.id)
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            try {
                stagingRepository.deleteStagingDocument(id)
                _uiState.value = _uiState.value.copy(
                    documents = _uiState.value.documents.filterNot { it.id == id },
                    selectedDocument = if (_uiState.value.selectedDocument?.id == id) null else _uiState.value.selectedDocument,
                    errorMessage = null,
                    isSuccess = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Gagal menghapus dokumen"
                )
            }
        }
    }

    fun addManualDocument(
        documentType: DocumentType,
        documentNumber: String?,
        classificationCode: String?,
        title: String,
        description: String?,
        year: Int,
        physicalForm: PhysicalForm,
        condition: DocumentCondition?,
        copyCount: Int,
        isCopy: Boolean?,
        status: DocumentStatus,
        originInstance: String?
    ) {
        val newDocument = StagingDocument(
            id = UUID.randomUUID().toString(),
            documentType = documentType,
            documentNumber = documentNumber.cleanTextOrNull(),
            classificationCode = classificationCode.cleanTextOrNull(),
            title = title.trim(),
            description = description.cleanTextOrNull(),
            year = year,
            physicalForm = physicalForm,
            condition = condition,
            copyCount = copyCount.coerceAtLeast(1),
            isCopy = isCopy,
            status = status,
            originInstance = originInstance.cleanTextOrNull(),
            source = StagingDocumentSource.MANUAL
        )

        viewModelScope.launch {
            try {
                stagingRepository.upsertStagingDocument(newDocument)
                _uiState.value = _uiState.value.copy(
                    documents = _uiState.value.documents + newDocument,
                    errorMessage = null,
                    isSuccess = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Gagal menambah dokumen"
                )
            }
        }
    }

    fun addScannedDocument(
        documentType: DocumentType
    ): String {
        val newDocument = StagingDocument(
            id = UUID.randomUUID().toString(),
            documentType = documentType,
            documentNumber = "SCAN-${System.currentTimeMillis()}",
            classificationCode = null,
            title = "Dokumen Hasil Scan",
            description = "Dokumen sementara hasil OCR dan AI parsing. Silakan cek dan edit hasilnya.",
            year = 2025,
            physicalForm = if (documentType == DocumentType.SURAT) {
                PhysicalForm.SHEET
            } else {
                PhysicalForm.BOOK
            },
            condition = null,
            copyCount = 1,
            isCopy = null,
            status = DocumentStatus.AVAILABLE,
            originInstance = "Hasil Scan",
            source = StagingDocumentSource.SCAN
        )

        viewModelScope.launch {
            try {
                stagingRepository.upsertStagingDocument(newDocument)
                _uiState.value = _uiState.value.copy(
                    documents = _uiState.value.documents + newDocument,
                    selectedDocument = newDocument,
                    errorMessage = null,
                    isSuccess = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Gagal menambah dokumen scan"
                )
            }
        }

        return newDocument.id
    }

    fun addScannedParsedDocument(
        parsedDocument: ParsedOcrDocument
    ): String {
        val validClassificationCode = normalizeClassificationCode(
            parsedDocument.classificationCode
        )

        val newDocument = StagingDocument(
            id = UUID.randomUUID().toString(),
            documentType = parsedDocument.documentType,
            documentNumber = parsedDocument.documentNumber.cleanTextOrNull(),
            classificationCode = validClassificationCode,
            title = parsedDocument.title.trim().ifBlank { "Dokumen Hasil Scan" },
            description = parsedDocument.description.cleanTextOrNull(),
            year = parsedDocument.year,
            physicalForm = parsedDocument.physicalForm,
            condition = parsedDocument.condition,
            copyCount = parsedDocument.copyCount.coerceAtLeast(1),
            isCopy = parsedDocument.isCopy,
            status = parsedDocument.status,
            originInstance = parsedDocument.originInstance.cleanTextOrNull(),
            source = StagingDocumentSource.SCAN
        )

        viewModelScope.launch {
            try {
                stagingRepository.upsertStagingDocument(newDocument)
                _uiState.value = _uiState.value.copy(
                    documents = _uiState.value.documents + newDocument,
                    selectedDocument = newDocument,
                    errorMessage = null,
                    isSuccess = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Gagal menambah dokumen scan terurai"
                )
            }
        }

        return newDocument.id
    }

    fun addDummyImportDocument() {
        val newDocument = StagingDocument(
            id = UUID.randomUUID().toString(),
            documentType = DocumentType.SURAT,
            documentNumber = "IMP-${System.currentTimeMillis()}",
            classificationCode = "000.5.3",
            title = "Dokumen Hasil Import Excel",
            description = "Dokumen sementara hasil import Excel.",
            year = 2025,
            physicalForm = PhysicalForm.SHEET,
            condition = DocumentCondition.GOOD,
            copyCount = 1,
            isCopy = false,
            status = DocumentStatus.AVAILABLE,
            originInstance = "Import Excel",
            source = StagingDocumentSource.IMPORT
        )

        viewModelScope.launch {
            try {
                stagingRepository.upsertStagingDocument(newDocument)
                _uiState.value = _uiState.value.copy(
                    documents = _uiState.value.documents + newDocument,
                    errorMessage = null,
                    isSuccess = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Gagal menambah dokumen dummy import"
                )
            }
        }
    }

    fun addDummyScanDocument() {
        val newDocument = StagingDocument(
            id = UUID.randomUUID().toString(),
            documentType = DocumentType.SURAT,
            documentNumber = "SCAN-${System.currentTimeMillis()}",
            classificationCode = null,
            title = "Dokumen Hasil Scan",
            description = "Dokumen sementara hasil scan.",
            year = 2025,
            physicalForm = PhysicalForm.SHEET,
            condition = null,
            copyCount = 1,
            isCopy = null,
            status = DocumentStatus.AVAILABLE,
            originInstance = "Hasil Scan",
            source = StagingDocumentSource.SCAN
        )

        viewModelScope.launch {
            try {
                stagingRepository.upsertStagingDocument(newDocument)
                _uiState.value = _uiState.value.copy(
                    documents = _uiState.value.documents + newDocument,
                    errorMessage = null,
                    isSuccess = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Gagal menambah dokumen dummy scan"
                )
            }
        }
    }

    fun importFromExcel(
        context: Context,
        uri: Uri
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isSuccess = false
            )

            try {
                val importedDocuments = withContext(Dispatchers.IO) {
                    ArchiveExcelService.importStagingDocuments(
                        context = context,
                        uri = uri
                    )
                }.map { document ->
                    document.copy(
                        classificationCode = normalizeClassificationCode(
                            document.classificationCode
                        )
                    )
                }

                // Persist each imported document
                importedDocuments.forEach {
                    stagingRepository.upsertStagingDocument(it)
                }

                _uiState.value = _uiState.value.copy(
                    documents = _uiState.value.documents + importedDocuments,
                    isLoading = false,
                    errorMessage = if (importedDocuments.isEmpty()) {
                        "File Excel berhasil dibaca, tetapi tidak ada data dokumen yang ditemukan."
                    } else {
                        null
                    }
                )
            } catch (throwable: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Gagal mengimpor file Excel"
                )
            }
        }
    }

    fun pushAllToArchive() {
        val currentState = _uiState.value

        if (!currentState.isStorageLocationValid) {
            _uiState.value = currentState.copy(
                errorMessage = "Ruangan dan rak wajib diisi sebelum menyimpan staging."
            )
            return
        }

        if (currentState.documents.isEmpty()) {
            _uiState.value = currentState.copy(
                errorMessage = "Tidak ada dokumen staging yang bisa disimpan."
            )
            return
        }

        val invalidDocument = currentState.documents.firstOrNull { document ->
            document.title.isBlank() ||
                    document.year !in 1900..2100 ||
                    document.copyCount <= 0
        }

        if (invalidDocument != null) {
            _uiState.value = currentState.copy(
                selectedDocument = invalidDocument,
                errorMessage = "Ada dokumen staging yang belum valid. Periksa judul, tahun, dan jumlah salinan."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isSuccess = false
            )

            try {
                val archiveDocuments = currentState.documents.map { it.toArchiveDocument() }

                archiveRepository.saveStagingDocuments(
                    documents = archiveDocuments,
                    room = currentState.room,
                    shelf = currentState.shelf,
                    boxNumber = currentState.boxNumber.takeIf { it.isNotBlank() }
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    documents = emptyList(),
                    selectedDocument = null,
                    room = "",
                    shelf = "",
                    boxNumber = ""
                )
            } catch (throwable: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Gagal menyimpan ke arsip utama"
                )
            }
        }
    }

    fun loadArchiveClassifications(
        keyword: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                classificationKeyword = keyword,
                isClassificationLoading = true
            )

            try {
                val classifications = getArchiveClassifications(keyword)

                _uiState.value = _uiState.value.copy(
                    archiveClassifications = classifications,
                    isClassificationLoading = false
                )
            } catch (throwable: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isClassificationLoading = false,
                    errorMessage = throwable.message ?: "Gagal memuat kode klasifikasi arsip"
                )
            }
        }
    }

    fun onClassificationKeywordChange(
        keyword: String
    ) {
        loadArchiveClassifications(keyword)
    }

    suspend fun getArchiveClassifications(
        keyword: String = ""
    ): List<ArchiveClassification> {
        return getArchiveClassificationsUseCase(keyword)
    }

    fun getLoadedArchiveClassificationLabel(
        code: String?
    ): String {
        if (code.isNullOrBlank()) return ""

        val classification = _uiState.value.archiveClassifications.firstOrNull { classification ->
            classification.code.equals(
                other = code.trim(),
                ignoreCase = true
            )
        }

        return classification?.displayName ?: code
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            isSuccess = false
        )
    }

    private fun normalizeClassificationCode(
        code: String?
    ): String? {
        val cleanedCode = code.cleanTextOrNull() ?: return null

        val loadedMatch = _uiState.value.archiveClassifications.firstOrNull { classification ->
            classification.code.equals(
                other = cleanedCode,
                ignoreCase = true
            )
        }

        if (loadedMatch != null) {
            return loadedMatch.code
        }

        return cleanedCode
    }

    private fun String?.cleanTextOrNull(): String? {
        return this
            ?.trim()
            ?.takeIf { value ->
                value.isNotBlank() &&
                        !value.equals("null", ignoreCase = true)
            }
    }
}
