package com.ai.heymika.core.ui.component.foundation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * Which ComicButton to use:
 * - [Primary]: the one main action of a screen or card (blue fill, #005FC4 base).
 * - [Secondary]: orange accent for celebration and gamification CTAs ("Tanya Mika",
 *   "Tambah Materi"); 3dp ink outline, ink label, #914C00 base.
 * - [Outline]: a strong neutral alternative (white, 2dp ink border, ink base).
 * - [Tonal]: soft secondary actions next to a Primary ("Buka Riwayat", "Lapor Bug", the Google
 *   sign-in button): surfaceContainerHigh fill, onSurface label, no border, a soft borderMuted base.
 * - [Ghost]: low-emphasis and text-like actions ("Lupa password?"); flat, primaryText label.
 * - [Danger]: destructive confirmations only (danger fill, white label, #6F3900 base).
 */
enum class ComicButtonVariant { Primary, Secondary, Outline, Tonal, Ghost, Danger }

/** Face heights: [Large] 56dp, [Medium] 48dp, [Small] 40dp (still a 48dp touch target). */
enum class ComicButtonSize { Large, Medium, Small }

/**
 * Colours of a ComicButton. [base] is the hard press shadow; [Color.Transparent] makes the button
 * flat (no base, no press-translate, ripple instead). [border] is drawn only when not transparent.
 */
@Immutable
data class ComicButtonColors(
    val container: Color,
    val content: Color,
    val border: Color,
    val base: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
    val disabledBorder: Color,
    val disabledBase: Color,
)

object ComicButtonDefaults {
    @Composable
    @ReadOnlyComposable
    fun colors(variant: ComicButtonVariant): ComicButtonColors {
        val c = HeyMikaTheme.colors
        val disabled =
            ComicButtonColors(
                container = c.disabledContainer,
                content = c.disabledContent,
                border = Color.Transparent,
                base = c.disabledBorder,
                disabledContainer = c.disabledContainer,
                disabledContent = c.disabledContent,
                disabledBorder = Color.Transparent,
                disabledBase = c.disabledBorder,
            )
        return when (variant) {
            ComicButtonVariant.Primary -> {
                disabled.copy(container = c.primary, content = c.onPrimary, base = c.primaryPressShadow)
            }

            ComicButtonVariant.Secondary -> {
                disabled.copy(
                    container = c.accent,
                    content = c.onAccent,
                    border = c.ink,
                    base = c.accentPressShadow,
                    disabledBorder = c.disabledBorder,
                )
            }

            ComicButtonVariant.Outline -> {
                disabled.copy(
                    container = c.surface,
                    content = c.ink,
                    border = c.ink,
                    base = c.ink,
                    disabledContainer = c.surface,
                    disabledBorder = c.disabledBorder,
                )
            }

            ComicButtonVariant.Tonal -> {
                disabled.copy(
                    container = MaterialTheme.colorScheme.surfaceContainerHigh,
                    content = c.textPrimary,
                    base = c.borderMuted,
                )
            }

            ComicButtonVariant.Ghost -> {
                disabled.copy(
                    container = Color.Transparent,
                    content = c.primaryText,
                    base = Color.Transparent,
                    disabledContainer = Color.Transparent,
                    disabledBase = Color.Transparent,
                )
            }

            ComicButtonVariant.Danger -> {
                disabled.copy(container = c.danger, content = c.onDanger, base = c.dangerPressShadow)
            }
        }
    }

    /** Secondary uses the 3dp ink border of the comic accent button; every other border is 2dp. */
    @Composable
    @ReadOnlyComposable
    fun borderWidth(variant: ComicButtonVariant): Dp =
        if (variant == ComicButtonVariant.Secondary) {
            HeyMikaTheme.dimens.borderInk
        } else {
            HeyMikaTheme.dimens.borderControl
        }

    @Composable
    @ReadOnlyComposable
    fun height(size: ComicButtonSize): Dp =
        when (size) {
            ComicButtonSize.Large -> HeyMikaTheme.dimens.buttonHeightLarge
            ComicButtonSize.Medium -> HeyMikaTheme.dimens.buttonHeightMedium
            ComicButtonSize.Small -> HeyMikaTheme.dimens.buttonHeightSmall
        }

    @Composable
    @ReadOnlyComposable
    fun contentPadding(size: ComicButtonSize): PaddingValues =
        when (size) {
            ComicButtonSize.Large -> PaddingValues(horizontal = HeyMikaTheme.dimens.spaceLg)
            ComicButtonSize.Medium -> PaddingValues(horizontal = HeyMikaTheme.dimens.spaceMdPlus)
            ComicButtonSize.Small -> PaddingValues(horizontal = HeyMikaTheme.dimens.spaceSmPlus)
        }

    @Composable
    @ReadOnlyComposable
    fun iconSize(size: ComicButtonSize): Dp =
        if (size == ComicButtonSize.Small) HeyMikaTheme.dimens.iconSizeSmall else HeyMikaTheme.dimens.iconSize

    @Composable
    @ReadOnlyComposable
    fun textStyle(size: ComicButtonSize): TextStyle =
        if (size == ComicButtonSize.Small) HeyMikaTheme.typography.buttonSmall else HeyMikaTheme.typography.button
}

/**
 * The HeyMika button: a face on a hard base that sinks 2dp when pressed (150ms), with a focus
 * ring for keyboard/D-pad, Role.Button and a 48dp minimum touch target.
 *
 * The layout height is the face height plus the 4dp base for raised variants (Large = 60dp).
 * While [loading] the label becomes [loadingText] (or stays [text]) next to a spinner, icons are
 * hidden and clicks are ignored; TalkBack hears "Memuat…" as the state.
 */
@Composable
fun ComicButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ComicButtonVariant = ComicButtonVariant.Primary,
    size: ComicButtonSize = ComicButtonSize.Large,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingText: String? = null,
    fullWidth: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    colors: ComicButtonColors = ComicButtonDefaults.colors(variant),
    interactionSource: MutableInteractionSource? = null,
) {
    ComicTextButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        size = size,
        enabled = enabled,
        loading = loading,
        loadingText = loadingText,
        fullWidth = fullWidth,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = colors,
        interactionSource = interactionSource,
    )
}

