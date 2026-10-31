package com.ai.heymika.core.ui.component.display

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.theme.HeyMikaMotion
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import kotlin.math.roundToInt

/**
 * - [Comic]: a 12dp white track with a 1.5dp ink border and the fill inset inside it (hero cards,
 *   the processing timeline, onboarding steps).
 * - [Plain]: an 8dp borderless surfaceContainer track (list cards, mastery rows, mini meters).
 */
enum class LinearProgressStyle { Comic, Plain }

/** One coloured run of a [SegmentedLinearProgress]; [fraction] is its share of the whole bar (0..1). */
@Immutable
data class ProgressSegment(
    val fraction: Float,
    val color: Color,
)

object LinearProgressDefaults {
    /** White behind the ink border for Comic, surfaceContainer for Plain. */
    @Composable
    @ReadOnlyComposable
    fun trackColor(style: LinearProgressStyle): Color =
        if (style == LinearProgressStyle.Comic) {
            HeyMikaTheme.colors.surface
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }

    /** 12dp for Comic, 8dp (progressTrackHeight) for Plain. */
    @Composable
    @ReadOnlyComposable
    fun height(style: LinearProgressStyle): Dp {
        val track = HeyMikaTheme.dimens.progressTrackHeight
        return if (style == LinearProgressStyle.Comic) track + HeyMikaTheme.dimens.spaceXs else track
    }
}

/**
 * A rounded progress bar filling the available width. [progress] is clamped to 0..1 and animates
 * over 200ms; under reduced motion it jumps.
 *
 * The fill colour comes from the caller. For a tone, pass `ToneSpec.accent` or
 * `PillDefaults.toneSpec(tone).accent`; MasteryBand maps onto tones in its own phase. TalkBack reads it
 * as a progress bar ("78 persen"); [contentDescription] names what is measured.
 */
@Composable
fun LinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = HeyMikaTheme.colors.primary,
    style: LinearProgressStyle = LinearProgressStyle.Comic,
    trackColor: Color = LinearProgressDefaults.trackColor(style),
    contentDescription: String? = null,
) {
    val target = progress.toFraction()
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = if (HeyMikaTheme.reducedMotion) snap() else tween(HeyMikaMotion.EXPAND_MILLIS),
        label = "linearProgress",
    )
    ProgressTrack(
        modifier =
            modifier
                .progressSemantics(target)
                .describedBy(contentDescription),
        style = style,
        trackColor = trackColor,
        segmentCount = 1,
        fractionAt = { animated },
        colorAt = { color },
    )
}

/**
 * A bar made of consecutive coloured runs, such as the "Kesiapan Ujian" bar (50% primary + 24%
 * orange). Segments are drawn in order and the total is capped at 1. Joins are flat and the end is
 * rounded. TalkBack reads the total as the progress value.
 */
@Composable
fun SegmentedLinearProgress(
    segments: List<ProgressSegment>,
    modifier: Modifier = Modifier,
    style: LinearProgressStyle = LinearProgressStyle.Plain,
    trackColor: Color = LinearProgressDefaults.trackColor(style),
    contentDescription: String? = null,
) {
    val total = segments.sumOf { it.fraction.toFraction().toDouble() }.toFloat().coerceAtMost(1f)
    ProgressTrack(
        modifier =
            modifier
                .progressSemantics(total)
                .describedBy(contentDescription),
        style = style,
        trackColor = trackColor,
        segmentCount = segments.size,
        fractionAt = { segments[it].fraction },
        colorAt = { segments[it].color },
    )
}

/** The three ring sizes: [Small] 40dp (stat cards), [Medium] 64dp, [Large] 96dp (score hero). */
enum class ProgressRingSize { Small, Medium, Large }

