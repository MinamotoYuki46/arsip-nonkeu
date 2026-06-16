package com.bpkpad.arsipnonkeu.domain.model

data class UserProfile(
    val id: String,
    val username: String,
    val name: String,
    val role: AppRole,
    val createdAt: String,
    val updatedAt: String
)
