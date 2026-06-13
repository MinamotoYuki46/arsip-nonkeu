package com.bpkpad.arsipnonkeu.ui.screen.staging

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.ui.component.ArchiveClassificationField
import com.bpkpad.arsipnonkeu.ui.component.ArchiveClassificationSelectorSheet
import com.bpkpad.arsipnonkeu.ui.component.TopBar
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray

private val PoppinsFont = FontFamily.Default

@Composable
fun StagingScreen(
    onBackClick: () -> Unit = {},
    onManualClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    onPushAllClick: () -> Unit = {},
    viewModel: StagingViewModel = viewModel(
        factory = StagingViewModelFactory(LocalContext.current.applicationContext)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    val excelImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.importFromExcel(context, uri)
            }
        }
    )

    var isFabExpanded by remember { mutableStateOf(false) }
    var isLocationExpanded by remember { mutableStateOf(false) }
    var showPushConfirmDialog by remember { mutableStateOf(false) }

    fun hasInvalidStagingDocument(): Boolean {
        return uiState.documents.any { document ->
            document.title.isBlank() ||
                    document.year !in 1900..2100 ||
                    document.copyCount <= 0
        }
    }

    fun saveCurrentDocumentsToLocalDrafts() {
        uiState.documents.forEach { document ->
            val archiveCode = document.classificationCode
                ?.takeIf { it.isNotBlank() }
                ?: document.documentNumber
                    ?.takeIf { it.isNotBlank() }
                ?: document.id

            viewModel.saveCurrentStaging(
                archiveCode = archiveCode,
                title = document.title,
                documentType = document.documentType.name,
                physicalForm = document.physicalForm.name,
                condition = document.condition?.name ?: "UNKNOWN",
                status = document.status.name,
                locationText = uiState.storageLocationLabel,
                description = document.description.orEmpty()
            )
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onPushAllClick()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Staging Arsip",
                onProfileClick = {}
            )
        },
        floatingActionButton = {
            StagingFabMenu(
                isExpanded = isFabExpanded,
                onExpandedChange = { isFabExpanded = it },
                onManualClick = {
                    isFabExpanded = false
                    onManualClick()
                },
                onScanClick = {
                    isFabExpanded = false
                    viewModel.addDummyScanDocument()
                    onScanClick()
                },
                onImportClick = {
                    isFabExpanded = false
                    excelImportLauncher.launch(
                        arrayOf(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                    )
                    onImportClick()
                }
            )
        },
        bottomBar = {
            StagingBottomBar(
                isLoading = uiState.isLoading,
                documentCount = uiState.documents.size,
                isStorageLocationValid = uiState.isStorageLocationValid,
                onPushAllClick = {
                    showPushConfirmDialog = true
                }
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            StorageLocationSection(
                isExpanded = isLocationExpanded,
                onExpandedChange = { isLocationExpanded = it },
                room = uiState.room,
                shelf = uiState.shelf,
                boxNumber = uiState.boxNumber,
                locationLabel = uiState.storageLocationLabel,
                onRoomChange = viewModel::onRoomChange,
                onShelfChange = viewModel::onShelfChange,
                onBoxNumberChange = viewModel::onBoxNumberChange
            )

            if (uiState.errorMessage != null) {
                ErrorMessageCard(
                    message = uiState.errorMessage.orEmpty(),
                    onDismiss = viewModel::clearMessage
                )
            }

            StagingContentSection(
                documents = uiState.documents,
                onDocumentClick = { documentId ->
                    viewModel.selectDocument(documentId)
                },
                onDeleteDocument = viewModel::deleteDocument
            )
        }
    }

    if (uiState.selectedDocument != null) {
        StagingDocumentDetailSheet(
            document = uiState.selectedDocument!!,
            onDismiss = viewModel::clearSelectedDocument,
            viewModel = viewModel,
            onSave = { documentType,
                       documentNumber,
                       documentCode,
                       title,
                       description,
                       year,
                       physicalForm,
                       condition,
                       copyCount,
                       isCopy,
                       status,
                       originInstance ->
                viewModel.updateSelectedDocument(
                    documentType = documentType,
                    documentNumber = documentNumber,
                    classificationCode = documentCode,
                    title = title,
                    description = description,
                    year = year,
                    physicalForm = physicalForm,
                    condition = condition,
                    copyCount = copyCount,
                    isCopy = isCopy,
                    status = status,
                    originInstance = originInstance
                )
            },
            onDelete = viewModel::deleteSelectedDocument
        )
    }

    if (showPushConfirmDialog) {
        ConfirmPushDialog(
            documentCount = uiState.documents.size,
            locationLabel = uiState.storageLocationLabel,
            onConfirm = {
                showPushConfirmDialog = false

                if (
                    uiState.isStorageLocationValid &&
                    uiState.documents.isNotEmpty() &&
                    !hasInvalidStagingDocument()
                ) {
                    saveCurrentDocumentsToLocalDrafts()
                }

                viewModel.pushAllToArchive()
            },
            onDismiss = {
                showPushConfirmDialog = false
            }
        )
    }
}

