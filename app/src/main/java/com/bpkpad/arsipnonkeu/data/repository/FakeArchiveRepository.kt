package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentFilter
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentListItem
import com.bpkpad.arsipnonkeu.domain.model.ArchiveYearSummary
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentPlacement
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.domain.model.StorageLocation
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository

class FakeArchiveRepository : ArchiveRepository {

    private val documents = mutableListOf(
        ArchiveDocument(
            id = "doc-001",
            documentType = DocumentType.SURAT,
            documentNumber = "001/UMUM/2025",
            classificationCode = "ARS-2025-001",
            title = "Surat Undangan Rapat Koordinasi",
            description = "Surat undangan rapat koordinasi internal BPKPAD.",
            year = 2025,
            physicalForm = PhysicalForm.SHEET,
            condition = DocumentCondition.GOOD,
            copyCount = 1,
            isCopy = false,
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
            classificationCode = "ARS-2025-002",
            title = "Peraturan Daerah Tentang Pengelolaan Keuangan Daerah",
            description = "Dokumen Peraturan Daerah terkait pengelolaan keuangan daerah.",
            year = 2025,
            physicalForm = PhysicalForm.BOOK,
            condition = DocumentCondition.GOOD,
            copyCount = 2,
            isCopy = true,
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
            classificationCode = "ARS-2024-001",
            title = "Keputusan Bupati Tentang Pembentukan Tim Kerja",
            description = "Dokumen keputusan bupati tentang pembentukan tim kerja daerah.",
            year = 2024,
            physicalForm = PhysicalForm.BOOK,
            condition = DocumentCondition.GOOD,
            copyCount = 1,
            isCopy = true,
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
            classificationCode = "ARS-2024-002",
            title = "Peraturan Kabupaten Tentang Tata Naskah Dinas",
            description = "Dokumen peraturan kabupaten terkait tata naskah dinas.",
            year = 2024,
            physicalForm = PhysicalForm.BOOK,
            condition = DocumentCondition.GOOD,
            copyCount = 1,
            isCopy = false,
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
            classificationCode = "ARS-2023-001",
            title = "Keputusan Gubernur Tentang Evaluasi Anggaran",
            description = "Dokumen keputusan gubernur terkait hasil evaluasi anggaran daerah.",
            year = 2023,
            physicalForm = PhysicalForm.BOOK,
            condition = DocumentCondition.GOOD,
            copyCount = 1,
            isCopy = false,
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
            classificationCode = "ARS-2023-002",
            title = "Surat Keterangan Pemindahan Arsip",
            description = "Surat keterangan terkait pemindahan arsip fisik ke ruang penyimpanan.",
            year = 2023,
            physicalForm = PhysicalForm.SHEET,
            condition = DocumentCondition.DAMAGED,
            copyCount = 1,
            isCopy = false,
            status = DocumentStatus.DISPOSED,
            originInstance = "Bagian Umum",
            createdBy = "user-003",
            updatedBy = null,
            createdAt = "2023-10-18",
            updatedAt = null,
            deletedAt = null
        )
    )

    private val storageLocations = listOf(
        StorageLocation(
            id = "loc-001",
            room = "Ruang Arsip",
            shelf = "Rak 04-B",
            boxNumber = "01"
        ),
        StorageLocation(
            id = "loc-002",
            room = "Ruang Arsip",
            shelf = "Rak 01-A",
            boxNumber = "02"
        ),
        StorageLocation(
            id = "loc-003",
            room = "Ruang Arsip",
            shelf = "Rak 02-C",
            boxNumber = "03"
        ),
        StorageLocation(
            id = "loc-004",
            room = "Ruang Arsip",
            shelf = "Rak 03-A",
            boxNumber = "04"
        ),
        StorageLocation(
            id = "loc-005",
            room = "Gudang Arsip",
            shelf = "Rak 06-B",
            boxNumber = "01"
        ),
        StorageLocation(
            id = "loc-006",
            room = "Gudang Arsip",
            shelf = "Rak 07-D",
            boxNumber = "05"
        )
    )

    private val placements = mutableListOf(
        DocumentPlacement(
            id = "place-001",
            archiveDocumentId = "doc-001",
            storageLocationId = "loc-001",
            placedAt = "2025-01-10",
            removedAt = null,
            userId = null,
        ),
        DocumentPlacement(
            id = "place-002",
            archiveDocumentId = "doc-002",
            storageLocationId = "loc-002",
            placedAt = "2025-02-14",
            removedAt = null,
            userId = null,
        ),
        DocumentPlacement(
            id = "place-003",
            archiveDocumentId = "doc-003",
            storageLocationId = "loc-003",
            placedAt = "2024-03-20",
            removedAt = null,
            userId = null,
        ),
        DocumentPlacement(
            id = "place-004",
            archiveDocumentId = "doc-004",
            storageLocationId = "loc-004",
            placedAt = "2024-05-12",
            removedAt = null,
            userId = null,
        ),
        DocumentPlacement(
            id = "place-005",
            archiveDocumentId = "doc-005",
            storageLocationId = "loc-005",
            placedAt = "2023-08-01",
            removedAt = null,
            userId = null,
        ),
        DocumentPlacement(
            id = "place-006",
            archiveDocumentId = "doc-006",
            storageLocationId = "loc-006",
            placedAt = "2023-10-18",
            removedAt = null,
            userId = null,
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

    override suspend fun getArchiveDocumentListItems(
        filter: ArchiveDocumentFilter
    ): List<ArchiveDocumentListItem> {
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
                    val listItem = buildListItem(document)

                    document.title.contains(keyword, ignoreCase = true) ||
                            document.description.orEmpty().contains(keyword, ignoreCase = true) ||
                            document.documentNumber.orEmpty().contains(keyword, ignoreCase = true) ||
                            document.classificationCode.orEmpty().contains(keyword, ignoreCase = true) ||
                            document.originInstance.orEmpty().contains(keyword, ignoreCase = true) ||
                            listItem.locationLabel.contains(keyword, ignoreCase = true)
                }
            }

        return result
            .sortedWith(
                compareByDescending<ArchiveDocument> { it.createdAt }
                    .thenBy { it.title }
            )
            .map { document ->
                buildListItem(document)
            }
    }

    override suspend fun getArchiveDocumentListItemById(
        id: String
    ): ArchiveDocumentListItem? {
        val document = documents.firstOrNull { document ->
            document.id == id && document.deletedAt == null
        } ?: return null

        return buildListItem(document)
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

    private fun buildListItem(
        document: ArchiveDocument
    ): ArchiveDocumentListItem {
        val currentPlacement = placements.firstOrNull { placement ->
            placement.archiveDocumentId == document.id &&
                    placement.removedAt == null
        }

        val storageLocation = storageLocations.firstOrNull { location ->
            location.id == currentPlacement?.storageLocationId
        }

        return ArchiveDocumentListItem(
            document = document,
            currentPlacement = currentPlacement,
            storageLocation = storageLocation
        )
    }

    private fun generateDocumentId(): String {
        val nextNumber = documents.size + 1
        return "doc-${nextNumber.toString().padStart(3, '0')}"
    }
}