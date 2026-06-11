package com.bpkpad.arsip.data.remote.dto

import com.bpkpad.arsip.domain.model.ArchiveDocument
import java.util.Date

data class ArchiveDto(
    val id: String?,
    val title: String?,
    val type: String?,
    val date: Long?,
    val description: String?,
    val boxId: String?,
    val locationId: String?,
    val imageUrl: String?
)

fun ArchiveDto.toDomain(): ArchiveDocument {
    return ArchiveDocument(
        id = id ?: "",
        title = title ?: "",
        type = type ?: "",
        date = date ?: System.currentTimeMillis(),
        description = description ?: "",
        boxId = boxId ?: "",
        locationId = locationId ?: "",
        imageUrl = imageUrl
    )
}

fun ArchiveDocument.toDto(): ArchiveDto {
    return ArchiveDto(
        id = id,
        title = title,
        type = type,
        date = date,
        description = description,
        boxId = boxId,
        locationId = locationId,
        imageUrl = imageUrl
    )
}