/**
 * A circular progress ring with a centred label: the beranda "Fokus Hari Ini" ring (Small, "78%")
 * and the quiz result donut (Large, "70%" over "Mandiri").
 *
 * - [label] defaults to the rounded percentage.
 * - [sublabel] shows under it on Medium and Large.
 * - The arc starts at 12 o'clock, has a round cap, and its stroke is 10% of the diameter.
 * - The arc animates over 200ms and jumps under reduced motion.
 * - TalkBack reads the value as a progress bar, plus [contentDescription] (or [sublabel]) as its
 *   name.
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: ProgressRingSize = ProgressRingSize.Small,
    color: Color = HeyMikaTheme.colors.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    label: String? = null,
    sublabel: String? = null,
    labelColor: Color = HeyMikaTheme.colors.textPrimary,
    contentDescription: String? = null,
) {
    val dimens = HeyMikaTheme.dimens
    val typography = HeyMikaTheme.typography
    val target = progress.toFraction()
    // Read only in the draw phase, so the sweep animates without recomposing.
    val animated =
        animateFloatAsState(
            targetValue = target,
            animationSpec = if (HeyMikaTheme.reducedMotion) snap() else tween(HeyMikaMotion.EXPAND_MILLIS),
            label = "progressRing",
        )
    val diameter =
        when (size) {
            ProgressRingSize.Small -> dimens.avatarSm
            ProgressRingSize.Medium -> dimens.avatarLg
            ProgressRingSize.Large -> dimens.avatarXxl
        }
    val labelStyle =
        when (size) {
            ProgressRingSize.Small -> typography.labelSm
            ProgressRingSize.Medium -> typography.labelMd
            ProgressRingSize.Large -> typography.headlineSm
        }
    val centerLabel = label ?: stringResource(R.string.display_percent, (target * PERCENT).roundToInt())
    val description = contentDescription ?: sublabel
    Box(
        modifier =
            modifier
                .size(diameter)
                .clearAndSetSemantics {
                    progressBarRangeInfo = ProgressBarRangeInfo(target, 0f..1f)
                    if (description != null) this.contentDescription = description
                }.drawWithCache {
                    val stroke = this.size.minDimension * RING_STROKE_RATIO
                    val topLeft = Offset(stroke / 2f, stroke / 2f)
                    val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
                    val trackStroke = Stroke(width = stroke)
                    val progressStroke = Stroke(width = stroke, cap = StrokeCap.Round)
                    onDrawBehind {
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = FULL_CIRCLE,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = trackStroke,
                        )
                        val sweep = animated.value
                        if (sweep > 0f) {
                            drawArc(
                                color = color,
                                startAngle = RING_START_ANGLE,
                                sweepAngle = FULL_CIRCLE * sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = progressStroke,
                            )
                        }
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = diameter * RING_LABEL_WIDTH_RATIO),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = centerLabel,
                color = labelColor,
                style = labelStyle,
                maxLines = 1,
            )
            if (sublabel != null && size != ProgressRingSize.Small) {
                Text(
                    text = sublabel,
                    color = HeyMikaTheme.colors.textMuted,
                    style = typography.caption,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ProgressTrack(
    modifier: Modifier,
    style: LinearProgressStyle,
    trackColor: Color,
    segmentCount: Int,
    fractionAt: (Int) -> Float,
    colorAt: (Int) -> Color,
) {
    val dimens = HeyMikaTheme.dimens
    val comic = style == LinearProgressStyle.Comic
    val borderWidth = if (comic) dimens.borderSubtle else 0.dp
    val inset = if (comic) dimens.borderSubtle + dimens.spaceXxs / 2 else 0.dp
    val borderColor = HeyMikaTheme.colors.ink
    Spacer(
        modifier =
            modifier
                .fillMaxWidth()
                .height(LinearProgressDefaults.height(style))
                .drawWithCache {
                    val borderPx = borderWidth.toPx()
                    val insetPx = inset.toPx()
                    val inner =
                        RoundRect(
                            left = insetPx,
                            top = insetPx,
                            right = (size.width - insetPx).coerceAtLeast(insetPx),
                            bottom = (size.height - insetPx).coerceAtLeast(insetPx),
                            cornerRadius = CornerRadius((size.height - insetPx * 2f).coerceAtLeast(0f) / 2f),
                        )
                    val innerClip = Path().apply { addRoundRect(inner) }
                    onDrawBehind {
                        drawRoundRect(color = trackColor, cornerRadius = CornerRadius(size.height / 2f))
                        clipPath(innerClip) {
                            drawProgressFill(inner, segmentCount, fractionAt, colorAt)
                        }
                        if (borderPx > 0f) {
                            val half = borderPx / 2f
                            drawRoundRect(
                                color = borderColor,
                                topLeft = Offset(half, half),
                                size = Size(size.width - borderPx, size.height - borderPx),
                                cornerRadius = CornerRadius((size.height - borderPx) / 2f),
                                style = Stroke(width = borderPx),
                            )
                        }
                    }
                },
    )
}

/**
 * Paints the runs inside [inner]. The last non-empty run spans the whole filled length so the bar
 * ends rounded; the earlier runs are painted over it with flat joins.
 */
