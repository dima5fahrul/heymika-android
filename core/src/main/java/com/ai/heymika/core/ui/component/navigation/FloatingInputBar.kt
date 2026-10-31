package com.ai.heymika.core.ui.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.component.foundation.ComicIconButton
import com.ai.heymika.core.ui.component.foundation.SubtleDivider
import com.ai.heymika.core.ui.modifier.ComicFocusRingDefaults
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.comicSurface
import com.ai.heymika.core.ui.modifier.subtleSurface
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * - [Docked]: full-width white bar with a top hairline, pinned to the bottom of the tutor chat.
 * - [Floating]: the Beranda "Tanya Mika" quick bar, a sky-bordered card with screen margins that
 *   floats above the bottom navigation.
 */
enum class FloatingInputBarStyle { Docked, Floating }

object FloatingInputBarDefaults {
    /** The field grows up to this many lines, then scrolls. */
    const val MAX_LINES = 4

    /** Docked bars sit above the navigation bar and the keyboard; floating ones are placed by the parent. */
    @Composable
    fun windowInsets(style: FloatingInputBarStyle): WindowInsets =
        when (style) {
            FloatingInputBarStyle.Docked -> WindowInsets.navigationBars.union(WindowInsets.ime)
            FloatingInputBarStyle.Floating -> WindowInsets(0, 0, 0, 0)
        }
}

/**
 * Chat-style "ask Mika" input: a growing text field and an orange send button (the comic send
 * FAB). The keyboard's Send action and the button both call [onSend], only when [value] is not
 * blank, the bar is [enabled] and nothing is [sending]. There is no voice button (voice is v2).
 *
 * [leading] is an optional slot before the field (the Mika avatar on Beranda). [topContent] sits
 * above the input row (the tutor's suggestion chips). [onHeightChange] reports the full measured
 * height, insets included, for the bottom padding of the scrolling content behind the bar.
 */
@Composable
fun FloatingInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.chrome_input_placeholder),
    style: FloatingInputBarStyle = FloatingInputBarStyle.Docked,
    enabled: Boolean = true,
    sending: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    topContent: (@Composable () -> Unit)? = null,
    onHeightChange: (Dp) -> Unit = {},
    windowInsets: WindowInsets = FloatingInputBarDefaults.windowInsets(style),
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val density = LocalDensity.current
    val currentOnHeightChange by rememberUpdatedState(onHeightChange)
    val canSend = enabled && !sending && value.isNotBlank()
    val reportHeight =
        Modifier.onSizeChanged { size -> currentOnHeightChange(with(density) { size.height.toDp() }) }
    val row: @Composable () -> Unit = {
        InputRow(
            value = value,
            onValueChange = onValueChange,
            onSend = onSend,
            placeholder = placeholder,
            style = style,
            enabled = enabled,
            sending = sending,
            canSend = canSend,
            leading = leading,
            source = source,
        )
    }
    when (style) {
        FloatingInputBarStyle.Docked -> {
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .then(reportHeight)
                        .background(colors.surface)
                        .pointerInput(Unit) {},
            ) {
                SubtleDivider()
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(windowInsets)
                            .padding(horizontal = dimens.spaceSmPlus, vertical = dimens.spaceSm),
                    verticalArrangement = Arrangement.spacedBy(dimens.spaceSm),
                ) {
                    topContent?.invoke()
                    row()
                }
            }
        }

        FloatingInputBarStyle.Floating -> {
            val shape = HeyMikaTheme.shapes.card
            Box(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .then(reportHeight)
                        .windowInsetsPadding(windowInsets)
                        .padding(horizontal = dimens.screenMargin, vertical = dimens.spaceSm),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .comicFocusRing(
                                interactionSource = source,
                                shape = shape,
                                color = ComicFocusRingDefaults.halo,
                                gap = 0.dp,
                            ).subtleSurface(shape = shape, borderColor = colors.sky, borderWidth = dimens.borderControl)
                            .pointerInput(Unit) {}
                            .padding(horizontal = dimens.spaceSm, vertical = dimens.spaceXs),
                    verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
                ) {
                    topContent?.invoke()
                    row()
                }
            }
        }
    }
}

