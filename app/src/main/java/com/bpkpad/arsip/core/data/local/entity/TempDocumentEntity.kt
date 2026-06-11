package com.bpkpad.arsip.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.bpkpad.arsip.domain.model.ArchiveDocument

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val username: String,
    val role: String,
    val instance: String,
    val password: String = "password123", // Added for local dev login
    val profilePhoto: String? = null
)

@Entity(tableName = "storage_locations")
data class StorageLocationEntity(
    @PrimaryKey val id: String,
    val room: String,
    val shelves: String,
    val number: String
)

@Entity(tableName = "archive_documents")
data class ArchiveDocumentEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val year: Int,
    val condition: String,
    val instance: String,
    val metadata: String, // JSONB as String
    val coverUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val timestampUserId: String
)

fun ArchiveDocumentEntity.toDomain(): ArchiveDocument {
    return ArchiveDocument(
        id = id,
        title = title,
        type = type,
        date = createdAt,
        description = "Condition: $condition", // Simple mapping for now
        boxId = "", // Not in entity
        locationId = "", // Not in entity
        imageUrl = coverUrl
    )
}

@Entity(
    tableName = "storing",
    foreignKeys = [
        ForeignKey(
            entity = StorageLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_storage_location"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ArchiveDocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_archive_documents"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StoringEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "id_storage_location") val idStorageLocation: String,
    @ColumnInfo(name = "id_archive_documents") val idArchiveDocuments: String,
    @ColumnInfo(name = "id_user") val idUser: String
)

@Entity(tableName = "temp_documents")
data class TempDocumentEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val year: Int,
    val condition: String,
    val instance: String,
    val metadata: String, // JSON string
    val coverLocalPath: String?,
    val status: String = "local_only",
    val createdAt: Long = System.currentTimeMillis()
)