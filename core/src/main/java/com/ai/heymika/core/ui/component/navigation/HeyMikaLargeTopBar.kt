package com.ai.heymika.core.ui.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * The large page header that opens a tab screen ("Materimu", "Pusat Notifikasi") or greets the
 * learner ("Halo, Budi!"). It scrolls with the content, below a compact [HeyMikaTopBar].
 *
 * [eyebrow] holds the pills above the title ("MANGA STUDY DECK", "6 Topik Aktif"), [titleBadge] a
 * sticker or count pill right after the title ("WORKSPACE", "3 Belum Dibaca"), and [actions]
 * trailing icon buttons. [titleStyle] is displayHero for page titles; pass headlineMd or
 * headlineLg for denser headers and greetings. The title is a two-line heading at most.
 */
@Composable
fun HeyMikaLargeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    eyebrow: (@Composable RowScope.() -> Unit)? = null,
    titleBadge: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    titleStyle: TextStyle = HeyMikaTheme.typography.displayHero,
    windowInsets: WindowInsets = WindowInsets(0, 0, 0, 0),
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(windowInsets)
                .padding(horizontal = dimens.screenMargin, vertical = dimens.spaceSmPlus),
        verticalArrangement = Arrangement.spacedBy(dimens.spaceSm),
    ) {
        if (eyebrow != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
                content = eyebrow,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
            ) {
                Text(
                    text = title,
                    style = titleStyle,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        Modifier
                            .weight(1f, fill = false)
                            .semantics { heading() },
                )
                titleBadge?.invoke()
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = HeyMikaTheme.typography.bodySm,
                color = colors.textMuted,
            )
        }
    }
}

@Composable
private fun PreviewPill(text: String) {
    Text(
        text = text,
        style = HeyMikaTheme.typography.labelSm,
        color = HeyMikaTheme.colors.ink,
        modifier =
            Modifier
                .background(HeyMikaTheme.colors.badgeCyan, HeyMikaTheme.shapes.pill)
                .padding(horizontal = 10.dp, vertical = 2.dp),
    )
}

@Composable
private fun PreviewSticker(text: String) {
    Text(
        text = text,
        style = HeyMikaTheme.typography.labelSm,
        color = HeyMikaTheme.colors.ink,
        modifier =
            Modifier
                .background(HeyMikaTheme.colors.yellow, HeyMikaTheme.shapes.small)
                .border(2.dp, HeyMikaTheme.colors.ink, HeyMikaTheme.shapes.small)
                .padding(horizontal = 10.dp, vertical = 2.dp),
    )
}

@Preview(name = "Page title (Materimu)", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun HeyMikaLargeTopBarPagePreview() {
    HeyMikaTheme {
        HeyMikaLargeTopBar(
            title = "Materimu",
            subtitle = "Kelola catatan kuliah, ringkasan AI, slide dosen, dan bank flashcard per topik bareng Mika!",
            eyebrow = {
                PreviewPill(text = "MANGA STUDY DECK")
                PreviewPill(text = "6 Topik Aktif")
            },
            titleBadge = { PreviewSticker(text = "WORKSPACE") },
        )
    }
}

@Preview(name = "Dense title with actions", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun HeyMikaLargeTopBarActionsPreview() {
    HeyMikaTheme {
        HeyMikaLargeTopBar(
            title = "Pusat Notifikasi",
            titleStyle = HeyMikaTheme.typography.headlineMd,
            titleBadge = { PreviewPill(text = "3 Belum Dibaca") },
            actions = {
                TopBarIconButton(icon = Icons.Filled.DoneAll, contentDescription = "Tandai semua dibaca", onClick = {})
                TopBarIconButton(icon = Icons.Outlined.Tune, contentDescription = "Pengaturan jam tenang", onClick = {})
            },
        )
    }
}

@Preview(name = "Greeting", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun HeyMikaLargeTopBarGreetingPreview() {
    HeyMikaTheme {
        HeyMikaLargeTopBar(
            title = "Halo, Budi! 🐾",
            titleStyle = HeyMikaTheme.typography.headlineLg,
            subtitle = "Mika sudah rapikan 3 tugas paling penting untukmu hari ini.",
            eyebrow = { PreviewPill(text = "LEVEL 12 • PELAJAR TANGKAS") },
        )
    }
}
