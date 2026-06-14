package com.bpkpad.arsipnonkeu.ui.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentListItem
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentPlacement
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.domain.model.StorageLocation
import com.bpkpad.arsipnonkeu.ui.component.ArchiveClassificationField
import com.bpkpad.arsipnonkeu.ui.component.ArchiveClassificationSelectorSheet
import com.bpkpad.arsipnonkeu.ui.component.TopBar
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray

private val PoppinsFont = FontFamily.Default

@Composable
fun DocumentDetailScreen(
    documentId: String,
    onBackClick: () -> Unit = {},
    viewModel: DocumentDetailViewModel = remember { DocumentDetailViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }
    var showEditConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var editedDocumentType by remember { mutableStateOf(DocumentType.values().first()) }
    var editedDocumentNumber by remember { mutableStateOf("") }
    var editedDocumentCode by remember { mutableStateOf("") }
    var editedTitle by remember { mutableStateOf("") }
    var editedDescription by remember { mutableStateOf("") }
    var editedYear by remember { mutableStateOf("") }
    var editedPhysicalForm by remember { mutableStateOf(PhysicalForm.values().first()) }
    var editedCondition by remember { mutableStateOf<DocumentCondition?>(null) }
    var editedIsCopy by remember { mutableStateOf<Boolean?>(null) }
    var editedCopyCount by remember { mutableStateOf("") }
    var editedStatus by remember { mutableStateOf(DocumentStatus.values().first()) }
    var editedOriginInstance by remember { mutableStateOf("") }

    var showClassificationSheet by remember { mutableStateOf(false) }
    var classificationKeyword by remember { mutableStateOf("") }

    fun syncEditedState(document: ArchiveDocument) {
        editedDocumentType = document.documentType
        editedDocumentNumber = document.documentNumber.orEmpty()
        editedDocumentCode = document.classificationCode.orEmpty()
        editedTitle = document.title
        editedDescription = document.description.orEmpty()
        editedYear = document.year.toString()
        editedPhysicalForm = document.physicalForm
        editedCondition = document.condition
        editedIsCopy = document.isCopy
        editedCopyCount = document.copyCount.toString()
        editedStatus = document.status
        editedOriginInstance = document.originInstance.orEmpty()
    }

    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }

    LaunchedEffect(uiState.item) {
        val document = uiState.item?.document ?: return@LaunchedEffect
        syncEditedState(document)
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Detail Arsip",
                onProfileClick = {}
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null && uiState.item == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = Color(0xFFBA1A1A),
                        fontSize = 14.sp,
                        fontFamily = PoppinsFont,
                        textAlign = TextAlign.Center
                    )
                }
            }

            uiState.item != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        start = 32.dp,
                        end = 32.dp,
                        top = 24.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DocumentInformationCard(
                            item = uiState.item!!,
                            isEditMode = isEditMode,

                            editedDocumentType = editedDocumentType,
                            onDocumentTypeChange = { editedDocumentType = it },

                            editedDocumentNumber = editedDocumentNumber,
                            onDocumentNumberChange = { editedDocumentNumber = it },

                            editedDocumentCode = editedDocumentCode,
                            viewModel = viewModel,
                            onClassificationClick = { showClassificationSheet = true },

                            editedTitle = editedTitle,
                            onTitleChange = { editedTitle = it },

                            editedDescription = editedDescription,
                            onDescriptionChange = { editedDescription = it },

                            editedYear = editedYear,
                            onYearChange = { editedYear = it },

                            editedPhysicalForm = editedPhysicalForm,
                            onPhysicalFormChange = { editedPhysicalForm = it },

                            editedCondition = editedCondition,
                            onConditionChange = { editedCondition = it },

                            editedIsCopy = editedIsCopy,
                            onIsCopyChange = { editedIsCopy = it },

                            editedCopyCount = editedCopyCount,
                            onCopyCountChange = { editedCopyCount = it },

                            editedStatus = editedStatus,
                            onStatusChange = { editedStatus = it },

                            editedOriginInstance = editedOriginInstance,
                            onOriginInstanceChange = { editedOriginInstance = it }
                        )
                    }

                    item {
                        DocumentPlacementCard(item = uiState.item!!)
                    }

                    item {
                        DocumentSystemCard(item = uiState.item!!)
                    }

                    uiState.errorMessage?.let { message ->
                        item {
                            MessageCard(
                                message = message,
                                isError = true
                            )
                        }
                    }

                    uiState.successMessage?.let { message ->
                        item {
                            MessageCard(
                                message = message,
                                isError = false
                            )
                        }
                    }

                    item {
                        DetailActionButtons(
                            isEditMode = isEditMode,
                            onEditClick = {
                                isEditMode = true
                                viewModel.clearMessage()
                            },
                            onCancelEditClick = {
                                isEditMode = false
                                uiState.item?.document?.let { document ->
                                    syncEditedState(document)
                                }
                            },
                            onSaveClick = {
                                showEditConfirmDialog = true
                            },
                            onDeleteClick = {
                                showDeleteConfirmDialog = true
                            },
                            onBackClick = onBackClick
                        )
                    }
                }
            }
        }
    }

    if (showEditConfirmDialog) {
        ConfirmDialog(
            title = "Konfirmasi Perubahan",
            message = "Simpan perubahan pada data dokumen arsip ini?",
            confirmText = "Simpan",
            dismissText = "Batal",
            onConfirm = {
                val currentDocument = uiState.item?.document

                if (currentDocument != null) {
                    viewModel.updateDocument(
                        currentDocument.copy(
                            documentType = editedDocumentType,
                            documentNumber = editedDocumentNumber.trim().takeIf { it.isNotBlank() },
                            classificationCode = editedDocumentCode.trim().takeIf { it.isNotBlank() },
                            title = editedTitle.trim().ifBlank { currentDocument.title },
                            description = editedDescription.trim().takeIf { it.isNotBlank() },
                            year = editedYear.toIntOrNull() ?: currentDocument.year,
                            physicalForm = editedPhysicalForm,
                            condition = editedCondition,
                            isCopy = editedIsCopy,
                            copyCount = editedCopyCount.toIntOrNull() ?: currentDocument.copyCount,
                            status = editedStatus,
                            originInstance = editedOriginInstance.trim().takeIf { it.isNotBlank() },
                            updatedAt = "UPDATED"
                        )
                    )
                }

                isEditMode = false
                showEditConfirmDialog = false
            },
            onDismiss = {
                showEditConfirmDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        ConfirmDialog(
            title = "Konfirmasi Hapus",
            message = "Hapus dokumen arsip ini? Data akan dihapus secara soft delete.",
            confirmText = "Hapus",
            dismissText = "Batal",
            isDanger = true,
            onConfirm = {
                viewModel.deleteDocument()
                showDeleteConfirmDialog = false
            },
            onDismiss = {
                showDeleteConfirmDialog = false
            }
        )
    }

    ArchiveClassificationSelectorSheet(
        visible = showClassificationSheet,
        classifications = uiState.archiveClassifications,
        selectedCode = editedDocumentCode,
        keyword = classificationKeyword,
        isLoading = uiState.isClassificationLoading,
        onKeywordChange = { keyword ->
            classificationKeyword = keyword
            viewModel.loadArchiveClassifications(keyword)
        },
        onSelect = { classification ->
            editedDocumentCode = classification.code
            classificationKeyword = ""
            showClassificationSheet = false
        },
        onDismiss = {
            showClassificationSheet = false
        }
    )
}

@Composable
private fun DocumentInformationCard(
    item: ArchiveDocumentListItem,
    isEditMode: Boolean,

    editedDocumentType: DocumentType,
    onDocumentTypeChange: (DocumentType) -> Unit,

    editedDocumentNumber: String,
    onDocumentNumberChange: (String) -> Unit,

    editedDocumentCode: String,
    viewModel: DocumentDetailViewModel,
    onClassificationClick: () -> Unit,

    editedTitle: String,
    onTitleChange: (String) -> Unit,

    editedDescription: String,
    onDescriptionChange: (String) -> Unit,

    editedYear: String,
    onYearChange: (String) -> Unit,

    editedPhysicalForm: PhysicalForm,
    onPhysicalFormChange: (PhysicalForm) -> Unit,

    editedCondition: DocumentCondition?,
    onConditionChange: (DocumentCondition?) -> Unit,

    editedIsCopy: Boolean?,
    onIsCopyChange: (Boolean?) -> Unit,

    editedCopyCount: String,
    onCopyCountChange: (String) -> Unit,

    editedStatus: DocumentStatus,
    onStatusChange: (DocumentStatus) -> Unit,

    editedOriginInstance: String,
    onOriginInstanceChange: (String) -> Unit
) {
    val document = item.document

    DetailCard(title = "Informasi Dokumen") {
        if (isEditMode) {
            DetailDropdownField(
                label = "Jenis Dokumen",
                value = editedDocumentType,
                options = DocumentType.values().toList(),
                optionLabel = { it.label },
                onValueChange = onDocumentTypeChange
            )

            DetailTextField(
                label = "Nomor Dokumen",
                value = editedDocumentNumber,
                onValueChange = onDocumentNumberChange
            )

            ArchiveClassificationField(
                selectedCode = editedDocumentCode,
                selectedLabel = viewModel.getLoadedArchiveClassificationLabel(editedDocumentCode),
                onClick = onClassificationClick
            )

            DetailTextField(
                label = "Judul Dokumen",
                value = editedTitle,
                onValueChange = onTitleChange
            )

            DetailTextField(
                label = "Deskripsi",
                value = editedDescription,
                onValueChange = onDescriptionChange,
                minLines = 3
            )

            DetailTextField(
                label = "Tahun",
                value = editedYear,
                onValueChange = onYearChange,
                keyboardType = KeyboardType.Number
            )

            DetailDropdownField(
                label = "Bentuk Fisik",
                value = editedPhysicalForm,
                options = PhysicalForm.values().toList(),
                optionLabel = { it.label },
                onValueChange = onPhysicalFormChange
            )

            DetailDropdownField(
                label = "Kondisi",
                value = editedCondition,
                options = listOf<DocumentCondition?>(null) + DocumentCondition.values().toList(),
                optionLabel = { condition ->
                    condition?.label ?: "Tidak diketahui"
                },
                onValueChange = onConditionChange
            )

            DetailDropdownField(
                label = "Status Keaslian",
                value = editedIsCopy,
                options = listOf<Boolean?>(null, false, true),
                optionLabel = { isCopy ->
                    when (isCopy) {
                        true -> "Kopi"
                        false -> "Asli"
                        null -> "Tidak diketahui"
                    }
                },
                onValueChange = onIsCopyChange
            )

            DetailTextField(
                label = "Jumlah Salinan",
                value = editedCopyCount,
                onValueChange = onCopyCountChange,
                keyboardType = KeyboardType.Number
            )

            DetailDropdownField(
                label = "Status",
                value = editedStatus,
                options = DocumentStatus.values().toList(),
                optionLabel = { it.label },
                onValueChange = onStatusChange
            )

            DetailTextField(
                label = "Asal Instansi",
                value = editedOriginInstance,
                onValueChange = onOriginInstanceChange
            )
        } else {
            DetailRow("Jenis Dokumen", document.documentType.label)
            DetailRow("Nomor Dokumen", document.documentNumber ?: "-")
            DetailRow(
                label = "Kode Klasifikasi",
                value = viewModel.getLoadedArchiveClassificationLabel(document.classificationCode)
                    .ifBlank { document.classificationCode ?: "-" }
            )
            DetailRow("Judul", document.title)
            DetailRow("Deskripsi", document.description ?: "-")
            DetailRow("Tahun", document.year.toString())
            DetailRow("Bentuk Fisik", document.physicalForm.label)
            DetailRow("Kondisi", document.condition?.label ?: "Tidak diketahui")
            DetailRow(
                label = "Status Keaslian",
                value = when (document.isCopy) {
                    true -> "Kopi"
                    false -> "Asli"
                    null -> "Tidak diketahui"
                }
            )
            DetailRow("Jumlah Salinan", document.copyCount.toString())
            DetailRow("Status", document.status.label)
            DetailRow("Asal Instansi", document.originInstance ?: "-")
        }
    }
}

@Composable
private fun DocumentPlacementCard(
    item: ArchiveDocumentListItem
) {
    val placement = item.currentPlacement
    val location = item.storageLocation

    DetailCard(title = "Penempatan Arsip") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LocationBadgeItem(
                title = "Ruang",
                code = location?.room ?: "-",
                modifier = Modifier.weight(1f)
            )

            LocationBadgeItem(
                title = "Rak",
                code = location?.shelf ?: "-",
                modifier = Modifier.weight(1f)
            )

            LocationBadgeItem(
                title = "Box",
                code = location?.boxNumber ?: "-",
                modifier = Modifier.weight(1f),
                isActiveColor = true
            )
        }

        DetailRow("Tanggal Penempatan", placement?.placedAt ?: "-")
        DetailRow("Tanggal Dipindah/Dikeluarkan", placement?.removedAt ?: "-")
    }
}

