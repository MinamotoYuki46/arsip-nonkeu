package com.bpkpad.arsipnonkeu.domain.repository

import com.bpkpad.arsipnonkeu.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeCurrentUserProfile(): Flow<UserProfile?>
    suspend fun refreshCurrentUserProfile(): UserProfile?

    suspend fun getCurrentUserProfile(): UserProfile?
    suspend fun getProfileById(id: String): UserProfile?
    suspend fun getAllProfiles(): List<UserProfile>
}
