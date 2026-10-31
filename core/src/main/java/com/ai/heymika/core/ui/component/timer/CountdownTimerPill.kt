package com.ai.heymika.core.ui.component.timer

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TimerOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.modifier.subtleSurface
import com.ai.heymika.core.ui.theme.HeyMikaMotion
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.core.ui.theme.ToneSpec
import com.ai.heymika.core.utils.time.CountdownClock
import com.ai.heymika.core.utils.time.CountdownMath

private const val PULSE_MIN_ALPHA = 0.35f

private enum class TimerPhase { Normal, Warning, Expired }

/**
 * A "28:40" timer pill. Normal: a white float pill (or [tone]'s container) with a pulsing green live
 * dot. Under [warningThresholdSeconds] it turns to the warning tone with a red dot; at 0 it shows
 * the danger tone and a static dot. [label] follows the time, as in "14:28 tersisa". The pulse stops
 * under reduced motion. TalkBack hears "Sisa waktu 14 menit 28 detik".
 */
@Composable
fun CountdownTimerPill(
    remainingSeconds: Int,
    modifier: Modifier = Modifier,
    warningThresholdSeconds: Int = TimerDefaults.WARNING_THRESHOLD_SECONDS,
    label: String? = null,
    showLiveDot: Boolean = true,
    tone: ToneSpec? = null,
) {
    val colors = HeyMikaTheme.colors
    val tones = HeyMikaTheme.tones
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.pill
    val phase =
        when {
            remainingSeconds <= 0 -> TimerPhase.Expired
            remainingSeconds < warningThresholdSeconds -> TimerPhase.Warning
            else -> TimerPhase.Normal
        }
    val phaseTone =
        when (phase) {
            TimerPhase.Normal -> tone
            TimerPhase.Warning -> tones.warning
            TimerPhase.Expired -> tones.danger
        }
    val textColor = phaseTone?.content ?: colors.textPrimary
    val iconColor = phaseTone?.content ?: colors.textMuted
    val dotColor =
        when (phase) {
            TimerPhase.Normal -> colors.success
            TimerPhase.Warning -> colors.ribbonRed
            TimerPhase.Expired -> colors.danger
        }
    val (minutes, seconds) = CountdownMath.splitMinutesSeconds(remainingSeconds)
    val description =
        if (phase == TimerPhase.Expired) {
            stringResource(R.string.chrome_timer_expired)
        } else {
            stringResource(R.string.chrome_timer_description, minutes, seconds)
        }
    val almostUp = stringResource(R.string.chrome_timer_almost_up)

    Row(
        modifier =
            modifier
                .clearAndSetSemantics {
                    contentDescription = description
                    if (phase == TimerPhase.Warning) stateDescription = almostUp
                }.then(
                    if (phaseTone == null) {
                        Modifier.subtleSurface(shape = shape)
                    } else {
                        Modifier.background(phaseTone.container, shape)
                    },
                ).padding(horizontal = dimens.spaceSmPlus, vertical = dimens.spaceXs),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showLiveDot) {
            LiveDot(color = dotColor, pulsing = phase != TimerPhase.Expired)
        }
        Icon(
            imageVector = if (phase == TimerPhase.Expired) Icons.Outlined.TimerOff else Icons.Outlined.Timer,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(dimens.iconSizeSmall),
        )
        Text(
            text = CountdownMath.formatClock(remainingSeconds),
            style = HeyMikaTheme.typography.mono,
            color = textColor,
        )
        if (label != null) {
            Text(
                text = label,
                style = HeyMikaTheme.typography.labelSm,
                color = textColor,
            )
        }
    }
}

/**
 * The same pill driven by a deadline on [clock] (server deadlines use the default wall clock).
 * [onExpired] runs once each time the countdown reaches 0.
 */
@Composable
fun CountdownTimerPill(
    deadlineMillis: Long,
    onExpired: () -> Unit,
    modifier: Modifier = Modifier,
    clock: CountdownClock = TimerDefaults.wallClock,
    warningThresholdSeconds: Int = TimerDefaults.WARNING_THRESHOLD_SECONDS,
    label: String? = null,
    showLiveDot: Boolean = true,
    tone: ToneSpec? = null,
) {
    val remaining by rememberCountdownSeconds(deadlineMillis, clock)
    val expired = remaining <= 0
    val currentOnExpired by rememberUpdatedState(onExpired)
    LaunchedEffect(expired) {
        if (expired) currentOnExpired()
    }
    CountdownTimerPill(
        remainingSeconds = remaining,
        modifier = modifier,
        warningThresholdSeconds = warningThresholdSeconds,
        label = label,
        showLiveDot = showLiveDot,
        tone = tone,
    )
}

@Composable
private fun LiveDot(
    color: Color,
    pulsing: Boolean,
) {
    val pulse: State<Float>? =
        if (pulsing && !HeyMikaTheme.reducedMotion) {
            rememberInfiniteTransition(label = "liveDot").animateFloat(
                initialValue = 1f,
                targetValue = PULSE_MIN_ALPHA,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = HeyMikaMotion.PULSE_MILLIS / 2),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "liveDotAlpha",
            )
        } else {
            null
        }
    Box(
        modifier =
            Modifier
                .size(HeyMikaTheme.dimens.spaceSm)
                .graphicsLayer { alpha = pulse?.value ?: 1f }
                .background(color, CircleShape),
    )
}

@Preview(name = "Timer phases", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun CountdownTimerPillPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CountdownTimerPill(remainingSeconds = 28 * 60 + 40)
            CountdownTimerPill(remainingSeconds = 42)
            CountdownTimerPill(remainingSeconds = 0)
        }
    }
}

@Preview(name = "Quiz timer (tersisa)", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun CountdownTimerPillQuizPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CountdownTimerPill(
                remainingSeconds = 14 * 60 + 28,
                label = "tersisa",
                showLiveDot = false,
                tone = HeyMikaTheme.tones.orange,
            )
            CountdownTimerPill(remainingSeconds = 35, label = "tersisa", showLiveDot = false)
        }
    }
}

private val PreviewDeadlineClock = CountdownClock { 0L }

@Preview(name = "Reset link, fake clock", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun CountdownTimerPillDeadlinePreview() {
    HeyMikaTheme {
        CountdownTimerPill(
            deadlineMillis = (28 * 60 + 40) * 1_000L,
            onExpired = {},
            modifier = Modifier.padding(16.dp),
            clock = PreviewDeadlineClock,
        )
    }
}
