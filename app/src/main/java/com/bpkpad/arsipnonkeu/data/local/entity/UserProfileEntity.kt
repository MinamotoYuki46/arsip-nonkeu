package com.bpkpad.arsipnonkeu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val name: String,
    val role: String,
    val createdAt: String,
    val updatedAt: String
)
