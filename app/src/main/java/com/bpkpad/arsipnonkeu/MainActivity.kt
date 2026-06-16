package com.bpkpad.arsipnonkeu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.bpkpad.arsipnonkeu.di.ArchiveModule
import com.bpkpad.arsipnonkeu.domain.model.UserProfile
import com.bpkpad.arsipnonkeu.ui.screen.add.NewRecordScreen
import com.bpkpad.arsipnonkeu.ui.screen.archive.ArchiveScreen
import com.bpkpad.arsipnonkeu.ui.screen.dashboard.DashboardScreen
import com.bpkpad.arsipnonkeu.ui.screen.detail.DocumentDetailScreen
import com.bpkpad.arsipnonkeu.ui.screen.login.LoginScreen
import com.bpkpad.arsipnonkeu.ui.screen.profile.ProfileScreen
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingScreen
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingViewModel
import com.bpkpad.arsipnonkeu.ui.theme.ArsipBPKADTheme
import com.bpkpad.arsipnonkeu.ui.screen.scan.ScanScreen
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingViewModelFactory
import kotlinx.coroutines.launch

/**
 * MainActivity - Entry point of the BPKPAD Balangan application.
 *
 * This version still uses simple manual navigation with route state.
 * Android system back is handled using BackHandler.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ArsipBPKADTheme {
                val scope = rememberCoroutineScope()
                val authRepository = ArchiveModule.authRepository
                val profileRepository = ArchiveModule.profileRepository
                val activityLogRepository = ArchiveModule.activityLogRepository

                var isLoggedIn by remember { mutableStateOf(false) }
                var userName by remember { mutableStateOf("") }
                var userRole by remember { mutableStateOf("") }

                var isLoggingIn by remember { mutableStateOf(false) }
                var loginError by remember { mutableStateOf<String?>(null) }

                var currentRoute by remember { mutableStateOf("login") }
                var lastRoute by remember { mutableStateOf("dashboard") }

                var selectedYear by remember { mutableIntStateOf(2025) }
                var selectedDocumentId by remember { mutableStateOf<String?>(null) }

                val stagingViewModel: StagingViewModel = viewModel(
                    key = selectedYear.toString(),
                    factory = StagingViewModelFactory(applicationContext, selectedYear)
                )

                BackHandler(enabled = currentRoute != "dashboard" && currentRoute != "login") {
                    currentRoute = when (currentRoute) {
                        "archive" -> {
                            "dashboard"
                        }

                        "staging" -> {
                            "archive"
                        }

                        "new_record" -> {
                            "staging"
                        }

                        "document_detail" -> {
                            lastRoute
                        }

                        "scan" -> {
                            "staging"
                        }

                        "profile" -> {
                            "dashboard"
                        }

                        else -> {
                            "dashboard"
                        }
                    }
                }

                if (!isLoggedIn) {
                    LoginScreen(
                        isLoading = isLoggingIn,
                        externalError = loginError,
                        onLoginSuccess = { username, password ->
                            scope.launch {
                                isLoggingIn = true
                                loginError = null

                                try {
                                    authRepository.login(username, password)

                                    val profile = profileRepository.getCurrentUserProfile()

                                    if (profile != null) {
                                        userName = profile.name
                                        userRole = profile.role.name
                                        isLoggedIn = true
                                        currentRoute = "dashboard"

                                        activityLogRepository.createActivityLog(
                                            action = "LOGIN",
                                            entity = "USER",
                                            entityId = profile.id,
                                            description = "User logged in to the application"
                                        )
                                    } else {
                                        loginError = "Profil pengguna tidak ditemukan"
                                        authRepository.logout()
                                    }
                                } catch (e: Exception) {
                                    loginError = "Username atau password salah"
                                } finally {
                                    isLoggingIn = false
                                }
                            }
                        }
                    )
                } else {
                    when (currentRoute) {
                        "dashboard" -> {
                            DashboardScreen(
                                onProfileClick = {
                                    currentRoute = "profile"
                                },
                                onArchiveYearClick = { year ->
                                    selectedYear = year
                                    currentRoute = "archive"
                                }
                            )
                        }

                        "archive" -> {
                            ArchiveScreen(
                                selectedYear = selectedYear,
                                onDocumentClick = { documentId ->
                                    selectedDocumentId = documentId
                                    lastRoute = "archive"
                                    currentRoute = "document_detail"
                                },
                                onStagingClick = {
                                    currentRoute = "staging"
                                }
                            )
                        }

                        "staging" -> {
                            StagingScreen(
                                selectedYear = selectedYear,
                                onBackClick = {
                                    currentRoute = "archive"
                                },
                                onManualClick = {
                                    currentRoute = "new_record"
                                },
                                onScanClick = {
                                    currentRoute = "scan"
                                },
                                onImportClick = {
                                    // Sementara import langsung ditangani di StagingScreen.
                                    // Tidak perlu pindah halaman dulu.
                                },
                                onPushAllClick = {
                                    currentRoute = "dashboard"
                                },
                                viewModel = stagingViewModel
                            )
                        }

                        "new_record" -> {
                            NewRecordScreen(
                                selectedYear = selectedYear,
                                onBackClick = {
                                    currentRoute = "staging"
                                },
                                onSave = {
                                    currentRoute = "staging"
                                },
                                viewModel = stagingViewModel
                            )
                        }

                        "scan" -> {
                            ScanScreen(
                                onBackClick = {
                                    currentRoute = "staging"
                                },
                                onScanCompleted = {
                                    currentRoute = "staging"
                                },
                                stagingViewModel = stagingViewModel
                            )
                        }

                        "document_detail" -> {
                            val documentId = selectedDocumentId

                            if (documentId != null) {
                                DocumentDetailScreen(
                                    documentId = documentId,
                                    onBackClick = {
                                        currentRoute = lastRoute
                                    }
                                )
                            } else {
                                currentRoute = lastRoute
                            }
                        }

                        "profile" -> {
                            ProfileScreen(
                                userName = userName,
                                userRole = userRole,
                                onNavItemSelected = { route ->
                                    currentRoute = route
                                },
                                onLogoutClick = {
                                    scope.launch {
                                        try {
                                            authRepository.logout()
                                        } catch (e: Exception) {
                                            // Handle error during logout if needed
                                        }
                                        isLoggedIn = false
                                        currentRoute = "login"
                                        userName = ""
                                        userRole = ""
                                    }
                                }
                            )
                        }

                        else -> {
                            DashboardScreen(
                                onProfileClick = {
                                    currentRoute = "profile"
                                },
                                onArchiveYearClick = { year ->
                                    selectedYear = year
                                    currentRoute = "archive"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}