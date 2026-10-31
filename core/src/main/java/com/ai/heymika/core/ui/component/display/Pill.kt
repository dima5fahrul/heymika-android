package com.ai.heymika.core.ui.component.display

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/** The leading dot of a [Pill]: none, static, or pulsing for live states (static under reduced motion). */
enum class PillDot { None, Static, Pulsing }

/**
 * A full-radius pill: status pills ("78% Selesai", "Tersimpan v2.1"), hero pills ("REKOMENDASI MIKA",
 * Cyan + uppercase + sparkle icon) and counters ("6 Topik Aktif").
 *
 * The pill is single-line with an ellipsis and has no fixed height. It is display-only: the
 * interactive pills are [FilterChipRow] and [PillTabRow]. To render a domain tone map, pass
 * `colors = PillDefaults.colors(toneSpec, style)`.
 */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    tone: DisplayTone = DisplayTone.Primary,
    style: PillStyle = PillStyle.Tinted,
    size: PillSize = PillSize.Medium,
    icon: ImageVector? = null,
    dot: PillDot = PillDot.None,
    uppercase: Boolean = false,
    colors: PillColors = PillDefaults.colors(tone, style),
) {
    val dimens = HeyMikaTheme.dimens
    ToneLabel(
        text = text,
        modifier = modifier,
        shape = HeyMikaTheme.shapes.pill,
        horizontalPadding = if (size == PillSize.Small) dimens.spaceSm else dimens.spaceSmPlus,
        size = size,
        colors = colors,
        icon = icon,
        dot = dot,
        uppercase = uppercase,
    )
}

/**
 * A 6dp-radius tag: subject and category tags ("Kuis Interaktif", "#Matematika"), difficulty tags
 * (Neutral), code badges and kickers ("FOKUS UTAMA" is Neutral + Filled + uppercase). It uses the
 * same colours and rules as [Pill].
 */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    tone: DisplayTone = DisplayTone.Primary,
    style: PillStyle = PillStyle.Tinted,
    size: PillSize = PillSize.Small,
    icon: ImageVector? = null,
    uppercase: Boolean = false,
    colors: PillColors = PillDefaults.colors(tone, style),
) {
    val dimens = HeyMikaTheme.dimens
    ToneLabel(
        text = text,
        modifier = modifier,
        shape = HeyMikaTheme.shapes.tag,
        horizontalPadding = if (size == PillSize.Small) dimens.spaceXs + dimens.spaceXxs else dimens.spaceSm,
        size = size,
        colors = colors,
        icon = icon,
        dot = PillDot.None,
        uppercase = uppercase,
    )
}

@Composable
private fun ToneLabel(
    text: String,
    modifier: Modifier,
    shape: Shape,
    horizontalPadding: Dp,
    size: PillSize,
    colors: PillColors,
    icon: ImageVector?,
    dot: PillDot,
    uppercase: Boolean,
) {
    val dimens = HeyMikaTheme.dimens
    val border =
        if (colors.border.alpha > 0f) Modifier.border(dimens.borderSubtle, colors.border, shape) else Modifier
    Row(
        modifier =
            modifier
                .background(colors.container, shape)
                .then(border)
                .padding(
                    horizontal = horizontalPadding,
                    vertical = if (size == PillSize.Small) dimens.spaceXxs else dimens.spaceXs,
                ),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dot != PillDot.None) {
            DotBadge(
                color = colors.dot,
                size = if (size == PillSize.Small) dimens.spaceXs + dimens.spaceXxs else dimens.spaceSm,
                pulsing = dot == PillDot.Pulsing,
            )
        }
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(PillDefaults.iconSize(size)),
                tint = colors.content,
            )
        }
        Text(
            text = if (uppercase) text.uppercase() else text,
            modifier = Modifier.weight(1f, fill = false),
            color = colors.content,
            style = PillDefaults.textStyle(size),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "Pill tones x styles", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun PillTonesPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DisplayTone.entries.forEach { tone ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillStyle.entries.forEach { style ->
                        Pill(text = tone.name, tone = tone, style = style, size = PillSize.Small)
                    }
                }
            }
        }
    }
}

@Preview(name = "Pill content", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun PillContentPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Pill(
                text = "Rekomendasi Mika Hari Ini",
                tone = DisplayTone.Cyan,
                icon = Icons.Filled.AutoAwesome,
                uppercase = true,
                size = PillSize.Small,
            )
            Pill(text = "78% Selesai", tone = DisplayTone.Success)
            Pill(text = "7 Hari", tone = DisplayTone.Orange, icon = Icons.Filled.LocalFireDepartment)
            Pill(text = "Tersimpan v2.1", tone = DisplayTone.Neutral, dot = PillDot.Static)
            Pill(text = "Sedang Diproses", tone = DisplayTone.Primary, dot = PillDot.Pulsing)
            Pill(text = "Skor: 88 (A)", tone = DisplayTone.Success, icon = Icons.Outlined.CheckCircle)
            Pill(text = "Selesai Dinilai", tone = DisplayTone.Success, style = PillStyle.Filled)
            Pill(
                text = "Pill yang sangat panjang terpotong dengan elipsis di akhir baris",
                modifier = Modifier.width(200.dp),
                icon = Icons.Outlined.Timer,
            )
            Pill(
                text = "Tone domain (teal)",
                colors = PillDefaults.colors(HeyMikaTheme.tones.teal),
            )
        }
    }
}

@Preview(name = "Tag", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun TagPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tag(text = "Fokus Utama", tone = DisplayTone.Neutral, style = PillStyle.Filled, uppercase = true)
                Tag(text = "Fokus Hari Ini", uppercase = true)
                Tag(text = "SRS Review", tone = DisplayTone.Orange, uppercase = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tag(text = "Kuis Interaktif")
                Tag(text = "Segera", tone = DisplayTone.Red, icon = Icons.Filled.LocalFireDepartment)
                Tag(text = "Menengah", tone = DisplayTone.Neutral)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tag(text = "#Matematika", size = PillSize.Medium)
                Tag(text = "-1 ATP", tone = DisplayTone.Danger, size = PillSize.Medium)
                Tag(text = "O(log n)", tone = DisplayTone.Sky, style = PillStyle.Outline, size = PillSize.Medium)
            }
        }
    }
}
