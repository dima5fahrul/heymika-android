package com.ai.heymika.core.ui.component.mascot

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.ui.theme.HeyMikaColors

/**
 * Placeholder Mika: a chibi orange cat with a red bow, built from simple vector shapes on a 48x48
 * viewport, with one face and prop set per [MikaPose]. The real illustrations replace this object
 * wholesale; only [rememberMikaPainter] references it.
 */
internal object MikaPlaceholderArt {
    fun build(
        pose: MikaPose,
        colors: HeyMikaColors,
    ): ImageVector {
        val spec = poseSpec(pose)
        val palette = ArtPalette(colors)
        return ImageVector
            .Builder(
                name = "Mika.${pose.name}",
                defaultWidth = 48.dp,
                defaultHeight = 48.dp,
                viewportWidth = VIEWPORT,
                viewportHeight = VIEWPORT,
            ).apply {
                drawBody(palette)
                drawHead(palette, stripes = Prop.HARDHAT !in spec.props)
                drawEyes(spec.eyes, palette)
                drawBrows(spec.brows, palette)
                drawMouth(spec.mouth, palette)
                drawBow(palette)
                spec.props.forEach { drawProp(it, palette) }
            }.build()
    }
}

private const val VIEWPORT = 48f
private const val OUTLINE = 1.6f
private const val DETAIL = 1.1f
private const val FINE = 0.8f
private const val PAW = 1.3f
private const val EYE_LINE = 1.5f
private const val BROW = 1.2f
private const val ARM = 4f
private const val ARM_OUTLINE = 6.2f
private const val BLUSH_ALPHA = 0.35f
private const val SPARKLE_PINCH = 0.2f

private enum class Eyes { OPEN, HAPPY, CLOSED, WINK, LOOK_UP, CONFUSED }

private enum class Brows { NONE, WORRIED, DETERMINED }

private enum class Mouth { SMILE, OPEN_SMILE, WOBBLY, YAWN, SMALL_O }

private enum class Prop {
    WAVE_PAW,
    PENCIL,
    ENVELOPE,
    SPARKLES,
    SWEAT,
    THOUGHT,
    CHIN_PAW,
    QUESTION,
    ZZZ,
    MAP,
    PADLOCK,
    HOURGLASS,
    PLUG,
    HARDHAT,
    WRENCH,
    TROPHY,
}

private class PoseSpec(
    val eyes: Eyes,
    val brows: Brows,
    val mouth: Mouth,
    val props: List<Prop>,
)

private class ArtPalette(
    colors: HeyMikaColors,
) {
    val ink: Brush = SolidColor(colors.ink)
    val fur: Brush = SolidColor(colors.accent)
    val furDark: Brush = SolidColor(colors.accentPressShadow)
    val white: Brush = SolidColor(colors.surface)
    val red: Brush = SolidColor(colors.ribbonRed)
    val yellow: Brush = SolidColor(colors.yellow)
    val sky: Brush = SolidColor(colors.sky)
    val cyan: Brush = SolidColor(colors.badgeCyan)
}

