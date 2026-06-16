package com.bpkpad.arsipnonkeu.domain.repository

interface AuthRepository {
    suspend fun login(username: String, password: String)
    suspend fun logout()
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
}
