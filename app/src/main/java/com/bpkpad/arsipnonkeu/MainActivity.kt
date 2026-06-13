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
import com.bpkpad.arsipnonkeu.ui.screen.add.NewRecordScreen
import com.bpkpad.arsipnonkeu.ui.screen.archive.ArchiveScreen
import com.bpkpad.arsipnonkeu.ui.screen.dashboard.DashboardScreen
import com.bpkpad.arsipnonkeu.ui.screen.detail.DocumentDetailScreen
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingScreen
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingViewModel
import com.bpkpad.arsipnonkeu.ui.theme.ArsipBPKADTheme
import com.bpkpad.arsipnonkeu.ui.screen.scan.ScanScreen
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingViewModelFactory

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
                var currentRoute by remember { mutableStateOf("dashboard") }
                var lastRoute by remember { mutableStateOf("dashboard") }

                var selectedYear by remember { mutableIntStateOf(2025) }
                var selectedDocumentId by remember { mutableStateOf<String?>(null) }

                val stagingViewModel: StagingViewModel = viewModel(
                    factory = StagingViewModelFactory(applicationContext)
                )

                BackHandler(enabled = currentRoute != "dashboard") {
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

                        else -> {
                            "dashboard"
                        }
                    }
                }

                when (currentRoute) {
                    "dashboard" -> {
                        DashboardScreen(
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

//                    "profile" -> {
//                        currentRoute = "dashboard"
//                    }

                    else -> {
                        DashboardScreen(
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