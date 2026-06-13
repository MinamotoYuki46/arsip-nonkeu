package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.data.local.ArchiveClassificationData
import com.bpkpad.arsipnonkeu.domain.model.ArchiveClassification

class FakeArchiveClassificationRepository {

    suspend fun getAll(): List<ArchiveClassification> {
        return ArchiveClassificationData.all
    }

    suspend fun search(
        keyword: String
    ): List<ArchiveClassification> {
        return ArchiveClassificationData.search(keyword)
    }

    suspend fun findByCode(
        code: String?
    ): ArchiveClassification? {
        return ArchiveClassificationData.findByCode(code)
    }

    suspend fun isValidCode(
        code: String?
    ): Boolean {
        return ArchiveClassificationData.isValidCode(code)
    }

    suspend fun getChildren(
        parentCode: String?
    ): List<ArchiveClassification> {
        return ArchiveClassificationData.childrenOf(parentCode)
    }

    suspend fun getTopLevel(): List<ArchiveClassification> {
        return ArchiveClassificationData.topLevel()
    }
}
