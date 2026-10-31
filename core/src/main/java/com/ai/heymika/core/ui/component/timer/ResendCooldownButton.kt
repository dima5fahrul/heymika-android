package com.ai.heymika.core.ui.component.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.ComicButtonDefaults
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.core.utils.time.CountdownClock
import com.ai.heymika.core.utils.time.CountdownMath

/**
 * When the resend button may be tapped again. [deadlineMillis] is on [clock]'s time base and is
 * what [rememberResendCooldownState] saves, so the wait survives rotation and process death.
 */
@Stable
class ResendCooldownState(
    val cooldownSeconds: Int,
    internal val clock: CountdownClock,
    initialDeadlineMillis: Long,
) {
    var deadlineMillis: Long by mutableLongStateOf(initialDeadlineMillis)
        private set

    /** Starts a new full cooldown from now. */
    fun restart() {
        deadlineMillis = CountdownMath.deadlineAfter(clock.nowMillis(), cooldownSeconds)
    }

    /** Ends the cooldown now, for example when the resend request failed. */
    fun reset() {
        deadlineMillis = clock.nowMillis()
    }

    fun remainingSecondsNow(): Int = CountdownMath.remainingSeconds(deadlineMillis, clock.nowMillis())

    companion object {
        /** Saves the deadline; a restored deadline is capped at one full cooldown (after a reboot). */
        fun saver(
            cooldownSeconds: Int,
            clock: CountdownClock,
        ): Saver<ResendCooldownState, Long> =
            Saver(
                save = { it.deadlineMillis },
                restore = { saved ->
                    val cap = CountdownMath.deadlineAfter(clock.nowMillis(), cooldownSeconds)
                    ResendCooldownState(cooldownSeconds, clock, saved.coerceAtMost(cap))
                },
            )
    }
}

/**
 * Remembers a [ResendCooldownState] across recomposition, rotation and process death. With
 * [startCoolingDown] the first wait starts right away, as after the first code was sent. [clock]
 * defaults to the monotonic clock; previews and tests pass a fake one.
 */
@Composable
fun rememberResendCooldownState(
    cooldownSeconds: Int = TimerDefaults.RESEND_COOLDOWN_SECONDS,
    startCoolingDown: Boolean = true,
    clock: CountdownClock = TimerDefaults.monotonicClock,
): ResendCooldownState =
    rememberSaveable(cooldownSeconds, saver = ResendCooldownState.saver(cooldownSeconds, clock)) {
        val now = clock.nowMillis()
        ResendCooldownState(
            cooldownSeconds = cooldownSeconds,
            clock = clock,
            initialDeadlineMillis = if (startCoolingDown) CountdownMath.deadlineAfter(now, cooldownSeconds) else now,
        )
    }

/**
 * "Kirim Ulang 00:46": a flat button that stays disabled while the cooldown runs and shows the
 * wait in a mono chip, then "Siap". A tap calls [onResend] and starts a new cooldown; call
 * [ResendCooldownState.reset] if the request fails. TalkBack hears the wait as the state.
 */
@Composable
fun ResendCooldownButton(
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
    cooldownSeconds: Int = TimerDefaults.RESEND_COOLDOWN_SECONDS,
    state: ResendCooldownState = rememberResendCooldownState(cooldownSeconds),
    text: String = stringResource(R.string.chrome_resend_label),
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val remaining by rememberCountdownSeconds(state.deadlineMillis, state.clock)
    ResendCooldownButtonContent(
        text = text,
        remainingSeconds = remaining,
        onClick = {
            onResend()
            state.restart()
        },
        modifier = modifier,
        enabled = enabled,
        loading = loading,
    )
}

@Composable
internal fun ResendCooldownButtonContent(
    text: String,
    remainingSeconds: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val coolingDown = remainingSeconds > 0
    val container = MaterialTheme.colorScheme.surfaceContainer
    val colors =
        ComicButtonDefaults
            .colors(ComicButtonVariant.Ghost)
            .copy(container = container, disabledContainer = container)
    val waitDescription = stringResource(R.string.chrome_resend_wait_description, remainingSeconds)
    val chipText =
        if (coolingDown) CountdownMath.formatClock(remainingSeconds) else stringResource(R.string.chrome_resend_ready)
    val dimens = HeyMikaTheme.dimens
    val caption = HeyMikaTheme.typography.caption
    val chipStyle =
        HeyMikaTheme.typography.mono.copy(
            fontSize = caption.fontSize,
            lineHeight = caption.lineHeight,
            fontWeight = FontWeight.Black,
        )

    ComicButton(
        onClick = onClick,
        modifier =
            modifier.semantics {
                if (coolingDown && !loading) stateDescription = waitDescription
            },
        variant = ComicButtonVariant.Ghost,
        size = ComicButtonSize.Medium,
        enabled = enabled && !coolingDown,
        loading = loading,
        colors = colors,
    ) {
        if (!loading) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Send,
                contentDescription = null,
                modifier = Modifier.size(ComicButtonDefaults.iconSize(ComicButtonSize.Medium)),
            )
        }
        Text(text = text)
        if (!loading) {
            Text(
                text = chipText,
                style = chipStyle,
                color = HeyMikaTheme.colors.textMuted,
                modifier =
                    Modifier
                        .clearAndSetSemantics {}
                        .background(MaterialTheme.colorScheme.surfaceBright, HeyMikaTheme.shapes.tag)
                        .padding(horizontal = dimens.spaceXs + dimens.spaceXxs, vertical = dimens.spaceXxs),
            )
        }
    }
}

@Preview(name = "Resend cooldown", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ResendCooldownButtonPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ResendCooldownButtonContent(text = "Kirim Ulang", remainingSeconds = 46, onClick = {})
            ResendCooldownButtonContent(text = "Kirim Ulang", remainingSeconds = 0, onClick = {})
            ResendCooldownButtonContent(text = "Kirim Ulang", remainingSeconds = 0, onClick = {}, loading = true)
            ResendCooldownButtonContent(text = "Kirim Ulang", remainingSeconds = 0, onClick = {}, enabled = false)
        }
    }
}

private val PreviewClock = CountdownClock { 0L }

@Preview(name = "Resend, fake clock (00:43)", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ResendCooldownButtonClockPreview() {
    HeyMikaTheme {
        ResendCooldownButton(
            onResend = {},
            modifier = Modifier.padding(16.dp),
            state = ResendCooldownState(cooldownSeconds = 60, clock = PreviewClock, initialDeadlineMillis = 43_000L),
        )
    }
}
