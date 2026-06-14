package com.bpkpad.arsipnonkeu.data.remote.model

import com.bpkpad.arsipnonkeu.domain.model.*
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingDocument
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StagingDocumentDto(
    @SerialName("id") val id: String? = null,
    @SerialName("document_type") val documentType: String,
    @SerialName("document_number") val documentNumber: String? = null,
    @SerialName("classification_code") val classificationCode: String? = null,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("year") val year: Int,
    @SerialName("physical_form") val physicalForm: String,
    @SerialName("condition") val condition: String,
    @SerialName("copy_count") val copyCount: Int,
    @SerialName("is_copy") val isCopy: Boolean? = null,
    @SerialName("status") val status: String,
    @SerialName("origin_instance") val originInstance: String? = null,
    @SerialName("source") val source: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class YearOnlyDto(
    @SerialName("year") val year: Int
)

@Serializable
data class ArchiveDocumentDto(
    @SerialName("id") val id: String? = null,
    @SerialName("document_type") val documentType: String,
    @SerialName("document_number") val documentNumber: String? = null,
    @SerialName("classification_code") val classificationCode: String? = null,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("year") val year: Int,
    @SerialName("physical_form") val physicalForm: String,
    @SerialName("condition") val condition: String,
    @SerialName("copy_count") val copyCount: Int,
    @SerialName("is_copy") val isCopy: Boolean? = null,
    @SerialName("status") val status: String,
    @SerialName("origin_instance") val originInstance: String? = null,
    @SerialName("storage_location_id") val storageLocationId: String? = null,
    @SerialName("source_staging_id") val sourceStagingId: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("updated_by") val updatedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null,
    
    // Join properties
    @SerialName("storage_locations") val storageLocation: StorageLocationDto? = null
)

@Serializable
data class StorageLocationDto(
    @SerialName("id") val id: String? = null,
    @SerialName("room") val room: String,
    @SerialName("shelf") val shelf: String? = null,
    @SerialName("box_number") val boxNumber: String? = null
)

@Serializable
data class DocumentPlacementDto(
    @SerialName("id") val id: String? = null,
    @SerialName("archive_document_id") val archiveDocumentId: String? = null,
    @SerialName("storage_location_id") val storageLocationId: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("placed_at") val placedAt: String? = null,
    @SerialName("removed_at") val removedAt: String? = null
)

@Serializable
data class ArchiveClassificationDto(
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("parent_code") val parentCode: String? = null,
    @SerialName("level") val level: Int
)

// Mappers

fun StagingDocument.toDto(): StagingDocumentDto {
    return StagingDocumentDto(
        id = id.takeIf { it.isNotBlank() },
        documentType = documentType.name,
        documentNumber = documentNumber,
        classificationCode = classificationCode,
        title = title,
        description = description,
        year = year,
        physicalForm = physicalForm.name,
        condition = condition?.name ?: "GOOD",
        copyCount = copyCount,
        isCopy = isCopy,
        status = status.name,
        originInstance = originInstance,
        source = source.name
    )
}

fun StagingDocumentDto.toDomain(): com.bpkpad.arsipnonkeu.ui.screen.staging.StagingDocument {
    return com.bpkpad.arsipnonkeu.ui.screen.staging.StagingDocument(
        id = id ?: "",
        documentType = try { DocumentType.valueOf(documentType) } catch (e: Exception) { DocumentType.SURAT },
        documentNumber = documentNumber,
        classificationCode = classificationCode,
        title = title,
        description = description,
        year = year,
        physicalForm = try { PhysicalForm.valueOf(physicalForm) } catch (e: Exception) { PhysicalForm.SHEET },
        condition = try { DocumentCondition.valueOf(condition) } catch (e: Exception) { DocumentCondition.GOOD },
        copyCount = copyCount,
        isCopy = isCopy,
        status = try { DocumentStatus.valueOf(status) } catch (e: Exception) { DocumentStatus.AVAILABLE },
        originInstance = originInstance,
        source = try { com.bpkpad.arsipnonkeu.ui.screen.staging.StagingDocumentSource.valueOf(source) } catch (e: Exception) { com.bpkpad.arsipnonkeu.ui.screen.staging.StagingDocumentSource.MANUAL }
    )
}

fun ArchiveClassificationDto.toDomain(): ArchiveClassification {
    return ArchiveClassification(
        code = code,
        name = name,
        parentCode = parentCode,
        level = level
    )
}
