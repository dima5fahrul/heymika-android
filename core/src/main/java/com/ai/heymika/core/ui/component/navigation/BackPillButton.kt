package com.ai.heymika.core.ui.component.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.staticFocusRing
import com.ai.heymika.core.ui.modifier.subtleSurface
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * The "← Materimu" breadcrumb pill: a white subtle-float pill with a primary back arrow and the
 * parent screen's name. TalkBack hears "Kembali ke [label]". The pill is about 32dp tall and sits
 * in a 48dp touch target.
 */
@Composable
fun BackPillButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    BackPillButtonImpl(
        label = label,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun BackPillButtonImpl(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    previewFocused: Boolean = false,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.pill
    val description = stringResource(R.string.chrome_back_to, label)
    val ring = if (previewFocused) Modifier.staticFocusRing(shape) else Modifier.comicFocusRing(source, shape)
    val contentColor = if (enabled) colors.textPrimary else colors.disabledContent
    Row(
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .then(ring)
                .subtleSurface(shape = shape)
                .clip(shape)
                .clickable(
                    interactionSource = source,
                    indication = ripple(),
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                ).semantics { contentDescription = description }
                .padding(horizontal = dimens.spaceSmPlus, vertical = dimens.spaceXs + dimens.spaceXxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs + dimens.spaceXxs),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = if (enabled) colors.primary else colors.disabledContent,
            modifier = Modifier.size(dimens.iconSize),
        )
        Text(
            text = label,
            style = HeyMikaTheme.typography.labelMd,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@Preview(name = "Back pill states", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun BackPillButtonPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BackPillButton(label = "Materimu", onClick = {})
            BackPillButtonImpl(label = "Fokus (keyboard)", onClick = {}, previewFocused = true)
            BackPillButton(label = "Tidak aktif", onClick = {}, enabled = false)
            BackPillButton(label = "Struktur Data & Algoritma Lanjutan Semester 3", onClick = {})
        }
    }
}
