package com.ai.heymika.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * A colour triple for tinted UI (tags, pills, stat tiles, progress fills).
 *
 * @property container tinted background.
 * @property content text and icons on [container]; every pair meets 4.5:1.
 * @property accent the saturated colour: progress fill, dot, border or hard shadow.
 */
@Immutable
data class ToneSpec(
    val container: Color,
    val content: Color,
    val accent: Color,
)

/**
 * The named-tone palette. Domain enums (WorkspaceCategory, MasteryBand, ...) are mapped onto these
 * names in the phase that introduces the enum; P0 defines only the palette. Read through
 * [HeyMikaTheme.tones].
 */
@Immutable
class HeyMikaTones(
    val blue: ToneSpec = ToneSpec(Color(0xFFEFF6FF), Color(0xFF1D4ED8), HeyMikaPalette.primary),
    val teal: ToneSpec = ToneSpec(Color(0xFFF0FDFA), Color(0xFF0F766E), Color(0xFF14B8A6)),
    val orange: ToneSpec = ToneSpec(Color(0xFFFFF7ED), Color(0xFFC2410C), HeyMikaPalette.shadowOrange),
    val emerald: ToneSpec = ToneSpec(Color(0xFFECFDF5), Color(0xFF047857), Color(0xFF10B981)),
    val rose: ToneSpec = ToneSpec(Color(0xFFFFF1F2), Color(0xFFBE123C), Color(0xFFF43F5E)),
    val purple: ToneSpec = ToneSpec(Color(0xFFFAF5FF), Color(0xFF7E22CE), Color(0xFFA855F7)),
    val slate: ToneSpec = ToneSpec(Color(0xFFF1F5F9), Color(0xFF475569), Color(0xFF94A3B8)),
    val sky: ToneSpec = ToneSpec(HeyMikaPalette.badgeCyan, HeyMikaPalette.ink, HeyMikaPalette.brandSky),
    val yellow: ToneSpec = ToneSpec(Color(0xFFFFF6D6), Color(0xFF6B4E00), HeyMikaPalette.brandYellow),
    val danger: ToneSpec = ToneSpec(HeyMikaPalette.dangerBg, HeyMikaPalette.danger, HeyMikaPalette.danger),
    val success: ToneSpec = ToneSpec(HeyMikaPalette.successBg, HeyMikaPalette.success, HeyMikaPalette.success),
    val warning: ToneSpec =
        ToneSpec(HeyMikaPalette.secondaryFixed, HeyMikaPalette.secondary, HeyMikaPalette.shadowOrange),
    val info: ToneSpec = ToneSpec(HeyMikaPalette.badgeCyan, HeyMikaPalette.ink, HeyMikaPalette.primary),
    val outline: ToneSpec = ToneSpec(HeyMikaPalette.surface, HeyMikaPalette.ink, HeyMikaPalette.borderMuted),
    val primaryContainer: ToneSpec =
        ToneSpec(
            HeyMikaPalette.primaryContainer,
            HeyMikaPalette.onPrimaryContainer,
            HeyMikaPalette.onPrimaryFixedVariant,
        ),
    /** Urgency and the unread dot. Never an error tone: errors use [danger]. */
    val ribbonRed: ToneSpec = ToneSpec(Color(0xFFFDE8E7), Color(0xFFB3261E), HeyMikaPalette.ribbonRed),
    val tertiaryFixedDim: ToneSpec =
        ToneSpec(
            HeyMikaPalette.tertiaryFixedDim,
            HeyMikaPalette.onTertiaryFixed,
            HeyMikaPalette.onTertiaryFixedVariant,
        ),
    val secondaryFixedDim: ToneSpec =
        ToneSpec(
            HeyMikaPalette.secondaryFixedDim,
            HeyMikaPalette.onSecondaryFixed,
            HeyMikaPalette.onSecondaryFixedVariant,
        ),
)

internal val DefaultHeyMikaTones = HeyMikaTones()

val LocalHeyMikaTones = staticCompositionLocalOf { DefaultHeyMikaTones }
