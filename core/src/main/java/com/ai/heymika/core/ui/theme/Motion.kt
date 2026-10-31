package com.ai.heymika.core.ui.theme

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

/** Motion durations from the design plan. */
object HeyMikaMotion {
    /** Button press-translate and card press-scale. */
    const val PRESS_MILLIS = 150

    /** Accordion expand/collapse and chevron rotation. */
    const val EXPAND_MILLIS = 200

    /** One beat of a pulsing live dot. */
    const val PULSE_MILLIS = 1_200

    /** One loop of a striped progress bar. */
    const val STRIPE_LOOP_MILLIS = 1_000
}

/**
 * True when the system animator duration scale is 0 (Developer options or "Remove animations").
 * Provided by [HeyMikaTheme]; read it as [HeyMikaTheme.reducedMotion]. Loops must stop and
 * expand/collapse must snap while it is true.
 */
val LocalReducedMotion = compositionLocalOf { false }

/** Observes the animator duration scale; always false in previews. */
@Composable
fun rememberReducedMotion(): Boolean {
    val inPreview = LocalInspectionMode.current
    val resolver = LocalContext.current.contentResolver
    var reduced by remember(resolver) { mutableStateOf(!inPreview && resolver.animatorScaleIsZero()) }
    DisposableEffect(resolver, inPreview) {
        if (inPreview) return@DisposableEffect onDispose {}
        val observer =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    reduced = resolver.animatorScaleIsZero()
                }
            }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return reduced
}

private fun ContentResolver.animatorScaleIsZero(): Boolean =
    Settings.Global.getFloat(this, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
