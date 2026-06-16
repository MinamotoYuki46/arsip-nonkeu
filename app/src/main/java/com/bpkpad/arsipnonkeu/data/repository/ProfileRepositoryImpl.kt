package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.data.local.datasource.ProfileLocalDataSource
import com.bpkpad.arsipnonkeu.data.remote.datasource.ProfileRemoteDataSource
import com.bpkpad.arsipnonkeu.data.remote.mapper.toDomain
import com.bpkpad.arsipnonkeu.domain.model.UserProfile
import com.bpkpad.arsipnonkeu.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class ProfileRepositoryImpl(
    private val remoteDataSource: ProfileRemoteDataSource,
    private val localDataSource: ProfileLocalDataSource
) : ProfileRepository {

    override fun observeCurrentUserProfile(): Flow<UserProfile?> {
        return localDataSource.getUserProfile()
    }

    override suspend fun refreshCurrentUserProfile(): UserProfile? {
        val remote = remoteDataSource.getCurrentUserProfile()?.toDomain()
        if (remote != null) {
            localDataSource.saveUserProfile(remote)
        }
        return remote
    }

    override suspend fun getCurrentUserProfile(): UserProfile? {
        // Try local first
        // Note: For current user profile, we might want to refresh it more often
        val remote = refreshCurrentUserProfile()
        return remote
    }

    override suspend fun getProfileById(id: String): UserProfile? {
        return remoteDataSource.getProfileById(id)?.toDomain()
    }

    override suspend fun getAllProfiles(): List<UserProfile> {
        return remoteDataSource.getAllProfiles().map { it.toDomain() }
    }
}
