package com.bpkpad.arsipnonkeu.ui.screen.add

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.ui.component.ArchiveClassificationField
import com.bpkpad.arsipnonkeu.ui.component.ArchiveClassificationSelectorSheet
import com.bpkpad.arsipnonkeu.ui.component.TopBar
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingViewModel
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray

private val PoppinsFont = FontFamily.Default

@Composable
fun NewRecordScreen(
    onBackClick: () -> Unit = {},
    onSave: () -> Unit = {},
    viewModel: StagingViewModel
) {
    var documentType by remember { mutableStateOf(DocumentType.SURAT) }
    var documentNumber by remember { mutableStateOf("") }
    var classificationCode by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var physicalForm by remember { mutableStateOf(PhysicalForm.SHEET) }
    var condition by remember { mutableStateOf<DocumentCondition?>(DocumentCondition.GOOD) }
    var copyCount by remember { mutableStateOf("1") }
    var status by remember { mutableStateOf(DocumentStatus.AVAILABLE) }
    var originInstance by remember { mutableStateOf("") }

    var showSaveConfirmDialog by remember { mutableStateOf(false) }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }

    var showClassificationSheet by rememberSaveable { mutableStateOf(false) }
    var classificationKeyword by rememberSaveable { mutableStateOf("") }

    val isFormValid by remember(
        title,
        year,
        copyCount
    ) {
        derivedStateOf {
            title.isNotBlank() &&
                    year.length == 4 &&
                    year.toIntOrNull() != null &&
                    copyCount.toIntOrNull() != null &&
                    (copyCount.toIntOrNull() ?: 0) > 0
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Input Manual",
                onProfileClick = {}
            )
        },
        bottomBar = {
            ManualInputBottomBar(
                isFormValid = isFormValid,
                onCancelClick = {
                    showCancelConfirmDialog = true
                },
                onSaveClick = {
                    showSaveConfirmDialog = true
                }
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 32.dp,
                end = 32.dp,
                top = 24.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FormSection(
                    title = "Identitas Dokumen",
                    description = "Data utama yang dipakai untuk mengenali dokumen arsip."
                ) {
                    EnumChipSelector(
                        title = "Jenis Dokumen",
                        selected = documentType,
                        values = DocumentType.values().toList(),
                        label = { it.label },
                        onSelected = { selected ->
                            documentType = selected ?: documentType
                        }
                    )

                    DetailTextField(
                        label = "Nomor Dokumen",
                        value = documentNumber,
                        onValueChange = { documentNumber = it },
                        placeholder = "Contoh: 001/UMUM/2025"
                    )

                    ArchiveClassificationField(
                        selectedCode = classificationCode,
                        selectedLabel = viewModel.getLoadedArchiveClassificationLabel(classificationCode),
                        isRequired = false,
                        onClick = {
                            showClassificationSheet = true
                        }
                    )

                    DetailTextField(
                        label = "Judul Dokumen",
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "Masukkan judul dokumen"
                    )

                    DetailTextField(
                        label = "Deskripsi",
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Masukkan deskripsi dokumen",
                        singleLine = false
                    )
                }
            }

            item {
                FormSection(
                    title = "Klasifikasi dan Kondisi",
                    description = "Informasi fisik dan status dokumen sebelum masuk ke arsip utama."
                ) {
                    DetailTextField(
                        label = "Tahun",
                        value = year,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                year = input
                            }
                        },
                        placeholder = "2025"
                    )

                    EnumChipSelector(
                        title = "Bentuk Fisik",
                        selected = physicalForm,
                        values = PhysicalForm.values().toList(),
                        label = { it.label },
                        onSelected = { selected ->
                            physicalForm = selected ?: physicalForm
                        }
                    )

                    EnumChipSelector(
                        title = "Kondisi",
                        selected = condition,
                        values = DocumentCondition.values().toList(),
                        label = { it.label },
                        allowNull = true,
                        nullLabel = "Tidak diketahui",
                        onSelected = { selected ->
                            condition = selected
                        }
                    )

                    DetailTextField(
                        label = "Jumlah Salinan",
                        value = copyCount,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                copyCount = input
                            }
                        },
                        placeholder = "1"
                    )

                    EnumChipSelector(
                        title = "Status",
                        selected = status,
                        values = DocumentStatus.values().toList(),
                        label = { it.label },
                        onSelected = { selected ->
                            status = selected ?: status
                        }
                    )
                }
            }

            item {
                FormSection(
                    title = "Asal Dokumen",
                    description = "Informasi sumber atau instansi asal dokumen."
                ) {
                    DetailTextField(
                        label = "Asal Instansi",
                        value = originInstance,
                        onValueChange = { originInstance = it },
                        placeholder = "Contoh: Bagian Umum"
                    )
                }
            }

            item {
                ValidationInfoCard(
                    isFormValid = isFormValid,
                    title = title,
                    year = year,
                    copyCount = copyCount
                )
            }
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    ArchiveClassificationSelectorSheet(
        visible = showClassificationSheet,
        classifications = uiState.archiveClassifications,
        selectedCode = classificationCode,
        keyword = classificationKeyword,
        isLoading = uiState.isClassificationLoading,
        onKeywordChange = { keyword ->
            classificationKeyword = keyword
            viewModel.loadArchiveClassifications(keyword)
        },
        onSelect = { classification ->
            classificationCode = classification.code
            classificationKeyword = ""
            showClassificationSheet = false
        },
        onDismiss = {
            showClassificationSheet = false
        }
    )

    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showSaveConfirmDialog = false
            },
            title = {
                Text("Simpan ke Staging")
            },
            text = {
                Text("Simpan dokumen ini ke daftar staging? Data belum masuk ke arsip utama.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addManualDocument(
                            documentType = documentType,
                            documentNumber = documentNumber.takeIf { it.isNotBlank() },
                            classificationCode = classificationCode.takeIf { it.isNotBlank() },
                            title = title,
                            description = description.takeIf { it.isNotBlank() },
                            year = year.toIntOrNull() ?: 0,
                            physicalForm = physicalForm,
                            condition = condition,
                            copyCount = copyCount.toIntOrNull() ?: 1,
                            status = status,
                            isCopy = false,
                            originInstance = originInstance.takeIf { it.isNotBlank() }
                        )

                        showSaveConfirmDialog = false
                        onSave()
                    }
                ) {
                    Text(
                        text = "Simpan",
                        color = Color(0xFF0D631B)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSaveConfirmDialog = false
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }

    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showCancelConfirmDialog = false
            },
            title = {
                Text("Batalkan Input")
            },
            text = {
                Text("Batalkan input manual? Perubahan yang belum disimpan akan hilang.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelConfirmDialog = false
                        onBackClick()
                    }
                ) {
                    Text(
                        text = "Batalkan",
                        color = Color(0xFFBA1A1A)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCancelConfirmDialog = false
                    }
                ) {
                    Text("Lanjut Input")
                }
            }
        )
    }
}

