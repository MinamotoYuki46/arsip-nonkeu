package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.data.remote.datasource.ActivityLogRemoteDataSource
import com.bpkpad.arsipnonkeu.data.remote.datasource.AuthRemoteDataSource
import com.bpkpad.arsipnonkeu.data.remote.dto.ActivityLogInsertDto
import com.bpkpad.arsipnonkeu.data.remote.mapper.toDomain
import com.bpkpad.arsipnonkeu.domain.model.ActivityLog
import com.bpkpad.arsipnonkeu.domain.repository.ActivityLogRepository

class ActivityLogRepositoryImpl(
    private val remoteDataSource: ActivityLogRemoteDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource
) : ActivityLogRepository {
    override suspend fun getActivityLogs(): List<ActivityLog> {
        return remoteDataSource.getActivityLogs().map { it.toDomain() }
    }

    override suspend fun createActivityLog(
        action: String,
        entity: String,
        entityId: String?,
        description: String?
    ) {
        val actorId = authRemoteDataSource.getCurrentUserId()
        val insertDto = ActivityLogInsertDto(
            actorId = actorId,
            action = action,
            entity = entity,
            entityId = entityId,
            description = description
        )
        remoteDataSource.createActivityLog(insertDto)
    }
}
