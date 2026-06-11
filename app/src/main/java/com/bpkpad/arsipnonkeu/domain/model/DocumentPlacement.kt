package com.bpkpad.arsipnonkeu.domain.model

data class DocumentPlacement(
    val id: String,
    val archiveDocumentId: String,
    val storageLocationId: String,
    val userId: String?,
    val placedAt: String?,
    val removedAt: String?
)
