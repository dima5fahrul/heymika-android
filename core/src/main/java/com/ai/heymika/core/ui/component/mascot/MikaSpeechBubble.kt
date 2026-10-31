package com.ai.heymika.core.ui.component.mascot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.theme.DefaultHeyMikaDimens
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/** The edge the tail points out of. [Start] and [End] follow the layout direction. */
enum class MikaBubbleTail { Start, End, Top, Bottom }

/** Geometry defaults of [MikaSpeechBubble]. */
object MikaSpeechBubbleDefaults {
    /** Base width of the triangular tail, measured along the edge. */
    val tailWidth: Dp = 18.dp

    /** How far the tail tip sticks out of the bubble body. */
    val tailDepth: Dp = 12.dp

    /** Corner radius of the body (DESIGN.md speech bubble, 16dp). */
    val cornerRadius: Dp get() = DefaultHeyMikaDimens.radiusCard
}

/**
 * A rounded rectangle with a triangular tail on one edge, built as ONE closed path, so a border
 * drawn with this shape follows the tail as well.
 *
 * The body is inset by [tailDepth] on the [tail] edge; the tail tip touches the layout bounds.
 * [tailBias] places the tail along its edge (0 = start/top, 1 = end/bottom) and is clamped so the
 * tail never runs into a corner. When the tail edge is shorter than two corners plus the tail base,
 * the corners shrink to make room.
 */
@Immutable
data class MikaBubbleShape(
    val tail: MikaBubbleTail,
    val tailBias: Float = 0.5f,
    val cornerRadius: Dp = MikaSpeechBubbleDefaults.cornerRadius,
    val tailWidth: Dp = MikaSpeechBubbleDefaults.tailWidth,
    val tailDepth: Dp = MikaSpeechBubbleDefaults.tailDepth,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val depth = with(density) { tailDepth.toPx() }
        val halfTail = with(density) { tailWidth.toPx() } / 2f
        val rtl = layoutDirection == LayoutDirection.Rtl
        val side =
            when (tail) {
                MikaBubbleTail.Start -> if (rtl) Side.Right else Side.Left
                MikaBubbleTail.End -> if (rtl) Side.Left else Side.Right
                MikaBubbleTail.Top -> Side.Top
                MikaBubbleTail.Bottom -> Side.Bottom
            }
        val left = if (side == Side.Left) depth else 0f
        val top = if (side == Side.Top) depth else 0f
        val right = size.width - if (side == Side.Right) depth else 0f
        val bottom = size.height - if (side == Side.Bottom) depth else 0f
        // The tail edge must hold both corners plus the tail base, so short bubbles (a one-line
        // quip with a Start or End tail) shrink their corners instead of folding the outline.
        val tailEdge = if (side == Side.Left || side == Side.Right) bottom - top else right - left
        val radius =
            with(density) { cornerRadius.toPx() }
                .coerceAtMost(minOf(right - left, bottom - top) / 2f)
                .coerceAtMost((tailEdge - 2 * halfTail) / 2f)
                .coerceAtLeast(0f)
        val horizontalBias = if (rtl) 1f - tailBias else tailBias
        val path = Path()
        path.moveTo(left + radius, top)
        if (side == Side.Top) {
            val c = tailCenter(left, right, radius, halfTail, horizontalBias)
            path.lineTo(c - halfTail, top)
            path.lineTo(c, 0f)
            path.lineTo(c + halfTail, top)
        }
        path.lineTo(right - radius, top)
        path.corner(Rect(right - 2 * radius, top, right, top + 2 * radius), -90f, radius)
        if (side == Side.Right) {
            val c = tailCenter(top, bottom, radius, halfTail, tailBias)
            path.lineTo(right, c - halfTail)
            path.lineTo(size.width, c)
            path.lineTo(right, c + halfTail)
        }
        path.lineTo(right, bottom - radius)
        path.corner(Rect(right - 2 * radius, bottom - 2 * radius, right, bottom), 0f, radius)
        if (side == Side.Bottom) {
            val c = tailCenter(left, right, radius, halfTail, horizontalBias)
            path.lineTo(c + halfTail, bottom)
            path.lineTo(c, size.height)
            path.lineTo(c - halfTail, bottom)
        }
        path.lineTo(left + radius, bottom)
        path.corner(Rect(left, bottom - 2 * radius, left + 2 * radius, bottom), 90f, radius)
        if (side == Side.Left) {
            val c = tailCenter(top, bottom, radius, halfTail, tailBias)
            path.lineTo(left, c + halfTail)
            path.lineTo(0f, c)
            path.lineTo(left, c - halfTail)
        }
        path.lineTo(left, top + radius)
        path.corner(Rect(left, top, left + 2 * radius, top + 2 * radius), 180f, radius)
        path.close()
        return Outline.Generic(path)
    }

    private enum class Side { Left, Top, Right, Bottom }
}

