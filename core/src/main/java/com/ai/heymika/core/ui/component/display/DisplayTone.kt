package com.ai.heymika.core.ui.component.display

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.core.ui.theme.ToneSpec

/**
 * The named tones of the display components: pills, tags, badges, filter chips and stat icons.
 *
 * Each tone maps onto a theme [ToneSpec] through [PillDefaults.toneSpec]. Domain enums
 * (WorkspaceCategory, MasteryBand, ...) keep their own ToneSpec maps and pass them through
 * `PillDefaults.colors(toneSpec, style)`.
 * - [Red] is the ribbonRed urgency tone ("Segera", "Prioritas"). It is never an error: errors use
 *   [Danger].
 * - [Cyan] is the badgeCyan brand hero pill with ink text.
 * - [Neutral] is the quiet slate-like tone for difficulty tags and inactive counters; filled, it is the
 *   ink kicker ("FOKUS UTAMA").
 */
enum class DisplayTone { Primary, Orange, Sky, Yellow, Red, Cyan, Success, Danger, Neutral }

/**
 * - [Tinted]: a light tone container with the tone's dark content. The default for tags and status
 *   pills.
 * - [Filled]: a saturated fill, for active states, count badges and kickers.
 * - [Outline]: a white face with a 1.5dp border in the content colour.
 */
enum class PillStyle { Tinted, Filled, Outline }

/** [Small] uses labelSm (12sp), [Medium] labelMd (14sp). Neither has a fixed height. */
enum class PillSize { Small, Medium }

/** Colours of a pill, tag or badge. [border] is drawn only when not transparent; [dot] tints the leading dot. */
@Immutable
data class PillColors(
    val container: Color,
    val content: Color,
    val border: Color,
    val dot: Color,
)

object PillDefaults {
    /**
     * The [ToneSpec] behind each [DisplayTone]. Every content/container pair meets 4.5:1; blue text
     * uses primaryText (#005AB5), never primary.
     */
    @Composable
    @ReadOnlyComposable
    fun toneSpec(tone: DisplayTone): ToneSpec {
        val c = HeyMikaTheme.colors
        val t = HeyMikaTheme.tones
        return when (tone) {
            DisplayTone.Primary -> ToneSpec(t.blue.container, c.primaryText, c.primary)
            DisplayTone.Orange -> t.orange
            DisplayTone.Sky -> ToneSpec(c.tertiaryFixed, c.onTertiaryFixedVariant, c.sky)
            DisplayTone.Yellow -> t.yellow
            DisplayTone.Red -> t.ribbonRed
            DisplayTone.Cyan -> t.sky
            DisplayTone.Success -> t.success
            DisplayTone.Danger -> t.danger
            DisplayTone.Neutral -> ToneSpec(MaterialTheme.colorScheme.surfaceContainerLow, c.textMuted, c.textMuted)
        }
    }

    /**
     * Colours of a [DisplayTone] in a [PillStyle]. Filled uses the brand fills (primary, orange with
     * ink, sky with ink, yellow with ink, ink for Neutral); Red fills with the dark ribbon tone, because
     * white text on #EF3E36 fails contrast.
     */
    @Composable
    @ReadOnlyComposable
    fun colors(
        tone: DisplayTone,
        style: PillStyle = PillStyle.Tinted,
    ): PillColors {
        val spec = toneSpec(tone)
        if (style != PillStyle.Filled) return colors(spec, style)
        val c = HeyMikaTheme.colors
        val scheme = MaterialTheme.colorScheme
        return when (tone) {
            DisplayTone.Primary -> filled(c.primary, c.onPrimary)
            DisplayTone.Orange -> filled(c.accent, c.onAccent)
            DisplayTone.Sky -> filled(c.sky, c.ink)
            DisplayTone.Yellow -> filled(c.yellow, c.ink)
            DisplayTone.Red -> filled(spec.content, c.surface)
            DisplayTone.Cyan -> filled(scheme.tertiaryContainer, scheme.onTertiaryContainer)
            DisplayTone.Success -> filled(c.success, c.surface)
            DisplayTone.Danger -> filled(c.danger, c.onDanger)
            DisplayTone.Neutral -> filled(c.ink, c.surface)
        }
    }

    /**
     * Colours of any [ToneSpec] (for domain tone maps). Filled uses the tone's content colour as the
     * fill with white text, which keeps the 4.5:1 pair.
     */
    @Composable
    @ReadOnlyComposable
    fun colors(
        tone: ToneSpec,
        style: PillStyle = PillStyle.Tinted,
    ): PillColors {
        val surface = HeyMikaTheme.colors.surface
        return when (style) {
            PillStyle.Tinted -> PillColors(tone.container, tone.content, Color.Transparent, tone.accent)
            PillStyle.Filled -> PillColors(tone.content, surface, Color.Transparent, surface)
            PillStyle.Outline -> PillColors(surface, tone.content, tone.content, tone.accent)
        }
    }

    @Composable
    @ReadOnlyComposable
    fun textStyle(size: PillSize): TextStyle =
        if (size == PillSize.Small) HeyMikaTheme.typography.labelSm else HeyMikaTheme.typography.labelMd

    /** 12dp for Small, 16dp for Medium. */
    @Composable
    @ReadOnlyComposable
    fun iconSize(size: PillSize): Dp {
        val base = HeyMikaTheme.dimens.iconSizeSmall
        return if (size == PillSize.Small) base * SMALL_ICON_SCALE else base
    }

    private fun filled(
        container: Color,
        content: Color,
    ): PillColors = PillColors(container, content, Color.Transparent, content)
}

private const val SMALL_ICON_SCALE = 0.75f
