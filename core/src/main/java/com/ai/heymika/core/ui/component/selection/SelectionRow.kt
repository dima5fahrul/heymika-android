package com.ai.heymika.core.ui.component.selection

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.staticFocusRing
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * Frame shared by CheckboxRow and RadioButtonRow: the [selectionModifier] (toggleable or
 * selectable, built by the caller on [interactionSource]) makes the whole row one control at least
 * 48dp tall. A single-line row centres the 20dp [control] on its label; longer labels keep the
 * control on the first line.
 */
@Composable
internal fun SelectionRow(
    selectionModifier: Modifier,
    interactionSource: MutableInteractionSource,
    label: AnnotatedString,
    description: String?,
    enabled: Boolean,
    previewFocused: Boolean,
    modifier: Modifier = Modifier,
    control: @Composable () -> Unit,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val typography = HeyMikaTheme.typography
    val shape = HeyMikaTheme.shapes.button
    val ring =
        if (previewFocused) {
            Modifier.staticFocusRing(shape = shape)
        } else {
            Modifier.comicFocusRing(interactionSource, shape = shape)
        }
    Row(
        modifier =
            modifier
                .then(ring)
                .clip(shape)
                .then(selectionModifier)
                .heightIn(min = dimens.minTouchTarget)
                .padding(vertical = dimens.spaceSmPlus + dimens.spaceXxs),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
        verticalAlignment = Alignment.Top,
    ) {
        control()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceXxs),
        ) {
            Text(
                text = label,
                style = typography.bodySm,
                color = if (enabled) colors.textPrimary else colors.disabledContent,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = typography.caption,
                    color = colors.textMuted,
                )
            }
        }
    }
}
