package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.data.remote.model.StagingDocumentDto
import com.bpkpad.arsipnonkeu.data.remote.model.toDomain
import com.bpkpad.arsipnonkeu.data.remote.model.toDto
import com.bpkpad.arsipnonkeu.domain.repository.StagingRepository
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingDocument
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class StagingRepositoryImpl(
    private val supabase: SupabaseClient
) : StagingRepository {

    override suspend fun getStagingDocuments(): List<StagingDocument> {
        return supabase.postgrest["staging_documents"]
            .select()
            .decodeList<StagingDocumentDto>()
            .map { it.toDomain() }
    }

    override suspend fun upsertStagingDocument(document: StagingDocument) {
        val dto = document.toDto()
        supabase.postgrest["staging_documents"].upsert(dto)
    }

    override suspend fun deleteStagingDocument(id: String) {
        supabase.postgrest["staging_documents"].delete {
            filter {
                eq("id", id)
            }
        }
    }
}
