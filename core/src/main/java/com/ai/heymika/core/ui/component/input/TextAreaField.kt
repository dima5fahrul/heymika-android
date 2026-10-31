package com.ai.heymika.core.ui.component.input

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * A multi-line [ComicTextField] that grows from [minLines] to [maxLines] and then scrolls.
 *
 * With [maxLength] it shows an "n/max" counter under the box (TalkBack: "n dari max karakter") and
 * refuses typing or pasting past the limit; deleting always works. The Enter key inserts a line
 * break, so put the submit action on a button.
 */
@Composable
fun TextAreaField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    labelTrailingText: String? = null,
    required: Boolean = false,
    helperText: String? = null,
    errorText: String? = null,
    isError: Boolean = errorText != null,
    enabled: Boolean = true,
    minLines: Int = DEFAULT_MIN_LINES,
    maxLines: Int = DEFAULT_MAX_LINES,
    maxLength: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    interactionSource: MutableInteractionSource? = null,
) {
    TextAreaFieldImpl(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        labelTrailingText = labelTrailingText,
        required = required,
        helperText = helperText,
        errorText = errorText,
        isError = isError,
        enabled = enabled,
        minLines = minLines,
        maxLines = maxLines,
        maxLength = maxLength,
        keyboardOptions = keyboardOptions,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun TextAreaFieldImpl(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    labelTrailingText: String? = null,
    required: Boolean = false,
    helperText: String? = null,
    errorText: String? = null,
    isError: Boolean = errorText != null,
    enabled: Boolean = true,
    minLines: Int = DEFAULT_MIN_LINES,
    maxLines: Int = DEFAULT_MAX_LINES,
    maxLength: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    interactionSource: MutableInteractionSource? = null,
    previewFocused: Boolean = false,
) {
    ComicTextFieldImpl(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        labelTrailingText = labelTrailingText,
        required = required,
        helperText = helperText,
        errorText = errorText,
        isError = isError,
        enabled = enabled,
        singleLine = false,
        minLines = minLines,
        maxLines = maxLines.coerceAtLeast(minLines),
        keyboardOptions = keyboardOptions,
        interactionSource = interactionSource,
        maxLength = maxLength,
        previewFocused = previewFocused,
    )
}

private const val DEFAULT_MIN_LINES = 3
private const val DEFAULT_MAX_LINES = 6

@Preview(name = "States", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun TextAreaFieldStatesPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextAreaField(
                value = "",
                onValueChange = {},
                label = "Biodata & Catatan Target Belajar",
                placeholder = "Ceritakan target belajarmu ke Mika...",
                maxLength = 160,
            )
            TextAreaFieldImpl(
                value = "Mahasiswa Informatika yang sedang berjuang menaklukkan Struktur Data & Algoritma!",
                onValueChange = {},
                label = "Biodata & Catatan Target Belajar",
                maxLength = 160,
                previewFocused = true,
            )
            TextAreaField(
                value = "Catatan",
                onValueChange = {},
                label = "Deskripsi Ruang Kerja",
                required = true,
                errorText = "Deskripsi minimal 20 karakter supaya Mika paham konteksnya.",
                maxLength = 280,
            )
            TextAreaField(
                value = "Ringkasan otomatis dari Mika tidak bisa diubah di v1.",
                onValueChange = {},
                label = "Ringkasan",
                helperText = "Mode sunting hadir di versi berikutnya.",
                minLines = 2,
                enabled = false,
            )
        }
    }
}
