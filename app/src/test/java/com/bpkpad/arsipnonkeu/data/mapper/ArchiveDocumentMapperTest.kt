package com.bpkpad.arsipnonkeu.data.mapper

import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ArchiveDocumentMapperTest : StringSpec({

    "String toDocumentType should map correctly" {
        "SURAT".toDocumentType() shouldBe DocumentType.SURAT
        "PERDA".toDocumentType() shouldBe DocumentType.PERDA
        "INVALID".toDocumentType() shouldBe DocumentType.SURAT // Default
        null.toDocumentType() shouldBe DocumentType.SURAT
    }

    "String toDocumentStatus should map correctly" {
        "AVAILABLE".toDocumentStatus() shouldBe DocumentStatus.AVAILABLE
        "BORROWED".toDocumentStatus() shouldBe DocumentStatus.BORROWED
        "DISPOSED".toDocumentStatus() shouldBe DocumentStatus.DISPOSED
        null.toDocumentStatus() shouldBe DocumentStatus.AVAILABLE
    }

    "String toPhysicalForm should map correctly" {
        "SHEET".toPhysicalForm() shouldBe PhysicalForm.SHEET
        "BOOK".toPhysicalForm() shouldBe PhysicalForm.BOOK
        null.toPhysicalForm() shouldBe PhysicalForm.SHEET
    }

    "String toDocumentConditionOrNull should map correctly" {
        "GOOD".toDocumentConditionOrNull() shouldBe DocumentCondition.GOOD
        "DAMAGED".toDocumentConditionOrNull() shouldBe DocumentCondition.DAMAGED
        "UNKNOWN".toDocumentConditionOrNull() shouldBe DocumentCondition.GOOD // Default in implementation
        null.toDocumentConditionOrNull() shouldBe DocumentCondition.GOOD
    }
})
