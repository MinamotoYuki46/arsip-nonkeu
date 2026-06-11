package com.bpkpad.arsipnonkeu.domain.model


data class User(
    val id: String,
    val name: String,
    val username: String,
    val role: UserRole
)

enum class UserRole {
    ARCHIVIST,
    HEAD_OF_DIVISION
}