package com.ai.heymika.core.ui.component.state

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.domain.model.AppError
import com.ai.heymika.core.domain.model.Quota
import com.ai.heymika.core.domain.model.QuotaKind
import com.ai.heymika.core.domain.model.QuotaPeriod
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.component.foundation.ComicCard
import com.ai.heymika.core.ui.component.foundation.ComicCardVariant
import com.ai.heymika.core.ui.component.mascot.HalftoneBackground
import com.ai.heymika.core.ui.component.mascot.MikaAvatar
import com.ai.heymika.core.ui.component.mascot.MikaBubbleTail
import com.ai.heymika.core.ui.component.mascot.MikaSpeechBubble
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.core.ui.theme.ToneSpec
import com.ai.heymika.core.utils.state.ErrorStateCopies
import com.ai.heymika.core.utils.state.ErrorStateCopy
import com.ai.heymika.core.utils.state.ErrorStateKind
import com.ai.heymika.core.utils.state.ErrorStateText

/**
 * Mika explains a failure and always offers a way out (catalog principles: no blame, a clear exit,
 * data kept safe). The copy, pose and status pill come from [ErrorStateCopies]; what each action
 * does is the caller's business, so navigation stays out of :core (see [ErrorStateCopies] for the
 * meaning of primary and secondary per kind).
 *
 * - [onSecondary] and [onReportBug] are hidden when null (and the secondary also when the state
 *   has no secondary copy, as for Validation).
 * - [searchContent] is the 404 quick-search slot (a search field and its "Cari" button); it is
 *   shown under its label only for states that ask for it.
 * - [primaryLoading] turns the primary button into its loading state, e.g. while reconnecting.
 */
@Composable
fun MikaErrorState(
    error: AppError,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    variant: MikaStateVariant = MikaStateVariant.FullScreen,
    onSecondary: (() -> Unit)? = null,
    onReportBug: (() -> Unit)? = null,
    primaryLoading: Boolean = false,
    searchContent: (@Composable () -> Unit)? = null,
) {
    val copy = remember(error) { ErrorStateCopies.of(error) }
    MikaErrorState(
        copy = copy,
        onPrimary = onPrimary,
        modifier = modifier,
        variant = variant,
        onSecondary = onSecondary,
        onReportBug = onReportBug,
        primaryLoading = primaryLoading,
        searchContent = searchContent,
    )
}

