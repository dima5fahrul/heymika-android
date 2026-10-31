package com.ai.heymika.core.utils.state

import androidx.annotation.StringRes
import com.ai.heymika.core.R
import com.ai.heymika.core.domain.model.AppError
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.domain.model.Quota
import com.ai.heymika.core.domain.model.QuotaKind
import com.ai.heymika.core.domain.model.QuotaPeriod

/** The catalog entry an [AppError] renders as. MikaErrorState maps it to icons and a tone. */
enum class ErrorStateKind {
    NOT_FOUND,
    FORBIDDEN,
    SESSION_EXPIRED,
    OFFLINE,
    MAINTENANCE,
    QUOTA_EXCEEDED,
    VALIDATION,
    SERVER,
    UNKNOWN,
}

/**
 * A string resource and its format arguments. An argument that is itself an [ErrorStateText] is
 * resolved first and passed in as a String, so phrases can nest ("Kamu telah %1$s hari ini").
 */
data class ErrorStateText(
    @param:StringRes val resId: Int,
    val args: List<Any> = emptyList(),
)

/**
 * Everything MikaErrorState shows for one error, as resource ids. Plain data with no Compose,
 * so the mapping in [ErrorStateCopies] is unit-testable.
 *
 * @property statusRes uppercase status pill ("KODE KESALAHAN: 404 NOT FOUND").
 * @property badge small technical badge ("HTTP: 404"), or null.
 * @property speechRes Mika's quip in the speech bubble.
 * @property secondaryCtaRes null when the state offers no secondary action.
 * @property showSearch true when the caller's search slot should be shown (404 only).
 */
data class ErrorStateCopy(
    val kind: ErrorStateKind,
    val pose: MikaPose,
    @param:StringRes val statusRes: Int,
    val badge: ErrorStateText?,
    @param:StringRes val speechRes: Int,
    @param:StringRes val titleRes: Int,
    val body: ErrorStateText,
    @param:StringRes val primaryCtaRes: Int,
    @param:StringRes val secondaryCtaRes: Int?,
    val showSearch: Boolean,
)

/**
 * Maps an [AppError] to its catalog copy (system_states mockup). What the primary and secondary
 * actions DO is up to the caller, per [ErrorStateKind]:
 *
 * | Kind | Primary | Secondary |
 * |---|---|---|
 * | NOT_FOUND | back to Beranda | open history |
 * | FORBIDDEN | request access | switch account |
 * | SESSION_EXPIRED | sign in again | check the verification email |
 * | OFFLINE | retry the connection | open offline flashcards |
 * | MAINTENANCE | open the server status | schedule a reminder |
 * | QUOTA_EXCEEDED | upgrade to Pro | use a bonus ticket |
 * | VALIDATION | go back to the form | none |
 * | SERVER, UNKNOWN | retry | back to Beranda |
 */
object ErrorStateCopies {
    fun of(error: AppError): ErrorStateCopy =
        when (error) {
            AppError.NotFound -> NOT_FOUND
            AppError.Forbidden -> FORBIDDEN
            AppError.SessionExpired -> SESSION_EXPIRED
            AppError.Offline -> OFFLINE
            is AppError.Maintenance -> maintenance(error.etaMinutes)
            is AppError.QuotaExceeded -> quotaExceeded(error.quota)
            is AppError.Validation -> VALIDATION
            is AppError.Server -> server(error.code)
            is AppError.Unknown -> UNKNOWN
        }

    private val NOT_FOUND =
        ErrorStateCopy(
            kind = ErrorStateKind.NOT_FOUND,
            pose = MikaPose.CONFUSED_MAP,
            statusRes = R.string.state_not_found_status,
            badge = ErrorStateText(R.string.state_not_found_badge),
            speechRes = R.string.state_not_found_speech,
            titleRes = R.string.state_not_found_title,
            body = ErrorStateText(R.string.state_not_found_body),
            primaryCtaRes = R.string.state_action_home,
            secondaryCtaRes = R.string.state_not_found_secondary,
            showSearch = true,
        )

    private val FORBIDDEN =
        ErrorStateCopy(
            kind = ErrorStateKind.FORBIDDEN,
            pose = MikaPose.PADLOCK,
            statusRes = R.string.state_forbidden_status,
            badge = ErrorStateText(R.string.state_forbidden_badge),
            speechRes = R.string.state_forbidden_speech,
            titleRes = R.string.state_forbidden_title,
            body = ErrorStateText(R.string.state_forbidden_body),
            primaryCtaRes = R.string.state_forbidden_primary,
            secondaryCtaRes = R.string.state_forbidden_secondary,
            showSearch = false,
        )

    private val SESSION_EXPIRED =
        ErrorStateCopy(
            kind = ErrorStateKind.SESSION_EXPIRED,
            pose = MikaPose.YAWN_HOURGLASS,
            statusRes = R.string.state_session_status,
            badge = ErrorStateText(R.string.state_session_badge),
            speechRes = R.string.state_session_speech,
            titleRes = R.string.state_session_title,
            body = ErrorStateText(R.string.state_session_body),
            primaryCtaRes = R.string.state_session_primary,
            secondaryCtaRes = R.string.state_session_secondary,
            showSearch = false,
        )

