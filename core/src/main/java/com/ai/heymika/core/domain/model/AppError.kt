package com.ai.heymika.core.domain.model

/**
 * Every failure the UI knows how to explain. Data sources map transport errors onto these
 * (core/data/error/ErrorMapper); screens map them onto MikaErrorState copy and actions.
 */
sealed class AppError {
    /** 404: the material, workspace or route no longer exists. */
    data object NotFound : AppError()

    /** 403: the content is private or needs another plan. */
    data object Forbidden : AppError()

    /** 401 after the refresh failed: the user must sign in again. */
    data object SessionExpired : AppError()

    /** No network; cached data may still be shown. */
    data object Offline : AppError()

    /** 503 planned maintenance. [etaMinutes] is null when the server gives no estimate. */
    data class Maintenance(
        val etaMinutes: Int?,
    ) : AppError()

    /** A plan limit was hit; [quota] says which one and when it resets. */
    data class QuotaExceeded(
        val quota: Quota,
    ) : AppError()

    /** Server-side validation of one input: [field] (e.g. "email") and a stable [code]. */
    data class Validation(
        val field: String,
        val code: String,
    ) : AppError()

    /** Any other 5xx failure (503 is [Maintenance]); [code] is the HTTP status. Unknown 4xx map to [Unknown]. */
    data class Server(
        val code: Int,
    ) : AppError()

    /** Anything unclassified. [cause] is for logging only, never shown to the user. */
    data class Unknown(
        val cause: Throwable?,
    ) : AppError()
}
