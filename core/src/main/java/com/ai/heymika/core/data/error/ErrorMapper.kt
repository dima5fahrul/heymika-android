package com.ai.heymika.core.data.error

import com.ai.heymika.core.domain.model.AppError
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import retrofit2.HttpException
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeoutException

/**
 * Maps a transport failure onto the [AppError] the UI knows how to explain. Plain Kotlin, no
 * Android, so every branch is unit tested.
 *
 * | Failure | AppError |
 * |---|---|
 * | IOException (unknown host, timeout, refused, reset, TLS) or TimeoutException, in the cause chain | Offline |
 * | HTTP 401 | SessionExpired |
 * | HTTP 403 | Forbidden |
 * | HTTP 404 | NotFound |
 * | HTTP 402 or 429 with a quota in the body | QuotaExceeded |
 * | HTTP 400 or 422 with a field in the body | Validation(field, code, else [DEFAULT_VALIDATION_CODE]) |
 * | HTTP 503 | Maintenance(Retry-After in whole minutes rounded up, else the body's etaMinutes) |
 * | any other HTTP 5xx | Server(status) |
 * | anything else, including malformed JSON and a 4xx without those details | Unknown(cause) |
 *
 * The error body format is documented on [ErrorBodyParser]. Backend contract: wrong credentials on
 * sign-in answer 400/422 with a field, never 401, because 401 always means the session is gone.
 *
 * Callers rethrow `CancellationException` before mapping; cancellation is not an error.
 */
object ErrorMapper {
    /** Validation code used when the server names the field but not why it was rejected. */
    const val DEFAULT_VALIDATION_CODE = "INVALID"

    private const val HTTP_BAD_REQUEST = 400
    private const val HTTP_UNAUTHORIZED = 401
    private const val HTTP_PAYMENT_REQUIRED = 402
    private const val HTTP_FORBIDDEN = 403
    private const val HTTP_NOT_FOUND = 404
    private const val HTTP_UNPROCESSABLE = 422
    private const val HTTP_TOO_MANY_REQUESTS = 429
    private const val HTTP_SERVER_ERROR_FIRST = 500
    private const val HTTP_UNAVAILABLE = 503
    private const val HTTP_SERVER_ERROR_LAST = 599

    private const val RETRY_AFTER = "Retry-After"
    private const val MAX_CAUSE_DEPTH = 8
    private const val SECONDS_PER_MINUTE = 60L
    private const val MILLIS_PER_MINUTE = 60_000L

    /** [nowMillis] resolves an HTTP-date Retry-After; tests pass a fixed clock. */
    fun map(
        throwable: Throwable,
        nowMillis: Long = System.currentTimeMillis(),
    ): AppError {
        var current: Throwable? = throwable
        var depth = 0
        while (current != null && depth < MAX_CAUSE_DEPTH) {
            when (current) {
                is HttpException -> return fromHttpException(current, nowMillis)
                is MalformedJsonException, is JsonSyntaxException -> return AppError.Unknown(throwable)
                is IOException, is TimeoutException -> return AppError.Offline
            }
            current = current.cause
            depth++
        }
        return AppError.Unknown(throwable)
    }

    /**
     * Maps an HTTP failure given as parts, for callers without an [HttpException] (fake data sources,
     * interceptors). [retryAfter] is the raw header value: delay-seconds or an HTTP-date.
     */
    fun fromHttp(
        status: Int,
        errorBody: String? = null,
        retryAfter: String? = null,
        nowMillis: Long = System.currentTimeMillis(),
        cause: Throwable? = null,
    ): AppError = resolve(status, { errorBody }, retryAfter, nowMillis, cause)

    private fun fromHttpException(
        exception: HttpException,
        nowMillis: Long,
    ): AppError {
        val response = exception.response()
        return resolve(
            status = exception.code(),
            readBody = { response?.errorBody()?.let { body -> runCatching { body.string() }.getOrNull() } },
            retryAfter = response?.headers()?.get(RETRY_AFTER),
            nowMillis = nowMillis,
            cause = exception,
        )
    }

    // The body is read lazily: most statuses never need it.
    private fun resolve(
        status: Int,
        readBody: () -> String?,
        retryAfter: String?,
        nowMillis: Long,
        cause: Throwable?,
    ): AppError =
        when (status) {
            HTTP_UNAUTHORIZED -> {
                AppError.SessionExpired
            }

            HTTP_FORBIDDEN -> {
                AppError.Forbidden
            }

            HTTP_NOT_FOUND -> {
                AppError.NotFound
            }

            HTTP_PAYMENT_REQUIRED, HTTP_TOO_MANY_REQUESTS -> {
                val quota = ErrorBodyParser.parse(readBody())?.quota
                if (quota != null) AppError.QuotaExceeded(quota) else AppError.Unknown(cause)
            }

            HTTP_BAD_REQUEST, HTTP_UNPROCESSABLE -> {
                ErrorBodyParser.parse(readBody())?.let(::validation) ?: AppError.Unknown(cause)
            }

            HTTP_UNAVAILABLE -> {
                val eta = retryAfterMinutes(retryAfter, nowMillis) ?: ErrorBodyParser.parse(readBody())?.etaMinutes
                AppError.Maintenance(eta)
            }

            in HTTP_SERVER_ERROR_FIRST..HTTP_SERVER_ERROR_LAST -> {
                AppError.Server(status)
            }

            else -> {
                AppError.Unknown(cause)
            }
        }

    private fun validation(body: ErrorBody): AppError.Validation? {
        val fieldError = body.fieldError ?: return null
        return AppError.Validation(fieldError.field, fieldError.code ?: body.code ?: DEFAULT_VALIDATION_CODE)
    }

    /** Retry-After as whole minutes rounded up; a date in the past is 0 and an unreadable value null. */
    private fun retryAfterMinutes(
        header: String?,
        nowMillis: Long,
    ): Int? {
        val value = header?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val seconds = value.toLongOrNull()
        val minutes =
            if (seconds != null) {
                if (seconds < 0) return null
                ceilDiv(seconds, SECONDS_PER_MINUTE)
            } else {
                val at = parseHttpDate(value) ?: return null
                ceilDiv((at - nowMillis).coerceAtLeast(0L), MILLIS_PER_MINUTE)
            }
        return minutes.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    private fun parseHttpDate(value: String): Long? =
        runCatching {
            ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
        }.getOrNull()

    private fun ceilDiv(
        value: Long,
        divisor: Long,
    ): Long = value / divisor + if (value % divisor == 0L) 0L else 1L
}
