package com.bpkpad.arsipnonkeu.data.remote.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRemoteDataSource(
    private val supabaseClient: SupabaseClient
) {
    suspend fun loginWithEmail(email: String, password: String) {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun logout() {
        supabaseClient.auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }

    fun getCurrentUserEmail(): String? {
        return supabaseClient.auth.currentUserOrNull()?.email
    }
}
