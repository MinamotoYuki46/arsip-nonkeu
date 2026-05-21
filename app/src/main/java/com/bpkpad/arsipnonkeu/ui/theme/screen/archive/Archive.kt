package com.bpkpad.arsipnonkeu.ui.theme.screen.archive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray
import com.bpkpad.arsipnonkeu.ui.theme.PrimaryGreen
import com.bpkpad.arsipnonkeu.ui.theme.component.BottomBar
import com.bpkpad.arsipnonkeu.ui.theme.component.TopBar

// Fallback font definition
val PoppinsFont = FontFamily.Default

// ─────────────────────────────────────────────────────────────────────────────
// Data Models
// ─────────────────────────────────────────────────────────────────────────────

enum class DocumentStatus(val label: String, val color: Color, val textColor: Color) {
    VERIFIED("Verified", Color(0xFFE8F5E9), Color(0xFF1B5E20)),
    PENDING("Pending", Color(0xFFFEF3C7), Color(0xFF92400E)),
    ARCHIVED("Archived", Color(0xFFE8F5E9), Color(0xFF1B5E20)), // Same as verified in HTML snippet Card 3
    RESTRICTED("Restricted", Color(0xFFFEE2E2), Color(0xFF991B1B))
}

data class ArchiveDocument(
    val id: String,
    val title: String,
    val noRak: String,
    val tipe: String,
    val status: DocumentStatus,
    val thumbnailColor: Color = Color(0xFFCFE6F2)
)

// ─────────────────────────────────────────────────────────────────────────────
// Static Sample Data
// ─────────────────────────────────────────────────────────────────────────────

private val sampleDocuments = listOf(
    ArchiveDocument(
        id = "#2023-A01",
        title = "Laporan Realisasi Anggaran Triwulan I -…",
        noRak = "RAK-04-B",
        tipe = "Laporan",
        status = DocumentStatus.VERIFIED
    ),
    ArchiveDocument(
        id = "#2023-B12",
        title = "Surat Keputusan Bupati No. 45/2023 -…",
        noRak = "RAK-01-A",
        tipe = "SK Resmi",
        status = DocumentStatus.PENDING
    ),
    ArchiveDocument(
        id = "#2023-C88",
        title = "Proposal Pengadaan Alat Kantor BPKPAD TA 2023",
        noRak = "RAK-09-C",
        tipe = "Proposal",
        status = DocumentStatus.ARCHIVED
    ),
    ArchiveDocument(
        id = "#2023-M05",
        title = "Berita Acara Serah Terima Aset Daerah - Kendaraan…",
        noRak = "RAK-02-B",
        tipe = "Berita Acara",
        status = DocumentStatus.VERIFIED
    ),
    ArchiveDocument(
        id = "#2023-S09",
        title = "Daftar Inventaris Kantor Semester II - Ruang Rapat",
        noRak = "RAK-12-A",
        tipe = "Inventaris",
        status = DocumentStatus.VERIFIED
    ),
    ArchiveDocument(
        id = "#2023-X99",
        title = "Evaluasi Kinerja Tahunan Tenaga Kontrak Daerah",
        noRak = "RAK-SAFE-1",
        tipe = "Confidential",
        status = DocumentStatus.RESTRICTED
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// ArchiveScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArchiveScreen(
    onNavItemSelected: (String) -> Unit = {},
    onDocumentClick: (ArchiveDocument) -> Unit = {}
) {
    var selectedRoute by remember { mutableStateOf("archive") }
    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(onProfileClick = {})
        },
        bottomBar = {
            BottomBar(
                selectedRoute = selectedRoute,
                onItemSelected = { route ->
                    selectedRoute = route
                    onNavItemSelected(route)
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
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Header Section ────────────────────────────────────────
            ArchiveHeader(onExportClick = { showExportDialog = true })

            // ── Search & Filter Controls ──────────────────────────────
            SearchFilterSection()

            // ── Archive Grid ──────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                sampleDocuments.forEach { doc ->
                    DocumentCard(
                        document = doc,
                        onDetailClick = { onDocumentClick(doc) }
                    )
                }
            }

            // ── Pagination Section ────────────────────────────────────
            PaginationSection()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showExportDialog) {
        ExportDataDialog(onDismiss = { showExportDialog = false })
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ArchiveHeader(onExportClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Rekap Per Tahun",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFont,
                lineHeight = 48.sp,
                color = Color(0xFF071E27)
            )
            Text(
                text = buildAnnotatedString {
                    append("Menampilkan arsip dokumen resmi\nuntuk periode anggaran ")
                    withStyle(style = SpanStyle(color = Color(0xFF0D631B), fontWeight = FontWeight.Bold)) {
                        append("2025")
                    }
                },
                fontSize = 18.sp,
                fontFamily = PoppinsFont,
                lineHeight = 28.sp,
                color = Color(0xFF40493D)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(
                text = "Ganti Tahun",
                icon = Icons.Default.CalendarToday,
                onClick = {}
            )
            ActionButton(
                text = "Ekspor Data",
                icon = Icons.Outlined.FileDownload,
                onClick = onExportClick
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)),
        shape = RoundedCornerShape(9999.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        modifier = Modifier.height(52.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PoppinsFont,
            color = Color.White
        )
    }
}

@Composable
private fun SearchFilterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFFE6F6FF))
            .border(1.dp, Color(0x4DBFCABA), RoundedCornerShape(32.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9999.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFBFCABA), RoundedCornerShape(9999.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF707A6C),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Cari nomor dokumen, perihal, atau lokasi rak...",
                color = Color(0xFF6B7280),
                fontSize = 16.sp,
                fontFamily = PoppinsFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(label = "Semua Tipe", isSelected = true)
            FilterChip(label = "Surat Masuk", isSelected = false)
            FilterChip(label = "Surat Keluar", isSelected = false)
            FilterChip(label = "Laporan Keuangan", isSelected = false)
        }
    }
}

