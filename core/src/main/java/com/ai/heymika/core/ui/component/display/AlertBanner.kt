package com.ai.heymika.core.ui.component.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.ComicButtonDefaults
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.component.foundation.ComicIconButton
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/** The status of an [InlineAlertBanner]. */
enum class AlertTone { Info, Success, Warning, Error }

/**
 * Colours of an [InlineAlertBanner]: the tinted panel, the title and bare icon ([content]), and the
 * body text. There is no border, so a primary outline always means keyboard focus.
 */
@Immutable
data class AlertBannerColors(
    val container: Color,
    val content: Color,
    val body: Color,
)

object InlineAlertBannerDefaults {
    @Composable
    @ReadOnlyComposable
    fun colors(tone: AlertTone): AlertBannerColors {
        val c = HeyMikaTheme.colors
        return when (tone) {
            AlertTone.Info -> AlertBannerColors(c.info, c.onInfo, c.textPrimary)
            AlertTone.Success -> AlertBannerColors(c.successBg, c.success, c.textPrimary)
            AlertTone.Warning -> AlertBannerColors(c.warningBg, c.warningContent, c.textPrimary)
            AlertTone.Error -> AlertBannerColors(c.dangerBg, c.danger, c.textPrimary)
        }
    }

    fun icon(tone: AlertTone): ImageVector =
        when (tone) {
            AlertTone.Info -> Icons.Outlined.Info
            AlertTone.Success -> Icons.Outlined.CheckCircle
            AlertTone.Warning -> Icons.Outlined.WarningAmber
            AlertTone.Error -> Icons.Outlined.ErrorOutline
        }
}

/**
 * An inline status banner inside a screen, such as the "Sinkronisasi tertunda" warning in the
 * workspace, "Materi berhasil diproses", or an upload error. It is not a toast or a snackbar.
 * - A borderless tinted panel (login mockup alert) with the tone's bare [icon], announced as the
 *   tone ("Peringatan").
 * - Then a bold [title] and an optional [body].
 * - Optional end action: a Ghost button with an arrow ([actionLabel] + [onAction]).
 * - Optional close button ([onDismiss]).
 *
 * The text merges into one TalkBack item, and the buttons stay separately focusable. With [announce]
 * the banner is a polite live region, so TalkBack reads it when it appears or changes.
 */
@Composable
fun InlineAlertBanner(
    tone: AlertTone,
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    icon: ImageVector = InlineAlertBannerDefaults.icon(tone),
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    announce: Boolean = true,
    colors: AlertBannerColors = InlineAlertBannerDefaults.colors(tone),
) {
    val dimens = HeyMikaTheme.dimens
    val typography = HeyMikaTheme.typography
    val shape = HeyMikaTheme.shapes.card
    val toneLabel = alertToneLabel(tone)
    val singleLine = body == null
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    if (announce) liveRegion = LiveRegionMode.Polite
                }.background(colors.container, shape)
                .padding(dimens.cardPaddingCompact),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = toneLabel,
                modifier = Modifier.size(dimens.iconSize),
                tint = colors.content,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceXxs),
            ) {
                Text(
                    text = title,
                    color = colors.content,
                    style = typography.labelMd,
                )
                if (body != null) {
                    Text(
                        text = body,
                        color = colors.body,
                        style = typography.bodySm,
                    )
                }
            }
            if (onDismiss != null) {
                ComicIconButton(
                    icon = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.display_alert_dismiss),
                    onClick = onDismiss,
                    variant = ComicButtonVariant.Ghost,
                    size = ComicButtonSize.Small,
                    colors = ComicButtonDefaults.colors(ComicButtonVariant.Ghost).copy(content = colors.content),
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            ComicButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.align(Alignment.End),
                variant = ComicButtonVariant.Ghost,
                size = ComicButtonSize.Small,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                colors = ComicButtonDefaults.colors(ComicButtonVariant.Ghost).copy(content = colors.content),
            )
        }
    }
}

/**
 * A quiet explanatory note, such as "Mika hanya menjawab dari materimu" or a privacy hint. It is
 * softer than [InlineAlertBanner]: a light panel with a tinted [icon], an optional bold [title], the
 * [body] and an optional Ghost action. [tone] tints the panel and icon; without it the note is neutral
 * grey with a primary icon. It is not a live region.
 */
@Composable
fun InfoNoteCard(
    body: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector = Icons.Outlined.Info,
    tone: DisplayTone? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val typography = HeyMikaTheme.typography
    val shape = HeyMikaTheme.shapes.input
    val spec = tone?.let { PillDefaults.toneSpec(it) }
    val container = spec?.container ?: MaterialTheme.colorScheme.surfaceContainerLow
    val accent = spec?.content ?: colors.primaryText
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {}
                .background(container, shape)
                .padding(dimens.cardPaddingCompact),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm + dimens.spaceXxs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(dimens.iconSize),
            tint = accent,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceXxs),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    style = typography.labelMd,
                )
            }
            Text(
                text = body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = typography.bodySm,
            )
            if (actionLabel != null && onAction != null) {
                ComicButton(
                    text = actionLabel,
                    onClick = onAction,
                    variant = ComicButtonVariant.Ghost,
                    size = ComicButtonSize.Small,
                    colors = ComicButtonDefaults.colors(ComicButtonVariant.Ghost).copy(content = accent),
                )
            }
        }
    }
}

@Composable
@ReadOnlyComposable
private fun alertToneLabel(tone: AlertTone): String =
    stringResource(
        when (tone) {
            AlertTone.Info -> R.string.display_alert_info
            AlertTone.Success -> R.string.display_alert_success
            AlertTone.Warning -> R.string.display_alert_warning
            AlertTone.Error -> R.string.display_alert_error
        },
    )

@Preview(name = "InlineAlertBanner", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun InlineAlertBannerPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InlineAlertBanner(
                tone = AlertTone.Info,
                title = "Mika sedang membaca materimu",
                body = "Ringkasan akan muncul dalam 1-2 menit.",
            )
            InlineAlertBanner(
                tone = AlertTone.Success,
                title = "Materi berhasil diproses",
                onDismiss = {},
            )
            InlineAlertBanner(
                tone = AlertTone.Warning,
                title = "Sinkronisasi tertunda",
                body = "2 file belum tersinkron. Periksa koneksi internetmu.",
                actionLabel = "Coba lagi",
                onAction = {},
                onDismiss = {},
            )
            InlineAlertBanner(
                tone = AlertTone.Error,
                title = "Gagal mengunggah file",
                body = "Ukuran file melebihi 20 MB.",
                actionLabel = "Pilih file lain",
                onAction = {},
            )
        }
    }
}

@Preview(name = "InfoNoteCard", showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360)
@Composable
private fun InfoNoteCardPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InfoNoteCard(body = "Mika hanya menjawab berdasarkan materi yang kamu unggah.")
            InfoNoteCard(
                title = "Tips belajar",
                body = "Ulangi flashcard yang salah besok pagi agar lebih melekat.",
                icon = Icons.Outlined.Lightbulb,
                tone = DisplayTone.Yellow,
            )
            InfoNoteCard(
                title = "Privasi terjaga",
                body = "File kamu hanya bisa diakses oleh akunmu sendiri.",
                icon = Icons.Outlined.PrivacyTip,
                tone = DisplayTone.Sky,
                actionLabel = "Pelajari",
                onAction = {},
            )
        }
    }
}
