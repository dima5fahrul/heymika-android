package com.ai.heymika.core.ui.component.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.component.foundation.ComicCard
import com.ai.heymika.core.ui.component.foundation.ComicCardVariant
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * A small KPI card ("Total Kuis 12", "Rata-rata Skor 82%", "Waktu Belajar 4j 20m") on a subtle card.
 * - Top row: an optional [icon] on a tinted tile ([iconTone]) and an optional [delta] tag
 *   ("+12%", [deltaTone], with [deltaIcon]).
 * - Then the [label], the bold [value] with an optional [unit], and an optional [caption]
 *   ("Minggu ini"), tinted by [captionTone].
 *
 * [valueTone] recolours the value, for example Danger for "3 salah". With [onClick] the whole card
 * is one button with the focus ring and [onClickLabel]. TalkBack reads one sentence:
 * "label, value unit, delta, caption".
 */
@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTone: DisplayTone = DisplayTone.Primary,
    unit: String? = null,
    delta: String? = null,
    deltaTone: DisplayTone = DisplayTone.Success,
    deltaIcon: ImageVector? = null,
    caption: String? = null,
    captionTone: DisplayTone? = null,
    valueTone: DisplayTone? = null,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val typography = HeyMikaTheme.typography
    val description =
        listOfNotNull(
            label,
            if (unit != null) "$value $unit" else value,
            delta,
            caption,
        ).joinToString(separator = ", ")
    ComicCard(
        modifier = modifier,
        variant = ComicCardVariant.Subtle,
        onClick = onClick,
        onClickLabel = onClickLabel,
        contentPadding = PaddingValues(dimens.cardPaddingCompact),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics { contentDescription = description },
            verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
        ) {
            if (icon != null || delta != null) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = dimens.spaceXs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (icon != null) StatIconTile(icon = icon, tone = iconTone) else Spacer(Modifier)
                    if (delta != null) {
                        Tag(
                            text = delta,
                            tone = deltaTone,
                            icon = deltaIcon,
                        )
                    }
                }
            }
            Text(
                text = label,
                color = colors.textMuted,
                style = typography.caption,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs),
            ) {
                Text(
                    text = value,
                    modifier = Modifier.alignByBaseline(),
                    color = valueTone?.let { PillDefaults.toneSpec(it).content } ?: colors.textPrimary,
                    style = typography.headlineMd,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (unit != null) {
                    Text(
                        text = unit,
                        modifier = Modifier.alignByBaseline(),
                        color = colors.textMuted,
                        style = typography.labelSm,
                        maxLines = 1,
                    )
                }
            }
            if (caption != null) {
                Text(
                    text = caption,
                    color = captionTone?.let { PillDefaults.toneSpec(it).content } ?: colors.textMuted,
                    style = typography.caption,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun StatIconTile(
    icon: ImageVector,
    tone: DisplayTone,
    modifier: Modifier = Modifier,
) {
    val dimens = HeyMikaTheme.dimens
    val spec = PillDefaults.toneSpec(tone)
    Box(
        modifier =
            modifier
                .size(dimens.iconSize + dimens.spaceSmPlus)
                .clip(HeyMikaTheme.shapes.small)
                .background(spec.container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(dimens.iconSize),
            tint = spec.content,
        )
    }
}

@Preview(name = "StatTile", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun StatTilePreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    value = "12",
                    label = "Total Kuis",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Quiz,
                    delta = "+3",
                    deltaIcon = Icons.AutoMirrored.Filled.TrendingUp,
                    caption = "Minggu ini",
                )
                StatTile(
                    value = "82",
                    unit = "%",
                    label = "Rata-rata Skor",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.CheckCircle,
                    iconTone = DisplayTone.Success,
                    delta = "-4%",
                    deltaTone = DisplayTone.Danger,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    value = "4j 20m",
                    label = "Waktu Belajar",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Timer,
                    iconTone = DisplayTone.Orange,
                    caption = "Target 5 jam",
                    captionTone = DisplayTone.Orange,
                    onClick = {},
                    onClickLabel = "Lihat statistik",
                )
                StatTile(
                    value = "3",
                    label = "Jawaban Salah",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Bookmark,
                    iconTone = DisplayTone.Red,
                    valueTone = DisplayTone.Danger,
                )
            }
            StatTile(value = "6", label = "Materi Aktif")
        }
    }
}
