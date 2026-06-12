package com.bpkpad.arsipnonkeu.data.mapper

import com.bpkpad.arsipnonkeu.data.remote.model.ArchiveDocumentDto
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType

fun ArchiveDocumentDto.toDomain(): ArchiveDocument {
    return ArchiveDocument(
        id = id.orEmpty(),
        documentType = documentType.toDocumentType(),
        documentNumber = documentNumber,
        documentCode = documentCode,
        title = title.orEmpty(),
        description = description,
        year = year,
        physicalForm = physicalForm.toPhysicalForm(),
        condition = condition.toDocumentConditionOrNull(),
        copyCount = copyCount ?: 1,
        isCopy = isCopy,
        status = status.toDocumentStatus(),
        originInstance = originInstance,
        createdBy = createdBy,
        updatedBy = updatedBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}

private fun String?.toDocumentType(): DocumentType {
    return when (this?.uppercase()) {
        "SURAT" -> DocumentType.SURAT
        "PERDA" -> DocumentType.PERDA
        "PERKAB" -> DocumentType.PERKAB
        "KEPBUP" -> DocumentType.KEPBUP
        "KEPGUB" -> DocumentType.KEPGUB
        else -> DocumentType.SURAT
    }
}

private fun String?.toDocumentStatus(): DocumentStatus {
    return when (this?.uppercase()) {
        "AVAILABLE" -> DocumentStatus.AVAILABLE
        "BORROWED" -> DocumentStatus.BORROWED
        "DISPOSED" -> DocumentStatus.DISPOSED
        else -> DocumentStatus.AVAILABLE
    }
}

private fun String?.toPhysicalForm(): PhysicalForm {
    return when (this?.uppercase()) {
        "SHEET" -> PhysicalForm.SHEET
        "BOOK" -> PhysicalForm.BOOK
        else -> PhysicalForm.SHEET
    }
}

private fun String?.toDocumentConditionOrNull(): DocumentCondition? {
    return when (this?.uppercase()) {
        "GOOD" -> DocumentCondition.GOOD
        "DAMAGED" -> DocumentCondition.DAMAGED
        else -> DocumentCondition.GOOD
    }
}