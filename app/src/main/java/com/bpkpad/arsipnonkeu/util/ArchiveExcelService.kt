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
import java.io.File
import java.util.UUID
import java.util.zip.ZipFile

object ArchiveExcelService {

    private const val MAX_TITLE_LENGTH = 255

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

    private data class SheetReadResult(
        val rows: List<Map<Int, String>>,
        val formulaCells: List<String>
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
                val doc = item.document
                val loc = item.storageLocation

                sheet.value(row, 0, doc.documentType.label)
                sheet.value(row, 1, doc.documentNumber.orEmpty())
                sheet.value(row, 2, doc.classificationCode.orEmpty())
                sheet.value(row, 3, doc.title)
                sheet.value(row, 4, doc.description.orEmpty())
                sheet.value(row, 5, doc.year.toDouble())
                sheet.value(row, 6, doc.physicalForm.label)
                sheet.value(row, 7, doc.condition?.label ?: "Tidak diketahui")
                sheet.value(row, 8, doc.copyCount.toDouble())

                sheet.value(
                    row,
                    9,
                    when (doc.isCopy) {
                        true -> "Kopi"
                        false -> "Asli"
                        null -> "Tidak diketahui"
                    }
                )

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
        val tempFile = copyUriToCacheFile(context, uri)

        return try {
            validateXlsxFile(tempFile)
            readStagingDocumentsFromXlsx(tempFile, expectedYear)
        } catch (exception: IllegalArgumentException) {
            throw exception
        } catch (throwable: Throwable) {
            throw IllegalStateException(
                "Gagal membaca file Excel. Pastikan file berformat .xlsx dan mengikuti template kolom arsip.",
                throwable
            )
        } finally {
            tempFile.delete()
        }
    }