@Composable
private fun StorageLocationSection(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    room: String,
    shelf: String,
    boxNumber: String,
    locationLabel: String,
    onRoomChange: (String) -> Unit,
    onShelfChange: (String) -> Unit,
    onBoxNumberChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundGray)
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0x33BFCABA), RoundedCornerShape(20.dp))
                .clickable { onExpandedChange(!isExpanded) }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Lokasi Penyimpanan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF071E27)
                )

                Text(
                    text = locationLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = PoppinsFont,
                    color = if (locationLabel == "Belum ditentukan") {
                        Color(0xFFBA1A1A)
                    } else {
                        Color(0xFF0D631B)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = if (isExpanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = null,
                tint = Color(0xFF0D631B)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0x33BFCABA), RoundedCornerShape(24.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailTextField(
                    label = "Ruangan",
                    value = room,
                    onValueChange = onRoomChange,
                    placeholder = "Contoh: Ruang Arsip"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailTextField(
                        label = "Rak",
                        value = shelf,
                        onValueChange = onShelfChange,
                        placeholder = "Rak 04-B",
                        modifier = Modifier.weight(1f)
                    )

                    DetailTextField(
                        label = "Box",
                        value = boxNumber,
                        onValueChange = onBoxNumberChange,
                        placeholder = "01",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StagingContentSection(
    documents: List<StagingDocument>,
    onDocumentClick: (String) -> Unit,
    onDeleteDocument: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dokumen Staging",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFont,
                color = Color(0xFF071E27)
            )

            Text(
                text = "${documents.size} dokumen",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PoppinsFont,
                color = Color(0xFF707A6C)
            )
        }

        if (documents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada dokumen di staging.\nGunakan tombol + untuk input manual, scan, atau import.",
                    fontSize = 14.sp,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF40493D)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 32.dp,
                    end = 32.dp,
                    top = 8.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = documents,
                    key = { it.id }
                ) { document ->
                    StagingDocumentCard(
                        document = document,
                        onClick = {
                            onDocumentClick(document.id)
                        },
                        onDeleteClick = {
                            onDeleteDocument(document.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StagingDocumentCard(
    document: StagingDocument,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.dp, Color(0x33BFCABA), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = document.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFont,
                color = Color(0xFF071E27),
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

        SmallBadge(text = document.source.label)

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Hapus",
            tint = Color(0xFFBA1A1A),
            modifier = Modifier
                .size(22.dp)
                .clickable {
                    showDeleteDialog = true
                }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Hapus dari Staging")
            },
            text = {
                Text("Hapus dokumen ini dari daftar staging?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text(
                        text = "Hapus",
                        color = Color(0xFFBA1A1A)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StagingDocumentDetailSheet(
    document: StagingDocument,
    onDismiss: () -> Unit,
    viewModel: StagingViewModel,
    onSave: (
        documentType: DocumentType,
        documentNumber: String?,
        documentCode: String?,
        title: String,
        description: String?,
        year: Int,
        physicalForm: PhysicalForm,
        condition: DocumentCondition?,
        copyCount: Int,
        isCopy: Boolean?,
        status: DocumentStatus,
        originInstance: String?
    ) -> Unit,
    onDelete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isEditMode by remember(document.id) { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }
    var showClassificationSheet by rememberSaveable(document.id) { mutableStateOf(false) }
    var classificationKeyword by rememberSaveable(document.id) { mutableStateOf("") }

    var title by remember(document.id) { mutableStateOf(document.title) }
    var documentNumber by remember(document.id) { mutableStateOf(document.documentNumber.orEmpty()) }
    var documentCode by remember(document.id) { mutableStateOf(document.classificationCode.orEmpty()) }
    var description by remember(document.id) { mutableStateOf(document.description.orEmpty()) }
    var year by remember(document.id) { mutableStateOf(document.year.toString()) }
    var copyCount by remember(document.id) { mutableStateOf(document.copyCount.toString()) }
    var isCopy by remember(document.id) { mutableStateOf(document.isCopy) }
    var originInstance by remember(document.id) { mutableStateOf(document.originInstance.orEmpty()) }

    var selectedType by remember(document.id) { mutableStateOf(document.documentType) }
    var selectedPhysicalForm by remember(document.id) { mutableStateOf(document.physicalForm) }
    var selectedCondition by remember(document.id) { mutableStateOf(document.condition) }
    var selectedStatus by remember(document.id) { mutableStateOf(document.status) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = if (isEditMode) "Edit Dokumen Staging" else "Detail Dokumen Staging",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF071E27)
                )
            }

            if (isEditMode) {
                item {
                    DetailTextField(
                        label = "Judul",
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "Judul dokumen"
                    )
                }

                item {
                    DetailTextField(
                        label = "Nomor Dokumen",
                        value = documentNumber,
                        onValueChange = { documentNumber = it },
                        placeholder = "Nomor dokumen"
                    )
                }

                item {
                    ArchiveClassificationField(
                        selectedCode = documentCode,
                        selectedLabel = viewModel.getLoadedArchiveClassificationLabel(documentCode),
                        isRequired = false,
                        onClick = {
                            showClassificationSheet = true
                        }
                    )
                }

                item {
                    DetailTextField(
                        label = "Deskripsi",
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Deskripsi dokumen",
                        singleLine = false
                    )
                }

                item {
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
                }

                item {
                    EnumChipSelector(
                        title = "Jenis Dokumen",
                        selected = selectedType,
                        values = DocumentType.values().toList(),
                        label = { it.label },
                        onSelected = { selected ->
                            selectedType = selected ?: selectedType
                        }
                    )
                }

                item {
                    EnumChipSelector(
                        title = "Bentuk Fisik",
                        selected = selectedPhysicalForm,
                        values = PhysicalForm.values().toList(),
                        label = { it.label },
                        onSelected = { selected ->
                            selectedPhysicalForm = selected ?: selectedPhysicalForm
                        }
                    )
                }

                item {
                    EnumChipSelector(
                        title = "Kondisi",
                        selected = selectedCondition,
                        values = DocumentCondition.values().toList(),
                        label = { it.label },
                        allowNull = true,
                        nullLabel = "Tidak diketahui",
                        onSelected = { selected ->
                            selectedCondition = selected
                        }
                    )
                }

                item {
                    EnumChipSelector(
                        title = "Keaslian",
                        selected = isCopy,
                        values = listOf(false, true),
                        label = { if (it == true) "Kopi" else "Asli" },
                        allowNull = true,
                        nullLabel = "Tidak diketahui",
                        onSelected = { selected ->
                            isCopy = selected
                        }
                    )
                }

                item {
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
                }

                item {
                    EnumChipSelector(
                        title = "Status",
                        selected = selectedStatus,
                        values = DocumentStatus.values().toList(),
                        label = { it.label },
                        onSelected = { selected ->
                            selectedStatus = selected ?: selectedStatus
                        }
                    )
                }

                item {
                    DetailTextField(
                        label = "Asal Instansi",
                        value = originInstance,
                        onValueChange = { originInstance = it },
                        placeholder = "Bagian Umum"
                    )
                }
            } else {
                item { DetailRow("Judul", document.title) }
                item { DetailRow("Nomor Dokumen", document.documentNumber ?: "-") }
                item {
                    DetailRow(
                        label = "Kode Klasifikasi Dokumen",
                        value = viewModel.getLoadedArchiveClassificationLabel(document.classificationCode)
                            .ifBlank { document.classificationCode ?: "-" }
                    )
                }
                item { DetailRow("Deskripsi", document.description ?: "-") }
                item { DetailRow("Jenis Dokumen", document.documentType.label) }
                item { DetailRow("Tahun", document.year.toString()) }
                item { DetailRow("Bentuk Fisik", document.physicalForm.label) }
                item { DetailRow("Kondisi", document.condition?.label ?: "Tidak diketahui") }
                item {
                    DetailRow(
                        "Keaslian",
                        when (document.isCopy) {
                            true -> "Kopi"
                            false -> "Asli"
                            null -> "Tidak diketahui"
                        }
                    )
                }
                item { DetailRow("Jumlah Salinan", document.copyCount.toString()) }
                item { DetailRow("Status", document.status.label) }
                item { DetailRow("Asal Instansi", document.originInstance ?: "-") }
                item { DetailRow("Sumber", document.source.label) }
                item { DetailRow("ID Staging", document.id) }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isEditMode) {
                        OutlinedButton(
                            onClick = {
                                isEditMode = false
                                title = document.title
                                documentNumber = document.documentNumber.orEmpty()
                                documentCode = document.classificationCode.orEmpty()
                                description = document.description.orEmpty()
                                year = document.year.toString()
                                copyCount = document.copyCount.toString()
                                isCopy = document.isCopy
                                originInstance = document.originInstance.orEmpty()
                                selectedType = document.documentType
                                selectedPhysicalForm = document.physicalForm
                                selectedCondition = document.condition
                                selectedStatus = document.status
                                showClassificationSheet = false
                                classificationKeyword = ""
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(9999.dp),
                            border = BorderStroke(1.dp, Color(0xFF0D631B))
                        ) {
                            Text(
                                text = "Batal",
                                color = Color(0xFF0D631B)
                            )
                        }

                        Button(
                            onClick = {
                                showSaveConfirmDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)),
                            shape = RoundedCornerShape(9999.dp)
                        ) {
                            Text("Simpan")
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                showDeleteDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(9999.dp),
                            border = BorderStroke(1.dp, Color(0xFFBA1A1A))
                        ) {
                            Text(
                                text = "Hapus",
                                color = Color(0xFFBA1A1A)
                            )
                        }

                        Button(
                            onClick = {
                                isEditMode = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)),
                            shape = RoundedCornerShape(9999.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text("Edit")
                        }
                    }
                }
            }
        }
    }

    ArchiveClassificationSelectorSheet(
        visible = showClassificationSheet,
        classifications = uiState.archiveClassifications,
        selectedCode = documentCode,
        keyword = classificationKeyword,
        isLoading = uiState.isClassificationLoading,
        onKeywordChange = { keyword ->
            classificationKeyword = keyword
            viewModel.loadArchiveClassifications(keyword)
        },
        onSelect = { classification ->
            documentCode = classification.code
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
                Text("Simpan Perubahan")
            },
            text = {
                Text("Simpan perubahan pada dokumen staging ini?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSave(
                            selectedType,
                            documentNumber.takeIf { it.isNotBlank() },
                            documentCode.takeIf { it.isNotBlank() },
                            title,
                            description.takeIf { it.isNotBlank() },
                            year.toIntOrNull() ?: document.year,
                            selectedPhysicalForm,
                            selectedCondition,
                            copyCount.toIntOrNull() ?: document.copyCount,
                            isCopy,
                            selectedStatus,
                            originInstance.takeIf { it.isNotBlank() }
                        )
                        isEditMode = false
                        showSaveConfirmDialog = false
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Hapus dari Staging")
            },
            text = {
                Text("Hapus dokumen ini dari staging? Data belum masuk ke arsip utama.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = "Hapus",
                        color = Color(0xFFBA1A1A)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }
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
            color = if (isSelected) Color(0xFFCBFFC2) else Color(0xFF40493D)
        )
    }
}

@Composable
private fun StagingFabMenu(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onManualClick: () -> Unit,
    onScanClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = onManualClick,
                    containerColor = Color.White,
                    contentColor = Color(0xFF0D631B)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Input Manual")
                }

                ExtendedFloatingActionButton(
                    onClick = onScanClick,
                    containerColor = Color.White,
                    contentColor = Color(0xFF0D631B)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Scan")
                }

                ExtendedFloatingActionButton(
                    onClick = onImportClick,
                    containerColor = Color.White,
                    contentColor = Color(0xFF0D631B)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Import")
                }
            }
        }

        FloatingActionButton(
            onClick = {
                onExpandedChange(!isExpanded)
            },
            containerColor = Color(0xFF0D631B),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Aksi staging"
            )
        }
    }
}

