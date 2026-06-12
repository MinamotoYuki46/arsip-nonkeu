package com.bpkpad.arsipnonkeu.util

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

object MlKitOcrService {

    suspend fun recognizeText(
        imageBytes: ByteArray
    ): String {
        val bitmap = BitmapFactory.decodeByteArray(
            imageBytes,
            0,
            imageBytes.size
        ) ?: error("Gagal membaca gambar hasil kamera")

        val image = InputImage.fromBitmap(bitmap, 0)

        val recognizer = TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )

        val result = recognizer.process(image).await()

        return result.text.trim()
    }
}