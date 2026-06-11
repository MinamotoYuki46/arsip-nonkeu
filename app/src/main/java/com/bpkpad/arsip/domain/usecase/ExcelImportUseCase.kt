package com.bpkpad.arsip.domain.usecase

import android.content.Context
import android.net.Uri
import com.bpkpad.arsip.core.domain.model.DocumentType
import com.bpkpad.arsip.core.domain.model.StagingDocument
import com.bpkpad.arsip.core.domain.model.StagingStatus
import com.bpkpad.arsip.core.domain.repository.StagingRepository
import com.bpkpad.arsip.domain.model.ArchiveDocument
import com.bpkpad.arsip.domain.repository.ArchiveRepository
import com.bpkpad.arsip.utils.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.util.UUID
import javax.inject.Inject

class ExcelImportUseCase @Inject constructor(
    private val stagingRepository: StagingRepository
) {
    operator fun invoke(context: Context, uri: Uri): Flow<ResultState<Int>> = flow {
        emit(ResultState.Loading)
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: throw Exception("Failed to open file")

            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)
            val importedDocs = mutableListOf<StagingDocument>()

            // Skip header row
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                
                val title = row.getCell(0)?.toString() ?: ""
                val typeStr = row.getCell(1)?.toString() ?: "SURAT"
                val type = try { DocumentType.valueOf(typeStr) } catch(e: Exception) { DocumentType.SURAT }
                val year = row.getCell(2)?.toString()?.toDoubleOrNull()?.toInt() ?: 2024
                val condition = row.getCell(3)?.toString() ?: "BAIK"
                val description = row.getCell(4)?.toString() ?: ""

                if (title.isNotEmpty()) {
                    importedDocs.add(
                        StagingDocument(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            type = type,
                            year = year,
                            metadata = mapOf("condition" to condition, "description" to description),
                            coverLocalPath = null,
                            status = StagingStatus.LOCAL_ONLY
                        )
                    )
                }
            }
            workbook.close()
            inputStream.close()

            importedDocs.forEach { doc ->
                stagingRepository.saveToStaging(doc)
            }

            emit(ResultState.Success(importedDocs.size))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Failed to import Excel"))
        }
    }.flowOn(Dispatchers.IO)
}
