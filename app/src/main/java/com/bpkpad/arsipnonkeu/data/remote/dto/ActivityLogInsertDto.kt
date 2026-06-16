package com.bpkpad.arsipnonkeu.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivityLogInsertDto(
    @SerialName("actor_id")
    val actorId: String? = null,
    @SerialName("action")
    val action: String,
    @SerialName("entity_type")
    val entity: String,
    @SerialName("entity_id")
    val entityId: String?,
    @SerialName("description")
    val description: String?
)
