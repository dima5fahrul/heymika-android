package com.ai.heymika.core.ui.component.display

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.theme.HeyMikaMotion
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * A number badge: unread counts, chip counters, "99+" past [max]. A pill of at least 20x20dp in
 * labelSm. The default is the ribbonRed urgency tone (filled with its dark red, white text), never an
 * error tone.
 *
 * TalkBack reads the number. Pass [contentDescription] ("3 notifikasi baru") when the number alone is
 * ambiguous; it then replaces the number.
 */
@Composable
fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    tone: DisplayTone = DisplayTone.Red,
    style: PillStyle = PillStyle.Filled,
    max: Int = DEFAULT_MAX_COUNT,
    colors: PillColors = PillDefaults.colors(tone, style),
    contentDescription: String? = null,
) {
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.pill
    val label = if (count > max) stringResource(R.string.display_count_overflow, max) else count.toString()
    val semantics =
        if (contentDescription != null) {
            Modifier.clearAndSetSemantics { this.contentDescription = contentDescription }
        } else {
            Modifier
        }
    val border =
        if (colors.border.alpha > 0f) Modifier.border(dimens.borderSubtle, colors.border, shape) else Modifier
    Box(
        modifier =
            modifier
                .then(semantics)
                .defaultMinSize(minWidth = dimens.iconSize, minHeight = dimens.iconSize)
                .background(colors.container, shape)
                .then(border)
                .padding(horizontal = dimens.spaceXs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = colors.content,
            style = HeyMikaTheme.typography.labelSm,
            maxLines = 1,
        )
    }
}

/**
 * A small status dot: the unread dot on the bell (ribbonRed), the online dot on an avatar (success),
 * the live dot of a status pill. [ringColor] draws a 2dp ring around it (the "ring-white" cut-out on
 * top of an icon); [Color.Transparent] draws none.
 *
 * [pulsing] adds a 1.2s halo for live states. It stops under reduced motion. The dot is decorative
 * unless [contentDescription] is given.
 */
@Composable
fun DotBadge(
    modifier: Modifier = Modifier,
    color: Color = HeyMikaTheme.colors.ribbonRed,
    size: Dp = HeyMikaTheme.dimens.spaceSm,
    ringColor: Color = Color.Transparent,
    pulsing: Boolean = false,
    contentDescription: String? = null,
) {
    val ringWidth = if (ringColor.alpha > 0f) HeyMikaTheme.dimens.borderControl else 0.dp
    val pulse = if (pulsing && !HeyMikaTheme.reducedMotion) rememberDotPulse() else null
    val semantics =
        if (contentDescription != null) {
            Modifier.clearAndSetSemantics { this.contentDescription = contentDescription }
        } else {
            Modifier.clearAndSetSemantics {}
        }
    val dotDiameter = size
    Canvas(
        modifier =
            modifier
                .then(semantics)
                .size(dotDiameter + ringWidth * 2),
    ) {
        val dotRadius = dotDiameter.toPx() / 2f
        val progress = pulse?.value
        if (progress != null) {
            drawCircle(
                color = color.copy(alpha = color.alpha * PULSE_MAX_ALPHA * (1f - progress)),
                radius = dotRadius * (1f + progress),
            )
        }
        if (ringWidth > 0.dp) {
            drawCircle(color = ringColor, radius = dotRadius + ringWidth.toPx())
        }
        drawCircle(color = color, radius = dotRadius)
    }
}

@Composable
private fun rememberDotPulse(): State<Float> {
    val transition = rememberInfiniteTransition(label = "dotPulse")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = HeyMikaMotion.PULSE_MILLIS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "dotPulseProgress",
    )
}

private const val DEFAULT_MAX_COUNT = 99
private const val PULSE_MAX_ALPHA = 0.45f

@Preview(name = "CountBadge", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun CountBadgePreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CountBadge(count = 3)
                CountBadge(count = 12)
                CountBadge(count = 150)
                CountBadge(count = 6, tone = DisplayTone.Primary)
                CountBadge(count = 2, tone = DisplayTone.Danger)
                CountBadge(count = 9, tone = DisplayTone.Neutral, style = PillStyle.Tinted)
                CountBadge(count = 4, tone = DisplayTone.Orange, style = PillStyle.Outline)
            }
            CountBadge(count = 3, contentDescription = "3 notifikasi baru")
        }
    }
}

@Preview(name = "DotBadge", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun DotBadgePreview() {
    HeyMikaTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DotBadge()
            DotBadge(color = HeyMikaTheme.colors.success, size = 12.dp, ringColor = HeyMikaTheme.colors.surface)
            DotBadge(color = HeyMikaTheme.colors.primary, pulsing = true)
            Box {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifikasi",
                    modifier = Modifier.size(24.dp),
                )
                DotBadge(
                    modifier = Modifier.align(Alignment.TopEnd),
                    ringColor = HeyMikaTheme.colors.canvas,
                    contentDescription = "Ada notifikasi baru",
                )
            }
        }
    }
}
