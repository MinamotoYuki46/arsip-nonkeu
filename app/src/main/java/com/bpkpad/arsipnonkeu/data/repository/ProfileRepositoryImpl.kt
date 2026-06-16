package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.data.remote.datasource.ProfileRemoteDataSource
import com.bpkpad.arsipnonkeu.data.remote.mapper.toDomain
import com.bpkpad.arsipnonkeu.domain.model.UserProfile
import com.bpkpad.arsipnonkeu.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val remoteDataSource: ProfileRemoteDataSource
) : ProfileRepository {
    override suspend fun getCurrentUserProfile(): UserProfile? {
        return remoteDataSource.getCurrentUserProfile()?.toDomain()
    }

    override suspend fun getProfileById(id: String): UserProfile? {
        return remoteDataSource.getProfileById(id)?.toDomain()
    }

    override suspend fun getAllProfiles(): List<UserProfile> {
        return remoteDataSource.getAllProfiles().map { it.toDomain() }
    }
}