private fun Path.corner(
    oval: Rect,
    startAngle: Float,
    radius: Float,
) {
    if (radius > 0f) arcTo(oval, startAngle, 90f, false)
}

/** Centre of the tail along an edge from [start] to [end], kept clear of the rounded corners. */
private fun tailCenter(
    start: Float,
    end: Float,
    radius: Float,
    halfTail: Float,
    bias: Float,
): Float {
    val low = start + radius + halfTail
    val high = end - radius - halfTail
    return if (low >= high) (start + end) / 2f else low + (high - low) * bias.coerceIn(0f, 1f)
}

/**
 * Mika's speech bubble: white body, 3dp ink border, 16dp corners and a tail on [tail], all one
 * outline ([MikaBubbleShape]).
 *
 * An optional speaker row ([speakerIcon], [speaker] in uppercase, [tag] pill) sits above
 * [content], and [footer] below it. TalkBack reads the bubble as one node.
 */
@Composable
fun MikaSpeechBubble(
    modifier: Modifier = Modifier,
    tail: MikaBubbleTail = MikaBubbleTail.Start,
    tailBias: Float = 0.5f,
    color: Color = HeyMikaTheme.colors.surface,
    borderColor: Color = HeyMikaTheme.colors.ink,
    borderWidth: Dp = HeyMikaTheme.dimens.borderInk,
    speaker: String? = null,
    speakerIcon: ImageVector? = null,
    speakerColor: Color = HeyMikaTheme.colors.primaryText,
    tag: String? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    val shape = remember(tail, tailBias) { MikaBubbleShape(tail = tail, tailBias = tailBias) }
    val inner = dimens.cardPaddingCompact
    val depth = MikaSpeechBubbleDefaults.tailDepth
    val padding =
        PaddingValues(
            start = inner + if (tail == MikaBubbleTail.Start) depth else 0.dp,
            top = inner + if (tail == MikaBubbleTail.Top) depth else 0.dp,
            end = inner + if (tail == MikaBubbleTail.End) depth else 0.dp,
            bottom = inner + if (tail == MikaBubbleTail.Bottom) depth else 0.dp,
        )
    val border =
        if (borderWidth > 0.dp && borderColor.alpha > 0f) {
            Modifier.border(borderWidth, borderColor, shape)
        } else {
            Modifier
        }
    Column(
        modifier =
            modifier
                .semantics(mergeDescendants = true) {}
                .background(color, shape)
                .then(border)
                .padding(padding),
        verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
    ) {
        CompositionLocalProvider(LocalContentColor provides HeyMikaTheme.colors.textPrimary) {
            if (speaker != null || tag != null) {
                SpeakerRow(speaker = speaker, icon = speakerIcon, color = speakerColor, tag = tag)
            }
            content()
            footer?.invoke(this)
        }
    }
}

/** [MikaSpeechBubble] holding a single line of copy. */
@Composable
fun MikaSpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    tail: MikaBubbleTail = MikaBubbleTail.Start,
    tailBias: Float = 0.5f,
    color: Color = HeyMikaTheme.colors.surface,
    borderColor: Color = HeyMikaTheme.colors.ink,
    borderWidth: Dp = HeyMikaTheme.dimens.borderInk,
    textStyle: TextStyle = HeyMikaTheme.typography.labelLg,
    textColor: Color = HeyMikaTheme.colors.textPrimary,
    speaker: String? = null,
    speakerIcon: ImageVector? = null,
    speakerColor: Color = HeyMikaTheme.colors.primaryText,
    tag: String? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    MikaSpeechBubble(
        text = AnnotatedString(text),
        modifier = modifier,
        tail = tail,
        tailBias = tailBias,
        color = color,
        borderColor = borderColor,
        borderWidth = borderWidth,
        textStyle = textStyle,
        textColor = textColor,
        speaker = speaker,
        speakerIcon = speakerIcon,
        speakerColor = speakerColor,
        tag = tag,
        footer = footer,
    )
}