@Composable
private fun FormSection(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0x33BFCABA), RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFont,
                color = Color(0xFF071E27)
            )

            Text(
                text = description,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = PoppinsFont,
                color = Color(0xFF40493D)
            )
        }

        HorizontalDivider(color = Color(0x4DBFCABA))

        content()
    }
}

@Composable
private fun ManualInputBottomBar(
    isFormValid: Boolean,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color(0x33BFCABA))
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!isFormValid) {
            Text(
                text = "Judul, tahun, dan jumlah salinan wajib valid.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PoppinsFont,
                color = Color(0xFFBA1A1A)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(9999.dp),
                border = BorderStroke(1.dp, Color(0xFF0D631B))
            ) {
                Text(
                    text = "Batal",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF0D631B)
                )
            }

            Button(
                onClick = onSaveClick,
                enabled = isFormValid,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)),
                shape = RoundedCornerShape(9999.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                Text(
                    text = "Simpan",
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
private fun ValidationInfoCard(
    isFormValid: Boolean,
    title: String,
    year: String,
    copyCount: String
) {
    val message = when {
        title.isBlank() -> "Judul dokumen belum diisi."
        year.length != 4 || year.toIntOrNull() == null -> "Tahun harus berupa 4 digit angka."
        copyCount.toIntOrNull() == null || (copyCount.toIntOrNull() ?: 0) <= 0 -> {
            "Jumlah salinan harus berupa angka lebih dari 0."
        }
        else -> "Form sudah valid dan siap disimpan ke staging."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isFormValid) Color(0xFFE8F5E9) else Color(0xFFFFF3CD))
            .padding(16.dp)
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PoppinsFont,
            color = if (isFormValid) Color(0xFF1B5E20) else Color(0xFF92400E)
        )
    }
}

@Composable
private fun DetailTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        placeholder = {
            Text(placeholder)
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF0D631B),
            unfocusedBorderColor = Color(0xFFBFCABA)
        )
    )
}

@Composable
private fun <T> EnumChipSelector(
    title: String,
    selected: T?,
    values: List<T>,
    label: (T) -> String,
    allowNull: Boolean = false,
    nullLabel: String = "Tidak diketahui",
    onSelected: (T?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PoppinsFont,
            color = Color(0xFF707A6C),
            letterSpacing = 0.48.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (allowNull) {
                EnumChip(
                    label = nullLabel,
                    isSelected = selected == null,
                    onClick = { onSelected(null) }
                )
            }

            values.forEach { value ->
                EnumChip(
                    label = label(value),
                    isSelected = selected == value,
                    onClick = { onSelected(value) }
                )
            }
        }
    }
}

@Composable
private fun EnumChip(
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
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PoppinsFont,
            color = if (isSelected) Color(0xFFCBFFC2) else Color(0xFF40493D),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

//@Preview(
//    showBackground = true,
//    showSystemUi = true,
//    device = "spec:width=390dp,height=844dp,dpi=420"
//)
//@Composable
//fun NewRecordScreenPreview() {
//    NewRecordScreen()
//}