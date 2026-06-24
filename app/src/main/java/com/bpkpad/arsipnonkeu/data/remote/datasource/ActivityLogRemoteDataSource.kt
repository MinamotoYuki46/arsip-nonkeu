package com.bpkpad.arsipnonkeu.data.remote.datasource

import com.bpkpad.arsipnonkeu.data.remote.dto.ActivityLogDto
import com.bpkpad.arsipnonkeu.data.remote.dto.ActivityLogInsertDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class ActivityLogRemoteDataSource(
    private val supabaseClient: SupabaseClient
) {
    suspend fun getActivityLogs(): List<ActivityLogDto> {
        return supabaseClient.postgrest["activity_logs"]
            .select(columns = Columns.ALL)
            .decodeList<ActivityLogDto>()
    }

    suspend fun createActivityLog(log: ActivityLogInsertDto) {
        supabaseClient.postgrest["activity_logs"].insert(log)
    }
}
