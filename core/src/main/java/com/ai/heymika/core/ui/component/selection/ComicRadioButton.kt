package com.ai.heymika.core.ui.component.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.staticFocusRing
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * A 20dp radio circle with a 2dp ink border. Selected fills primary with a white centre dot. Error:
 * danger border on a dangerBg face. Disabled: #E7EEFF face and borderMuted border.
 *
 * With [onClick] it is its own control: a 48dp touch target with Role.RadioButton, an unbounded
 * ripple and the focus ring. Pass null to draw the circle only inside a parent that owns the
 * selection (rows, option cards). Wrap a group in `Modifier.selectableGroup()`; prefer
 * [RadioButtonRow].
 */
@Composable
fun ComicRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    ComicRadioButtonImpl(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun ComicRadioButtonImpl(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    previewFocused: Boolean = false,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.pill
    val select =
        if (onClick != null) {
            Modifier
                .minimumInteractiveComponentSize()
                .selectable(
                    selected = selected,
                    interactionSource = source,
                    indication = ripple(bounded = false, radius = dimens.minTouchTarget / 2),
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
        } else {
            Modifier
        }
    val ring =
        when {
            previewFocused -> Modifier.staticFocusRing(shape = shape)
            onClick != null -> Modifier.comicFocusRing(source, shape = shape)
            else -> Modifier
        }
    Box(
        modifier = modifier.then(select),
        contentAlignment = Alignment.Center,
    ) {
        RadioCircle(
            selected = selected,
            enabled = enabled,
            isError = isError,
            modifier = ring,
        )
    }
}

/**
 * A radio button and its label as ONE control: the whole row (at least 48dp tall) selects, with
 * Role.RadioButton, a merged TalkBack label, a ripple and the focus ring around the row. Put the
 * rows of one question in a parent with `Modifier.selectableGroup()`.
 */
@Composable
fun RadioButtonRow(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    RadioButtonRowImpl(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        description = description,
        enabled = enabled,
        isError = isError,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun RadioButtonRowImpl(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    previewFocused: Boolean = false,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    SelectionRow(
        selectionModifier =
            Modifier.selectable(
                selected = selected,
                interactionSource = source,
                indication = ripple(),
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        interactionSource = source,
        label = AnnotatedString(label),
        description = description,
        enabled = enabled,
        previewFocused = previewFocused,
        modifier = modifier,
    ) {
        ComicRadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
            isError = isError,
        )
    }
}

@Composable
private fun RadioCircle(
    selected: Boolean,
    enabled: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = HeyMikaTheme.colors
    val shape = HeyMikaTheme.shapes.pill
    val fill =
        when {
            !enabled && selected -> colors.disabledBorder
            !enabled -> colors.disabledContainer
            selected -> colors.primary
            isError -> colors.dangerBg
            else -> colors.surface
        }
    val border =
        when {
            !enabled -> colors.disabledBorder
            isError -> colors.danger
            else -> colors.ink
        }
    Box(
        modifier =
            modifier
                .size(HeyMikaTheme.dimens.checkboxSize)
                .background(fill, shape)
                .border(HeyMikaTheme.dimens.borderControl, border, shape)
                .then(
                    if (selected) {
                        Modifier.radioDot(if (enabled) colors.onPrimary else colors.disabledContent)
                    } else {
                        Modifier
                    },
                ),
    )
}

private fun Modifier.radioDot(color: Color): Modifier =
    this.drawBehind {
        drawCircle(color = color, radius = size.minDimension * DOT_RADIUS_FRACTION)
    }

// An 8dp dot in the 20dp circle.
private const val DOT_RADIUS_FRACTION = 0.2f

@Preview(name = "Radio states", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicRadioButtonPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ComicRadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.semantics { contentDescription = "Belum dipilih" },
                )
                ComicRadioButton(
                    selected = true,
                    onClick = {},
                    modifier = Modifier.semantics { contentDescription = "Dipilih" },
                )
                ComicRadioButtonImpl(
                    selected = true,
                    onClick = {},
                    modifier = Modifier.semantics { contentDescription = "Fokus" },
                    previewFocused = true,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ComicRadioButton(
                    selected = false,
                    onClick = {},
                    isError = true,
                    modifier = Modifier.semantics { contentDescription = "Error" },
                )
                ComicRadioButton(
                    selected = false,
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.semantics { contentDescription = "Tidak aktif" },
                )
                ComicRadioButton(
                    selected = true,
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.semantics { contentDescription = "Tidak aktif dipilih" },
                )
            }
        }
    }
}

@Preview(name = "Radio rows", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun RadioButtonRowPreview() {
    HeyMikaTheme {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .selectableGroup(),
        ) {
            RadioButtonRow(
                selected = false,
                onClick = {},
                label = "15 Menit / Hari",
                description = "Santai & Ringan (Review kilat 5 kartu)",
            )
            RadioButtonRowImpl(
                selected = true,
                onClick = {},
                label = "30 Menit / Hari",
                description = "Konsisten & Ideal untuk pemahaman mendalam",
                previewFocused = true,
            )
            RadioButtonRow(
                selected = false,
                onClick = {},
                label = "60 Menit / Hari",
                description = "Ambisius & Kejar Ujian Semester",
                isError = true,
            )
            RadioButtonRow(
                selected = false,
                onClick = {},
                label = "Ekspor PDF",
                description = "Hadir di versi berikutnya",
                enabled = false,
            )
        }
    }
}
