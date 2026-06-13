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
        "Lokasi"
    )

    fun exportArchiveDocuments(
        context: Context,
        uri: Uri,
        documents: List<ArchiveDocumentListItem>
    ) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val workbook = Workbook(outputStream, "ArsipNonKeu", "1.0")
            val sheet = workbook.newWorksheet("Arsip")

            headers.forEachIndexed { index, header ->
                sheet.value(0, index, header)
            }

            documents.forEachIndexed { rowIndex, item ->
                val row = rowIndex + 1
                val document = item.document

                sheet.value(row, 0, document.documentType.label)
                sheet.value(row, 1, document.documentNumber.orEmpty())
                sheet.value(row, 2, document.classificationCode.orEmpty())
                sheet.value(row, 3, document.title)
                sheet.value(row, 4, document.description.orEmpty())
                sheet.value(row, 5, document.year)
                sheet.value(row, 6, document.physicalForm.label)
                sheet.value(row, 7, document.condition?.label.orEmpty())
                sheet.value(row, 8, document.copyCount)
                sheet.value(row, 9, when (document.isCopy) {
                    true -> "Kopi"
                    false -> "Asli"
                    null -> "Tidak diketahui"
                })
                sheet.value(row, 10, document.status.label)
                sheet.value(row, 11, document.originInstance.orEmpty())
                sheet.value(row, 12, item.locationLabel)
            }

            workbook.finish()
        }
    }

    fun importStagingDocuments(
        context: Context,
        uri: Uri
    ): List<StagingDocument> {
        val bytes = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        } ?: return emptyList()

        return try {
            readStagingDocumentsFromXlsx(bytes)
        } catch (throwable: Throwable) {
            throw IllegalStateException(
                "Gagal membaca file Excel. Pastikan file berformat .xlsx dan mengikuti template kolom arsip.",
                throwable
            )
        }
    }

    private fun readStagingDocumentsFromXlsx(
        xlsxBytes: ByteArray
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

        return rows
            .drop(1) // skip header
            .mapNotNull { row ->
                val title = row[3].orEmpty()

                val isEmptyRow = listOf(
                    row[0].orEmpty(),
                    row[1].orEmpty(),
                    row[2].orEmpty(),
                    title,
                    row[5].orEmpty()
                ).all { it.isBlank() }

                if (isEmptyRow) {
                    null
                } else {
                    StagingDocument(
                        id = UUID.randomUUID().toString(),
                        documentType = parseDocumentType(row[0].orEmpty()),
                        documentNumber = row[1].orEmpty().takeIf { it.isNotBlank() },
                        classificationCode = row[2].orEmpty().takeIf { it.isNotBlank() },
                        title = title.ifBlank { "Dokumen Tanpa Judul" },
                        description = row[4].orEmpty().takeIf { it.isNotBlank() },
                        year = parseIntCell(row[5].orEmpty(), 2025),
                        physicalForm = parsePhysicalForm(row[6].orEmpty()),
                        condition = parseDocumentCondition(row[7].orEmpty()),
                        copyCount = parseIntCell(row[8].orEmpty(), 1).coerceAtLeast(1),
                        isCopy = parseIsCopy(row[9].orEmpty()),
                        status = parseDocumentStatus(row[10].orEmpty()),
                        originInstance = row[11].orEmpty().takeIf { it.isNotBlank() },
                        source = StagingDocumentSource.IMPORT
                    )
                }
            }
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