package com.ai.heymika.core.data.source.remote.network

import com.ai.heymika.core.domain.model.AppError

sealed class ApiResponse<out R> {
    data class Success<out T>(
        val data: T,
    ) : ApiResponse<T>()

    /** [errorMessage] is for logs; [error] is the typed cause the UI branches on. */
    data class Error(
        val errorMessage: String,
        val error: AppError = AppError.Unknown(null),
    ) : ApiResponse<Nothing>()

    object Empty : ApiResponse<Nothing>()
}
