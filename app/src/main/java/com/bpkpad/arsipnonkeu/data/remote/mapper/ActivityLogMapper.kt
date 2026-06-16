package com.bpkpad.arsipnonkeu.data.remote.mapper

import com.bpkpad.arsipnonkeu.data.remote.dto.ActivityLogDto
import com.bpkpad.arsipnonkeu.domain.model.ActivityLog

fun ActivityLogDto.toDomain(): ActivityLog {
    return ActivityLog(
        id = id,
        actorId = actorId,
        action = action,
        entity = entity,
        entityId = entityId,
        description = description,
        createdAt = createdAt
    )
}
