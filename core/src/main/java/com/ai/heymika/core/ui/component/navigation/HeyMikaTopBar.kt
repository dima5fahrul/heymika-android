package com.ai.heymika.core.ui.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.component.foundation.ComicButtonDefaults
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.component.foundation.ComicIconButton
import com.ai.heymika.core.ui.component.foundation.SubtleDivider
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * - [Solid]: surface background with a bottom hairline. Stack and tab screens.
 * - [Transparent]: no background or hairline; the back and action buttons sit on white circles so
 *   they stay legible over a hero image or banner. Switch to [Solid] once the hero scrolls away.
 */
enum class HeyMikaTopBarStyle { Solid, Transparent }

/** Lets [TopBarIconButton] pick the flat (Solid) or floating-circle (Transparent) look. */
internal val LocalTopBarStyle = staticCompositionLocalOf { HeyMikaTopBarStyle.Solid }

/**
 * The 64dp HeyMika top bar, padded for the status bar.
 *
 * Leading slot, in order of precedence: [navigationContent] (the logo on a tab root), a
 * [BackPillButton] when both [onBack] and [backLabel] are set ("← Materimu"), a back arrow when only
 * [onBack] is set, or nothing. The [title] is a single-line headlineSm heading that ellipsizes,
 * never a logo, so long titles never clip. [actions] are usually [TopBarIconButton]s (at most two
 * plus an avatar).
 */
@Composable
fun HeyMikaTopBar(
    title: String?,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backLabel: String? = null,
    style: HeyMikaTopBarStyle = HeyMikaTopBarStyle.Solid,
    navigationContent: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = WindowInsets.statusBars,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val solid = style == HeyMikaTopBarStyle.Solid
    CompositionLocalProvider(LocalTopBarStyle provides style) {
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(if (solid) colors.surface else Color.Transparent)
                    .windowInsetsPadding(windowInsets),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(dimens.topBarHeight)
                        .padding(horizontal = dimens.spaceXs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs),
            ) {
                when {
                    navigationContent != null -> {
                        navigationContent()
                    }

                    onBack != null && backLabel != null -> {
                        BackPillButton(
                            label = backLabel,
                            onClick = onBack,
                            modifier = Modifier.padding(start = dimens.spaceXs),
                        )
                    }

                    onBack != null -> {
                        TopBarIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.chrome_back),
                            onClick = onBack,
                        )
                    }

                    else -> {
                        Spacer(modifier = Modifier.width(dimens.spaceSm))
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = HeyMikaTheme.typography.headlineSm,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.semantics { heading() },
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions,
                )
            }
            if (solid) SubtleDivider()
        }
    }
}

/**
 * An icon action for [HeyMikaTopBar]: a flat ink icon in a 48dp circle on a Solid bar, a white
 * bordered circle on a Transparent bar. [showBadgeDot] adds the ribbonRed unread dot and appends
 * "ada yang baru" to [contentDescription].
 */
@Composable
fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBadgeDot: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val floating = LocalTopBarStyle.current == HeyMikaTopBarStyle.Transparent
    val ghost = ComicButtonDefaults.colors(ComicButtonVariant.Ghost)
    val buttonColors =
        if (floating) {
            ghost.copy(
                container = colors.surface,
                content = colors.textPrimary,
                border = colors.borderMuted,
                disabledContainer = colors.surface,
            )
        } else {
            ghost.copy(content = colors.textPrimary)
        }
    val description =
        if (showBadgeDot) stringResource(R.string.chrome_with_badge, contentDescription) else contentDescription
    Box(modifier = modifier) {
        ComicIconButton(
            icon = icon,
            contentDescription = description,
            onClick = onClick,
            size = if (floating) ComicButtonSize.Small else ComicButtonSize.Medium,
            enabled = enabled,
            colors = buttonColors,
        )
        if (showBadgeDot) {
            BadgeDot(
                color = colors.ribbonRed,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = dimens.spaceSm, end = dimens.spaceSm),
            )
        }
    }
}

@Preview(name = "Stack: back, title, actions", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun HeyMikaTopBarStackPreview() {
    HeyMikaTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            HeyMikaTopBar(
                title = "Mengerjakan Kuis",
                onBack = {},
                actions = {
                    TopBarIconButton(icon = Icons.Outlined.BookmarkBorder, contentDescription = "Simpan", onClick = {})
                    TopBarIconButton(
                        icon = Icons.Outlined.Notifications,
                        contentDescription = "Notifikasi",
                        onClick = {},
                        showBadgeDot = true,
                    )
                },
            )
            HeyMikaTopBar(
                title = "Percakapan AI Tutor: Biologi Kedokteran Bab 4 Metabolisme",
                onBack = {},
                actions = {
                    TopBarIconButton(icon = Icons.Filled.MoreVert, contentDescription = "Opsi lainnya", onClick = {})
                },
            )
            HeyMikaTopBar(
                title = "Detail Materi",
                onBack = {},
                backLabel = "Materimu",
                actions = {
                    TopBarIconButton(icon = Icons.Filled.MoreVert, contentDescription = "Opsi lainnya", onClick = {})
                },
            )
        }
    }
}

@Preview(name = "Tab root and no navigation", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun HeyMikaTopBarTabPreview() {
    HeyMikaTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            HeyMikaTopBar(
                title = null,
                navigationContent = {
                    Text(
                        text = "HeyMika",
                        style = HeyMikaTheme.typography.headlineSm,
                        color = HeyMikaTheme.colors.primaryText,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                },
                actions = {
                    TopBarIconButton(
                        icon = Icons.Outlined.Notifications,
                        contentDescription = "Notifikasi",
                        onClick = {},
                        showBadgeDot = true,
                    )
                },
            )
            HeyMikaTopBar(title = "Pusat Notifikasi")
        }
    }
}

@Preview(name = "Transparent over hero", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun HeyMikaTopBarTransparentPreview() {
    HeyMikaTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.background(HeyMikaTheme.colors.sky)) {
                HeyMikaTopBar(
                    title = null,
                    onBack = {},
                    style = HeyMikaTopBarStyle.Transparent,
                    actions = {
                        TopBarIconButton(
                            icon = Icons.Outlined.BookmarkBorder,
                            contentDescription = "Simpan",
                            onClick = {},
                        )
                        TopBarIconButton(
                            icon = Icons.Outlined.Notifications,
                            contentDescription = "Notifikasi",
                            onClick = {},
                            showBadgeDot = true,
                        )
                    },
                )
            }
            Box(modifier = Modifier.background(HeyMikaTheme.colors.yellow)) {
                HeyMikaTopBar(
                    title = null,
                    onBack = {},
                    backLabel = "Materimu",
                    style = HeyMikaTopBarStyle.Transparent,
                    actions = {
                        TopBarIconButton(
                            icon = Icons.Filled.MoreVert,
                            contentDescription = "Opsi lainnya",
                            onClick = {},
                            enabled = false,
                        )
                    },
                )
            }
        }
    }
}
