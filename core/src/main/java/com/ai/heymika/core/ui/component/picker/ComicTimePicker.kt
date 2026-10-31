package com.ai.heymika.core.ui.component.picker

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.component.overlay.BasicComicDialog
import com.ai.heymika.core.ui.component.overlay.ComicDialogButtons
import com.ai.heymika.core.ui.component.overlay.ComicDialogSurface
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import java.time.LocalTime

/**
 * A 24-hour Material3 TimePicker inside a comic dialog, themed so it never falls back to stock
 * colours: the dial on surfaceContainerLow, a primary selector, badgeCyan hour and minute chips
 * with ink digits, and ComicButtons. [stepLabel] adds a pill above the title for multi-step flows
 * ("Mulai · 1/2"); [errorText] shows a danger callout and keeps the dialog open.
 *
 * Returns the chosen time as a [LocalTime] (seconds are zero).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicTimePicker(
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.chrome_time_picker_title),
    stepLabel: String? = null,
    confirmLabel: String = stringResource(R.string.chrome_picker_confirm),
    dismissLabel: String = stringResource(R.string.chrome_picker_cancel),
    errorText: String? = null,
) {
    val state =
        rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = true,
        )
    BasicComicDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        paneTitle = title,
    ) {
        ComicTimePickerContent(
            state = state,
            title = title,
            stepLabel = stepLabel,
            confirmLabel = confirmLabel,
            dismissLabel = dismissLabel,
            errorText = errorText,
            onConfirm = { onConfirm(LocalTime.of(state.hour, state.minute)) },
            onDismiss = onDismissRequest,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ColumnScope.ComicTimePickerContent(
    state: TimePickerState,
    title: String,
    stepLabel: String?,
    confirmLabel: String,
    dismissLabel: String,
    errorText: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    PickerDialogHeader(title = title, stepLabel = stepLabel)
    TimePicker(state = state, colors = comicTimePickerColors())
    if (errorText != null) PickerErrorText(text = errorText)
    ComicDialogButtons(
        confirmLabel = confirmLabel,
        onConfirm = onConfirm,
        dismissLabel = dismissLabel,
        onDismiss = onDismiss,
        stacked = false,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun comicTimePickerColors(): TimePickerColors {
    val colors = HeyMikaTheme.colors
    val scheme = MaterialTheme.colorScheme
    return TimePickerDefaults.colors(
        clockDialColor = scheme.surfaceContainerLow,
        clockDialSelectedContentColor = colors.onPrimary,
        clockDialUnselectedContentColor = colors.textPrimary,
        selectorColor = colors.primary,
        containerColor = colors.surface,
        periodSelectorBorderColor = colors.ink,
        periodSelectorSelectedContainerColor = colors.badgeCyan,
        periodSelectorUnselectedContainerColor = colors.surface,
        periodSelectorSelectedContentColor = colors.ink,
        periodSelectorUnselectedContentColor = colors.textMuted,
        timeSelectorSelectedContainerColor = colors.badgeCyan,
        timeSelectorUnselectedContainerColor = scheme.surfaceContainerLow,
        timeSelectorSelectedContentColor = colors.ink,
        timeSelectorUnselectedContentColor = colors.textPrimary,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Time picker", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicTimePickerPreview() {
    HeyMikaTheme {
        val state = rememberTimePickerState(initialHour = 22, initialMinute = 0, is24Hour = true)
        ComicDialogSurface(paneTitle = "Pilih jam") {
            ComicTimePickerContent(
                state = state,
                title = "Pilih jam",
                stepLabel = "Mulai · 1/2",
                confirmLabel = "Lanjut",
                dismissLabel = "Batal",
                errorText = null,
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Time picker with error", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicTimePickerErrorPreview() {
    HeyMikaTheme {
        val state = rememberTimePickerState(initialHour = 22, initialMinute = 0, is24Hour = true)
        ComicDialogSurface(paneTitle = "Pilih jam") {
            ComicTimePickerContent(
                state = state,
                title = "Pilih jam",
                stepLabel = "Selesai · 2/2",
                confirmLabel = "Simpan",
                dismissLabel = "Batal",
                errorText = "Jam mulai dan jam selesai tidak boleh sama.",
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
