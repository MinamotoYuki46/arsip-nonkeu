package com.bpkpad.arsipnonkeu.util

import com.bpkpad.arsipnonkeu.BuildConfig
import com.bpkpad.arsipnonkeu.domain.model.DocumentCondition
import com.bpkpad.arsipnonkeu.domain.model.DocumentStatus
import com.bpkpad.arsipnonkeu.domain.model.DocumentType
import com.bpkpad.arsipnonkeu.domain.model.PhysicalForm
import com.bpkpad.arsipnonkeu.ui.screen.scan.ParsedOcrDocument
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

object AiOcrParserService {

    suspend fun parseOcrText(
        documentType: DocumentType,
        rawOcrText: String
    ): ParsedOcrDocument {
        val prompt = OcrPromptFactory.buildPrompt(
            documentType = documentType,
            rawOcrText = rawOcrText
        )

        val responseText = when (BuildConfig.OCR_AI_PROVIDER.lowercase().trim()) {
            "groq" -> callGroq(prompt)
            "gemini" -> callGemini(prompt)
            else -> error("OCR_AI_PROVIDER tidak dikenal: ${BuildConfig.OCR_AI_PROVIDER}")
        }

        val jsonText = extractJson(responseText)

        return parseJsonToDocument(
            jsonText = jsonText,
            fallbackDocumentType = documentType,
            fallbackRawOcrText = rawOcrText
        )
    }

    fun createFallbackDocument(
        documentType: DocumentType,
        rawOcrText: String
    ): ParsedOcrDocument {
        return ParsedOcrDocument(
            documentType = documentType,
            documentNumber = null,
            classificationCode = null,
            title = "Dokumen Hasil Scan",
            description = rawOcrText.ifBlank { null },
            year = LocalDate.now().year,
            physicalForm = if (documentType == DocumentType.SURAT) {
                PhysicalForm.SHEET
            } else {
                PhysicalForm.BOOK
            },
            condition = null,
            copyCount = 1,
            isCopy = null,
            status = DocumentStatus.AVAILABLE,
            originInstance = null
        )
    }

    private fun callGroq(
        prompt: String
    ): String {
        val apiKey = BuildConfig.GROQ_API_KEY

        if (apiKey.isBlank()) {
            error("GROQ_API_KEY belum diatur")
        }

        val url = URL("https://api.groq.com/openai/v1/chat/completions")

        val body = JSONObject()
            .put("model", BuildConfig.OCR_AI_MODEL)
            .put("temperature", 0.1)
            .put(
                "messages",
                JSONArray()
                    .put(
                        JSONObject()
                            .put("role", "system")
                            .put(
                                "content",
                                "Kamu adalah sistem ekstraksi metadata arsip dokumen pemerintahan Indonesia. Output wajib JSON valid saja, tanpa markdown."
                            )
                    )
                    .put(
                        JSONObject()
                            .put("role", "user")
                            .put("content", prompt)
                    )
            )

        val response = postJson(
            url = url,
            body = body,
            headers = mapOf(
                "Authorization" to "Bearer $apiKey",
                "Content-Type" to "application/json"
            )
        )

        val root = JSONObject(response)
        val choices = root.optJSONArray("choices")
        val firstChoice = choices?.optJSONObject(0)
        val message = firstChoice?.optJSONObject("message")

        return message?.optString("content").orEmpty()
    }

    private fun callGemini(
        prompt: String
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank()) {
            error("GEMINI_API_KEY belum diatur")
        }

        val model = BuildConfig.OCR_AI_MODEL.ifBlank {
            "gemini-2.0-flash-lite"
        }

