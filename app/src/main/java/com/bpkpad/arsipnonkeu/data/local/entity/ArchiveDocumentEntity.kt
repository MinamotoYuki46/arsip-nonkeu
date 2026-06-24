package com.bpkpad.arsipnonkeu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "archive_documents")
data class ArchiveDocumentEntity(
    @PrimaryKey
    val id: String,
    val documentType: String,
    val documentNumber: String?,
    val classificationCode: String?,
    val title: String,
    val description: String?,
    val year: Int,
    val physicalForm: String,
    val condition: String?,
    val copyCount: Int,
    val isCopy: Boolean?,
    val status: String,
    val originInstance: String?,
    val room: String?,
    val shelf: String?,
    val boxNumber: String?,
    val createdBy: String?,
    val updatedBy: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val deletedAt: String?
)
