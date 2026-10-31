package com.ai.heymika.core.ui.component.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.modifier.animatePressTravel
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.comicPressSurface
import com.ai.heymika.core.ui.modifier.staticFocusRing
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/** One segment: a [label], an optional [sublabel] ("Tingkat 1", "Paling Cocok") and a 16dp [icon]. */
@Immutable
data class SegmentedOption(
    val label: String,
    val sublabel: String? = null,
    val icon: ImageVector? = null,
)

/**
 * - [Track]: equal pill segments in a tinted pill track (onboarding "Gaya Bahasa"). The active
 *   segment is a white face with a 2dp ink border on a 2dp ink ledge, label primaryText.
 * - [Separated]: equal rounded segments with gaps (Profil "Ukuran Huruf"). The active segment is a
 *   primary face on a #00458E ledge; the others are tinted on a borderMuted ledge.
 */
enum class SegmentedControlStyle { Track, Separated }

/**
 * A single-choice control of 2 to 4 equal segments. Each segment is a Role.RadioButton inside a
 * selectable group, at least 48dp tall, with the focus ring; pressing sinks the face onto its 2dp
 * ledge. Segments share the height of the tallest one.
 *
 * [selectedIndex] outside the range shows no active segment. Labels are single-line with an
 * ellipsis, so keep them short.
 */
@Composable
fun SegmentedControl(
    options: List<SegmentedOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    style: SegmentedControlStyle = SegmentedControlStyle.Track,
    enabled: Boolean = true,
) {
    SegmentedControlImpl(
        options = options,
        selectedIndex = selectedIndex,
        onSelect = onSelect,
        modifier = modifier,
        style = style,
        enabled = enabled,
    )
}

