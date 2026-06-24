package com.bpkpad.arsipnonkeu.util

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    private val isoFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
    private val humanFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale("id", "ID"))

    fun formatIsoToHuman(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "-"
        return try {
            val zonedDateTime = ZonedDateTime.parse(isoString)
            zonedDateTime.format(humanFormatter)
        } catch (e: Exception) {
            isoString // Fallback to raw string if parsing fails
        }
    }
}
