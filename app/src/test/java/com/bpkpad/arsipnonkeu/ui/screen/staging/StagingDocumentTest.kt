package com.bpkpad.arsipnonkeu.ui.screen.staging

import com.bpkpad.arsipnonkeu.domain.model.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class StagingDocumentTest : StringSpec({

    "toArchiveDocument should map all fields correctly" {
        val staging = StagingDocument(
            id = "S01",
            documentType = DocumentType.PERDA,
            documentNumber = "001",
            classificationCode = "C01",
            title = "Test",
            description = "Desc",
            year = 2024,
            physicalForm = PhysicalForm.BOOK,
            condition = DocumentCondition.GOOD,
            copyCount = 1,
            isCopy = false,
            status = DocumentStatus.AVAILABLE,
            originInstance = "BPKPAD",
            source = StagingDocumentSource.MANUAL
        )

        val archive = staging.toArchiveDocument()

        archive.id shouldBe "S01"
        archive.documentType shouldBe DocumentType.PERDA
        archive.title shouldBe "Test"
        archive.year shouldBe 2024
        archive.createdBy shouldBe "user-current"
    }
})
