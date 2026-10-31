package com.ai.heymika.core.ui.component.display

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.modifier.subtleSurface
import com.ai.heymika.core.ui.theme.HeyMikaMotion
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * A grey placeholder block for content that is loading. Size it with [modifier], for example
 * `Modifier.fillMaxWidth(0.6f).height(14.dp)`.
 *
 * With [shimmer] a soft highlight band sweeps across it. The band is read only in the draw phase,
 * so it never recomposes. Under reduced motion (system animator scale 0) the block is static.
 * It is decorative: the enclosing item, such as [ListItemSkeleton], carries the "Memuat konten…"
 * description.
 */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = HeyMikaTheme.shapes.small,
    shimmer: Boolean = true,
) {
    val base = MaterialTheme.colorScheme.surfaceContainer
    val highlight = MaterialTheme.colorScheme.surfaceContainerLowest
    val sweep = if (shimmer && !HeyMikaTheme.reducedMotion) rememberShimmerSweep() else null
    Spacer(
        modifier =
            modifier
                .clearAndSetSemantics {}
                .clip(shape)
                .drawWithCache {
                    // Built once per size; each frame only moves the band.
                    val band = size.width * SHIMMER_BAND_RATIO
                    val bandSize = Size(band, size.height)
                    val brush =
                        Brush.horizontalGradient(
                            colors = listOf(highlight.copy(alpha = 0f), highlight, highlight.copy(alpha = 0f)),
                            startX = 0f,
                            endX = band,
                        )
                    onDrawBehind {
                        drawRect(base)
                        val progress = sweep?.value ?: return@onDrawBehind
                        translate(left = -band + (size.width + band) * progress) {
                            drawRect(brush = brush, size = bandSize)
                        }
                    }
                },
    )
}

/**
 * A loading placeholder for one list row or card: an optional round avatar block and [lines] text
 * bars (the first is a shorter title) on a subtle card. TalkBack reads it once as
 * [contentDescription], "Memuat konten…" by default.
 */
@Composable
fun ListItemSkeleton(
    modifier: Modifier = Modifier,
    showAvatar: Boolean = true,
    lines: Int = 2,
    shimmer: Boolean = true,
    contentDescription: String = stringResource(R.string.display_skeleton_loading),
) {
    val dimens = HeyMikaTheme.dimens
    val lineCount = lines.coerceAtLeast(1)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clearAndSetSemantics { this.contentDescription = contentDescription }
                .subtleSurface(HeyMikaTheme.shapes.card)
                .padding(dimens.cardPaddingCompact),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showAvatar) {
            SkeletonBlock(
                modifier = Modifier.size(dimens.avatarSm),
                shape = HeyMikaTheme.shapes.pill,
                shimmer = shimmer,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceSm),
        ) {
            repeat(lineCount) { index ->
                val fraction =
                    when {
                        index == 0 -> TITLE_LINE_FRACTION
                        index == lineCount - 1 -> LAST_LINE_FRACTION
                        else -> 1f
                    }
                SkeletonBlock(
                    modifier =
                        Modifier
                            .fillMaxWidth(fraction)
                            .height(if (index == 0) dimens.spaceMd else dimens.spaceSmPlus),
                    shimmer = shimmer,
                )
            }
        }
    }
}

@Composable
private fun rememberShimmerSweep(): State<Float> =
    rememberInfiniteTransition(label = "skeletonShimmer").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = HeyMikaMotion.PULSE_MILLIS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "skeletonShimmerProgress",
    )

private const val SHIMMER_BAND_RATIO = 0.6f
private const val TITLE_LINE_FRACTION = 0.6f
private const val LAST_LINE_FRACTION = 0.85f

@Preview(name = "Skeleton", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun SkeletonPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonBlock(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                shape = HeyMikaTheme.shapes.card,
                shimmer = false,
            )
            ListItemSkeleton(shimmer = false)
            ListItemSkeleton(showAvatar = false, lines = 3, shimmer = false)
            ListItemSkeleton(lines = 1)
        }
    }
}
