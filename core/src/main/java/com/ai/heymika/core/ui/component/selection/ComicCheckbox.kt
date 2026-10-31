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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.staticFocusRing
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * A 20dp rounded-square checkbox with a 2dp ink border. Checked fills primary with a white
 * hand-drawn check. Error: danger border on a dangerBg face. Disabled: #E7EEFF face and borderMuted
 * border (checked: borderMuted face, textMuted check).
 *
 * With [onCheckedChange] it is its own control: a 48dp touch target with Role.Checkbox, an
 * unbounded ripple and the focus ring around the box. Name it for TalkBack with
 * `Modifier.semantics { contentDescription = ... }`, or prefer [CheckboxRow]. Pass null to draw the
 * box only, inside a parent that owns the toggle (rows, option cards).
 */
@Composable
fun ComicCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    ComicCheckboxImpl(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun ComicCheckboxImpl(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    previewFocused: Boolean = false,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.tag
    val toggle =
        if (onCheckedChange != null) {
            Modifier
                .minimumInteractiveComponentSize()
                .toggleable(
                    value = checked,
                    interactionSource = source,
                    indication = ripple(bounded = false, radius = dimens.minTouchTarget / 2),
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = onCheckedChange,
                )
        } else {
            Modifier
        }
    val ring =
        when {
            previewFocused -> Modifier.staticFocusRing(shape = shape)
            onCheckedChange != null -> Modifier.comicFocusRing(source, shape = shape)
            else -> Modifier
        }
    Box(
        modifier = modifier.then(toggle),
        contentAlignment = Alignment.Center,
    ) {
        CheckboxBox(
            checked = checked,
            enabled = enabled,
            isError = isError,
            modifier = ring,
        )
    }
}

/**
 * A checkbox and its label as ONE control: the whole row (at least 48dp tall) toggles, with
 * Role.Checkbox, a merged TalkBack label, a ripple and the focus ring around the row.
 *
 * [label] may carry LinkAnnotations ("Ketentuan Layanan", "Kebijakan Privasi"); links stay
 * separately tappable and focusable. [isError] turns the box danger; show the message with
 * ComicFieldSupportingText under the row.
 */
@Composable
fun CheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: AnnotatedString,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    CheckboxRowImpl(
        checked = checked,
        onCheckedChange = onCheckedChange,
        label = label,
        modifier = modifier,
        description = description,
        enabled = enabled,
        isError = isError,
        interactionSource = interactionSource,
    )
}

/** [CheckboxRow] with a plain-text label. */
@Composable
fun CheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    CheckboxRowImpl(
        checked = checked,
        onCheckedChange = onCheckedChange,
        label = AnnotatedString(label),
        modifier = modifier,
        description = description,
        enabled = enabled,
        isError = isError,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun CheckboxRowImpl(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: AnnotatedString,
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
            Modifier.toggleable(
                value = checked,
                interactionSource = source,
                indication = ripple(),
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
        interactionSource = source,
        label = label,
        description = description,
        enabled = enabled,
        previewFocused = previewFocused,
        modifier = modifier,
    ) {
        ComicCheckbox(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            isError = isError,
        )
    }
}

@Composable
private fun CheckboxBox(
    checked: Boolean,
    enabled: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = HeyMikaTheme.colors
    val shape = HeyMikaTheme.shapes.tag
    val fill =
        when {
            !enabled && checked -> colors.disabledBorder
            !enabled -> colors.disabledContainer
            checked -> colors.primary
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
                    if (checked) {
                        Modifier.checkMark(if (enabled) colors.onPrimary else colors.disabledContent)
                    } else {
                        Modifier
                    },
                ),
    )
}

