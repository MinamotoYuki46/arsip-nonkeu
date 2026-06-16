package com.bpkpad.arsipnonkeu.domain.model

data class ActivityLog(
    val id: String,
    val actorId: String?,
    val action: String,
    val entity: String,
    val entityId: String?,
    val description: String?,
    val createdAt: String
)
