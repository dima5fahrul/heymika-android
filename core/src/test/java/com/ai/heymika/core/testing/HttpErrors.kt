package com.ai.heymika.core.testing

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import okhttp3.Response as RawResponse

/** A Retrofit [HttpException] as a failed call would throw it: [status], a JSON [body], an optional Retry-After. */
fun httpException(
    status: Int,
    body: String = "",
    retryAfter: String? = null,
): HttpException {
    val raw =
        RawResponse
            .Builder()
            .request(Request.Builder().url("https://api.example.com/materials").build())
            .protocol(Protocol.HTTP_1_1)
            .code(status)
            .message("HTTP $status")
            .apply { if (retryAfter != null) header("Retry-After", retryAfter) }
            .build()
    return HttpException(Response.error<Any>(body.toResponseBody("application/json".toMediaType()), raw))
}
