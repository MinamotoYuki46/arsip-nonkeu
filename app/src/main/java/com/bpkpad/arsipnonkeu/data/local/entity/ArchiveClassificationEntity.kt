package com.bpkpad.arsipnonkeu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "archive_classifications")
data class ArchiveClassificationEntity(
    @PrimaryKey
    val code: String,
    val name: String,
    val parentCode: String?,
    val level: Int,
    val isActive: Boolean
)
