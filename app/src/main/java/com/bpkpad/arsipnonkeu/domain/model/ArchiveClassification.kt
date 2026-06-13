package com.bpkpad.arsipnonkeu.domain.model

data class ArchiveClassification(
    val code: String,
    val name: String,
    val parentCode: String?,
    val level: Int,
    val isActive: Boolean = true
) {
    val displayName: String
        get() = "$code - $name"

    val searchText: String
        get() = "$code $name".lowercase()

    fun matchesKeyword(keyword: String): Boolean {
        val normalizedKeyword = keyword.trim().lowercase()

        if (normalizedKeyword.isBlank()) {
            return true
        }

        return searchText.contains(normalizedKeyword)
    }
}
