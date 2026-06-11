package com.bpkpad.arsip.core.domain.model

data class StagingDocument(
    val id: String,
    val type: DocumentType,
    val title: String,
    val year: Int,
    val metadata: Map<String, String>,
    val coverLocalPath: String?,
    val status: StagingStatus
)

enum class StagingStatus { LOCAL_ONLY, SYNCED }