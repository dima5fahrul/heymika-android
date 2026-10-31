package com.ai.heymika.core.ui.component.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.ComicButtonDefaults
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.component.foundation.ComicIconButton
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/** The intent of a toast, which sets its leading icon and the icon's tint. */
enum class HeyMikaSnackbarTone { Info, Success, Warning, Error }

/**
 * [SnackbarVisuals] with a [tone]. Show it through [showHeyMikaSnackbar] on the host state that
 * [HeyMikaSnackbarHost] renders. A message with an action defaults to [SnackbarDuration.Long].
 */
@Immutable
data class HeyMikaSnackbarVisuals(
    override val message: String,
    val tone: HeyMikaSnackbarTone = HeyMikaSnackbarTone.Info,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration =
        if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Long,
) : SnackbarVisuals

/** Shows a HeyMika toast and suspends until it is dismissed or its action is pressed. */
suspend fun SnackbarHostState.showHeyMikaSnackbar(
    message: String,
    tone: HeyMikaSnackbarTone = HeyMikaSnackbarTone.Info,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Long,
): SnackbarResult =
    showSnackbar(
        HeyMikaSnackbarVisuals(
            message = message,
            tone = tone,
            actionLabel = actionLabel,
            withDismissAction = withDismissAction,
            duration = duration,
        ),
    )

/**
 * The app-wide [SnackbarHostState]. HeyMikaRoot provides the instance its [HeyMikaSnackbarHost]
 * shows, so any screen, including those in dynamic feature modules, can call
 * `LocalHeyMikaSnackbarHostState.current.showHeyMikaSnackbar(...)`. Reading it without a provider
 * fails fast: a detached state would suspend `showSnackbar` forever and show nothing.
 */
val LocalHeyMikaSnackbarHostState =
    staticCompositionLocalOf<SnackbarHostState> {
        error("LocalHeyMikaSnackbarHostState not provided: wrap the content in HeyMikaRootFrame")
    }

/**
 * The app-wide toast host (mounted once in the root, above the bottom bar). Material's host keeps
 * the queue, the accessibility-aware timeout, the polite live region and the dismiss action; every
 * message is drawn as a [HeyMikaSnackbar].
 */
@Composable
fun HeyMikaSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        HeyMikaSnackbar(data = data)
    }
}

/**
 * The HeyMika toast (system-states mockup): an inverse-surface #113059 card with 16dp corners and a
 * soft shadow, no border or hard shadow. A tone icon (brandYellow for Info and Success, named for
 * TalkBack), the message in inverseOnSurface, an optional inversePrimary text action and an
 * optional close button. Visuals that are not [HeyMikaSnackbarVisuals] render with the Info tone.
 */
@Composable
fun HeyMikaSnackbar(
    data: SnackbarData,
    modifier: Modifier = Modifier,
) {
    val visuals = data.visuals
    HeyMikaSnackbarContent(
        message = visuals.message,
        tone = (visuals as? HeyMikaSnackbarVisuals)?.tone ?: HeyMikaSnackbarTone.Info,
        modifier = modifier,
        actionLabel = visuals.actionLabel,
        onAction = data::performAction,
        onDismiss = if (visuals.withDismissAction) data::dismiss else null,
    )
}

@Composable
internal fun HeyMikaSnackbarContent(
    message: String,
    tone: HeyMikaSnackbarTone,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    onDismiss: (() -> Unit)? = null,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val scheme = MaterialTheme.colorScheme
    val shape = HeyMikaTheme.shapes.card
    val hasTrailing = actionLabel != null || onDismiss != null
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.screenMargin, vertical = dimens.spaceSm)
                .shadow(elevation = dimens.spaceSm, shape = shape, spotColor = colors.subtleShadow)
                .background(scheme.inverseSurface, shape)
                .heightIn(min = dimens.minTouchTarget + dimens.spaceSm)
                .padding(
                    start = dimens.cardPadding,
                    end = if (hasTrailing) dimens.spaceXs else dimens.cardPadding,
                    top = dimens.spaceXs,
                    bottom = dimens.spaceXs,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
    ) {
        Icon(
            imageVector = tone.icon(),
            contentDescription = tone.label(),
            tint = tone.iconTint(),
            modifier = Modifier.size(dimens.iconSize),
        )
        Text(
            text = message,
            style = HeyMikaTheme.typography.bodySm,
            color = scheme.inverseOnSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(vertical = dimens.spaceSmPlus),
        )
        if (actionLabel != null) {
            ComicButton(
                text = actionLabel,
                onClick = onAction,
                variant = ComicButtonVariant.Ghost,
                size = ComicButtonSize.Small,
                colors = ComicButtonDefaults.colors(ComicButtonVariant.Ghost).copy(content = scheme.inversePrimary),
            )
        }
        if (onDismiss != null) {
            ComicIconButton(
                icon = Icons.Filled.Close,
                contentDescription = stringResource(R.string.chrome_snackbar_dismiss),
                onClick = onDismiss,
                size = ComicButtonSize.Small,
                colors = ComicButtonDefaults.colors(ComicButtonVariant.Ghost).copy(content = scheme.inverseOnSurface),
            )
        }
    }
}

