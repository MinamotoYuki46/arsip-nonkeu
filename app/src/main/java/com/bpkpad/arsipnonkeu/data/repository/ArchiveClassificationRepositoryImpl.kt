package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.data.remote.model.ArchiveClassificationDto
import com.bpkpad.arsipnonkeu.data.remote.model.toDomain
import com.bpkpad.arsipnonkeu.domain.model.ArchiveClassification
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveClassificationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperator

class ArchiveClassificationRepositoryImpl(
    private val supabase: SupabaseClient
) : ArchiveClassificationRepository {

    override suspend fun getAll(): List<ArchiveClassification> {
        return supabase.postgrest["archive_classifications"]
            .select()
            .decodeList<ArchiveClassificationDto>()
            .map { it.toDomain() }
    }

    override suspend fun search(keyword: String): List<ArchiveClassification> {
        return supabase.postgrest["archive_classifications"]
            .select {
                filter {
                    or {
                        ilike("name", "%$keyword%")
                        ilike("code", "%$keyword%")
                    }
                }
            }
            .decodeList<ArchiveClassificationDto>()
            .map { it.toDomain() }
    }

    override suspend fun findByCode(code: String?): ArchiveClassification? {
        if (code == null) return null
        return supabase.postgrest["archive_classifications"]
            .select {
                filter {
                    eq("code", code)
                }
            }
            .decodeSingleOrNull<ArchiveClassificationDto>()
            ?.toDomain()
    }

    override suspend fun isValidCode(code: String?): Boolean {
        return findByCode(code) != null
    }

    override suspend fun getChildren(parentCode: String?): List<ArchiveClassification> {
        return supabase.postgrest["archive_classifications"]
            .select {
                filter {
                    if (parentCode == null) {
                        filter("parent_code", FilterOperator.IS, "null")
                    } else {
                        eq("parent_code", parentCode)
                    }
                }
            }
            .decodeList<ArchiveClassificationDto>()
            .map { it.toDomain() }
    }

    override suspend fun getTopLevel(): List<ArchiveClassification> {
        return getChildren(null)
    }
}