private fun poseSpec(pose: MikaPose): PoseSpec =
    when (pose) {
        MikaPose.WAVING -> {
            PoseSpec(Eyes.WINK, Brows.NONE, Mouth.OPEN_SMILE, listOf(Prop.WAVE_PAW))
        }

        MikaPose.WAVING_PENCIL -> {
            PoseSpec(Eyes.WINK, Brows.NONE, Mouth.OPEN_SMILE, listOf(Prop.WAVE_PAW, Prop.PENCIL))
        }

        MikaPose.ENVELOPE -> {
            PoseSpec(Eyes.OPEN, Brows.NONE, Mouth.SMILE, listOf(Prop.ENVELOPE))
        }

        MikaPose.HAPPY -> {
            PoseSpec(Eyes.HAPPY, Brows.NONE, Mouth.OPEN_SMILE, listOf(Prop.SPARKLES))
        }

        MikaPose.WORRIED -> {
            PoseSpec(Eyes.OPEN, Brows.WORRIED, Mouth.WOBBLY, listOf(Prop.SWEAT))
        }

        MikaPose.THINKING -> {
            PoseSpec(Eyes.LOOK_UP, Brows.NONE, Mouth.SMILE, listOf(Prop.THOUGHT, Prop.CHIN_PAW))
        }

        MikaPose.CONFUSED -> {
            PoseSpec(Eyes.CONFUSED, Brows.NONE, Mouth.SMALL_O, listOf(Prop.QUESTION))
        }

        MikaPose.SLEEPING -> {
            PoseSpec(Eyes.CLOSED, Brows.NONE, Mouth.SMILE, listOf(Prop.ZZZ))
        }

        MikaPose.CONFUSED_MAP -> {
            PoseSpec(Eyes.CONFUSED, Brows.NONE, Mouth.SMALL_O, listOf(Prop.QUESTION, Prop.MAP))
        }

        MikaPose.PADLOCK -> {
            PoseSpec(Eyes.OPEN, Brows.DETERMINED, Mouth.SMILE, listOf(Prop.PADLOCK))
        }

        MikaPose.YAWN_HOURGLASS -> {
            PoseSpec(Eyes.CLOSED, Brows.NONE, Mouth.YAWN, listOf(Prop.HOURGLASS))
        }

        MikaPose.UNPLUGGED -> {
            PoseSpec(Eyes.OPEN, Brows.WORRIED, Mouth.WOBBLY, listOf(Prop.SWEAT, Prop.PLUG))
        }

        MikaPose.HARDHAT -> {
            PoseSpec(Eyes.OPEN, Brows.NONE, Mouth.OPEN_SMILE, listOf(Prop.HARDHAT, Prop.WRENCH))
        }

        MikaPose.TROPHY -> {
            PoseSpec(Eyes.HAPPY, Brows.NONE, Mouth.OPEN_SMILE, listOf(Prop.SPARKLES, Prop.TROPHY))
        }
    }

private fun ImageVector.Builder.drawBody(p: ArtPalette) {
    shape("M14 48V42.5C14 37.5 18.2 34 24 34C29.8 34 34 37.5 34 42.5V48Z", fill = p.fur, stroke = p.ink)
    shape("M19.2 48C19.2 42 21.2 38.4 24 38.4C26.8 38.4 28.8 42 28.8 48Z", fill = p.white)
}

private fun ImageVector.Builder.drawHead(
    p: ArtPalette,
    stripes: Boolean,
) {
    shape("M11 19L12.5 6.5L21.5 12Z", fill = p.fur, stroke = p.ink)
    shape("M37 19L35.5 6.5L26.5 12Z", fill = p.fur, stroke = p.ink)
    shape("M13.4 15L14.1 9.6L18.4 12.3ZM34.6 15L33.9 9.6L29.6 12.3Z", fill = p.red, fillAlpha = BLUSH_ALPHA)
    oval(24f, 22f, 14f, 12.5f, fill = p.fur, stroke = p.ink)
    if (stripes) line("M24 10V13.4M20.2 10.5L20.9 13.2M27.8 10.5L27.1 13.2", p.furDark, BROW)
    oval(24f, 27f, 6.5f, 4.5f, fill = p.white)
    oval(15.6f, 26.2f, 2.2f, 1.3f, fill = p.red, fillAlpha = BLUSH_ALPHA)
    oval(32.4f, 26.2f, 2.2f, 1.3f, fill = p.red, fillAlpha = BLUSH_ALPHA)
    shape("M22.7 24.5H25.3L24 26Z", fill = p.ink, stroke = p.ink, strokeWidth = FINE)
}

private fun ImageVector.Builder.drawEyes(
    eyes: Eyes,
    p: ArtPalette,
) {
    when (eyes) {
        Eyes.OPEN -> {
            openEye(18.8f, p)
            openEye(29.2f, p)
        }

        Eyes.HAPPY -> {
            line("M16.4 22.6Q18.8 19.2 21.2 22.6M26.8 22.6Q29.2 19.2 31.6 22.6", p.ink, EYE_LINE)
        }

        Eyes.CLOSED -> {
            line("M16.4 21.2Q18.8 24 21.2 21.2M26.8 21.2Q29.2 24 31.6 21.2", p.ink, EYE_LINE)
        }

        Eyes.WINK -> {
            openEye(18.8f, p)
            line("M26.8 22.6Q29.2 19.2 31.6 22.6", p.ink, EYE_LINE)
        }

        Eyes.LOOK_UP -> {
            oval(18.8f, 21f, 2.3f, fill = p.ink)
            oval(29.2f, 21f, 2.3f, fill = p.ink)
            oval(19.2f, 19.6f, 1f, fill = p.white)
            oval(29.6f, 19.6f, 1f, fill = p.white)
        }

        Eyes.CONFUSED -> {
            oval(18.8f, 21.3f, 2.7f, fill = p.ink)
            oval(19.8f, 20.3f, 0.9f, fill = p.white)
            oval(29.2f, 21.8f, 1.3f, fill = p.ink)
        }
    }
}