private fun Modifier.checkMark(color: Color): Modifier =
    this.drawWithCache {
        val path =
            Path().apply {
                moveTo(size.width * CHECK_START_X, size.height * CHECK_START_Y)
                lineTo(size.width * CHECK_MID_X, size.height * CHECK_MID_Y)
                lineTo(size.width * CHECK_END_X, size.height * CHECK_END_Y)
            }
        val stroke =
            Stroke(
                width = size.width * CHECK_STROKE_FRACTION,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            )
        onDrawBehind { drawPath(path = path, color = color, style = stroke) }
    }

// Check-mark polyline as fractions of the 20dp box, drawn with a round 2.6dp stroke.
private const val CHECK_START_X = 0.26f
private const val CHECK_START_Y = 0.53f
private const val CHECK_MID_X = 0.43f
private const val CHECK_MID_Y = 0.70f
private const val CHECK_END_X = 0.76f
private const val CHECK_END_Y = 0.33f
private const val CHECK_STROKE_FRACTION = 0.13f

@Preview(name = "Checkbox states", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicCheckboxPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ComicCheckbox(
                    checked = false,
                    onCheckedChange = {},
                    modifier = Modifier.semantics { contentDescription = "Belum dicentang" },
                )
                ComicCheckbox(
                    checked = true,
                    onCheckedChange = {},
                    modifier = Modifier.semantics { contentDescription = "Dicentang" },
                )
                ComicCheckboxImpl(
                    checked = true,
                    onCheckedChange = {},
                    modifier = Modifier.semantics { contentDescription = "Fokus" },
                    previewFocused = true,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ComicCheckbox(
                    checked = false,
                    onCheckedChange = {},
                    isError = true,
                    modifier = Modifier.semantics { contentDescription = "Error" },
                )
                ComicCheckbox(
                    checked = true,
                    onCheckedChange = {},
                    isError = true,
                    modifier = Modifier.semantics { contentDescription = "Error dicentang" },
                )
                ComicCheckbox(
                    checked = false,
                    onCheckedChange = {},
                    enabled = false,
                    modifier = Modifier.semantics { contentDescription = "Tidak aktif" },
                )
                ComicCheckbox(
                    checked = true,
                    onCheckedChange = {},
                    enabled = false,
                    modifier = Modifier.semantics { contentDescription = "Tidak aktif dicentang" },
                )
            }
        }
    }
}

@Preview(name = "Checkbox rows", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun CheckboxRowPreview() {
    HeyMikaTheme {
        val linkStyle =
            TextLinkStyles(
                style = SpanStyle(color = HeyMikaTheme.colors.primaryText, fontWeight = FontWeight.Bold),
            )
        val terms =
            buildAnnotatedString {
                append("Saya menyetujui ")
                withLink(LinkAnnotation.Url(url = "https://heymika.id/ketentuan", styles = linkStyle)) {
                    append("Ketentuan Layanan")
                }
                append(" dan ")
                withLink(LinkAnnotation.Url(url = "https://heymika.id/privasi", styles = linkStyle)) {
                    append("Kebijakan Privasi")
                }
                append(" HeyMika. *")
            }
        Column(modifier = Modifier.padding(16.dp)) {
            CheckboxRow(
                checked = true,
                onCheckedChange = {},
                label = "Ingat saya",
            )
            CheckboxRow(
                checked = false,
                onCheckedChange = {},
                label = terms,
                isError = true,
            )
            CheckboxRow(
                checked = true,
                onCheckedChange = {},
                label =
                    "Kirimkan tips belajar mingguan, rekomendasi latihan soal, dan promo spesial dari Mika " +
                        "(Opsional).",
            )
            CheckboxRowImpl(
                checked = true,
                onCheckedChange = {},
                label = AnnotatedString("Modul 04 - Binary Search Tree & AVL"),
                description = "PDF • 42 halaman • Index Terproses 100%",
                previewFocused = true,
            )
            CheckboxRow(
                checked = false,
                onCheckedChange = {},
                label = "Slide Dosen - Dynamic Programming",
                description = "PPTX • 28 slide • Belum terindeks penuh",
                enabled = false,
            )
        }
    }
}
