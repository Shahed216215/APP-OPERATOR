package com.mira.operator

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Minimal local audit trail.
 * Never accepts UI text, message bodies, credentials, OTPs, or screenshots.
 */
object OperatorAuditLog {
    private const val PREFS = "operator_audit"
    private const val KEY_ENTRIES = "entries"
    private const val MAX_ENTRIES = 200

    @Synchronized
    fun record(
        context: Context,
        action: String,
        packageName: String?,
        outcome: String
    ) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = runCatching {
            JSONArray(prefs.getString(KEY_ENTRIES, "[]") ?: "[]")
        }.getOrDefault(JSONArray())

        val entry = JSONObject().apply {
            put("timestamp_ms", System.currentTimeMillis())
            put("action", action.take(48))
            put("package", packageName?.take(160) ?: JSONObject.NULL)
            put("outcome", outcome.take(48))
        }

        current.put(entry)
        val bounded = JSONArray()
        val start = (current.length() - MAX_ENTRIES).coerceAtLeast(0)
        for (i in start until current.length()) {
            bounded.put(current.getJSONObject(i))
        }

        // apply() writes asynchronously; records contain metadata only.
        prefs.edit().putString(KEY_ENTRIES, bounded.toString()).apply()
    }

    fun exportMetadata(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ENTRIES, "[]") ?: "[]"
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_ENTRIES)
            .apply()
    }
}
