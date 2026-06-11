package com.bpkpad.arsipnonkeu.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DocumentPlacementDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("archive_document_id")
    val archiveDocumentId: String? = null,

    @SerialName("storage_location_id")
    val storageLocationId: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("placed_at")
    val placedAt: String? = null,

    @SerialName("removed_at")
    val removedAt: String? = null
)