private fun ImageVector.Builder.openEye(
    cx: Float,
    p: ArtPalette,
) {
    oval(cx, 21.5f, 2.4f, fill = p.ink)
    oval(cx + 0.9f, 20.6f, 0.85f, fill = p.white)
}

private fun ImageVector.Builder.drawBrows(
    brows: Brows,
    p: ArtPalette,
) {
    when (brows) {
        Brows.NONE -> Unit
        Brows.WORRIED -> line("M16.2 17.4L20.8 16M31.8 17.4L27.2 16", p.ink, BROW)
        Brows.DETERMINED -> line("M16.2 15.9L20.8 17.3M31.8 15.9L27.2 17.3", p.ink, BROW)
    }
}

private fun ImageVector.Builder.drawMouth(
    mouth: Mouth,
    p: ArtPalette,
) {
    when (mouth) {
        Mouth.SMILE -> {
            line("M24 26V27.2M21.4 27.2Q22.7 28.9 24 27.2Q25.3 28.9 26.6 27.2", p.ink, DETAIL)
        }

        Mouth.OPEN_SMILE -> {
            shape(
                "M21.2 27.2Q24 27.9 26.8 27.2Q26.4 31 24 31Q21.6 31 21.2 27.2Z",
                fill = p.red,
                stroke = p.ink,
                strokeWidth = DETAIL,
            )
        }

        Mouth.WOBBLY -> {
            line("M21.6 28.6Q22.8 27.4 24 28.4Q25.2 29.4 26.4 28.2", p.ink, DETAIL)
        }

        Mouth.YAWN -> {
            oval(24f, 29f, 2f, 2.4f, fill = p.red, stroke = p.ink, strokeWidth = DETAIL)
        }

        Mouth.SMALL_O -> {
            oval(24f, 28.4f, 1.1f, 1f, fill = p.white, stroke = p.ink, strokeWidth = DETAIL)
        }
    }
}

private fun ImageVector.Builder.drawBow(p: ArtPalette) {
    shape("M24 34.6L19.6 32.4V36.8ZM24 34.6L28.4 32.4V36.8Z", fill = p.red, stroke = p.ink, strokeWidth = DETAIL)
    oval(24f, 34.6f, 1.4f, fill = p.red, stroke = p.ink, strokeWidth = DETAIL)
}