@Composable
private fun StagingBottomBar(
    isLoading: Boolean,
    documentCount: Int,
    isStorageLocationValid: Boolean,
    onPushAllClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color(0x33BFCABA))
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(24.dp)
            )
        }


        Button(
            onClick = onPushAllClick,
            enabled = !isLoading && documentCount > 0 && isStorageLocationValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)),
            shape = RoundedCornerShape(9999.dp)
        ) {
            Text(
                text = "Simpan Semua ke Arsip",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PoppinsFont,
                color = Color.White
            )
        }

        if (!isStorageLocationValid) {
            Text(
                text = "Ruangan dan rak wajib diisi.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PoppinsFont,
                color = Color(0xFFBA1A1A)
            )
        }
    }
}

@Composable
private fun ConfirmPushDialog(
    documentCount: Int,
    locationLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Simpan ke Arsip")
        },
        text = {
            Text(
                text = "Simpan $documentCount dokumen staging ke lokasi:\n\n$locationLabel?"
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Simpan",
                    color = Color(0xFF0D631B)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun ErrorMessageCard(
    message: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFEE2E2))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PoppinsFont,
            color = Color(0xFF991B1B),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "Tutup",
            modifier = Modifier.clickable(onClick = onDismiss),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PoppinsFont,
            color = Color(0xFF991B1B)
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
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF0D631B),
            unfocusedBorderColor = Color(0xFFBFCABA)
        )
    )
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

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=390dp,height=844dp,dpi=420"
)
@Composable
fun StagingScreenPreview() {
    StagingScreen()
}