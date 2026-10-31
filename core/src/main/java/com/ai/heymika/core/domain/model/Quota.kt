package com.ai.heymika.core.domain.model

/** What a [Quota] counts. */
enum class QuotaKind { DOCUMENTS, PAGES, AI_CHAT, GENERATOR_CREDITS }

/** How often a [Quota] resets. */
enum class QuotaPeriod { DAILY, MONTHLY }

/**
 * One usage meter of the user's plan, minimal on purpose (P3 extends it into AiQuota/Entitlement).
 *
 * @property used units consumed in the current [period].
 * @property limit units allowed per [period]; null means unlimited.
 * @property resetAt when the meter resets, in epoch milliseconds; null when unknown.
 */
data class Quota(
    val kind: QuotaKind,
    val used: Int,
    val limit: Int?,
    val period: QuotaPeriod,
    val resetAt: Long?,
) {
    /** Units left, or null when unlimited. Never negative. */
    val remaining: Int?
        get() = limit?.let { (it - used).coerceAtLeast(0) }

    /** True when a limit exists and has been reached. */
    val isExhausted: Boolean
        get() = limit != null && used >= limit
}
