package com.bpkpad.arsipnonkeu.ui.screen.login

import com.bpkpad.arsipnonkeu.domain.model.AppRole
import com.bpkpad.arsipnonkeu.domain.model.UserProfile
import com.bpkpad.arsipnonkeu.domain.repository.ActivityLogRepository
import com.bpkpad.arsipnonkeu.domain.repository.AuthRepository
import com.bpkpad.arsipnonkeu.domain.repository.ProfileRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest : BehaviorSpec({

    val testDispatcher = StandardTestDispatcher()
    val authRepository = mockk<AuthRepository>()
    val profileRepository = mockk<ProfileRepository>()
    val activityLogRepository = mockk<ActivityLogRepository>()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    given("LoginViewModel") {
        
        val viewModel = LoginViewModel(authRepository, profileRepository, activityLogRepository)

        `when`("login succeeds") {
            val dummyProfile = UserProfile(
                id = "1",
                username = "admin",
                name = "Administrator",
                role = AppRole.ARSIPARIS,
                createdAt = "2024-01-01",
                updatedAt = "2024-01-01"
            )
            
            coEvery { authRepository.login(any(), any()) } just Runs
            coEvery { profileRepository.getCurrentUserProfile() } returns dummyProfile
            coEvery { activityLogRepository.createActivityLog(any(), any(), any(), any()) } just Runs
            
            viewModel.login("admin", "password123")
            
            then("uiState should reflect logged in status") {
                testDispatcher.scheduler.advanceUntilIdle()
                viewModel.uiState.value.isLoggedIn shouldBe true
                viewModel.uiState.value.userProfile shouldBe dummyProfile
                viewModel.uiState.value.isLoading shouldBe false
            }
        }

        `when`("login fails") {
            val errorMsg = "Username atau password salah"
            coEvery { authRepository.login(any(), any()) } throws Exception("Any Error")
            
            viewModel.login("wrong", "user")
            
            then("uiState should show error") {
                testDispatcher.scheduler.advanceUntilIdle()
                viewModel.uiState.value.isLoggedIn shouldBe false
                viewModel.uiState.value.error shouldBe errorMsg
                viewModel.uiState.value.isLoading shouldBe false
            }
        }
    }
})
