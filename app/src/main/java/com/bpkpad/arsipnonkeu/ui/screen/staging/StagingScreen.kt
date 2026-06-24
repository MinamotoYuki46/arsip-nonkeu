package com.bpkpad.arsipnonkeu.ui.screen.staging

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bpkpad.arsipnonkeu.R
import com.bpkpad.arsipnonkeu.domain.model.*
import com.bpkpad.arsipnonkeu.ui.component.*
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray

private val PoppinsFont = FontFamily.Default

@Composable
fun StagingScreen(
    selectedYear: Int,
    onProfileClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onManualClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    onPushAllClick: () -> Unit = {},
    viewModel: StagingViewModel
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

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onPushAllClick()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.staging_title),
                onProfileClick = onProfileClick
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
                    onScanClick()
                },
                onImportClick = {
                    isFabExpanded = false
                    excelImportLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    onImportClick()
                }
            )
        },
        bottomBar = {
            StagingBottomBar(
                isLoading = uiState.isLoading,
                documentCount = uiState.documents.size,
                isStorageLocationValid = uiState.isStorageLocationValid,
                onPushAllClick = { showPushConfirmDialog = true }
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

            uiState.errorMessage?.let {
                ArsipMessageCard(
                    message = it,
                    variant = MessageVariant.DANGER,
                    onDismiss = viewModel::clearMessage,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                )
            }

            uiState.duplicateWarning?.let {
                ArsipMessageCard(
                    message = it,
                    variant = MessageVariant.WARNING,
                    onDismiss = viewModel::clearMessage,
                    onConfirm = { viewModel.pushAllToArchive() },
                    confirmText = stringResource(R.string.staging_warning_confirm),
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                )
            }

            StagingContentSection(
                documents = uiState.documents,
                onDocumentClick = { documentId -> viewModel.selectDocument(documentId) },
                onDeleteDocument = viewModel::deleteDocument
            )
        }
    }

    if (uiState.selectedDocument != null) {
        StagingDocumentDetailSheet(
            document = uiState.selectedDocument!!,
            selectedYear = selectedYear,
            onDismiss = viewModel::clearSelectedDocument,
            viewModel = viewModel,
            onSave = { type, num, code, title, desc, year, form, cond, count, copy, stat, inst ->
                viewModel.updateSelectedDocument(type, num, code, title, desc, year, form, cond, count, copy, stat, inst)
            },
            onDelete = viewModel::deleteSelectedDocument
        )
    }

    if (showPushConfirmDialog) {
        ArsipConfirmDialog(
            onDismissRequest = { showPushConfirmDialog = false },
            onConfirm = {
                showPushConfirmDialog = false
                viewModel.pushAllToArchive()
            },
            title = stringResource(R.string.staging_push_confirm_title),
            message = stringResource(R.string.staging_push_confirm_message, uiState.documents.size, uiState.storageLocationLabel),
            confirmText = stringResource(R.string.dashboard_add_button),
            dismissText = stringResource(R.string.dashboard_cancel_button)
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
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.staging_location_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF071E27)
                )

                val emptyLabel = stringResource(R.string.staging_location_empty)
                Text(
                    text = locationLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = PoppinsFont,
                    color = if (locationLabel == emptyLabel) Color(0xFFBA1A1A) else Color(0xFF0D631B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF0D631B)
            )
        }

        AnimatedVisibility(visible = isExpanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0x33BFCABA), RoundedCornerShape(24.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArsipTextField(
                    label = stringResource(R.string.staging_room_label),
                    value = room,
                    onValueChange = onRoomChange,
                    placeholder = stringResource(R.string.staging_room_placeholder)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ArsipTextField(
                        label = stringResource(R.string.staging_shelf_label),
                        value = shelf,
                        onValueChange = onShelfChange,
                        placeholder = stringResource(R.string.staging_shelf_placeholder),
                        modifier = Modifier.weight(1f)
                    )
                    ArsipTextField(
                        label = stringResource(R.string.staging_box_label),
                        value = boxNumber,
                        onValueChange = onBoxNumberChange,
                        placeholder = stringResource(R.string.staging_box_placeholder),
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
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.staging_content_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFont,
                color = Color(0xFF071E27)
            )
            Text(
                text = stringResource(R.string.staging_document_count_summary, documents.size),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PoppinsFont,
                color = Color(0xFF707A6C)
            )
        }

        if (documents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.staging_empty_message),
                    fontSize = 14.sp,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF40493D),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = documents, key = { it.id }) { document ->
                    StagingDocumentCard(document = document, onClick = { onDocumentClick(document.id) }, onDeleteClick = { onDeleteDocument(document.id) })
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
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
        ArsipBadge(text = document.source.label, variant = BadgeVariant.SUCCESS)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = stringResource(R.string.staging_delete_label),
            tint = Color(0xFFBA1A1A),
            modifier = Modifier.size(22.dp).clickable { showDeleteDialog = true }
        )
    }

    if (showDeleteDialog) {
        ArsipConfirmDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDeleteClick()
            },
            title = stringResource(R.string.staging_delete_dialog_title),
            message = stringResource(R.string.staging_delete_dialog_message),
            confirmText = stringResource(R.string.staging_delete_label),
            dismissText = stringResource(R.string.dashboard_cancel_button),
            isDanger = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StagingDocumentDetailSheet(
    document: StagingDocument,
    selectedYear: Int,
    onDismiss: () -> Unit,
    viewModel: StagingViewModel,
    onSave: (DocumentType, String?, String?, String, String?, Int, PhysicalForm, DocumentCondition?, Int, Boolean?, DocumentStatus, String?) -> Unit,
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
    var year by remember(document.id) { mutableStateOf(selectedYear.toString()) }
    var copyCount by remember(document.id) { mutableStateOf(document.copyCount.toString()) }
    var isCopy by remember(document.id) { mutableStateOf(document.isCopy) }
    var originInstance by remember(document.id) { mutableStateOf(document.originInstance.orEmpty()) }

    var selectedType by remember(document.id) { mutableStateOf(document.documentType) }
    var selectedPhysicalForm by remember(document.id) { mutableStateOf(document.physicalForm) }
    var selectedCondition by remember(document.id) { mutableStateOf(document.condition) }
    var selectedStatus by remember(document.id) { mutableStateOf(document.status) }

    val copyLabelTrue = stringResource(R.string.staging_field_copy_true)
    val copyLabelFalse = stringResource(R.string.staging_field_copy_false)
    val unknownLabel = stringResource(R.string.staging_field_unknown)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color.White) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = if (isEditMode) stringResource(R.string.staging_edit_sheet_title) else stringResource(R.string.staging_detail_sheet_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFont,
                    color = Color(0xFF071E27)
                )
            }

            if (isEditMode) {
                item { ArsipTextField(label = stringResource(R.string.staging_field_title_label), value = title, onValueChange = { if (it.length <= 255) title = it }, placeholder = stringResource(R.string.staging_field_title_placeholder), isError = title.isBlank()) }
                item { ArsipTextField(label = stringResource(R.string.staging_field_number_label), value = documentNumber, onValueChange = { if (it.length <= 50) documentNumber = it }, placeholder = stringResource(R.string.staging_field_number_placeholder)) }
                item {
                    ArchiveClassificationField(
                        selectedCode = documentCode,
                        selectedLabel = viewModel.getLoadedArchiveClassificationLabel(documentCode),
                        isRequired = false,
                        onClick = { showClassificationSheet = true }
                    )
                }
                item { ArsipTextField(label = stringResource(R.string.staging_field_desc_label), value = description, onValueChange = { if (it.length <= 1000) description = it }, placeholder = stringResource(R.string.staging_field_desc_placeholder), singleLine = false, minLines = 3) }
                item { ArsipTextField(label = stringResource(R.string.staging_field_year_label), value = year, onValueChange = {}, placeholder = stringResource(R.string.staging_field_year_placeholder), readOnly = true) }
                item { ArsipDropdownField(label = stringResource(R.string.staging_field_type_label), value = selectedType, options = DocumentType.entries.toList(), optionLabel = { it.label }, onValueChange = { it?.let { selectedType = it } }) }
                item { ArsipDropdownField(label = stringResource(R.string.staging_field_physical_label), value = selectedPhysicalForm, options = PhysicalForm.entries.toList(), optionLabel = { it.label }, onValueChange = { it?.let { selectedPhysicalForm = it } }) }
                item { ArsipDropdownField(label = stringResource(R.string.staging_field_condition_label), value = selectedCondition, options = DocumentCondition.entries.toList(), optionLabel = { it.label }, allowNull = true, nullLabel = unknownLabel, onValueChange = { selectedCondition = it }) }
                item { ArsipDropdownField(label = stringResource(R.string.staging_field_copy_label), value = isCopy, options = listOf(false, true), optionLabel = { if (it == true) copyLabelTrue else copyLabelFalse }, allowNull = true, nullLabel = unknownLabel, onValueChange = { isCopy = it }) }
                item { ArsipTextField(label = stringResource(R.string.staging_field_count_label), value = copyCount, onValueChange = { if (it.all { it.isDigit() }) copyCount = it }, placeholder = "1", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
                item { ArsipDropdownField(label = stringResource(R.string.staging_field_status_label), value = selectedStatus, options = DocumentStatus.entries.toList(), optionLabel = { it.label }, onValueChange = { it?.let { selectedStatus = it } }) }
                item { ArsipTextField(label = stringResource(R.string.staging_field_origin_label), value = originInstance, onValueChange = { if (it.length <= 100) originInstance = it }, placeholder = stringResource(R.string.staging_field_origin_placeholder)) }
            } else {
                item { ArsipDetailRow(stringResource(R.string.staging_field_title_label), document.title) }
                item { ArsipDetailRow(stringResource(R.string.staging_field_number_label), document.documentNumber ?: "-") }
                item { ArsipDetailRow("Kode Klasifikasi Dokumen", viewModel.getLoadedArchiveClassificationLabel(document.classificationCode).ifBlank { document.classificationCode ?: "-" }) }
                item { ArsipDetailRow(stringResource(R.string.staging_field_desc_label), document.description ?: "-") }
                item { ArsipDetailRow(stringResource(R.string.staging_field_type_label), document.documentType.label) }
                item { ArsipDetailRow(stringResource(R.string.staging_field_year_label), document.year.toString()) }
                item { ArsipDetailRow(stringResource(R.string.staging_field_physical_label), document.physicalForm.label) }
                item { ArsipDetailRow(stringResource(R.string.staging_field_condition_label), document.condition?.label ?: unknownLabel) }
                item {
                    ArsipDetailRow(
                        stringResource(R.string.staging_field_copy_label),
                        when (document.isCopy) {
                            true -> copyLabelTrue
                            false -> copyLabelFalse
                            null -> unknownLabel
                        }
                    )
                }
                item { ArsipDetailRow(stringResource(R.string.staging_field_count_label), document.copyCount.toString()) }
                item { ArsipDetailRow(stringResource(R.string.staging_field_status_label), document.status.label) }
                item { ArsipDetailRow(stringResource(R.string.staging_field_origin_label), document.originInstance ?: "-") }
                item { ArsipDetailRow("Sumber", document.source.label) }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(9999.dp),
                            border = BorderStroke(1.dp, Color(0xFF0D631B))
                        ) {
                            Text(text = stringResource(R.string.dashboard_cancel_button), color = Color(0xFF0D631B))
                        }
                        Button(onClick = { showSaveConfirmDialog = true }, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)), shape = RoundedCornerShape(9999.dp)) {
                            Text(text = stringResource(R.string.dashboard_add_button), color = Color.White)
                        }
                    } else {
                        OutlinedButton(onClick = { showDeleteDialog = true }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(9999.dp), border = BorderStroke(1.dp, Color(0xFFBA1A1A))) {
                            Text(text = stringResource(R.string.staging_delete_label), color = Color(0xFFBA1A1A))
                        }
                        Button(onClick = { isEditMode = true }, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)), shape = RoundedCornerShape(9999.dp)) {
                            Icon(imageVector = Icons.Outlined.Edit, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Edit", color = Color.White)
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
        onDismiss = { showClassificationSheet = false }
    )

    if (showSaveConfirmDialog) {
        ArsipConfirmDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            onConfirm = {
                onSave(selectedType, documentNumber.takeIf { it.isNotBlank() }, documentCode.takeIf { it.isNotBlank() }, title, description.takeIf { it.isNotBlank() }, year.toIntOrNull() ?: document.year, selectedPhysicalForm, selectedCondition, copyCount.toIntOrNull() ?: document.copyCount, isCopy, selectedStatus, originInstance.takeIf { it.isNotBlank() })
                isEditMode = false
                showSaveConfirmDialog = false
            },
            title = stringResource(R.string.staging_save_confirm_title),
            message = stringResource(R.string.staging_save_confirm_message),
            confirmText = stringResource(R.string.dashboard_add_button),
            dismissText = stringResource(R.string.dashboard_cancel_button)
        )
    }

    if (showDeleteDialog) {
        ArsipConfirmDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            title = stringResource(R.string.staging_delete_dialog_title),
            message = stringResource(R.string.staging_delete_confirm_message),
            confirmText = stringResource(R.string.staging_delete_label),
            dismissText = stringResource(R.string.dashboard_cancel_button),
            isDanger = true
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
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AnimatedVisibility(visible = isExpanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExtendedFloatingActionButton(onClick = onManualClick, containerColor = Color.White, contentColor = Color(0xFF0D631B)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.staging_manual_input))
                }
                ExtendedFloatingActionButton(onClick = onScanClick, containerColor = Color.White, contentColor = Color(0xFF0D631B)) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.staging_scan))
                }
                ExtendedFloatingActionButton(onClick = onImportClick, containerColor = Color.White, contentColor = Color(0xFF0D631B)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.staging_import))
                }
            }
        }
        FloatingActionButton(onClick = { onExpandedChange(!isExpanded) }, containerColor = Color(0xFF0D631B), contentColor = Color.White, shape = CircleShape) {
            Icon(imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add, contentDescription = "Aksi staging")
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
            LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp))
        }

        Button(
            onClick = onPushAllClick,
            enabled = !isLoading && documentCount > 0 && isStorageLocationValid,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)),
            shape = RoundedCornerShape(9999.dp)
        ) {
            Text(
                text = stringResource(R.string.staging_push_button),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PoppinsFont,
                color = Color.White
            )
        }

        if (!isStorageLocationValid) {
            Text(
                text = stringResource(R.string.staging_error_location_invalid),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PoppinsFont,
                color = Color(0xFFBA1A1A)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = "spec:width=390dp,height=844dp,dpi=420")
@Composable
fun StagingScreenPreview() {
    val context = LocalContext.current
    val viewModel: StagingViewModel = viewModel(factory = StagingViewModelFactory(context, 2025))
    StagingScreen(selectedYear = 2025, viewModel = viewModel)
}
