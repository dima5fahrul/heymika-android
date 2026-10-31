package com.ai.heymika.core.data

import com.ai.heymika.core.domain.model.AppError

/**
 * Wrapper for anything that is loaded asynchronously and can fail.
 *
 * [Loading] may carry stale cached [data] while a refresh is in flight, and
 * [Error] may carry the last known good [data] alongside its [message], so a
 * screen can keep showing content instead of blanking out.
 */
sealed class Resource<T>(
    val data: T? = null,
    open val message: String? = null,
) {
    class Loading<T>(
        data: T? = null,
    ) : Resource<T>(data)

    class Success<T>(
        data: T,
    ) : Resource<T>(data)

    /**
     * [message] narrows the parent's nullable declaration: an error always has
     * one, so a screen smart-cast to [Error] can pass it straight to a composable
     * that requires a non-null String.
     *
     * [error] is the typed cause the UI maps onto MikaErrorState; it defaults to
     * [AppError.Unknown] so call sites that only have a message keep compiling.
     */
    class Error<T>(
        override val message: String,
        data: T? = null,
        val error: AppError = AppError.Unknown(null),
    ) : Resource<T>(data, message)
}
