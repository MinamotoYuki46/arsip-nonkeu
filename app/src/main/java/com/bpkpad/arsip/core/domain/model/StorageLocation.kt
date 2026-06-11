package com.bpkpad.arsip.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StorageLocation(
    val id: String,
    val room: String,
    val shelves: String,
    val number: String
)