package com.bpkpad.arsipnonkeu.data.local.mapper

import com.bpkpad.arsipnonkeu.data.local.entity.ArchiveClassificationEntity
import com.bpkpad.arsipnonkeu.domain.model.ArchiveClassification

fun ArchiveClassificationEntity.toDomain(): ArchiveClassification {
    return ArchiveClassification(
        code = code,
        name = name,
        parentCode = parentCode,
        level = level,
        isActive = isActive
    )
}

fun ArchiveClassification.toEntity(): ArchiveClassificationEntity {
    return ArchiveClassificationEntity(
        code = code,
        name = name,
        parentCode = parentCode,
        level = level,
        isActive = isActive
    )
}
