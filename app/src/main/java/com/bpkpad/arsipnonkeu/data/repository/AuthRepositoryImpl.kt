package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.data.local.datasource.ProfileLocalDataSource
import com.bpkpad.arsipnonkeu.data.remote.datasource.AuthRemoteDataSource
import com.bpkpad.arsipnonkeu.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val profileLocalDataSource: ProfileLocalDataSource
) : AuthRepository {

    override suspend fun login(username: String, password: String) {
        val normalizedUsername = username.trim().lowercase()
        val email = "$normalizedUsername@example.com"

        println("LOGIN_DEBUG username=[$username]")
        println("LOGIN_DEBUG email=[$email]")
        println("LOGIN_DEBUG passwordLength=${password.length}")
        remoteDataSource.loginWithEmail(email, password)
    }

    override suspend fun logout() {
        remoteDataSource.logout()
        // Clear local profile cache on logout
        profileLocalDataSource.clearUserProfile()
    }

    override fun getCurrentUserId(): String? {
        return remoteDataSource.getCurrentUserId()
    }

    override fun getCurrentUserEmail(): String? {
        return remoteDataSource.getCurrentUserEmail()
    }

    private fun usernameToAuthEmail(username: String): String {
        return "${username.trim().lowercase()}@example.com"
    }
}
