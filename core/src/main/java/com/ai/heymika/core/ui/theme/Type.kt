package com.ai.heymika.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ai.heymika.core.R

/** Nunito Sans, bundled in core/src/main/res/font (OFL), weights 400-900. */
val NunitoSans =
    FontFamily(
        Font(R.font.nunito_sans_regular, FontWeight.Normal),
        Font(R.font.nunito_sans_medium, FontWeight.Medium),
        Font(R.font.nunito_sans_semibold, FontWeight.SemiBold),
        Font(R.font.nunito_sans_bold, FontWeight.Bold),
        Font(R.font.nunito_sans_extrabold, FontWeight.ExtraBold),
        Font(R.font.nunito_sans_black, FontWeight.Black),
    )

/**
 * JetBrains Mono Bold, bundled in core/src/main/res/font (OFL, docs/licenses), for timers, IDs
 * and code chips. Only the 700 weight ships; other weights fall back to it.
 */
val JetBrainsMono =
    FontFamily(
        Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
    )

private val CenteredLineHeight =
    LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    )

private fun nunito(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: TextUnit = 0.em,
): TextStyle =
    TextStyle(
        fontFamily = NunitoSans,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing,
        lineHeightStyle = CenteredLineHeight,
    )

/**
 * The DESIGN.md type scale under its own names (mobile sizes; v1 is phone-only), plus the derived
 * styles the components need. Read through [HeyMikaTheme.typography]. Uppercase is applied to the
 * text itself (`text.uppercase()`), never by a style.
 */
@Immutable
class HeyMikaTypography(
    /** display-hero-mobile 32/40/900/-0.01em. Screen heroes ("Materimu"). */
    val displayHero: TextStyle = nunito(32, 40, FontWeight.Black, (-0.01).em),
    /** headline-lg-mobile 26/34/800/-0.01em. Auth and page titles. */
    val headlineLg: TextStyle = nunito(26, 34, FontWeight.ExtraBold, (-0.01).em),
    /** headline-md 24/32/800. */
    val headlineMd: TextStyle = nunito(24, 32, FontWeight.ExtraBold),
    /** headline-sm 20/28/700. Card and dialog titles. */
    val headlineSm: TextStyle = nunito(20, 28, FontWeight.Bold),
    /** body-lg 18/28/400. Lead paragraphs. */
    val bodyLg: TextStyle = nunito(18, 28, FontWeight.Normal),
    /** body-md 16/24/400. Default body and input text. */
    val bodyMd: TextStyle = nunito(16, 24, FontWeight.Normal),
    /** body-sm 14/20/400. Secondary copy. */
    val bodySm: TextStyle = nunito(14, 20, FontWeight.Normal),
    /** label-lg 16/22/700. Field labels, list titles. */
    val labelLg: TextStyle = nunito(16, 22, FontWeight.Bold),
    /** label-md 14/18/700/+0.02em. Chips, tabs, small buttons. */
    val labelMd: TextStyle = nunito(14, 18, FontWeight.Bold, 0.02.em),
    /** label-sm 12/16/700/+0.05em. Uppercase pills and badges. */
    val labelSm: TextStyle = nunito(12, 16, FontWeight.Bold, 0.05.em),
    /** caption 12/16/500. Helper text, timestamps. 12sp is the floor: never go smaller. */
    val caption: TextStyle = nunito(12, 16, FontWeight.Medium),
    /** Label of Large and Medium ComicButtons: 16/22/700. */
    val button: TextStyle = nunito(16, 22, FontWeight.Bold),
    /** Label of Small ComicButtons: 14/18/700. */
    val buttonSmall: TextStyle = nunito(14, 18, FontWeight.Bold, 0.01.em),
    /** Section headers ("Katalog Kondisi Sistem"): 20/28/800. */
    val sectionTitle: TextStyle = nunito(20, 28, FontWeight.ExtraBold, (-0.005).em),
    /** Uppercase kicker above a title ("REKOMENDASI MIKA HARI INI"): 12/16/800/+0.06em. */
    val overline: TextStyle = nunito(12, 16, FontWeight.ExtraBold, 0.06.em),
    /** Timers, order and job IDs: JetBrains Mono 14/20/700. */
    val mono: TextStyle =
        TextStyle(
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            lineHeightStyle = CenteredLineHeight,
        ),
    /** display-hero 48/56/900/-0.02em. Desktop/tablet only; not used in v1 (phone-only). */
    val displayHeroLarge: TextStyle = nunito(48, 56, FontWeight.Black, (-0.02).em),
    /** headline-lg 36/44/800/-0.015em. Desktop/tablet only; not used in v1 (phone-only). */
    val headlineLgLarge: TextStyle = nunito(36, 44, FontWeight.ExtraBold, (-0.015).em),
)

internal val DefaultHeyMikaTypography = HeyMikaTypography()

/**
 * DESIGN.md mapped onto the Material 3 slots, so stock M3 components (TextField, Button, Snackbar)
 * render in Nunito Sans at the design sizes.
 */
val HeyMikaMaterialTypography: Typography =
    DefaultHeyMikaTypography.let { type ->
        Typography(
            displayLarge = type.displayHeroLarge,
            displayMedium = type.headlineLgLarge,
            displaySmall = type.displayHero,
            headlineLarge = type.headlineLg,
            headlineMedium = type.headlineMd,
            headlineSmall = type.headlineSm,
            titleLarge = type.headlineSm,
            titleMedium = type.labelLg,
            titleSmall = type.labelMd,
            bodyLarge = type.bodyMd,
            bodyMedium = type.bodySm,
            bodySmall = type.caption,
            labelLarge = type.labelLg,
            labelMedium = type.labelMd,
            labelSmall = type.labelSm,
        )
    }

val LocalHeyMikaTypography = staticCompositionLocalOf { DefaultHeyMikaTypography }
