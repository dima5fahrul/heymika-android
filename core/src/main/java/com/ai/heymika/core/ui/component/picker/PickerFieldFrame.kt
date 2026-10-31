package com.ai.heymika.core.ui.component.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.component.input.FieldErrorCallout
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.staticFocusRing
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * The read-only field that opens a picker: label above, a 56dp white field with a 2dp border, a
 * leading icon, the value (or a muted placeholder) and a chevron, then a helper or error line.
 * The whole field is one Role.Button that TalkBack reads as "[label], [accessibilityValue]"; an
 * error line is attached to it as the error, a helper line is read after it (as
 * [supportingDescription] when given, for text whose separators read badly).
 */
@Composable
internal fun PickerFieldFrame(
    label: String,
    valueText: String?,
    placeholder: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    accessibilityValue: String,
    onClickLabel: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    supportingDescription: String? = null,
    previewFocused: Boolean = false,
) {
    val source = remember { MutableInteractionSource() }
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.input
    val description = stringResource(R.string.chrome_picker_field_description, label, accessibilityValue)
    val ring = if (previewFocused) Modifier.staticFocusRing(shape) else Modifier.comicFocusRing(source, shape)
    val borderColor =
        when {
            !enabled -> colors.disabledBorder
            isError -> colors.danger
            else -> colors.borderMuted
        }
    val iconTint = if (enabled) colors.textMuted else colors.disabledContent
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimens.spaceSm),
    ) {
        Text(
            text = label,
            style = HeyMikaTheme.typography.labelLg,
            color = colors.textPrimary,
            modifier = Modifier.clearAndSetSemantics {},
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = dimens.inputHeight)
                    .then(ring)
                    .clip(shape)
                    .background(if (enabled) colors.surface else colors.disabledContainer)
                    .border(dimens.borderControl, borderColor, shape)
                    .clickable(
                        interactionSource = source,
                        indication = ripple(),
                        enabled = enabled,
                        onClickLabel = onClickLabel,
                        role = Role.Button,
                        onClick = onClick,
                    ).semantics {
                        contentDescription = description
                        if (isError && supportingText != null) error(supportingText)
                    }.padding(horizontal = dimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(dimens.iconSize),
            )
            Text(
                text = valueText ?: placeholder,
                style = HeyMikaTheme.typography.bodyMd,
                color =
                    when {
                        !enabled -> colors.disabledContent
                        valueText == null -> colors.textMuted
                        else -> colors.textPrimary
                    },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(dimens.iconSizeLarge),
            )
        }
        if (supportingText != null) {
            if (isError) {
                FieldErrorCallout(text = supportingText, modifier = Modifier.clearAndSetSemantics {})
            } else {
                Text(
                    text = supportingText,
                    modifier =
                        if (supportingDescription != null) {
                            Modifier.clearAndSetSemantics { contentDescription = supportingDescription }
                        } else {
                            Modifier
                        },
                    style = HeyMikaTheme.typography.caption,
                    color = colors.textMuted,
                )
            }
        }
    }
}

/** A danger callout inside a picker dialog, announced politely when it appears. */
@Composable
internal fun PickerErrorText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.dangerBg, HeyMikaTheme.shapes.small)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
                .padding(horizontal = dimens.spaceSmPlus, vertical = dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = colors.danger,
            modifier = Modifier.size(dimens.iconSize),
        )
        Text(
            text = text,
            style = HeyMikaTheme.typography.bodySm,
            color = colors.danger,
        )
    }
}

/** Title block of a picker dialog: an optional step pill ("Mulai · 1/2") above a centred heading. */
@Composable
internal fun PickerDialogHeader(
    title: String,
    modifier: Modifier = Modifier,
    stepLabel: String? = null,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
    ) {
        if (stepLabel != null) {
            Text(
                text = stepLabel.uppercase(),
                style = HeyMikaTheme.typography.labelSm,
                color = colors.onInfo,
                modifier =
                    Modifier
                        .background(colors.info, HeyMikaTheme.shapes.pill)
                        .padding(horizontal = dimens.spaceSmPlus, vertical = dimens.spaceXxs),
            )
        }
        Text(
            text = title,
            style = HeyMikaTheme.typography.headlineSm,
            color = colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
    }
}
