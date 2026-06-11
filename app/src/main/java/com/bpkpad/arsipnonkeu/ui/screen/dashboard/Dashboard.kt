package com.bpkpad.arsipnonkeu.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bpkpad.arsipnonkeu.domain.model.ArchiveYearSummary
import com.bpkpad.arsipnonkeu.ui.component.TopBar
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray

val PoppinsFont = FontFamily.Default

@Composable
fun DashboardScreen(
    onProfileClick: () -> Unit = {},
    onArchiveYearClick: (Int) -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadYears()
    }

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
            item {
                DashboardHeaderSection()
            }

            item {
                AnnualArchivesSection(
                    isLoading = uiState.isLoading,
                    years = uiState.years,
                    errorMessage = uiState.errorMessage,
                    onYearClick = onArchiveYearClick
                )
            }

//            item {
//                StagingStatusSection(onPushAllClick = onPushAllClick)
//            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DashboardHeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF2E7D32))
            .padding(horizontal = 32.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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

        Text(
            text = "Pilih tahun arsip untuk melihat daftar dokumen.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontFamily = PoppinsFont,
            fontWeight = FontWeight.Normal,
            color = Color(0xFFE7FFE2)
        )
    }
}

@Composable
private fun AnnualArchivesSection(
    isLoading: Boolean,
    years: List<ArchiveYearSummary>,
    errorMessage: String?,
    onYearClick: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    fontFamily = PoppinsFont,
                    color = Color(0xFFBA1A1A)
                )
            }

            years.isEmpty() -> {
                Text(
                    text = "Belum ada data arsip.",
                    fontSize = 14.sp,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF40493D)
                )
            }

            else -> {
                YearCardGrid(
                    years = years,
                    onYearClick = onYearClick
                )
            }
        }
    }
}

@Composable
private fun YearCardGrid(
    years: List<ArchiveYearSummary>,
    onYearClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        years.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { summary ->
                    YearCard(
                        summary = summary,
                        onClick = { onYearClick(summary.year) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun YearCard(
    summary: ArchiveYearSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(120.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(0.5.dp, Color(0xFFBFCABA), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = summary.year.toString(),
            fontSize = 36.sp,
            lineHeight = 36.sp,
            fontFamily = PoppinsFont,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D631B)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "${summary.documentCount} dokumen",
                fontSize = 18.sp,
                lineHeight = 18.sp,
                fontFamily = PoppinsFont,
                fontWeight = FontWeight.Medium,
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