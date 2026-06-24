package com.bpkpad.arsipnonkeu.data.remote.datasource

import com.bpkpad.arsipnonkeu.data.remote.dto.UserProfileDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

class ProfileRemoteDataSource(
    private val supabaseClient: SupabaseClient
) {
    suspend fun getProfileById(id: String): UserProfileDto? {
        return supabaseClient.postgrest["profiles"]
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<UserProfileDto>()
    }

    suspend fun getProfileByUsername(username: String): UserProfileDto? {
        return supabaseClient.postgrest["profiles"]
            .select {
                filter {
                    eq("username", username)
                }
            }
            .decodeSingleOrNull<UserProfileDto>()
    }

    suspend fun getCurrentUserProfile(): UserProfileDto? {
        val id = supabaseClient.auth.currentUserOrNull()?.id ?: return null
        return getProfileById(id)
    }

    suspend fun getAllProfiles(): List<UserProfileDto> {
        return supabaseClient.postgrest["profiles"]
            .select()
            .decodeList<UserProfileDto>()
    }
}
