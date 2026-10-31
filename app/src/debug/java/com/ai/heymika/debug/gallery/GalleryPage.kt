package com.ai.heymika.debug.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.ai.heymika.R
import com.ai.heymika.core.ui.component.display.SectionHeader
import com.ai.heymika.core.ui.component.navigation.HeyMikaTopBar
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import java.util.Locale

/** One gallery page: a top bar with a "Galeri" back pill over a scrolling column of groups. */
@Composable
fun GalleryPage(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    Column(modifier = Modifier.fillMaxSize()) {
        HeyMikaTopBar(
            title = title,
            onBack = onBack,
            backLabel = stringResource(R.string.gallery_back),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = dimens.screenMargin, vertical = dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(dimens.sectionGap),
            content = content,
        )
    }
}

/** A titled block of specimens inside a [GalleryPage]. */
@Composable
fun GalleryGroup(
    title: String,
    modifier: Modifier = Modifier,
    note: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
    ) {
        SectionHeader(title = title, subtitle = note, compact = true)
        content()
    }
}

/** A small muted caption naming the state of the specimen below it. */
@Composable
fun SpecimenLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = HeyMikaTheme.typography.caption,
        color = HeyMikaTheme.colors.textMuted,
    )
}

/** [items] laid out in rows of [columns] equal-width cells, so every specimen keeps the same width. */
@Composable
fun <T> GalleryGrid(
    items: List<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    cell: @Composable (item: T, modifier: Modifier) -> Unit,
) {
    val spacing = HeyMikaTheme.dimens.spaceSm
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacing)) {
        items.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                row.forEach { item -> cell(item, Modifier.weight(1f)) }
                repeat(columns - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * Lets a horizontally scrolling row ignore the page's side margin, so it scrolls edge to edge
 * like it does on a real screen and keeps its own `contentPadding` for the first item.
 */
fun Modifier.fullBleed(margin: Dp): Modifier =
    layout { measurable, constraints ->
        val extra = margin.roundToPx() * 2
        val placeable =
            measurable.measure(
                constraints.copy(
                    minWidth = constraints.minWidth + extra,
                    maxWidth = constraints.maxWidth + extra,
                ),
            )
        layout(constraints.maxWidth, placeable.height) { placeable.place(-extra / 2, 0) }
    }

/** "#RRGGBB", or "#AARRGGBB" when the colour is translucent. */
fun Color.toHex(): String {
    val argb = toArgb()
    return if (alpha < 1f) {
        "#%08X".format(Locale.ROOT, argb)
    } else {
        "#%06X".format(Locale.ROOT, argb and 0xFFFFFF)
    }
}
