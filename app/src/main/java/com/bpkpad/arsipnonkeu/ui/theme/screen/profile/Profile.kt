package com.bpkpad.arsipnonkeu.ui.theme.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray
import com.bpkpad.arsipnonkeu.ui.theme.component.BottomBar
import com.bpkpad.arsipnonkeu.ui.theme.component.TopBar

val PoppinsFont = FontFamily.Default

@Composable
fun ProfileScreen(
    onNavItemSelected: (String) -> Unit = {},
    onProfileSettingsClick: () -> Unit = {},
    onActivityHistoryClick: () -> Unit = {},
    onHelpSupportClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val selectedRoute = "profile"

    Scaffold(
        topBar = {
            TopBar(
                title = "Profil Pengguna",
                onProfileClick = {} // Halaman aktif profil, callback internal diabaikan
            )
        },
        bottomBar = {
            BottomBar(
                selectedRoute = selectedRoute,
                onItemSelected = { route ->
                    if (route != selectedRoute) {
                        onNavItemSelected(route)
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header Section (Avatar + Name + Badges) ──────────────
            ProfileHeaderSection()

            // ── Account Settings Section ─────────────────────────────
            AccountSettingsSection(
                onProfileSettingsClick = onProfileSettingsClick,
                onActivityHistoryClick = onActivityHistoryClick,
                onHelpSupportClick = onHelpSupportClick,
                onLogoutClick = onLogoutClick
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. Profile Header Section Component
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileHeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Avatar Frame dengan badge edit mengambang (Floating Action Area)
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFCFE6F2)) // background: #CFE6F2 dari HTML
                    .border(4.dp, Color.White, CircleShape), // outline: 4px white solid
                contentAlignment = Alignment.Center
            ) {
                // Representasi Faux Avatar Gambar AI dari Foto Anda
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(110.dp),
                    tint = Color(0xFF0D631B).copy(alpha = 0.4f)
                )
            }

            // Tombol edit mengambang hijau kecil di pojok kanan bawah foto profil
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .shadow(elevation = 4.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF0D631B)) // background: #0D631B
                    .clickable { /* Trigger Ganti Foto */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Username
        Text(
            text = "Lorem Ipsum",
            fontSize = 40.sp, // font-size: 40px
            lineHeight = 48.sp,
            fontFamily = PoppinsFont,
            fontWeight = FontWeight.Bold, // font-weight: 700
            color = Color(0xFF071E27) // color: #071E27
        )

        // Row Badges (Role & ID)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge Arsiparis BPKPAD
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0xFFE8F5E9)) // background: #E8F5E9
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Arsiparis BPKPAD",
                    fontSize = 12.sp,
                    fontFamily = PoppinsFont,
                    fontWeight = FontWeight.SemiBold, // font-weight: 600
                    color = Color(0xFF1B5E20), // color: #1B5E20
                    letterSpacing = 0.48.sp
                )
            }

            // Badge ID User
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0xFFDBF1FE)) // background: #DBF1FE
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ID: BAL-4920",
                    fontSize = 12.sp,
                    fontFamily = PoppinsFont,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF40493D), // color: #40493D
                    letterSpacing = 0.48.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Account Settings Section Component
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AccountSettingsSection(
    onProfileSettingsClick: () -> Unit,
    onActivityHistoryClick: () -> Unit,
    onHelpSupportClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Label Section Header
        /*Text(
            text = "Account",
            fontSize = 24.sp, // font-size: 24px
            lineHeight = 32.sp,
            fontFamily = PoppinsFont,
            fontWeight = FontWeight.SemiBold, // font-weight: 600
            color = Color(0xFF071E27)
        )*/

        // Menu Cards Stack
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {/*
            SettingMenuRow(
                title = "Profile Settings",
                subtitle = "Lorem Ipsum dot silor amet salwaliya",
                icon = Icons.Default.Person,
                iconBgColor = Color(0xFFACF4A4), // background #ACF4A4
                iconTintColor = Color(0xFF2A6B2C),
                onClick = onProfileSettingsClick
            )*/

            SettingMenuRow(
                title = "Activity History",
                subtitle = "Lorem Ipsum dot silor amet salwaliya",
                icon = Icons.Default.History,
                iconBgColor = Color(0xFFDBF1FE), // background #DBF1FE
                iconTintColor = Color(0xFF0D631B),
                onClick = onActivityHistoryClick
            )

            SettingMenuRow(
                title = "Help & Support",
                subtitle = "Lorem Ipsum dot silor amet salwaliya",
                icon = Icons.Default.HelpOutline,
                iconBgColor = Color(0xFFABF4AC), // background #ABF4AC
                iconTintColor = Color(0xFF07521D),
                onClick = onHelpSupportClick
            )

            SettingMenuRow(
                title = "Logout",
                subtitle = "Keluar dari akun",
                icon = Icons.AutoMirrored.Filled.Logout,
                iconBgColor = Color(0xFFFFDAD6), // background #FFDAD6 (Alert/Red Light)
                iconTintColor = Color(0xFFBA1A1A), // fill #BA1A1A
                onClick = onLogoutClick
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable Row Component untuk Menu Pengaturan
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingMenuRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp) // height: 90px dari HTML
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFFAFAF9), RoundedCornerShape(32.dp)) // outline: 1px #FAFAF9 solid
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Icon Rounded Container Frame
            Box(
                modifier = Modifier
                    .size(48.dp) // width & height: 48px
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTintColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Labels Column (Title & Subtitle)
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = title,
                    fontSize = 14.sp, // font-size: 14px
                    lineHeight = 20.sp,
                    fontFamily = PoppinsFont,
                    fontWeight = FontWeight.Bold, // font-weight: 600
                    color = Color(0xFF071E27),
                    letterSpacing = 0.28.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    fontFamily = PoppinsFont,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF78716C) // color: #78716C
                )
            }
        }

        // Chevron Right Icon penunjuk navigasi kontras rendah
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xA8A29E), // fill="#A8A29E"
            modifier = Modifier.size(24.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview Engine
// ─────────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}