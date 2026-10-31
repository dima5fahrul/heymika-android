package com.ai.heymika.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Size, border, shadow and spacing tokens. Read through [HeyMikaTheme.dimens].
 *
 * Phone-only v1: the comic shadow is the DESIGN.md mobile 6dp offset.
 */
@Immutable
class HeyMikaDimens(
    /** Comic ink border: shells, strict cards, speech bubbles, accent button, ComicDivider. */
    val borderInk: Dp = 3.dp,
    /** Control border: inputs (borderMuted), outline button (ink), checkbox, radio. */
    val borderControl: Dp = 2.dp,
    /** Subtle border: subtle cards, list rows, SubtleDivider. */
    val borderSubtle: Dp = 1.5.dp,
    /** Hard orange offset of strict surfaces, on both x and y. */
    val comicShadowOffset: Dp = 6.dp,
    /** Hard y-only base under raised buttons at rest. */
    val buttonShadow: Dp = 4.dp,
    /** Base left visible while a raised button is pressed (= buttonShadow - pressTranslate). */
    val buttonShadowPressed: Dp = 2.dp,
    /** How far a raised face sinks onto its base when pressed. */
    val pressTranslate: Dp = 2.dp,
    /** Blur radius of the subtle float shadow (CSS 0 4px 16px). */
    val subtleFloatBlur: Dp = 16.dp,
    /** Y offset of the subtle float shadow. */
    val subtleFloatOffsetY: Dp = 4.dp,
    val buttonHeightLarge: Dp = 56.dp,
    val buttonHeightMedium: Dp = 48.dp,
    val buttonHeightSmall: Dp = 40.dp,
    val inputHeight: Dp = 56.dp,
    /** Minimum touch target for anything interactive. */
    val minTouchTarget: Dp = 48.dp,
    val iconSizeSmall: Dp = 16.dp,
    val iconSize: Dp = 20.dp,
    val iconSizeLarge: Dp = 24.dp,
    val checkboxSize: Dp = 20.dp,
    val progressTrackHeight: Dp = 8.dp,
    val topBarHeight: Dp = 64.dp,
    val bottomNavHeight: Dp = 64.dp,
    val bottomNavCenterTab: Dp = 56.dp,
    val avatarXs: Dp = 32.dp,
    val avatarSm: Dp = 40.dp,
    val avatarMd: Dp = 56.dp,
    val avatarLg: Dp = 64.dp,
    val avatarXl: Dp = 80.dp,
    val avatarXxl: Dp = 96.dp,
    val avatarHero: Dp = 144.dp,
    val radiusTag: Dp = 6.dp,
    val radiusSmall: Dp = 8.dp,
    val radiusControl: Dp = 14.dp,
    val radiusCard: Dp = 16.dp,
    val radiusCardLarge: Dp = 24.dp,
    val radiusShell: Dp = 28.dp,
    val spaceXxs: Dp = 2.dp,
    val spaceXs: Dp = 4.dp,
    val spaceSm: Dp = 8.dp,
    val spaceSmPlus: Dp = 12.dp,
    val spaceMd: Dp = 16.dp,
    val spaceMdPlus: Dp = 20.dp,
    val spaceLg: Dp = 24.dp,
    val spaceXl: Dp = 32.dp,
    val spaceXxl: Dp = 48.dp,
    /** Left/right margin of every screen. */
    val screenMargin: Dp = 16.dp,
    /** Vertical gap between screen sections. */
    val sectionGap: Dp = 24.dp,
    val cardPadding: Dp = 16.dp,
    val cardPaddingCompact: Dp = 12.dp,
    /** Internal padding of shells (auth card, hero). */
    val shellPadding: Dp = 20.dp,
    /** Gap between the component edge and the focus ring. */
    val focusRingGap: Dp = 2.dp,
    /** Stroke width of the focus ring. */
    val focusRingWidth: Dp = 3.dp,
)

internal val DefaultHeyMikaDimens = HeyMikaDimens()

val LocalHeyMikaDimens = staticCompositionLocalOf { DefaultHeyMikaDimens }
