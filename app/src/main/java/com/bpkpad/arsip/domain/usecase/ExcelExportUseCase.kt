package com.bpkpad.arsip.domain.usecase

import com.bpkpad.arsip.domain.model.ArchiveDocument
import com.bpkpad.arsip.utils.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ExcelExportUseCase @Inject constructor() {
    operator fun invoke(documents: List<ArchiveDocument>, filePath: String): Flow<ResultState<File>> = flow {
        emit(ResultState.Loading)
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Archives")
            
            // Header Row
            val headerRow = sheet.createRow(0)
            val columns = listOf("ID", "Title", "Type", "Date", "Description", "Box ID", "Location ID")
            columns.forEachIndexed { index, column ->
                headerRow.createCell(index).setCellValue(column)
            }
            
            // Data Rows
            documents.forEachIndexed { index, doc ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(doc.id)
                row.createCell(1).setCellValue(doc.title)
                row.createCell(2).setCellValue(doc.type)
                row.createCell(3).setCellValue(doc.date.toString())
                row.createCell(4).setCellValue(doc.description)
                row.createCell(5).setCellValue(doc.boxId)
                row.createCell(6).setCellValue(doc.locationId)
            }
            
            val file = File(filePath)
            val out = FileOutputStream(file)
            workbook.write(out)
            out.close()
            workbook.close()
            
            emit(ResultState.Success(file))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Failed to export Excel", e))
        }
    }.flowOn(Dispatchers.IO)
}
