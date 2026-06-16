package com.bpkpad.arsipnonkeu.data.local.mapper

import com.bpkpad.arsipnonkeu.data.local.entity.UserProfileEntity
import com.bpkpad.arsipnonkeu.domain.model.AppRole
import com.bpkpad.arsipnonkeu.domain.model.UserProfile

fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        id = id,
        username = username,
        name = name,
        role = AppRole.valueOf(role),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        username = username,
        name = name,
        role = role.name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