/** [MikaSpeechBubble] holding styled copy, such as the output of RichText's markup mapper. */
@Composable
fun MikaSpeechBubble(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    tail: MikaBubbleTail = MikaBubbleTail.Start,
    tailBias: Float = 0.5f,
    color: Color = HeyMikaTheme.colors.surface,
    borderColor: Color = HeyMikaTheme.colors.ink,
    borderWidth: Dp = HeyMikaTheme.dimens.borderInk,
    textStyle: TextStyle = HeyMikaTheme.typography.labelLg,
    textColor: Color = HeyMikaTheme.colors.textPrimary,
    speaker: String? = null,
    speakerIcon: ImageVector? = null,
    speakerColor: Color = HeyMikaTheme.colors.primaryText,
    tag: String? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    MikaSpeechBubble(
        modifier = modifier,
        tail = tail,
        tailBias = tailBias,
        color = color,
        borderColor = borderColor,
        borderWidth = borderWidth,
        speaker = speaker,
        speakerIcon = speakerIcon,
        speakerColor = speakerColor,
        tag = tag,
        footer = footer,
    ) {
        Text(text = text, style = textStyle, color = textColor)
    }
}

@Composable
private fun SpeakerRow(
    speaker: String?,
    icon: ImageVector?,
    color: Color,
    tag: String?,
) {
    val dimens = HeyMikaTheme.dimens
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(dimens.iconSizeSmall),
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            if (speaker != null) {
                Text(text = speaker.uppercase(), style = HeyMikaTheme.typography.overline, color = color)
            }
        }
        if (tag != null) {
            Text(
                text = tag,
                modifier =
                    Modifier
                        .background(HeyMikaTheme.colors.badgeCyan, HeyMikaTheme.shapes.pill)
                        .padding(horizontal = dimens.spaceSm, vertical = dimens.spaceXxs),
                style = HeyMikaTheme.typography.caption,
                color = HeyMikaTheme.colors.onInfo,
                maxLines = 1,
            )
        }
    }
}

@Preview(name = "Tail sides", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun MikaSpeechBubbleTailsPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MikaSpeechBubble(text = "Ekor di kiri (Start).", tail = MikaBubbleTail.Start)
            MikaSpeechBubble(text = "Ekor di kanan (End).", tail = MikaBubbleTail.End)
            MikaSpeechBubble(
                text = "Satu baris pendek",
                tail = MikaBubbleTail.Start,
                textStyle = HeyMikaTheme.typography.caption,
            )
            MikaSpeechBubble(
                text = "“Wah, Mika cari ke mana-mana halamannya nggak ketemu, meong!”",
                modifier = Modifier.fillMaxWidth(),
                tail = MikaBubbleTail.Top,
            )
            MikaSpeechBubble(
                text = "Ekor di bawah, digeser ke awal.",
                modifier = Modifier.fillMaxWidth(),
                tail = MikaBubbleTail.Bottom,
                tailBias = 0.1f,
            )
        }
    }
}

@Preview(name = "Speaker, tag and footer", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun MikaSpeechBubbleSpeakerPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MikaSpeechBubble(
                text = "\"Halo calon juara! Siapa nama panggilan kerenmu? Biar Mika bisa sapa kamu pas belajar!\"",
                modifier = Modifier.fillMaxWidth(),
                speaker = "Sapaan Mika",
                speakerIcon = Icons.Filled.ChatBubble,
                footer = {
                    Text(
                        text = "Tips: Belajar rutin 15–30 menit sehari lebih awet nempel di otak!",
                        style = HeyMikaTheme.typography.caption,
                        color = HeyMikaTheme.colors.textMuted,
                    )
                },
            )
            MikaSpeechBubble(
                text = "“Tanya saja, jangan ragu! Nggak ada pertanyaan yang bodoh di sini, Meow!”",
                modifier = Modifier.fillMaxWidth(),
                color = HeyMikaTheme.colors.canvas,
                borderColor = HeyMikaTheme.colors.borderMuted,
                borderWidth = HeyMikaTheme.dimens.borderSubtle,
                textStyle = HeyMikaTheme.typography.bodySm.copy(fontStyle = FontStyle.Italic),
                speaker = "Mika Berbisik",
                speakerIcon = Icons.Filled.Pets,
                speakerColor = HeyMikaTheme.colors.warningContent,
                tag = "Fokus 100%",
            )
        }
    }
}
