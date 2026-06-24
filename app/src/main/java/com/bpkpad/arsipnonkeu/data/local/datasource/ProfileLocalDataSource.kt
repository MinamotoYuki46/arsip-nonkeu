package com.bpkpad.arsipnonkeu.data.local.datasource

import com.bpkpad.arsipnonkeu.data.local.dao.UserProfileDao
import com.bpkpad.arsipnonkeu.data.local.mapper.toDomain
import com.bpkpad.arsipnonkeu.data.local.mapper.toEntity
import com.bpkpad.arsipnonkeu.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileLocalDataSource(private val userProfileDao: UserProfileDao) {
    fun getUserProfile(): Flow<UserProfile?> {
        return userProfileDao.getUserProfile().map { it?.toDomain() }
    }

    suspend fun saveUserProfile(userProfile: UserProfile) {
        userProfileDao.insertUserProfile(userProfile.toEntity())
    }

    suspend fun clearUserProfile() {
        userProfileDao.clearUserProfile()
    }
}
