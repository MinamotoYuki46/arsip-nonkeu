package com.bpkpad.arsipnonkeu.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray
import com.bpkpad.arsipnonkeu.ui.component.BottomBar
import com.bpkpad.arsipnonkeu.ui.component.TopBar

val PoppinsFont = FontFamily.Default

@Composable
fun ProfileScreen(
    userName: String = "Lorem Ipsum",
    userRole: String = "Arsiparis BPKPAD",
    onBackClick: () -> Unit = {},
    onNavItemSelected: (String) -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val selectedRoute = "profile"

    Scaffold(
        topBar = {
            TopBar(
                title = "Profil Pengguna",
                showProfileButton = false,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header Section (Avatar + Name + Badges) ──────────────
            ProfileDisplaySection(userName, userRole)

            // ── Logout Button ─────────────────────────────
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFDAD6), contentColor = Color(0xFFBA1A1A))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Keluar dari Akun",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProfileDisplaySection(name: String, role: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Avatar Frame
        Box(
            modifier = Modifier
                .size(160.dp)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color(0xFFCFE6F2)) 
                .border(4.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(110.dp),
                tint = Color(0xFF0D631B).copy(alpha = 0.4f)
            )
        }

        // Info
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = name,
                fontSize = 28.sp,
                fontFamily = PoppinsFont,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF071E27)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0xFFE8F5E9)) 
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = role,
                    fontSize = 14.sp,
                    fontFamily = PoppinsFont,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B5E20),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
