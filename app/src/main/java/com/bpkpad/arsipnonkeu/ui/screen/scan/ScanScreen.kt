package com.bpkpad.arsipnonkeu.ui.screen.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.bpkpad.arsipnonkeu.R
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.ui.component.LoadingIndicator
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingViewModel
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val PoppinsFont = FontFamily.Default

@Composable
fun ScanScreen(
    onBackClick: () -> Unit = {},
    onScanCompleted: () -> Unit = {},
    viewModel: ScanViewModel = remember { ScanViewModel() },
    stagingViewModel: StagingViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        ScanCameraContent(
            uiState = uiState,
            selectedDocumentType = uiState.selectedDocumentType,
            onDocumentTypeChange = viewModel::onDocumentTypeChange,
            onBackClick = onBackClick,
            onImageCaptured = { imageBytes ->
                viewModel.processCapturedImage(
                    imageBytes = imageBytes,
                    onParsed = { parsedDocument ->
                        stagingViewModel.addScannedParsedDocument(parsedDocument)
                        onScanCompleted()
                    }
                )
            },
            onDismissError = viewModel::clearMessage
        )
    } else {
        CameraPermissionDeniedContent(
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
            onBackClick = onBackClick
        )
    }
}

@Composable
private fun ScanCameraContent(
    uiState: ScanUiState,
    selectedDocumentType: DocumentType,
    onDocumentTypeChange: (DocumentType) -> Unit,
    onBackClick: () -> Unit,
    onImageCaptured: (ByteArray) -> Unit,
    onDismissError: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isPreviewMode = LocalInspectionMode.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) { onDispose { cameraExecutor.shutdown() } }

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val imageCapture = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    }

    if (!isPreviewMode) {
        val cameraErrorMsg = stringResource(R.string.scan_error_capture)
        DisposableEffect(Unit) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val listener = Runnable {
                val cameraProvider = cameraProviderFuture.get()
                val preview = androidx.camera.core.Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                } catch (exception: Exception) {
                    Log.e("ScanScreen", "Camera binding failed", exception)
                    Toast.makeText(context, exception.message ?: cameraErrorMsg, Toast.LENGTH_LONG).show()
                }
            }
            cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))
            onDispose { try { cameraProviderFuture.get().unbindAll() } catch (_: Exception) {} }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isPreviewMode) {
            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
        } else {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        }

        ScanOverlayTopBar(selectedDocumentType = selectedDocumentType, onDocumentTypeChange = onDocumentTypeChange, onBackClick = onBackClick)
        ScanGuideFrame(modifier = Modifier.align(Alignment.Center))

        if (uiState.isProcessing) {
            ProcessingOverlay(modifier = Modifier.align(Alignment.Center))
        }

        val errorReadMsg = stringResource(R.string.scan_error_read)
        val errorCaptureMsg = stringResource(R.string.scan_error_capture)

        CaptureButton(
            enabled = !uiState.isProcessing,
            onCaptureClick = {
                capturePhotoToJpegBytes(
                    context = context,
                    imageCapture = imageCapture,
                    cameraExecutor = cameraExecutor,
                    onSuccess = onImageCaptured,
                    onError = { message ->
                        val finalMsg = if (message.contains("file")) errorReadMsg else errorCaptureMsg
                        Toast.makeText(context, finalMsg, Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp)
        )

        uiState.errorMessage?.let { message ->
            ErrorMessageBox(message = message, onDismiss = onDismissError, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 150.dp).padding(horizontal = 24.dp))
        }
    }
}

@Composable
private fun ScanOverlayTopBar(selectedDocumentType: DocumentType, onDocumentTypeChange: (DocumentType) -> Unit, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.45f)).padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = stringResource(R.string.scan_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = PoppinsFont, color = Color.White)
                Text(text = stringResource(R.string.scan_subtitle), fontSize = 12.sp, fontFamily = PoppinsFont, color = Color.White.copy(alpha = 0.85f))
            }
            Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)).clickable(onClick = onBackClick), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.staging_error_dismiss), tint = Color.White)
            }
        }
        DocumentTypeScanSelector(selectedType = selectedDocumentType, onSelected = onDocumentTypeChange)
    }
}

