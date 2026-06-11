package com.bpkpad.arsip.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ArchiveDocument(
    val id: String,
    val type: DocumentType,
    val title: String,
    val year: Int,
    val condition: String,
    val instance: String,
    val metadata: Map<String, String>,
    val coverUrl: String?,
    val storageLocation: StorageLocation?,
    val deletedAt: String? = null
)