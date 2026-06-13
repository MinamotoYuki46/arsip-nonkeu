package com.bpkpad.arsipnonkeu.ui.screen.staging

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsipnonkeu.data.repository.StagingDraftRepository
import com.bpkpad.arsipnonkeu.di.ArchiveModule
import com.bpkpad.arsipnonkeu.domain.model.ArchiveClassification
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.domain.model.StagingDraft
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository
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
    val id: String,
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
    private val repository: StagingDraftRepository,
    private val archiveRepository: ArchiveRepository
) : ViewModel() {

    private val _savedDrafts = MutableStateFlow<List<StagingDraft>>(emptyList())
    val savedDrafts: StateFlow<List<StagingDraft>> = _savedDrafts.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val getArchiveClassificationsUseCase =
        ArchiveModule.getArchiveClassificationsUseCase

    private val _uiState = MutableStateFlow(
        StagingUiState(
            documents = listOf(
                StagingDocument(
                    id = "stage-001",
                    documentType = DocumentType.SURAT,
                    documentNumber = "001/UMUM/2025",
                    classificationCode = "000.1.5",
                    title = "Surat Undangan Rapat Koordinasi",
                    description = "Surat undangan rapat koordinasi internal BPKPAD.",
                    year = 2025,
                    physicalForm = PhysicalForm.SHEET,
                    condition = DocumentCondition.GOOD,
                    copyCount = 1,
                    isCopy = false,
                    status = DocumentStatus.AVAILABLE,
                    originInstance = "Bagian Umum",
                    source = StagingDocumentSource.MANUAL
                ),
                StagingDocument(
                    id = "stage-002",
                    documentType = DocumentType.KEPBUP,
                    documentNumber = "188.45/25/KUM/2024",
                    classificationCode = "100.3.3",
                    title = "Keputusan Bupati Tentang Pembentukan Tim Kerja",
                    description = "Dokumen keputusan bupati tentang pembentukan tim kerja daerah.",
                    year = 2024,
                    physicalForm = PhysicalForm.BOOK,
                    condition = DocumentCondition.GOOD,
                    copyCount = 1,
                    isCopy = false,
                    status = DocumentStatus.AVAILABLE,
                    originInstance = "Bagian Hukum",
                    source = StagingDocumentSource.SCAN
                ),
                StagingDocument(
                    id = "stage-003",
                    documentType = DocumentType.PERDA,
                    documentNumber = "12 Tahun 2025",
                    classificationCode = "100.3.2",
                    title = "Peraturan Daerah Tentang Pengelolaan Keuangan Daerah",
                    description = "Dokumen Peraturan Daerah terkait pengelolaan keuangan daerah.",
                    year = 2025,
                    physicalForm = PhysicalForm.BOOK,
                    condition = DocumentCondition.GOOD,
                    copyCount = 2,
                    isCopy = true,
                    status = DocumentStatus.AVAILABLE,
                    originInstance = "Sekretariat Daerah",
                    source = StagingDocumentSource.IMPORT
                )
            )
        )
    )

    val uiState: StateFlow<StagingUiState> = _uiState.asStateFlow()

    init {
        loadArchiveClassifications()
        loadSavedDrafts()
    }

    fun loadSavedDrafts() {
        _savedDrafts.value = repository.getDrafts()
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

    fun saveCurrentStaging(
        archiveCode: String,
        title: String,
        documentType: String,
        physicalForm: String,
        condition: String,
        status: String,
        locationText: String,
        description: String
    ) {
        if (archiveCode.isBlank()) {
            _message.value = "Kode arsip tidak boleh kosong"
            return
        }

        if (title.isBlank()) {
            _message.value = "Judul dokumen tidak boleh kosong"
            return
        }

        val now = System.currentTimeMillis()

        val draft = StagingDraft(
            id = UUID.randomUUID().toString(),
            archiveCode = archiveCode.trim(),
            title = title.trim(),
            documentType = documentType,
            physicalForm = physicalForm,
            condition = condition,
            status = status,
            locationText = locationText.trim(),
            description = description.trim(),
            createdAtMillis = now,
            updatedAtMillis = now
        )

        repository.saveDraft(draft)
        loadSavedDrafts()

        _message.value = "Data staging berhasil disimpan"
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

    fun getFilteredArchiveClassifications(
        keyword: String
    ): List<ArchiveClassification> {
        val normalizedKeyword = keyword.trim().lowercase()
        val classifications = _uiState.value.archiveClassifications

        if (normalizedKeyword.isBlank()) {
            return classifications
        }

        return classifications.filter { classification ->
            classification.code.lowercase().contains(normalizedKeyword) ||
                    classification.name.lowercase().contains(normalizedKeyword) ||
                    classification.parentCode.orEmpty().lowercase().contains(normalizedKeyword)
        }
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

        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents.map { document ->
                if (document.id == selectedDocument.id) {
                    updatedDocument
                } else {
                    document
                }
            },
            selectedDocument = updatedDocument,
            errorMessage = null,
            isSuccess = false
        )
    }

    fun deleteDraft(id: String) {
        repository.deleteDraft(id)
        loadSavedDrafts()

        _message.value = "Draft staging berhasil dihapus"
    }


    fun deleteSelectedDocument() {
        val selectedDocument = _uiState.value.selectedDocument ?: return

        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents.filterNot { document ->
                document.id == selectedDocument.id
            },
            selectedDocument = null,
            errorMessage = null,
            isSuccess = false
        )
    }

    fun deleteDocument(id: String) {
        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents.filterNot { document ->
                document.id == id
            },
            selectedDocument = if (_uiState.value.selectedDocument?.id == id) {
                null
            } else {
                _uiState.value.selectedDocument
            },
            errorMessage = null,
            isSuccess = false
        )
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

        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents + newDocument,
            errorMessage = null,
            isSuccess = false
        )
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

        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents + newDocument,
            selectedDocument = newDocument,
            errorMessage = null,
            isSuccess = false
        )

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

        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents + newDocument,
            selectedDocument = newDocument,
            errorMessage = null,
            isSuccess = false
        )

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

        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents + newDocument,
            errorMessage = null,
            isSuccess = false
        )
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

        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents + newDocument,
            errorMessage = null,
            isSuccess = false
        )
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
                    selectedDocument = null
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

    suspend fun findArchiveClassificationByCode(
        code: String?
    ): ArchiveClassification? {
        if (code.isNullOrBlank()) return null

        return getArchiveClassificationsUseCase.findByCode(
            code.trim()
        )
    }

    fun findLoadedArchiveClassificationByCode(
        code: String?
    ): ArchiveClassification? {
        if (code.isNullOrBlank()) return null

        return _uiState.value.archiveClassifications.firstOrNull { classification ->
            classification.code.equals(
                other = code.trim(),
                ignoreCase = true
            )
        }
    }

    fun getLoadedArchiveClassificationLabel(
        code: String?
    ): String {
        if (code.isNullOrBlank()) return ""

        val classification = findLoadedArchiveClassificationByCode(code)

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