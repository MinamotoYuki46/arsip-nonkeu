package com.bpkpad.arsipnonkeu.data.local

import android.content.Context
import com.bpkpad.arsipnonkeu.domain.model.StagingDraft
import org.json.JSONArray
import org.json.JSONObject

class StagingDraftLocalDataSource(
    context: Context
) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    fun getAll(): List<StagingDraft> {
        val rawJson = prefs.getString(KEY_DRAFTS, "[]") ?: "[]"

        return runCatching {
            val array = JSONArray(rawJson)

            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(obj.toStagingDraft())
                }
            }
        }.getOrDefault(emptyList())
    }

    fun upsert(draft: StagingDraft) {
        val currentDrafts = getAll().toMutableList()
        val index = currentDrafts.indexOfFirst { it.id == draft.id }

        if (index >= 0) {
            currentDrafts[index] = draft
        } else {
            currentDrafts.add(draft)
        }

        saveAll(currentDrafts)
    }

    fun deleteById(id: String) {
        val updatedDrafts = getAll().filterNot { it.id == id }
        saveAll(updatedDrafts)
    }

    fun clearAll() {
        prefs.edit()
            .remove(KEY_DRAFTS)
            .apply()
    }

    private fun saveAll(drafts: List<StagingDraft>) {
        val array = JSONArray()

        drafts.forEach { draft ->
            array.put(draft.toJsonObject())
        }

        prefs.edit()
            .putString(KEY_DRAFTS, array.toString())
            .apply()
    }

    private fun StagingDraft.toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("archiveCode", archiveCode)
            put("title", title)
            put("documentType", documentType)
            put("physicalForm", physicalForm)
            put("condition", condition)
            put("status", status)
            put("locationText", locationText)
            put("description", description)
            put("createdAtMillis", createdAtMillis)
            put("updatedAtMillis", updatedAtMillis)
        }
    }

    private fun JSONObject.toStagingDraft(): StagingDraft {
        return StagingDraft(
            id = optString("id"),
            archiveCode = optString("archiveCode"),
            title = optString("title"),
            documentType = optString("documentType"),
            physicalForm = optString("physicalForm"),
            condition = optString("condition"),
            status = optString("status"),
            locationText = optString("locationText"),
            description = optString("description"),
            createdAtMillis = optLong("createdAtMillis"),
            updatedAtMillis = optLong("updatedAtMillis")
        )
    }

    private companion object {
        const val PREF_NAME = "arsip_nonkeu_staging_pref"
        const val KEY_DRAFTS = "staging_drafts"
    }
}