package com.bpkpad.arsipnonkeu.ui.screen.profile.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Fallback font definition
val PoppinsFont = FontFamily.Default

data class UserActivityItem(
    val deviceName: String,
    val loginType: String,
    val sessionId: String,
    val status: String,
    val date: String,
    val time: String,
    val location: String,
    val isActive: Boolean = false
)

@Composable
fun UserActivityScreen(
    onBackClick: () -> Unit = {}
) {
    val activities = listOf(
        UserActivityItem(
            deviceName = "Samsung Galaxy S23 Ultra",
            loginType = "Aplikasi Android",
            sessionId = "#SESS-8829-BPKPAD-2024",
            status = "Aktif",
            date = "12 Feb 2024",
            time = "14:20 WITA",
            location = "Banjarbaru",
            isActive = true
        ),
        UserActivityItem(
            deviceName = "Xiaomi 13 Pro",
            loginType = "Aplikasi Android",
            sessionId = "#SESS-7721-BPKPAD-2024",
            status = "Selesai",
            date = "10 Feb 2024",
            time = "09:15 WITA",
            location = "Balangan"
        ),
        UserActivityItem(
            deviceName = "Google Pixel 7",
            loginType = "Aplikasi Android",
            sessionId = "#SESS-6612-BPKPAD-2024",
            status = "Selesai",
            date = "08 Feb 2024",
            time = "16:45 WITA",
            location = "Paringin"
        )
    )

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black) // Dark theme as per image
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                // Back Button & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Aktivitas Login",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFont
                    )
                }

                // Search Bar
                SearchBarPlaceholder()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Filters
                FilterRowPlaceholder()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Riwayat Aktivitas",
                        color = Color(0xFF40493D),
                        fontSize = 14.sp,
                        fontFamily = PoppinsFont
                    )
                    Text(
                        text = "${activities.size} sesi ditemukan",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontFamily = PoppinsFont
                    )
                }
            }
        },
        containerColor = Color.Black // Matches image background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            items(activities) { activity ->
                ActivityCard(activity)
            }
        }
    }
}

@Composable
private fun SearchBarPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9999.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "Cari riwayat perangkat...",
            color = Color.Gray,
            fontSize = 16.sp,
            fontFamily = PoppinsFont,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.Cancel,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun FilterRowPlaceholder() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            tint = Color.Gray
        )
        
        // Year Filter
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9999.dp))
                .background(Color(0xFF0D631B))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Tahun: 2024", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Cancel, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun ActivityCard(activity: UserActivityItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFCFE6F2), RoundedCornerShape(24.dp))
    ) {
        // Top Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.deviceName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF071E27),
                    fontFamily = PoppinsFont
                )
                Text(
                    text = activity.loginType,
                    fontSize = 14.sp,
                    color = Color(0xFF40493D),
                    fontFamily = PoppinsFont
                )
                Text(
                    text = activity.sessionId,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontFamily = PoppinsFont,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Status Badge
            val statusColor = if (activity.isActive) Color(0xFFACF4A4) else Color(0xFFCFE6F2)
            val textColor = if (activity.isActive) Color(0xFF0D631B) else Color(0xFF40493D)
            val dotColor = if (activity.isActive) Color(0xFF2A6B2C) else Color(0xFF0D631B)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(statusColor)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(dotColor)
                )
                Text(
                    text = if (activity.isActive) "Sedang Login" else "Sudah Logout",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontFamily = PoppinsFont
                )
            }
        }

        // Bottom Section (Bento Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE6F6FF))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoColumn(label = "TANGGAL", value = activity.date)
            InfoColumn(label = "WAKTU", value = activity.time)
            InfoColumn(label = "LOKASI", value = activity.location)
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF78716C),
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF071E27),
            fontFamily = PoppinsFont
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserActivityScreenPreview() {
    UserActivityScreen()
}
