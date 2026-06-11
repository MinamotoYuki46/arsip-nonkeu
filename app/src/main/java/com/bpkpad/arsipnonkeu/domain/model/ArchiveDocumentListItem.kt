package com.bpkpad.arsipnonkeu.domain.model

data class ArchiveDocumentListItem(
    val document: ArchiveDocument,
    val currentPlacement: DocumentPlacement?,
    val storageLocation: StorageLocation?
) {
    val locationLabel: String
        get() = storageLocation?.displayName ?: "Belum ditentukan"
}