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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpkpad.arsipnonkeu.R
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
                title = stringResource(R.string.staging_manual_input),
                onProfileClick = onProfileClick
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
                    title = stringResource(R.string.manual_id_section_title),
                    description = stringResource(R.string.manual_id_section_desc)
                ) {
                    StagingDropdownField(
                        label = stringResource(R.string.staging_field_type_label),
                        value = documentType,
                        options = DocumentType.values().toList(),
                        optionLabel = { it.label },
                        onValueChange = { selected ->
                            documentType = selected ?: documentType
                        }
                    )

                    DetailTextField(
                        label = stringResource(R.string.staging_field_number_label),
                        value = documentNumber,
                        onValueChange = { if (it.length <= 50) documentNumber = it },
                        placeholder = stringResource(R.string.manual_number_placeholder)
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
                        label = stringResource(R.string.manual_title_label),
                        value = title,
                        onValueChange = { if (it.length <= 255) title = it },
                        placeholder = stringResource(R.string.manual_title_placeholder),
                        error = title.isBlank()
                    )

                    DetailTextField(
                        label = stringResource(R.string.staging_field_desc_label),
                        value = description,
                        onValueChange = { if (it.length <= 1000) description = it },
                        placeholder = stringResource(R.string.manual_desc_placeholder),
                        singleLine = false
                    )
                }
            }

            item {
                FormSection(
                    title = stringResource(R.string.manual_class_section_title),
                    description = stringResource(R.string.manual_class_section_desc)
                ) {
                    DetailTextField(
                        label = stringResource(R.string.staging_field_year_label),
                        value = year,
                        onValueChange = {},
                        placeholder = stringResource(R.string.staging_field_year_placeholder),
                        readOnly = true
                    )

                    StagingDropdownField(
                        label = stringResource(R.string.staging_field_physical_label),
                        value = physicalForm,
                        options = PhysicalForm.values().toList(),
                        optionLabel = { it.label },
                        onValueChange = { selected ->
                            physicalForm = selected ?: physicalForm
                        }
                    )

                    StagingDropdownField(
                        label = stringResource(R.string.staging_field_condition_label),
                        value = condition,
                        options = DocumentCondition.values().toList(),
                        optionLabel = { it.label },
                        allowNull = true,
                        nullLabel = stringResource(R.string.staging_field_unknown),
                        onValueChange = { selected ->
                            condition = selected
                        }
                    )

                    DetailTextField(
                        label = stringResource(R.string.staging_field_count_label),
                        value = copyCount,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                copyCount = input
                            }
                        },
                        placeholder = "1",
                        keyboardType = KeyboardType.Number
                    )

                    StagingDropdownField(
                        label = stringResource(R.string.staging_field_status_label),
                        value = status,
                        options = DocumentStatus.values().toList(),
                        optionLabel = { it.label },
                        onValueChange = { selected ->
                            status = selected ?: status
                        }
                    )
                }
            }

            item {
                FormSection(
                    title = stringResource(R.string.manual_origin_section_title),
                    description = stringResource(R.string.manual_origin_section_desc)
                ) {
                    DetailTextField(
                        label = stringResource(R.string.staging_field_origin_label),
                        value = originInstance,
                        onValueChange = { if (it.length <= 100) originInstance = it },
                        placeholder = stringResource(R.string.manual_origin_placeholder)
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
                Text(stringResource(R.string.staging_save_confirm_title))
            },
            text = {
                Text(stringResource(R.string.manual_class_section_desc))
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
                        text = stringResource(R.string.dashboard_add_button),
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
                    Text(stringResource(R.string.dashboard_cancel_button))
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
                Text(stringResource(R.string.manual_cancel_dialog_title))
            },
            text = {
                Text(stringResource(R.string.manual_cancel_dialog_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelConfirmDialog = false
                        onBackClick()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.manual_cancel_button),
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
                    Text(stringResource(R.string.manual_continue_button))
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
                text = stringResource(R.string.manual_error_invalid),
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
                    text = stringResource(R.string.dashboard_cancel_button),
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
                    contentDescription = null,
                    tint = Color.White
                )

                Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                Text(
                    text = stringResource(R.string.dashboard_add_button),
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
        title.isBlank() -> stringResource(R.string.manual_validation_empty_title)
        year.length != 4 || year.toIntOrNull() == null -> stringResource(R.string.manual_validation_invalid_year)
        copyCount.toIntOrNull() == null || (copyCount.toIntOrNull() ?: 0) <= 0 -> {
            stringResource(R.string.manual_validation_invalid_count)
        }
        else -> stringResource(R.string.manual_validation_valid)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> StagingDropdownField(
    label: String,
    value: T?,
    options: List<T>,
    optionLabel: (T) -> String,
    onValueChange: (T?) -> Unit,
    allowNull: Boolean = false,
    nullLabel: String = "Tidak diketahui"
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
            value = value?.let { optionLabel(it) } ?: nullLabel,
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
                color = Color.Black
            ),
            colors = detailTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            if (allowNull) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = nullLabel,
                            color = Color.Black,
                            fontFamily = PoppinsFont
                        )
                    },
                    onClick = {
                        onValueChange(null)
                        expanded = false
                    }
                )
            }

            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = optionLabel(option),
                            color = MaterialTheme.colorScheme.onSurface,
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
private fun DetailTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    error: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        label = {
            Text(
                text = label,
                color = if (error) MaterialTheme.colorScheme.error else Color.Black
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = Color.Black
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        shape = RoundedCornerShape(16.dp),
        isError = error,
        textStyle = LocalTextStyle.current.copy(
            color = Color.Black
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        colors = detailTextFieldColors()
    )
}

// @Preview(
//    showBackground = true,
//    showSystemUi = true,
//    device = "spec:width=390dp,height=844dp,dpi=420"
//)
// @Composable
// fun NewRecordScreenPreview() {
//    // NewRecordScreen(selectedYear = 2025, viewModel = ...)
// }