        val url = URL(
            "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        )

        val body = JSONObject()
            .put(
                "contents",
                JSONArray()
                    .put(
                        JSONObject()
                            .put(
                                "parts",
                                JSONArray()
                                    .put(
                                        JSONObject()
                                            .put("text", prompt)
                                    )
                            )
                    )
            )
            .put(
                "generationConfig",
                JSONObject()
                    .put("temperature", 0.1)
                    .put("responseMimeType", "application/json")
            )

        val response = postJson(
            url = url,
            body = body,
            headers = mapOf(
                "Content-Type" to "application/json"
            )
        )

        val root = JSONObject(response)
        val candidates = root.optJSONArray("candidates")
        val firstCandidate = candidates?.optJSONObject(0)
        val content = firstCandidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val firstPart = parts?.optJSONObject(0)

        return firstPart?.optString("text").orEmpty()
    }

    private fun postJson(
        url: URL,
        body: JSONObject,
        headers: Map<String, String>
    ): String {
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000

            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }

            connection.outputStream.use { outputStream ->
                outputStream.write(body.toString().toByteArray())
            }

            val responseCode = connection.responseCode

            val response = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }
                    ?: "AI provider error. responseCode=$responseCode"
            }

            if (responseCode == 401 || responseCode == 403) {
                error("API key tidak valid atau tidak punya akses. Cek API key provider AI.")
            }

            if (responseCode == 429) {
                error("Kuota/rate limit AI provider habis. Coba model lain, tunggu beberapa saat, atau cek dashboard provider.")
            }

            if (responseCode !in 200..299) {
                error("AI provider gagal memproses OCR. Kode error: $responseCode\n$response")
            }

            return response
        } finally {
            connection.disconnect()
        }
    }

    private fun extractJson(
        responseText: String
    ): String {
        val trimmed = responseText
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed
        }

        val start = trimmed.indexOf("{")
        val end = trimmed.lastIndexOf("}")

        if (start != -1 && end != -1 && end > start) {
            return trimmed.substring(start, end + 1)
        }

        error("Response AI tidak berisi JSON valid")
    }

    private fun parseJsonToDocument(
        jsonText: String,
        fallbackDocumentType: DocumentType,
        fallbackRawOcrText: String
    ): ParsedOcrDocument {
        val json = JSONObject(jsonText)

        val documentType = parseEnumOrDefault(
            value = json.optString("documentType"),
            default = fallbackDocumentType
        )

        val physicalForm = parseEnumOrDefault(
            value = json.optString("physicalForm"),
            default = if (documentType == DocumentType.SURAT) {
                PhysicalForm.SHEET
            } else {
                PhysicalForm.BOOK
            }
        )

        val status = parseEnumOrDefault(
            value = json.optString("status"),
            default = DocumentStatus.AVAILABLE
        )

        val condition = parseNullableEnum<DocumentCondition>(
            value = json.optString("condition")
        )

        return ParsedOcrDocument(
            documentType = documentType,
            documentNumber = json.optNullableString("documentNumber"),
            classificationCode = json.optNullableString("documentCode"),
            title = json.optString("title").ifBlank { "Dokumen Hasil Scan" },
            description = json.optNullableString("description") ?: fallbackRawOcrText.ifBlank { null },
            year = json.optInt("year", LocalDate.now().year),
            physicalForm = physicalForm,
            condition = condition,
            copyCount = json.optInt("copyCount", 1).coerceAtLeast(1),
            isCopy = if (json.has("isCopy") && !json.isNull("isCopy")) json.optBoolean("isCopy") else null,
            status = status,
            originInstance = json.optNullableString("originInstance")
        )
    }

    private inline fun <reified T : Enum<T>> parseEnumOrDefault(
        value: String?,
        default: T
    ): T {
        return enumValues<T>().firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: default
    }

    private inline fun <reified T : Enum<T>> parseNullableEnum(
        value: String?
    ): T? {
        if (value.isNullOrBlank() || value.equals("null", ignoreCase = true)) {
            return null
        }

        return enumValues<T>().firstOrNull {
            it.name.equals(value, ignoreCase = true)
        }
    }

    private fun JSONObject.optNullableString(
        key: String
    ): String? {
        if (!has(key) || isNull(key)) return null

        val value = optString(key).trim()

        return value.takeIf {
            it.isNotBlank() && !it.equals("null", ignoreCase = true)
        }
    }
}