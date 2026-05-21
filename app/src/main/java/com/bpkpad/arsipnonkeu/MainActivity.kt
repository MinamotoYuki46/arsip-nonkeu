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

                when (currentRoute) {
                    "dashboard" -> DashboardScreen(
                        onNavItemSelected = { currentRoute = it },
                        onArchiveYearClick = { currentRoute = "archive" }
                    )
                    "archive" -> ArchiveScreen(
                        onNavItemSelected = { currentRoute = it },
                        onDocumentClick = {
                            lastRoute = "archive"
                            currentRoute = "document_detail"
                        }
                    )
                    "search" -> SearchScreen(
                        onNavItemSelected = { currentRoute = it },
                        onResultClick = {
                            lastRoute = "search"
                            currentRoute = "document_detail"
                        }
                    )
                    "new_record" -> NewRecordScreen(
                        onNavItemSelected = { currentRoute = it }
                    )
                    "profile" -> {
                        // Profile screen placeholder or just stay on dashboard
                        DashboardScreen(onNavItemSelected = { currentRoute = it })
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
