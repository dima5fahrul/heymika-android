package com.ai.heymika.core.ui.modifier

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusEventModifierNode
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import com.ai.heymika.core.ui.theme.DefaultHeyMikaColors
import com.ai.heymika.core.ui.theme.DefaultHeyMikaDimens
import com.ai.heymika.core.ui.theme.DefaultHeyMikaShapes
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Defaults of [comicFocusRing]: a 3dp primary ring, 2dp outside the component. */
object ComicFocusRingDefaults {
    val shape: Shape get() = DefaultHeyMikaShapes.button
    val color: Color get() = DefaultHeyMikaColors.focusRing
    val width: Dp get() = DefaultHeyMikaDimens.focusRingWidth
    val gap: Dp get() = DefaultHeyMikaDimens.focusRingGap

    /** Halo recipe for text fields, whose own border turns primary: `color = halo, gap = 0.dp`. */
    val halo: Color get() = DefaultHeyMikaColors.focusHalo
}

/**
 * Draws a visible ring around the component while [interactionSource] reports focus, which for
 * clickables only happens through a keyboard, D-pad or TAB (touch never focuses a clickable), and
 * for text fields whenever they are being edited.
 *
 * Pass the SAME interaction source as the `clickable`/`toggleable`/`selectable`/text field. The
 * ring is drawn outside the bounds ([gap] then a [width] stroke), so layout never shifts. Apply
 * it before any `clip`, so the ring is not clipped. [shape] should match the component's shape;
 * the ring is that shape inflated by `gap + width / 2`.
 */
fun Modifier.comicFocusRing(
    interactionSource: InteractionSource,
    shape: Shape = ComicFocusRingDefaults.shape,
    color: Color = ComicFocusRingDefaults.color,
    width: Dp = ComicFocusRingDefaults.width,
    gap: Dp = ComicFocusRingDefaults.gap,
): Modifier = this then FocusRingElement(interactionSource, shape, color, width, gap, alwaysVisible = false)

/**
 * Same ring, driven by focus events instead of an interaction source: use it when the component
 * owns no interaction source. Place it BEFORE (to the left of) the `focusable`/`clickable` it
 * should observe; it shows only while that focus target itself is focused.
 */
fun Modifier.comicFocusRing(
    shape: Shape = ComicFocusRingDefaults.shape,
    color: Color = ComicFocusRingDefaults.color,
    width: Dp = ComicFocusRingDefaults.width,
    gap: Dp = ComicFocusRingDefaults.gap,
): Modifier = this then FocusRingElement(null, shape, color, width, gap, alwaysVisible = false)

/** Draws the ring unconditionally. Previews and gallery screenshots of the focused state only. */
internal fun Modifier.staticFocusRing(
    shape: Shape = ComicFocusRingDefaults.shape,
    color: Color = ComicFocusRingDefaults.color,
    width: Dp = ComicFocusRingDefaults.width,
    gap: Dp = ComicFocusRingDefaults.gap,
): Modifier = this then FocusRingElement(null, shape, color, width, gap, alwaysVisible = true)

private data class FocusRingElement(
    val interactionSource: InteractionSource?,
    val shape: Shape,
    val color: Color,
    val width: Dp,
    val gap: Dp,
    val alwaysVisible: Boolean,
) : ModifierNodeElement<FocusRingNode>() {
    override fun create(): FocusRingNode = FocusRingNode(interactionSource, shape, color, width, gap, alwaysVisible)

    override fun update(node: FocusRingNode) {
        node.update(interactionSource, shape, color, width, gap, alwaysVisible)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "comicFocusRing"
        properties["interactionSource"] = interactionSource
        properties["shape"] = shape
        properties["color"] = color
        properties["width"] = width
        properties["gap"] = gap
    }
}

private class FocusRingNode(
    private var interactionSource: InteractionSource?,
    private var shape: Shape,
    private var color: Color,
    private var width: Dp,
    private var gap: Dp,
    private var alwaysVisible: Boolean,
) : Modifier.Node(),
    DrawModifierNode,
    FocusEventModifierNode {
    private var interactionFocused = false
    private var eventFocused = false
    private var collectJob: Job? = null

    override fun onAttach() {
        collectFocus()
    }

    override fun onDetach() {
        collectJob = null
        interactionFocused = false
        eventFocused = false
    }

    fun update(
        interactionSource: InteractionSource?,
        shape: Shape,
        color: Color,
        width: Dp,
        gap: Dp,
        alwaysVisible: Boolean,
    ) {
        if (this.interactionSource != interactionSource) {
            this.interactionSource = interactionSource
            collectJob?.cancel()
            interactionFocused = false
            if (isAttached) collectFocus()
        }
        this.shape = shape
        this.color = color
        this.width = width
        this.gap = gap
        this.alwaysVisible = alwaysVisible
        invalidateDraw()
    }

    private fun collectFocus() {
        val source = interactionSource ?: return
        collectJob =
            coroutineScope.launch {
                val active = mutableListOf<FocusInteraction.Focus>()
                source.interactions.collect { interaction ->
                    when (interaction) {
                        is FocusInteraction.Focus -> active.add(interaction)
                        is FocusInteraction.Unfocus -> active.remove(interaction.focus)
                    }
                    val focused = active.isNotEmpty()
                    if (focused != interactionFocused) {
                        interactionFocused = focused
                        invalidateDraw()
                    }
                }
            }
    }

    override fun onFocusEvent(focusState: FocusState) {
        if (focusState.isFocused != eventFocused) {
            eventFocused = focusState.isFocused
            if (interactionSource == null) invalidateDraw()
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        val visible = alwaysVisible || if (interactionSource != null) interactionFocused else eventFocused
        if (!visible) return
        val strokePx = width.toPx()
        val inset = gap.toPx() + strokePx / 2f
        val ringSize = Size(size.width + inset * 2f, size.height + inset * 2f)
        val outline = shape.createOutline(ringSize, layoutDirection, this)
        translate(left = -inset, top = -inset) {
            drawOutline(outline, color, style = Stroke(strokePx))
        }
    }
}
