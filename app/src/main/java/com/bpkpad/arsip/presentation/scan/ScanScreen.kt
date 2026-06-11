package com.bpkpad.arsip.presentation.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpkpad.arsip.core.domain.model.DocumentType
import com.bpkpad.arsip.core.domain.model.StagingStatus
import com.bpkpad.arsip.domain.model.ArchiveDocument
import com.bpkpad.arsip.presentation.staging.StagingViewModel

@Composable
fun ScanScreen(
    onNavigateToReview: (ArchiveDocument) -> Unit,
    viewModel: ScanViewModel = hiltViewModel(),
    stagingViewModel: StagingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )
    
    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.parsedDocument) {
        uiState.parsedDocument?.let { doc ->
            stagingViewModel.addDocument(
                title = doc.title,
                type = try { DocumentType.valueOf(doc.type) } catch(e: Exception) { DocumentType.SURAT },
                year = 2024
            )
            onNavigateToReview(doc)
            viewModel.clearState()
        }
    }

    var selectedType by remember { mutableStateOf(DocumentType.SURAT) }

    if (hasCameraPermission) {
        ScanScreenContent(
            uiState = uiState,
            selectedType = selectedType,
            onCapture = { imageBytes ->
                viewModel.onImageCaptured(imageBytes, selectedType)
            },
            onTypeChange = { selectedType = it }
        )
    } else {
        PermissionDeniedContent(onOpenSettings = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        })
    }
}

@Composable
fun PermissionDeniedContent(onOpenSettings: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Camera permission is required to scan documents.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        }
    }
}

@Composable
fun ScanScreenContent(
    uiState: ScanUiState,
    selectedType: DocumentType,
    onCapture: (ByteArray) -> Unit,
    onTypeChange: (DocumentType) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    LaunchedEffect(previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("ScanScreen", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    ScanScreenUI(
        uiState = uiState,
        previewView = previewView,
        selectedType = selectedType,
        onCaptureClick = {
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)
                        onCapture(bytes)
                        image.close()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("ScanScreen", "Image capture failed", exception)
                    }
                }
            )
        },
        onTypeChange = onTypeChange
    )
}

@Composable
fun ScanScreenUI(
    uiState: ScanUiState,
    previewView: PreviewView?,
    selectedType: DocumentType,
    onCaptureClick: () -> Unit,
    onTypeChange: (DocumentType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (previewView != null) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Placeholder for Preview
            Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        }

        // Top Dropdown for Document Type
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.5f),
                contentColor = Color.White
            ),
            shape = CircleShape
        ) {
            Row(
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Scan Type: ${selectedType.name}")
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DocumentType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name) },
                        onClick = {
                            onTypeChange(type)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        // Capture Button (Rounded & Centered at Bottom)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
                .clickable(enabled = !uiState.isLoading) { onCaptureClick() }
                .padding(4.dp) // Gap between outer ring and button
                .border(BorderStroke(2.dp, Color.White), CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color.White)
        )

        if (uiState.error != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 140.dp)
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    ScanScreenUI(
        uiState = ScanUiState(),
        previewView = null,
        selectedType = DocumentType.SURAT,
        onCaptureClick = {},
        onTypeChange = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PermissionDeniedPreview() {
    PermissionDeniedContent(onOpenSettings = {})
}
