package com.bpkpad.arsipnonkeu.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivityLogDto(
    @SerialName("id")
    val id: String,
    @SerialName("actor_id")
    val actorId: String?,
    @SerialName("action")
    val action: String,
    @SerialName("entity")
    val entity: String,
    @SerialName("entity_id")
    val entityId: String?,
    @SerialName("description")
    val description: String?,
    @SerialName("created_at")
    val createdAt: String
)
