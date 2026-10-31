package com.ai.heymika.core.utils.state

import com.ai.heymika.core.R
import com.ai.heymika.core.domain.model.AppError
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.domain.model.Quota
import com.ai.heymika.core.domain.model.QuotaKind
import com.ai.heymika.core.domain.model.QuotaPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorStateCopiesTest {
    @Test
    fun `every error maps to its catalog kind and pose`() {
        val expected =
            listOf(
                AppError.NotFound to (ErrorStateKind.NOT_FOUND to MikaPose.CONFUSED_MAP),
                AppError.Forbidden to (ErrorStateKind.FORBIDDEN to MikaPose.PADLOCK),
                AppError.SessionExpired to (ErrorStateKind.SESSION_EXPIRED to MikaPose.YAWN_HOURGLASS),
                AppError.Offline to (ErrorStateKind.OFFLINE to MikaPose.UNPLUGGED),
                AppError.Maintenance(null) to (ErrorStateKind.MAINTENANCE to MikaPose.HARDHAT),
                AppError.QuotaExceeded(quota()) to (ErrorStateKind.QUOTA_EXCEEDED to MikaPose.TROPHY),
                AppError.Validation("email", "INVALID") to (ErrorStateKind.VALIDATION to MikaPose.THINKING),
                AppError.Server(500) to (ErrorStateKind.SERVER to MikaPose.WORRIED),
                AppError.Unknown(null) to (ErrorStateKind.UNKNOWN to MikaPose.CONFUSED),
            )
        expected.forEach { (error, kindAndPose) ->
            val copy = ErrorStateCopies.of(error)
            assertEquals("kind of $error", kindAndPose.first, copy.kind)
            assertEquals("pose of $error", kindAndPose.second, copy.pose)
        }
    }

    @Test
    fun `only not found shows the search slot`() {
        assertTrue(ErrorStateCopies.of(AppError.NotFound).showSearch)
        assertFalse(ErrorStateCopies.of(AppError.Offline).showSearch)
        assertFalse(ErrorStateCopies.of(AppError.Server(502)).showSearch)
    }

    @Test
    fun `validation has no secondary action and no badge`() {
        val copy = ErrorStateCopies.of(AppError.Validation("email", "INVALID"))
        assertNull(copy.secondaryCtaRes)
        assertNull(copy.badge)
    }

    @Test
    fun `maintenance without a positive eta uses the generic body`() {
        assertEquals(
            ErrorStateText(R.string.state_maintenance_body),
            ErrorStateCopies.of(AppError.Maintenance(null)).body,
        )
        assertEquals(
            ErrorStateText(R.string.state_maintenance_body),
            ErrorStateCopies.of(AppError.Maintenance(0)).body,
        )
    }

    @Test
    fun `maintenance with an eta passes the minutes`() {
        assertEquals(
            ErrorStateText(R.string.state_maintenance_body_eta, listOf(15)),
            ErrorStateCopies.of(AppError.Maintenance(15)).body,
        )
    }

    @Test
    fun `server error carries its status code in the badge`() {
        assertEquals(
            ErrorStateText(R.string.state_server_badge, listOf(502)),
            ErrorStateCopies.of(AppError.Server(502)).badge,
        )
    }

    @Test
    fun `daily ai quota uses the ai copy and nests the usage phrase`() {
        val copy = ErrorStateCopies.of(AppError.QuotaExceeded(quota(kind = QuotaKind.AI_CHAT, used = 20)))
        assertEquals(R.string.state_quota_status_daily, copy.statusRes)
        assertEquals(R.string.state_quota_title_daily_ai, copy.titleRes)
        assertEquals(R.string.state_quota_speech_daily_ai, copy.speechRes)
        assertEquals(
            ErrorStateText(
                R.string.state_quota_body_daily,
                listOf(ErrorStateText(R.string.state_quota_usage_ai_chat, listOf(20))),
            ),
            copy.body,
        )
    }

    @Test
    fun `daily non ai quota uses the generic daily copy`() {
        val copy = ErrorStateCopies.of(AppError.QuotaExceeded(quota(kind = QuotaKind.DOCUMENTS, used = 3)))
        assertEquals(R.string.state_quota_title_daily, copy.titleRes)
        assertEquals(R.string.state_quota_speech_daily, copy.speechRes)
        assertEquals(
            ErrorStateText(
                R.string.state_quota_body_daily,
                listOf(ErrorStateText(R.string.state_quota_usage_documents, listOf(3))),
            ),
            copy.body,
        )
    }

    @Test
    fun `monthly quota uses the monthly status and title`() {
        val ai = ErrorStateCopies.of(AppError.QuotaExceeded(quota(QuotaKind.AI_CHAT, 5, QuotaPeriod.MONTHLY)))
        assertEquals(R.string.state_quota_status_monthly, ai.statusRes)
        assertEquals(R.string.state_quota_title_monthly_ai, ai.titleRes)
        assertEquals(R.string.state_quota_speech_monthly, ai.speechRes)

        val pages = ErrorStateCopies.of(AppError.QuotaExceeded(quota(QuotaKind.PAGES, 5, QuotaPeriod.MONTHLY)))
        assertEquals(R.string.state_quota_title_monthly, pages.titleRes)
        assertEquals(
            ErrorStateText(
                R.string.state_quota_body_monthly,
                listOf(ErrorStateText(R.string.state_quota_usage_pages, listOf(5))),
            ),
            pages.body,
        )
    }

    @Test
    fun `unknown usage selects the unknown bodies`() {
        assertEquals(
            ErrorStateText(R.string.state_quota_body_daily_unknown),
            ErrorStateCopies.of(AppError.QuotaExceeded(quota(used = 0))).body,
        )
        assertEquals(
            ErrorStateText(R.string.state_quota_body_monthly_unknown),
            ErrorStateCopies.of(AppError.QuotaExceeded(quota(used = 0, period = QuotaPeriod.MONTHLY))).body,
        )
    }

    @Test
    fun `generator credits have their own usage phrase`() {
        val copy = ErrorStateCopies.of(AppError.QuotaExceeded(quota(kind = QuotaKind.GENERATOR_CREDITS, used = 7)))
        assertEquals(
            ErrorStateText(
                R.string.state_quota_body_daily,
                listOf(ErrorStateText(R.string.state_quota_usage_generator_credits, listOf(7))),
            ),
            copy.body,
        )
    }

    private fun quota(
        kind: QuotaKind = QuotaKind.AI_CHAT,
        used: Int = 10,
        period: QuotaPeriod = QuotaPeriod.DAILY,
    ): Quota = Quota(kind = kind, used = used, limit = 10, period = period, resetAt = null)
}