    private fun copyUriToCacheFile(
        context: Context,
        uri: Uri
    ): File {
        val file = File(
            context.cacheDir,
            "arsip_import_${System.currentTimeMillis()}.xlsx"
        )

        context.contentResolver.openInputStream(uri).use { inputStream ->
            requireNotNull(inputStream) {
                "Tidak bisa membuka file Excel."
            }

            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return file
    }

    private fun validateXlsxFile(file: File) {
        if (!file.exists() || file.length() <= 0L) {
            throw IllegalArgumentException("File Excel kosong atau tidak ditemukan.")
        }

        val header = ByteArray(4)
        val readBytes = file.inputStream().use { inputStream ->
            inputStream.read(header)
        }

        val isZipFile = readBytes >= 4 &&
                header[0] == 'P'.code.toByte() &&
                header[1] == 'K'.code.toByte()

        if (!isZipFile) {
            throw IllegalArgumentException(
                "File tidak valid. Pastikan file berformat .xlsx, bukan .xls, .csv, atau file yang hanya diganti ekstensinya."
            )
        }
    }

    private fun readStagingDocumentsFromXlsx(
        xlsxFile: File,
        expectedYear: Int
    ): List<StagingDocument> {
        val sharedStrings = readSharedStrings(xlsxFile)

        val sheetXml = readZipEntry(
            xlsxFile = xlsxFile,
            entryName = "xl/worksheets/sheet1.xml"
        ) ?: throw IllegalArgumentException(
            "Sheet utama tidak ditemukan. Pastikan file Excel mengikuti template ekspor aplikasi."
        )

        val sheetReadResult = readSheetRows(
            sheetXml = sheetXml,
            sharedStrings = sharedStrings
        )

        validateNoFormulaCells(sheetReadResult.formulaCells)

        val rows = sheetReadResult.rows

        validateHeaders(rows.firstOrNull())

        val importedDocs = mutableListOf<StagingDocument>()
        val errors = mutableListOf<String>()

        rows.drop(1).forEachIndexed { index, row ->
            val rowIndex = index + 2
            val isRowEmpty = row.values.all { it.isBlank() }

            if (isRowEmpty) return@forEachIndexed

            val title = row[3].orEmpty().trim()
            val rowYearStr = row[5].orEmpty().trim()
            val rowYear = parseIntCell(rowYearStr, -1)

            val rowErrors = mutableListOf<String>()

            if (title.isBlank()) {
                rowErrors.add("Judul wajib diisi")
            } else if (title.length > MAX_TITLE_LENGTH) {
                rowErrors.add("Judul maksimal $MAX_TITLE_LENGTH karakter")
            }

            if (rowYear == -1) {
                if (rowYearStr.isBlank()) {
                    rowErrors.add("Tahun wajib diisi")
                } else {
                    rowErrors.add("Format tahun tidak valid ('$rowYearStr')")
                }
            } else if (rowYear != expectedYear) {
                rowErrors.add("Tahun ($rowYear) tidak sesuai dengan tahun aktif ($expectedYear)")
            }

            if (rowErrors.isNotEmpty()) {
                errors.add("Baris $rowIndex: ${rowErrors.joinToString(", ")}")
            } else {
                importedDocs.add(
                    StagingDocument(
                        id = UUID.randomUUID().toString(),
                        documentType = parseDocumentType(row[0].orEmpty()),
                        documentNumber = row[1].orEmpty().trim().takeIf { it.isNotBlank() },
                        classificationCode = row[2].orEmpty().trim().takeIf { it.isNotBlank() },
                        title = title,
                        description = row[4].orEmpty().trim().takeIf { it.isNotBlank() },
                        year = rowYear,
                        physicalForm = parsePhysicalForm(row[6].orEmpty()),
                        condition = parseDocumentCondition(row[7].orEmpty()),
                        copyCount = parseIntCell(row[8].orEmpty(), 1).coerceAtLeast(1),
                        isCopy = parseIsCopy(row[9].orEmpty()),
                        status = parseDocumentStatus(row[10].orEmpty()),
                        originInstance = row[11].orEmpty().trim().takeIf { it.isNotBlank() },
                        source = StagingDocumentSource.IMPORT
                    )
                )
            }
        }

        if (errors.isNotEmpty()) {
            val errorSummary = if (errors.size > 10) {
                errors.take(10).joinToString("\n") +
                        "\n...dan ${errors.size - 10} kesalahan lainnya."
            } else {
                errors.joinToString("\n")
            }

            throw IllegalArgumentException(
                "Ditemukan kesalahan pada file Excel:\n$errorSummary"
            )
        }

        return importedDocs
    }

    private fun validateNoFormulaCells(
        formulaCells: List<String>
    ) {
        val distinctFormulaCells = formulaCells
            .filter { it.isNotBlank() }
            .distinct()

        if (distinctFormulaCells.isEmpty()) return

        val shownCells = distinctFormulaCells
            .take(10)
            .joinToString(", ")

        val extraCount = distinctFormulaCells.size - 10

        val suffix = if (extraCount > 0) {
            ", dan $extraCount sel lainnya"
        } else {
            ""
        }

        throw IllegalArgumentException(
            "File Excel tidak boleh mengandung formula. " +
                    "Hapus formula pada sel: $shownCells$suffix, lalu import ulang."
        )
    }

    private fun validateHeaders(
        headerRow: Map<Int, String>?
    ) {
        if (headerRow == null) {
            throw IllegalArgumentException(
                "Template Excel tidak valid. Baris header tidak ditemukan."
            )
        }

        val mismatches = headers.mapIndexedNotNull { index, expectedHeader ->
            val actualHeader = headerRow[index].orEmpty().trim()

            if (!actualHeader.equals(expectedHeader, ignoreCase = true)) {
                "Kolom ${index + 1}: seharusnya '$expectedHeader', terbaca '${actualHeader.ifBlank { "(kosong)" }}'"
            } else {
                null
            }
        }

        if (mismatches.isNotEmpty()) {
            throw IllegalArgumentException(
                "Template Excel tidak sesuai:\n${mismatches.joinToString("\n")}"
            )
        }
    }

    private fun readSharedStrings(
        xlsxFile: File
    ): List<String> {
        val sharedStringsXml = readZipEntry(
            xlsxFile = xlsxFile,
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
    ): SheetReadResult {
        val rows = mutableListOf<Map<Int, String>>()
        val formulaCells = mutableListOf<String>()

        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(sheetXml), null)

        var eventType = parser.eventType

        var currentRow: MutableMap<Int, String>? = null
        var currentCellColumnIndex: Int? = null
        var currentCellType: String? = null
        var currentCellReference: String? = null
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

                            currentCellReference = cellReference.ifBlank {
                                "sel tidak diketahui"
                            }

                            currentCellColumnIndex = parseColumnIndexFromCellReference(cellReference)
                            currentCellType = parser.getAttributeValue(null, "t")
                        }

                        "f" -> {
                            if (insideCell) {
                                formulaCells.add(
                                    currentCellReference ?: "sel tidak diketahui"
                                )

                                try {
                                    parser.nextText()
                                } catch (_: Throwable) {
                                    // Formula tetap dianggap terdeteksi.
                                    // Isi tag formula tidak perlu dibaca untuk proses import.
                                }
                            }
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
                                    currentRow?.set(columnIndex, value.trim())
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
                            currentCellReference = null
                        }

                        "row" -> {
                            val row = currentRow

                            if (row != null) {
                                rows.add(row)
                            }

                            currentRow = null
                        }
                    }
                }
            }

            eventType = parser.next()
        }

        return SheetReadResult(
            rows = rows,
            formulaCells = formulaCells
        )
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

            "inlineStr" -> rawValue

            else -> rawValue
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
        xlsxFile: File,
        entryName: String
    ): ByteArray? {
        ZipFile(xlsxFile).use { zipFile ->
            val entry = zipFile.getEntry(entryName) ?: return null

            return zipFile.getInputStream(entry).use { inputStream ->
                inputStream.readBytes()
            }
        }
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

    private fun parseDocumentType(
        value: String
    ): DocumentType {
        return DocumentType.values().firstOrNull { type ->
            value.equals(type.name, ignoreCase = true) ||
                    value.equals(type.label, ignoreCase = true)
        } ?: DocumentType.SURAT
    }

    private fun parsePhysicalForm(
        value: String
    ): PhysicalForm {
        return PhysicalForm.values().firstOrNull { form ->
            value.equals(form.name, ignoreCase = true) ||
                    value.equals(form.label, ignoreCase = true)
        } ?: PhysicalForm.SHEET
    }

    private fun parseDocumentCondition(
        value: String
    ): DocumentCondition? {
        if (value.isBlank()) return null

        return DocumentCondition.values().firstOrNull { condition ->
            value.equals(condition.name, ignoreCase = true) ||
                    value.equals(condition.label, ignoreCase = true)
        }
    }

    private fun parseDocumentStatus(
        value: String
    ): DocumentStatus {
        return DocumentStatus.values().firstOrNull { status ->
            value.equals(status.name, ignoreCase = true) ||
                    value.equals(status.label, ignoreCase = true)
        } ?: DocumentStatus.AVAILABLE
    }

    private fun parseIsCopy(
        value: String
    ): Boolean? {
        if (value.isBlank() || value.contains("diketahui", ignoreCase = true)) {
            return null
        }

        return when {
            value.equals("Kopi", ignoreCase = true) -> true
            value.equals("Asli", ignoreCase = true) -> false
            else -> null
        }
    }
}