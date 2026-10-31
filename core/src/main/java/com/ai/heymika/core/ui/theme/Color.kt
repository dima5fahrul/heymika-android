package com.ai.heymika.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Every raw colour of the HeyMika design system (DESIGN.md YAML + prose), named after its token.
 *
 * Screens and components never read this object directly: they use [HeyMikaTheme.colors] or
 * `MaterialTheme.colorScheme`. It is public so token tests and modifier defaults can reach it.
 */
object HeyMikaPalette {
    // Brand (DESIGN.md prose). primary is the PO override of the YAML #005AB5.
    val ink = Color(0xFF082A52)
    val primary = Color(0xFF0075E8)
    val primaryPressShadow = Color(0xFF005FC4)
    val primaryText = Color(0xFF005AB5)
    val shadowOrange = Color(0xFFFF8A00)

    // Named accents.
    val brandSky = Color(0xFF16B5F5)
    val brandYellow = Color(0xFFFFD34E)
    val ribbonRed = Color(0xFFEF3E36)
    val badgeCyan = Color(0xFFC5EFFF)
    val canvas = Color(0xFFF8FCFF)
    val borderMuted = Color(0xFFD8DDF4)
    val textMuted = Color(0xFF5868A4)
    val danger = Color(0xFFBC2535)
    val dangerBg = Color(0xFFFFF0F2)
    val success = Color(0xFF167549)

    // Danger on the inverse surface (toast error icon): the Material dark-scheme error tone.
    val inverseDanger = Color(0xFFFFB4AB)

    // Derived: success at 10% over white, ink at 6% (subtle float), primary at 25% (focus halo).
    val successBg = Color(0xFFE8F1ED)
    val subtleShadow = Color(0x0F082A52)
    val focusHalo = Color(0x400075E8)

    // YAML slots.
    val surface = Color(0xFFFFFFFF)
    val surfaceDim = Color(0xFFC8DBFF)
    val surfaceBright = Color(0xFFF9F9FF)
    val surfaceContainerLowest = Color(0xFFFFFFFF)
    val surfaceContainerLow = Color(0xFFF0F3FF)
    val surfaceContainer = Color(0xFFE7EEFF)
    val surfaceContainerHigh = Color(0xFFDEE8FF)
    val surfaceContainerHighest = Color(0xFFD6E3FF)
    val onSurface = Color(0xFF001B3C)
    val onSurfaceVariant = Color(0xFF414754)
    val inverseSurface = Color(0xFF113059)
    val inverseOnSurface = Color(0xFFECF1FF)
    val outline = Color(0xFF717785)
    val outlineVariant = Color(0xFFC1C6D6)
    val surfaceTint = Color(0xFF005CBA)
    val onPrimary = Color(0xFFFFFFFF)
    val primaryContainer = Color(0xFF0072E3)
    val onPrimaryContainer = Color(0xFFFEFCFF)
    val inversePrimary = Color(0xFFAAC7FF)
    val secondary = Color(0xFF914C00)
    val onSecondary = Color(0xFFFFFFFF)
    val secondaryContainer = Color(0xFFFF8A00)
    val onSecondaryContainer = Color(0xFF613100)
    val tertiary = Color(0xFF006388)
    val onTertiary = Color(0xFFFFFFFF)
    val tertiaryContainer = Color(0xFF007DAB)
    val onTertiaryContainer = Color(0xFFFCFCFF)
    val onError = Color(0xFFFFFFFF)
    val primaryFixed = Color(0xFFD7E3FF)
    val primaryFixedDim = Color(0xFFAAC7FF)
    val onPrimaryFixed = Color(0xFF001B3E)
    val onPrimaryFixedVariant = Color(0xFF00458E)
    val secondaryFixed = Color(0xFFFFDCC4)
    val secondaryFixedDim = Color(0xFFFFB77F)
    val onSecondaryFixed = Color(0xFF2F1500)
    val onSecondaryFixedVariant = Color(0xFF6F3900)
    val tertiaryFixed = Color(0xFFC5E7FF)
    val tertiaryFixedDim = Color(0xFF7FD0FF)
    val onTertiaryFixed = Color(0xFF001E2D)
    val onTertiaryFixedVariant = Color(0xFF004C6A)
    val onBackground = Color(0xFF001B3C)
    val surfaceVariant = Color(0xFFD6E3FF)

    // YAML values kept for reference only; the design uses the overrides above instead.
    val yamlPrimary = Color(0xFF005AB5)
    val yamlBackground = Color(0xFFF9F9FF)
    val yamlError = Color(0xFFBA1A1A)
    val yamlErrorContainer = Color(0xFFFFDAD6)
    val yamlOnErrorContainer = Color(0xFF93000A)
}

/**
 * The Material 3 light scheme, filled from every YAML slot that Material3 1.3 knows about.
 *
 * Overrides: primary is #0075E8 (PO decision), background is canvas #F8FCFF, and the error family
 * maps to danger/dangerBg. surfaceTint keeps the YAML #005CBA; HeyMikaTheme disables tonal
 * elevation, so it never tints a surface. The fixed families live in [HeyMikaColors].
 */
