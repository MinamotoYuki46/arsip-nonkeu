package com.bpkpad.arsipnonkeu.ui.theme.screen.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray

// Menggunakan Font Poppins (Fallback Default)
val PoppinsFont = FontFamily.Default

@Composable
fun DocumentDetailScreen(
    onBackClick: () -> Unit = {},
    onEditMetadataClick: () -> Unit = {},
    onExportPdfClick: () -> Unit = {},
    onViewFullScreenPdfClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            // Menggunakan komponen khusus internal topbar yang berbeda dengan halaman utama
            InternalDetailTopBar(onBackClick = onBackClick)
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // ── Header Actions & Title Section ───────────────────────
            HeaderActionsAndTitleSection(
                onEditMetadataClick = onEditMetadataClick,
                onExportPdfClick = onExportPdfClick
            )

            // ── Bento Grid Layout Section ────────────────────────────
            BentoGridLayout(
                onViewFullScreenPdfClick = onViewFullScreenPdfClick
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Komponen Khusus TopBar Internal (Bukan TopBar Utama Aplikasi)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun InternalDetailTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "←",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBackClick() }
            )
            Text(
                text = "Detail Dokumen",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFont,
                color = Color(0xFF071E27)
            )
        }
        Text(text = "⋮", fontSize = 20.sp, color = Color(0xFF071E27))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. Header Actions & Title Section
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeaderActionsAndTitleSection(
    onEditMetadataClick: () -> Unit,
    onExportPdfClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Badges Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SP2D Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xFFCFE6F2))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SP2D",
                        fontSize = 12.sp,
                        fontFamily = PoppinsFont,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.48.sp,
                        color = Color(0xFF40493D)
                    )
                }

                // Verified Status Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xFFACF4A4))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2A6B2C))
                    )
                    Text(
                        text = "Verified",
                        fontSize = 12.sp,
                        fontFamily = PoppinsFont,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.48.sp,
                        color = Color(0xFF307231)
                    )
                }
            }

            // Heading Title
            Text(
                text = "SP2D-2023-11-0045",
                fontSize = 24.sp,
                fontFamily = PoppinsFont,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 32.sp,
                color = Color(0xFF071E27),
                modifier = Modifier.padding(top = 4.dp)
            )

            // Description/Meta
            Text(
                text = "Uploaded on Nov 12, 2023 by Admin",
                fontSize = 16.sp,
                fontFamily = PoppinsFont,
                fontWeight = FontWeight.Normal,
                lineHeight = 24.sp,
                color = Color(0xFF40493D)
            )
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Edit Metadata Button
            OutlinedButton(
                onClick = onEditMetadataClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0D631B)),
                border = BorderStroke(1.dp, Color(0xFF707A6C)),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Metadata",
                    fontFamily = PoppinsFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    letterSpacing = 0.28.sp
                )
            }

            // Export PDF Button
            Button(
                onClick = onExportPdfClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)),
                contentPadding = PaddingValues(horizontal = 24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export PDF",
                    fontFamily = PoppinsFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    letterSpacing = 0.28.sp,
                    color = Color.White
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Bento Grid Layout Container
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BentoGridLayout(onViewFullScreenPdfClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(29.dp)
    ) {
        DigitalScanCard(onViewFullScreenPdfClick = onViewFullScreenPdfClick)
        MetadataCard()
        PhysicalLocationCard()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bento Component A: Digital Scan Preview
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DigitalScanCard(onViewFullScreenPdfClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(461.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFCFE6F2), RoundedCornerShape(32.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(73.dp)
                .background(Color(0xFFE6F6FF).copy(alpha = 0.5f))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🔍", fontSize = 20.sp)
                Text(
                    text = "Digital Scan",
                    fontSize = 22.sp,
                    fontFamily = PoppinsFont,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF071E27)
                )
            }
            IconButton(onClick = onViewFullScreenPdfClick) {
                Text(text = "⛶", fontSize = 20.sp, color = Color(0xFF0D631B))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFE6F6FF))
                .padding(vertical = 32.dp, horizontal = 24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFCFE6F2)),
            ) {
                // Faux Document Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp)
                        .border(width = 0.dp, color = Color.Transparent)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color(0xFFCFE6F2),
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFCFE6F2)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .width(137.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFCFE6F2))
                    )
                }

                // Faux Document Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .alpha(0.6f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFCFE6F2)))
                    Box(modifier = Modifier.width(215.dp).height(8.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFCFE6F2)))
                    Box(modifier = Modifier.width(172.dp).height(8.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFCFE6F2)))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(128.dp)
                            .border(2.dp, Color(0xFFCFE6F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "[TABLE DATA]",
                            color = Color(0xFF707A6C),
                            fontSize = 12.sp,
                            fontFamily = PoppinsFont,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFCFE6F2)))
                    Box(modifier = Modifier.width(193.dp).height(8.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFCFE6F2)))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bento Component B: Dokumen Metadata Info
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MetadataCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFCFE6F2), RoundedCornerShape(32.dp))
            .padding(25.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Color(0xFF0D631B),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Dokumen Metadata",
                fontSize = 22.sp,
                fontFamily = PoppinsFont,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF071E27)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MetadataFieldRow(
                term = "Nama dokumen",
                detail = "Surat Perintah Pencairan Dana\n(Dinas Pendidikan)"
            )
            MetadataFieldRow(
                term = "nomor dokumen",
                detail = "REF/PEND/2023/11/45",
                isMonoSpace = true
            )
            MetadataFieldRow(
                term = "dinas",
                detail = "Dinas Pendidikan dan Kebudayaan"
            )
            MetadataFieldRow(
                term = "tanggal dokumen",
                detail = "10 November 2023"
            )
            MetadataFieldRow(
                term = "masa berlaku",
                detail = "10 Tahun (Expired 2033)",
                hasBorderBottom = false
            )
        }
    }
}