/**
 * Slot version of [ComicButton] for custom content (a painter icon such as the Google "G", a
 * trailing pill). [content] is laid out in a centred Row with the variant's content colour and
 * text style already provided. While [loading] a spinner is shown before [content].
 */
@Composable
fun ComicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ComicButtonVariant = ComicButtonVariant.Primary,
    size: ComicButtonSize = ComicButtonSize.Large,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
    colors: ComicButtonColors = ComicButtonDefaults.colors(variant),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    ComicButtonFrame(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        size = size,
        enabled = enabled,
        loading = loading,
        fullWidth = fullWidth,
        colors = colors,
        interactionSource = interactionSource,
    ) {
        if (loading) ComicButtonSpinner(size)
        content()
    }
}

@Composable
internal fun ComicTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ComicButtonVariant = ComicButtonVariant.Primary,
    size: ComicButtonSize = ComicButtonSize.Large,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingText: String? = null,
    fullWidth: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    colors: ComicButtonColors = ComicButtonDefaults.colors(variant),
    interactionSource: MutableInteractionSource? = null,
    previewPressed: Boolean = false,
    previewFocused: Boolean = false,
) {
    val iconSize = ComicButtonDefaults.iconSize(size)
    ComicButtonFrame(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        size = size,
        enabled = enabled,
        loading = loading,
        fullWidth = fullWidth,
        colors = colors,
        interactionSource = interactionSource,
        previewPressed = previewPressed,
        previewFocused = previewFocused,
    ) {
        if (loading) {
            ComicButtonSpinner(size)
        } else if (leadingIcon != null) {
            Icon(imageVector = leadingIcon, contentDescription = null, modifier = Modifier.size(iconSize))
        }
        Text(
            text = if (loading) loadingText ?: text else text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!loading && trailingIcon != null) {
            Icon(imageVector = trailingIcon, contentDescription = null, modifier = Modifier.size(iconSize))
        }
    }
}

