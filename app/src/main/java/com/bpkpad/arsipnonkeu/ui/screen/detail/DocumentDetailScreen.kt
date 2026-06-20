package com.bpkpad.arsipnonkeu.ui.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpkpad.arsipnonkeu.R
import com.bpkpad.arsipnonkeu.domain.model.*
import com.bpkpad.arsipnonkeu.ui.component.*
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray
import com.bpkpad.arsipnonkeu.util.DateFormatter

private val PoppinsFont = FontFamily.Default

@Composable
fun DocumentDetailScreen(
    documentId: String,
    userRole: String = "",
    onProfileClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: DocumentDetailViewModel = remember { DocumentDetailViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val canEdit = remember(userRole) { userRole.equals("ARSIPARIS", ignoreCase = true) }

    var isEditMode by remember { mutableStateOf(false) }
    var showEditConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var editedDocumentType by remember { mutableStateOf(DocumentType.SURAT) }
    var editedDocumentNumber by remember { mutableStateOf("") }
    var editedDocumentCode by remember { mutableStateOf("") }
    var editedTitle by remember { mutableStateOf("") }
    var editedDescription by remember { mutableStateOf("") }
    var editedYear by remember { mutableStateOf("") }
    var editedPhysicalForm by remember { mutableStateOf(PhysicalForm.SHEET) }
    var editedCondition by remember { mutableStateOf<DocumentCondition?>(null) }
    var editedIsCopy by remember { mutableStateOf<Boolean?>(null) }
    var editedCopyCount by remember { mutableStateOf("") }
    var editedStatus by remember { mutableStateOf(DocumentStatus.AVAILABLE) }
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

    LaunchedEffect(documentId) { viewModel.loadDocument(documentId) }
    LaunchedEffect(uiState.item) { uiState.item?.document?.let { syncEditedState(it) } }
    LaunchedEffect(uiState.isDeleted) { if (uiState.isDeleted) onBackClick() }

    Scaffold(
        topBar = { TopBar(title = stringResource(R.string.detail_title_topbar), onProfileClick = onProfileClick) },
        containerColor = BackgroundGray
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    LoadingIndicator()
                }
            }
            uiState.errorMessage != null && uiState.item == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(text = uiState.errorMessage.orEmpty(), color = Color(0xFFBA1A1A), fontSize = 14.sp, fontFamily = PoppinsFont, textAlign = TextAlign.Center)
                }
            }
            uiState.item != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 32.dp),
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
                    item { DocumentPlacementCard(item = uiState.item!!, viewModel = viewModel) }
                    item { DocumentSystemCard(item = uiState.item!!, viewModel = viewModel) }
                    
                    uiState.errorMessage?.let { message ->
                        item { ArsipMessageCard(message = message, variant = MessageVariant.DANGER) }
                    }
                    uiState.successMessage?.let { message ->
                        item { ArsipMessageCard(message = message, variant = MessageVariant.SUCCESS) }
                    }

                    item {
                        DetailActionButtons(
                            isEditMode = isEditMode,
                            canEdit = canEdit,
                            onEditClick = { if (canEdit) { isEditMode = true; viewModel.clearMessage() } },
                            onCancelEditClick = { isEditMode = false; uiState.item?.document?.let { syncEditedState(it) } },
                            onSaveClick = { showEditConfirmDialog = true },
                            onDeleteClick = { showDeleteConfirmDialog = true },
                            onBackClick = onBackClick
                        )
                    }
                }
            }
        }
    }

    if (showEditConfirmDialog) {
        ArsipConfirmDialog(
            onDismissRequest = { showEditConfirmDialog = false },
            onConfirm = {
                uiState.item?.document?.let { currentDocument ->
                    viewModel.updateDocument(currentDocument.copy(
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
                    ))
                }
                isEditMode = false
                showEditConfirmDialog = false
            },
            title = stringResource(R.string.detail_edit_confirm_title),
            message = stringResource(R.string.detail_edit_confirm_message),
            confirmText = stringResource(R.string.dashboard_add_button),
            dismissText = stringResource(R.string.dashboard_cancel_button)
        )
    }

    if (showDeleteConfirmDialog) {
        ArsipConfirmDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            onConfirm = { viewModel.deleteDocument(); showDeleteConfirmDialog = false },
            title = stringResource(R.string.detail_delete_confirm_title),
            message = stringResource(R.string.detail_delete_confirm_message),
            confirmText = stringResource(R.string.staging_delete_label),
            dismissText = stringResource(R.string.dashboard_cancel_button),
            isDanger = true
        )
    }

    ArchiveClassificationSelectorSheet(
        visible = showClassificationSheet,
        classifications = uiState.archiveClassifications,
        selectedCode = editedDocumentCode,
        keyword = classificationKeyword,
        isLoading = uiState.isClassificationLoading,
        onKeywordChange = { keyword -> classificationKeyword = keyword; viewModel.loadArchiveClassifications(keyword) },
        onSelect = { classification -> editedDocumentCode = classification.code; classificationKeyword = ""; showClassificationSheet = false },
        onDismiss = { showClassificationSheet = false }
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
    val copyLabelTrue = stringResource(R.string.staging_field_copy_true)
    val copyLabelFalse = stringResource(R.string.staging_field_copy_false)
    val unknownLabel = stringResource(R.string.staging_field_unknown)

    ArsipCard(title = stringResource(R.string.detail_section_info)) {
        if (isEditMode) {
            ArsipDropdownField(label = stringResource(R.string.staging_field_type_label), value = editedDocumentType, options = DocumentType.entries.toList(), optionLabel = { it.label }, onValueChange = { it?.let { onDocumentTypeChange(it) } })
            ArsipTextField(label = stringResource(R.string.staging_field_number_label), value = editedDocumentNumber, onValueChange = onDocumentNumberChange)
            ArchiveClassificationField(selectedCode = editedDocumentCode, selectedLabel = viewModel.getLoadedArchiveClassificationLabel(editedDocumentCode), onClick = onClassificationClick)
            ArsipTextField(label = stringResource(R.string.manual_title_label), value = editedTitle, onValueChange = onTitleChange)
            ArsipTextField(label = stringResource(R.string.staging_field_desc_label), value = editedDescription, onValueChange = onDescriptionChange, minLines = 3, singleLine = false)
            ArsipTextField(label = stringResource(R.string.staging_field_year_label), value = editedYear, onValueChange = {}, readOnly = true)
            ArsipDropdownField(label = stringResource(R.string.staging_field_physical_label), value = editedPhysicalForm, options = PhysicalForm.entries.toList(), optionLabel = { it.label }, onValueChange = { it?.let { onPhysicalFormChange(it) } })
            ArsipDropdownField(label = stringResource(R.string.staging_field_condition_label), value = editedCondition, options = listOf<DocumentCondition?>(null) + DocumentCondition.entries.toList(), optionLabel = { it?.label ?: unknownLabel }, allowNull = true, nullLabel = unknownLabel, onValueChange = onConditionChange)
            ArsipDropdownField(label = stringResource(R.string.staging_field_copy_label), value = editedIsCopy, options = listOf<Boolean?>(null, false, true), optionLabel = { when (it) { true -> copyLabelTrue; false -> copyLabelFalse; null -> unknownLabel } }, allowNull = true, nullLabel = unknownLabel, onValueChange = onIsCopyChange)
            ArsipTextField(label = stringResource(R.string.staging_field_count_label), value = editedCopyCount, onValueChange = onCopyCountChange, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            ArsipDropdownField(label = stringResource(R.string.staging_field_status_label), value = editedStatus, options = DocumentStatus.entries.toList(), optionLabel = { it.label }, onValueChange = { it?.let { onStatusChange(it) } })
            ArsipTextField(label = stringResource(R.string.staging_field_origin_label), value = editedOriginInstance, onValueChange = onOriginInstanceChange)
        } else {
            ArsipDetailRow(stringResource(R.string.staging_field_type_label), document.documentType.label)
            ArsipDetailRow(stringResource(R.string.staging_field_number_label), document.documentNumber ?: "-")
            ArsipDetailRow("Kode Klasifikasi", viewModel.getLoadedArchiveClassificationLabel(document.classificationCode).ifBlank { document.classificationCode ?: "-" })
            ArsipDetailRow(stringResource(R.string.staging_field_title_label), document.title)
            ArsipDetailRow(stringResource(R.string.staging_field_desc_label), document.description ?: "-")
            ArsipDetailRow(stringResource(R.string.staging_field_year_label), document.year.toString())
            ArsipDetailRow(stringResource(R.string.staging_field_physical_label), document.physicalForm.label)
            ArsipDetailRow(stringResource(R.string.staging_field_condition_label), document.condition?.label ?: unknownLabel)
            ArsipDetailRow(stringResource(R.string.staging_field_copy_label), when (document.isCopy) { true -> copyLabelTrue; false -> copyLabelFalse; null -> unknownLabel })
            ArsipDetailRow(stringResource(R.string.staging_field_count_label), document.copyCount.toString())
            ArsipDetailRow(stringResource(R.string.staging_field_status_label), document.status.label)
            ArsipDetailRow(stringResource(R.string.staging_field_origin_label), document.originInstance ?: "-")
        }
    }
}

@Composable
private fun DocumentPlacementCard(item: ArchiveDocumentListItem, viewModel: DocumentDetailViewModel) {
    val placement = item.currentPlacement
    val location = item.storageLocation

    ArsipCard(title = stringResource(R.string.detail_section_placement)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LocationBadgeItem(title = stringResource(R.string.detail_placement_room), code = location?.room ?: "-", modifier = Modifier.weight(1f))
            LocationBadgeItem(title = stringResource(R.string.detail_placement_shelf), code = location?.shelf ?: "-", modifier = Modifier.weight(1f))
            LocationBadgeItem(title = stringResource(R.string.detail_placement_box), code = location?.boxNumber ?: "-", modifier = Modifier.weight(1f), isActiveColor = true)
        }
        ArsipDetailRow(stringResource(R.string.detail_placed_at), DateFormatter.formatIsoToHuman(placement?.placedAt))
        ArsipDetailRow(stringResource(R.string.detail_removed_at), DateFormatter.formatIsoToHuman(placement?.removedAt))
        ArsipDetailRow(stringResource(R.string.detail_placed_by), viewModel.getUserDisplayName(placement?.userId))
    }
}