@Composable
private fun InputRow(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    placeholder: String,
    style: FloatingInputBarStyle,
    enabled: Boolean,
    sending: Boolean,
    canSend: Boolean,
    leading: (@Composable () -> Unit)?,
    source: MutableInteractionSource,
) {
    val dimens = HeyMikaTheme.dimens
    val sendingLabel = stringResource(R.string.chrome_input_sending)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
    ) {
        leading?.invoke()
        InputField(
            value = value,
            onValueChange = onValueChange,
            onSend = { if (canSend) onSend() },
            placeholder = placeholder,
            docked = style == FloatingInputBarStyle.Docked,
            enabled = enabled,
            source = source,
        )
        ComicIconButton(
            icon = Icons.AutoMirrored.Filled.Send,
            contentDescription = stringResource(R.string.chrome_input_send),
            onClick = onSend,
            modifier = if (sending) Modifier.semantics { stateDescription = sendingLabel } else Modifier,
            variant = ComicButtonVariant.Secondary,
            size = if (style == FloatingInputBarStyle.Docked) ComicButtonSize.Medium else ComicButtonSize.Small,
            shape = HeyMikaTheme.shapes.button,
            enabled = canSend || (enabled && sending),
            loading = sending,
        )
    }
}

@Composable
private fun RowScope.InputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    placeholder: String,
    docked: Boolean,
    enabled: Boolean,
    source: MutableInteractionSource,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.cardLarge
    val focused by source.collectIsFocusedAsState()
    // Docked: a white pill on a hard 2dp ledge that turns primary with a halo while typing.
    // Floating: borderless inside the sky card, which carries the halo instead.
    val surface =
        if (docked) {
            Modifier
                .comicFocusRing(source, shape = shape, color = ComicFocusRingDefaults.halo, gap = 0.dp)
                .comicSurface(
                    shape = shape,
                    color = colors.surface,
                    borderColor = if (focused) colors.primary else colors.borderMuted,
                    borderWidth = dimens.borderSubtle,
                    shadowColor = if (focused) colors.primary else colors.borderMuted,
                    shadowOffset = DpOffset(0.dp, dimens.spaceXxs),
                )
        } else {
            Modifier
        }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            Modifier
                .weight(1f)
                .heightIn(min = dimens.minTouchTarget)
                .then(surface),
        enabled = enabled,
        textStyle = HeyMikaTheme.typography.bodyMd.copy(color = colors.textPrimary),
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
            ),
        keyboardActions = KeyboardActions(onSend = { onSend() }),
        singleLine = false,
        maxLines = FloatingInputBarDefaults.MAX_LINES,
        interactionSource = source,
        cursorBrush = SolidColor(colors.primary),
        decorationBox = { innerTextField ->
            Box(
                modifier =
                    Modifier.padding(
                        horizontal = if (docked) dimens.spaceMd else dimens.spaceXs,
                        vertical = dimens.spaceSmPlus,
                    ),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = HeyMikaTheme.typography.bodyMd,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun PreviewMikaAvatar() {
    Box(
        modifier =
            Modifier
                .size(32.dp)
                .background(HeyMikaTheme.colors.yellow, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Pets,
            contentDescription = null,
            tint = HeyMikaTheme.colors.ink,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun PreviewSuggestions() {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        listOf("Peran enzim aldolase", "Lanjut fase pembayaran", "Buat 3 latihan soal").forEach { text ->
            Text(
                text = text,
                style = HeyMikaTheme.typography.labelSm,
                color = HeyMikaTheme.colors.primaryText,
                modifier =
                    Modifier
                        .background(HeyMikaTheme.colors.canvas, HeyMikaTheme.shapes.pill)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Preview(name = "Docked (tutor)", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun FloatingInputBarDockedPreview() {
    HeyMikaTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FloatingInputBar(
                value = "",
                onValueChange = {},
                onSend = {},
                placeholder = "Tanya Mika seputar Bab 4 ini…",
                topContent = { PreviewSuggestions() },
            )
            FloatingInputBar(
                value = "Kenapa fase investasi butuh 2 ATP padahal tujuannya menghasilkan energi?",
                onValueChange = {},
                onSend = {},
            )
            FloatingInputBar(value = "Sedang dikirim", onValueChange = {}, onSend = {}, sending = true)
            FloatingInputBar(value = "", onValueChange = {}, onSend = {}, enabled = false)
        }
    }
}

@Preview(name = "Floating (Beranda)", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun FloatingInputBarFloatingPreview() {
    HeyMikaTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FloatingInputBar(
                value = "",
                onValueChange = {},
                onSend = {},
                style = FloatingInputBarStyle.Floating,
                leading = { PreviewMikaAvatar() },
            )
            FloatingInputBar(
                value = "Jelaskan BFS vs DFS",
                onValueChange = {},
                onSend = {},
                style = FloatingInputBarStyle.Floating,
                leading = { PreviewMikaAvatar() },
            )
        }
    }
}
