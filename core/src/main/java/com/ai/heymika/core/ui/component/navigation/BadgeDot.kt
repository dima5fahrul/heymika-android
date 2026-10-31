package com.ai.heymika.core.ui.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * An 8dp dot in a 2dp surface ring, drawn over an icon's top-end corner. Decorative: the owner adds
 * "ada yang baru" to its own content description.
 */
@Composable
internal fun BadgeDot(
    color: Color,
    modifier: Modifier = Modifier,
) {
    val dimens = HeyMikaTheme.dimens
    Box(
        modifier =
            modifier
                .size(dimens.spaceSm + dimens.spaceXs)
                .background(HeyMikaTheme.colors.surface, CircleShape)
                .padding(dimens.spaceXxs)
                .background(color, CircleShape),
    )
}