@Composable
private fun DocumentSystemCard(
    item: ArchiveDocumentListItem
) {
    val document = item.document

    DetailCard(title = "Informasi Sistem") {
        DetailRow("Dibuat oleh", document.createdBy ?: "-")
        DetailRow("Diubah oleh", document.updatedBy ?: "-")
        DetailRow("Dibuat pada", document.createdAt ?: "-")
        DetailRow("Diubah pada", document.updatedAt ?: "-")
        DetailRow("Dihapus pada", document.deletedAt ?: "-")
    }
}

@Composable
private fun DetailActionButtons(
    isEditMode: Boolean,
    onEditClick: () -> Unit,
    onCancelEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isEditMode) {
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D631B)
                ),
                shape = RoundedCornerShape(9999.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = Color.White
                )

                Spacer(modifier = Modifier.padding(4.dp))

                Text(
                    text = "Simpan Perubahan",
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = onCancelEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(9999.dp)
            ) {
                Text(
                    text = "Batal Edit",
                    color = Color(0xFFBA1A1A)
                )
            }
        } else {
            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D631B)
                ),
                shape = RoundedCornerShape(9999.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.White
                )

                Spacer(modifier = Modifier.padding(4.dp))

                Text(
                    text = "Edit Dokumen",
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(9999.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFBA1A1A)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                Text(
                    text = "Hapus Dokumen",
                    color = Color(0xFFBA1A1A)
                )
            }

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(9999.dp)
            ) {
                Text(
                    text = "Kembali",
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0x33BFCABA),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFont,
                color = Color(0xFF071E27)
            )

            HorizontalDivider(color = Color(0x4DBFCABA))
        }

        content()
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PoppinsFont,
            color = Color(0xFF707A6C),
            letterSpacing = 0.48.sp
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PoppinsFont,
            color = Color(0xFF071E27),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DetailTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = Color.Black
            )
        },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        textStyle = LocalTextStyle.current.copy(
            color = Color.Black,
            fontFamily = PoppinsFont,
            fontSize = 14.sp
        ),
        colors = detailTextFieldColors()
    )
}

