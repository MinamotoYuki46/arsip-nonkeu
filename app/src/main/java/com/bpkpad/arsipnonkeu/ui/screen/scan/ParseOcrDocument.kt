package com.bpkpad.arsipnonkeu.ui.screen.scan

import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm

data class ParsedOcrDocument(
    val documentType: DocumentType,
    val documentNumber: String?,
    val documentCode: String?,
    val title: String,
    val description: String?,
    val year: Int,
    val physicalForm: PhysicalForm,
    val condition: DocumentCondition?,
    val copyCount: Int,
    val isCopy: Boolean?,
    val status: DocumentStatus,
    val originInstance: String?
)