package com.ai.heymika.core.ui.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.theme.DefaultHeyMikaColors
import com.ai.heymika.core.ui.theme.DefaultHeyMikaDimens
import com.ai.heymika.core.ui.theme.DefaultHeyMikaShapes

/** Defaults of [comicSurface]: white face, 3dp ink border, 6dp/6dp orange hard shadow. */
object ComicSurfaceDefaults {
    val shape: Shape get() = DefaultHeyMikaShapes.card
    val color: Color get() = DefaultHeyMikaColors.surface
    val borderColor: Color get() = DefaultHeyMikaColors.ink
    val borderWidth: Dp get() = DefaultHeyMikaDimens.borderInk
    val shadowColor: Color get() = DefaultHeyMikaColors.comicShadow
    val shadowOffset: DpOffset
        get() = DpOffset(DefaultHeyMikaDimens.comicShadowOffset, DefaultHeyMikaDimens.comicShadowOffset)
}

/** Defaults of [subtleSurface]: white face, 1.5dp borderMuted border, CSS 0 4px 16px ink@6%. */
object SubtleSurfaceDefaults {
    val shape: Shape get() = DefaultHeyMikaShapes.card
    val color: Color get() = DefaultHeyMikaColors.surface
    val borderColor: Color get() = DefaultHeyMikaColors.borderMuted
    val borderWidth: Dp get() = DefaultHeyMikaDimens.borderSubtle
    val floatColor: Color get() = DefaultHeyMikaColors.subtleShadow
    val floatBlur: Dp get() = DefaultHeyMikaDimens.subtleFloatBlur
    val floatOffsetY: Dp get() = DefaultHeyMikaDimens.subtleFloatOffsetY
}

/**
 * Strict comic surface: a HARD offset shadow (no blur) drawn behind, then a solid [color] face,
 * then the [borderWidth] ink border on top of the content.
 *
 * The shadow is drawn outside the layout bounds. Leave room for it: at least [shadowOffset] of
 * end/bottom space before the next sibling or the edge of a clipping parent (LazyColumn
 * contentPadding, a Column spacing of 8dp or more). It does not clip the content; add
 * `Modifier.clip(shape)` after it for full-bleed images.
 *
 * Pass `borderWidth = 0.dp` for a borderless shell (the login card), or a [Color.Transparent]
 * [shadowColor] / `DpOffset.Zero` to drop the shadow. A hard "ledge" (beranda list cards) is
 * `shadowOffset = DpOffset(0.dp, 4.dp)` with `shadowColor = borderMuted`.
 */
fun Modifier.comicSurface(
    shape: Shape = ComicSurfaceDefaults.shape,
    color: Color = ComicSurfaceDefaults.color,
    borderColor: Color = ComicSurfaceDefaults.borderColor,
    borderWidth: Dp = ComicSurfaceDefaults.borderWidth,
    shadowColor: Color = ComicSurfaceDefaults.shadowColor,
    shadowOffset: DpOffset = ComicSurfaceDefaults.shadowOffset,
): Modifier =
    this
        .drawWithCache {
            val outline = shape.createOutline(size, layoutDirection, this)
            val dx = shadowOffset.x.toPx()
            val dy = shadowOffset.y.toPx()
            val hasShadow = shadowColor.alpha > 0f && (dx != 0f || dy != 0f)
            onDrawBehind {
                if (hasShadow) {
                    translate(left = dx, top = dy) { drawOutline(outline, shadowColor) }
                }
            }
        }.background(color, shape)
        .inkBorder(borderWidth, borderColor, shape)

/**
 * Subtle float surface for list, feed and utility cards: a soft ambient shadow
 * ([floatOffsetY]/[floatBlur] in [floatColor]), a solid [color] face and a thin border.
 */
fun Modifier.subtleSurface(
    shape: Shape = SubtleSurfaceDefaults.shape,
    color: Color = SubtleSurfaceDefaults.color,
    borderColor: Color = SubtleSurfaceDefaults.borderColor,
    borderWidth: Dp = SubtleSurfaceDefaults.borderWidth,
    floatColor: Color = SubtleSurfaceDefaults.floatColor,
    floatBlur: Dp = SubtleSurfaceDefaults.floatBlur,
    floatOffsetY: Dp = SubtleSurfaceDefaults.floatOffsetY,
): Modifier =
    this
        .drawWithCache {
            val outline = shape.createOutline(size, layoutDirection, this)
            val paint = Paint().apply { this.color = color }
            if (floatColor.alpha > 0f && floatBlur > 0.dp) {
                // A shadow layer on the face paint gives a blurred, coloured, offset shadow like the
                // CSS spec (hardware accelerated for shapes from API 28). CSS blur b has sigma b/2,
                // and Skia's sigma is 0.577 * radius, so radius ~= 0.87 * b.
                paint.asFrameworkPaint().setShadowLayer(
                    floatBlur.toPx() * CSS_BLUR_TO_SHADOW_RADIUS,
                    0f,
                    floatOffsetY.toPx(),
                    floatColor.toArgb(),
                )
            }
            onDrawBehind {
                drawIntoCanvas { canvas -> canvas.drawOutline(outline, paint) }
            }
        }.inkBorder(borderWidth, borderColor, shape)

private const val CSS_BLUR_TO_SHADOW_RADIUS = 0.87f

private fun Modifier.inkBorder(
    width: Dp,
    color: Color,
    shape: Shape,
): Modifier =
    // border(0.dp) would draw a hairline, so a zero width must skip the modifier entirely.
    if (width > 0.dp && color.alpha > 0f) border(width, color, shape) else this
