package com.ai.heymika.core.ui.component.mascot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.component.foundation.ComicCard
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import kotlin.math.hypot

/** Defaults of the halftone decorations: the mockups' 20dp sky dot grid at about 14% ink. */
object HalftoneDefaults {
    /** Distance between dot centres of [HalftoneBackground]. */
    val spacing: Dp = 20.dp

    /** Radius of a [HalftoneBackground] dot. */
    val dotRadius: Dp = 1.5.dp

    /** Distance between dot centres of [HalftoneDecoration]. */
    val decorationSpacing: Dp = 10.dp

    /** Largest dot of [HalftoneDecoration], at its corner. */
    val decorationMaxRadius: Dp = 3.dp

    @Composable
    @ReadOnlyComposable
    fun dotColor(): Color = HeyMikaTheme.colors.sky.copy(alpha = BACKGROUND_DOT_ALPHA)

    @Composable
    @ReadOnlyComposable
    fun decorationColor(): Color = HeyMikaTheme.colors.sky.copy(alpha = DECORATION_DOT_ALPHA)
}

/** Which corner a [HalftoneDecoration] grows from. Start and end follow the layout direction. */
enum class HalftoneCorner { TopStart, TopEnd, BottomStart, BottomEnd }

/**
 * The paper ground of a screen: [color] with a manga screentone dot grid and, optionally, a few
 * sparkles at the edges. The decoration is drawn behind [content], computed once per size, and
 * hidden from accessibility services.
 */
@Composable
fun HalftoneBackground(
    modifier: Modifier = Modifier,
    color: Color = HeyMikaTheme.colors.canvas,
    dotColor: Color = HalftoneDefaults.dotColor(),
    sparkles: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val c = HeyMikaTheme.colors
    val sparkleColors =
        listOf(
            c.yellow.copy(alpha = SPARKLE_ALPHA),
            c.primary.copy(alpha = SPARKLE_ALPHA_SOFT),
            c.sky.copy(alpha = SPARKLE_ALPHA),
        )
    Box(modifier = modifier.background(color)) {
        Spacer(
            modifier =
                Modifier
                    .matchParentSize()
                    .clearAndSetSemantics {}
                    .halftoneGrid(dotColor, HalftoneDefaults.spacing, HalftoneDefaults.dotRadius)
                    .then(if (sparkles) Modifier.edgeSparkles(sparkleColors) else Modifier),
        )
        content()
    }
}

/**
 * A corner patch of halftone dots that shrink away from [corner], for cards and hero panels.
 * Give it a size (or `matchParentSize()` inside a Box) and put it behind the content; it is
 * decorative and never announced.
 */
@Composable
fun HalftoneDecoration(
    modifier: Modifier = Modifier,
    corner: HalftoneCorner = HalftoneCorner.TopEnd,
    color: Color = HalftoneDefaults.decorationColor(),
) {
    Spacer(
        modifier =
            modifier
                .clearAndSetSemantics {}
                .drawWithCache {
                    val step = HalftoneDefaults.decorationSpacing.toPx()
                    val maxRadius = HalftoneDefaults.decorationMaxRadius.toPx()
                    val rtl = layoutDirection == LayoutDirection.Rtl
                    val atStart = corner == HalftoneCorner.TopStart || corner == HalftoneCorner.BottomStart
                    val atTop = corner == HalftoneCorner.TopStart || corner == HalftoneCorner.TopEnd
                    val originX = if (atStart != rtl) 0f else size.width
                    val originY = if (atTop) 0f else size.height
                    val reach = hypot(size.width, size.height)
                    val path = Path()
                    if (step > 0f && reach > 0f) {
                        var y = step / 2f
                        while (y < size.height) {
                            var x = step / 2f
                            while (x < size.width) {
                                val fade = 1f - hypot(x - originX, y - originY) / reach
                                val radius = maxRadius * fade * fade
                                if (radius >= MIN_DOT_RADIUS_PX) path.addOval(Rect(Offset(x, y), radius))
                                x += step
                            }
                            y += step
                        }
                    }
                    onDrawBehind { drawPath(path, color) }
                },
    )
}

private fun Modifier.halftoneGrid(
    color: Color,
    spacing: Dp,
    radius: Dp,
): Modifier =
    drawWithCache {
        val step = spacing.toPx()
        val r = radius.toPx()
        val points = ArrayList<Offset>()
        if (step > 0f) {
            var y = r
            while (y < size.height) {
                var x = r
                while (x < size.width) {
                    points.add(Offset(x, y))
                    x += step
                }
                y += step
            }
        }
        onDrawBehind {
            drawPoints(points, PointMode.Points, color, strokeWidth = r * 2f, cap = StrokeCap.Round)
        }
    }

/** Three comic sparkles near the edges, away from where body text usually sits. */
private fun Modifier.edgeSparkles(colors: List<Color>): Modifier =
    drawWithCache {
        val anchors = listOf(Offset(0.9f, 0.06f), Offset(0.06f, 0.3f), Offset(0.92f, 0.84f))
        val sizes = listOf(SPARKLE_LARGE.toPx(), SPARKLE_SMALL.toPx(), SPARKLE_SMALL.toPx())
        val paths =
            anchors.mapIndexed { index, anchor ->
                sparklePath(Offset(size.width * anchor.x, size.height * anchor.y), sizes[index])
            }
        onDrawBehind {
            paths.forEachIndexed { index, path -> drawPath(path, colors[index % colors.size]) }
        }
    }

private fun sparklePath(
    center: Offset,
    arm: Float,
): Path {
    val pinch = arm * SPARKLE_PINCH
    return Path().apply {
        moveTo(center.x, center.y - arm)
        quadraticTo(center.x + pinch, center.y - pinch, center.x + arm, center.y)
        quadraticTo(center.x + pinch, center.y + pinch, center.x, center.y + arm)
        quadraticTo(center.x - pinch, center.y + pinch, center.x - arm, center.y)
        quadraticTo(center.x - pinch, center.y - pinch, center.x, center.y - arm)
        close()
    }
}

private const val BACKGROUND_DOT_ALPHA = 0.14f
private const val DECORATION_DOT_ALPHA = 0.35f
private const val SPARKLE_ALPHA = 0.6f
private const val SPARKLE_ALPHA_SOFT = 0.4f
private const val SPARKLE_PINCH = 0.2f
private const val MIN_DOT_RADIUS_PX = 0.5f
private val SPARKLE_LARGE = 10.dp
private val SPARKLE_SMALL = 7.dp

@Preview(name = "Halftone background", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun HalftoneBackgroundPreview() {
    HeyMikaTheme {
        HalftoneBackground(modifier = Modifier.fillMaxWidth().height(320.dp)) {
            ComicCard(
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
            ) {
                Text(text = "Periksa Emailmu", style = HeyMikaTheme.typography.headlineSm)
                Text(text = "Titik halftone tidak pernah di belakang teks isi.", style = HeyMikaTheme.typography.bodySm)
            }
        }
    }
}

@Preview(name = "Halftone corner decoration", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun HalftoneDecorationPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HalftoneCorner.entries.forEach { corner ->
                    Box(modifier = Modifier.size(64.dp).background(HeyMikaTheme.colors.surface)) {
                        HalftoneDecoration(modifier = Modifier.matchParentSize(), corner = corner)
                    }
                }
            }
            ComicCard(modifier = Modifier.fillMaxWidth()) {
                Box {
                    HalftoneDecoration(modifier = Modifier.matchParentSize())
                    Text(text = "Kartu dengan sudut halftone", style = HeyMikaTheme.typography.labelLg)
                }
            }
        }
    }
}
