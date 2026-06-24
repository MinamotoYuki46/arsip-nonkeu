package com.bpkpad.arsipnonkeu.ui.screen.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpkpad.arsipnonkeu.R
import com.bpkpad.arsipnonkeu.domain.model.*
import com.bpkpad.arsipnonkeu.ui.component.*
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingViewModel
import com.bpkpad.arsipnonkeu.ui.theme.BackgroundGray

private val PoppinsFont = FontFamily.Default

@Composable
fun NewRecordScreen(
    selectedYear: Int,
    onProfileClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onSave: () -> Unit = {},
    viewModel: StagingViewModel
) {
    var documentType by remember { mutableStateOf(DocumentType.SURAT) }
    var documentNumber by remember { mutableStateOf("") }
    var classificationCode by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var year by remember { mutableStateOf(selectedYear.toString()) }
    var physicalForm by remember { mutableStateOf(PhysicalForm.SHEET) }
    var condition by remember { mutableStateOf<DocumentCondition?>(DocumentCondition.GOOD) }
    var copyCount by remember { mutableStateOf("1") }
    var status by remember { mutableStateOf(DocumentStatus.AVAILABLE) }
    var originInstance by remember { mutableStateOf("") }

    var showSaveConfirmDialog by remember { mutableStateOf(false) }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }

    var showClassificationSheet by rememberSaveable { mutableStateOf(false) }
    var classificationKeyword by rememberSaveable { mutableStateOf("") }

    val isFormValid by remember(title, year, copyCount) {
        derivedStateOf {
            title.isNotBlank() && year.length == 4 && year.toIntOrNull() != null &&
                    copyCount.toIntOrNull() != null && (copyCount.toIntOrNull() ?: 0) > 0
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.staging_manual_input),
                onProfileClick = onProfileClick
            )
        },
        bottomBar = {
            ManualInputBottomBar(
                isFormValid = isFormValid,
                onCancelClick = { showCancelConfirmDialog = true },
                onSaveClick = { showSaveConfirmDialog = true }
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ArsipCard(title = stringResource(R.string.manual_id_section_title)) {
                    Text(text = stringResource(R.string.manual_id_section_desc), fontSize = 13.sp, color = Color(0xFF40493D))
                    ArsipDropdownField(
                        label = stringResource(R.string.staging_field_type_label),
                        value = documentType,
                        options = DocumentType.entries.toList(),
                        optionLabel = { it.label },
                        onValueChange = { it?.let { documentType = it } }
                    )
                    ArsipTextField(
                        label = stringResource(R.string.staging_field_number_label),
                        value = documentNumber,
                        onValueChange = { if (it.length <= 50) documentNumber = it },
                        placeholder = stringResource(R.string.manual_number_placeholder)
                    )
                    ArchiveClassificationField(
                        selectedCode = classificationCode,
                        selectedLabel = viewModel.getLoadedArchiveClassificationLabel(classificationCode),
                        isRequired = false,
                        onClick = { showClassificationSheet = true }
                    )
                    ArsipTextField(
                        label = stringResource(R.string.manual_title_label),
                        value = title,
                        onValueChange = { if (it.length <= 255) title = it },
                        placeholder = stringResource(R.string.manual_title_placeholder),
                        isError = title.isBlank()
                    )
                    ArsipTextField(
                        label = stringResource(R.string.staging_field_desc_label),
                        value = description,
                        onValueChange = { if (it.length <= 1000) description = it },
                        placeholder = stringResource(R.string.manual_desc_placeholder),
                        singleLine = false,
                        minLines = 3
                    )
                }
            }

            item {
                ArsipCard(title = stringResource(R.string.manual_class_section_title)) {
                    Text(text = stringResource(R.string.manual_class_section_desc), fontSize = 13.sp, color = Color(0xFF40493D))
                    ArsipTextField(label = stringResource(R.string.staging_field_year_label), value = year, onValueChange = {}, placeholder = stringResource(R.string.staging_field_year_placeholder), readOnly = true)
                    ArsipDropdownField(label = stringResource(R.string.staging_field_physical_label), value = physicalForm, options = PhysicalForm.entries.toList(), optionLabel = { it.label }, onValueChange = { it?.let { physicalForm = it } })
                    ArsipDropdownField(label = stringResource(R.string.staging_field_condition_label), value = condition, options = DocumentCondition.entries.toList(), optionLabel = { it.label }, allowNull = true, nullLabel = stringResource(R.string.staging_field_unknown), onValueChange = { condition = it })
                    ArsipTextField(label = stringResource(R.string.staging_field_count_label), value = copyCount, onValueChange = { if (it.all { it.isDigit() }) copyCount = it }, placeholder = "1", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    ArsipDropdownField(label = stringResource(R.string.staging_field_status_label), value = status, options = DocumentStatus.entries.toList(), optionLabel = { it.label }, onValueChange = { it?.let { status = it } })
                }
            }

            item {
                ArsipCard(title = stringResource(R.string.manual_origin_section_title)) {
                    Text(text = stringResource(R.string.manual_origin_section_desc), fontSize = 13.sp, color = Color(0xFF40493D))
                    ArsipTextField(label = stringResource(R.string.staging_field_origin_label), value = originInstance, onValueChange = { if (it.length <= 100) originInstance = it }, placeholder = stringResource(R.string.manual_origin_placeholder))
                }
            }

            item {
                ValidationInfoCard(isFormValid = isFormValid, title = title, year = year, copyCount = copyCount)
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
        onDismiss = { showClassificationSheet = false }
    )

    if (showSaveConfirmDialog) {
        ArsipConfirmDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            onConfirm = {
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
            },
            title = stringResource(R.string.staging_save_confirm_title),
            message = stringResource(R.string.manual_class_section_desc),
            confirmText = stringResource(R.string.dashboard_add_button),
            dismissText = stringResource(R.string.dashboard_cancel_button)
        )
    }

    if (showCancelConfirmDialog) {
        ArsipConfirmDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            onConfirm = {
                showCancelConfirmDialog = false
                onBackClick()
            },
            title = stringResource(R.string.manual_cancel_dialog_title),
            message = stringResource(R.string.manual_cancel_dialog_message),
            confirmText = stringResource(R.string.manual_cancel_button),
            dismissText = stringResource(R.string.manual_continue_button),
            isDanger = true
        )
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
                text = stringResource(R.string.manual_error_invalid),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PoppinsFont,
                color = Color(0xFFBA1A1A)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(9999.dp),
                border = BorderStroke(1.dp, Color(0xFF0D631B))
            ) {
                Text(text = stringResource(R.string.dashboard_cancel_button), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = PoppinsFont, color = Color(0xFF0D631B))
            }
            Button(
                onClick = onSaveClick,
                enabled = isFormValid,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B)),
                shape = RoundedCornerShape(9999.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = stringResource(R.string.dashboard_add_button), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = PoppinsFont, color = Color.White)
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
        title.isBlank() -> stringResource(R.string.manual_validation_empty_title)
        title.length > 255 -> stringResource(R.string.manual_validation_title_too_long)
        year.length != 4 || year.toIntOrNull() == null -> stringResource(R.string.manual_validation_invalid_year)
        copyCount.toIntOrNull() == null || (copyCount.toIntOrNull() ?: 0) <= 0 -> stringResource(R.string.manual_validation_invalid_count)
        else -> stringResource(R.string.manual_validation_valid)
    }

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(if (isFormValid) Color(0xFFE8F5E9) else Color(0xFFFFF3CD)).padding(16.dp)
    ) {
        Text(text = message, fontSize = 13.sp, fontWeight = FontWeight.Medium, fontFamily = PoppinsFont, color = if (isFormValid) Color(0xFF1B5E20) else Color(0xFF92400E))
    }
}
