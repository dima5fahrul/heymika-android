package com.ai.heymika.core.ui.component.picker

import android.content.res.Configuration
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.component.overlay.BasicComicDialog
import com.ai.heymika.core.ui.component.overlay.ComicDialogButtons
import com.ai.heymika.core.ui.component.overlay.ComicDialogSurface
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.core.utils.time.DateMath
import java.time.LocalDate

/**
 * A Material3 DatePicker (calendar mode only) inside a comic dialog, with token colours: primary
 * selected day, primaryText today ring, ink navigation, white container. Days outside
 * [minDate]..[maxDate] cannot be picked (pass `LocalDate.now()` as [minDate] for exam targets).
 * The confirm button stays disabled until a day is selected; the result is a [LocalDate].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicDatePicker(
    initialDate: LocalDate?,
    onConfirm: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.chrome_date_picker_title),
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    confirmLabel: String = stringResource(R.string.chrome_picker_confirm),
    dismissLabel: String = stringResource(R.string.chrome_picker_cancel),
) {
    val selectableDates =
        remember(minDate, maxDate) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    DateMath.isWithin(DateMath.fromUtcMillis(utcTimeMillis), minDate, maxDate)

                override fun isSelectableYear(year: Int): Boolean = DateMath.isYearWithin(year, minDate, maxDate)
            }
        }
    val state =
        remember(selectableDates) {
            DatePickerState(
                locale = DateMath.locale,
                initialSelectedDateMillis = initialDate?.let(DateMath::toUtcMillis),
                selectableDates = selectableDates,
            )
        }
    BasicComicDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        paneTitle = title,
        contentPadding = PaddingValues(vertical = HeyMikaTheme.dimens.shellPadding),
    ) {
        ComicDatePickerContent(
            state = state,
            title = title,
            confirmLabel = confirmLabel,
            dismissLabel = dismissLabel,
            onConfirm = { state.selectedDateMillis?.let { onConfirm(DateMath.fromUtcMillis(it)) } },
            onDismiss = onDismissRequest,
        )
    }
}

/** The picker body. It is laid out edge to edge so the 7-day grid fits phone widths. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ColumnScope.ComicDatePickerContent(
    state: DatePickerState,
    title: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sidePadding = HeyMikaTheme.dimens.shellPadding
    PickerDialogHeader(title = title, modifier = Modifier.padding(horizontal = sidePadding))
    val configuration = LocalConfiguration.current
    val indonesianConfiguration =
        remember(configuration) { Configuration(configuration).apply { setLocale(DateMath.locale) } }
    // The headline and month label read the configuration locale; keep them Indonesian like the rest of the app.
    CompositionLocalProvider(LocalConfiguration provides indonesianConfiguration) {
        DatePicker(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            title = null,
            showModeToggle = false,
            colors = comicDatePickerColors(),
        )
    }
    ComicDialogButtons(
        confirmLabel = confirmLabel,
        onConfirm = onConfirm,
        modifier = Modifier.padding(horizontal = sidePadding),
        dismissLabel = dismissLabel,
        onDismiss = onDismiss,
        confirmEnabled = state.selectedDateMillis != null,
        stacked = false,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun comicDatePickerColors(): DatePickerColors {
    val colors = HeyMikaTheme.colors
    return DatePickerDefaults.colors(
        containerColor = colors.surface,
        titleContentColor = colors.textMuted,
        headlineContentColor = colors.textPrimary,
        weekdayContentColor = colors.textMuted,
        subheadContentColor = colors.textMuted,
        navigationContentColor = colors.ink,
        yearContentColor = colors.textPrimary,
        currentYearContentColor = colors.primaryText,
        selectedYearContentColor = colors.onPrimary,
        selectedYearContainerColor = colors.primary,
        dayContentColor = colors.textPrimary,
        selectedDayContentColor = colors.onPrimary,
        selectedDayContainerColor = colors.primary,
        todayContentColor = colors.primaryText,
        todayDateBorderColor = colors.primary,
        dividerColor = colors.borderMuted,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Date picker", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360, heightDp = 760)
@Composable
private fun ComicDatePickerPreview() {
    HeyMikaTheme {
        val state =
            remember {
                DatePickerState(
                    locale = DateMath.locale,
                    initialSelectedDateMillis = DateMath.toUtcMillis(LocalDate.of(2025, 7, 14)),
                )
            }
        ComicDialogSurface(
            paneTitle = "Tanggal ujian",
            contentPadding = PaddingValues(vertical = HeyMikaTheme.dimens.shellPadding),
        ) {
            ComicDatePickerContent(
                state = state,
                title = "Tanggal ujian",
                confirmLabel = "Simpan",
                dismissLabel = "Batal",
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Date, empty", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360, heightDp = 760)
@Composable
private fun ComicDatePickerEmptyPreview() {
    HeyMikaTheme {
        val state = remember { DatePickerState(locale = DateMath.locale) }
        ComicDialogSurface(
            paneTitle = "Pilih tanggal",
            contentPadding = PaddingValues(vertical = HeyMikaTheme.dimens.shellPadding),
        ) {
            ComicDatePickerContent(
                state = state,
                title = "Pilih tanggal",
                confirmLabel = "Simpan",
                dismissLabel = "Batal",
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
