package com.ai.heymika.core.ui.component.foundation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.comicSurface
import com.ai.heymika.core.ui.modifier.staticFocusRing
import com.ai.heymika.core.ui.modifier.subtleSurface
import com.ai.heymika.core.ui.theme.HeyMikaMotion
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * - [Strict]: DESIGN.md comic surface, 3dp ink border + 6dp orange hard shadow. Shells, heroes,
 *   selected content, auth cards, one featured card per screen.
 * - [Subtle]: 1.5dp borderMuted + soft ambient float. List, feed and utility cards, so long
 *   scrolling screens stay calm.
 */
enum class ComicCardVariant { Strict, Subtle }

/**
 * A card on [comicSurface] or [subtleSurface]. Content is laid out in a Column inside
 * [contentPadding] and clipped to [shape] (the border stays on top).
 *
 * With [onClick] the whole card becomes one button: Role.Button, merged TalkBack label, a 0.99
 * press scale, a ripple and the focus ring. A Strict card's shadow sits outside its bounds; leave
 * 6dp of end/bottom room (see [comicSurface]).
 */
@Composable
fun ComicCard(
    modifier: Modifier = Modifier,
    variant: ComicCardVariant = ComicCardVariant.Strict,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    enabled: Boolean = true,
    shape: Shape = HeyMikaTheme.shapes.card,
    color: Color = HeyMikaTheme.colors.surface,
    contentPadding: PaddingValues = PaddingValues(HeyMikaTheme.dimens.cardPadding),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ComicCardImpl(
        modifier = modifier,
        variant = variant,
        onClick = onClick,
        onClickLabel = onClickLabel,
        enabled = enabled,
        shape = shape,
        color = color,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
internal fun ComicCardImpl(
    modifier: Modifier = Modifier,
    variant: ComicCardVariant = ComicCardVariant.Strict,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    enabled: Boolean = true,
    shape: Shape = HeyMikaTheme.shapes.card,
    color: Color = HeyMikaTheme.colors.surface,
    contentPadding: PaddingValues = PaddingValues(HeyMikaTheme.dimens.cardPadding),
    interactionSource: MutableInteractionSource? = null,
    previewFocused: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val clickable = onClick != null && enabled
    val scale by animateFloatAsState(
        targetValue = if (pressed && clickable) PRESSED_CARD_SCALE else 1f,
        animationSpec = if (HeyMikaTheme.reducedMotion) snap() else tween(HeyMikaMotion.PRESS_MILLIS),
        label = "cardPressScale",
    )
    val surface =
        when (variant) {
            ComicCardVariant.Strict -> Modifier.comicSurface(shape = shape, color = color)
            ComicCardVariant.Subtle -> Modifier.subtleSurface(shape = shape, color = color)
        }
    val ring =
        when {
            previewFocused -> Modifier.staticFocusRing(shape)
            onClick != null -> Modifier.comicFocusRing(source, shape)
            else -> Modifier
        }
    val click =
        if (onClick != null) {
            Modifier.clickable(
                interactionSource = source,
                indication = ripple(),
                enabled = enabled,
                onClickLabel = onClickLabel,
                role = Role.Button,
                onClick = onClick,
            )
        } else {
            Modifier
        }
    Column(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.then(ring)
                .then(surface)
                .clip(shape)
                .then(click)
                .padding(contentPadding),
        content = content,
    )
}

private const val PRESSED_CARD_SCALE = 0.99f

@Preview(name = "Strict and Subtle", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicCardPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ComicCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Strict", style = HeyMikaTheme.typography.headlineSm)
                Text(text = "Shell, hero, dan konten terpilih.", style = HeyMikaTheme.typography.bodySm)
            }
            ComicCard(modifier = Modifier.fillMaxWidth(), variant = ComicCardVariant.Subtle) {
                Text(text = "Subtle", style = HeyMikaTheme.typography.headlineSm)
                Text(text = "Kartu daftar dan feed.", style = HeyMikaTheme.typography.bodySm)
            }
            ComicCard(
                modifier = Modifier.fillMaxWidth(),
                variant = ComicCardVariant.Subtle,
                onClick = {},
                onClickLabel = "Buka materi",
            ) {
                Text(text = "Subtle, bisa diketuk", style = HeyMikaTheme.typography.labelLg)
            }
            ComicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = HeyMikaTheme.shapes.shell,
                contentPadding = PaddingValues(HeyMikaTheme.dimens.shellPadding),
            ) {
                Text(text = "Shell 28dp", style = HeyMikaTheme.typography.headlineLg)
            }
        }
    }
}

@Preview(name = "Clickable states", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicCardStatesPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ComicCardImpl(modifier = Modifier.fillMaxWidth(), onClick = {}, previewFocused = true) {
                Text(text = "Strict, fokus", style = HeyMikaTheme.typography.labelLg)
            }
            ComicCardImpl(
                modifier = Modifier.fillMaxWidth(),
                variant = ComicCardVariant.Subtle,
                onClick = {},
                previewFocused = true,
            ) {
                Text(text = "Subtle, fokus", style = HeyMikaTheme.typography.labelLg)
            }
            ComicCard(modifier = Modifier.fillMaxWidth(), onClick = {}, enabled = false) {
                Text(text = "Tidak aktif", style = HeyMikaTheme.typography.labelLg)
            }
        }
    }
}
