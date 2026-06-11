package com.bpkpad.arsipnonkeu.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivityLogDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("archive_document_id")
    val archiveDocumentId: String? = null,

    @SerialName("action")
    val action: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)