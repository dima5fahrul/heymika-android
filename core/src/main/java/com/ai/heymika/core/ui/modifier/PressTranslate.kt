package com.ai.heymika.core.ui.modifier

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.ai.heymika.core.ui.theme.DefaultHeyMikaDimens
import com.ai.heymika.core.ui.theme.DefaultHeyMikaShapes
import com.ai.heymika.core.ui.theme.HeyMikaMotion
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/** Defaults of the press helpers: a 4dp base and a 2dp sink, as on every raised button. */
object PressSurfaceDefaults {
    val shape: Shape get() = DefaultHeyMikaShapes.button
    val baseHeight: Dp get() = DefaultHeyMikaDimens.buttonShadow
    val travel: Dp get() = DefaultHeyMikaDimens.pressTranslate
}

/**
 * How far a raised face has sunk: animates to [travel] while [interactionSource] is pressed and
 * back to 0 on release, over 150ms (instant under reduced motion). Read the returned state only in
 * layout or draw lambdas ([comicPressSurface], [pressTranslate]) so a press never recomposes.
 */
@Composable
fun animatePressTravel(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    travel: Dp = HeyMikaTheme.dimens.pressTranslate,
): State<Dp> {
    val pressed by interactionSource.collectIsPressedAsState()
    return animateDpAsState(
        targetValue = if (pressed && enabled) travel else 0.dp,
        animationSpec = if (HeyMikaTheme.reducedMotion) snap() else tween(HeyMikaMotion.PRESS_MILLIS),
        label = "pressTravel",
    )
}

/** Moves the content down by [travel] in the placement phase (no recomposition per frame). */
fun Modifier.pressTranslate(travel: () -> Dp): Modifier =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.place(IntOffset(0, travel().roundToPx()))
        }
    }

/**
 * A face resting on a hard base, the raised-button look.
 *
 * The modifier's bounds are the face plus [baseHeight] below it, so the base is inside the layout
 * (nothing to reserve). The content and face are measured `baseHeight` shorter than the incoming
 * constraints and sink by [travel] (clamped to `0..baseHeight`); the base never moves, so the
 * visible ledge shrinks from 4dp to 2dp when pressed. Pass `baseColor = Color.Transparent` for a
 * flat face. Apply clickable, focus ring and semantics BEFORE this modifier, so the touch target
 * and the ring cover the base too.
 */
fun Modifier.comicPressSurface(
    travel: () -> Dp,
    color: Color,
    baseColor: Color,
    shape: Shape = PressSurfaceDefaults.shape,
    baseHeight: Dp = PressSurfaceDefaults.baseHeight,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
): Modifier {
    val faceBorder =
        if (borderWidth > 0.dp && borderColor.alpha > 0f) {
            Modifier.border(borderWidth, borderColor, shape)
        } else {
            Modifier
        }
    return this
        .layout { measurable, constraints ->
            val basePx = baseHeight.roundToPx()
            val placeable = measurable.measure(constraints.offset(vertical = -basePx))
            layout(placeable.width, placeable.height + basePx) {
                placeable.place(IntOffset(0, travel().roundToPx().coerceIn(0, basePx)))
            }
        }.drawWithCache {
            // Drawn in the face's (already sunk) coordinates: the base sits basePx below the
            // resting face, i.e. (basePx - travel) below the current one.
            val outline = shape.createOutline(size, layoutDirection, this)
            val basePx = baseHeight.toPx()
            onDrawBehind {
                if (baseColor.alpha > 0f && basePx > 0f) {
                    val travelPx = travel().toPx().coerceIn(0f, basePx)
                    translate(top = basePx - travelPx) { drawOutline(outline, baseColor) }
                }
            }
        }.background(color, shape)
        .then(faceBorder)
}