@Composable
private fun ComicButtonFrame(
    onClick: () -> Unit,
    modifier: Modifier,
    variant: ComicButtonVariant,
    size: ComicButtonSize,
    enabled: Boolean,
    loading: Boolean,
    fullWidth: Boolean,
    colors: ComicButtonColors,
    interactionSource: MutableInteractionSource?,
    previewPressed: Boolean = false,
    previewFocused: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val loadingLabel = stringResource(R.string.foundation_loading)
    val gap = if (size == ComicButtonSize.Small) HeyMikaTheme.dimens.spaceXs else HeyMikaTheme.dimens.spaceSm
    ComicPressable(
        onClick = onClick,
        modifier = if (fullWidth) modifier.fillMaxWidth() else modifier,
        shape = HeyMikaTheme.shapes.button,
        colors = colors,
        borderWidth = ComicButtonDefaults.borderWidth(variant),
        faceHeight = ComicButtonDefaults.height(size),
        faceWidth = null,
        contentPadding = ComicButtonDefaults.contentPadding(size),
        enabled = enabled,
        clickEnabled = enabled && !loading,
        interactionSource = interactionSource,
        semantics = { if (loading) stateDescription = loadingLabel },
        previewPressed = previewPressed,
        previewFocused = previewFocused,
    ) {
        ProvideTextStyle(ComicButtonDefaults.textStyle(size)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

private val SpinnerStrokeWidth = 2.5.dp

/** The loading spinner, sized like the button's icons (16dp Small, 20dp otherwise). */
@Composable
internal fun ComicButtonSpinner(size: ComicButtonSize) {
    CircularProgressIndicator(
        modifier = Modifier.size(ComicButtonDefaults.iconSize(size)),
        color = LocalContentColor.current,
        strokeWidth = SpinnerStrokeWidth,
        trackColor = Color.Transparent,
        strokeCap = StrokeCap.Round,
    )
}

@Preview(name = "Variants", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicButtonVariantsPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ComicButton(
                text = "Masuk Sekarang",
                onClick = {},
                fullWidth = true,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            )
            ComicButton(
                text = "Tambah Materi",
                onClick = {},
                variant = ComicButtonVariant.Secondary,
                fullWidth = true,
                leadingIcon = Icons.Filled.Add,
            )
            ComicButton(text = "Pilihan netral", onClick = {}, variant = ComicButtonVariant.Outline)
            ComicButton(text = "Lanjutkan dengan Google", onClick = {}, variant = ComicButtonVariant.Tonal)
            ComicButton(text = "Lupa password?", onClick = {}, variant = ComicButtonVariant.Ghost)
            ComicButton(
                text = "Hapus Akun",
                onClick = {},
                variant = ComicButtonVariant.Danger,
                leadingIcon = Icons.Filled.DeleteOutline,
            )
        }
    }
}

@Preview(name = "Sizes", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicButtonSizesPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ComicButton(text = "Besar 56dp", onClick = {}, size = ComicButtonSize.Large)
            ComicButton(text = "Sedang 48dp", onClick = {}, size = ComicButtonSize.Medium)
            ComicButton(text = "Kecil 40dp", onClick = {}, size = ComicButtonSize.Small)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ComicButton(
                    text = "Kerjakan Kuis",
                    onClick = {},
                    size = ComicButtonSize.Small,
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                )
                ComicButton(
                    text = "Mulai Review",
                    onClick = {},
                    variant = ComicButtonVariant.Outline,
                    size = ComicButtonSize.Small,
                )
            }
        }
    }
}

@Preview(name = "States", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicButtonStatesPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ComicTextButton(text = "Ditekan (pressed)", onClick = {}, fullWidth = true, previewPressed = true)
            ComicTextButton(text = "Fokus (keyboard)", onClick = {}, fullWidth = true, previewFocused = true)
            ComicButton(
                text = "Masuk Sekarang",
                onClick = {},
                fullWidth = true,
                loading = true,
                loadingText = "Memverifikasi...",
            )
            ComicButton(text = "Tidak aktif", onClick = {}, fullWidth = true, enabled = false)
            ComicButton(
                text = "Tidak aktif",
                onClick = {},
                variant = ComicButtonVariant.Outline,
                fullWidth = true,
                enabled = false,
            )
            ComicButton(
                text = "Tidak aktif",
                onClick = {},
                variant = ComicButtonVariant.Ghost,
                enabled = false,
            )
        }
    }
}

@Preview(name = "Pressed and focused, all variants", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicButtonInteractionPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ComicButtonVariant.entries.forEach { variant ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ComicTextButton(
                        text = "Ditekan",
                        onClick = {},
                        variant = variant,
                        size = ComicButtonSize.Medium,
                        previewPressed = true,
                    )
                    ComicTextButton(
                        text = "Fokus",
                        onClick = {},
                        variant = variant,
                        size = ComicButtonSize.Medium,
                        previewFocused = true,
                    )
                }
            }
        }
    }
}

@Preview(name = "Slot content", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicButtonSlotPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ComicButton(onClick = {}, variant = ComicButtonVariant.Outline, fullWidth = true) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(text = "Konten bebas")
            }
            ComicButton(onClick = {}, fullWidth = true, loading = true) {
                Text(text = "Mengunci Transaksi...")
            }
        }
    }
}
