package com.bpkpad.arsipnonkeu.util

import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import java.time.LocalDate

object OcrPromptFactory {

    fun buildPrompt(
        documentType: DocumentType,
        rawOcrText: String
    ): String {
        val currentYear = LocalDate.now().year

        return """
            Kamu adalah sistem ekstraksi metadata arsip dokumen pemerintahan Indonesia.

            Tugas:
            Ekstrak metadata dokumen dari teks OCR mentah.
            Jenis dokumen yang dipilih user adalah: ${documentType.label}.
            Gunakan jenis dokumen tersebut sebagai konteks utama.

            Output WAJIB berupa JSON valid saja.
            Jangan gunakan markdown.
            Jangan tulis penjelasan.
            Jangan tulis ```json.

            Schema JSON:
            {
              "documentType": "SURAT|PERDA|PERKAB|KEPBUP|KEPGUB",
              "documentNumber": string|null,
              "classificationCode": string|null,
              "title": string,
              "description": string|null,
              "year": number,
              "physicalForm": "SHEET|BOOK",
              "condition": "GOOD|DAMAGED|null",
              "copyCount": number,
              "isCopy": boolean|null,
              "status": "AVAILABLE|BORROWED|DISPOSED",
              "originInstance": string|null
            }

            Aturan:
            - documentType harus salah satu enum di atas.
            - Jika OCR tidak jelas, pakai jenis dokumen pilihan user.
            - title harus ringkas dan representatif.
            - year ambil dari tanggal, nomor, atau isi dokumen jika ada.
            - Jika tahun tidak ditemukan, gunakan $currentYear.
            - physicalForm default SHEET untuk surat.
            - physicalForm default BOOK untuk perda/perkab/kepbup/kepgub.
            - isCopy: true jika ada indikasi salinan/copy/tembusan/legalisir, false jika asli, null jika ragu.
            - condition default null.
            - copyCount default 1.
            - status default AVAILABLE.
            - originInstance isi jika ada instansi/bagian yang jelas.
            - Jangan mengarang nomor dokumen jika tidak ada.
            - description boleh berisi ringkasan singkat isi OCR.

            Teks OCR mentah:
            $rawOcrText
        """.trimIndent()
    }
}