@Composable
private fun DetailReadOnlyField(
    label: String,
    value: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = {
            Text(
                text = label,
                color = Color.Black
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        textStyle = LocalTextStyle.current.copy(
            color = Color.Black,
            fontFamily = PoppinsFont,
            fontSize = 14.sp
        ),
        colors = detailTextFieldColors()
    )
}

@Composable
private fun DetailClickableField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            enabled = false,
            label = {
                Text(
                    text = label,
                    color = Color.Black
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            textStyle = LocalTextStyle.current.copy(
                color = Color.Black,
                fontFamily = PoppinsFont,
                fontSize = 14.sp
            ),
            colors = detailTextFieldColors()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DetailDropdownField(
    label: String,
    value: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onValueChange: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = optionLabel(value),
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    text = label,
                    color = Color.Black
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            textStyle = LocalTextStyle.current.copy(
                color = Color.Black,
                fontFamily = PoppinsFont,
                fontSize = 14.sp
            ),
            colors = detailTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            containerColor = Color.White,
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = optionLabel(option),
                            color = Color.Black,
                            fontFamily = PoppinsFont
                        )
                    },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun detailTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    disabledTextColor = Color.Black,

    focusedLabelColor = Color.Black,
    unfocusedLabelColor = Color.Black,
    disabledLabelColor = Color.Black,

    focusedPlaceholderColor = Color.Black,
    unfocusedPlaceholderColor = Color.Black,
    disabledPlaceholderColor = Color.Black,

    focusedBorderColor = Color.Black,
    unfocusedBorderColor = Color.Black,
    disabledBorderColor = Color.Black,

    cursorColor = Color.Black,

    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent
)

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
            .background(
                if (isActiveColor) {
                    Color(0xFF2E7D32)
                } else {
                    Color(0xFFE6F6FF)
                }
            )
            .border(
                width = 1.dp,
                color = if (isActiveColor) {
                    Color(0xFF0D631B)
                } else {
                    Color(0xFFBFCABA)
                },
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
            color = if (isActiveColor) {
                Color(0xFFCBFFC2)
            } else {
                Color(0xFF40493D)
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = code,
            fontSize = 24.sp,
            fontFamily = PoppinsFont,
            fontWeight = FontWeight.SemiBold,
            color = if (isActiveColor) {
                Color(0xFFCBFFC2)
            } else {
                Color(0xFF0D631B)
            }
        )
    }
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isError) {
                    Color(0xFFFEE2E2)
                } else {
                    Color(0xFFE8F5E9)
                }
            )
            .padding(16.dp)
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PoppinsFont,
            color = if (isError) {
                Color(0xFF991B1B)
            } else {
                Color(0xFF1B5E20)
            }
        )
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    isDanger: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = if (isDanger) {
                        Color(0xFFBA1A1A)
                    } else {
                        Color(0xFF0D631B)
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        }
    )
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=390dp,height=844dp,dpi=420"
)
@Composable
fun DocumentDetailContentPreview() {
    val viewModel = remember { DocumentDetailViewModel() }

    val document = ArchiveDocument(
        id = "doc-003",
        documentType = DocumentType.KEPBUP,
        documentNumber = "188.45/25/KUM/2024",
        classificationCode = "ARS-2024-001",
        title = "Keputusan Bupati Tentang Pembentukan Tim Kerja",
        description = "Dokumen keputusan bupati tentang pembentukan tim kerja daerah.",
        year = 2024,
        physicalForm = PhysicalForm.BOOK,
        condition = DocumentCondition.GOOD,
        copyCount = 1,
        isCopy = false,
        status = DocumentStatus.BORROWED,
        originInstance = "Bagian Hukum",
        createdBy = "user-002",
        updatedBy = null,
        createdAt = "2024-03-20",
        updatedAt = null,
        deletedAt = null
    )

    val location = StorageLocation(
        id = "loc-003",
        room = "Ruang Arsip",
        shelf = "Rak 02-C",
        boxNumber = "03"
    )

    val placement = DocumentPlacement(
        id = "place-003",
        archiveDocumentId = "doc-003",
        storageLocationId = "loc-003",
        placedAt = "2024-03-20",
        removedAt = null,
        userId = null
    )

    val item = ArchiveDocumentListItem(
        document = document,
        currentPlacement = placement,
        storageLocation = location
    )

    Scaffold(
        topBar = {
            TopBar(
                title = "Detail Arsip",
                onProfileClick = {}
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
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DocumentInformationCard(
                    item = item,
                    isEditMode = false,

                    editedDocumentType = document.documentType,
                    onDocumentTypeChange = {},

                    editedDocumentNumber = document.documentNumber.orEmpty(),
                    onDocumentNumberChange = {},

                    editedDocumentCode = document.classificationCode.orEmpty(),
                    viewModel = viewModel,
                    onClassificationClick = {},

                    editedTitle = document.title,
                    onTitleChange = {},

                    editedDescription = document.description.orEmpty(),
                    onDescriptionChange = {},

                    editedYear = document.year.toString(),
                    onYearChange = {},

                    editedPhysicalForm = document.physicalForm,
                    onPhysicalFormChange = {},

                    editedCondition = document.condition,
                    onConditionChange = {},

                    editedIsCopy = document.isCopy,
                    onIsCopyChange = {},

                    editedCopyCount = document.copyCount.toString(),
                    onCopyCountChange = {},

                    editedStatus = document.status,
                    onStatusChange = {},

                    editedOriginInstance = document.originInstance.orEmpty(),
                    onOriginInstanceChange = {}
                )
            }

            item {
                DocumentPlacementCard(item = item)
            }

            item {
                DocumentSystemCard(item = item)
            }

            item {
                DetailActionButtons(
                    isEditMode = false,
                    onEditClick = {},
                    onCancelEditClick = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onBackClick = {}
                )
            }
        }
    }
}