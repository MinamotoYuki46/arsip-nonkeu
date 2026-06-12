package com.bpkpad.arsipnonkeu.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArchiveDocumentDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("document_type")
    val documentType: String? = null,

    @SerialName("document_number")
    val documentNumber: String? = null,

    @SerialName("document_code")
    val documentCode: String? = null,

    @SerialName("title")
    val title: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("year")
    val year: Int,

    @SerialName("physical_form")
    val physicalForm: String? = null,

    @SerialName("condition")
    val condition: String? = null,

    @SerialName("copy_count")
    val copyCount: Int? = null,

    @SerialName("is_copy")
    val isCopy: Boolean? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("origin_instance")
    val originInstance: String? = null,

    @SerialName("created_by")
    val createdBy: String? = null,

    @SerialName("updated_by")
    val updatedBy: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("deleted_at")
    val deletedAt: String? = null
)