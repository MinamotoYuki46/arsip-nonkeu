package com.bpkpad.arsipnonkeu.util

import android.content.Context
import android.net.Uri
import android.util.Xml
import com.bpkpad.arsipnonkeu.domain.model.ArchiveDocumentListItem
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingDocument
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingDocumentSource
import org.dhatim.fastexcel.Workbook
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.util.UUID
import java.util.zip.ZipInputStream

object ArchiveExcelService {

    private val headers = listOf(
        "Jenis Dokumen",
        "Nomor Dokumen",
        "Kode Klasifikasi",
        "Judul",
        "Deskripsi",
        "Tahun",
        "Bentuk Fisik",
        "Kondisi",
        "Jumlah Salinan",
        "Keaslian",
        "Status",
        "Asal Instansi",
        "Ruangan",
        "Rak",
        "Boks"
    )

    fun exportArchiveDocuments(
        context: Context,
        uri: Uri,
        documents: List<ArchiveDocumentListItem>
    ) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val workbook = Workbook(outputStream, "ArsipNonKeu", "1.0")
            val sheet = workbook.newWorksheet("Arsip")

            // Headers
            headers.forEachIndexed { index, header ->
                sheet.value(0, index, header)
            }

            // Data
            documents.forEachIndexed { rowIndex, item ->
                val row = rowIndex + 1
                val doc = item.document
                val loc = item.storageLocation

                sheet.value(row, 0, doc.documentType.label)
                sheet.value(row, 1, doc.documentNumber.orEmpty())
                sheet.value(row, 2, doc.classificationCode.orEmpty())
                sheet.value(row, 3, doc.title)
                sheet.value(row, 4, doc.description.orEmpty())
                sheet.value(row, 5, doc.year.toDouble()) // numeric in excel
                sheet.value(row, 6, doc.physicalForm.label)
                sheet.value(row, 7, doc.condition?.label ?: "Tidak diketahui")
                sheet.value(row, 8, doc.copyCount.toDouble()) // numeric in excel
                sheet.value(row, 9, when (doc.isCopy) {
                    true -> "Kopi"
                    false -> "Asli"
                    null -> "Tidak diketahui"
                })
                sheet.value(row, 10, doc.status.label)
                sheet.value(row, 11, doc.originInstance.orEmpty())
                sheet.value(row, 12, loc?.room.orEmpty())
                sheet.value(row, 13, loc?.shelf.orEmpty())
                sheet.value(row, 14, loc?.boxNumber.orEmpty())
            }

