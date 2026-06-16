package com.bpkpad.arsipnonkeu.domain.repository

import com.bpkpad.arsipnonkeu.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getCurrentUserProfile(): UserProfile?
    suspend fun getProfileById(id: String): UserProfile?
    suspend fun getAllProfiles(): List<UserProfile>
}
