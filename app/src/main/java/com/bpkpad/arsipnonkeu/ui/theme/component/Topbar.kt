package com.bpkpad.arsipnonkeu.ui.theme.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpkpad.arsipnonkeu.R

/**
 * TopBar Component - Reusable top navigation bar for BPKPAD Balangan app.
 *
 * Diperbarui berdasarkan desain konversi HTML yang diberikan.
 *
 * @param title Judul yang ditampilkan di sebelah logo. Default "BPKPAD Balangan".
 * @param onProfileClick Callback saat tombol profil di-tap.
 */
@Composable
fun TopBar(
    title: String = "BPKPAD Balangan",
    onProfileClick: () -> Unit = {},
    navigationIcon: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.White)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo + Title Group
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Navigation Icon (Back Button)
                if (navigationIcon != null) {
                    navigationIcon()
                }

                // Logo Balangan
                Image(
                    painter = painterResource(id = R.drawable.logo_balangan),
                    contentDescription = "Logo BPKPAD",
                    modifier = Modifier.size(width = 28.dp, height = 38.dp)
                )

                // Title Text
                Text(
                    text = title,
                    color = Color(0xFF0D631B),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black, // Weight 900
                    lineHeight = 28.sp
                )
            }

            // Profile Button (Circle background #D5ECF8)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD5ECF8))
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF40493D),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Border Bottom 1px #CFE6F2
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFCFE6F2))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar()
}
