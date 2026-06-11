package com.bpkpad.arsipnonkeu.domain.model

data class ArchiveDocumentFilter(
    val year: Int,
    val documentType: DocumentType? = null,
    val status: DocumentStatus? = null,
    val physicalForm: PhysicalForm? = null,
    val condition: DocumentCondition? = null,
    val originInstance: String? = null,
    val keyword: String? = null
)