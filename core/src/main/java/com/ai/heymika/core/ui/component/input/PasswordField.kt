package com.ai.heymika.core.ui.component.input

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * A [ComicTextField] for passwords: masked by default, with a lock [leadingIcon] and a trailing eye
 * button that shows or hides the text. The button's TalkBack label says what it will do
 * ("Tampilkan kata sandi" / "Sembunyikan kata sandi"). The visibility survives rotation.
 *
 * The keyboard is a password keyboard (no suggestions) with [imeAction]. Pair it with a
 * [PasswordStrengthChecklist] for new passwords.
 */
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = Icons.Outlined.Lock,
    labelTrailingText: String? = null,
    required: Boolean = false,
    helperText: String? = null,
    errorText: String? = null,
    isError: Boolean = errorText != null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource? = null,
) {
    PasswordFieldImpl(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        labelTrailingText = labelTrailingText,
        required = required,
        helperText = helperText,
        errorText = errorText,
        isError = isError,
        enabled = enabled,
        imeAction = imeAction,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun PasswordFieldImpl(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = Icons.Outlined.Lock,
    labelTrailingText: String? = null,
    required: Boolean = false,
    helperText: String? = null,
    errorText: String? = null,
    isError: Boolean = errorText != null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource? = null,
    initiallyVisible: Boolean = false,
    previewFocused: Boolean = false,
) {
    var visible by rememberSaveable { mutableStateOf(initiallyVisible) }
    val mask = remember { PasswordVisualTransformation() }
    val toggleLabel =
        stringResource(if (visible) R.string.controls_password_hide else R.string.controls_password_show)
    ComicTextFieldImpl(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailing = {
            ComicFieldIconButton(
                icon = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                contentDescription = toggleLabel,
                onClick = { visible = !visible },
                enabled = enabled,
            )
        },
        labelTrailingText = labelTrailingText,
        required = required,
        helperText = helperText,
        errorText = errorText,
        isError = isError,
        enabled = enabled,
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else mask,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        previewFocused = previewFocused,
    )
}

@Preview(name = "States", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun PasswordFieldStatesPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PasswordField(
                value = "",
                onValueChange = {},
                label = "Kata Sandi",
                placeholder = "Minimal 12 karakter aman",
                labelTrailingText = "Mendukung autofill & paste",
                required = true,
            )
            PasswordFieldImpl(
                value = "rahasia123",
                onValueChange = {},
                label = "Kata Sandi",
                previewFocused = true,
            )
            PasswordField(
                value = "rahasia123",
                onValueChange = {},
                label = "Kata Sandi",
                errorText = "Email atau sandi salah. Coba periksa lagi, ya.",
            )
            PasswordField(
                value = "secretpassword123",
                onValueChange = {},
                label = "Kata Sandi Saat Ini",
                enabled = false,
            )
        }
    }
}

@Preview(name = "Visible and confirm", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun PasswordFieldVisiblePreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PasswordFieldImpl(
                value = "Rahasia#Mika2025",
                onValueChange = {},
                label = "Password Baru",
                required = true,
                imeAction = ImeAction.Next,
                initiallyVisible = true,
            )
            PasswordField(
                value = "",
                onValueChange = {},
                label = "Konfirmasi Password Baru",
                placeholder = "Ulangi password baru",
                required = true,
            )
        }
    }
}
