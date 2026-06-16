package com.bpkpad.arsipnonkeu.data.remote.mapper

import com.bpkpad.arsipnonkeu.data.remote.dto.UserProfileDto
import com.bpkpad.arsipnonkeu.domain.model.AppRole
import com.bpkpad.arsipnonkeu.domain.model.UserProfile

fun UserProfileDto.toDomain(): UserProfile {
    return UserProfile(
        id = id,
        username = username,
        name = name,
        role = try { AppRole.valueOf(role) } catch (e: Exception) { AppRole.ARSIPARIS },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UserProfile.toDto(): UserProfileDto {
    return UserProfileDto(
        id = id,
        username = username,
        name = name,
        role = role.name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
