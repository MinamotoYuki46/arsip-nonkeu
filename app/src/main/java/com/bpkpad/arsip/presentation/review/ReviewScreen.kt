package com.bpkpad.arsip.presentation.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpkpad.arsip.domain.model.ArchiveDocument
import com.bpkpad.arsip.presentation.components.BpkpadButton
import com.bpkpad.arsip.presentation.components.BpkpadTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    initialDocument: ArchiveDocument? = null,
    stagingId: String? = null,
    archiveId: String? = null,
    onSaveSuccess: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(initialDocument, stagingId, archiveId) {
        if (stagingId != null) {
            viewModel.loadFromStaging(stagingId)
        } else if (archiveId != null) {
            viewModel.loadFromArchive(archiveId)
        } else if (initialDocument != null) {
            viewModel.setDocument(initialDocument)
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSaveSuccess()
        }
    }

    ReviewScreenContent(
        uiState = uiState,
        isArchiveMode = archiveId != null,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onTypeChange = viewModel::onTypeChange,
        onSaveClick = {
            if (stagingId != null) {
                viewModel.updateInStaging()
            } else {
                viewModel.saveDocument(null)
            }
        },
        onDeleteClick = {
            if (stagingId != null) {
                viewModel.deleteFromStaging()
            } else if (archiveId != null) {
                viewModel.deleteFromArchive()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreenContent(
    uiState: ReviewUiState,
    isArchiveMode: Boolean = false,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    var isEditMode by remember { mutableStateOf(!isArchiveMode) }
    val isNewEntry = uiState.document?.id.isNullOrEmpty()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when {
                            isNewEntry -> "Add New Archive"
                            isEditMode -> "Edit Archive"
                            else -> "Archive Detail"
                        }
                    ) 
                },
                actions = {
                    if (isArchiveMode && !isEditMode) {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                    
                    if (!isNewEntry) {
                        IconButton(onClick = onDeleteClick ?: {}) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        val document = uiState.document
        
        if (document != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BpkpadTextField(
                    value = document.title,
                    onValueChange = onTitleChange,
                    label = "Title",
                    readOnly = !isEditMode
                )

                BpkpadTextField(
                    value = document.type,
                    onValueChange = onTypeChange,
                    label = "Type",
                    readOnly = !isEditMode
                )

                BpkpadTextField(
                    value = document.description,
                    onValueChange = onDescriptionChange,
                    label = "Description",
                    readOnly = !isEditMode
                )

                // Box & Location - Always Read-only and Grayed out
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    BpkpadTextField(
                        value = document.boxId,
                        onValueChange = {},
                        label = "Box ID",
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )
                    BpkpadTextField(
                        value = document.locationId,
                        onValueChange = {},
                        label = "Location ID",
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(text = "Date: ${java.util.Date(document.date)}", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.weight(1f))

                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
                }

                if (uiState.error != null) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }

                if (isEditMode) {
                    BpkpadButton(
                        text = if (isNewEntry) "Add to Archive" else "Update Document",
                        onClick = onSaveClick,
                        enabled = !uiState.isLoading
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewScreenPreview() {
    ReviewScreenContent(
        uiState = ReviewUiState(
            document = ArchiveDocument(
                id = "1",
                title = "Laporan Keuangan",
                type = "SURAT",
                date = System.currentTimeMillis(),
                description = "Laporan tahunan 2023",
                boxId = "B01",
                locationId = "L10"
            )
        ),
        onTitleChange = {},
        onDescriptionChange = {},
        onTypeChange = {},
        onSaveClick = {}
    )
}
