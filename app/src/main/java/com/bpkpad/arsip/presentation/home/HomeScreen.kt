package com.bpkpad.arsip.presentation.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpkpad.arsip.domain.model.ArchiveDocument
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToManual: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val excelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                viewModel.importFromExcel(context, it)
                onNavigateToManual() // Navigate to staging/input area
            }
        }
    )

    LaunchedEffect(uiState.exportFile) {
        uiState.exportFile?.let { file ->
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Excel File"))
            viewModel.clearExportFile()
        }
    }

    HomeScreenContent(
        uiState = uiState,
        onRetry = { viewModel.getArchives() },
        onScanClick = onNavigateToScan,
        onManualClick = onNavigateToManual,
        onExportClick = { viewModel.exportToExcel(context.filesDir) },
        onImportClick = {
            excelPickerLauncher.launch(
                arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            )
        },
        onItemClick = onNavigateToDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onRetry: () -> Unit,
    onScanClick: () -> Unit,
    onManualClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onItemClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BPKPAD Archives") },
                actions = {
                    IconButton(onClick = onExportClick) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export to Excel")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onManualClick, // This will now go to Staging
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Document")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = uiState.error, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }

            if (!uiState.isLoading && uiState.archives.isEmpty() && uiState.error == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No archives yet.",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Tap + to add manually or Scan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.archives) { archive ->
                    ArchiveItem(
                        archive = archive,
                        onClick = { onItemClick(archive.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveItem(
    archive: ArchiveDocument,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = archive.title, style = MaterialTheme.typography.titleMedium)
            Text(text = archive.type, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = archive.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(
        uiState = HomeUiState(
            archives = listOf(
                ArchiveDocument(
                    id = "1",
                    title = "Legal Document 2023",
                    type = "Legal",
                    date = System.currentTimeMillis(),
                    description = "Description for legal document",
                    boxId = "B1",
                    locationId = "L1"
                )
            )
        ),
        onRetry = {},
        onScanClick = {},
        onManualClick = {},
        onExportClick = {},
        onImportClick = {},
        onItemClick = {}
    )
}
