package com.bpkpad.arsip.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bpkpad.arsip.feature.auth.LoginScreen
import com.bpkpad.arsip.core.domain.repository.AuthRepository
import com.bpkpad.arsip.domain.model.ArchiveDocument
import com.bpkpad.arsip.presentation.home.HomeScreen
import com.bpkpad.arsip.presentation.review.ReviewScreen
import com.bpkpad.arsip.presentation.scan.ScanScreen
import com.bpkpad.arsip.presentation.staging.InputScreen
import com.bpkpad.arsip.presentation.staging.StagingScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    authRepository: AuthRepository
) {
    val startDestination = if (authRepository.isSessionValid()) Routes.DASHBOARD else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            HomeScreen(
                onNavigateToScan = { navController.navigate(Routes.CAMERA) },
                onNavigateToManual = { navController.navigate(Routes.STAGING) },
                onNavigateToDetail = { id ->
                    navController.navigate(Routes.DETAIL.replace("{documentId}", id))
                }
            )
        }

        composable(Routes.DETAIL) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId")
            ReviewScreen(
                archiveId = documentId,
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable(Routes.STAGING) {
            StagingScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Routes.EDIT_STAGING.replace("{stagingId}", id))
                },
                onNavigateToInput = { navController.navigate(Routes.INPUT) },
                onNavigateToScan = { navController.navigate(Routes.CAMERA) }
            )
        }

        composable(Routes.EDIT_STAGING) { backStackEntry ->
            val stagingId = backStackEntry.arguments?.getString("stagingId")
            ReviewScreen(
                stagingId = stagingId,
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.INPUT) {
            InputScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStaging = { 
                    // Already in staging data-wise, just go back
                    navController.popBackStack()
                }
            )
        }
        
        // ... (Keep existing MANUAL route if needed, or remove if INPUT replaces it)

        composable(Routes.CAMERA) {
            ScanScreen(
                onNavigateToReview = { document ->
                    // For now, we can pass it via a shared ViewModel or just navigate.
                    // Since passing complex objects in Navigation is tricky without extras,
                    // we might need a workaround or just navigate to a placeholder.
                    navController.navigate(Routes.MANUAL) // Placeholder for now
                }
            )
        }
        
        // Other routes...
    }
}