package com.bpkpad.arsip.core.data.repository

import android.content.SharedPreferences
import com.bpkpad.arsip.core.data.local.dao.UserDao
import com.bpkpad.arsip.core.domain.model.UserRole
import com.bpkpad.arsip.core.domain.model.UserSession
import com.bpkpad.arsip.core.domain.repository.AuthRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val sharedPreferences: SharedPreferences
) : AuthRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun login(username: String, password: String): Result<UserSession> {
        return try {
            // Local Login Bypass for Dev (Supabase not ready)
            val user = userDao.getUserByUsername(username)
                ?: throw Exception("User with username '$username' not found")
            
            // Password verification
            if (user.password != password) {
                throw Exception("Invalid password")
            }
            
            val session = UserSession(
                userId = user.id,
                username = user.username,
                role = UserRole.valueOf(user.role),
                instance = user.instance,
                accessToken = "local_token_${user.id}",
                refreshToken = "local_refresh_token_${user.id}",
                expiresAt = System.currentTimeMillis() + 3600000 * 24 // 24 hours
            )
            
            saveSession(session)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            // supabaseClient.auth.signOut()
            clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshSession(): Result<UserSession> {
        return try {
            // supabaseClient.auth.refreshCurrentSession()
            val session = getActiveSession() ?: throw Exception("No active session")
            /*
            val updatedSession = session.copy(
                accessToken = supabaseClient.auth.currentAccessTokenOrNull() ?: session.accessToken,
                refreshToken = supabaseClient.auth.currentSessionOrNull()?.refreshToken ?: session.refreshToken
            )
            */
            // For local, just return existing or refresh logic
            val updatedSession = session.copy(
                expiresAt = System.currentTimeMillis() + 3600000 * 24
            )
            saveSession(updatedSession)
            Result.success(updatedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getActiveSession(): UserSession? {
        val sessionJson = sharedPreferences.getString("user_session", null)
        return if (sessionJson != null) {
            json.decodeFromString<UserSession>(sessionJson)
        } else {
            // Mock session for local development
            UserSession(
                userId = "mock-id-123",
                username = "dev_arsiparis",
                role = UserRole.ARSIPARIS,
                instance = "BPKPAD",
                accessToken = "mock_access_token",
                refreshToken = "mock_refresh_token",
                expiresAt = System.currentTimeMillis() + 3600000 * 24
            )
        }
    }

    override fun isSessionValid(): Boolean {
        return true // Always valid to skip login screen
    }

    private fun saveSession(session: UserSession) {
        sharedPreferences.edit()
            .putString("user_session", json.encodeToString(session))
            .apply()
    }

    private fun clearSession() {
        sharedPreferences.edit()
            .remove("user_session")
            .apply()
    }
}