    private val OFFLINE =
        ErrorStateCopy(
            kind = ErrorStateKind.OFFLINE,
            pose = MikaPose.UNPLUGGED,
            statusRes = R.string.state_offline_status,
            badge = ErrorStateText(R.string.state_offline_badge),
            speechRes = R.string.state_offline_speech,
            titleRes = R.string.state_offline_title,
            body = ErrorStateText(R.string.state_offline_body),
            primaryCtaRes = R.string.state_offline_primary,
            secondaryCtaRes = R.string.state_offline_secondary,
            showSearch = false,
        )

    private val VALIDATION =
        ErrorStateCopy(
            kind = ErrorStateKind.VALIDATION,
            pose = MikaPose.THINKING,
            statusRes = R.string.state_validation_status,
            badge = null,
            speechRes = R.string.state_validation_speech,
            titleRes = R.string.state_validation_title,
            body = ErrorStateText(R.string.state_validation_body),
            primaryCtaRes = R.string.state_validation_primary,
            secondaryCtaRes = null,
            showSearch = false,
        )

    private val UNKNOWN =
        ErrorStateCopy(
            kind = ErrorStateKind.UNKNOWN,
            pose = MikaPose.CONFUSED,
            statusRes = R.string.state_unknown_status,
            badge = null,
            speechRes = R.string.state_unknown_speech,
            titleRes = R.string.state_unknown_title,
            body = ErrorStateText(R.string.state_unknown_body),
            primaryCtaRes = R.string.state_action_retry,
            secondaryCtaRes = R.string.state_action_home_short,
            showSearch = false,
        )

    private fun maintenance(etaMinutes: Int?): ErrorStateCopy =
        ErrorStateCopy(
            kind = ErrorStateKind.MAINTENANCE,
            pose = MikaPose.HARDHAT,
            statusRes = R.string.state_maintenance_status,
            badge = ErrorStateText(R.string.state_maintenance_badge),
            speechRes = R.string.state_maintenance_speech,
            titleRes = R.string.state_maintenance_title,
            body =
                if (etaMinutes != null && etaMinutes > 0) {
                    ErrorStateText(R.string.state_maintenance_body_eta, listOf(etaMinutes))
                } else {
                    ErrorStateText(R.string.state_maintenance_body)
                },
            primaryCtaRes = R.string.state_maintenance_primary,
            secondaryCtaRes = R.string.state_maintenance_secondary,
            showSearch = false,
        )

    private fun quotaExceeded(quota: Quota): ErrorStateCopy {
        val daily = quota.period == QuotaPeriod.DAILY
        val ai = quota.kind == QuotaKind.AI_CHAT
        val usage = ErrorStateText(quota.kind.usageRes(), listOf(quota.used))
        val body =
            when {
                quota.used <= 0 && daily -> ErrorStateText(R.string.state_quota_body_daily_unknown)
                quota.used <= 0 -> ErrorStateText(R.string.state_quota_body_monthly_unknown)
                daily -> ErrorStateText(R.string.state_quota_body_daily, listOf(usage))
                else -> ErrorStateText(R.string.state_quota_body_monthly, listOf(usage))
            }
        return ErrorStateCopy(
            kind = ErrorStateKind.QUOTA_EXCEEDED,
            pose = MikaPose.TROPHY,
            statusRes = if (daily) R.string.state_quota_status_daily else R.string.state_quota_status_monthly,
            badge = ErrorStateText(R.string.state_quota_badge),
            speechRes =
                when {
                    daily && ai -> R.string.state_quota_speech_daily_ai
                    daily -> R.string.state_quota_speech_daily
                    else -> R.string.state_quota_speech_monthly
                },
            titleRes =
                when {
                    daily && ai -> R.string.state_quota_title_daily_ai
                    daily -> R.string.state_quota_title_daily
                    ai -> R.string.state_quota_title_monthly_ai
                    else -> R.string.state_quota_title_monthly
                },
            body = body,
            primaryCtaRes = R.string.state_quota_primary,
            secondaryCtaRes = R.string.state_quota_secondary,
            showSearch = false,
        )
    }

    private fun server(code: Int): ErrorStateCopy =
        ErrorStateCopy(
            kind = ErrorStateKind.SERVER,
            pose = MikaPose.WORRIED,
            statusRes = R.string.state_server_status,
            badge = ErrorStateText(R.string.state_server_badge, listOf(code)),
            speechRes = R.string.state_server_speech,
            titleRes = R.string.state_server_title,
            body = ErrorStateText(R.string.state_server_body),
            primaryCtaRes = R.string.state_action_retry,
            secondaryCtaRes = R.string.state_action_home_short,
            showSearch = false,
        )

    @StringRes
    private fun QuotaKind.usageRes(): Int =
        when (this) {
            QuotaKind.AI_CHAT -> R.string.state_quota_usage_ai_chat
            QuotaKind.DOCUMENTS -> R.string.state_quota_usage_documents
            QuotaKind.PAGES -> R.string.state_quota_usage_pages
            QuotaKind.GENERATOR_CREDITS -> R.string.state_quota_usage_generator_credits
        }
}