@Composable
private fun DocumentTypeScanSelector(selectedType: DocumentType, onSelected: (DocumentType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.scan_prompt_label), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = PoppinsFont, color = Color.White)
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DocumentType.entries.forEach { type ->
                val isSelected = selectedType == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (isSelected) Color(0xFF2E7D32) else Color.White.copy(alpha = 0.18f))
                        .border(width = 1.dp, color = if (isSelected) Color(0xFFCBFFC2) else Color.White.copy(alpha = 0.5f), shape = RoundedCornerShape(9999.dp))
                        .clickable { onSelected(type) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(text = type.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = PoppinsFont, color = if (isSelected) Color(0xFFCBFFC2) else Color.White)
                }
            }
        }
    }
}

@Composable
private fun ScanGuideFrame(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(0.84f).height(420.dp).border(width = 2.dp, color = Color.White.copy(alpha = 0.85f), shape = RoundedCornerShape(20.dp))) {
        Text(
            text = stringResource(R.string.scan_guide_text),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp).background(color = Color.Black.copy(alpha = 0.42f), shape = RoundedCornerShape(9999.dp)).padding(horizontal = 14.dp, vertical = 6.dp),
            fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = PoppinsFont, color = Color.White
        )
    }
}

@Composable
private fun CaptureButton(enabled: Boolean, onCaptureClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(88.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)).clickable(enabled = enabled, onClick = onCaptureClick).padding(6.dp)
            .border(border = BorderStroke(2.dp, Color.White), shape = CircleShape).padding(6.dp).clip(CircleShape).background(if (enabled) Color.White else Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = stringResource(R.string.staging_scan), tint = Color(0xFF0D631B), modifier = Modifier.size(30.dp))
    }
}

@Composable
private fun ProcessingOverlay(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(24.dp)).background(Color.Black.copy(alpha = 0.68f)).padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LoadingIndicator(color = Color.White)
        Text(text = stringResource(R.string.scan_processing), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = PoppinsFont, color = Color.White)
    }
}

@Composable
private fun ErrorMessageBox(message: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(18.dp)).background(Color(0xFFFEE2E2)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = message, fontSize = 13.sp, fontWeight = FontWeight.Medium, fontFamily = PoppinsFont, color = Color(0xFF991B1B))
            Text(text = stringResource(R.string.staging_error_dismiss), modifier = Modifier.clickable(onClick = onDismiss), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = PoppinsFont, color = Color(0xFF991B1B))
        }
    }
}

@Composable
private fun CameraPermissionDeniedContent(onOpenSettings: () -> Unit, onBackClick: () -> Unit) {
    Scaffold(containerColor = Color(0xFF071E27)) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(32.dp), contentAlignment = Alignment.Center) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.scan_permission_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = PoppinsFont, color = Color.White, textAlign = TextAlign.Center)
                Text(text = stringResource(R.string.scan_permission_message), fontSize = 14.sp, fontFamily = PoppinsFont, color = Color.White.copy(alpha = 0.82f), textAlign = TextAlign.Center)
                Button(onClick = onOpenSettings, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D631B))) { Text(stringResource(R.string.scan_permission_settings)) }
                OutlinedButton(onClick = onBackClick, border = BorderStroke(width = 1.dp, color = Color.White)) { Text(text = stringResource(R.string.detail_back_button), color = Color.White) }
            }
        }
    }
}

private fun capturePhotoToJpegBytes(context: Context, imageCapture: ImageCapture, cameraExecutor: ExecutorService, onSuccess: (ByteArray) -> Unit, onError: (String) -> Unit) {
    val photoFile = File(context.cacheDir, "ocr_capture_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            try { val bytes = photoFile.readBytes(); photoFile.delete(); onSuccess(bytes) }
            catch (exception: Exception) { photoFile.delete(); onError(exception.message ?: "error") }
        }
        override fun onError(exception: ImageCaptureException) { photoFile.delete(); onError(exception.message ?: "error") }
    })
}

@Preview(showBackground = true, showSystemUi = true, device = "spec:width=390dp,height=844dp,dpi=420")
@Composable
private fun ScanCameraContentPreview() {
    ScanPreviewOnly(isProcessing = false)
}

@Composable
private fun ScanPreviewOnly(isProcessing: Boolean) {
    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
        ScanOverlayTopBar(selectedDocumentType = DocumentType.SURAT, onDocumentTypeChange = {}, onBackClick = {})
        ScanGuideFrame(modifier = Modifier.align(Alignment.Center))
        if (isProcessing) ProcessingOverlay(modifier = Modifier.align(Alignment.Center))
        CaptureButton(enabled = !isProcessing, onCaptureClick = {}, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp))
    }
}
