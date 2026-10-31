package com.ai.heymika.core.data.error

import com.ai.heymika.core.domain.model.AppError
import com.ai.heymika.core.domain.model.Quota
import com.ai.heymika.core.domain.model.QuotaKind
import com.ai.heymika.core.domain.model.QuotaPeriod
import com.ai.heymika.core.testing.httpException
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Instant
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException

class ErrorMapperTest {
    // region offline

    @Test
    fun `unknown host is offline`() {
        assertEquals(AppError.Offline, map(UnknownHostException("api.example.com")))
    }

    @Test
    fun `socket timeout is offline`() {
        assertEquals(AppError.Offline, map(SocketTimeoutException("timeout")))
    }

    @Test
    fun `refused connection is offline`() {
        assertEquals(AppError.Offline, map(ConnectException("Connection refused")))
    }

    @Test
    fun `any other IOException is offline`() {
        assertEquals(AppError.Offline, map(IOException("stream was reset")))
        assertEquals(AppError.Offline, map(SSLHandshakeException("captive portal")))
    }

    @Test
    fun `concurrent timeout is offline`() {
        assertEquals(AppError.Offline, map(TimeoutException()))
    }

    @Test
    fun `IOException in the cause chain is offline`() {
        assertEquals(AppError.Offline, map(IllegalStateException("wrapped", RuntimeException(UnknownHostException()))))
    }

    // endregion

    // region unknown throwables

    @Test
    fun `malformed json is unknown although it is an IOException`() {
        val error = MalformedJsonException("Unterminated object")

        assertEquals(AppError.Unknown(error), map(error))
    }

    @Test
    fun `json syntax error is unknown and keeps the outer throwable`() {
        val error = JsonSyntaxException(MalformedJsonException("Expected BEGIN_OBJECT"))

        assertEquals(AppError.Unknown(error), map(error))
    }

    @Test
    fun `unclassified throwable is unknown with its cause`() {
        val error = IllegalStateException("boom")

        assertEquals(AppError.Unknown(error), map(error))
    }

    @Test
    fun `cyclic cause chain terminates as unknown`() {
        val first = IllegalStateException("first")
        val second = IllegalArgumentException("second", first)
        first.initCause(second)

        assertEquals(AppError.Unknown(first), map(first))
    }

    // endregion

    // region http status

    @Test
    fun `401 is session expired even with a field in the body`() {
        assertEquals(AppError.SessionExpired, map(httpException(401, """{"field":"password","code":"WRONG"}""")))
    }

    @Test
    fun `403 is forbidden`() {
        assertEquals(AppError.Forbidden, map(httpException(403)))
    }

    @Test
    fun `404 is not found`() {
        assertEquals(AppError.NotFound, map(httpException(404)))
    }

    @Test
    fun `HttpException in the cause chain is mapped by its status`() {
        assertEquals(AppError.NotFound, map(RuntimeException(httpException(404))))
    }

    @Test
    fun `402 with a quota is quota exceeded`() {
        val body =
            """
            {"code":"QUOTA_EXCEEDED","quota":{"kind":"GENERATOR_CREDITS","used":30,"limit":30,
            "period":"MONTHLY","resetAt":1798761600000}}
            """.trimIndent()

        assertEquals(
            AppError.QuotaExceeded(Quota(QuotaKind.GENERATOR_CREDITS, 30, 30, QuotaPeriod.MONTHLY, 1798761600000)),
            map(httpException(402, body)),
        )
    }

    @Test
    fun `429 reads a wrapped quota with loose kind, period and ISO reset time`() {
        val body =
            """
            {"error":{"quota":{"kind":"ai-chat","used":"20","limit":20,"period":"daily",
            "resetAt":"2026-10-30T00:00:00Z"}}}
            """.trimIndent()

        assertEquals(
            AppError.QuotaExceeded(
                Quota(
                    QuotaKind.AI_CHAT,
                    20,
                    20,
                    QuotaPeriod.DAILY,
                    Instant.parse("2026-10-30T00:00:00Z").toEpochMilli(),
                ),
            ),
            map(httpException(429, body)),
        )
    }

    @Test
    fun `quota without used or period is full and daily`() {
        val body = """{"quota":{"kind":"PAGES","limit":100}}"""

        assertEquals(
            AppError.QuotaExceeded(Quota(QuotaKind.PAGES, 100, 100, QuotaPeriod.DAILY, null)),
            map(httpException(429, body)),
        )
    }

    @Test
    fun `429 without a body is unknown`() {
        val error = httpException(429)

        assertEquals(AppError.Unknown(error), map(error))
    }

    @Test
    fun `429 with a quota of unknown kind is unknown`() {
        val error = httpException(429, """{"quota":{"kind":"VIDEO_MINUTES","used":5,"limit":5}}""")

        assertEquals(AppError.Unknown(error), map(error))
    }

