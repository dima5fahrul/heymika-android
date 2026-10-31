package com.ai.heymika.core.ui.component.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.core.utils.time.TimeMath
import com.ai.heymika.core.utils.time.TimeRange
import com.ai.heymika.core.utils.time.TimeRangeError
import java.time.LocalTime

private const val STEP_NONE = 0
private const val STEP_START = 1
private const val STEP_END = 2

/**
 * A read-only "HH:mm – HH:mm" field for a daily window such as quiet hours. Tapping it opens two
 * [ComicTimePicker] steps, "Mulai" then "Selesai"; the range is validated with [TimeMath.validate]
 * before [onValueChange] fires. Ranges across midnight (22:00 – 07:00) are valid unless
 * [allowCrossMidnight] is false; an empty range (same start and end) never is. Without
 * [supportingText] the helper line shows the duration and flags a range that crosses midnight.
 */
@Composable
fun TimeRangeField(
    value: TimeRange,
    onValueChange: (TimeRange) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector = Icons.Outlined.Schedule,
    enabled: Boolean = true,
    allowCrossMidnight: Boolean = true,
    supportingText: String? = null,
) {
    TimeRangeFieldImpl(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        leadingIcon = leadingIcon,
        enabled = enabled,
        allowCrossMidnight = allowCrossMidnight,
        supportingText = supportingText,
    )
}

@Composable
internal fun TimeRangeFieldImpl(
    value: TimeRange,
    onValueChange: (TimeRange) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector = Icons.Outlined.Schedule,
    enabled: Boolean = true,
    allowCrossMidnight: Boolean = true,
    supportingText: String? = null,
    previewFocused: Boolean = false,
) {
    var step by rememberSaveable { mutableIntStateOf(STEP_NONE) }
    var pendingStartMinute by rememberSaveable { mutableIntStateOf(TimeMath.minuteOfDay(value.start)) }
    var rangeError by rememberSaveable { mutableStateOf<TimeRangeError?>(null) }

    val start = TimeMath.formatClock(value.start)
    val end = TimeMath.formatClock(value.end)
    val duration = durationText(value.durationMinutes)
    val crossMidnight = stringResource(R.string.chrome_time_range_cross_midnight)
    val helper =
        when {
            supportingText != null -> supportingText
            value.crossesMidnight -> stringResource(R.string.chrome_time_range_summary, crossMidnight, duration)
            else -> duration
        }
    val helperDescription =
        if (supportingText == null && value.crossesMidnight) {
            stringResource(R.string.chrome_picker_field_description, crossMidnight, duration)
        } else {
            null
        }
    val rangeText = stringResource(R.string.chrome_time_range_value, start, end)

    PickerFieldFrame(
        label = label,
        valueText = rangeText,
        placeholder = rangeText,
        leadingIcon = leadingIcon,
        onClick = {
            pendingStartMinute = TimeMath.minuteOfDay(value.start)
            rangeError = null
            step = STEP_START
        },
        accessibilityValue = stringResource(R.string.chrome_time_range_value_description, start, end),
        onClickLabel = stringResource(R.string.chrome_time_range_edit),
        modifier = modifier,
        enabled = enabled,
        supportingText = helper,
        supportingDescription = helperDescription,
        previewFocused = previewFocused,
    )

    when (step) {
        STEP_START -> {
            ComicTimePicker(
                initialTime = value.start,
                onConfirm = { time ->
                    pendingStartMinute = TimeMath.minuteOfDay(time)
                    step = STEP_END
                },
                onDismissRequest = { step = STEP_NONE },
                title = label,
                stepLabel = stringResource(R.string.chrome_time_range_step_start),
                confirmLabel = stringResource(R.string.chrome_picker_next),
            )
        }

        STEP_END -> {
            ComicTimePicker(
                initialTime = value.end,
                onConfirm = { time ->
                    val range = TimeRange(TimeMath.timeAtMinute(pendingStartMinute), time)
                    val problem = TimeMath.validate(range, allowCrossMidnight)
                    rangeError = problem
                    if (problem == null) {
                        step = STEP_NONE
                        onValueChange(range)
                    }
                },
                onDismissRequest = {
                    rangeError = null
                    step = STEP_NONE
                },
                title = label,
                stepLabel = stringResource(R.string.chrome_time_range_step_end),
                errorText = rangeError?.let { errorText(it) },
            )
        }
    }
}

@Composable
private fun durationText(totalMinutes: Int): String {
    val (hours, minutes) = TimeMath.splitHoursMinutes(totalMinutes)
    return when {
        hours == 0 -> stringResource(R.string.chrome_time_range_duration_minutes, minutes)
        minutes == 0 -> stringResource(R.string.chrome_time_range_duration_hours, hours)
        else -> stringResource(R.string.chrome_time_range_duration_hours_minutes, hours, minutes)
    }
}

@Composable
private fun errorText(error: TimeRangeError): String =
    stringResource(
        when (error) {
            TimeRangeError.SAME_TIME -> R.string.chrome_time_range_error_same_time
            TimeRangeError.CROSSES_MIDNIGHT -> R.string.chrome_time_range_error_cross_midnight
        },
    )

@Preview(name = "Time range states", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun TimeRangeFieldPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            TimeRangeField(
                value = TimeRange(LocalTime.of(22, 0), LocalTime.of(7, 0)),
                onValueChange = {},
                label = "Jam Tenang",
                leadingIcon = Icons.Outlined.Bedtime,
            )
            TimeRangeField(
                value = TimeRange(LocalTime.of(19, 0), LocalTime.of(20, 30)),
                onValueChange = {},
                label = "Jadwal belajar harian",
            )
            TimeRangeFieldImpl(
                value = TimeRange(LocalTime.of(8, 0), LocalTime.of(10, 0)),
                onValueChange = {},
                label = "Fokus (keyboard)",
                previewFocused = true,
            )
            TimeRangeField(
                value = TimeRange(LocalTime.of(22, 0), LocalTime.of(7, 0)),
                onValueChange = {},
                label = "Jam Tenang",
                leadingIcon = Icons.Outlined.Bedtime,
                enabled = false,
                supportingText = "Aktifkan notifikasi untuk mengatur jam tenang.",
            )
        }
    }
}
