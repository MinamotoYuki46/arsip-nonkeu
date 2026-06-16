package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.data.remote.model.StagingDocumentDto
import com.bpkpad.arsipnonkeu.data.remote.model.toDomain
import com.bpkpad.arsipnonkeu.data.remote.model.toDto
import com.bpkpad.arsipnonkeu.domain.repository.StagingRepository
import com.bpkpad.arsipnonkeu.ui.screen.staging.StagingDocument
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

class StagingRepositoryImpl(
    private val supabase: SupabaseClient
) : StagingRepository {

    override suspend fun getStagingDocuments(year: Int): List<StagingDocument> {
        return supabase.postgrest["staging_documents"]
            .select {
                filter {
                    eq("year", year)
                }
            }
            .decodeList<StagingDocumentDto>()
            .map { it.toDomain() }
    }

    override suspend fun upsertStagingDocument(document: StagingDocument) {
        val actorId = supabase.auth.currentUserOrNull()?.id
        val dto = document.toDto().copy(
            createdBy = actorId,
            updatedBy = actorId,
            updatedAt = null
        )

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
