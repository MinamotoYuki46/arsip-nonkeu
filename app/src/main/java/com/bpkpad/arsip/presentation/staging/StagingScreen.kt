package com.bpkpad.arsip.presentation.staging

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpkpad.arsip.core.domain.model.StagingDocument
import com.bpkpad.arsip.presentation.components.BpkpadButton
import com.bpkpad.arsip.presentation.components.BpkpadTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StagingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToInput: () -> Unit,
    onNavigateToScan: () -> Unit,
    viewModel: StagingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isFabExpanded by remember { mutableStateOf(false) }

    val excelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { viewModel.importFromExcel(context, it) }
        }
    )

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staging Area") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            // Only show FAB if NOT loading to prevent layout shifting
            if (!uiState.isLoading) {
                Column(horizontalAlignment = Alignment.End) {
                    AnimatedVisibility(
                        visible = isFabExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            SmallFloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    onNavigateToInput()
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Manual Form")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.EditNote, contentDescription = null)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            SmallFloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    onNavigateToScan()
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Scan Document")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.DocumentScanner, contentDescription = null)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            SmallFloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    excelPickerLauncher.launch(
                                        arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                    )
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Import Excel")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.FileUpload, contentDescription = null)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    FloatingActionButton(
                        onClick = { isFabExpanded = !isFabExpanded },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            if (isFabExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Expand Options"
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    if (uiState.isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }

                    if (uiState.error != null) {
                        Text(
                            text = uiState.error ?: "Unknown Error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    BpkpadButton(
                        text = "Save All to Archive",
                        onClick = viewModel::pushToCloud,
                        enabled = !uiState.isLoading && uiState.documents.isNotEmpty()
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Bulk Assignment Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Assign to All", style = MaterialTheme.typography.titleMedium)
                    BpkpadTextField(
                        value = uiState.boxId,
                        onValueChange = viewModel::onBoxIdChange,
                        label = "Box ID"
                    )
                    BpkpadTextField(
                        value = uiState.locationId,
                        onValueChange = viewModel::onLocationIdChange,
                        label = "Location ID"
                    )
                }
            }

            Text(
                text = "Documents in Session (${uiState.documents.size})",
                style = MaterialTheme.typography.titleSmall
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Extra padding for FAB
            ) {
                items(uiState.documents) { doc ->
                    StagingItem(
                        document = doc,
                        onEdit = { onNavigateToEdit(doc.id) },
                        onDelete = { viewModel.deleteDocument(doc.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StagingItem(
    document: StagingDocument,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = document.title, style = MaterialTheme.typography.bodyLarge)
                Text(text = "${document.type} • ${document.year}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
