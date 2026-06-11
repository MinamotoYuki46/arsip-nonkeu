package com.bpkpad.arsipnonkeu.data.mapper

import com.bpkpad.arsipnonkeu.data.remote.model.ActivityLogDto
import com.bpkpad.arsipnonkeu.domain.model.ActivityAction
import com.bpkpad.arsipnonkeu.domain.model.ActivityLog


fun ActivityLogDto.toDomain(): ActivityLog {
    return ActivityLog(
        id = id.orEmpty(),
        userId = userId,
        archiveDocumentId = archiveDocumentId,
        action = action.toActivityAction(),
        description = description,
        createdAt = createdAt
    )
}

private fun String?.toActivityAction(): ActivityAction {
    return when (this?.uppercase()) {
        "CREATE_ARCHIVE" -> ActivityAction.CREATE_ARCHIVE
        "UPDATE_ARCHIVE" -> ActivityAction.UPDATE_ARCHIVE
        "DELETE_ARCHIVE" -> ActivityAction.DELETE_ARCHIVE
        "MOVE_ARCHIVE" -> ActivityAction.MOVE_ARCHIVE
        "BORROW_ARCHIVE" -> ActivityAction.BORROW_ARCHIVE
        "RETURN_ARCHIVE" -> ActivityAction.RETURN_ARCHIVE
        "DISPOSE_ARCHIVE" -> ActivityAction.DISPOSE_ARCHIVE
        "EXPORT_ARCHIVE" -> ActivityAction.EXPORT_ARCHIVE
        "LOGIN" -> ActivityAction.LOGIN
        else -> ActivityAction.OTHER
    }
}