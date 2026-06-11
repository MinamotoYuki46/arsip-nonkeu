package com.bpkpad.arsipnonkeu.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StorageLocationDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("room")
    val room: String? = null,

    @SerialName("shelf")
    val shelf: String? = null,

    @SerialName("box_number")
    val boxNumber: String? = null
)