@Composable
private fun DocumentSystemCard(item: ArchiveDocumentListItem, viewModel: DocumentDetailViewModel) {
    val document = item.document
    ArsipCard(title = stringResource(R.string.detail_section_system)) {
        ArsipDetailRow(stringResource(R.string.detail_created_by), viewModel.getUserDisplayName(document.createdBy))
        ArsipDetailRow(stringResource(R.string.detail_updated_by), viewModel.getUserDisplayName(document.updatedBy))
        ArsipDetailRow(stringResource(R.string.detail_created_at), DateFormatter.formatIsoToHuman(document.createdAt))
        ArsipDetailRow(stringResource(R.string.detail_updated_at), DateFormatter.formatIsoToHuman(document.updatedAt))
        ArsipDetailRow(stringResource(R.string.detail_deleted_at), DateFormatter.formatIsoToHuman(document.deletedAt))
    }
}

@Composable
private fun DetailActionButtons(isEditMode: Boolean, canEdit: Boolean, onEditClick: () -> Unit, onCancelEditClick: () -> Unit, onSaveClick: () -> Unit, onDeleteClick: () -> Unit, onBackClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isEditMode) {
            Button(onClick = onSaveClick, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)), shape = RoundedCornerShape(9999.dp)) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.padding(4.dp))
                Text(text = stringResource(R.string.detail_save_changes), color = Color.White)
            }
            OutlinedButton(onClick = onCancelEditClick, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(9999.dp)) {
                Text(text = stringResource(R.string.detail_cancel_edit), color = Color(0xFFBA1A1A))
            }
        } else {
            if (canEdit) {
                Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)), shape = RoundedCornerShape(9999.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = stringResource(R.string.detail_edit_button), color = Color.White)
                }
                OutlinedButton(onClick = onDeleteClick, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(9999.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFBA1A1A))
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = stringResource(R.string.detail_delete_button), color = Color(0xFFBA1A1A))
                }
            }
            OutlinedButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(9999.dp)) {
                Text(text = stringResource(R.string.detail_back_button), color = Color.Black)
            }
        }
    }
}

@Composable
private fun LocationBadgeItem(title: String, code: String, isActiveColor: Boolean = false, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(if (isActiveColor) Color(0xFF2E7D32) else Color(0xFFE6F6FF))
            .border(width = 1.dp, color = if (isActiveColor) Color(0xFF0D631B) else Color(0xFFBFCABA), shape = RoundedCornerShape(32.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title.uppercase(), fontSize = 12.sp, fontFamily = PoppinsFont, fontWeight = FontWeight.Bold, letterSpacing = 0.48.sp, color = if (isActiveColor) Color(0xFFCBFFC2) else Color(0xFF40493D))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = code, fontSize = 24.sp, fontFamily = PoppinsFont, fontWeight = FontWeight.SemiBold, color = if (isActiveColor) Color(0xFFCBFFC2) else Color(0xFF0D631B))
    }
}

@Preview(showBackground = true, showSystemUi = true, device = "spec:width=390dp,height=844dp,dpi=420")
@Composable
fun DocumentDetailContentPreview() {
    // Preview logic remains same but using new components
}
