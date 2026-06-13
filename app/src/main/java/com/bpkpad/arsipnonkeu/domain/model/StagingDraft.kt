package com.bpkpad.arsipnonkeu.domain.model

data class StagingDraft(
    val id: String,
    val archiveCode: String,
    val title: String,
    val documentType: String,
    val physicalForm: String,
    val condition: String,
    val status: String,
    val locationText: String,
    val description: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)