package com.bpkpad.arsipnonkeu.ui.screen.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpkpad.arsipnonkeu.ui.component.TopBar
import com.bpkpad.arsipnonkeu.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// NewRecordScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * NewRecordScreen - Layar untuk menambahkan arsip dokumen baru.
 *
 * Berisi dua seksi utama:
 * 1. **Detail Dokumen** — Tipe, Nama, Nomor Dokumen, Dinas, Tahun, Masa Berlaku, Subject
 * 2. **Lokasi Gudang** — Dropdown Gudang, field Rak No & Box No
 * 3. **Upload Scan** — Area upload file digital (PDF, JPG, PNG)
 *
 * Aksi bawah layar: tombol **Clear** dan **Save Archive**.
 *
 * @param onNavItemSelected Callback saat tab bottom nav ditekan.
 * @param onClear Callback saat tombol Clear ditekan.
 * @param onSave Callback saat tombol Save Archive ditekan.
 */
@Composable
fun NewRecordScreen(
    onBackClick: () -> Unit = {},
    onClear: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    // Form state
    var tipeDokumen by remember { mutableStateOf("") }
    var namaDokumen by remember { mutableStateOf("") }
    var nomorDokumen by remember { mutableStateOf("") }
    var dinas by remember { mutableStateOf("") }
    var tahun by remember { mutableStateOf("") }
    var masaBerlaku by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var gudang by remember { mutableStateOf("") }
    var rakNo by remember { mutableStateOf("") }
    var boxNo by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopBar(
                title = "Arsip Baru",
                onProfileClick = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Page Title ────────────────────────────────────────────
            PageHeader()

            // ── Import / Scan buttons ─────────────────────────────────
            ImportScanRow()

            // ── Detail Dokumen Card ───────────────────────────────────
            FormCard(title = "Detail Dokumen") {
                // Tipe Dokumen
                FormDropdown(
                    label = "Tipe Dokumen",
                    placeholder = "Pilih tipe....",
                    value = tipeDokumen,
                    onValueChange = { tipeDokumen = it }
                )

                // Nama Dokumen
                FormTextField(
                    label = "Nama Dokumen",
                    placeholder = "e.x. Surat Non-keuangan",
                    value = namaDokumen,
                    onValueChange = { namaDokumen = it }
                )

                // Nomor Dokumen
                FormTextField(
                    label = "Nomor Dokumen",
                    placeholder = "e.x. 123/BPKPAD/2023",
                    value = nomorDokumen,
                    onValueChange = { nomorDokumen = it }
                )

                // Dinas
                FormDropdown(
                    label = "Dinas",
                    placeholder = "Pilih Dinas...",
                    value = dinas,
                    onValueChange = { dinas = it }
                )

                // Tahun
                FormTextField(
                    label = "Tahun",
                    placeholder = "DD/MM/YYYY",
                    value = tahun,
                    onValueChange = { tahun = it }
                )

                // Masa Berlaku
                FormTextField(
                    label = "Masa Berlaku",
                    placeholder = "DD/MM/YYYY",
                    value = masaBerlaku,
                    onValueChange = { masaBerlaku = it }
                )

                // Subject
                FormTextArea(
                    label = "Subject",
                    placeholder = "Deskripsi tentang dokumen",
                    value = subject,
                    onValueChange = { subject = it }
                )
            }

            // ── Lokasi Gudang Card ────────────────────────────────────
            FormCard(title = "Lokasi Gudang") {
                // Gudang dropdown
                FormDropdown(
                    label = "Gudang",
                    placeholder = "Pilih Gudang...",
                    value = gudang,
                    onValueChange = { gudang = it }
                )

                // Rak No + Box No (side by side)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            label = "Rak No",
                            placeholder = "e.x. R-12",
                            value = rakNo,
                            onValueChange = { rakNo = it }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            label = "Box No",
                            placeholder = "e.x. B-05",
                            value = boxNo,
                            onValueChange = { boxNo = it }
                        )
                    }
                }
            }

            // ── Upload Scan Card ──────────────────────────────────────
            UploadScanCard()

            // ── Action Buttons ────────────────────────────────────────
            ActionButtonsRow(onClear = onClear, onSave = onSave)

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Page Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PageHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Menambahkan Arsip Baru",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(
            text = "Tambahkan detail dokumen baru",
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Import / Scan Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ImportScanRow() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Import dari Spreadsheet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, PrimaryGreen, RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable {}
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "📥", fontSize = 16.sp)
            Text(
                text = "Import dari Spreadsheet",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryGreen
            )
        }

        // Scan Langsung
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, Color(0xFFD1D5DB), RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable {}
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "📷", fontSize = 16.sp)
            Text(
                text = "Scan Langsung",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Generic Form Card wrapper
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FormCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Form Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FormTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            letterSpacing = 0.2.sp
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = TextPrimary
            ),
            cursorBrush = SolidColor(PrimaryGreen),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                .background(Color(0xFFFAFAFA))
                .padding(horizontal = 14.dp, vertical = 13.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 14.sp,
                            color = Color(0xFFB0B8C1)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun FormDropdown(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            letterSpacing = 0.2.sp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                .background(Color(0xFFFAFAFA))
                .clickable {}
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (value.isEmpty()) placeholder else value,
                fontSize = 14.sp,
                color = if (value.isEmpty()) Color(0xFFB0B8C1) else TextPrimary
            )
            Text(text = "▾", fontSize = 14.sp, color = Color(0xFF9CA3AF))
        }
    }
}

@Composable
private fun FormTextArea(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            letterSpacing = 0.2.sp
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = TextPrimary,
                lineHeight = 20.sp
            ),
            cursorBrush = SolidColor(PrimaryGreen),
            maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                .background(Color(0xFFFAFAFA))
                .padding(horizontal = 14.dp, vertical = 13.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 14.sp,
                            color = Color(0xFFB0B8C1)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Upload Scan Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UploadScanCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Upload icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFE0F2FE)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "☁️", fontSize = 26.sp)
        }

        Text(
            text = "Upload Scan",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            text = "Attach digital copy (PDF, JPG, PNG)",
            fontSize = 12.sp,
            color = TextSecondary
        )

        // Select File button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(1.5.dp, Color(0xFFD1D5DB), RoundedCornerShape(10.dp))
                .background(Color.White)
                .clickable {}
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "📁", fontSize = 14.sp)
                Text(
                    text = "Select File",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Action Buttons Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActionButtonsRow(onClear: () -> Unit, onSave: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Clear button
        Box(
            modifier = Modifier
                .weight(0.38f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, Color(0xFFD1D5DB), RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable(onClick = onClear)
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Clear",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        // Save Archive button
        Box(
            modifier = Modifier
                .weight(0.62f)
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryGreen)
                .clickable(onClick = onSave)
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Save Archive",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
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
fun NewRecordScreenPreview() {
    NewRecordScreen()
}