val HeyMikaLightColorScheme: ColorScheme =
    lightColorScheme(
        primary = HeyMikaPalette.primary,
        onPrimary = HeyMikaPalette.onPrimary,
        primaryContainer = HeyMikaPalette.primaryContainer,
        onPrimaryContainer = HeyMikaPalette.onPrimaryContainer,
        inversePrimary = HeyMikaPalette.inversePrimary,
        secondary = HeyMikaPalette.secondary,
        onSecondary = HeyMikaPalette.onSecondary,
        secondaryContainer = HeyMikaPalette.secondaryContainer,
        onSecondaryContainer = HeyMikaPalette.onSecondaryContainer,
        tertiary = HeyMikaPalette.tertiary,
        onTertiary = HeyMikaPalette.onTertiary,
        tertiaryContainer = HeyMikaPalette.tertiaryContainer,
        onTertiaryContainer = HeyMikaPalette.onTertiaryContainer,
        background = HeyMikaPalette.canvas,
        onBackground = HeyMikaPalette.onBackground,
        surface = HeyMikaPalette.surface,
        onSurface = HeyMikaPalette.onSurface,
        surfaceVariant = HeyMikaPalette.surfaceVariant,
        onSurfaceVariant = HeyMikaPalette.onSurfaceVariant,
        surfaceTint = HeyMikaPalette.surfaceTint,
        inverseSurface = HeyMikaPalette.inverseSurface,
        inverseOnSurface = HeyMikaPalette.inverseOnSurface,
        error = HeyMikaPalette.danger,
        onError = HeyMikaPalette.onError,
        errorContainer = HeyMikaPalette.dangerBg,
        onErrorContainer = HeyMikaPalette.danger,
        outline = HeyMikaPalette.outline,
        outlineVariant = HeyMikaPalette.outlineVariant,
        surfaceBright = HeyMikaPalette.surfaceBright,
        surfaceContainer = HeyMikaPalette.surfaceContainer,
        surfaceContainerHigh = HeyMikaPalette.surfaceContainerHigh,
        surfaceContainerHighest = HeyMikaPalette.surfaceContainerHighest,
        surfaceContainerLow = HeyMikaPalette.surfaceContainerLow,
        surfaceContainerLowest = HeyMikaPalette.surfaceContainerLowest,
        surfaceDim = HeyMikaPalette.surfaceDim,
    )

/**
 * Tokens that have no Material 3 slot. Read them through [HeyMikaTheme.colors].
 *
 * Pairing rules: text on [surface]/[canvas] uses `colorScheme.onSurface` or [ink]; links and small
 * blue text use [primaryText] (6.7:1), never [primary] (4.46:1). Labels on [accent] use [onAccent]
 * (ink, 6:1) because white on orange is only 2.4:1.
 */
@Immutable
class HeyMikaColors(
    val ink: Color = HeyMikaPalette.ink,
    val primary: Color = HeyMikaPalette.primary,
    val onPrimary: Color = HeyMikaPalette.onPrimary,
    val primaryText: Color = HeyMikaPalette.primaryText,
    val primaryPressShadow: Color = HeyMikaPalette.primaryPressShadow,
    val accent: Color = HeyMikaPalette.shadowOrange,
    val onAccent: Color = HeyMikaPalette.ink,
    val accentPressShadow: Color = HeyMikaPalette.secondary,
    val comicShadow: Color = HeyMikaPalette.shadowOrange,
    val sky: Color = HeyMikaPalette.brandSky,
    val yellow: Color = HeyMikaPalette.brandYellow,
    val ribbonRed: Color = HeyMikaPalette.ribbonRed,
    val badgeCyan: Color = HeyMikaPalette.badgeCyan,
    val canvas: Color = HeyMikaPalette.canvas,
    val surface: Color = HeyMikaPalette.surface,
    val textPrimary: Color = HeyMikaPalette.onSurface,
    val textMuted: Color = HeyMikaPalette.textMuted,
    val borderMuted: Color = HeyMikaPalette.borderMuted,
    val danger: Color = HeyMikaPalette.danger,
    val onDanger: Color = HeyMikaPalette.onError,
    val dangerBg: Color = HeyMikaPalette.dangerBg,
    val dangerPressShadow: Color = HeyMikaPalette.onSecondaryFixedVariant,
    val inverseDanger: Color = HeyMikaPalette.inverseDanger,
    val success: Color = HeyMikaPalette.success,
    val successBg: Color = HeyMikaPalette.successBg,
    val warningBg: Color = HeyMikaPalette.secondaryFixed,
    val warningContent: Color = HeyMikaPalette.secondary,
    val info: Color = HeyMikaPalette.badgeCyan,
    val onInfo: Color = HeyMikaPalette.ink,
    val focusRing: Color = HeyMikaPalette.primary,
    val focusHalo: Color = HeyMikaPalette.focusHalo,
    val subtleShadow: Color = HeyMikaPalette.subtleShadow,
    val disabledContainer: Color = HeyMikaPalette.surfaceContainer,
    val disabledContent: Color = HeyMikaPalette.textMuted,
    val disabledBorder: Color = HeyMikaPalette.borderMuted,
    val primaryFixed: Color = HeyMikaPalette.primaryFixed,
    val primaryFixedDim: Color = HeyMikaPalette.primaryFixedDim,
    val onPrimaryFixed: Color = HeyMikaPalette.onPrimaryFixed,
    val onPrimaryFixedVariant: Color = HeyMikaPalette.onPrimaryFixedVariant,
    val secondaryFixed: Color = HeyMikaPalette.secondaryFixed,
    val secondaryFixedDim: Color = HeyMikaPalette.secondaryFixedDim,
    val onSecondaryFixed: Color = HeyMikaPalette.onSecondaryFixed,
    val onSecondaryFixedVariant: Color = HeyMikaPalette.onSecondaryFixedVariant,
    val tertiaryFixed: Color = HeyMikaPalette.tertiaryFixed,
    val tertiaryFixedDim: Color = HeyMikaPalette.tertiaryFixedDim,
    val onTertiaryFixed: Color = HeyMikaPalette.onTertiaryFixed,
    val onTertiaryFixedVariant: Color = HeyMikaPalette.onTertiaryFixedVariant,
)

internal val DefaultHeyMikaColors = HeyMikaColors()

val LocalHeyMikaColors = staticCompositionLocalOf { DefaultHeyMikaColors }
