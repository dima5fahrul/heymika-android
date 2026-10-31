package com.ai.heymika.core.ui.component.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * A section title row ("Materi Terakhir", "Kuis Mendatang") with an optional end action, which reads
 * "Lihat semua" by default.
 * - An optional [kicker] above the title renders as a small uppercase tag ("FOKUS UTAMA").
 * - [subtitle] is a muted line under the title.
 * - [trailingText] is muted end text such as "3 dari 6", placed before the action.
 * - [compact] uses the smaller labelLg title, for sections inside cards.
 *
 * The title is a heading for TalkBack. With the default label the action is announced as
 * "Lihat semua <title>", so several of them on one screen stay distinct.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    kicker: String? = null,
    trailingText: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    compact: Boolean = false,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val typography = HeyMikaTheme.typography
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(if (compact) dimens.iconSize else dimens.iconSizeLarge),
                tint = colors.primaryText,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceXxs),
        ) {
            if (kicker != null) {
                Tag(
                    text = kicker,
                    modifier = Modifier.padding(bottom = dimens.spaceXxs),
                    tone = DisplayTone.Neutral,
                    style = PillStyle.Filled,
                    uppercase = true,
                )
            }
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                color = colors.textPrimary,
                style = if (compact) typography.labelLg else typography.sectionTitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = colors.textMuted,
                    style = if (compact) typography.caption else typography.bodySm,
                )
            }
        }
        if (trailingText != null) {
            Text(
                text = trailingText,
                color = colors.textMuted,
                style = typography.labelSm,
                maxLines = 1,
            )
        }
        if (onAction != null) {
            val label = actionLabel ?: stringResource(R.string.display_section_see_all)
            val description =
                if (actionLabel == null) stringResource(R.string.display_section_see_all_description, title) else null
            ComicButton(
                text = label,
                onClick = onAction,
                modifier =
                    if (description != null) {
                        Modifier.semantics { contentDescription = description }
                    } else {
                        Modifier
                    },
                variant = ComicButtonVariant.Ghost,
                size = ComicButtonSize.Small,
            )
        }
    }
}

@Preview(name = "SectionHeader", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun SectionHeaderPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SectionHeader(title = "Materi Terakhir", onAction = {})
            SectionHeader(
                title = "Lanjutkan Bab 3: Fotosintesis",
                kicker = "Fokus Utama",
                subtitle = "Biologi Sel - 12 menit tersisa",
            )
            SectionHeader(
                title = "Target Minggu Ini",
                icon = Icons.Outlined.TrackChanges,
                trailingText = "3 dari 5",
                actionLabel = "Ubah",
                onAction = {},
            )
            SectionHeader(
                title = "Sumber Materi",
                icon = Icons.Outlined.AutoStories,
                subtitle = "6 file terhubung",
                compact = true,
                onAction = {},
            )
        }
    }
}