private fun ImageVector.Builder.drawProp(
    prop: Prop,
    p: ArtPalette,
) {
    when (prop) {
        Prop.WAVE_PAW -> {
            line("M13.8 38L8.4 28.8", p.ink, ARM_OUTLINE)
            line("M13.8 38L8.4 28.8", p.fur, ARM)
            oval(7.6f, 26.4f, 3.4f, fill = p.fur, stroke = p.ink, strokeWidth = PAW)
            line("M2.2 22Q1.4 24.6 2.2 27.2M4.4 18.6Q3.2 20.4 3.8 22.4", p.sky, DETAIL)
        }

        Prop.PENCIL -> {
            group(rotate = 25f, pivotX = 38f, pivotY = 32f) {
                roundRect(36.6f, 22.5f, 39.4f, 36f, 0.6f, fill = p.yellow, stroke = p.ink)
                roundRect(36.6f, 20.5f, 39.4f, 22.5f, 0.6f, fill = p.red, stroke = p.ink)
                shape("M36.6 36H39.4L38 39.6Z", fill = p.white, stroke = p.ink, strokeWidth = DETAIL)
                shape("M37.6 38.6H38.4L38 39.6Z", fill = p.ink)
            }
            oval(37.4f, 33.4f, 2.8f, fill = p.fur, stroke = p.ink, strokeWidth = PAW)
        }

        Prop.ENVELOPE -> {
            roundRect(15f, 37f, 33f, 47f, 1f, fill = p.white, stroke = p.ink)
            line("M15.6 37.6L24 43L32.4 37.6", p.ink, DETAIL)
            oval(24f, 42.6f, 1.4f, fill = p.red)
            oval(15.4f, 42f, 2.6f, fill = p.fur, stroke = p.ink, strokeWidth = PAW)
            oval(32.6f, 42f, 2.6f, fill = p.fur, stroke = p.ink, strokeWidth = PAW)
        }

        Prop.SPARKLES -> {
            sparkle(41.5f, 8.5f, 4.2f, fill = p.yellow, stroke = p.ink)
            sparkle(6.5f, 11f, 2.6f, fill = p.sky, stroke = p.ink)
        }

        Prop.SWEAT -> {
            shape(
                "M40 9.8C40 9.8 42.8 13 42.8 14.4C42.8 16 41.6 17 40 17C38.4 17 37.2 16 37.2 14.4" +
                    "C37.2 13 40 9.8 40 9.8Z",
                fill = p.sky,
                stroke = p.ink,
                strokeWidth = FINE,
            )
        }

        Prop.THOUGHT -> {
            oval(37.5f, 12.5f, 1.1f, fill = p.white, stroke = p.ink, strokeWidth = FINE)
            oval(40.5f, 8.8f, 1.6f, fill = p.white, stroke = p.ink, strokeWidth = FINE)
            oval(44.2f, 4.6f, 2.3f, fill = p.white, stroke = p.ink, strokeWidth = FINE)
        }

        Prop.CHIN_PAW -> {
            oval(31.5f, 30.5f, 2.8f, fill = p.fur, stroke = p.ink, strokeWidth = PAW)
        }

        Prop.QUESTION -> {
            line("M38.2 7.4Q38.2 4.4 41.2 4.4Q44.2 4.4 44.2 7.2Q44.2 9.2 41.4 10.4V12", p.ink, OUTLINE)
            oval(41.4f, 14.6f, 0.95f, fill = p.ink)
        }

        Prop.ZZZ -> {
            line("M33.5 5H38.5L33.5 10.5H38.5", p.ink, BROW)
            line("M40.5 1.5H43.5L40.5 4.8H43.5", p.ink, FINE)
        }

        Prop.MAP -> {
            shape(
                "M31 36L35.5 34.5L40 36L44.5 34.5V45.5L40 47L35.5 45.5L31 47Z",
                fill = p.cyan,
                stroke = p.ink,
                strokeWidth = DETAIL,
            )
            line("M35.5 34.5V45.5M40 36V47", p.ink, FINE)
            line("M41.2 39.5L43.2 41.5M43.2 39.5L41.2 41.5", p.red, DETAIL)
            oval(31.4f, 41.5f, 2.6f, fill = p.fur, stroke = p.ink, strokeWidth = PAW)
        }

        Prop.PADLOCK -> {
            line("M36.8 35V31.6Q36.8 28 40.5 28Q44.2 28 44.2 31.6V35", p.ink, OUTLINE)
            roundRect(34.5f, 34.5f, 46.5f, 45.5f, 1.8f, fill = p.yellow, stroke = p.ink, strokeWidth = OUTLINE)
            oval(40.5f, 39f, 1.3f, fill = p.ink)
            line("M40.5 39.6V42.2", p.ink, BROW)
        }

        Prop.HOURGLASS -> {
            shape(
                "M36.5 28.5H44.5L41.4 36.8L44.5 45H36.5L39.6 36.8Z",
                fill = p.white,
                stroke = p.ink,
                strokeWidth = DETAIL,
            )
            shape("M38 30.2H43L40.5 34.6Z", fill = p.yellow)
            shape("M37.8 44.4H43.2L40.5 40.6Z", fill = p.yellow)
            line("M35 28H46M35 45.5H46", p.ink, OUTLINE)
        }

        Prop.PLUG -> {
            line("M30 46Q31 42 33.5 41.5", p.ink, DETAIL)
            roundRect(33f, 38.5f, 37.8f, 44.5f, 1.2f, fill = p.sky, stroke = p.ink)
            line("M37.8 40.2H40M37.8 42.8H40", p.ink, DETAIL)
            roundRect(42.5f, 37.5f, 46.8f, 45.5f, 1.2f, fill = p.white, stroke = p.ink)
            shape(
                "M42.2 30.5L40.2 34.2H42L40.8 37.2L43.8 33.2H42L43.4 30.5Z",
                fill = p.yellow,
                stroke = p.ink,
                strokeWidth = FINE,
            )
        }

        Prop.HARDHAT -> {
            shape("M11 15.5Q11.5 4.5 24 4Q36.5 4.5 37 15.5Z", fill = p.yellow, stroke = p.ink)
            line("M24 4.2V14.5", p.ink, DETAIL)
            roundRect(8f, 14.5f, 40f, 18f, 1.6f, fill = p.yellow, stroke = p.ink, strokeWidth = OUTLINE)
        }

        Prop.WRENCH -> {
            line("M38 44L43.4 37.6", p.ink, 2.4f)
            oval(44.6f, 36.2f, 2.3f, fill = null, stroke = p.ink, strokeWidth = 1.4f)
            oval(37.6f, 44.2f, 2.6f, fill = p.fur, stroke = p.ink, strokeWidth = PAW)
        }

        Prop.TROPHY -> {
            line(
                "M34.7 32Q31.8 32.2 32.4 34.6Q33 36.2 35.4 36M45.3 32Q48.2 32.2 47.6 34.6Q47 36.2 44.6 36",
                p.ink,
                DETAIL,
            )
            shape(
                "M34.5 30.5H45.5Q45.5 38.5 40 39Q34.5 38.5 34.5 30.5Z",
                fill = p.yellow,
                stroke = p.ink,
                strokeWidth = DETAIL,
            )
            sparkle(40f, 34.2f, 1.8f, fill = p.white)
            line("M40 39V41.6", p.ink, OUTLINE)
            roundRect(36.5f, 41.5f, 43.5f, 44.8f, 1f, fill = p.yellow, stroke = p.ink)
        }
    }
}