private fun DrawScope.drawProgressFill(
    inner: RoundRect,
    segmentCount: Int,
    fractionAt: (Int) -> Float,
    colorAt: (Int) -> Color,
) {
    var lastIndex = -1
    var total = 0f
    for (i in 0 until segmentCount) {
        val fraction = fractionAt(i).toFraction()
        if (fraction > 0f) lastIndex = i
        total += fraction
    }
    total = total.coerceAtMost(1f)
    if (lastIndex < 0 || total <= 0f) return
    val innerWidth = inner.width
    val innerHeight = inner.height
    drawRoundRect(
        color = colorAt(lastIndex),
        topLeft = Offset(inner.left, inner.top),
        size = Size(innerWidth * total, innerHeight),
        cornerRadius = CornerRadius(innerHeight / 2f),
    )
    var x = inner.left
    for (i in 0 until lastIndex) {
        val width = (innerWidth * fractionAt(i).toFraction()).coerceAtMost(inner.right - x)
        if (width > 0f) {
            drawRect(
                color = colorAt(i),
                topLeft = Offset(x, inner.top),
                size = Size(width, innerHeight),
            )
        }
        x += width
    }
}

private fun Modifier.describedBy(description: String?): Modifier =
    if (description != null) semantics { contentDescription = description } else this

private fun Float.toFraction(): Float = if (isNaN()) 0f else coerceIn(0f, 1f)

private const val PERCENT = 100
private const val FULL_CIRCLE = 360f
private const val RING_START_ANGLE = -90f
private const val RING_STROKE_RATIO = 0.1f
private const val RING_LABEL_WIDTH_RATIO = 0.7f

@Preview(name = "LinearProgress", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun LinearProgressPreview() {
    HeyMikaTheme {
        val tones = HeyMikaTheme.tones
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LinearProgress(progress = 0f)
            LinearProgress(progress = 0.72f)
            LinearProgress(progress = 1f, color = HeyMikaTheme.colors.success)
            LinearProgress(progress = 0.65f, style = LinearProgressStyle.Plain)
            LinearProgress(progress = 0.4f, style = LinearProgressStyle.Plain, color = HeyMikaTheme.colors.accent)
            LinearProgress(progress = 0.45f, style = LinearProgressStyle.Plain, color = tones.danger.accent)
            LinearProgress(progress = 0.92f, style = LinearProgressStyle.Plain, color = tones.teal.accent)
            LinearProgress(progress = 0.02f, style = LinearProgressStyle.Plain)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Mini meter", modifier = Modifier.weight(1f))
                LinearProgress(
                    progress = 0.67f,
                    modifier = Modifier.width(64.dp),
                    style = LinearProgressStyle.Plain,
                )
            }
        }
    }
}

@Preview(name = "SegmentedLinearProgress", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun SegmentedLinearProgressPreview() {
    HeyMikaTheme {
        val colors = HeyMikaTheme.colors
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SegmentedLinearProgress(
                segments = listOf(ProgressSegment(0.5f, colors.primary), ProgressSegment(0.24f, colors.accent)),
                contentDescription = "Kesiapan ujian",
            )
            SegmentedLinearProgress(
                segments =
                    listOf(
                        ProgressSegment(0.4f, colors.primary),
                        ProgressSegment(0.35f, colors.sky),
                        ProgressSegment(0.25f, colors.accent),
                    ),
                style = LinearProgressStyle.Comic,
            )
        }
    }
}

@Preview(name = "ProgressRing", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ProgressRingPreview() {
    HeyMikaTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProgressRing(progress = 0.78f, labelColor = HeyMikaTheme.colors.primaryText)
            ProgressRing(progress = 0f)
            ProgressRing(progress = 0.4f, size = ProgressRingSize.Medium, color = HeyMikaTheme.colors.accent)
            ProgressRing(progress = 0.7f, size = ProgressRingSize.Large, sublabel = "Mandiri")
        }
    }
}
