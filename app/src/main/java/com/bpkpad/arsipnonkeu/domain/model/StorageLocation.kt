package com.bpkpad.arsipnonkeu.domain.model


data class StorageLocation(
    val id: String,
    val room: String,
    val shelf: String?,
    val boxNumber: String?
)
