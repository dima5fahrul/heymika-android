package com.ai.heymika.core.ui.component.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoStories
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
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * One chip of a [FilterChipRow]. [count] renders as a bubble after the label ("Aktif 5"). [tone]
 * tints the chip while unselected and fills it while selected ("Salah (2)" in Danger); without a tone
 * the chip is a white outline pill that turns primary when selected.
 */
@Immutable
data class FilterChipItem<out K>(
    val key: K,
    val label: String,
    val count: Int? = null,
    val icon: ImageVector? = null,
    val tone: DisplayTone? = null,
)

/**
 * A horizontally scrollable row of single-select filter pills ("Semua / Sedang Berjalan / Selesai").
 * It is stateless: [selectedKey] is the active chip, and null means none. The row scrolls the
 * selection into view.
 *
 * Every chip is a RadioButton in a selectable group, with a 48dp touch target and the focus ring.
 * [contentPadding] defaults to the screen margin, so the row can bleed to the screen edges.
 */
@Composable
fun <K> FilterChipRow(
    items: List<FilterChipItem<K>>,
    selectedKey: K?,
    onSelect: (K) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = HeyMikaTheme.dimens.screenMargin),
) {
    FilterChipRowImpl(
        items = items,
        isSelected = { it == selectedKey },
        multiSelect = false,
        onClick = onSelect,
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
        scrollToIndex = items.indexOfFirst { it.key == selectedKey },
    )
}

/**
 * The multi-select variant of [FilterChipRow]: every chip is a Checkbox that toggles its key in
 * [selectedKeys], and a selected chip shows a leading check.
 */
@Composable
fun <K> MultiSelectFilterChipRow(
    items: List<FilterChipItem<K>>,
    selectedKeys: Set<K>,
    onToggle: (K) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = HeyMikaTheme.dimens.screenMargin),
) {
    FilterChipRowImpl(
        items = items,
        isSelected = { it in selectedKeys },
        multiSelect = true,
        onClick = onToggle,
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
        scrollToIndex = -1,
    )
}

@Composable
internal fun <K> FilterChipRowImpl(
    items: List<FilterChipItem<K>>,
    isSelected: (K) -> Boolean,
    multiSelect: Boolean,
    onClick: (K) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    contentPadding: PaddingValues,
    scrollToIndex: Int,
    previewFocusedIndex: Int = -1,
) {
    val state = rememberLazyListState()
    ScrollIntoViewEffect(state, scrollToIndex)
    val dimens = HeyMikaTheme.dimens
    LazyRow(
        modifier = if (multiSelect) modifier else modifier.selectableGroup(),
        state = state,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(items) { index, item ->
            val selected = isSelected(item.key)
            SelectionPill(
                label = item.label,
                selected = selected,
                onClick = { onClick(item.key) },
                colors = filterChipColors(item.tone, selected, enabled),
                role = if (multiSelect) Role.Checkbox else Role.RadioButton,
                multiSelect = multiSelect,
                faceHeight = dimens.buttonHeightSmall - dimens.spaceSm,
                horizontalPadding = dimens.spaceSmPlus,
                icon = if (multiSelect && selected) Icons.Filled.Check else item.icon,
                count = item.count,
                enabled = enabled,
                previewFocused = index == previewFocusedIndex,
            )
        }
    }
}

@Composable
@ReadOnlyComposable
private fun filterChipColors(
    tone: DisplayTone?,
    selected: Boolean,
    enabled: Boolean,
): SelectionPillColors {
    val c = HeyMikaTheme.colors
    return when {
        !enabled -> {
            SelectionPillColors(c.disabledContainer, c.disabledContent, Color.Transparent, c.surface, c.disabledContent)
        }

        selected -> {
            val active = tone ?: DisplayTone.Primary
            val filled = PillDefaults.colors(active, PillStyle.Filled)
            SelectionPillColors(
                container = filled.container,
                content = filled.content,
                border = Color.Transparent,
                countContainer = c.surface,
                countContent = PillDefaults.toneSpec(active).content,
            )
        }

        tone != null -> {
            val spec = PillDefaults.toneSpec(tone)
            SelectionPillColors(spec.container, spec.content, Color.Transparent, c.surface, spec.content)
        }

        else -> {
            SelectionPillColors(
                container = c.surface,
                content = c.textMuted,
                border = c.borderMuted,
                countContainer = MaterialTheme.colorScheme.surfaceContainer,
                countContent = c.textPrimary,
            )
        }
    }
}

private enum class PreviewFilter { Semua, Berjalan, Selesai, Diarsipkan }

private val previewItems =
    listOf(
        FilterChipItem(PreviewFilter.Semua, "Semua", count = 6),
        FilterChipItem(PreviewFilter.Berjalan, "Sedang Berjalan", count = 5),
        FilterChipItem(PreviewFilter.Selesai, "Selesai"),
        FilterChipItem(PreviewFilter.Diarsipkan, "Diarsipkan", count = 1),
    )

@Preview(name = "FilterChipRow single", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun FilterChipRowPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChipRow(items = previewItems, selectedKey = PreviewFilter.Semua, onSelect = {})
            FilterChipRow(items = previewItems, selectedKey = PreviewFilter.Diarsipkan, onSelect = {})
            FilterChipRow(
                items =
                    listOf(
                        FilterChipItem("semua", "Semua", count = 10),
                        FilterChipItem("salah", "Salah", count = 2, tone = DisplayTone.Danger),
                        FilterChipItem("kosong", "Tidak Dijawab", count = 1),
                        FilterChipItem("benar", "Benar", count = 7, tone = DisplayTone.Success),
                    ),
                selectedKey = "semua",
                onSelect = {},
            )
            FilterChipRow(items = previewItems, selectedKey = PreviewFilter.Semua, onSelect = {}, enabled = false)
        }
    }
}

@Preview(name = "FilterChipRow multi and focus", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun MultiSelectFilterChipRowPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MultiSelectFilterChipRow(
                items =
                    listOf(
                        FilterChipItem("catatan", "Catatan", icon = Icons.Outlined.AutoStories),
                        FilterChipItem("kuis", "Kuis", icon = Icons.Outlined.Quiz),
                        FilterChipItem("flashcard", "Flashcard", icon = Icons.Outlined.Style),
                    ),
                selectedKeys = setOf("catatan", "flashcard"),
                onToggle = {},
            )
            FilterChipRowImpl(
                items = previewItems,
                isSelected = { it == PreviewFilter.Semua },
                multiSelect = false,
                onClick = {},
                modifier = Modifier,
                enabled = true,
                contentPadding = PaddingValues(horizontal = 16.dp),
                scrollToIndex = 0,
                previewFocusedIndex = 1,
            )
        }
    }
}
