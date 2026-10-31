package com.ai.heymika.core.ui.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.staticFocusRing
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.core.utils.text.clampEditToMaxLength

/**
 * The HeyMika text field: a white box at least 56dp tall with a 14dp radius and a 2dp borderMuted
 * border, a 20dp textMuted [leadingIcon] and an optional [trailing] slot for icon buttons
 * ([ComicFieldIconButton]).
 *
 * The label row ([label], a danger asterisk when [required], a right-aligned [labelTrailingText])
 * sits above the box. Below it sits [helperText] or, while [isError], [errorText] in a dangerBg
 * callout with an alert icon. Label, placeholder, helper and counter belong to the field's single
 * TalkBack node, and [errorText] is exposed as the field's error.
 *
 * - Focused: the border turns primary and a 3dp primary-25% halo appears outside it.
 * - Error: danger border (the halo still shows while editing).
 * - Disabled: #E7EEFF face, borderMuted border, textMuted text.
 *
 * The field fills the width it is given; constrain it with `Modifier.width` if needed. Pass a
 * [MutableInteractionSource] to observe focus from outside.
 */
@Composable
fun ComicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    labelTrailingText: String? = null,
    required: Boolean = false,
    helperText: String? = null,
    errorText: String? = null,
    isError: Boolean = errorText != null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource? = null,
) {
    ComicTextFieldImpl(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailing = trailing,
        labelTrailingText = labelTrailingText,
        required = required,
        helperText = helperText,
        errorText = errorText,
        isError = isError,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
    )
}

/**
 * Shared implementation of every field in this package. [shape] sets the corners, [maxLength] adds
 * the "n/max" counter and cuts growth past it like HTML maxlength (a long paste keeps what fits;
 * deleting is always allowed), and [previewFocused] freezes the focused look for previews only.
 */
@Composable
internal fun ComicTextFieldImpl(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    labelTrailingText: String? = null,
    required: Boolean = false,
    helperText: String? = null,
    errorText: String? = null,
    isError: Boolean = errorText != null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = HeyMikaTheme.shapes.input,
    maxLength: Int? = null,
    previewFocused: Boolean = false,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val typography = HeyMikaTheme.typography
    val borderColor =
        when {
            !enabled -> colors.disabledBorder
            isError -> colors.danger
            focused || previewFocused -> colors.primary
            else -> colors.borderMuted
        }
    val halo =
        if (previewFocused) {
            Modifier.staticFocusRing(shape = shape, color = colors.focusHalo, gap = 0.dp)
        } else {
            Modifier.comicFocusRing(source, shape = shape, color = colors.focusHalo, gap = 0.dp)
        }
    val errorMessage = if (isError) errorText else null
    val contentColor = if (enabled) colors.textPrimary else colors.disabledContent
    val iconTint = if (enabled) colors.textMuted else colors.disabledContent
    BasicTextField(
        value = value,
        onValueChange = { next ->
            onValueChange(if (maxLength == null) next else clampEditToMaxLength(value, next, maxLength))
        },
        modifier = modifier.semantics { if (errorMessage != null) error(errorMessage) },
        enabled = enabled,
        readOnly = readOnly,
        textStyle = typography.bodyMd.copy(color = contentColor),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        interactionSource = source,
        cursorBrush = SolidColor(if (isError) colors.danger else colors.primary),
        decorationBox = { innerTextField ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceSm),
            ) {
                if (label != null) {
                    ComicFieldLabel(
                        text = label,
                        required = required,
                        trailingText = labelTrailingText,
                        enabled = enabled,
                    )
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = dimens.inputHeight)
                            .then(halo)
                            .background(if (enabled) colors.surface else colors.disabledContainer, shape)
                            .border(dimens.borderControl, borderColor, shape)
                            .padding(
                                start = dimens.spaceSmPlus,
                                end = if (trailing != null) dimens.spaceXs else dimens.spaceSmPlus,
                            ),
                    horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .padding(top = if (singleLine) 0.dp else dimens.spaceMd + dimens.spaceXxs)
                                    .size(dimens.iconSize),
                            tint = iconTint,
                        )
                    }
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(vertical = if (singleLine) dimens.spaceSm else dimens.spaceMd),
                        contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
                    ) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = typography.bodyMd,
                                color = colors.textMuted,
                                maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                    if (trailing != null) trailing()
                }
                FieldMessages(
                    helperText = helperText,
                    errorText = errorMessage,
                    length = value.length,
                    maxLength = maxLength,
                    enabled = enabled,
                )
            }
        },
    )
}

