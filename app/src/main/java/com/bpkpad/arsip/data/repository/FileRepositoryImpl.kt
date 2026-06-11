package com.bpkpad.arsip.data.repository

import com.bpkpad.arsip.domain.repository.FileRepository
import com.bpkpad.arsip.utils.ResultState
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : FileRepository {

    override fun uploadImage(imageBytes: ByteArray): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val fileName = "archives/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(fileName)
            val uploadTask = ref.putBytes(imageBytes).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            emit(ResultState.Success(downloadUrl))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Upload failed", e))
        }
    }

    override fun extractTextFromImage(imageBytes: ByteArray): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)
        try {
            // Need to convert ByteArray to Bitmap or direct InputImage if possible
            // For now, assuming we can create InputImage from a Bitmap created from bytes
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            if (bitmap == null) {
                trySend(ResultState.Error("Failed to decode image"))
                close()
                return@callbackFlow
            }
            
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    trySend(ResultState.Success(visionText.text))
                    close()
                }
                .addOnFailureListener { e ->
                    trySend(ResultState.Error(e.message ?: "OCR Failed", e))
                    close()
                }
        } catch (e: Exception) {
            trySend(ResultState.Error(e.message ?: "Unknown OCR Error", e))
            close()
        }
        awaitClose { /* Cleanup if needed */ }
    }
}
