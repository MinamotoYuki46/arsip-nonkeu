package com.bpkpad.arsipnonkeu.ui.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentListItem
import com.bpkpad.arsipnonkeu.ui.component.TopBar
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray

//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.foundation.layout.PaddingValues
//import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocument
//import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentListItem
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentPlacement
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.domain.model.StorageLocation
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

    var editedTitle by remember { mutableStateOf("") }
    var editedDescription by remember { mutableStateOf("") }
    var editedDocumentNumber by remember { mutableStateOf("") }
    var editedDocumentCode by remember { mutableStateOf("") }
    var editedOriginInstance by remember { mutableStateOf("") }
    var editedCopyCount by remember { mutableStateOf("") }

    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }

    LaunchedEffect(uiState.item) {
        val document = uiState.item?.document ?: return@LaunchedEffect

        editedTitle = document.title
        editedDescription = document.description.orEmpty()
        editedDocumentNumber = document.documentNumber.orEmpty()
        editedDocumentCode = document.classificationCode.orEmpty()
        editedOriginInstance = document.originInstance.orEmpty()
        editedCopyCount = document.copyCount.toString()
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
                        DocumentHeaderCard(
                            item = uiState.item!!,
                            isEditMode = isEditMode,
                            editedTitle = editedTitle,
                            onTitleChange = { editedTitle = it },
                            editedDocumentNumber = editedDocumentNumber,
                            onDocumentNumberChange = { editedDocumentNumber = it }
                        )
                    }

                    item {
                        DocumentInformationCard(
                            item = uiState.item!!,
                            isEditMode = isEditMode,
                            editedDescription = editedDescription,
                            onDescriptionChange = { editedDescription = it },
                            editedDocumentCode = editedDocumentCode,
                            onDocumentCodeChange = { editedDocumentCode = it },
                            editedOriginInstance = editedOriginInstance,
                            onOriginInstanceChange = { editedOriginInstance = it },
                            editedCopyCount = editedCopyCount,
                            onCopyCountChange = { editedCopyCount = it }
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
                                val document = uiState.item?.document
                                if (document != null) {
                                    editedTitle = document.title
                                    editedDescription = document.description.orEmpty()
                                    editedDocumentNumber = document.documentNumber.orEmpty()
                                    editedDocumentCode = document.classificationCode.orEmpty()
                                    editedOriginInstance = document.originInstance.orEmpty()
                                    editedCopyCount = document.copyCount.toString()
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
                            title = editedTitle,
                            description = editedDescription.takeIf { it.isNotBlank() },
                            documentNumber = editedDocumentNumber.takeIf { it.isNotBlank() },
                            classificationCode = editedDocumentCode.takeIf { it.isNotBlank() },
                            originInstance = editedOriginInstance.takeIf { it.isNotBlank() },
                            copyCount = editedCopyCount.toIntOrNull() ?: currentDocument.copyCount,
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
}

@Composable
private fun DocumentHeaderCard(
    item: ArchiveDocumentListItem,
    isEditMode: Boolean,
    editedTitle: String,
    onTitleChange: (String) -> Unit,
    editedDocumentNumber: String,
    onDocumentNumberChange: (String) -> Unit
) {
    val document = item.document

    DetailCard {
        if (isEditMode) {
            DetailTextField(
                label = "Judul Dokumen",
                value = editedTitle,
                onValueChange = onTitleChange
            )

            DetailTextField(
                label = "Nomor Dokumen",
                value = editedDocumentNumber,
                onValueChange = onDocumentNumberChange
            )
        } else {
            Text(
                text = document.title,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFont,
                color = Color(0xFF071E27)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = document.documentNumber ?: document.classificationCode ?: "-",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PoppinsFont,
                color = Color(0xFF707A6C)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallBadge(text = document.documentType.label)
            SmallBadge(text = document.status.label)
            SmallBadge(text = document.year.toString())
        }
    }
}

@Composable
private fun DocumentInformationCard(
    item: ArchiveDocumentListItem,
    isEditMode: Boolean,
    editedDescription: String,
    onDescriptionChange: (String) -> Unit,
    editedDocumentCode: String,
    onDocumentCodeChange: (String) -> Unit,
    editedOriginInstance: String,
    onOriginInstanceChange: (String) -> Unit,
    editedCopyCount: String,
    onCopyCountChange: (String) -> Unit
) {
    val document = item.document

    DetailCard(title = "Informasi Dokumen") {
        if (isEditMode) {
            DetailTextField(
                label = "Deskripsi",
                value = editedDescription,
                onValueChange = onDescriptionChange,
                minLines = 3
            )

            DetailTextField(
                label = "Kode Dokumen",
                value = editedDocumentCode,
                onValueChange = onDocumentCodeChange
            )

            DetailTextField(
                label = "Asal Instansi",
                value = editedOriginInstance,
                onValueChange = onOriginInstanceChange
            )

            DetailTextField(
                label = "Jumlah Salinan",
                value = editedCopyCount,
                onValueChange = onCopyCountChange
            )
        } else {
            DetailRow("ID", document.id)
            DetailRow("Jenis Dokumen", document.documentType.label)
            DetailRow("Nomor Dokumen", document.documentNumber ?: "-")
            DetailRow("Kode Klasifikasi", document.classificationCode ?: "-")
            DetailRow("Judul", document.title)
            DetailRow("Deskripsi", document.description ?: "-")
            DetailRow("Tahun", document.year.toString())
            DetailRow("Bentuk Fisik", document.physicalForm.label)
            DetailRow("Kondisi", document.condition?.label ?: "Tidak diketahui")
            DetailRow("Status Keaslian", when(document.isCopy) {
                true -> "Kopi"
                false -> "Asli"
                null -> "Tidak diketahui"
            })
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
        DetailRow("Lokasi / Rak", item.locationLabel)
        DetailRow("ID Placement", placement?.id ?: "-")
        DetailRow("ID Lokasi", location?.id ?: "-")
        DetailRow("Ruangan", location?.room ?: "-")
        DetailRow("Rak", location?.shelf ?: "-")
        DetailRow("Box", location?.boxNumber ?: "-")
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)),
                shape = RoundedCornerShape(9999.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Simpan Perubahan")
            }

            OutlinedButton(
                onClick = onCancelEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(9999.dp)
            ) {
                Text("Batal Edit")
            }
        } else {
            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)),
                shape = RoundedCornerShape(9999.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Edit Dokumen")
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
                Text("Kembali")
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
            .border(1.dp, Color(0x33BFCABA), RoundedCornerShape(24.dp))
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
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
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
private fun SmallBadge(
    text: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(Color(0xFFE8F5E9))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PoppinsFont,
            color = Color(0xFF1B5E20)
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
            .background(if (isError) Color(0xFFFEE2E2) else Color(0xFFE8F5E9))
            .padding(16.dp)
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PoppinsFont,
            color = if (isError) Color(0xFF991B1B) else Color(0xFF1B5E20)
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
                    color = if (isDanger) Color(0xFFBA1A1A) else Color(0xFF0D631B)
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
                DocumentHeaderCard(
                    item = item,
                    isEditMode = false,
                    editedTitle = document.title,
                    onTitleChange = {},
                    editedDocumentNumber = document.documentNumber.orEmpty(),
                    onDocumentNumberChange = {}
                )
            }

            item {
                DocumentInformationCard(
                    item = item,
                    isEditMode = false,
                    editedDescription = document.description.orEmpty(),
                    onDescriptionChange = {},
                    editedDocumentCode = document.classificationCode.orEmpty(),
                    onDocumentCodeChange = {},
                    editedOriginInstance = document.originInstance.orEmpty(),
                    onOriginInstanceChange = {},
                    editedCopyCount = document.copyCount.toString(),
                    onCopyCountChange = {}
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