package com.bpkpad.arsip.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bpkpad.arsip.domain.model.ArchiveDocument
import java.util.Date

@Entity(tableName = "archives")
data class ArchiveEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val date: Long,
    val description: String,
    val boxId: String,
    val locationId: String,
    val imageUrl: String?
)

fun ArchiveEntity.toDomain(): ArchiveDocument {
    return ArchiveDocument(
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

fun ArchiveDocument.toEntity(): ArchiveEntity {
    return ArchiveEntity(
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