/** Icon tints that stay readable (>= 3:1) on the inverse surface #113059. */
@Composable
@ReadOnlyComposable
private fun HeyMikaSnackbarTone.iconTint(): Color =
    when (this) {
        HeyMikaSnackbarTone.Info, HeyMikaSnackbarTone.Success -> HeyMikaTheme.colors.yellow
        HeyMikaSnackbarTone.Warning -> HeyMikaTheme.colors.accent
        HeyMikaSnackbarTone.Error -> HeyMikaTheme.colors.inverseDanger
    }

private fun HeyMikaSnackbarTone.icon(): ImageVector =
    when (this) {
        HeyMikaSnackbarTone.Info -> Icons.Outlined.Info
        HeyMikaSnackbarTone.Success -> Icons.Outlined.CheckCircle
        HeyMikaSnackbarTone.Warning -> Icons.Outlined.WarningAmber
        HeyMikaSnackbarTone.Error -> Icons.Outlined.ErrorOutline
    }

@Composable
private fun HeyMikaSnackbarTone.label(): String =
    stringResource(
        when (this) {
            HeyMikaSnackbarTone.Info -> R.string.chrome_snackbar_tone_info
            HeyMikaSnackbarTone.Success -> R.string.chrome_snackbar_tone_success
            HeyMikaSnackbarTone.Warning -> R.string.chrome_snackbar_tone_warning
            HeyMikaSnackbarTone.Error -> R.string.chrome_snackbar_tone_error
        },
    )

@Preview(name = "Tones", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun HeyMikaSnackbarTonesPreview() {
    HeyMikaTheme {
        Column {
            HeyMikaSnackbarContent(message = "Mika sedang merangkum materimu.", tone = HeyMikaSnackbarTone.Info)
            HeyMikaSnackbarContent(message = "Workspace berhasil dibuat!", tone = HeyMikaSnackbarTone.Success)
            HeyMikaSnackbarContent(
                message = "Kuota AI tinggal 15% hari ini.",
                tone = HeyMikaSnackbarTone.Warning,
                actionLabel = "Lihat",
            )
            HeyMikaSnackbarContent(
                message = "Gagal mengunggah file. Periksa koneksimu lalu coba lagi.",
                tone = HeyMikaSnackbarTone.Error,
                actionLabel = "Coba Lagi",
                onDismiss = {},
            )
        }
    }
}

@Preview(name = "Action and close", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun HeyMikaSnackbarPreview() {
    HeyMikaTheme {
        val data =
            remember {
                object : SnackbarData {
                    override val visuals: SnackbarVisuals =
                        HeyMikaSnackbarVisuals(
                            message = "Notifikasi dihapus.",
                            tone = HeyMikaSnackbarTone.Info,
                            actionLabel = "Urungkan",
                            withDismissAction = true,
                        )

                    override fun performAction() = Unit

                    override fun dismiss() = Unit
                }
            }
        HeyMikaSnackbar(data = data)
    }
}

@Preview(name = "Host (interactive mode)", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun HeyMikaSnackbarHostPreview() {
    HeyMikaTheme {
        val hostState = remember { SnackbarHostState() }
        LaunchedEffect(hostState) {
            hostState.showHeyMikaSnackbar(
                message = "Pengingat ditunda 1 jam.",
                tone = HeyMikaSnackbarTone.Success,
                actionLabel = "Urungkan",
            )
        }
        HeyMikaSnackbarHost(hostState = hostState)
    }
}
