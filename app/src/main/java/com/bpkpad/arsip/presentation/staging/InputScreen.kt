package com.bpkpad.arsip.presentation.staging

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpkpad.arsip.core.domain.model.DocumentType
import com.bpkpad.arsip.core.domain.model.StagingDocument
import com.bpkpad.arsip.core.domain.model.StagingStatus
import com.bpkpad.arsip.presentation.components.BpkpadButton
import com.bpkpad.arsip.presentation.components.BpkpadTextField
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStaging: () -> Unit,
    viewModel: StagingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Local form state
    var title by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DocumentType.SURAT) }
    var typeExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Input Archive") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateToStaging) {
                        Text(
                            text = "Finish (${uiState.documents.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
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
                    BpkpadButton(
                        text = "Add to List",
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.addDocument(
                                    title = title,
                                    type = selectedType,
                                    year = year.toIntOrNull() ?: 2024
                                )
                                title = "" // clear form
                                year = ""
                            }
                        },
                        enabled = title.isNotBlank() && year.length == 4
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sticky Form at the top
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BpkpadTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Document Title"
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = selectedType.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Type") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { typeExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, null)
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false }
                            ) {
                                DocumentType.entries.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.name) },
                                        onClick = {
                                            selectedType = type
                                            typeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        BpkpadTextField(
                            value = year,
                            onValueChange = { if (it.length <= 4) year = it },
                            label = "Year",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