@Composable
private fun MetadataFieldRow(
    term: String,
    detail: String,
    isMonoSpace: Boolean = false,
    hasBorderBottom: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (hasBorderBottom) 12.dp else 0.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = term.uppercase(),
            fontSize = 12.sp,
            fontFamily = PoppinsFont,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = Color(0xFF40493D)
        )
        Text(
            text = detail,
            fontSize = 18.sp,
            fontFamily = if (isMonoSpace) FontFamily.Monospace else PoppinsFont,
            fontWeight = FontWeight.Normal,
            lineHeight = 28.sp,
            color = Color(0xFF071E27)
        )
        if (hasBorderBottom) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp),
                color = Color(0xFFCFE6F2).copy(alpha = 0.5f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bento Component C: Lokasi Gudang Berkas Fisik Mapping
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PhysicalLocationCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFCFE6F2), RoundedCornerShape(32.dp))
            .padding(25.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = Color(0xFF0D631B),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Lokasi Berkas Fisik",
                fontSize = 22.sp,
                fontFamily = PoppinsFont,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF071E27)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LocationBadgeItem(title = "Gudang", code = "G1", modifier = Modifier.weight(1f))
            LocationBadgeItem(title = "RAK", code = "12", modifier = Modifier.weight(1f))
            LocationBadgeItem(
                title = "BOX",
                code = "5",
                isActiveColor = true,
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFFE6F6FF))
                .border(1.dp, Color(0xFFCFE6F2), RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.alpha(0.8f)
            ) {
                Box(modifier = Modifier.size(width = 32.dp, height = 96.dp).background(Color(0xFFCFE6F2)).border(1.dp, Color(0xFFBFCABA)))
                Box(modifier = Modifier.size(width = 32.dp, height = 96.dp).background(Color(0xFFCFE6F2)).border(1.dp, Color(0xFFBFCABA)))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0D631B), RoundedCornerShape(999.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Rak 12",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .size(width = 48.dp, height = 112.dp)
                            .background(Color(0xFF2E7D32), RoundedCornerShape(2.dp))
                            .border(2.dp, Color(0xFF0D631B), RoundedCornerShape(2.dp))
                            .padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(Color(0xFFCBFFC2).copy(alpha = 0.2f), RoundedCornerShape(2.dp)))
                        Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(Color(0xFFCBFFC2).copy(alpha = 0.2f), RoundedCornerShape(2.dp)))
                        Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(Color(0xFFCBFFC2), RoundedCornerShape(2.dp)))
                    }
                }

                Box(modifier = Modifier.size(width = 32.dp, height = 96.dp).background(Color(0xFFCFE6F2)).border(1.dp, Color(0xFFBFCABA)))
            }
        }
    }
}

@Composable
private fun LocationBadgeItem(
    title: String,
    code: String,
    isActiveColor: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(if (isActiveColor) Color(0xFF2E7D32) else Color(0xFFE6F6FF))
            .border(
                width = 1.dp,
                color = if (isActiveColor) Color(0xFF0D631B) else Color(0xFFBFCABA),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontFamily = PoppinsFont,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.48.sp,
            color = if (isActiveColor) Color(0xFFCBFFC2) else Color(0xFF40493D)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = code,
            fontSize = 24.sp,
            fontFamily = PoppinsFont,
            fontWeight = FontWeight.SemiBold,
            color = if (isActiveColor) Color(0xFFCBFFC2) else Color(0xFF0D631B)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DocumentDetailScreenPreview() {
    DocumentDetailScreen()
}