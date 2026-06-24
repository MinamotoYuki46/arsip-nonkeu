package com.bpkpad.arsipnonkeu.util

import com.bpkpad.arsipnonkeu.domain.model.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ArchiveExcelServiceTest : StringSpec({

    "parseDocumentType should return correct enum" {
        ArchiveExcelService.parseDocumentType("Surat") shouldBe DocumentType.SURAT
        ArchiveExcelService.parseDocumentType("Peraturan Daerah") shouldBe DocumentType.PERDA
        ArchiveExcelService.parseDocumentType("Unknown") shouldBe DocumentType.SURAT // Default
    }

    "parsePhysicalForm should return correct enum" {
        ArchiveExcelService.parsePhysicalForm("Lembaran") shouldBe PhysicalForm.SHEET
        ArchiveExcelService.parsePhysicalForm("Buku") shouldBe PhysicalForm.BOOK
        ArchiveExcelService.parsePhysicalForm("Unknown") shouldBe PhysicalForm.SHEET // Default
    }

    "parseDocumentCondition should return correct enum or null" {
        ArchiveExcelService.parseDocumentCondition("Baik") shouldBe DocumentCondition.GOOD
        ArchiveExcelService.parseDocumentCondition("Rusak") shouldBe DocumentCondition.DAMAGED
        ArchiveExcelService.parseDocumentCondition("") shouldBe null
    }

    "parseIsCopy should return correct boolean or null" {
        ArchiveExcelService.parseIsCopy("Asli") shouldBe false
        ArchiveExcelService.parseIsCopy("Kopi") shouldBe true
        ArchiveExcelService.parseIsCopy("Unknown") shouldBe null
    }

    "parseColumnIndexFromCellReference should extract correct index" {
        ArchiveExcelService.parseColumnIndexFromCellReference("A1") shouldBe 0
        ArchiveExcelService.parseColumnIndexFromCellReference("B10") shouldBe 1
        ArchiveExcelService.parseColumnIndexFromCellReference("C5") shouldBe 2
        ArchiveExcelService.parseColumnIndexFromCellReference("AA1") shouldBe 26
    }
})
