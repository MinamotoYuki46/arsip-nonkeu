package com.bpkpad.arsipnonkeu.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("username")
    val username: String? = null,

    @SerialName("role")
    val role: String? = null
)