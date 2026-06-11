package com.bpkpad.arsipnonkeu.ui.theme.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray
import com.bpkpad.arsipnonkeu.ui.theme.component.BottomBar
import com.bpkpad.arsipnonkeu.ui.theme.component.TopBar

// Menyesuaikan dengan definisi font di project Anda
val PoppinsFont = FontFamily.Default

@Composable
fun DashboardScreen(
    onProfileClick: () -> Unit = {},
    onNavItemSelected: (String) -> Unit = {},
    onArchiveYearClick: (Int) -> Unit = {},
    onPushAllClick: () -> Unit = {}
) {
    // Mengunci active state route agar BottomBar component mengenali halaman ini sebagai dashboard
    val selectedRoute = "dashboard"

    Scaffold(
        topBar = {
            TopBar(
                title = "BPKPAD Balangan",
                onProfileClick = onProfileClick
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Hero Search Section ──────────────────────────────────
            item { HeroSearchSection() }

            // ── Annual Archives Section (Bento Style) ────────────────
            item { AnnualArchivesSection(onYearClick = onArchiveYearClick) }

            // ── Staging Status Section ───────────────────────────────
            item { StagingStatusSection(onPushAllClick = onPushAllClick) }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero Search Section Component
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeroSearchSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF2E7D32))
            .padding(horizontal = 48.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Archival\nRepository",
            fontSize = 40.sp,
            lineHeight = 48.sp,
            fontFamily = PoppinsFont,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCBFFC2)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9999.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFBFCABA), RoundedCornerShape(9999.dp))
                .padding(top = 18.dp, bottom = 19.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF707A6C),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Search records by year, keyword, or ID...",
                color = Color(0xFF6B7280),
                fontSize = 16.sp,
                fontFamily = PoppinsFont,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Annual Archives Section Component
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AnnualArchivesSection(onYearClick: (Int) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Annual\nArchives",
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontFamily = PoppinsFont,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0D631B)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LegendItem(color = Color(0xFF0D631B), label = "Finalized")
                LegendItem(color = Color(0xFFBA1A1A), label = "Restricted")
            }
        }

        listOf(2025, 2024, 2023, 2022).forEach { year ->
            YearCard(year = year, onClick = { onYearClick(year) })
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = PoppinsFont,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.48.sp,
            color = Color(0xFF40493D)
        )
    }
}

@Composable
private fun YearCard(year: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(15.6.dp))
            .clip(RoundedCornerShape(15.6.dp))
            .background(Color.White)
            .border(0.5.dp, Color(0xFFBFCABA), RoundedCornerShape(15.6.dp))
            .clickable(onClick = onClick)
            .padding(13.7.dp),
        verticalArrangement = Arrangement.spacedBy(11.7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = year.toString(),
                fontSize = 25.sp,
                lineHeight = 19.5.sp,
                fontFamily = PoppinsFont,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D631B)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4888.dp))
                    .background(Color(0xFFACF4A4))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "CURRENT",
                    fontSize = 6.sp,
                    lineHeight = 7.8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF307231),
                    letterSpacing = 0.29.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "1,248 Records",
                    fontSize = 7.sp,
                    lineHeight = 9.8.sp,
                    fontFamily = PoppinsFont,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF40493D),
                    letterSpacing = 0.14.sp
                )
                Text(
                    text = "Updated 2h ago",
                    fontSize = 7.sp,
                    lineHeight = 9.8.sp,
                    fontFamily = PoppinsFont,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF40493D).copy(alpha = 0.7f)
                )
            }

            Text(
                text = "›",
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF0D631B)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Staging Status Section Component
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StagingStatusSection(onPushAllClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFFCFE6F2))
            .border(1.dp, Color(0xFFBFCABA), RoundedCornerShape(32.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "⇄", fontSize = 24.sp, color = Color(0xFF0D631B))

                Text(
                    text = "Staging\nStatus",
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    fontFamily = PoppinsFont,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF071E27)
                )
            }

            Text(
                text = "Push All to\nDatabase",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontFamily = PoppinsFont,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0D631B),
                textAlign = TextAlign.End,
                letterSpacing = 0.28.sp,
                modifier = Modifier.clickable(onClick = onPushAllClick)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                StagingFileRow(fileName = "Land_Tax_Report_2024_Q1.pdf")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFBFCABA)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3 items in staging •\nTotal 48.5 MB",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF40493D)
                )

                Box(
                    modifier = Modifier
                        .width(129.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xFFCFE6F2))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.66f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFF0D631B))
                    )
                }
            }
        }
    }
}

@Composable
private fun StagingFileRow(fileName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFBFCABA), RoundedCornerShape(32.dp))
            .padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 33.dp, height = 40.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(Color(0xFFACF4A4)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📄", fontSize = 18.sp)
        }

        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = fileName,
                fontSize = 11.5.sp,
                lineHeight = 16.4.sp,
                fontFamily = PoppinsFont,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF071E27),
                letterSpacing = 0.23.sp
            )
            Text(
                text = "Waiting for signature verification •\n2.4 MB",
                fontSize = 11.5.sp,
                lineHeight = 16.4.sp,
                fontFamily = PoppinsFont,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF40493D)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen()
}