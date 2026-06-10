package com.bpkpad.arsipnonkeu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.bpkpad.arsipnonkeu.ui.theme.ArsipBPKADTheme
import com.bpkpad.arsipnonkeu.ui.theme.screen.add.NewRecordScreen
import com.bpkpad.arsipnonkeu.ui.theme.screen.archive.ArchiveScreen
import com.bpkpad.arsipnonkeu.ui.theme.screen.dashboard.DashboardScreen
import com.bpkpad.arsipnonkeu.ui.theme.screen.detail.DocumentDetailScreen
import com.bpkpad.arsipnonkeu.ui.theme.screen.search.SearchScreen

/**
 * MainActivity - Entry point of the BPKPAD Balangan application.
 *
 * Sets up the Compose content and handles basic navigation between screens.
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

                when (currentRoute) {
                    "dashboard" -> DashboardScreen(
                        onNavItemSelected = { currentRoute = it },
                        onArchiveYearClick = { year ->
                            selectedYear = year
                            currentRoute = "archive"
                        }
                    )
                    "archive" -> ArchiveScreen(
                        selectedYear = selectedYear,
                        onBackClick = { currentRoute = "dashboard" },
                        onDocumentClick = {
                            lastRoute = "archive"
                            currentRoute = "document_detail"
                        },
                        onCreateClick = { currentRoute = "new_record" },
                        onScanClick = { currentRoute = "new_record" },
                        onUploadClick = { currentRoute = "new_record" }
                    )
                    "search" -> SearchScreen(
                        onBackClick = { currentRoute = "dashboard" },
                        onResultClick = {
                            lastRoute = "search"
                            currentRoute = "document_detail"
                        }
                    )
                    "new_record" -> NewRecordScreen(
                        onBackClick = { currentRoute = "dashboard" }
                    )
                    "profile" -> {
                        // Profile screen placeholder
                        currentRoute = "dashboard"
                    }
                    "document_detail" -> DocumentDetailScreen(
                        onBackClick = { currentRoute = lastRoute }
                    )
                    else -> DashboardScreen(onNavItemSelected = { currentRoute = it })
                }
            }
        }
    }
}