    @Test
    fun `422 with field and code is validation`() {
        assertEquals(
            AppError.Validation("email", "EMAIL_TAKEN"),
            map(httpException(422, """{"field":"email","code":"EMAIL_TAKEN","message":"Email sudah dipakai"}""")),
        )
    }

    @Test
    fun `400 reads the first entry of an errors array`() {
        val body = """{"errors":["not an object",{"field":"password","code":"TOO_SHORT"},{"field":"email"}]}"""

        assertEquals(AppError.Validation("password", "TOO_SHORT"), map(httpException(400, body)))
    }

    @Test
    fun `errors entry without a code takes the top level code`() {
        val body = """{"error":{"code":"VALIDATION","errors":[{"field":"name"}]}}"""

        assertEquals(AppError.Validation("name", "VALIDATION"), map(httpException(400, body)))
    }

    @Test
    fun `field without any code uses the default validation code`() {
        assertEquals(
            AppError.Validation("password", ErrorMapper.DEFAULT_VALIDATION_CODE),
            map(httpException(422, """{"field":"password"}""")),
        )
    }

    @Test
    fun `422 without a field is unknown`() {
        val error = httpException(422, """{"code":"INVALID_STATE","message":"Coba lagi"}""")

        assertEquals(AppError.Unknown(error), map(error))
    }

    @Test
    fun `400 with an html body is unknown`() {
        val error = httpException(400, "<html><body>Bad Request</body></html>")

        assertEquals(AppError.Unknown(error), map(error))
    }

    @Test
    fun `503 with retry after seconds rounds up to minutes`() {
        assertEquals(AppError.Maintenance(2), map(httpException(503, retryAfter = "120")))
        assertEquals(AppError.Maintenance(2), map(httpException(503, retryAfter = "90")))
        assertEquals(AppError.Maintenance(0), map(httpException(503, retryAfter = "0")))
    }

    @Test
    fun `503 with an http date counts the minutes from now`() {
        assertEquals(AppError.Maintenance(30), map(httpException(503, retryAfter = "Thu, 29 Oct 2026 08:30:00 GMT")))
        assertEquals(AppError.Maintenance(1), map(httpException(503, retryAfter = "Thu, 29 Oct 2026 08:00:30 GMT")))
    }

    @Test
    fun `503 with a past http date is zero minutes`() {
        assertEquals(AppError.Maintenance(0), map(httpException(503, retryAfter = "Thu, 29 Oct 2026 07:00:00 GMT")))
    }

    @Test
    fun `503 without retry after reads the eta from the body`() {
        assertEquals(AppError.Maintenance(45), map(httpException(503, """{"etaMinutes":45}""")))
        assertEquals(AppError.Maintenance(15), map(httpException(503, """{"error":{"etaMinutes":15}}""")))
    }

    @Test
    fun `503 header wins over the body`() {
        assertEquals(AppError.Maintenance(1), map(httpException(503, """{"etaMinutes":45}""", retryAfter = "60")))
    }

    @Test
    fun `503 with an unreadable retry after and no body has no eta`() {
        assertEquals(AppError.Maintenance(null), map(httpException(503, retryAfter = "soon")))
        assertEquals(AppError.Maintenance(null), map(httpException(503, retryAfter = "-5")))
        assertEquals(AppError.Maintenance(null), map(httpException(503)))
    }

    @Test
    fun `other 5xx is server error with its status`() {
        assertEquals(AppError.Server(500), map(httpException(500)))
        assertEquals(AppError.Server(502), map(httpException(502)))
        assertEquals(AppError.Server(599), map(httpException(599)))
    }

    @Test
    fun `other 4xx is unknown with the exception as cause`() {
        listOf(405, 409, 410, 418).forEach { status ->
            val error = httpException(status)

            assertEquals(AppError.Unknown(error), map(error))
        }
    }

    // endregion

    // region fromHttp

    @Test
    fun `fromHttp maps a bare status`() {
        assertEquals(AppError.SessionExpired, ErrorMapper.fromHttp(401))
        assertEquals(AppError.Server(504), ErrorMapper.fromHttp(504))
        assertEquals(AppError.Unknown(null), ErrorMapper.fromHttp(409))
    }

    @Test
    fun `fromHttp reads the body and retry after`() {
        assertEquals(
            AppError.QuotaExceeded(Quota(QuotaKind.DOCUMENTS, 3, 3, QuotaPeriod.MONTHLY, null)),
            ErrorMapper.fromHttp(402, """{"quota":{"kind":"DOCUMENTS","used":3,"limit":3,"period":"MONTHLY"}}"""),
        )
        assertEquals(AppError.Maintenance(5), ErrorMapper.fromHttp(503, retryAfter = "300", nowMillis = NOW))
    }

    // endregion

    private fun map(throwable: Throwable): AppError = ErrorMapper.map(throwable, nowMillis = NOW)

    private companion object {
        val NOW: Long = Instant.parse("2026-10-29T08:00:00Z").toEpochMilli()
    }
}
