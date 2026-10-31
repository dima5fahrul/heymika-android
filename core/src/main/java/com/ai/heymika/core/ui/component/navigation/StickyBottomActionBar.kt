package com.ai.heymika.core.ui.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.component.foundation.SubtleDivider
import com.ai.heymika.core.ui.theme.HeyMikaTheme

object StickyBottomActionBarDefaults {
    /** Above the navigation bar and, while it is open, above the keyboard. */
    val windowInsets: WindowInsets
        @Composable
        get() = WindowInsets.navigationBars.union(WindowInsets.ime)
}

/**
 * A white action bar pinned to the bottom of a screen, with a top hairline, holding an optional
 * [caption] row ("7/10 terjawab") and one or two rows of buttons in [content]. Its background
 * reaches under the navigation bar while the buttons stay above it ([windowInsets]); it swallows
 * touches so nothing underneath is tapped through it.
 *
 * [onHeightChange] reports the full measured height, insets included, whenever it changes: give it
 * to the scrolling content as bottom padding when the bar overlays the content in a Box, so the
 * last item is never hidden under the bar.
 */
@Composable
fun StickyBottomActionBar(
    modifier: Modifier = Modifier,
    onHeightChange: (Dp) -> Unit = {},
    caption: (@Composable RowScope.() -> Unit)? = null,
    windowInsets: WindowInsets = StickyBottomActionBarDefaults.windowInsets,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val density = LocalDensity.current
    val currentOnHeightChange by rememberUpdatedState(onHeightChange)
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .onSizeChanged { size -> currentOnHeightChange(with(density) { size.height.toDp() }) }
                .background(colors.surface)
                .pointerInput(Unit) {},
    ) {
        SubtleDivider()
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(windowInsets)
                    .padding(horizontal = dimens.screenMargin, vertical = dimens.spaceSmPlus),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceSm),
        ) {
            if (caption != null) {
                CompositionLocalProvider(LocalContentColor provides colors.textMuted) {
                    ProvideTextStyle(HeyMikaTheme.typography.caption) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
                            content = caption,
                        )
                    }
                }
            }
            content()
        }
    }
}

@Preview(name = "Quiz bar", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun StickyBottomActionBarQuizPreview() {
    HeyMikaTheme {
        StickyBottomActionBar(
            caption = {
                Text(text = "7 dari 10 soal terjawab", modifier = Modifier.weight(1f))
                Text(text = "Tersimpan otomatis")
            },
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ComicButton(
                    text = "No. 3",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    variant = ComicButtonVariant.Outline,
                    size = ComicButtonSize.Small,
                    fullWidth = true,
                    leadingIcon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                )
                ComicButton(
                    text = "No. 5",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    variant = ComicButtonVariant.Outline,
                    size = ComicButtonSize.Small,
                    fullWidth = true,
                    trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                )
            }
            ComicButton(
                text = "Selesaikan & Kumpulkan",
                onClick = {},
                variant = ComicButtonVariant.Secondary,
                fullWidth = true,
                leadingIcon = Icons.Filled.DoneAll,
            )
        }
    }
}

@Preview(name = "Overlaying a list", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360, heightDp = 240)
@Composable
private fun StickyBottomActionBarOverlayPreview() {
    HeyMikaTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Konten layar di sini. Daftar memakai tinggi bar sebagai padding bawah.",
                modifier = Modifier.padding(16.dp),
            )
            StickyBottomActionBar(modifier = Modifier.align(Alignment.BottomCenter)) {
                ComicButton(text = "Simpan Perubahan", onClick = {}, fullWidth = true)
            }
        }
    }
}
