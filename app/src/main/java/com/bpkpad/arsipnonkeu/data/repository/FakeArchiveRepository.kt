package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentFilter
import com.bpkpad.arsipnonkeu.domain.model.ArchiveYearSummary
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository

class FakeArchiveRepository : ArchiveRepository {

    private val documents = mutableListOf(
        ArchiveDocument(
            id = "doc-001",
            documentType = DocumentType.SURAT,
            documentNumber = "001/UMUM/2025",
            documentCode = "ARS-2025-001",
            title = "Surat Undangan Rapat Koordinasi",
            description = "Surat undangan rapat koordinasi internal BPKPAD.",
            year = 2025,
            physicalForm = PhysicalForm.SHEET,
            condition = DocumentCondition.GOOD,
            copyCount = 1,
            status = DocumentStatus.AVAILABLE,
            originInstance = "Bagian Umum",
            createdBy = "user-001",
            updatedBy = null,
            createdAt = "2025-01-10",
            updatedAt = null,
            deletedAt = null
        ),
        ArchiveDocument(
            id = "doc-002",
            documentType = DocumentType.PERDA,
            documentNumber = "12 Tahun 2025",
            documentCode = "ARS-2025-002",
            title = "Peraturan Daerah Tentang Pengelolaan Keuangan Daerah",
            description = "Dokumen Peraturan Daerah terkait pengelolaan keuangan daerah.",
            year = 2025,
            physicalForm = PhysicalForm.BOOK,
            condition = DocumentCondition.GOOD,
            copyCount = 2,
            status = DocumentStatus.AVAILABLE,
            originInstance = "Sekretariat Daerah",
            createdBy = "user-001",
            updatedBy = null,
            createdAt = "2025-02-14",
            updatedAt = null,
            deletedAt = null
        ),
        ArchiveDocument(
            id = "doc-003",
            documentType = DocumentType.KEPBUP,
            documentNumber = "188.45/25/KUM/2024",
            documentCode = "ARS-2024-001",
            title = "Keputusan Bupati Tentang Pembentukan Tim Kerja",
            description = "Dokumen keputusan bupati tentang pembentukan tim kerja daerah.",
            year = 2024,
            physicalForm = PhysicalForm.BOOK,
            condition = DocumentCondition.GOOD,
            copyCount = 1,
            status = DocumentStatus.BORROWED,
            originInstance = "Bagian Hukum",
            createdBy = "user-002",
            updatedBy = null,
            createdAt = "2024-03-20",
            updatedAt = null,
            deletedAt = null
        ),
        ArchiveDocument(
            id = "doc-004",
            documentType = DocumentType.PERKAB,
            documentNumber = "05 Tahun 2024",
            documentCode = "ARS-2024-002",
            title = "Peraturan Kabupaten Tentang Tata Naskah Dinas",
            description = "Dokumen peraturan kabupaten terkait tata naskah dinas.",
            year = 2024,
            physicalForm = PhysicalForm.BOOK,
            condition = DocumentCondition.GOOD,
            copyCount = 1,
            status = DocumentStatus.AVAILABLE,
            originInstance = "Sekretariat Daerah",
            createdBy = "user-002",
            updatedBy = null,
            createdAt = "2024-05-12",
            updatedAt = null,
            deletedAt = null
        ),
        ArchiveDocument(
            id = "doc-005",
            documentType = DocumentType.KEPGUB,
            documentNumber = "100.3.3.1/77/2023",
            documentCode = "ARS-2023-001",
            title = "Keputusan Gubernur Tentang Evaluasi Anggaran",
            description = "Dokumen keputusan gubernur terkait hasil evaluasi anggaran daerah.",
            year = 2023,
            physicalForm = PhysicalForm.BOOK,
            condition = DocumentCondition.GOOD,
            copyCount = 1,
            status = DocumentStatus.AVAILABLE,
            originInstance = "Pemerintah Provinsi",
            createdBy = "user-003",
            updatedBy = null,
            createdAt = "2023-08-01",
            updatedAt = null,
            deletedAt = null
        ),
        ArchiveDocument(
            id = "doc-006",
            documentType = DocumentType.SURAT,
            documentNumber = "045/ARSIP/2023",
            documentCode = "ARS-2023-002",
            title = "Surat Keterangan Pemindahan Arsip",
            description = "Surat keterangan terkait pemindahan arsip fisik ke ruang penyimpanan.",
            year = 2023,
            physicalForm = PhysicalForm.SHEET,
            condition = DocumentCondition.DAMAGED,
            copyCount = 1,
            status = DocumentStatus.DISPOSED,
            originInstance = "Bagian Umum",
            createdBy = "user-003",
            updatedBy = null,
            createdAt = "2023-10-18",
            updatedAt = null,
            deletedAt = null
        )
    )

    override suspend fun getArchiveYearSummaries(): List<ArchiveYearSummary> {
        return documents
            .filter { it.deletedAt == null }
            .groupBy { it.year }
            .map { (year, documentsInYear) ->
                ArchiveYearSummary(
                    year = year,
                    documentCount = documentsInYear.size
                )
            }
            .sortedByDescending { it.year }
    }

    override suspend fun getArchiveDocuments(
        filter: ArchiveDocumentFilter
    ): List<ArchiveDocument> {
        var result = documents
            .filter { it.deletedAt == null }
            .filter { it.year == filter.year }

        filter.documentType?.let { documentType ->
            result = result.filter { it.documentType == documentType }
        }

        filter.status?.let { status ->
            result = result.filter { it.status == status }
        }

        filter.physicalForm?.let { physicalForm ->
            result = result.filter { it.physicalForm == physicalForm }
        }

        filter.condition?.let { condition ->
            result = result.filter { it.condition == condition }
        }

        filter.originInstance
            ?.takeIf { it.isNotBlank() }
            ?.let { originInstance ->
                result = result.filter { document ->
                    document.originInstance.orEmpty()
                        .contains(originInstance, ignoreCase = true)
                }
            }

        filter.keyword
            ?.takeIf { it.isNotBlank() }
            ?.let { keyword ->
                result = result.filter { document ->
                    document.title.contains(keyword, ignoreCase = true) ||
                            document.description.orEmpty().contains(keyword, ignoreCase = true) ||
                            document.documentNumber.orEmpty().contains(keyword, ignoreCase = true) ||
                            document.documentCode.orEmpty().contains(keyword, ignoreCase = true) ||
                            document.originInstance.orEmpty().contains(keyword, ignoreCase = true)
                }
            }

        return result.sortedByDescending { it.createdAt }
    }

    override suspend fun getArchiveDocumentById(
        id: String
    ): ArchiveDocument? {
        return documents.firstOrNull { document ->
            document.id == id && document.deletedAt == null
        }
    }

    override suspend fun createArchiveDocument(
        document: ArchiveDocument
    ) {
        val documentToInsert = if (document.id.isBlank()) {
            document.copy(id = generateDocumentId())
        } else {
            document
        }

        documents.add(documentToInsert)
    }

    override suspend fun updateArchiveDocument(
        document: ArchiveDocument
    ) {
        val index = documents.indexOfFirst { it.id == document.id }

        if (index != -1) {
            documents[index] = document
        }
    }

    override suspend fun deleteArchiveDocument(
        id: String
    ) {
        val index = documents.indexOfFirst { it.id == id }

        if (index != -1) {
            documents[index] = documents[index].copy(
                deletedAt = "SOFT_DELETED"
            )
        }
    }

    private fun generateDocumentId(): String {
        return "doc-${documents.size + 1}"
    }
}