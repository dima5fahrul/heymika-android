package com.ai.heymika.core.ui.component.display

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.staticFocusRing
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/** Colours of one state of a [SelectionPill]; the count bubble has its own pair. */
@Immutable
internal data class SelectionPillColors(
    val container: Color,
    val content: Color,
    val border: Color,
    val countContainer: Color,
    val countContent: Color,
)

/**
 * The interactive pill shared by [FilterChipRow] and [PillTabRow]: a 48dp touch target around a
 * [faceHeight] pill, the comic focus ring, a ripple clipped to the pill, and `selectable` (single
 * choice, tabs) or `toggleable` (multi-select) semantics with [role].
 */
@Composable
internal fun SelectionPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: SelectionPillColors,
    role: Role,
    multiSelect: Boolean,
    faceHeight: Dp,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    count: Int? = null,
    enabled: Boolean = true,
    previewFocused: Boolean = false,
) {
    val source = remember { MutableInteractionSource() }
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.pill
    val ring = if (previewFocused) Modifier.staticFocusRing(shape) else Modifier.comicFocusRing(source, shape)
    val indication = ripple(color = colors.content)
    val selection =
        if (multiSelect) {
            Modifier.toggleable(
                value = selected,
                interactionSource = source,
                indication = indication,
                enabled = enabled,
                role = role,
                onValueChange = { onClick() },
            )
        } else {
            Modifier.selectable(
                selected = selected,
                interactionSource = source,
                indication = indication,
                enabled = enabled,
                role = role,
                onClick = onClick,
            )
        }
    val border =
        if (colors.border.alpha > 0f) Modifier.border(dimens.borderSubtle, colors.border, shape) else Modifier
    Row(
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .heightIn(min = faceHeight)
                .then(ring)
                .clip(shape)
                .then(selection)
                .background(colors.container)
                .then(border)
                .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs + dimens.spaceXxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(dimens.iconSizeSmall),
                tint = colors.content,
            )
        }
        Text(
            text = label,
            color = colors.content,
            style = HeyMikaTheme.typography.labelMd,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (count != null) {
            CountBadge(
                count = count,
                colors =
                    PillColors(
                        container = colors.countContainer,
                        content = colors.countContent,
                        border = Color.Transparent,
                        dot = colors.countContent,
                    ),
            )
        }
    }
}

/**
 * Scrolls item [index] of a lazy row into view when it changes and is not fully visible: instantly
 * on the first run and under reduced motion, animated otherwise. A negative index does nothing.
 */
@Composable
internal fun ScrollIntoViewEffect(
    state: LazyListState,
    index: Int,
) {
    val reducedMotion = HeyMikaTheme.reducedMotion
    val firstRun = remember { mutableStateOf(true) }
    LaunchedEffect(state, index) {
        if (index >= 0) {
            val info = state.layoutInfo
            val item = info.visibleItemsInfo.firstOrNull { it.index == index }
            val fullyVisible =
                item != null &&
                    item.offset >= info.viewportStartOffset &&
                    item.offset + item.size <= info.viewportEndOffset
            if (!fullyVisible) {
                if (firstRun.value || reducedMotion) state.scrollToItem(index) else state.animateScrollToItem(index)
            }
        }
        firstRun.value = false
    }
}
