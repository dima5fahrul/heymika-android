package com.ai.heymika.core.ui.component.foundation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * A square or round icon-only ComicButton. Same variants, sizes, press-translate and focus ring as
 * [ComicButton]; the face is [size] x [size] and the touch target is at least 48dp.
 *
 * [contentDescription] is required for TalkBack (Indonesian, from strings), unless a visible label
 * next to it already names the action, in which case pass null. Defaults to a flat
 * [ComicButtonVariant.Ghost] circle, the top-bar style; use [ComicButtonVariant.Secondary] with a
 * rounded [shape] for the send FAB. While [loading] a spinner replaces the icon, clicks are ignored
 * and TalkBack hears the loading state; the face keeps its enabled colours.
 */
@Composable
fun ComicIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ComicButtonVariant = ComicButtonVariant.Ghost,
    size: ComicButtonSize = ComicButtonSize.Medium,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    loading: Boolean = false,
    colors: ComicButtonColors = ComicButtonDefaults.colors(variant),
    interactionSource: MutableInteractionSource? = null,
) {
    ComicIconButtonImpl(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        size = size,
        shape = shape,
        enabled = enabled,
        loading = loading,
        colors = colors,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun ComicIconButtonImpl(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ComicButtonVariant = ComicButtonVariant.Ghost,
    size: ComicButtonSize = ComicButtonSize.Medium,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    loading: Boolean = false,
    colors: ComicButtonColors = ComicButtonDefaults.colors(variant),
    interactionSource: MutableInteractionSource? = null,
    previewPressed: Boolean = false,
    previewFocused: Boolean = false,
) {
    val face = ComicButtonDefaults.height(size)
    val iconSize =
        if (size == ComicButtonSize.Large) HeyMikaTheme.dimens.iconSizeLarge else HeyMikaTheme.dimens.iconSize
    val loadingLabel = stringResource(R.string.foundation_loading)
    ComicPressable(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        colors = colors,
        borderWidth = ComicButtonDefaults.borderWidth(variant),
        faceHeight = face,
        faceWidth = face,
        contentPadding = PaddingValues(0.dp),
        enabled = enabled,
        clickEnabled = enabled && !loading,
        interactionSource = interactionSource,
        semantics = { if (loading) stateDescription = loadingLabel },
        previewPressed = previewPressed,
        previewFocused = previewFocused,
    ) {
        if (loading) {
            // The spinner itself is silent; the button keeps its name plus the loading state.
            val label = contentDescription
            Box(
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .semantics { if (label != null) this.contentDescription = label },
            ) {
                ComicButtonSpinner(size)
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .size(iconSize),
            )
        }
    }
}

@Preview(name = "Variants and states", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicIconButtonPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ComicIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    onClick = {},
                )
                ComicIconButton(
                    icon = Icons.Outlined.Notifications,
                    contentDescription = "Notifikasi",
                    onClick = {},
                    variant = ComicButtonVariant.Outline,
                    size = ComicButtonSize.Small,
                )
                ComicIconButton(
                    icon = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Kirim ke Mika",
                    onClick = {},
                    variant = ComicButtonVariant.Secondary,
                    shape = HeyMikaTheme.shapes.button,
                )
                ComicIconButton(
                    icon = Icons.Filled.MoreVert,
                    contentDescription = "Opsi lainnya",
                    onClick = {},
                    variant = ComicButtonVariant.Primary,
                    size = ComicButtonSize.Large,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ComicIconButtonImpl(
                    icon = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Ditekan",
                    onClick = {},
                    variant = ComicButtonVariant.Primary,
                    previewPressed = true,
                )
                ComicIconButtonImpl(
                    icon = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Fokus",
                    onClick = {},
                    variant = ComicButtonVariant.Primary,
                    previewFocused = true,
                )
                ComicIconButton(
                    icon = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Tidak aktif",
                    onClick = {},
                    variant = ComicButtonVariant.Primary,
                    enabled = false,
                )
                ComicIconButton(
                    icon = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Mengirim",
                    onClick = {},
                    variant = ComicButtonVariant.Secondary,
                    shape = HeyMikaTheme.shapes.button,
                    loading = true,
                )
                ComicIconButtonImpl(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Fokus",
                    onClick = {},
                    previewFocused = true,
                )
            }
        }
    }
}
