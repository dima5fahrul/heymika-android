package com.ai.heymika.core.ui.theme

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.LocalTonalElevationEnabled
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

// The comic focus ring is the only focus indicator, so ripples drop their focus state layer.
@OptIn(ExperimentalMaterial3Api::class)
private val HeyMikaRippleConfiguration =
    RippleConfiguration(
        rippleAlpha =
            RippleAlpha(
                draggedAlpha = 0.16f,
                focusedAlpha = 0f,
                hoveredAlpha = 0.08f,
                pressedAlpha = 0.10f,
            ),
    )

/**
 * The HeyMika theme: light only, phone only, no dynamic colour.
 *
 * Provides the Material 3 scheme, typography and shapes plus every HeyMika local. Tonal elevation
 * is disabled globally: comic shadows replace elevation, so no M3 surface is ever tinted. Default
 * content colour is onBackground (navy), never black, and ripples have no focus layer because
 * every interactive component draws the comic focus ring instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeyMikaTheme(content: @Composable () -> Unit) {
    val reducedMotion = rememberReducedMotion()
    CompositionLocalProvider(
        LocalHeyMikaColors provides DefaultHeyMikaColors,
        LocalHeyMikaTypography provides DefaultHeyMikaTypography,
        LocalHeyMikaShapes provides DefaultHeyMikaShapes,
        LocalHeyMikaDimens provides DefaultHeyMikaDimens,
        LocalHeyMikaTones provides DefaultHeyMikaTones,
        LocalReducedMotion provides reducedMotion,
        LocalTonalElevationEnabled provides false,
    ) {
        MaterialTheme(
            colorScheme = HeyMikaLightColorScheme,
            typography = HeyMikaMaterialTypography,
            shapes = HeyMikaMaterialShapes,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides HeyMikaLightColorScheme.onBackground,
                LocalRippleConfiguration provides HeyMikaRippleConfiguration,
                content = content,
            )
        }
    }
}

/** Accessors for the HeyMika tokens inside [HeyMikaTheme]. */
object HeyMikaTheme {
    val colors: HeyMikaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalHeyMikaColors.current

    val typography: HeyMikaTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalHeyMikaTypography.current

    val shapes: HeyMikaShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalHeyMikaShapes.current

    val dimens: HeyMikaDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalHeyMikaDimens.current

    val tones: HeyMikaTones
        @Composable
        @ReadOnlyComposable
        get() = LocalHeyMikaTones.current

    val reducedMotion: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalReducedMotion.current
}
