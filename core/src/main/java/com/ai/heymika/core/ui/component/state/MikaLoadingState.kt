package com.ai.heymika.core.ui.component.state

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.ui.component.mascot.MikaAvatar
import com.ai.heymika.core.ui.theme.HeyMikaMotion
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import kotlin.math.PI
import kotlin.math.cos

/**
 * Mika waiting with the user: a gently bobbing mascot, a [message] and three pulsing dots.
 * TalkBack announces it as an indeterminate progress indicator with the message. Under reduced
 * motion the mascot and dots stand still.
 */
@Composable
fun MikaLoadingState(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.state_loading_default),
    pose: MikaPose = MikaPose.THINKING,
    variant: MikaStateVariant = MikaStateVariant.FullScreen,
) {
    val dimens = HeyMikaTheme.dimens
    val phase = rememberLoadingPhase(animate = !HeyMikaTheme.reducedMotion)
    val bobTravel = dimens.spaceXs
    val progressSemantics =
        Modifier.semantics(mergeDescendants = true) {
            progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
            liveRegion = LiveRegionMode.Polite
        }
    when (variant) {
        MikaStateVariant.FullScreen -> {
            Column(
                modifier =
                    modifier
                        .fillMaxSize()
                        .then(progressSemantics)
                        .padding(dimens.screenMargin),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimens.spaceMd, Alignment.CenterVertically),
            ) {
                MikaAvatar(
                    pose = pose,
                    modifier = Modifier.bob(phase, bobTravel),
                    size = dimens.avatarXxl,
                )
                Text(
                    text = message,
                    style = HeyMikaTheme.typography.bodyMd,
                    color = HeyMikaTheme.colors.textMuted,
                    textAlign = TextAlign.Center,
                )
                LoadingDots(phase = phase)
            }
        }

        MikaStateVariant.Inline -> {
            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .then(progressSemantics)
                        .padding(dimens.cardPaddingCompact),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
            ) {
                MikaAvatar(
                    pose = pose,
                    modifier = Modifier.bob(phase, bobTravel),
                    size = dimens.avatarMd,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
                ) {
                    Text(
                        text = message,
                        style = HeyMikaTheme.typography.bodySm,
                        color = HeyMikaTheme.colors.textMuted,
                    )
                    LoadingDots(phase = phase)
                }
            }
        }
    }
}

/** A 0..1 loop over one pulse; stays at 0 when [animate] is false. */
@Composable
private fun rememberLoadingPhase(animate: Boolean): State<Float> {
    if (!animate) return remember { mutableFloatStateOf(0f) }
    val transition = rememberInfiniteTransition(label = "mikaLoading")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(HeyMikaMotion.PULSE_MILLIS, easing = LinearEasing)),
        label = "mikaLoadingPhase",
    )
}

/** Lifts the content by up to [travel] and back once per phase loop, in the layer (no recomposition). */
private fun Modifier.bob(
    phase: State<Float>,
    travel: Dp,
): Modifier =
    graphicsLayer {
        translationY = -travel.toPx() * wave(phase.value)
    }

@Composable
private fun LoadingDots(phase: State<Float>) {
    val dimens = HeyMikaTheme.dimens
    val color = HeyMikaTheme.colors.primary
    Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
        repeat(DOT_COUNT) { index ->
            Box(
                modifier =
                    Modifier
                        .size(dimens.spaceSm)
                        .graphicsLayer {
                            val shifted = (phase.value - index.toFloat() / DOT_COUNT + 1f) % 1f
                            alpha = MIN_DOT_ALPHA + (1f - MIN_DOT_ALPHA) * wave(shifted)
                        }.background(color, HeyMikaTheme.shapes.pill),
            )
        }
    }
}

/** 0 at the ends of the loop, 1 in the middle, eased like a cosine. */
private fun wave(phase: Float): Float = 0.5f - 0.5f * cos(phase * 2f * PI.toFloat())

private const val DOT_COUNT = 3
private const val MIN_DOT_ALPHA = 0.3f

@Preview(name = "Full screen", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360, heightDp = 480)
@Composable
private fun MikaLoadingStateFullScreenPreview() {
    HeyMikaTheme {
        MikaLoadingState()
    }
}

@Preview(name = "Inline, custom message", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun MikaLoadingStateInlinePreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MikaLoadingState(
                message = "Mika sedang merangkum Bab 4…",
                pose = MikaPose.WAVING_PENCIL,
                variant = MikaStateVariant.Inline,
            )
            MikaLoadingState(
                message = "Menyiapkan kuis…",
                variant = MikaStateVariant.Inline,
            )
        }
    }
}
