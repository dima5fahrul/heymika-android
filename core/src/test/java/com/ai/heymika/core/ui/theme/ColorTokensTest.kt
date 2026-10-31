package com.ai.heymika.core.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class ColorTokensTest {
    private val colors = HeyMikaColors()
    private val scheme = HeyMikaLightColorScheme
    private val white = Color(0xFFFFFFFF)

    @Test
    fun `brand tokens follow the product owner decisions`() {
        assertEquals(Color(0xFF0075E8), HeyMikaPalette.primary)
        assertEquals(Color(0xFF005AB5), HeyMikaPalette.primaryText)
        assertEquals(Color(0xFF082A52), HeyMikaPalette.ink)
        assertEquals(Color(0xFFFF8A00), HeyMikaPalette.shadowOrange)
        assertEquals(Color(0xFF005FC4), HeyMikaPalette.primaryPressShadow)
        assertEquals(Color(0xFFF8FCFF), HeyMikaPalette.canvas)
        assertEquals(Color(0xFFBC2535), HeyMikaPalette.danger)
        assertEquals(Color(0xFFFFF0F2), HeyMikaPalette.dangerBg)
        assertEquals(Color(0xFF167549), HeyMikaPalette.success)
    }

    @Test
    fun `material scheme applies the overrides`() {
        assertEquals(HeyMikaPalette.primary, scheme.primary)
        assertEquals(HeyMikaPalette.canvas, scheme.background)
        assertEquals(HeyMikaPalette.onBackground, scheme.onBackground)
        assertEquals(HeyMikaPalette.surface, scheme.surface)
        assertEquals(HeyMikaPalette.danger, scheme.error)
        assertEquals(HeyMikaPalette.dangerBg, scheme.errorContainer)
        assertEquals(HeyMikaPalette.danger, scheme.onErrorContainer)
        assertEquals(Color(0xFF005CBA), scheme.surfaceTint)
    }

    @Test
    fun `material scheme takes every yaml slot`() {
        assertEquals(HeyMikaPalette.surfaceDim, scheme.surfaceDim)
        assertEquals(HeyMikaPalette.surfaceBright, scheme.surfaceBright)
        assertEquals(HeyMikaPalette.surfaceContainerLowest, scheme.surfaceContainerLowest)
        assertEquals(HeyMikaPalette.surfaceContainerLow, scheme.surfaceContainerLow)
        assertEquals(HeyMikaPalette.surfaceContainer, scheme.surfaceContainer)
        assertEquals(HeyMikaPalette.surfaceContainerHigh, scheme.surfaceContainerHigh)
        assertEquals(HeyMikaPalette.surfaceContainerHighest, scheme.surfaceContainerHighest)
        assertEquals(HeyMikaPalette.inversePrimary, scheme.inversePrimary)
        assertEquals(HeyMikaPalette.secondaryContainer, scheme.secondaryContainer)
        assertEquals(HeyMikaPalette.tertiary, scheme.tertiary)
        assertEquals(HeyMikaPalette.outlineVariant, scheme.outlineVariant)
        assertEquals(HeyMikaPalette.surfaceVariant, scheme.surfaceVariant)
    }

    @Test
    fun `extended colours map the comic roles`() {
        assertEquals(HeyMikaPalette.shadowOrange, colors.accent)
        assertEquals(HeyMikaPalette.shadowOrange, colors.comicShadow)
        assertEquals(HeyMikaPalette.ink, colors.onAccent)
        assertEquals(HeyMikaPalette.primary, colors.focusRing)
        assertEquals(HeyMikaPalette.primaryText, colors.primaryText)
        assertEquals(Color(0xFF001B3C), colors.textPrimary)
        assertEquals(HeyMikaPalette.primaryFixed, colors.primaryFixed)
        assertEquals(HeyMikaPalette.onTertiaryFixedVariant, colors.onTertiaryFixedVariant)
    }

    @Test
    fun `blue text on white uses primaryText because primary is below AA`() {
        assertAa(colors.primaryText, white, "primaryText on white")
        assertTrue(contrast(colors.primary, white) < AA)
    }

    @Test
    fun `labels on orange use ink because white is below AA`() {
        assertAa(colors.onAccent, colors.accent, "onAccent on accent")
        assertTrue(contrast(white, colors.accent) < AA)
    }

    @Test
    fun `white on primary meets at least the large text level`() {
        // Today 4.46:1: enough for icons and bold 18.66sp+ text, just under 4.5:1 for 16sp button labels.
        assertTrue(contrast(colors.onPrimary, colors.primary) >= AA_LARGE)
    }

    @Test
    fun `body text, danger and success pairs meet AA`() {
        assertAa(colors.textPrimary, colors.canvas, "textPrimary on canvas")
        assertAa(colors.textMuted, colors.surface, "textMuted on surface")
        assertAa(colors.danger, colors.dangerBg, "danger on dangerBg")
        assertAa(colors.success, colors.successBg, "success on successBg")
        assertAa(colors.warningContent, colors.warningBg, "warningContent on warningBg")
        assertAa(colors.onInfo, colors.info, "onInfo on info")
        assertAa(colors.disabledContent, colors.disabledContainer, "disabledContent on disabledContainer")
    }

    @Test
    fun `every tone content colour meets AA on its container`() {
        val tones = HeyMikaTones()
        mapOf(
            "blue" to tones.blue,
            "teal" to tones.teal,
            "orange" to tones.orange,
            "emerald" to tones.emerald,
            "rose" to tones.rose,
            "purple" to tones.purple,
            "slate" to tones.slate,
            "sky" to tones.sky,
            "yellow" to tones.yellow,
            "danger" to tones.danger,
            "success" to tones.success,
            "warning" to tones.warning,
            "info" to tones.info,
            "outline" to tones.outline,
            "primaryContainer" to tones.primaryContainer,
            "ribbonRed" to tones.ribbonRed,
            "tertiaryFixedDim" to tones.tertiaryFixedDim,
            "secondaryFixedDim" to tones.secondaryFixedDim,
        ).forEach { (name, tone) -> assertAa(tone.content, tone.container, "tone $name") }
    }

    @Test
    fun `rich text tones stay readable on code chips and highlights`() {
        val chip = scheme.surfaceContainerHigh
        val highlight = colors.yellow.copy(alpha = 0.5f).over(white)
        listOf(colors.primaryText, colors.success, colors.danger, colors.warningContent, colors.textPrimary)
            .forEach { tone ->
                assertAa(tone, chip, "$tone on code chip")
                assertAa(tone, highlight, "$tone on highlight")
            }
    }

    @Test
    fun `saturated error-state pills meet AA`() {
        mapOf(
            "danger" to (colors.danger to colors.onDanger),
            "secondary" to (scheme.secondary to scheme.onSecondary),
            "textMuted" to (colors.textMuted to colors.surface),
            "tertiary" to (scheme.tertiary to scheme.onTertiary),
            "accent" to (colors.accent to scheme.onSecondaryContainer),
            "quota" to (HeyMikaTones().ribbonRed.content to colors.surface),
        ).forEach { (name, pair) -> assertAa(pair.second, pair.first, "$name pill") }
    }

    @Test
    fun `toast text, action and icons stay readable on the inverse surface`() {
        val toast = scheme.inverseSurface
        assertAa(scheme.inverseOnSurface, toast, "inverseOnSurface on toast")
        assertAa(scheme.inversePrimary, toast, "inversePrimary on toast")
        listOf(colors.yellow, colors.accent, colors.inverseDanger).forEach { icon ->
            assertTrue("$icon icon on toast", contrast(icon, toast) >= AA_LARGE)
        }
    }

    private fun assertAa(
        foreground: Color,
        background: Color,
        label: String,
    ) {
        val ratio = contrast(foreground, background)
        assertTrue("$label is %.2f:1".format(ratio), ratio >= AA)
    }

    /** WCAG 2.x contrast ratio of two opaque sRGB colours. */
    private fun contrast(
        first: Color,
        second: Color,
    ): Double {
        val a = luminance(first)
        val b = luminance(second)
        return (max(a, b) + 0.05) / (min(a, b) + 0.05)
    }

    private fun luminance(color: Color): Double {
        fun channel(value: Float): Double {
            val v = value.toDouble()
            return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
    }

    /** Source-over in sRGB, like CSS: what a translucent token looks like on [background]. */
    private fun Color.over(background: Color): Color =
        Color(
            red = red * alpha + background.red * (1 - alpha),
            green = green * alpha + background.green * (1 - alpha),
            blue = blue * alpha + background.blue * (1 - alpha),
        )

    private companion object {
        const val AA = 4.5
        const val AA_LARGE = 3.0
    }
}
