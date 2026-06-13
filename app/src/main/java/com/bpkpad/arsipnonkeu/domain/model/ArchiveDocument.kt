package com.bpkpad.arsipnonkeu.domain.model

data class ArchiveDocument(
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
    val createdBy: String?,
    val updatedBy: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val deletedAt: String?
)


enum class DocumentType(val label: String) {
    SURAT("Surat"),
    PERDA("Peraturan Daerah"),
    PERKAB("Peraturan Kabupaten"),
    KEPBUP("Keputusan Bupati"),
    KEPGUB("Keputusan Gubernur")
}


enum class DocumentStatus(val label: String) {
    AVAILABLE("Tersedia"),
    BORROWED("Dipinjam"),
    DISPOSED("Dimusnahkan")
}


enum class PhysicalForm(val label: String) {
    SHEET("Lembaran"),
    BOOK("Buku")
}

enum class DocumentCondition(val label: String) {
    GOOD("Baik"),
    DAMAGED("Rusak"),
}