package com.bpkpad.arsip.core.domain.repository

import com.bpkpad.arsip.core.domain.model.UserSession

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<UserSession>
    suspend fun logout(): Result<Unit>
    suspend fun refreshSession(): Result<UserSession>
    fun getActiveSession(): UserSession?
    fun isSessionValid(): Boolean
}