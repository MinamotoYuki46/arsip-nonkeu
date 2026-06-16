package com.bpkpad.arsipnonkeu.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsipnonkeu.di.ArchiveModule
import com.bpkpad.arsipnonkeu.domain.model.UserProfile
import com.bpkpad.arsipnonkeu.domain.repository.ActivityLogRepository
import com.bpkpad.arsipnonkeu.domain.repository.AuthRepository
import com.bpkpad.arsipnonkeu.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val userProfile: UserProfile? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository = ArchiveModule.authRepository,
    private val profileRepository: ProfileRepository = ArchiveModule.profileRepository,
    private val activityLogRepository: ActivityLogRepository = ArchiveModule.activityLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    isLoggedIn = false,
                    userProfile = null
                )
            }

            // 1. Login Auth dulu.
            try {
                authRepository.login(username, password)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Username atau password salah"
                    )
                }
                return@launch
            }

            // 2. Kalau sampai sini, berarti login Supabase Auth berhasil.
            val profile = try {
                profileRepository.getCurrentUserProfile()
            } catch (e: Exception) {
                authRepository.logout()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Login berhasil, tetapi gagal mengambil profil pengguna"
                    )
                }
                return@launch
            }

            if (profile == null) {
                authRepository.logout()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Login berhasil, tetapi profil pengguna tidak ditemukan"
                    )
                }
                return@launch
            }

            // 3. Activity log jangan bikin login gagal.
            try {
                activityLogRepository.createActivityLog(
                    action = "LOGIN",
                    entity = "USER",
                    entityId = profile.id,
                    description = "User logged in to the application"
                )
            } catch (e: Exception) {
                // Untuk sekarang abaikan dulu.
                // Login tetap dianggap berhasil.
                println("LOGIN_ACTIVITY_LOG_ERROR: ${e.message}")
            }

            // 4. Baru update state sukses.
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = null,
                    isLoggedIn = true,
                    userProfile = profile
                )
            }
        }
    }

    fun resetError() {
        _uiState.update {
            it.copy(error = null)
        }
    }

    fun resetLoginState() {
        _uiState.update {
            LoginUiState()
        }
    }
}