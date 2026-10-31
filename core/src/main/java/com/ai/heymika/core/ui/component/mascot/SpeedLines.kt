package com.ai.heymika.core.ui.component.mascot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * - [Radial]: tapered strokes bursting out of the centre, as behind a manga hero panel.
 * - [Diagonal]: two parallel bursts entering from the top-end and bottom-start corners.
 */
enum class SpeedLinesStyle { Radial, Diagonal }

object SpeedLinesDefaults {
    const val LINE_COUNT = 18

    /** Share of each side the lines may cover; the inner area stays clear for content. */
    const val PERIPHERAL_FRACTION = 0.2f

    @Composable
    @ReadOnlyComposable
    fun color(): Color = HeyMikaTheme.colors.sky.copy(alpha = SPEED_LINE_ALPHA)
}

/**
 * Manga speed lines that frame a celebration or hero moment (verify success, quiz score, payment
 * paid). The strokes are tapered wedges drawn only in the outer [peripheralFraction] of each side,
 * so the middle stays clear for text. The path is built once per size with a fixed seed, so
 * nothing is allocated per frame and the layout never shimmers; it is static, so reduced motion
 * needs no special case. Give it a size, for example `matchParentSize()` behind the content. It
 * is decorative and hidden from accessibility services.
 */
@Composable
fun SpeedLinesDecoration(
    modifier: Modifier = Modifier,
    color: Color = SpeedLinesDefaults.color(),
    style: SpeedLinesStyle = SpeedLinesStyle.Radial,
    lineCount: Int = SpeedLinesDefaults.LINE_COUNT,
    peripheralFraction: Float = SpeedLinesDefaults.PERIPHERAL_FRACTION,
) {
    Spacer(
        modifier =
            modifier
                .clearAndSetSemantics {}
                .drawWithCache {
                    val maxHalfWidth = MAX_LINE_WIDTH.toPx() / 2f
                    val band = peripheralFraction.coerceIn(0f, 0.5f)
                    val path =
                        when (style) {
                            SpeedLinesStyle.Radial -> radialSpeedLines(size, lineCount, band, maxHalfWidth)
                            SpeedLinesStyle.Diagonal -> diagonalSpeedLines(size, lineCount, band, maxHalfWidth)
                        }
                    onDrawBehind { clipRect { drawPath(path, color) } }
                },
    )
}

private fun radialSpeedLines(
    size: Size,
    count: Int,
    band: Float,
    maxHalfWidth: Float,
): Path {
    val path = Path()
    if (size.isEmpty() || count <= 0) return path
    val random = Random(SEED)
    val cx = size.width / 2f
    val cy = size.height / 2f
    val clearHalfWidth = cx * (1f - 2f * band)
    val clearHalfHeight = cy * (1f - 2f * band)
    val step = (2.0 * PI / count).toFloat()
    repeat(count) { index ->
        val angle = index * step + (random.nextFloat() - 0.5f) * step * ANGLE_JITTER
        val dx = cos(angle)
        val dy = sin(angle)
        val inner = rayToRect(clearHalfWidth, clearHalfHeight, dx, dy) * (1f + random.nextFloat() * LENGTH_JITTER)
        val outer = rayToRect(cx, cy, dx, dy) * OVERSHOOT
        val half = maxHalfWidth * (MIN_WIDTH_SHARE + random.nextFloat() * (1f - MIN_WIDTH_SHARE))
        if (outer > inner) {
            val baseX = cx + dx * outer
            val baseY = cy + dy * outer
            path.moveTo(cx + dx * inner, cy + dy * inner)
            path.lineTo(baseX - dy * half, baseY + dx * half)
            path.lineTo(baseX + dy * half, baseY - dx * half)
            path.close()
        }
    }
    return path
}

private fun diagonalSpeedLines(
    size: Size,
    count: Int,
    band: Float,
    maxHalfWidth: Float,
): Path {
    val path = Path()
    if (size.isEmpty() || count <= 0) return path
    val random = Random(SEED)
    val shortSide = min(size.width, size.height)
    val reach = shortSide * band * DIAGONAL_REACH
    val spread = shortSide * DIAGONAL_SPREAD
    val perCorner = max(1, count / 2)
    // (corner x, corner y, direction x, direction y): lines run from the corner toward the centre.
    val bursts =
        listOf(
            floatArrayOf(size.width, 0f, -INV_SQRT2, INV_SQRT2),
            floatArrayOf(0f, size.height, INV_SQRT2, -INV_SQRT2),
        )
    bursts.forEach { (cornerX, cornerY, dx, dy) ->
        repeat(perCorner) { index ->
            val offset = ((index + 0.5f) / perCorner - 0.5f) * 2f * spread
            val baseX = cornerX - dy * offset
            val baseY = cornerY + dx * offset
            val length = reach * (MIN_LENGTH_SHARE + random.nextFloat() * (1f - MIN_LENGTH_SHARE)) + abs(offset)
            val half = maxHalfWidth * (MIN_WIDTH_SHARE + random.nextFloat() * (1f - MIN_WIDTH_SHARE))
            path.moveTo(baseX + dx * length, baseY + dy * length)
            path.lineTo(baseX - dy * half, baseY + dx * half)
            path.lineTo(baseX + dy * half, baseY - dx * half)
            path.close()
        }
    }
    return path
}

/** Distance from the centre along (dx, dy) to the edge of a centred rect with the given half sizes. */
private fun rayToRect(
    halfWidth: Float,
    halfHeight: Float,
    dx: Float,
    dy: Float,
): Float {
    val tx = if (abs(dx) < EPSILON) Float.MAX_VALUE else halfWidth / abs(dx)
    val ty = if (abs(dy) < EPSILON) Float.MAX_VALUE else halfHeight / abs(dy)
    return min(tx, ty)
}

private const val SEED = 7
private const val SPEED_LINE_ALPHA = 0.12f
private const val ANGLE_JITTER = 0.6f
private const val LENGTH_JITTER = 0.3f
private const val OVERSHOOT = 1.05f
private const val MIN_WIDTH_SHARE = 0.4f
private const val MIN_LENGTH_SHARE = 0.5f
private const val DIAGONAL_REACH = 1.6f
private const val DIAGONAL_SPREAD = 0.35f
private const val INV_SQRT2 = 0.70710677f
private const val EPSILON = 1e-4f
private val MAX_LINE_WIDTH = 8.dp

@Preview(name = "Radial and diagonal", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun SpeedLinesPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(HeyMikaTheme.colors.surface),
                contentAlignment = Alignment.Center,
            ) {
                SpeedLinesDecoration(modifier = Modifier.matchParentSize())
                MikaAvatar(pose = MikaPose.TROPHY, size = HeyMikaTheme.dimens.avatarXxl)
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(HeyMikaTheme.colors.surface),
            ) {
                SpeedLinesDecoration(
                    modifier = Modifier.matchParentSize(),
                    color = HeyMikaTheme.colors.ink.copy(alpha = 0.08f),
                    style = SpeedLinesStyle.Diagonal,
                )
            }
        }
    }
}
