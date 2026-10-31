package com.ai.heymika.core.ui.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.isSpecified
import com.ai.heymika.core.domain.text.FormulaScript
import com.ai.heymika.core.domain.text.MarkupEmphasis
import com.ai.heymika.core.domain.text.MarkupParser
import com.ai.heymika.core.domain.text.MarkupSegment
import com.ai.heymika.core.domain.text.MarkupTone
import com.ai.heymika.core.domain.text.ScriptPosition
import com.ai.heymika.core.ui.component.foundation.ComicCard
import com.ai.heymika.core.ui.component.foundation.ComicCardVariant
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * Colours of [RichText] and [InlineCodeChip]. The defaults assume a white or canvas surface; pass
 * your own on a filled surface (for example the blue user bubble) so every tone keeps 4.5:1.
 */
@Immutable
data class RichTextColors(
    val primary: Color,
    val success: Color,
    val danger: Color,
    val warning: Color,
    val highlight: Color,
    val codeContainer: Color,
    val codeContent: Color,
) {
    fun tone(tone: MarkupTone): Color =
        when (tone) {
            MarkupTone.PRIMARY -> primary
            MarkupTone.SUCCESS -> success
            MarkupTone.DANGER -> danger
            MarkupTone.WARNING -> warning
        }
}

object RichTextDefaults {
    private const val HIGHLIGHT_ALPHA = 0.5f

    /** Inline code wider than this is drawn as a wrapping span instead of a chip, so it never overflows. */
    val maxChipWidth: Dp get() = 200.dp

    /**
     * Tones follow the contrast rules: blue text is primaryText (never primary), and the highlight
     * is brand yellow at 50% so every tone colour on it stays above 4.5:1 on white.
     */
    @Composable
    @ReadOnlyComposable
    fun colors(): RichTextColors {
        val c = HeyMikaTheme.colors
        return RichTextColors(
            primary = c.primaryText,
            success = c.success,
            danger = c.danger,
            warning = c.warningContent,
            highlight = c.yellow.copy(alpha = HIGHLIGHT_ALPHA),
            codeContainer = MaterialTheme.colorScheme.surfaceContainerHigh,
            codeContent = c.primaryText,
        )
    }
}

/**
 * Renders Mika's lightweight markup (see [MarkupParser]): bold, italic, highlight, `[text]{.tone}`
 * colours, formulas (with `_`/`^` scripts, see [FormulaScript]), and inline code as [InlineCodeChip]s.
 * Toned text is also bold, so colour is never the only signal. Unbalanced markers show up literally.
 *
 * TalkBack reads the plain text; each chip is announced through its alternate text.
 */
