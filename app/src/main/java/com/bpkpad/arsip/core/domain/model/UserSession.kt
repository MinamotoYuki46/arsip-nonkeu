package com.bpkpad.arsip.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val userId: String,
    val username: String,
    val role: UserRole,
    val instance: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long
)