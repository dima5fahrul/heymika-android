package com.ai.heymika.core.ui.component.foundation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.modifier.animatePressTravel
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.comicPressSurface
import com.ai.heymika.core.ui.modifier.staticFocusRing
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * Shared frame of [ComicButton] and [ComicIconButton]: 48dp minimum touch target, focus ring,
 * clickable with Role.Button, and a [comicPressSurface] face that sinks onto its base.
 *
 * Raised colours (a visible base) get no ripple: the press-translate is the feedback. Flat colours
 * (Ghost) get a ripple clipped to [shape]. [previewPressed]/[previewFocused] freeze those states
 * for previews only.
 */
@Composable
internal fun ComicPressable(
    onClick: () -> Unit,
    modifier: Modifier,
    shape: Shape,
    colors: ComicButtonColors,
    borderWidth: Dp,
    faceHeight: Dp,
    faceWidth: Dp?,
    contentPadding: PaddingValues,
    enabled: Boolean,
    clickEnabled: Boolean,
    interactionSource: MutableInteractionSource?,
    semantics: SemanticsPropertyReceiver.() -> Unit = {},
    previewPressed: Boolean = false,
    previewFocused: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val dimens = HeyMikaTheme.dimens
    val raised = colors.base.alpha > 0f
    val baseHeight = if (raised) dimens.buttonShadow else 0.dp
    val travelState = animatePressTravel(source, enabled = clickEnabled)
    val pressedTravel = dimens.pressTranslate
    val travel: () -> Dp = if (previewPressed) ({ pressedTravel }) else ({ travelState.value })
    val contentColor = if (enabled) colors.content else colors.disabledContent
    val ring = if (previewFocused) Modifier.staticFocusRing(shape) else Modifier.comicFocusRing(source, shape)
    val size =
        if (faceWidth != null) Modifier.width(faceWidth) else Modifier.widthIn(min = faceHeight)
    Box(
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .height(faceHeight + baseHeight)
                .then(size)
                .then(ring)
                .then(if (raised) Modifier else Modifier.clip(shape))
                .clickable(
                    interactionSource = source,
                    indication = if (raised) null else ripple(color = contentColor),
                    enabled = clickEnabled,
                    role = Role.Button,
                    onClick = onClick,
                ).semantics(properties = semantics)
                .comicPressSurface(
                    travel = travel,
                    color = if (enabled) colors.container else colors.disabledContainer,
                    baseColor = if (enabled) colors.base else colors.disabledBase,
                    shape = shape,
                    baseHeight = baseHeight,
                    borderWidth = borderWidth,
                    borderColor = if (enabled) colors.border else colors.disabledBorder,
                ).padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}
