package com.bpkpad.arsipnonkeu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bpkpad.arsipnonkeu.ui.screen.add.NewRecordScreen
import com.bpkpad.arsipnonkeu.ui.screen.archive.ArchiveScreen
import com.bpkpad.arsipnonkeu.ui.screen.dashboard.DashboardScreen
import com.bpkpad.arsipnonkeu.ui.screen.detail.DocumentDetailScreen
import com.bpkpad.arsipnonkeu.ui.screen.login.LoginScreen
import com.bpkpad.arsipnonkeu.ui.screen.profile.ProfileScreen
import com.bpkpad.arsipnonkeu.ui.screen.scan.ScanScreen
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingScreen
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingViewModel
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingViewModelFactory
import com.bpkpad.arsipnonkeu.ui.theme.ArsipBPKADTheme

/**
 * MainActivity - Entry point of the BPKPAD Balangan application.
 *
 * This version uses simple manual navigation with route state.
 * Android system back is handled using BackHandler.
 *
 * Logout is handled locally first and does not depend on network request,
 * so logout remains safe when the device is offline.
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ArsipBPKADTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                var userName by remember { mutableStateOf("") }
                var userRole by remember { mutableStateOf("") }

                var currentRoute by remember { mutableStateOf("login") }
                var lastRoute by remember { mutableStateOf("dashboard") }
                var routeBeforeProfile by remember { mutableStateOf("dashboard") }

                var selectedYear by remember { mutableIntStateOf(2025) }
                var selectedDocumentId by remember { mutableStateOf<String?>(null) }

                var isExplicitlyLoggedOut by remember { mutableStateOf(false) }

                val stagingViewModel: StagingViewModel = viewModel(
                    key = selectedYear.toString(),
                    factory = StagingViewModelFactory(applicationContext, selectedYear)
                )

                fun performLocalLogout() {
                    isExplicitlyLoggedOut = true

                    userName = ""
                    userRole = ""

                    selectedDocumentId = null
                    selectedYear = 2025

                    lastRoute = "dashboard"
                    routeBeforeProfile = "dashboard"

                    currentRoute = "login"
                    isLoggedIn = false
                }

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
                            routeBeforeProfile
                        }

                        else -> {
                            "dashboard"
                        }
                    }
                }

                if (!isLoggedIn) {
                    LoginScreen(
                        skipAutoLogin = isExplicitlyLoggedOut,
                        onLoginSuccess = { profile ->
                            isExplicitlyLoggedOut = false

                            userName = profile.name
                            userRole = profile.role.name

                            selectedDocumentId = null
                            selectedYear = 2025

                            lastRoute = "dashboard"
                            routeBeforeProfile = "dashboard"

                            currentRoute = "dashboard"
                            isLoggedIn = true
                        }
                    )
                } else {
                    when (currentRoute) {
                        "dashboard" -> {
                            DashboardScreen(
                                userRole = userRole,
                                onProfileClick = {
                                    routeBeforeProfile = "dashboard"
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
                                userRole = userRole,
                                onProfileClick = {
                                    routeBeforeProfile = "archive"
                                    currentRoute = "profile"
                                },
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
                            if (!userRole.equals("ARSIPARIS", ignoreCase = true)) {
                                currentRoute = "dashboard"
                            } else {
                                StagingScreen(
                                    selectedYear = selectedYear,
                                    onProfileClick = {
                                        routeBeforeProfile = "staging"
                                        currentRoute = "profile"
                                    },
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
                                        // Import langsung ditangani di StagingScreen.
                                        // Tidak perlu pindah halaman.
                                    },
                                    onPushAllClick = {
                                        currentRoute = "dashboard"
                                    },
                                    viewModel = stagingViewModel
                                )
                            }
                        }

                        "new_record" -> {
                            if (!userRole.equals("ARSIPARIS", ignoreCase = true)) {
                                currentRoute = "dashboard"
                            } else {
                                NewRecordScreen(
                                    selectedYear = selectedYear,
                                    onProfileClick = {
                                        routeBeforeProfile = "new_record"
                                        currentRoute = "profile"
                                    },
                                    onBackClick = {
                                        currentRoute = "staging"
                                    },
                                    onSave = {
                                        currentRoute = "staging"
                                    },
                                    viewModel = stagingViewModel
                                )
                            }
                        }

                        "scan" -> {
                            if (!userRole.equals("ARSIPARIS", ignoreCase = true)) {
                                currentRoute = "dashboard"
                            } else {
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
                        }

                        "document_detail" -> {
                            val documentId = selectedDocumentId

                            if (documentId != null) {
                                DocumentDetailScreen(
                                    documentId = documentId,
                                    userRole = userRole,
                                    onProfileClick = {
                                        routeBeforeProfile = "document_detail"
                                        currentRoute = "profile"
                                    },
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
                                onBackClick = {
                                    currentRoute = routeBeforeProfile
                                },
                                onNavItemSelected = { route ->
                                    currentRoute = route
                                },
                                onLogoutClick = {
                                    performLocalLogout()
                                }
                            )
                        }

                        else -> {
                            currentRoute = "dashboard"
                        }
                    }
                }
            }
        }
    }
}