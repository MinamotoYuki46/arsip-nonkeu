package com.bpkpad.arsipnonkeu.ui.screen.staging

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray
import com.bpkpad.arsipnonkeu.ui.component.QuickActionFab
import com.bpkpad.arsipnonkeu.ui.component.TopBar

val PoppinsFont = FontFamily.Default

data class StagedDocument(
    val fileName: String,
    val size: String,
    val status: String,
    val type: String
)

@Composable
fun StagingScreen(
    onBackClick: () -> Unit = {},
    onManualClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    onPushAllClick: () -> Unit = {}
) {
    val stagedItems = listOf(
        StagedDocument("Surat_Perintah_2024_01.pdf", "1.2 MB", "Ready to Sync", "Surat Masuk"),
        StagedDocument("Laporan_Keuangan_Q1.pdf", "4.5 MB", "Metadata Incomplete", "Laporan"),
        StagedDocument("Aset_Daerah_Update.png", "800 KB", "Ready to Sync", "Berita Acara")
    )

    Scaffold(
        topBar = {
            TopBar(
                title = "Staging Area",
                onProfileClick = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            QuickActionFab(
                onManualClick = onManualClick,
                onScanClick = onScanClick,
                onImportClick = onImportClick
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Header Section ────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Antrean Unggahan",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFont,
                        color = Color(0xFF071E27)
                    )
                    Text(
                        text = "Dokumen di bawah ini belum tersimpan ke database utama. Lakukan validasi sebelum melakukan sinkronisasi.",
                        fontSize = 14.sp,
                        fontFamily = PoppinsFont,
                        color = Color(0xFF40493D),
                        lineHeight = 20.sp
                    )
                }
            }

            // ── Push Action Card ──────────────────────────────────────
            item {
                PushActionCard(
                    itemCount = stagedItems.size,
                    totalSize = "6.5 MB",
                    onPushClick = onPushAllClick
                )
            }

            // ── Staged Items List ─────────────────────────────────────
            items(stagedItems) { item ->
                StagingItemRow(item)
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun PushActionCard(
    itemCount: Int,
    totalSize: String,
    onPushClick: () -> Unit
) {
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
            Column {
                Text(
                    text = "$itemCount Dokumen Menunggu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF071E27),
                    fontFamily = PoppinsFont
                )
                Text(
                    text = "Total Ukuran: $totalSize",
                    fontSize = 14.sp,
                    color = Color(0xFF40493D),
                    fontFamily = PoppinsFont
                )
            }

            Button(
                onClick = onPushClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)),
                shape = RoundedCornerShape(9999.dp)
            ) {
                Text("Sync All", fontWeight = FontWeight.Bold)
            }
        }

        // Progress bar simulation
        LinearProgressIndicator(
            progress = 0.4f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = Color(0xFF0D631B),
            trackColor = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun StagingItemRow(item: StagedDocument) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFCFE6F2), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📄", fontSize = 24.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.fileName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF071E27),
                maxLines = 1
            )
            Text(
                text = "${item.type} • ${item.size}",
                fontSize = 12.sp,
                color = Color(0xFF707A6C)
            )
            Text(
                text = item.status,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (item.status == "Ready to Sync") Color(0xFF1B5E20) else Color(0xFFBA1A1A)
            )
        }

        IconButton(onClick = { /* Delete/Edit from staging */ }) {
            Text("⋮", fontSize = 20.sp, color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StagingScreenPreview() {
    StagingScreen()
}
