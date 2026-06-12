package com.bpkpad.arsipnonkeu.ui.screen.staging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import android.content.Context
import android.net.Uri
import com.bpkpad.arsipnonkeu.ui.screen.scan.ParsedOcrDocument
import com.bpkpad.arsipnonkeu.util.ArchiveExcelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class StagingDocument(
    val id: String,
    val documentType: DocumentType,
    val documentNumber: String?,
    val documentCode: String?,
    val title: String,
    val description: String?,
    val year: Int,
    val physicalForm: PhysicalForm,
    val condition: DocumentCondition?,
    val copyCount: Int,
    val status: DocumentStatus,
    val originInstance: String?,
    val source: StagingDocumentSource
)

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
    val isLoading: Boolean = false,
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

class StagingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        StagingUiState(
            documents = listOf(
                StagingDocument(
                    id = "stage-001",
                    documentType = DocumentType.SURAT,
                    documentNumber = "001/UMUM/2025",
                    documentCode = "STG-2025-001",
                    title = "Surat Undangan Rapat Koordinasi",
                    description = "Surat undangan rapat koordinasi internal BPKPAD.",
                    year = 2025,
                    physicalForm = PhysicalForm.SHEET,
                    condition = DocumentCondition.GOOD,
                    copyCount = 1,
                    status = DocumentStatus.AVAILABLE,
                    originInstance = "Bagian Umum",
                    source = StagingDocumentSource.MANUAL
                ),
                StagingDocument(
                    id = "stage-002",
                    documentType = DocumentType.KEPBUP,
                    documentNumber = "188.45/25/KUM/2024",
                    documentCode = "STG-2024-001",
                    title = "Keputusan Bupati Tentang Pembentukan Tim Kerja",
                    description = "Dokumen keputusan bupati tentang pembentukan tim kerja daerah.",
                    year = 2024,
                    physicalForm = PhysicalForm.BOOK,
                    condition = DocumentCondition.GOOD,
                    copyCount = 1,
                    status = DocumentStatus.AVAILABLE,
                    originInstance = "Bagian Hukum",
                    source = StagingDocumentSource.SCAN
                ),
                StagingDocument(
                    id = "stage-003",
                    documentType = DocumentType.PERDA,
                    documentNumber = "12 Tahun 2025",
                    documentCode = "STG-2025-002",
                    title = "Peraturan Daerah Tentang Pengelolaan Keuangan Daerah",
                    description = "Dokumen Peraturan Daerah terkait pengelolaan keuangan daerah.",
                    year = 2025,
                    physicalForm = PhysicalForm.BOOK,
                    condition = DocumentCondition.GOOD,
                    copyCount = 2,
                    status = DocumentStatus.AVAILABLE,
                    originInstance = "Sekretariat Daerah",
                    source = StagingDocumentSource.IMPORT
                )
            )
        )
    )

    val uiState: StateFlow<StagingUiState> = _uiState.asStateFlow()

    fun onRoomChange(value: String) {
        _uiState.value = _uiState.value.copy(room = value)
    }

    fun onShelfChange(value: String) {
        _uiState.value = _uiState.value.copy(shelf = value)
    }

    fun onBoxNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(boxNumber = value)
    }

    fun selectDocument(documentId: String) {
        val document = _uiState.value.documents.firstOrNull { it.id == documentId }

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
        documentCode: String?,
        title: String,
        description: String?,
        year: Int,
        physicalForm: PhysicalForm,
        condition: DocumentCondition?,
        copyCount: Int,
        status: DocumentStatus,
        originInstance: String?
    ) {
        val selectedDocument = _uiState.value.selectedDocument ?: return

        val updatedDocument = selectedDocument.copy(
            documentType = documentType,
            documentNumber = documentNumber,
            documentCode = documentCode,
            title = title,
            description = description,
            year = year,
            physicalForm = physicalForm,
            condition = condition,
            copyCount = copyCount,
            status = status,
            originInstance = originInstance
        )

        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents.map { document ->
                if (document.id == selectedDocument.id) updatedDocument else document
            },
            selectedDocument = updatedDocument,
            errorMessage = null
        )
    }

    fun deleteSelectedDocument() {
        val selectedDocument = _uiState.value.selectedDocument ?: return

        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents.filterNot { it.id == selectedDocument.id },
            selectedDocument = null,
            errorMessage = null
        )
    }

    fun deleteDocument(id: String) {
        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents.filterNot { it.id == id },
            errorMessage = null
        )
    }

    fun addDummyImportDocument() {
        val newDocument = StagingDocument(
            id = UUID.randomUUID().toString(),
            documentType = DocumentType.SURAT,
            documentNumber = "IMP-${System.currentTimeMillis()}",
            documentCode = "STG-IMP-${_uiState.value.documents.size + 1}",
            title = "Dokumen Hasil Import Excel",
            description = "Dokumen sementara hasil import Excel.",
            year = 2025,
            physicalForm = PhysicalForm.SHEET,
            condition = DocumentCondition.GOOD,
            copyCount = 1,
            status = DocumentStatus.AVAILABLE,
            originInstance = "Import Excel",
            source = StagingDocumentSource.IMPORT
        )

        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents + newDocument,
            errorMessage = null
        )
    }

    fun addDummyScanDocument() {
        val newDocument = StagingDocument(
            id = UUID.randomUUID().toString(),
            documentType = DocumentType.SURAT,
            documentNumber = "SCAN-${System.currentTimeMillis()}",
            documentCode = "STG-SCAN-${_uiState.value.documents.size + 1}",
            title = "Dokumen Hasil Scan",
            description = "Dokumen sementara hasil scan.",
            year = 2025,
            physicalForm = PhysicalForm.SHEET,
            condition = null,
            copyCount = 1,
            status = DocumentStatus.AVAILABLE,
            originInstance = "Hasil Scan",
            source = StagingDocumentSource.SCAN
        )

        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents + newDocument,
            errorMessage = null
        )
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

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            // TODO: nanti insert:
            // 1. storage_locations
            // 2. archive_documents
            // 3. document_placements
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isSuccess = true,
                documents = emptyList(),
                selectedDocument = null
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun addManualDocument(
        documentType: DocumentType,
        documentNumber: String?,
        documentCode: String?,
        title: String,
        description: String?,
        year: Int,
        physicalForm: PhysicalForm,
        condition: DocumentCondition?,
        copyCount: Int,
        status: DocumentStatus,
        originInstance: String?
    ) {
        val newDocument = StagingDocument(
            id = UUID.randomUUID().toString(),
            documentType = documentType,
            documentNumber = documentNumber,
            documentCode = documentCode,
            title = title,
            description = description,
            year = year,
            physicalForm = physicalForm,
            condition = condition,
            copyCount = copyCount,
            status = status,
            originInstance = originInstance,
            source = StagingDocumentSource.MANUAL
        )

        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents + newDocument,
            errorMessage = null
        )
    }

    fun addScannedDocument(
        documentType: DocumentType
    ): String {
        val newDocument = StagingDocument(
            id = UUID.randomUUID().toString(),
            documentType = documentType,
            documentNumber = "SCAN-${System.currentTimeMillis()}",
            documentCode = "STG-SCAN-${_uiState.value.documents.size + 1}",
            title = "Dokumen Hasil Scan",
            description = "Dokumen sementara hasil OCR dan AI parsing. Silakan cek dan edit hasilnya.",
            year = 2025,
            physicalForm = PhysicalForm.SHEET,
            condition = null,
            copyCount = 1,
            status = DocumentStatus.AVAILABLE,
            originInstance = "Hasil Scan",
            source = StagingDocumentSource.SCAN
        )

        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents + newDocument,
            selectedDocument = newDocument,
            errorMessage = null
        )

        return newDocument.id
    }

    fun addScannedParsedDocument(
        parsedDocument: ParsedOcrDocument
    ): String {
        val newDocument = StagingDocument(
            id = UUID.randomUUID().toString(),
            documentType = parsedDocument.documentType,
            documentNumber = parsedDocument.documentNumber,
            documentCode = parsedDocument.documentCode,
            title = parsedDocument.title,
            description = parsedDocument.description,
            year = parsedDocument.year,
            physicalForm = parsedDocument.physicalForm,
            condition = parsedDocument.condition,
            copyCount = parsedDocument.copyCount,
            status = parsedDocument.status,
            originInstance = parsedDocument.originInstance,
            source = StagingDocumentSource.SCAN
        )

        _uiState.value = _uiState.value.copy(
            documents = _uiState.value.documents + newDocument,
            selectedDocument = newDocument,
            errorMessage = null
        )

        return newDocument.id
    }

    fun importFromExcel(
        context: Context,
        uri: Uri
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val importedDocuments = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    ArchiveExcelService.importStagingDocuments(
                        context = context,
                        uri = uri
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
}