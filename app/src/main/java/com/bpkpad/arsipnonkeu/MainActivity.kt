package com.bpkpad.arsipnonkeu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.bpkpad.arsipnonkeu.ui.screen.add.NewRecordScreen
import com.bpkpad.arsipnonkeu.ui.screen.archive.ArchiveScreen
import com.bpkpad.arsipnonkeu.ui.screen.dashboard.DashboardScreen
import com.bpkpad.arsipnonkeu.ui.screen.detail.DocumentDetailScreen
import com.bpkpad.arsipnonkeu.ui.screen.search.SearchScreen
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingScreen
import com.bpkpad.arsipnonkeu.ui.theme.ArsipBPKADTheme

/**
 * MainActivity - Entry point of the BPKPAD Balangan application.
 *
 * Sets up Compose content and handles temporary manual navigation between screens.
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
                                currentRoute = "new_record"
                            },
                            onImportClick = {
                                currentRoute = "new_record"
                            },
                            onPushAllClick = {
                                currentRoute = "dashboard"
                            }
                        )
                    }

                    "search" -> {
                        SearchScreen(
                            onBackClick = {
                                currentRoute = "dashboard"
                            },
                            onResultClick = {
                                lastRoute = "search"
                                currentRoute = "document_detail"
                            }
                        )
                    }

                    "new_record" -> {
                        NewRecordScreen(
                            onBackClick = {
                                currentRoute = "staging"
                            },
                            onSave = {
                                currentRoute = "staging"
                            }
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
                        currentRoute = "dashboard"
                    }

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