private fun ImageVector.Builder.addShape(
    nodes: List<PathNode>,
    fill: Brush?,
    stroke: Brush?,
    strokeWidth: Float,
    fillAlpha: Float,
) {
    addPath(
        pathData = nodes,
        fill = fill,
        fillAlpha = fillAlpha,
        stroke = stroke,
        strokeLineWidth = if (stroke == null) 0f else strokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    )
}

/** A filled (and optionally outlined) shape from SVG path data in viewport units. */
private fun ImageVector.Builder.shape(
    pathData: String,
    fill: Brush?,
    stroke: Brush? = null,
    strokeWidth: Float = OUTLINE,
    fillAlpha: Float = 1f,
) {
    addShape(addPathNodes(pathData), fill, stroke, strokeWidth, fillAlpha)
}

/** An open stroke with round caps. */
private fun ImageVector.Builder.line(
    pathData: String,
    color: Brush,
    width: Float,
) {
    addShape(addPathNodes(pathData), fill = null, stroke = color, strokeWidth = width, fillAlpha = 1f)
}

private fun ImageVector.Builder.oval(
    cx: Float,
    cy: Float,
    rx: Float,
    ry: Float = rx,
    fill: Brush?,
    stroke: Brush? = null,
    strokeWidth: Float = OUTLINE,
    fillAlpha: Float = 1f,
) {
    val nodes =
        PathData {
            moveTo(cx - rx, cy)
            arcToRelative(rx, ry, 0f, true, true, rx * 2f, 0f)
            arcToRelative(rx, ry, 0f, true, true, -rx * 2f, 0f)
            close()
        }
    addShape(nodes, fill, stroke, strokeWidth, fillAlpha)
}

private fun ImageVector.Builder.roundRect(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radius: Float,
    fill: Brush?,
    stroke: Brush? = null,
    strokeWidth: Float = DETAIL,
) {
    val nodes =
        PathData {
            moveTo(left + radius, top)
            lineTo(right - radius, top)
            arcTo(radius, radius, 0f, false, true, right, top + radius)
            lineTo(right, bottom - radius)
            arcTo(radius, radius, 0f, false, true, right - radius, bottom)
            lineTo(left + radius, bottom)
            arcTo(radius, radius, 0f, false, true, left, bottom - radius)
            lineTo(left, top + radius)
            arcTo(radius, radius, 0f, false, true, left + radius, top)
            close()
        }
    addShape(nodes, fill, stroke, strokeWidth, fillAlpha = 1f)
}

/** A four-point comic sparkle centred on ([cx], [cy]) with arm length [size]. */
private fun ImageVector.Builder.sparkle(
    cx: Float,
    cy: Float,
    size: Float,
    fill: Brush,
    stroke: Brush? = null,
) {
    val pinch = size * SPARKLE_PINCH
    val nodes =
        PathData {
            moveTo(cx, cy - size)
            quadTo(cx + pinch, cy - pinch, cx + size, cy)
            quadTo(cx + pinch, cy + pinch, cx, cy + size)
            quadTo(cx - pinch, cy + pinch, cx - size, cy)
            quadTo(cx - pinch, cy - pinch, cx, cy - size)
            close()
        }
    addShape(nodes, fill, stroke, strokeWidth = FINE, fillAlpha = 1f)
}
