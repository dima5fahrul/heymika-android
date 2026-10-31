package com.ai.heymika.core.data.error

import com.ai.heymika.core.domain.model.Quota
import com.ai.heymika.core.domain.model.QuotaKind
import com.ai.heymika.core.domain.model.QuotaPeriod
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.time.Instant

/** One rejected input: the field name (e.g. `email`) and its stable code, when the server sent one. */
internal data class FieldError(
    val field: String,
    val code: String?,
)

/** What the backend said about a failure, read from the HTTP error body. Every part is optional. */
internal data class ErrorBody(
    val code: String?,
    val fieldError: FieldError?,
    val quota: Quota?,
    val etaMinutes: Int?,
)

/**
 * Reads an error body tolerantly: anything that is not a JSON object gives null, and unknown or
 * mistyped members are ignored. Accepted shapes, optionally wrapped in `{"error": {...}}`:
 *
 * ```
 * {"code": "EMAIL_TAKEN", "field": "email"}
 * {"code": "VALIDATION", "errors": [{"field": "email", "code": "EMAIL_TAKEN"}]}
 * {"code": "QUOTA_EXCEEDED", "quota": {"kind": "AI_CHAT", "used": 20, "limit": 20, "period": "DAILY",
 *   "resetAt": 1767225600000}}            // resetAt may also be an ISO-8601 instant
 * {"code": "MAINTENANCE", "etaMinutes": 30}
 * ```
 *
 * A quota needs a known `kind`; `used` defaults to `limit` (the meter is full) and `period` to DAILY.
 */
internal object ErrorBodyParser {
    private const val MAX_BODY_CHARS = 64 * 1024

    fun parse(raw: String?): ErrorBody? {
        if (raw.isNullOrBlank() || raw.length > MAX_BODY_CHARS) return null
        val root =
            runCatching { JsonParser.parseString(raw) }
                .getOrNull()
                ?.takeIf { it.isJsonObject }
                ?.asJsonObject
                ?: return null
        val sources = listOfNotNull(root.objectOrNull("error"), root)
        return ErrorBody(
            code = sources.firstNotNullOfOrNull { it.stringOrNull("code") },
            fieldError = sources.firstNotNullOfOrNull(::fieldError),
            quota = sources.firstNotNullOfOrNull { it.objectOrNull("quota") }?.let(::quota),
            etaMinutes = sources.firstNotNullOfOrNull { it.intOrNull("etaMinutes") }?.coerceAtLeast(0),
        )
    }

    private fun fieldError(json: JsonObject): FieldError? {
        json.stringOrNull("field")?.let { return FieldError(it, json.stringOrNull("code")) }
        val first =
            json
                .get("errors")
                ?.takeIf { it.isJsonArray }
                ?.asJsonArray
                ?.firstOrNull { it.isJsonObject }
                ?.asJsonObject
                ?: return null
        val field = first.stringOrNull("field") ?: return null
        return FieldError(field, first.stringOrNull("code"))
    }

    private fun quota(json: JsonObject): Quota? {
        val kind = json.stringOrNull("kind")?.let(::quotaKind) ?: return null
        val limit = json.intOrNull("limit")
        return Quota(
            kind = kind,
            used = json.intOrNull("used") ?: limit ?: 0,
            limit = limit,
            period = json.stringOrNull("period")?.let(::quotaPeriod) ?: QuotaPeriod.DAILY,
            resetAt = json.epochMillisOrNull("resetAt"),
        )
    }

    private fun quotaKind(raw: String): QuotaKind? =
        QuotaKind.entries.firstOrNull { it.name.equals(raw.replace('-', '_'), ignoreCase = true) }

    private fun quotaPeriod(raw: String): QuotaPeriod? =
        QuotaPeriod.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }

    private fun JsonObject.objectOrNull(name: String): JsonObject? = get(name)?.takeIf { it.isJsonObject }?.asJsonObject

    private fun JsonObject.primitiveOrNull(name: String): JsonPrimitive? =
        get(name)?.takeIf { it.isJsonPrimitive }?.asJsonPrimitive

    private fun JsonObject.stringOrNull(name: String): String? =
        primitiveOrNull(name)?.takeIf { it.isString }?.asString?.takeIf { it.isNotBlank() }

    private fun JsonObject.intOrNull(name: String): Int? =
        primitiveOrNull(name)?.takeUnless { it.isBoolean }?.let { runCatching { it.asInt }.getOrNull() }

    private fun JsonObject.epochMillisOrNull(name: String): Long? {
        val value = primitiveOrNull(name)?.takeUnless { it.isBoolean } ?: return null
        if (value.isNumber) return runCatching { value.asLong }.getOrNull()
        val text = value.asString
        return text.toLongOrNull() ?: runCatching { Instant.parse(text).toEpochMilli() }.getOrNull()
    }
}
