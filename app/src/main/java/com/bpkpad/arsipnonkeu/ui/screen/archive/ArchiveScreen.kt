package com.bpkpad.arsipnonkeu.ui.screen.archive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentFilter
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentListItem
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.ui.component.TopBar
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.bpkpad.arsipnonkeu.util.ArchiveExcelService

private val PoppinsFont = FontFamily.Default

@Composable
fun ArchiveScreen(
    selectedYear: Int,
    onDocumentClick: (String) -> Unit = {},
    onStagingClick: () -> Unit = {},
    viewModel: ArchiveViewModel = remember { ArchiveViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    val excelExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ),
        onResult = { uri ->
            if (uri != null) {
                try {
                    ArchiveExcelService.exportArchiveDocuments(
                        context = context,
                        uri = uri,
                        documents = uiState.documents
                    )

                    Toast.makeText(
                        context,
                        "Data arsip berhasil diekspor",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (exception: Exception) {
                    Toast.makeText(
                        context,
                        exception.message ?: "Gagal mengekspor data arsip",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    )

    var showExportDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(selectedYear) {
        viewModel.loadDocumentsByYear(selectedYear)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Arsip $selectedYear",
                onProfileClick = {}
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStagingClick,
                containerColor = Color(0xFF0D631B),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah dokumen"
                )
            }
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ArchiveControlSection(
                selectedYear = selectedYear,
                filter = uiState.filter,
                resultCount = uiState.documents.size,
                onKeywordChange = viewModel::updateKeyword,
                onFilterClick = { showFilterSheet = true },
                onExportClick = {
                    excelExportLauncher.launch("arsip-$selectedYear.xlsx")
                }
            )

            ArchiveContentSection(
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                documents = uiState.documents,
                onDocumentClick = onDocumentClick
            )
        }
    }

    if (showFilterSheet) {
        ArchiveFilterBottomSheet(
            filter = uiState.filter,
            onDismiss = { showFilterSheet = false },
            onDocumentTypeChange = viewModel::updateDocumentType,
            onStatusChange = viewModel::updateStatus,
            onPhysicalFormChange = viewModel::updatePhysicalForm,
            onConditionChange = viewModel::updateCondition,
            onResetFilter = {
                viewModel.resetFilter()
                showFilterSheet = false
            }
        )
    }

    if (showExportDialog) {
        ExportDataDialog(
            year = selectedYear,
            documentCount = uiState.documents.size,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
private fun ArchiveControlSection(
    selectedYear: Int,
    filter: ArchiveDocumentFilter?,
    resultCount: Int,
    onKeywordChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundGray)
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SearchInput(
            keyword = filter?.keyword.orEmpty(),
            onKeywordChange = onKeywordChange
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$resultCount dokumen • Tahun $selectedYear",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PoppinsFont,
                color = Color(0xFF40493D)
            )

            Text(
                text = activeFilterText(filter),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PoppinsFont,
                color = Color(0xFF707A6C)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onFilterClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(9999.dp),
                border = BorderStroke(1.dp, Color(0xFF0D631B))
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = Color(0xFF0D631B),
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Filter",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF0D631B)
                )
            }

            Button(
                onClick = onExportClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)),
                shape = RoundedCornerShape(9999.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Ekspor",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PoppinsFont,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ArchiveContentSection(
    isLoading: Boolean,
    errorMessage: String?,
    documents: List<ArchiveDocumentListItem>,
    onDocumentClick: (String) -> Unit
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        errorMessage != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFBA1A1A),
                    fontSize = 14.sp,
                    fontFamily = PoppinsFont,
                    textAlign = TextAlign.Center
                )
            }
        }

        documents.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada dokumen untuk filter saat ini.",
                    color = Color(0xFF40493D),
                    fontSize = 14.sp,
                    fontFamily = PoppinsFont,
                    textAlign = TextAlign.Center
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 32.dp,
                    end = 32.dp,
                    top = 16.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = documents,
                    key = { it.document.id }
                ) { item ->
                    ArchiveDocumentCard(
                        item = item,
                        onDetailClick = {
                            onDocumentClick(item.document.id)
                        }
                    )
                }

                item {
                    ArchiveListFooter(totalFound = documents.size)
                }
            }
        }
    }
}

