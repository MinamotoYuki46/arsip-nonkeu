package com.bpkpad.arsipnonkeu.domain.repository

import com.bpkpad.arsipnonkeu.domain.model.ActivityLog

interface ActivityLogRepository {
    suspend fun getActivityLogs(): List<ActivityLog>
    suspend fun createActivityLog(
        action: String,
        entity: String,
        entityId: String?,
        description: String?
    )
}
