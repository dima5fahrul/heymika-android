package com.ai.heymika.core.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Corner tokens. Read through [HeyMikaTheme.shapes]. The Tailwind overrides in the mockup HTML
 * (lg=8, xl=12) are ignored in favour of the DESIGN.md prose radii.
 */
@Immutable
class HeyMikaShapes(
    /** 6dp: subject tags, code badges. */
    val tag: CornerBasedShape = RoundedCornerShape(DefaultHeyMikaDimens.radiusTag),
    /** 8dp: small chips, inline buttons inside cards. */
    val small: CornerBasedShape = RoundedCornerShape(DefaultHeyMikaDimens.radiusSmall),
    /** 14dp: text fields and pickers. */
    val input: CornerBasedShape = RoundedCornerShape(DefaultHeyMikaDimens.radiusControl),
    /** 14dp: ComicButton, ComicIconButton (square variant), selectable rows. */
    val button: CornerBasedShape = RoundedCornerShape(DefaultHeyMikaDimens.radiusControl),
    /** 16dp: list and feed cards, nested panels (DESIGN lg). */
    val card: CornerBasedShape = RoundedCornerShape(DefaultHeyMikaDimens.radiusCard),
    /** 24dp: hero cards inside a screen. */
    val cardLarge: CornerBasedShape = RoundedCornerShape(DefaultHeyMikaDimens.radiusCardLarge),
    /** 28dp: auth and hero shells (DESIGN 28-32dp). */
    val shell: CornerBasedShape = RoundedCornerShape(DefaultHeyMikaDimens.radiusShell),
    /** 28dp: ComicDialog. */
    val dialog: CornerBasedShape = RoundedCornerShape(DefaultHeyMikaDimens.radiusShell),
    /** 28dp top corners only: ComicBottomSheet. */
    val sheet: CornerBasedShape =
        RoundedCornerShape(
            topStart = DefaultHeyMikaDimens.radiusShell,
            topEnd = DefaultHeyMikaDimens.radiusShell,
        ),
    /** 16dp body of a speech bubble (the tail is added by the bubble's own shape). */
    val speechBubble: CornerBasedShape = RoundedCornerShape(DefaultHeyMikaDimens.radiusCard),
    /** Fully rounded: pills, chips, avatars, status badges. */
    val pill: CornerBasedShape = CircleShape,
)

internal val DefaultHeyMikaShapes = HeyMikaShapes()

/** HeyMika radii mapped onto the Material 3 shape slots used by stock components. */
val HeyMikaMaterialShapes: Shapes =
    Shapes(
        extraSmall = DefaultHeyMikaShapes.tag,
        small = DefaultHeyMikaShapes.small,
        medium = DefaultHeyMikaShapes.card,
        large = DefaultHeyMikaShapes.cardLarge,
        extraLarge = DefaultHeyMikaShapes.shell,
    )

val LocalHeyMikaShapes = staticCompositionLocalOf { DefaultHeyMikaShapes }
