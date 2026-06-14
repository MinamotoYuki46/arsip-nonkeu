package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.data.remote.model.*
import com.bpkpad.arsipnonkeu.data.mapper.*
import com.bpkpad.arsipnonkeu.domain.model.*
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ArchiveRepositoryImpl(
    private val supabase: SupabaseClient
) : ArchiveRepository {

    override suspend fun getArchiveYearSummaries(): List<ArchiveYearSummary> {
        val response = supabase.postgrest["archive_documents"]
            .select(columns = Columns.raw("year")) {
                filter {
                    filter("deleted_at", FilterOperator.IS, "null")
                }
            }
        
        val years = response.decodeList<YearOnlyDto>()
            .map { it.year }
            .distinct()
            .sortedDescending()

        return years.map { year ->
            val countResponse = supabase.postgrest["archive_documents"]
                .select(columns = Columns.raw("id")) {
                    filter {
                        eq("year", year)
                        filter("deleted_at", FilterOperator.IS, "null")
                    }
                }
            
            ArchiveYearSummary(year, countResponse.decodeList<Map<String, String>>().size)
        }
    }

    override suspend fun getArchiveDocumentListItems(
        filter: ArchiveDocumentFilter
    ): List<ArchiveDocumentListItem> {
        val query = supabase.postgrest["archive_documents"].select(
            columns = Columns.raw("*, storage_locations(*)")
        ) {
            filter {
                eq("year", filter.year)
                filter("deleted_at", FilterOperator.IS, "null")
                filter.documentType?.let { eq("document_type", it.name) }
                filter.status?.let { eq("status", it.name) }
                filter.physicalForm?.let { eq("physical_form", it.name) }
                filter.condition?.let { eq("condition", it.name) }
                if (!filter.keyword.isNullOrBlank()) {
                    or {
                        ilike("title", "%${filter.keyword}%")
                        ilike("document_number", "%${filter.keyword}%")
                    }
                }
            }
        }

        return query.decodeList<ArchiveDocumentDto>().map { dto ->
            ArchiveDocumentListItem(
                document = dto.toDomain(),
                currentPlacement = null, // Can fetch from document_placements if needed
                storageLocation = dto.storageLocation?.toDomain()
            )
        }
    }

    override suspend fun getArchiveDocumentById(
        id: String
    ): ArchiveDocument? {
        return supabase.postgrest["archive_documents"].select {
            filter {
                eq("id", id)
            }
        }.decodeSingleOrNull<ArchiveDocumentDto>()?.toDomain()
    }

    override suspend fun createArchiveDocument(
        document: ArchiveDocument
    ) {
        // This is usually done via push_staging_document_to_archive
        // but can be implemented for direct creation if needed.
    }

    override suspend fun updateArchiveDocument(
        document: ArchiveDocument
    ) {
        val dto = ArchiveDocumentDto(
            documentType = document.documentType.name,
            documentNumber = document.documentNumber,
            classificationCode = document.classificationCode,
            title = document.title,
            description = document.description,
            year = document.year,
            physicalForm = document.physicalForm.name,
            condition = document.condition?.name ?: "GOOD",
            copyCount = document.copyCount,
            isCopy = document.isCopy,
            status = document.status.name,
            originInstance = document.originInstance
        )

        supabase.postgrest["archive_documents"].update(dto) {
            filter {
                eq("id", document.id)
            }
        }
    }

    override suspend fun deleteArchiveDocument(
        id: String
    ) {
        supabase.postgrest["archive_documents"].update(
            buildJsonObject {
                put("deleted_at", System.currentTimeMillis().toString()) // Should use DB now() ideally
            }
        ) {
            filter {
                eq("id", id)
            }
        }
    }

    override suspend fun saveStagingDocuments(
        documents: List<ArchiveDocument>,
        room: String,
        shelf: String,
        boxNumber: String?
    ) {
        // We use RPC push_staging_document_to_archive for each document
        documents.forEach { doc ->
            supabase.postgrest.rpc(
                function = "push_staging_document_to_archive",
                parameters = buildJsonObject {
                    put("p_staging_document_id", doc.id)
                    put("p_room", room)
                    put("p_shelf", shelf)
                    put("p_box_number", boxNumber)
                }
            )
        }
    }

    override suspend fun getArchiveDocumentListItemById(
        id: String
    ): ArchiveDocumentListItem? {
        val dto = supabase.postgrest["archive_documents"].select(
            columns = Columns.raw("*, storage_locations(*)")
        ) {
            filter {
                eq("id", id)
            }
        }.decodeSingleOrNull<ArchiveDocumentDto>() ?: return null

        return ArchiveDocumentListItem(
            document = dto.toDomain(),
            currentPlacement = null,
            storageLocation = dto.storageLocation?.toDomain()
        )
    }
}