/** [MikaErrorState] for custom copy, e.g. `ErrorStateCopies.of(error).copy(titleRes = ...)`. */
@Composable
fun MikaErrorState(
    copy: ErrorStateCopy,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    variant: MikaStateVariant = MikaStateVariant.FullScreen,
    onSecondary: (() -> Unit)? = null,
    onReportBug: (() -> Unit)? = null,
    primaryLoading: Boolean = false,
    searchContent: (@Composable () -> Unit)? = null,
) {
    when (variant) {
        MikaStateVariant.FullScreen -> {
            FullScreenErrorState(
                copy = copy,
                onPrimary = onPrimary,
                onSecondary = onSecondary,
                onReportBug = onReportBug,
                primaryLoading = primaryLoading,
                searchContent = searchContent,
                modifier = modifier,
            )
        }

        MikaStateVariant.Inline -> {
            InlineErrorState(
                copy = copy,
                onPrimary = onPrimary,
                onSecondary = onSecondary,
                onReportBug = onReportBug,
                primaryLoading = primaryLoading,
                searchContent = searchContent,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun FullScreenErrorState(
    copy: ErrorStateCopy,
    onPrimary: () -> Unit,
    onSecondary: (() -> Unit)?,
    onReportBug: (() -> Unit)?,
    primaryLoading: Boolean,
    searchContent: (@Composable () -> Unit)?,
    modifier: Modifier,
) {
    val dimens = HeyMikaTheme.dimens
    val typography = HeyMikaTheme.typography
    HalftoneBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(dimens.screenMargin),
            verticalArrangement = Arrangement.Center,
        ) {
            ComicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = HeyMikaTheme.shapes.cardLarge,
            ) {
                StatusHeader(copy = copy)
                Spacer(modifier = Modifier.height(dimens.spaceMd))
                MikaAvatar(
                    pose = copy.pose,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    size = dimens.avatarHero,
                    badge = stringResource(R.string.mascot_badge_nyaa),
                )
                Spacer(modifier = Modifier.height(dimens.spaceSm))
                MikaSpeechBubble(
                    modifier = Modifier.fillMaxWidth(),
                    tail = MikaBubbleTail.Top,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubble,
                            contentDescription = null,
                            tint = HeyMikaTheme.colors.accent,
                            modifier = Modifier.size(dimens.iconSizeLarge),
                        )
                        Text(
                            text = stringResource(copy.speechRes),
                            style = typography.labelLg.copy(fontWeight = FontWeight.ExtraBold),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(dimens.spaceMd))
                Text(
                    text = stringResource(copy.titleRes),
                    modifier = Modifier.semantics { heading() },
                    style = typography.headlineMd,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(dimens.spaceXs))
                Text(
                    text = copy.body.resolve(),
                    style = typography.bodyMd,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (copy.showSearch && searchContent != null) {
                    SearchSlot(content = searchContent)
                }
                Spacer(modifier = Modifier.height(dimens.spaceLg))
                ErrorActions(
                    copy = copy,
                    onPrimary = onPrimary,
                    onSecondary = onSecondary,
                    onReportBug = onReportBug,
                    primaryLoading = primaryLoading,
                    compact = false,
                )
            }
        }
    }
}

@Composable
private fun InlineErrorState(
    copy: ErrorStateCopy,
    onPrimary: () -> Unit,
    onSecondary: (() -> Unit)?,
    onReportBug: (() -> Unit)?,
    primaryLoading: Boolean,
    searchContent: (@Composable () -> Unit)?,
    modifier: Modifier,
) {
    val dimens = HeyMikaTheme.dimens
    ComicCard(
        modifier = modifier.fillMaxWidth(),
        variant = ComicCardVariant.Subtle,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus)) {
            MikaAvatar(pose = copy.pose, size = dimens.avatarLg)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
            ) {
                StatusPill(copy = copy)
                Text(
                    text = stringResource(copy.titleRes),
                    modifier = Modifier.semantics { heading() },
                    style = HeyMikaTheme.typography.headlineSm,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = copy.body.resolve(),
                    style = HeyMikaTheme.typography.bodySm,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (copy.showSearch && searchContent != null) {
            SearchSlot(content = searchContent)
        }
        Spacer(modifier = Modifier.height(dimens.spaceMd))
        ErrorActions(
            copy = copy,
            onPrimary = onPrimary,
            onSecondary = onSecondary,
            onReportBug = onReportBug,
            primaryLoading = primaryLoading,
            compact = true,
        )
    }
}

@Composable
private fun StatusHeader(copy: ErrorStateCopy) {
    Column(verticalArrangement = Arrangement.spacedBy(HeyMikaTheme.dimens.spaceXs)) {
        StatusPill(copy = copy)
        copy.badge?.let { badge -> CodeBadge(text = badge.resolve()) }
    }
}

@Composable
private fun StatusPill(copy: ErrorStateCopy) {
    val tone = copy.kind.pillColors()
    val dimens = HeyMikaTheme.dimens
    Row(
        modifier =
            Modifier
                .background(tone.container, HeyMikaTheme.shapes.pill)
                .padding(horizontal = dimens.spaceSmPlus, vertical = dimens.spaceXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs),
    ) {
        Icon(
            imageVector = copy.kind.statusIcon(),
            contentDescription = null,
            tint = tone.content,
            modifier = Modifier.size(dimens.iconSizeSmall),
        )
        Text(
            text = stringResource(copy.statusRes),
            style = HeyMikaTheme.typography.labelSm,
            color = tone.content,
        )
    }
}

@Composable
private fun CodeBadge(text: String) {
    val dimens = HeyMikaTheme.dimens
    Text(
        text = text,
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, HeyMikaTheme.shapes.pill)
                .padding(horizontal = dimens.spaceSm, vertical = dimens.spaceXxs),
        style = HeyMikaTheme.typography.labelSm,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun SearchSlot(content: @Composable () -> Unit) {
    val dimens = HeyMikaTheme.dimens
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
    ) {
        Text(
            text = stringResource(R.string.state_not_found_search_label),
            style = HeyMikaTheme.typography.labelSm,
            color = HeyMikaTheme.colors.textMuted,
        )
        content()
    }
}

/**
 * The primary CTA at full width, then the secondary CTA and "Lapor Bug" side by side as soft
 * Tonal buttons (system-states mockup grid). Whichever of the two is present alone takes the
 * full width.
 */
@Composable
private fun ErrorActions(
    copy: ErrorStateCopy,
    onPrimary: () -> Unit,
    onSecondary: (() -> Unit)?,
    onReportBug: (() -> Unit)?,
    primaryLoading: Boolean,
    compact: Boolean,
) {
    val dimens = HeyMikaTheme.dimens
    val secondaryRes = copy.secondaryCtaRes
    val showSecondary = secondaryRes != null && onSecondary != null
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimens.spaceSm),
    ) {
        ComicButton(
            text = stringResource(copy.primaryCtaRes),
            onClick = onPrimary,
            size = if (compact) ComicButtonSize.Medium else ComicButtonSize.Large,
            loading = primaryLoading,
            fullWidth = true,
            leadingIcon = copy.kind.primaryIcon(),
        )
        if (showSecondary || onReportBug != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
            ) {
                if (secondaryRes != null && onSecondary != null) {
                    TonalAction(
                        text = stringResource(secondaryRes),
                        icon = copy.kind.secondaryIcon(),
                        onClick = onSecondary,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (onReportBug != null) {
                    TonalAction(
                        text = stringResource(R.string.state_action_report_bug),
                        icon = Icons.Filled.ReportProblem,
                        onClick = onReportBug,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** A half-width Tonal button whose label may wrap to two lines ("Buka Flashcard Offline"). */
@Composable
private fun TonalAction(
    text: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    ComicButton(
        onClick = onClick,
        modifier = modifier,
        variant = ComicButtonVariant.Tonal,
        size = ComicButtonSize.Medium,
        fullWidth = true,
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(HeyMikaTheme.dimens.iconSize))
        }
        Text(
            text = text,
            style = HeyMikaTheme.typography.buttonSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Resolves the resource and its arguments; nested [ErrorStateText] arguments resolve first. */
@Composable
@ReadOnlyComposable
internal fun ErrorStateText.resolve(): String {
    if (args.isEmpty()) return stringResource(resId)
    val formatArgs = ArrayList<Any>(args.size)
    for (arg in args) {
        formatArgs.add(if (arg is ErrorStateText) arg.resolve() else arg)
    }
    return stringResource(resId, *formatArgs.toTypedArray())
}

/**
 * The saturated status pill of each state (system-states mockup badges), with a label colour that
 * meets 4.5:1. Errors never use ribbonRed: 404, Server and Unknown take danger. Quota is urgency,
 * not an error, so it is red, but the darker ribbonRed tone (#B3261E) keeps white text readable.
 */
@Composable
@ReadOnlyComposable
private fun ErrorStateKind.pillColors(): ToneSpec {
    val colors = HeyMikaTheme.colors
    val scheme = MaterialTheme.colorScheme
    val tones = HeyMikaTheme.tones
    return when (this) {
        ErrorStateKind.NOT_FOUND, ErrorStateKind.SERVER, ErrorStateKind.UNKNOWN -> {
            ToneSpec(colors.danger, colors.onDanger, colors.danger)
        }

        ErrorStateKind.FORBIDDEN, ErrorStateKind.VALIDATION -> {
            ToneSpec(scheme.secondary, scheme.onSecondary, scheme.secondary)
        }

        ErrorStateKind.SESSION_EXPIRED -> {
            ToneSpec(colors.textMuted, colors.surface, colors.textMuted)
        }

        ErrorStateKind.OFFLINE -> {
            ToneSpec(scheme.tertiary, scheme.onTertiary, scheme.tertiary)
        }

        ErrorStateKind.MAINTENANCE -> {
            ToneSpec(colors.accent, scheme.onSecondaryContainer, colors.accent)
        }

        ErrorStateKind.QUOTA_EXCEEDED -> {
            ToneSpec(tones.ribbonRed.content, colors.surface, colors.ribbonRed)
        }
    }
}

private fun ErrorStateKind.statusIcon(): ImageVector =
    when (this) {
        ErrorStateKind.NOT_FOUND -> Icons.Filled.Error
        ErrorStateKind.FORBIDDEN -> Icons.Filled.Lock
        ErrorStateKind.SESSION_EXPIRED -> Icons.Filled.TimerOff
        ErrorStateKind.OFFLINE -> Icons.Filled.WifiOff
        ErrorStateKind.MAINTENANCE -> Icons.Filled.Build
        ErrorStateKind.QUOTA_EXCEEDED -> Icons.Filled.Bolt
        ErrorStateKind.VALIDATION, ErrorStateKind.UNKNOWN -> Icons.Filled.ErrorOutline
        ErrorStateKind.SERVER -> Icons.Filled.CloudOff
    }

private fun ErrorStateKind.primaryIcon(): ImageVector =
    when (this) {
        ErrorStateKind.NOT_FOUND -> Icons.Filled.Home
        ErrorStateKind.FORBIDDEN -> Icons.Filled.Key
        ErrorStateKind.SESSION_EXPIRED -> Icons.Filled.LockOpen
        ErrorStateKind.OFFLINE -> Icons.Filled.Sync
        ErrorStateKind.MAINTENANCE -> Icons.Filled.Insights
        ErrorStateKind.QUOTA_EXCEEDED -> Icons.Filled.Star
        ErrorStateKind.VALIDATION -> Icons.Filled.Edit
        ErrorStateKind.SERVER, ErrorStateKind.UNKNOWN -> Icons.Filled.Refresh
    }

private fun ErrorStateKind.secondaryIcon(): ImageVector? =
    when (this) {
        ErrorStateKind.NOT_FOUND -> Icons.Filled.History
        ErrorStateKind.FORBIDDEN -> Icons.Filled.SwitchAccount
        ErrorStateKind.SESSION_EXPIRED -> Icons.Filled.Mail
        ErrorStateKind.OFFLINE -> Icons.Filled.Style
        ErrorStateKind.MAINTENANCE -> Icons.Filled.NotificationsActive
        ErrorStateKind.QUOTA_EXCEEDED -> Icons.Filled.ConfirmationNumber
        ErrorStateKind.SERVER, ErrorStateKind.UNKNOWN -> Icons.Filled.Home
        ErrorStateKind.VALIDATION -> null
    }

internal class AppErrorPreviewProvider : PreviewParameterProvider<AppError> {
    override val values: Sequence<AppError> =
        sequenceOf(
            AppError.NotFound,
            AppError.Forbidden,
            AppError.SessionExpired,
            AppError.Offline,
            AppError.Maintenance(etaMinutes = 15),
            AppError.QuotaExceeded(
                Quota(kind = QuotaKind.AI_CHAT, used = 50, limit = 50, period = QuotaPeriod.DAILY, resetAt = null),
            ),
            AppError.Validation(field = "email", code = "invalid"),
            AppError.Server(code = 500),
            AppError.Unknown(cause = null),
        )
}

/** Stand-in for the controls agent's search field, previews only. */
@Composable
private fun PreviewSearchField() {
    val dimens = HeyMikaTheme.dimens
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = dimens.minTouchTarget)
                .border(dimens.borderControl, HeyMikaTheme.colors.borderMuted, HeyMikaTheme.shapes.input)
                .padding(horizontal = dimens.spaceSmPlus),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = HeyMikaTheme.colors.textMuted,
            modifier = Modifier.size(dimens.iconSize),
        )
        Text(
            text = stringResource(R.string.state_not_found_search_placeholder),
            style = HeyMikaTheme.typography.bodySm,
            color = HeyMikaTheme.colors.textMuted,
        )
    }
}

@Preview(
    name = "Full screen",
    showBackground = true,
    backgroundColor = 0xFFF8FCFF,
    widthDp = 360,
    heightDp = 1100,
)
@Composable
private fun MikaErrorStateFullScreenPreview(
    @PreviewParameter(AppErrorPreviewProvider::class) error: AppError,
) {
    HeyMikaTheme {
        MikaErrorState(
            error = error,
            onPrimary = {},
            onSecondary = {},
            onReportBug = {},
            searchContent = { PreviewSearchField() },
        )
    }
}

@Preview(name = "Inline", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360, heightDp = 1300)
@Composable
private fun MikaErrorStateInlinePreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MikaErrorState(
                error = AppError.Offline,
                onPrimary = {},
                variant = MikaStateVariant.Inline,
                onSecondary = {},
            )
            MikaErrorState(
                error = AppError.Server(code = 502),
                onPrimary = {},
                variant = MikaStateVariant.Inline,
                onReportBug = {},
            )
            MikaErrorState(
                error =
                    AppError.QuotaExceeded(
                        Quota(
                            kind = QuotaKind.DOCUMENTS,
                            used = 20,
                            limit = 20,
                            period = QuotaPeriod.MONTHLY,
                            resetAt = null,
                        ),
                    ),
                onPrimary = {},
                variant = MikaStateVariant.Inline,
                onSecondary = {},
            )
            MikaErrorState(
                error = AppError.Validation(field = "email", code = "invalid"),
                onPrimary = {},
                variant = MikaStateVariant.Inline,
            )
        }
    }
}

@Preview(
    name = "Primary loading, no ETA",
    showBackground = true,
    backgroundColor = 0xFFF8FCFF,
    widthDp = 360,
    heightDp = 1000,
)
@Composable
private fun MikaErrorStateLoadingPreview() {
    HeyMikaTheme {
        MikaErrorState(
            error = AppError.Maintenance(etaMinutes = null),
            onPrimary = {},
            primaryLoading = true,
        )
    }
}
