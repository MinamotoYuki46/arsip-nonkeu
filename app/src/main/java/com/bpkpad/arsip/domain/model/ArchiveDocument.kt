package com.bpkpad.arsip.domain.model

data class ArchiveDocument(
    val id: String = "",
    val title: String = "",
    val type: String = "",
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val boxId: String = "",
    val locationId: String = "",
    val imageUrl: String? = null
)
