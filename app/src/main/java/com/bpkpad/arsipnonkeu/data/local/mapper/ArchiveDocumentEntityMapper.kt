package com.bpkpad.arsipnonkeu.data.local.mapper

import com.bpkpad.arsipnonkeu.data.local.entity.ArchiveDocumentEntity
import com.bpkpad.arsipnonkeu.domain.model.*

fun ArchiveDocumentEntity.toDomain(): ArchiveDocument {
    return ArchiveDocument(
        id = id,
        documentType = DocumentType.valueOf(documentType),
        documentNumber = documentNumber,
        classificationCode = classificationCode,
        title = title,
        description = description,
        year = year,
        physicalForm = PhysicalForm.valueOf(physicalForm),
        condition = condition?.let { DocumentCondition.valueOf(it) },
        copyCount = copyCount,
        isCopy = isCopy,
        status = DocumentStatus.valueOf(status),
        originInstance = originInstance,
        room = room,
        shelf = shelf,
        boxNumber = boxNumber,
        createdBy = createdBy,
        updatedBy = updatedBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}

fun ArchiveDocument.toEntity(): ArchiveDocumentEntity {
    return ArchiveDocumentEntity(
        id = id,
        documentType = documentType.name,
        documentNumber = documentNumber,
        classificationCode = classificationCode,
        title = title,
        description = description,
        year = year,
        physicalForm = physicalForm.name,
        condition = condition?.name,
        copyCount = copyCount,
        isCopy = isCopy,
        status = status.name,
        originInstance = originInstance,
        room = room,
        shelf = shelf,
        boxNumber = boxNumber,
        createdBy = createdBy,
        updatedBy = updatedBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}