@Composable
internal fun SegmentedControlImpl(
    options: List<SegmentedOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    style: SegmentedControlStyle = SegmentedControlStyle.Track,
    enabled: Boolean = true,
    previewFocusedIndex: Int = -1,
) {
    require(options.size in MIN_SEGMENTS..MAX_SEGMENTS) { "SegmentedControl takes 2 to 4 options" }
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val track =
        when (style) {
            SegmentedControlStyle.Track -> {
                Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerLow, HeyMikaTheme.shapes.pill)
                    .border(dimens.borderSubtle, colors.borderMuted, HeyMikaTheme.shapes.pill)
                    .padding(dimens.spaceXs)
            }

            SegmentedControlStyle.Separated -> {
                Modifier
            }
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(track)
                .height(IntrinsicSize.Min)
                .selectableGroup(),
        horizontalArrangement =
            Arrangement.spacedBy(
                if (style == SegmentedControlStyle.Track) dimens.spaceXs else dimens.spaceSm,
            ),
    ) {
        options.forEachIndexed { index, option ->
            Segment(
                option = option,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                style = style,
                enabled = enabled,
                previewFocused = index == previewFocusedIndex,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun Segment(
    option: SegmentedOption,
    selected: Boolean,
    onClick: () -> Unit,
    style: SegmentedControlStyle,
    enabled: Boolean,
    previewFocused: Boolean,
    modifier: Modifier = Modifier,
) {
    val source = remember { MutableInteractionSource() }
    val travelState = animatePressTravel(source, enabled = enabled)
    val dimens = HeyMikaTheme.dimens
    val shape = if (style == SegmentedControlStyle.Track) HeyMikaTheme.shapes.pill else HeyMikaTheme.shapes.button
    val segmentColors = segmentColors(style, selected, enabled)
    val ring =
        if (previewFocused) {
            Modifier.staticFocusRing(shape = shape)
        } else {
            Modifier.comicFocusRing(source, shape = shape)
        }
    Box(
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .then(ring)
                .selectable(
                    selected = selected,
                    interactionSource = source,
                    indication = null,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick,
                ).comicPressSurface(
                    travel = { travelState.value },
                    color = segmentColors.face,
                    baseColor = segmentColors.base,
                    shape = shape,
                    baseHeight = dimens.buttonShadowPressed,
                    borderWidth = dimens.borderControl,
                    borderColor = segmentColors.border,
                ).padding(horizontal = dimens.spaceSm, vertical = dimens.spaceXs),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val icon = option.icon
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(dimens.iconSizeSmall),
                        tint = segmentColors.label,
                    )
                }
                Text(
                    text = option.label,
                    style =
                        HeyMikaTheme.typography.labelMd.copy(
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                        ),
                    color = segmentColors.label,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val sublabel = option.sublabel
            if (sublabel != null) {
                Text(
                    text = sublabel,
                    style = HeyMikaTheme.typography.caption,
                    color = segmentColors.sublabel,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Immutable
private data class SegmentColors(
    val face: Color,
    val base: Color,
    val border: Color,
    val label: Color,
    val sublabel: Color,
)

@Composable
@ReadOnlyComposable
private fun segmentColors(
    style: SegmentedControlStyle,
    selected: Boolean,
    enabled: Boolean,
): SegmentColors {
    val c = HeyMikaTheme.colors
    val none = Color.Transparent
    val disabledFlat =
        SegmentColors(
            face = none,
            base = none,
            border = none,
            label = c.disabledContent,
            sublabel = c.disabledContent,
        )
    return when (style) {
        SegmentedControlStyle.Track -> {
            when {
                !enabled && selected -> {
                    disabledFlat.copy(
                        face = c.disabledContainer,
                        base = c.disabledBorder,
                        border = c.disabledBorder,
                    )
                }

                !enabled -> {
                    disabledFlat
                }

                selected -> {
                    SegmentColors(
                        face = c.surface,
                        base = c.ink,
                        border = c.ink,
                        label = c.primaryText,
                        sublabel = c.textMuted,
                    )
                }

                else -> {
                    disabledFlat.copy(label = c.textMuted, sublabel = c.textMuted)
                }
            }
        }

        SegmentedControlStyle.Separated -> {
            when {
                !enabled -> {
                    disabledFlat.copy(face = c.disabledContainer, base = c.disabledBorder)
                }

                selected -> {
                    SegmentColors(
                        face = c.primary,
                        base = c.onPrimaryFixedVariant,
                        border = none,
                        label = c.onPrimary,
                        sublabel = c.onPrimary,
                    )
                }

                else -> {
                    disabledFlat.copy(
                        face = MaterialTheme.colorScheme.surfaceContainerLow,
                        base = c.borderMuted,
                        label = c.textMuted,
                        sublabel = c.textMuted,
                    )
                }
            }
        }
    }
}

private const val MIN_SEGMENTS = 2
private const val MAX_SEGMENTS = 4

@Preview(name = "Track style", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun SegmentedControlTrackPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val languages =
                listOf(
                    SegmentedOption("Indonesia", icon = Icons.Outlined.Translate),
                    SegmentedOption("English", icon = Icons.Outlined.Language),
                    SegmentedOption("Gaul", icon = Icons.AutoMirrored.Outlined.Chat),
                )
            SegmentedControl(
                options = languages,
                selectedIndex = 0,
                onSelect = {},
            )
            SegmentedControl(
                options =
                    listOf(
                        SegmentedOption("Mudah", sublabel = "Tingkat 1"),
                        SegmentedOption("Sedang", sublabel = "Paling Cocok"),
                        SegmentedOption("Sulit / HOTS", sublabel = "Tingkat 3"),
                    ),
                selectedIndex = 1,
                onSelect = {},
            )
            SegmentedControlImpl(
                options = languages,
                selectedIndex = 0,
                onSelect = {},
                previewFocusedIndex = 1,
            )
            SegmentedControl(
                options = languages,
                selectedIndex = 0,
                onSelect = {},
                enabled = false,
            )
        }
    }
}

@Preview(name = "Separated style", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun SegmentedControlSeparatedPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val sizes =
                listOf(
                    SegmentedOption("Kecil"),
                    SegmentedOption("Normal"),
                    SegmentedOption("Besar"),
                )
            SegmentedControl(
                options = sizes,
                selectedIndex = 1,
                onSelect = {},
                style = SegmentedControlStyle.Separated,
            )
            SegmentedControl(
                options =
                    listOf(
                        SegmentedOption("Sederhana"),
                        SegmentedOption("Seimbang"),
                        SegmentedOption("Teknis"),
                        SegmentedOption("Pakar"),
                    ),
                selectedIndex = 1,
                onSelect = {},
                style = SegmentedControlStyle.Separated,
            )
            SegmentedControlImpl(
                options = sizes,
                selectedIndex = 1,
                onSelect = {},
                style = SegmentedControlStyle.Separated,
                previewFocusedIndex = 2,
            )
            SegmentedControl(
                options = sizes.take(2),
                selectedIndex = 0,
                onSelect = {},
                style = SegmentedControlStyle.Separated,
                enabled = false,
            )
        }
    }
}
