package com.bpkpad.arsip.domain.usecase

import com.bpkpad.arsip.core.domain.model.DocumentType
import com.bpkpad.arsip.data.remote.GeminiClient
import com.bpkpad.arsip.domain.model.ArchiveDocument
import com.bpkpad.arsip.utils.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class ParseMetadataWithAiUseCase @Inject constructor(
    private val geminiClient: GeminiClient
) {
    operator fun invoke(rawText: String, type: DocumentType = DocumentType.SURAT): Flow<ResultState<ArchiveDocument>> = flow {
        emit(ResultState.Loading)
        
        val prompt = createPrompt(rawText, type)
        val response = geminiClient.generateContent(prompt)

        if (response != null) {
            try {
                val json = Json.parseToJsonElement(response).jsonObject
                val title = json["title"]?.jsonPrimitive?.content ?: ""
                val description = json["description"]?.jsonPrimitive?.content ?: ""
                
                emit(ResultState.Success(
                    ArchiveDocument(
                        title = title,
                        type = type.name,
                        description = description,
                        date = System.currentTimeMillis()
                    )
                ))
            } catch (e: Exception) {
                emit(ResultState.Error("Gagal mengurai respon AI: ${e.message}"))
            }
        } else {
            emit(ResultState.Error("Tidak ada respon dari Gemini"))
        }
    }

    private fun createPrompt(rawText: String, type: DocumentType): String {
        val typeInstruction = when (type) {
            DocumentType.SURAT -> "Ini adalah Surat Dinas. Ekstrak nomor surat, perihal, dan pengirim."
            DocumentType.PERDA -> "Ini adalah Peraturan Daerah. Ekstrak nomor peraturan, tahun, dan tentang apa peraturan tersebut."
            DocumentType.PERBUP -> "Ini adalah Peraturan Bupati. Ekstrak nomor peraturan, tahun, dan tentang apa peraturan tersebut."
            DocumentType.KEPUTUSAN_BUPATI -> "Ini adalah Keputusan Bupati. Ekstrak nomor keputusan, tahun, dan perihal keputusan."
            DocumentType.KEPUTUSAN_GUBERNUR -> "Ini adalah Keputusan Gubernur. Ekstrak nomor keputusan, tahun, dan perihal keputusan."
        }

        return """
            Tugas: Anda adalah asisten pengarsipan pemerintah yang ahli. 
            Instruksi: Analisis teks mentah hasil OCR di bawah ini dan ekstrak informasinya.
            $typeInstruction
            
            Format Output: Kembalikan HANYA dalam format JSON dengan kunci berikut:
            - "title": Judul ringkas dokumen (Contoh: "SK - 001/2023 - Pengangkatan Pegawai")
            - "description": Ringkasan singkat isi dokumen (maksimal 2 kalimat).
            
            Teks OCR:
            $rawText
        """.trimIndent()
    }
}