@Composable
fun RichText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = HeyMikaTheme.typography.bodyMd,
    color: Color = Color.Unspecified,
    colors: RichTextColors = RichTextDefaults.colors(),
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val segments = remember(text) { MarkupParser.parse(text) }
    RichText(
        segments = segments,
        modifier = modifier,
        style = style,
        color = color,
        colors = colors,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

/** Same as the String overload, for segments a ViewModel already parsed off the main thread. */
@Composable
fun RichText(
    segments: List<MarkupSegment>,
    modifier: Modifier = Modifier,
    style: TextStyle = HeyMikaTheme.typography.bodyMd,
    color: Color = Color.Unspecified,
    colors: RichTextColors = RichTextDefaults.colors(),
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val chipStyle = inlineCodeTextStyle(style)
    val chipPadding = HeyMikaTheme.dimens.spaceXs
    val maxChipWidth = RichTextDefaults.maxChipWidth
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val content =
        remember(segments, colors, chipStyle, chipPadding, maxChipWidth, density, measurer) {
            val chipMetrics = ChipMetrics(chipStyle, chipPadding, maxChipWidth, density, measurer)
            buildRichContent(segments, colors, chipMetrics)
        }
    Text(
        text = content.text,
        modifier = modifier,
        color = color,
        style = style,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        inlineContent = content.inlineContent,
    )
}

/** Monospace at 90% of the surrounding text, so a chip fits inside the line height. */
@Composable
@ReadOnlyComposable
internal fun inlineCodeTextStyle(base: TextStyle): TextStyle {
    val fontSize = if (base.fontSize.isSpecified) base.fontSize else HeyMikaTheme.typography.bodyMd.fontSize
    return HeyMikaTheme.typography.mono.copy(fontSize = fontSize * CODE_FONT_SCALE, lineHeight = CODE_LINE_HEIGHT)
}

private const val CODE_FONT_SCALE = 0.9f
private val CODE_LINE_HEIGHT = 1.2.em
private val CHIP_HEIGHT = 1.3.em

private class RichContent(
    val text: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent>,
)

private class ChipMetrics(
    val style: TextStyle,
    val padding: Dp,
    val maxWidth: Dp,
    val density: Density,
    val measurer: TextMeasurer,
) {
    /** A placeholder sized to the measured code, or null when the chip would be too wide. */
    fun placeholderFor(code: String): Placeholder? {
        val textWidth = measurer.measure(code, style = style, maxLines = 1, softWrap = false).size.width
        val widthPx = textWidth + 2 * with(density) { padding.roundToPx() }
        if (widthPx > with(density) { maxWidth.toPx() }) return null
        return Placeholder(
            width = with(density) { widthPx.toSp() },
            height = CHIP_HEIGHT,
            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
        )
    }
}

private fun buildRichContent(
    segments: List<MarkupSegment>,
    colors: RichTextColors,
    chips: ChipMetrics,
): RichContent {
    val inlineContent = HashMap<String, InlineTextContent>()
    val text =
        buildAnnotatedString {
            segments.forEachIndexed { index, segment ->
                when (segment) {
                    is MarkupSegment.Text -> {
                        withStyle(textSpanStyle(segment, colors)) { append(segment.text) }
                    }

                    is MarkupSegment.Code -> {
                        val placeholder = chips.placeholderFor(segment.text)
                        if (placeholder != null) {
                            val id = "code-$index"
                            inlineContent[id] =
                                InlineTextContent(placeholder) {
                                    InlineCodeChip(
                                        text = segment.text,
                                        modifier = Modifier.fillMaxSize().clearAndSetSemantics {},
                                        tone = segment.tone,
                                        textStyle = chips.style,
                                        colors = colors,
                                    )
                                }
                            appendInlineContent(id, alternateText = segment.text)
                        } else {
                            withStyle(longCodeSpanStyle(segment, colors, chips.style)) { append(segment.text) }
                        }
                    }

                    is MarkupSegment.Formula -> {
                        withStyle(formulaSpanStyle(segment, colors)) {
                            FormulaScript.split(segment.text).forEach { run ->
                                val shift = run.position.baselineShift()
                                if (shift == null) {
                                    append(run.text)
                                } else {
                                    withStyle(SpanStyle(baselineShift = shift, fontSize = SCRIPT_FONT_SCALE.em)) {
                                        append(run.text)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    return RichContent(text, inlineContent)
}

private fun textSpanStyle(
    segment: MarkupSegment.Text,
    colors: RichTextColors,
): SpanStyle {
    val toneColor = segment.tone?.let(colors::tone)
    val bold = MarkupEmphasis.BOLD in segment.emphasis || toneColor != null
    return SpanStyle(
        color = toneColor ?: Color.Unspecified,
        fontWeight = if (bold) FontWeight.Bold else null,
        fontStyle = if (MarkupEmphasis.ITALIC in segment.emphasis) FontStyle.Italic else null,
        background = if (MarkupEmphasis.HIGHLIGHT in segment.emphasis) colors.highlight else Color.Unspecified,
    )
}

private fun longCodeSpanStyle(
    segment: MarkupSegment.Code,
    colors: RichTextColors,
    chipStyle: TextStyle,
): SpanStyle =
    SpanStyle(
        color = segment.tone?.let(colors::tone) ?: colors.codeContent,
        fontFamily = chipStyle.fontFamily,
        fontWeight = chipStyle.fontWeight,
        fontSize = chipStyle.fontSize,
        background = colors.codeContainer,
    )

private const val SCRIPT_FONT_SCALE = 0.75f

private fun ScriptPosition.baselineShift(): BaselineShift? =
    when (this) {
        ScriptPosition.BASELINE -> null
        ScriptPosition.SUBSCRIPT -> BaselineShift.Subscript
        ScriptPosition.SUPERSCRIPT -> BaselineShift.Superscript
    }

private fun formulaSpanStyle(
    segment: MarkupSegment.Formula,
    colors: RichTextColors,
): SpanStyle =
    SpanStyle(
        color = segment.tone?.let(colors::tone) ?: Color.Unspecified,
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Italic,
    )

@Preview(name = "Emphasis", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun RichTextEmphasisPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RichText(text = "Teks biasa tanpa penanda apa pun.")
            RichText(text = "Reaksi ketiga dikatalisis **Fosfofruktokinase-1** dan disebut *committed step*.")
            RichText(text = "Ingat ==hasil bersih +2 ATP== ya, ***bukan 4***!")
        }
    }
}

@Preview(name = "Tones", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun RichTextTonesPreview() {
    HeyMikaTheme {
        ComicCard(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            variant = ComicCardVariant.Subtle,
        ) {
            RichText(
                text =
                    "Tahap Investasi memakai [2 ATP]{.danger}, lalu Tahap Payoff menghasilkan [4 ATP]{.success}. " +
                        "Jadi perolehan *BERSIH (Netto)* tetap [+2 ATP]{.primary}, " +
                        "[bukan 4!]{.warning} Catat yaa~",
                style = HeyMikaTheme.typography.bodySm,
            )
        }
    }
}

@Preview(name = "Code and formulas", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun RichTextCodePreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RichText(
                text =
                    "Pencarian rata-rata `O(log n)`, namun dapat memburuk menjadi [`O(n)`]{.danger} " +
                        "bila pohon condong tak berimbang.",
                style = HeyMikaTheme.typography.bodySm,
            )
            RichText(text = "Relaksasi sisi total mencapai `O((V + E) log V)` dengan Binary Min-Heap.")
            RichText(text = "Kode panjang `val hasil = daftar.filter { it.aktif }.map { it.nama }` ikut terbungkus.")
            RichText(text = "Rumus energi \$E = mc^2\$ dan pecahan \$\$\\frac{a}{b}\$\$ tampil miring.")
        }
    }
}

@Preview(name = "Unbalanced markers stay literal", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun RichTextUnbalancedPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RichText(text = "**tebal tanpa penutup, `kode terbuka, dan \$5 atau \$10")
            RichText(text = "Nada tak dikenal [ungu]{.ungu} dan nama_variabel_ini tetap utuh.")
        }
    }
}

@Preview(
    name = "Large font scale",
    showBackground = true,
    backgroundColor = 0xFFF8FCFF,
    widthDp = 360,
    fontScale = 1.6f,
)
@Composable
private fun RichTextLargeFontPreview() {
    HeyMikaTheme {
        RichText(
            text = "Chip `O(log n)` tetap muat, chip `O((V + E) log V)` jadi teks terbungkus.",
            modifier = Modifier.padding(16.dp),
        )
    }
}
