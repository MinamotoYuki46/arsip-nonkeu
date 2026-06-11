package com.bpkpad.arsipnonkeu.domain.model

data class ActivityLog(
    val id: String,
    val userId: String?,
    val archiveDocumentId: String?,
    val action: ActivityAction,
    val description: String?,
    val createdAt: String?
)

enum class ActivityAction {
    CREATE_ARCHIVE,
    UPDATE_ARCHIVE,
    DELETE_ARCHIVE,
    MOVE_ARCHIVE,
    BORROW_ARCHIVE,
    RETURN_ARCHIVE,
    DISPOSE_ARCHIVE,
    EXPORT_ARCHIVE,
    LOGIN,
    OTHER
}
