package com.bpkpad.arsipnonkeu.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bpkpad.arsipnonkeu.R
import com.bpkpad.arsipnonkeu.domain.model.ArchiveYearSummary
import com.bpkpad.arsipnonkeu.ui.component.ArsipTextField
import com.bpkpad.arsipnonkeu.ui.component.LoadingIndicator
import com.bpkpad.arsipnonkeu.ui.component.TopBar
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray

val PoppinsFont = FontFamily.Default

@Composable
fun DashboardScreen(
    userRole: String = "",
    onProfileClick: () -> Unit = {},
    onArchiveYearClick: (Int) -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddYearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadYears()
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.dashboard_title),
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
                    userRole = userRole,
                    onYearClick = onArchiveYearClick,
                    onAddYearClick = { showAddYearDialog = true }
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

    if (showAddYearDialog) {
        var yearInput by remember { mutableStateOf("") }
        val isDuplicate = remember(yearInput, uiState.years) {
            yearInput.toIntOrNull()?.let { inputYear ->
                uiState.years.any { it.year == inputYear }
            } ?: false
        }
        
        AlertDialog(
            onDismissRequest = { showAddYearDialog = false },
            title = { Text(stringResource(R.string.dashboard_add_year_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.dashboard_add_year_dialog_message))
                    ArsipTextField(
                        value = yearInput,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) yearInput = it },
                        label = stringResource(R.string.dashboard_year_label),
                        placeholder = stringResource(R.string.dashboard_year_placeholder),
                        isError = isDuplicate,
                        singleLine = true
                    )
                    if (isDuplicate) {
                        Text(
                            text = stringResource(R.string.dashboard_error_duplicate_year, yearInput),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val year = yearInput.toIntOrNull()
                        if (year != null && year > 1900 && !isDuplicate) {
                            viewModel.addNewYear(year)
                            showAddYearDialog = false
                        }
                    },
                    enabled = yearInput.length == 4 && !isDuplicate
                ) {
                    Text(stringResource(R.string.dashboard_add_button), color = Color(0xFF0D631B))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddYearDialog = false }) {
                    Text(stringResource(R.string.dashboard_cancel_button))
                }
            }
        )
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
            text = stringResource(R.string.dashboard_header_title),
            fontSize = 40.sp,
            lineHeight = 48.sp,
            fontFamily = PoppinsFont,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCBFFC2)
        )

        Text(
            text = stringResource(R.string.dashboard_header_subtitle),
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
    userRole: String,
    onYearClick: (Int) -> Unit,
    onAddYearClick: () -> Unit
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
                    LoadingIndicator()
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

            else -> {
                YearCardGrid(
                    years = years,
                    userRole = userRole,
                    onYearClick = onYearClick,
                    onAddYearClick = onAddYearClick
                )
            }
        }
    }
}

@Composable
private fun YearCardGrid(
    years: List<ArchiveYearSummary>,
    userRole: String,
    onYearClick: (Int) -> Unit,
    onAddYearClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Combiner years with an "Add" placeholder only if ARSIPARIS
        val isArsiparis = userRole.equals("ARSIPARIS", ignoreCase = true)
        val items = if (isArsiparis) years + null else years
        
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { summary ->
                    if (summary != null) {
                        YearCard(
                            summary = summary,
                            onClick = { onYearClick(summary.year) },
                            modifier = Modifier.weight(1f)
                        )
                    } else if (isArsiparis) {
                        AddYearCard(
                            onClick = onAddYearClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AddYearCard(
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
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = Color(0xFF0D631B)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.dashboard_add_year_card),
            fontSize = 16.sp,
            fontFamily = PoppinsFont,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D631B)
        )
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
                text = stringResource(R.string.dashboard_document_count, summary.documentCount),
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