@Composable
private fun FilterChip(label: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(if (isSelected) Color(0xFF2E7D32) else Color.White)
            .then(
                if (!isSelected) Modifier.border(1.dp, Color(0xFFBFCABA), RoundedCornerShape(9999.dp))
                else Modifier
            )
            .clickable {}
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PoppinsFont,
            color = if (isSelected) Color(0xFFCBFFC2) else Color(0xFF40493D)
        )
    }
}

@Composable
private fun DocumentCard(
    document: ArchiveDocument,
    onDetailClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .border(1.dp, Color(0x33BFCABA), RoundedCornerShape(32.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 96.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(document.thumbnailColor)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = document.status)
                    Text(
                        text = document.id,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = PoppinsFont,
                        color = Color(0xFF707A6C)
                    )
                }

                Text(
                    text = document.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF071E27),
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NO. RAK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFont,
                            color = Color(0xFF707A6C),
                            letterSpacing = 0.48.sp
                        )
                        Text(
                            text = document.noRak,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFont,
                            color = Color(0xFF071E27)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TIPE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFont,
                            color = Color(0xFF707A6C),
                            letterSpacing = 0.48.sp
                        )
                        Text(
                            text = document.tipe,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFont,
                            color = Color(0xFF071E27)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0x4DBFCABA), thickness = 1.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onDetailClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = Color(0xFF0D631B),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Lihat Detail",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF0D631B)
                )
            }
            IconButton(onClick = {}) {
                Text(text = "⋮", fontSize = 20.sp, color = Color(0xFF40493D))
            }
        }
    }
}

@Composable
private fun StatusBadge(status: DocumentStatus) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(status.color)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PoppinsFont,
            color = status.textColor,
            letterSpacing = 0.48.sp
        )
    }
}

@Composable
private fun PaginationSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = {},
            border = BorderStroke(2.dp, Color(0xFF0D631B)),
            shape = RoundedCornerShape(9999.dp),
            modifier = Modifier
                .height(44.dp)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Muat Lebih Banyak",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PoppinsFont,
                color = Color(0xFF0D631B)
            )
        }
        Text(
            text = "Menampilkan 6 dari 482 dokumen ditemukan",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = PoppinsFont,
            color = Color(0xFF40493D)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dialog (Existing logic kept but styled if needed)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExportDataDialog(onDismiss: () -> Unit) {
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
                    text = "Pilih Tahun untuk Ekspor data ke format dokumen yang di inginkan.",
                    fontSize = 14.sp,
                    color = Color(0xFF40493D),
                    fontFamily = PoppinsFont
                )
                
                // Placeholder for dialog content
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

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=390dp,height=844dp,dpi=420"
)
@Composable
fun ArchiveScreenPreview() {
    ArchiveScreen()
}