@Composable
private fun SearchInput(
    keyword: String,
    onKeywordChange: (String) -> Unit
) {
    OutlinedTextField(
        value = keyword,
        onValueChange = onKeywordChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Cari judul, nomor dokumen, kode, instansi, atau rak...",
                fontSize = 14.sp,
                fontFamily = PoppinsFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF707A6C)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(9999.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF0D631B),
            unfocusedBorderColor = Color(0xFFBFCABA)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveFilterBottomSheet(
    filter: ArchiveDocumentFilter?,
    onDismiss: () -> Unit,
    onDocumentTypeChange: (DocumentType?) -> Unit,
    onStatusChange: (DocumentStatus?) -> Unit,
    onPhysicalFormChange: (PhysicalForm?) -> Unit,
    onConditionChange: (DocumentCondition?) -> Unit,
    onResetFilter: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Filter Dokumen",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF071E27)
                )

                Text(
                    text = "Atur filter untuk mempersempit daftar arsip.",
                    fontSize = 14.sp,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF40493D)
                )
            }

            FilterChipRow(
                title = "Jenis Dokumen",
                selected = filter?.documentType,
                values = DocumentType.values().toList(),
                label = { it.label },
                onSelected = onDocumentTypeChange
            )

            FilterChipRow(
                title = "Status",
                selected = filter?.status,
                values = DocumentStatus.values().toList(),
                label = { it.label },
                onSelected = onStatusChange
            )

            FilterChipRow(
                title = "Bentuk Fisik",
                selected = filter?.physicalForm,
                values = PhysicalForm.values().toList(),
                label = { it.label },
                onSelected = onPhysicalFormChange
            )

            FilterChipRow(
                title = "Kondisi",
                selected = filter?.condition,
                values = DocumentCondition.values().toList(),
                label = { it.label },
                onSelected = onConditionChange
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onResetFilter,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(9999.dp),
                    border = BorderStroke(1.dp, Color(0xFF0D631B))
                ) {
                    Text(
                        text = "Reset",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = PoppinsFont,
                        color = Color(0xFF0D631B)
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)),
                    shape = RoundedCornerShape(9999.dp)
                ) {
                    Text(
                        text = "Terapkan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = PoppinsFont,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> FilterChipRow(
    title: String,
    selected: T?,
    values: List<T>,
    label: (T) -> String,
    onSelected: (T?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PoppinsFont,
            color = Color(0xFF40493D),
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SimpleFilterChip(
                label = "Semua",
                isSelected = selected == null,
                onClick = { onSelected(null) }
            )

            values.forEach { value ->
                SimpleFilterChip(
                    label = label(value),
                    isSelected = selected == value,
                    onClick = { onSelected(value) }
                )
            }
        }
    }
}

@Composable
private fun SimpleFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(if (isSelected) Color(0xFF2E7D32) else Color.White)
            .then(
                if (!isSelected) {
                    Modifier.border(
                        width = 1.dp,
                        color = Color(0xFFBFCABA),
                        shape = RoundedCornerShape(9999.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PoppinsFont,
            color = if (isSelected) Color(0xFFCBFFC2) else Color(0xFF40493D)
        )
    }
}

@Composable
private fun ArchiveDocumentCard(
    item: ArchiveDocumentListItem,
    onDetailClick: () -> Unit
) {
    val document = item.document

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0x33BFCABA), RoundedCornerShape(24.dp))
            .clickable(onClick = onDetailClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = document.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF071E27),
                    lineHeight = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = document.documentNumber ?: document.classificationCode ?: "-",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF707A6C),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            StatusBadge(status = document.status)
        }

        HorizontalDivider(color = Color(0x4DBFCABA), thickness = 1.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetadataItem(
                label = "LOKASI / RAK",
                value = item.locationLabel,
                modifier = Modifier.weight(1f)
            )

            MetadataItem(
                label = "JENIS",
                value = document.documentType.label,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetadataItem(
                label = "BENTUK",
                value = document.physicalForm.label,
                modifier = Modifier.weight(1f)
            )

            MetadataItem(
                label = "KONDISI",
                value = document.condition?.label ?: "Tidak diketahui",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetadataItem(
                label = "KEASLIAN",
                value = when(document.isCopy) {
                    true -> "Kopi"
                    false -> "Asli"
                    null -> "Tidak diketahui"
                },
                modifier = Modifier.weight(1f)
            )

            MetadataItem(
                label = "SALINAN",
                value = "${document.copyCount} berkas",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetadataItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PoppinsFont,
            color = Color(0xFF707A6C),
            letterSpacing = 0.48.sp
        )

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PoppinsFont,
            color = Color(0xFF071E27),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusBadge(status: DocumentStatus) {
    val backgroundColor = when (status) {
        DocumentStatus.AVAILABLE -> Color(0xFFE8F5E9)
        DocumentStatus.BORROWED -> Color(0xFFFFF3CD)
        DocumentStatus.DISPOSED -> Color(0xFFFEE2E2)
    }

    val textColor = when (status) {
        DocumentStatus.AVAILABLE -> Color(0xFF1B5E20)
        DocumentStatus.BORROWED -> Color(0xFF92400E)
        DocumentStatus.DISPOSED -> Color(0xFF991B1B)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PoppinsFont,
            color = textColor
        )
    }
}

@Composable
private fun ArchiveListFooter(totalFound: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = {},
            enabled = false,
            border = BorderStroke(1.dp, Color(0xFFBFCABA)),
            shape = RoundedCornerShape(9999.dp),
            modifier = Modifier.height(44.dp),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Semua data sudah ditampilkan",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PoppinsFont,
                color = Color(0xFF707A6C)
            )
        }

        Text(
            text = "Menampilkan $totalFound dokumen ditemukan",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = PoppinsFont,
            color = Color(0xFF40493D)
        )
    }
}

@Composable
private fun ExportDataDialog(
    year: Int,
    documentCount: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Ekspor Data Arsip",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF071E27)
                )

                Text(
                    text = "Data yang diekspor mengikuti hasil filter yang sedang tampil.",
                    fontSize = 14.sp,
                    color = Color(0xFF40493D),
                    fontFamily = PoppinsFont
                )

                Text(
                    text = "Tahun: $year\nJumlah dokumen: $documentCount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF40493D),
                    fontFamily = PoppinsFont
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B))
                ) {
                    Text("Tutup")
                }
            }
        }
    }
}

private fun activeFilterText(filter: ArchiveDocumentFilter?): String {
    if (filter == null) return "Tanpa filter"

    val count = listOfNotNull(
        filter.documentType,
        filter.status,
        filter.physicalForm,
        filter.condition,
        filter.originInstance
    ).size

    return if (count == 0) {
        "Tanpa filter"
    } else {
        "$count filter aktif"
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=390dp,height=844dp,dpi=420"
)
@Composable
fun ArchiveScreenPreview() {
    ArchiveScreen(selectedYear = 2024)
}