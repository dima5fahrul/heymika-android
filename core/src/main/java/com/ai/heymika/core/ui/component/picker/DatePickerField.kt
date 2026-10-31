package com.ai.heymika.core.ui.component.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Event
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.ai.heymika.core.utils.time.DateMath
import java.time.LocalDate

/**
 * A read-only date field ("14 Juli 2025") that opens [ComicDatePicker]. [minDate] and [maxDate]
 * limit which days and years can be picked, for example an exam date that must be in the future.
 * [supportingText] is shown under the field and turns into an announced error when [isError].
 */
@Composable
fun DatePickerField(
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.chrome_date_field_placeholder),
    leadingIcon: ImageVector = Icons.Outlined.CalendarMonth,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    dialogTitle: String = label,
) {
    DatePickerFieldImpl(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        minDate = minDate,
        maxDate = maxDate,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText,
        dialogTitle = dialogTitle,
    )
}

@Composable
internal fun DatePickerFieldImpl(
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.chrome_date_field_placeholder),
    leadingIcon: ImageVector = Icons.Outlined.CalendarMonth,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    dialogTitle: String = label,
    previewFocused: Boolean = false,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    val valueText = value?.let(DateMath::formatLongDate)

    PickerFieldFrame(
        label = label,
        valueText = valueText,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        onClick = { open = true },
        accessibilityValue = valueText ?: stringResource(R.string.chrome_picker_not_set),
        onClickLabel = stringResource(R.string.chrome_date_field_edit),
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText,
        previewFocused = previewFocused,
    )

    if (open && enabled) {
        ComicDatePicker(
            initialDate = value,
            onConfirm = { date ->
                open = false
                onValueChange(date)
            },
            onDismissRequest = { open = false },
            title = dialogTitle,
            minDate = minDate,
            maxDate = maxDate,
        )
    }
}

@Preview(name = "Date field states", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun DatePickerFieldPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            DatePickerField(
                value = null,
                onValueChange = {},
                label = "Tanggal ujian",
                leadingIcon = Icons.Outlined.Event,
            )
            DatePickerField(
                value = LocalDate.of(2025, 7, 14),
                onValueChange = {},
                label = "Tanggal ujian",
                leadingIcon = Icons.Outlined.Event,
                supportingText = "Mika menyusun jadwal belajar sampai hari ini.",
            )
            DatePickerField(
                value = LocalDate.of(2024, 1, 2),
                onValueChange = {},
                label = "Tanggal ujian",
                isError = true,
                supportingText = "Tanggal ujian harus setelah hari ini.",
            )
            DatePickerField(
                value = LocalDate.of(2008, 3, 17),
                onValueChange = {},
                label = "Tanggal lahir",
                enabled = false,
            )
        }
    }
}

@Preview(name = "Date field focused", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun DatePickerFieldFocusedPreview() {
    HeyMikaTheme {
        DatePickerFieldImpl(
            value = LocalDate.of(2025, 7, 14),
            onValueChange = {},
            label = "Tanggal ujian",
            modifier = Modifier.padding(16.dp),
            previewFocused = true,
        )
    }
}