            workbook.finish()
        }
    }

    fun importStagingDocuments(
        context: Context,
        uri: Uri,
        expectedYear: Int
    ): List<StagingDocument> {
        val bytes = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        } ?: return emptyList()

        return try {
            readStagingDocumentsFromXlsx(bytes, expectedYear)
        } catch (throwable: Throwable) {
            throw IllegalStateException(
                throwable.message ?: "Gagal membaca file Excel. Pastikan file berformat .xlsx dan mengikuti template kolom arsip.",
                throwable
            )
        }
    }

    private fun readStagingDocumentsFromXlsx(
        xlsxBytes: ByteArray,
        expectedYear: Int
    ): List<StagingDocument> {
        val sharedStrings = readSharedStrings(xlsxBytes)
        val sheetXml = readZipEntry(
            zipBytes = xlsxBytes,
            entryName = "xl/worksheets/sheet1.xml"
        ) ?: return emptyList()

        val rows = readSheetRows(
            sheetXml = sheetXml,
            sharedStrings = sharedStrings
        )

        val importedDocs = mutableListOf<StagingDocument>()
        val invalidYearRows = mutableListOf<Int>()

        rows.drop(1).forEachIndexed { index, row ->
            val rowIndex = index + 2 // 1-based, +1 for header
            val title = row[3].orEmpty()

            val isEmptyRow = listOf(
                row[0].orEmpty(),
                row[1].orEmpty(),
                row[2].orEmpty(),
                title,
                row[5].orEmpty()
            ).all { it.isBlank() }

            if (!isEmptyRow) {
                val rowYear = parseIntCell(row[5].orEmpty(), -1)
                
                if (rowYear != expectedYear) {
                    invalidYearRows.add(rowIndex)
                } else {
                    importedDocs.add(
                        StagingDocument(
                            id = UUID.randomUUID().toString(),
                            documentType = parseDocumentType(row[0].orEmpty()),
                            documentNumber = row[1].orEmpty().takeIf { it.isNotBlank() },
                            classificationCode = row[2].orEmpty().takeIf { it.isNotBlank() },
                            title = title.ifBlank { "Dokumen Tanpa Judul" },
                            description = row[4].orEmpty().takeIf { it.isNotBlank() },
                            year = rowYear,
                            physicalForm = parsePhysicalForm(row[6].orEmpty()),
                            condition = parseDocumentCondition(row[7].orEmpty()),
                            copyCount = parseIntCell(row[8].orEmpty(), 1).coerceAtLeast(1),
                            isCopy = parseIsCopy(row[9].orEmpty()),
                            status = parseDocumentStatus(row[10].orEmpty()),
                            originInstance = row[11].orEmpty().takeIf { it.isNotBlank() },
                            source = StagingDocumentSource.IMPORT
                        )
                    )
                }
            }
        }

        if (invalidYearRows.isNotEmpty()) {
            throw IllegalArgumentException(
                "Ditemukan data dengan tahun yang tidak sesuai ($expectedYear) pada baris: ${invalidYearRows.joinToString(", ")}. Mohon periksa kembali file Excel Anda."
            )
        }

        return importedDocs
    }

    private fun readSharedStrings(
        xlsxBytes: ByteArray
    ): List<String> {
        val sharedStringsXml = readZipEntry(
            zipBytes = xlsxBytes,
            entryName = "xl/sharedStrings.xml"
        ) ?: return emptyList()

        val result = mutableListOf<String>()
        val parser = Xml.newPullParser()

        parser.setInput(ByteArrayInputStream(sharedStringsXml), null)

        var eventType = parser.eventType
        var currentText: StringBuilder? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "si" -> {
                            currentText = StringBuilder()
                        }

                        "t" -> {
                            val text = parser.nextText()
                            currentText?.append(text)
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (parser.name == "si") {
                        result.add(currentText?.toString().orEmpty())
                        currentText = null
                    }
                }
            }

            eventType = parser.next()
        }

        return result
    }

    private fun readSheetRows(
        sheetXml: ByteArray,
        sharedStrings: List<String>
    ): List<Map<Int, String>> {
        val result = mutableListOf<Map<Int, String>>()
        val parser = Xml.newPullParser()

        parser.setInput(ByteArrayInputStream(sheetXml), null)

        var eventType = parser.eventType

        var currentRow: MutableMap<Int, String>? = null
        var currentCellColumnIndex: Int? = null
        var currentCellType: String? = null
        var insideCell = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> {
                            currentRow = mutableMapOf()
                        }

                        "c" -> {
                            insideCell = true

                            val cellReference = parser.getAttributeValue(null, "r").orEmpty()
                            currentCellColumnIndex = parseColumnIndexFromCellReference(cellReference)
                            currentCellType = parser.getAttributeValue(null, "t")
                        }

                        "v" -> {
                            if (insideCell) {
                                val rawValue = parser.nextText()
                                val value = decodeCellValue(
                                    rawValue = rawValue,
                                    cellType = currentCellType,
                                    sharedStrings = sharedStrings
                                )

                                val columnIndex = currentCellColumnIndex
                                if (columnIndex != null) {
                                    currentRow?.set(columnIndex, value)
                                }
                            }
                        }

                        "t" -> {
                            if (insideCell && currentCellType == "inlineStr") {
                                val value = parser.nextText()
                                val columnIndex = currentCellColumnIndex
                                if (columnIndex != null) {
                                    currentRow?.set(columnIndex, value)
                                }
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "c" -> {
                            insideCell = false
                            currentCellColumnIndex = null
                            currentCellType = null
                        }

                        "row" -> {
                            val row = currentRow
                            if (row != null) {
                                result.add(row)
                            }
                            currentRow = null
                        }
                    }
                }
            }

            eventType = parser.next()
        }

        return result
    }

    private fun decodeCellValue(
        rawValue: String,
        cellType: String?,
        sharedStrings: List<String>
    ): String {
        return when (cellType) {
            "s" -> {
                val index = rawValue.toIntOrNull()
                if (index != null) {
                    sharedStrings.getOrNull(index).orEmpty()
                } else {
                    rawValue
                }
            }

            "inlineStr" -> {
                rawValue
            }

            else -> {
                rawValue
            }
        }.trim()
    }

    private fun parseColumnIndexFromCellReference(
        cellReference: String
    ): Int? {
        val letters = cellReference.takeWhile { it.isLetter() }

        if (letters.isBlank()) return null

        var result = 0

        letters.forEach { char ->
            result = result * 26 + (char.uppercaseChar() - 'A' + 1)
        }

        return result - 1
    }

    private fun readZipEntry(
        zipBytes: ByteArray,
        entryName: String
    ): ByteArray? {
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zipInputStream ->
            while (true) {
                val entry = zipInputStream.nextEntry ?: break

                if (entry.name == entryName) {
                    return zipInputStream.readBytes()
                }
            }
        }

        return null
    }

    private fun parseIntCell(
        value: String,
        defaultValue: Int
    ): Int {
        val normalized = value
            .trim()
            .replace(",", "")
            .removeSuffix(".0")

        return normalized.toIntOrNull() ?: defaultValue
    }

    private fun parseDocumentType(value: String): DocumentType {
        return DocumentType.values().firstOrNull { type ->
            value.equals(type.name, ignoreCase = true) ||
                    value.equals(type.label, ignoreCase = true)
        } ?: DocumentType.SURAT
    }

    private fun parsePhysicalForm(value: String): PhysicalForm {
        return PhysicalForm.values().firstOrNull { form ->
            value.equals(form.name, ignoreCase = true) ||
                    value.equals(form.label, ignoreCase = true)
        } ?: PhysicalForm.SHEET
    }

    private fun parseDocumentCondition(value: String): DocumentCondition? {
        if (value.isBlank()) return null

        return DocumentCondition.values().firstOrNull { condition ->
            value.equals(condition.name, ignoreCase = true) ||
                    value.equals(condition.label, ignoreCase = true)
        }
    }

    private fun parseDocumentStatus(value: String): DocumentStatus {
        return DocumentStatus.values().firstOrNull { status ->
            value.equals(status.name, ignoreCase = true) ||
                    value.equals(status.label, ignoreCase = true)
        } ?: DocumentStatus.AVAILABLE
    }

    private fun parseIsCopy(value: String): Boolean? {
        if (value.isBlank() || value.contains("diketahui", ignoreCase = true)) return null

        return when {
            value.equals("Kopi", ignoreCase = true) -> true
            value.equals("Asli", ignoreCase = true) -> false
            else -> null
        }
    }
}