@Composable
private fun FieldMessages(
    helperText: String?,
    errorText: String?,
    length: Int,
    maxLength: Int?,
    enabled: Boolean,
) {
    if (helperText == null && errorText == null && maxLength == null) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HeyMikaTheme.dimens.spaceSm),
        verticalAlignment = Alignment.Top,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            when {
                // The field node already carries the error semantics, so the callout is visual only.
                errorText != null -> {
                    FieldErrorCallout(
                        text = errorText,
                        modifier = Modifier.clearAndSetSemantics {},
                    )
                }

                helperText != null -> {
                    FieldHelperText(
                        text = helperText,
                        enabled = enabled,
                    )
                }
            }
        }
        if (maxLength != null) {
            FieldCounter(
                length = length,
                maxLength = maxLength,
            )
        }
    }
}

@Composable
private fun FieldCounter(
    length: Int,
    maxLength: Int,
) {
    val description = stringResource(R.string.controls_counter_description, length, maxLength)
    Text(
        text = stringResource(R.string.controls_counter, length, maxLength),
        modifier = Modifier.semantics { contentDescription = description },
        style = HeyMikaTheme.typography.caption,
        color = if (length > maxLength) HeyMikaTheme.colors.danger else HeyMikaTheme.colors.textMuted,
    )
}

@Preview(name = "States", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicTextFieldStatesPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ComicTextField(
                value = "",
                onValueChange = {},
                label = "Nama Lengkap",
                placeholder = "Contoh: Mikael Pratama",
                leadingIcon = Icons.Outlined.Person,
                labelTrailingText = "Nama panggilan belajarmu",
                required = true,
            )
            ComicTextFieldImpl(
                value = "pelajar@heymika.id",
                onValueChange = {},
                label = "Email",
                leadingIcon = Icons.Outlined.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                previewFocused = true,
            )
            ComicTextField(
                value = "pelajar@heymika",
                onValueChange = {},
                label = "Email",
                leadingIcon = Icons.Outlined.Email,
                errorText = "Format email belum benar. Contoh: nama@email.com",
            )
            ComicTextField(
                value = "budi.pratama@kampus.ac.id",
                onValueChange = {},
                label = "Email Terdaftar",
                leadingIcon = Icons.Outlined.Email,
                helperText = "Diperbarui 3 bulan lalu via SSO Kampus",
                enabled = false,
            )
        }
    }
}

@Preview(name = "Helper, no icon, read-only", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicTextFieldVariantsPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ComicTextField(
                value = "budipratama_id",
                onValueChange = {},
                label = "Username Pengenal",
                leadingIcon = Icons.Outlined.AlternateEmail,
                helperText = "Tampil pada peringkat kelas dan sesi belajar kelompok bersama Mika.",
            )
            ComicTextField(
                value = "",
                onValueChange = {},
                placeholder = "Tanpa label dan ikon",
            )
            ComicTextField(
                value = "Kak Budi",
                onValueChange = {},
                label = "Sapaan Mika ke Kamu",
                readOnly = true,
            )
            ComicTextFieldImpl(
                value = "",
                onValueChange = {},
                label = "Fokus dan error",
                placeholder = "nama@email.com",
                leadingIcon = Icons.Outlined.Email,
                errorText = "Email wajib diisi.",
                previewFocused = true,
            )
        }
    }
}
