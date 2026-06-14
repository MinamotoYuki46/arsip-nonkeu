package com.bpkpad.arsipnonkeu.domain.repository

import com.bpkpad.arsipnonkeu.domain.model.ArchiveClassification

interface ArchiveClassificationRepository {
    suspend fun getAll(): List<ArchiveClassification>
    suspend fun search(keyword: String): List<ArchiveClassification>
    suspend fun findByCode(code: String?): ArchiveClassification?
    suspend fun isValidCode(code: String?): Boolean
    suspend fun getChildren(parentCode: String?): List<ArchiveClassification>
    suspend fun getTopLevel(): List<ArchiveClassification>
}
