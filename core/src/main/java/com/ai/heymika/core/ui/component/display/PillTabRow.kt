package com.ai.heymika.core.ui.component.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.component.foundation.SubtleDivider
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/** One tab of a [PillTabRow]. [count] renders as a bubble after the label ("Flashcard 42"). */
@Immutable
data class PillTab(
    val label: String,
    val icon: ImageVector? = null,
    val count: Int? = null,
)

/**
 * A horizontally scrollable row of pill tabs, such as the Detail workspace tabs "Ringkasan AI /
 * Sumber Materi / Flashcard / Kuis". The active tab is filled with primary; the others are white
 * pills with a 1.5dp borderMuted border.
 *
 * It is sticky-friendly: the row paints an opaque [containerColor] (the screen canvas by default)
 * with an optional bottom hairline, so it can be pinned with `LazyColumn { stickyHeader { ... } }`.
 * It is stateless and scrolls the selected tab into view. Every tab has Role.Tab in a selectable
 * group, a 48dp touch target and the focus ring.
 */
@Composable
fun PillTabRow(
    tabs: List<PillTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = HeyMikaTheme.colors.canvas,
    showDivider: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = HeyMikaTheme.dimens.screenMargin),
) {
    PillTabRowImpl(
        tabs = tabs,
        selectedIndex = selectedIndex,
        onSelect = onSelect,
        modifier = modifier,
        containerColor = containerColor,
        showDivider = showDivider,
        contentPadding = contentPadding,
    )
}

@Composable
internal fun PillTabRowImpl(
    tabs: List<PillTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
    containerColor: Color,
    showDivider: Boolean,
    contentPadding: PaddingValues,
    previewFocusedIndex: Int = -1,
) {
    val state = rememberLazyListState()
    ScrollIntoViewEffect(state, selectedIndex)
    val dimens = HeyMikaTheme.dimens
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(containerColor),
    ) {
        LazyRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .selectableGroup()
                    .padding(vertical = dimens.spaceXxs),
            state = state,
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            itemsIndexed(tabs) { index, tab ->
                val selected = index == selectedIndex
                SelectionPill(
                    label = tab.label,
                    selected = selected,
                    onClick = { onSelect(index) },
                    colors = pillTabColors(selected),
                    role = Role.Tab,
                    multiSelect = false,
                    faceHeight = dimens.buttonHeightSmall,
                    horizontalPadding = dimens.spaceMd,
                    icon = tab.icon,
                    count = tab.count,
                    previewFocused = index == previewFocusedIndex,
                )
            }
        }
        if (showDivider) SubtleDivider()
    }
}

@Composable
@ReadOnlyComposable
private fun pillTabColors(selected: Boolean): SelectionPillColors {
    val c = HeyMikaTheme.colors
    return if (selected) {
        SelectionPillColors(c.primary, c.onPrimary, Color.Transparent, c.surface, c.primaryText)
    } else {
        SelectionPillColors(
            container = c.surface,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            border = c.borderMuted,
            countContainer = MaterialTheme.colorScheme.surfaceContainer,
            countContent = c.textPrimary,
        )
    }
}

private val previewTabs =
    listOf(
        PillTab("Ringkasan AI", icon = Icons.Outlined.AutoStories),
        PillTab("Sumber Materi", icon = Icons.Outlined.Folder, count = 6),
        PillTab("Flashcard", icon = Icons.Outlined.Style, count = 42),
        PillTab("Kuis Interaktif", icon = Icons.Outlined.Quiz, count = 3),
    )

@Preview(name = "PillTabRow", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun PillTabRowPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PillTabRow(tabs = previewTabs, selectedIndex = 0, onSelect = {})
            PillTabRow(tabs = previewTabs, selectedIndex = 2, onSelect = {}, showDivider = false)
            PillTabRow(
                tabs = listOf(PillTab("Ringkasan"), PillTab("Glosarium"), PillTab("Sitasi")),
                selectedIndex = 1,
                onSelect = {},
                containerColor = HeyMikaTheme.colors.surface,
            )
        }
    }
}

@Preview(name = "PillTabRow focused", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun PillTabRowFocusedPreview() {
    HeyMikaTheme {
        PillTabRowImpl(
            tabs = previewTabs,
            selectedIndex = 0,
            onSelect = {},
            modifier = Modifier.padding(vertical = 16.dp),
            containerColor = HeyMikaTheme.colors.canvas,
            showDivider = true,
            contentPadding = PaddingValues(horizontal = 16.dp),
            previewFocusedIndex = 1,
        )
    }
}
