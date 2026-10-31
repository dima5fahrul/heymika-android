package com.ai.heymika.core.ui.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.component.foundation.ComicButtonDefaults
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.component.foundation.ComicIconButton
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * The label row above a field: [text] in labelMd, a danger asterisk when [required] (TalkBack
 * hears "<label>, wajib diisi" instead of "star"), and an optional right-aligned [trailingText] in
 * caption ("Disapa di dashboard", "Maks. 80 Karakter", "Cocok!" with a success colour).
 *
 * Every field of this package draws it; use it directly above a picker or an option-card group.
 */
@Composable
fun ComicFieldLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    trailingText: String? = null,
    trailingTextColor: Color = HeyMikaTheme.colors.textMuted,
    enabled: Boolean = true,
) {
    val colors = HeyMikaTheme.colors
    val requiredLabel = stringResource(R.string.controls_field_required_label, text)
    val labelText =
        buildAnnotatedString {
            append(text)
            if (required) {
                append(" ")
                withStyle(SpanStyle(color = colors.danger)) { append("*") }
            }
        }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HeyMikaTheme.dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = labelText,
            modifier =
                Modifier
                    .weight(1f)
                    .then(if (required) Modifier.semantics { contentDescription = requiredLabel } else Modifier),
            style = HeyMikaTheme.typography.labelMd,
            color = if (enabled) colors.textPrimary else colors.disabledContent,
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = HeyMikaTheme.typography.caption,
                color = trailingTextColor,
                textAlign = TextAlign.End,
            )
        }
    }
}

/**
 * Helper or error text under a field. [errorText] wins over [helperText]: it renders as a dangerBg
 * callout with an alert icon and TalkBack announces it politely when it appears. Renders nothing
 * when both are null.
 *
 * The fields of this package draw their own; use this under a picker, a required CheckboxRow
 * or an option-card group.
 */
@Composable
fun ComicFieldSupportingText(
    modifier: Modifier = Modifier,
    helperText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
) {
    when {
        errorText != null -> {
            FieldErrorCallout(
                text = errorText,
                modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }

        helperText != null -> {
            FieldHelperText(
                text = helperText,
                enabled = enabled,
                modifier = modifier,
            )
        }
    }
}

/**
 * A 20dp textMuted icon button for a field's trailing slot (password toggle, clear, filter): a flat
 * Ghost circle with a 48dp touch target, a ripple and the focus ring. [contentDescription] is
 * required because the icon is the only label.
 */
@Composable
fun ComicFieldIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    ComicIconButton(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        variant = ComicButtonVariant.Ghost,
        size = ComicButtonSize.Small,
        enabled = enabled,
        colors = ComicButtonDefaults.colors(ComicButtonVariant.Ghost).copy(content = HeyMikaTheme.colors.textMuted),
    )
}

@Composable
internal fun FieldErrorCallout(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.dangerBg, HeyMikaTheme.shapes.small)
                .padding(horizontal = dimens.spaceSm, vertical = dimens.spaceXs),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(dimens.iconSizeSmall),
            tint = colors.danger,
        )
        Text(
            text = text,
            style = HeyMikaTheme.typography.caption,
            color = colors.danger,
        )
    }
}

@Composable
internal fun FieldHelperText(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = HeyMikaTheme.typography.caption,
        color = if (enabled) HeyMikaTheme.colors.textMuted else HeyMikaTheme.colors.disabledContent,
    )
}

@Preview(name = "Label row", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicFieldLabelPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ComicFieldLabel(text = "Email")
            ComicFieldLabel(
                text = "Nama Panggilan Kamu",
                required = true,
                trailingText = "Disapa di dashboard",
            )
            ComicFieldLabel(
                text = "Konfirmasi Kata Sandi",
                required = true,
                trailingText = "Cocok!",
                trailingTextColor = HeyMikaTheme.colors.success,
            )
            ComicFieldLabel(
                text = "Email Terdaftar",
                trailingText = "Tidak bisa diubah",
                enabled = false,
            )
        }
    }
}

@Preview(name = "Supporting text", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicFieldSupportingTextPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ComicFieldSupportingText(helperText = "Tampil pada peringkat kelas dan sesi belajar kelompok.")
            ComicFieldSupportingText(errorText = "Kamu perlu menyetujui Ketentuan Layanan dulu, ya.")
            ComicFieldSupportingText(
                helperText = "Diabaikan karena ada error",
                errorText = "Pilih salah satu status belajar.",
            )
            ComicFieldSupportingText(
                helperText = "Bantuan saat tidak aktif",
                enabled = false,
            )
        }
    }
}

@Preview(name = "Field icon button", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicFieldIconButtonPreview() {
    HeyMikaTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ComicFieldIconButton(
                icon = Icons.Outlined.Tune,
                contentDescription = "Filter",
                onClick = {},
            )
            ComicFieldIconButton(
                icon = Icons.Outlined.ExpandMore,
                contentDescription = "Buka pilihan",
                onClick = {},
                enabled = false,
            )
        }
    }
}
