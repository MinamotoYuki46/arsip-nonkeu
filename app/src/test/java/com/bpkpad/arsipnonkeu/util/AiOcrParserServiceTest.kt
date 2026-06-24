package com.bpkpad.arsipnonkeu.util

import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class AiOcrParserServiceTest : StringSpec({

    "extractJson should strip markdown code blocks" {
        val rawResponse = "Here is the result: ```json {\"title\": \"Test\"} ``` and more text."
        val result = AiOcrParserService.extractJson(rawResponse)
        result shouldBe "{\"title\": \"Test\"}"
    }

    "extractJson should return same string if no code blocks" {
        val rawResponse = "{\"title\": \"Test\"}"
        val result = AiOcrParserService.extractJson(rawResponse)
        result shouldBe "{\"title\": \"Test\"}"
    }

    "parseJsonToDocument should handle missing fields gracefully" {
        val json = "{\"title\": \"Document Title\", \"year\": 2023}"
        val result = AiOcrParserService.parseJsonToDocument(json, DocumentType.SURAT, "raw text")
        
        result.title shouldBe "Document Title"
        result.year shouldBe 2023
        result.documentType shouldBe DocumentType.SURAT
        result.physicalForm shouldBe PhysicalForm.SHEET // Default from parser logic
    }

    "createFallbackDocument should return document with raw text in description" {
        val rawText = "This is some unrecognized text"
        val result = AiOcrParserService.createFallbackDocument(DocumentType.PERDA, rawText)
        
        result.documentType shouldBe DocumentType.PERDA
        result.title shouldBe "Dokumen Hasil Scan"
        result.description shouldContain rawText
    }
})
