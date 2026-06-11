package com.bpkpad.arsip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.bpkpad.arsip.core.domain.repository.AuthRepository
import com.bpkpad.arsip.ui.navigation.NavGraph
import com.bpkpad.arsip.ui.theme.ArsipBPKADTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArsipBPKADTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    authRepository = authRepository
                )
            }
        }
    }
}