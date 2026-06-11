package com.bpkpad.arsipnonkeu.data.mapper

import com.bpkpad.arsipnonkeu.data.remote.model.DocumentPlacementDto
import com.bpkpad.arsipnonkeu.domain.model.DocumentPlacement

fun DocumentPlacementDto.toDomain(): DocumentPlacement {
    return DocumentPlacement(
        id = id.orEmpty(),
        archiveDocumentId = archiveDocumentId.orEmpty(),
        storageLocationId = storageLocationId.orEmpty(),
        userId = userId,
        placedAt = placedAt,
        removedAt = removedAt
    )
}