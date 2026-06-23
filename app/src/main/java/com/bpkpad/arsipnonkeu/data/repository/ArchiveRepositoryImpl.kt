package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.data.remote.model.*
import com.bpkpad.arsipnonkeu.data.mapper.*
import com.bpkpad.arsipnonkeu.data.local.datasource.ArchiveLocalDataSource
import com.bpkpad.arsipnonkeu.domain.model.*
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ArchiveRepositoryImpl(
    private val supabase: SupabaseClient,
    private val localDataSource: ArchiveLocalDataSource
) : ArchiveRepository {

    // --- NEW FLOW-BASED METHODS ---

    override fun observeArchiveYearSummaries(): Flow<List<ArchiveYearSummary>> {
        return localDataSource.observeArchiveYearSummaries()
    }

    override fun observeArchiveDocumentListItems(year: Int): Flow<List<ArchiveDocumentListItem>> {
        return localDataSource.observeArchiveDocumentListItems(year)
    }

    override suspend fun refreshArchiveYearSummaries() {
        // Fetch unique years from Supabase
        val response = supabase.postgrest["archive_documents"]
            .select(columns = Columns.raw("year")) {
                filter {
                    filter("deleted_at", FilterOperator.IS, "null")
                }
            }
        
        val years = response.decodeList<YearOnlyDto>()
            .map { it.year }
            .distinct()

        // For each year, fetch some documents to populate the local cache for year counts
        // A more efficient way would be an RPC that returns counts per year, 
        // but for now we'll just fetch documents.
        years.forEach { year ->
            refreshArchiveDocuments(year)
        }
    }

    override suspend fun refreshArchiveDocuments(year: Int) {
        val query = supabase.postgrest["archive_documents"].select(
            columns = Columns.raw("*, storage_locations(*)")
        ) {
            filter {
                eq("year", year)
                filter("deleted_at", FilterOperator.IS, "null")
            }
        }

        val documents = query.decodeList<ArchiveDocumentDto>().map { it.toDomain() }
        
        // Update local cache
        localDataSource.clearArchiveDocumentsByYear(year)
        localDataSource.saveArchiveDocuments(documents)
    }

    override suspend fun refreshArchiveDocumentById(id: String) {
        val dto = supabase.postgrest["archive_documents"].select {
            filter {
                eq("id", id)
            }
        }.decodeSingleOrNull<ArchiveDocumentDto>()

        dto?.let {
            localDataSource.saveArchiveDocument(it.toDomain())
        }
    }

    // --- LEGACY/SYNC METHODS ---

    override suspend fun getArchiveYearSummaries(): List<ArchiveYearSummary> {
        // Still can call remote if needed, but preferably use observe + refresh
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
                currentPlacement = null,
                storageLocation = dto.storageLocation?.toDomain()
            )
        }
    }

    override suspend fun getArchiveDocumentById(
        id: String
    ): ArchiveDocument? {
        // Try local first
        val local = localDataSource.getArchiveDocumentById(id)
        if (local != null) return local

        // Fallback to remote
        val remote = supabase.postgrest["archive_documents"].select {
            filter {
                eq("id", id)
            }
        }.decodeSingleOrNull<ArchiveDocumentDto>()?.toDomain()

        // Cache it if found
        remote?.let { localDataSource.saveArchiveDocument(it) }
        
        return remote
    }

    // --- WRITE OPERATIONS ---

    override suspend fun createArchiveDocument(
        document: ArchiveDocument
    ) {
        val actorId = supabase.auth.currentUserOrNull()?.id
        val dto = document.toDto().copy(createdBy = actorId)
        
        // 1. Remote Insert
        val insertedDto = supabase.postgrest["archive_documents"].insert(dto) {
            select()
        }.decodeSingle<ArchiveDocumentDto>()

        // 2. Local Cache Update
        localDataSource.saveArchiveDocument(insertedDto.toDomain())
    }

    override suspend fun updateArchiveDocument(
        document: ArchiveDocument
    ) {
        val actorId = supabase.auth.currentUserOrNull()?.id
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
            originInstance = document.originInstance,
            updatedBy = actorId
        )

        // 1. Remote Update
        val updatedDto = supabase.postgrest["archive_documents"].update(dto) {
            filter {
                eq("id", document.id)
            }
            select()
        }.decodeSingleOrNull<ArchiveDocumentDto>()

        // 2. Local Cache Update
        updatedDto?.let { localDataSource.saveArchiveDocument(it.toDomain()) }
    }

    override suspend fun deleteArchiveDocument(
        id: String
    ) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val timestamp = sdf.format(Date())

        // 1. Remote Soft Delete
        val updatedDto = supabase.postgrest["archive_documents"].update(
            buildJsonObject {
                put("deleted_at", timestamp)
            }
        ) {
            filter {
                eq("id", id)
            }
            select()
        }.decodeSingleOrNull<ArchiveDocumentDto>()

        // 2. Local Cache Update (or delete)
        updatedDto?.let { 
            // In our system, we don't show items with deleted_at IS NOT NULL
            // So we can either delete it or update it and the query filter will hide it.
            localDataSource.saveArchiveDocument(it.toDomain())
        }
    }

    override suspend fun saveStagingDocuments(
        documents: List<ArchiveDocument>,
        room: String,
        shelf: String,
        boxNumber: String?,
        actorId: String?
    ) {
        documents.forEach { doc ->
            supabase.postgrest.rpc(
                function = "push_staging_document_to_archive",
                parameters = buildJsonObject {
                    put("p_staging_document_id", doc.id)
                    put("p_room", room)
                    put("p_shelf", shelf)
                    put("p_box_number", boxNumber)
                    put("p_actor_id", actorId)
                }
            )
            // After successful push, we should ideally refresh the documents for that year
            refreshArchiveDocuments(doc.year)
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

        val domain = dto.toDomain()
        
        // Sync local cache
        localDataSource.saveArchiveDocument(domain)

        return ArchiveDocumentListItem(
            document = domain,
            currentPlacement = null,
            storageLocation = dto.storageLocation?.toDomain()
        )
    }

    override suspend fun checkStorageLocationExists(
        room: String,
        shelf: String,
        boxNumber: String?
    ): Boolean {
        val query = supabase.postgrest["storage_locations"].select {
            filter {
                eq("room", room)
                eq("shelf", shelf)
                if (boxNumber != null) {
                    eq("box_number", boxNumber)
                } else {
                    filter("box_number", FilterOperator.IS, "null")
                }
            }
        }
        return query.decodeList<StorageLocationDto>().isNotEmpty()
    }

    override suspend fun checkDocumentDuplicate(
        title: String,
        documentNumber: String?,
        year: Int
    ): Boolean {
        val response = supabase.postgrest["archive_documents"].select {
            filter {
                eq("title", title.trim())
                eq("year", year)
                if (documentNumber != null) {
                    eq("document_number", documentNumber.trim())
                }
                filter("deleted_at", FilterOperator.IS, "null")
            }
        }
        return response.decodeList<ArchiveDocumentDto>().isNotEmpty()
    }
}
