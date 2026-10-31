package com.ai.heymika.core.ui.component.timer

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import com.ai.heymika.core.utils.time.CountdownClock
import com.ai.heymika.core.utils.time.CountdownMath
import kotlinx.coroutines.delay

object TimerDefaults {
    /** Time since boot. It keeps counting through process death, so saved cooldown deadlines use it. */
    val monotonicClock: CountdownClock = CountdownClock { SystemClock.elapsedRealtime() }

    /** Epoch milliseconds, for deadlines that come from the server (quiz end, reset-link expiry). */
    val wallClock: CountdownClock = CountdownClock { System.currentTimeMillis() }

    const val RESEND_COOLDOWN_SECONDS = 60
    const val WARNING_THRESHOLD_SECONDS = 60
}

/**
 * Whole seconds left until [deadlineMillis] on [clock], updated exactly when the displayed second
 * changes and stopping at 0. Pass a stable [clock] (a top-level value or a remembered one); a new
 * clock instance restarts the countdown.
 */
@Composable
fun rememberCountdownSeconds(
    deadlineMillis: Long,
    clock: CountdownClock = TimerDefaults.wallClock,
): State<Int> {
    val seconds =
        remember(deadlineMillis, clock) {
            mutableIntStateOf(CountdownMath.remainingSeconds(deadlineMillis, clock.nowMillis()))
        }
    LaunchedEffect(seconds, deadlineMillis, clock) {
        while (true) {
            val now = clock.nowMillis()
            val left = CountdownMath.remainingSeconds(deadlineMillis, now)
            seconds.intValue = left
            if (left == 0) break
            delay(CountdownMath.millisUntilNextTick(deadlineMillis, now))
        }
    }
    return seconds
}
