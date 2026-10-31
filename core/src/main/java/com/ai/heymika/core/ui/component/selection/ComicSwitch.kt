package com.ai.heymika.core.ui.component.selection

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.staticFocusRing
import com.ai.heymika.core.ui.modifier.subtleSurface
import com.ai.heymika.core.ui.theme.HeyMikaMotion
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * The HeyMika switch: a 48x28dp pill track with a 2dp ink border and a 20dp thumb. On: primary
 * track, white thumb at the end. Off: white track, textMuted thumb at the start. Disabled uses the
 * disabled tokens. The thumb slides in 150ms and snaps under reduced motion.
 *
 * With [onCheckedChange] it is its own control (48dp target, Role.Switch, focus ring around the
 * track); name it with `Modifier.semantics { contentDescription = ... }`, or prefer [ToggleRow].
 * Pass null to draw the switch inside a parent that owns the toggle.
 */
@Composable
fun ComicSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    ComicSwitchImpl(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun ComicSwitchImpl(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    previewFocused: Boolean = false,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.pill
    val toggle =
        if (onCheckedChange != null) {
            Modifier
                .minimumInteractiveComponentSize()
                .toggleable(
                    value = checked,
                    interactionSource = source,
                    indication = ripple(bounded = false, radius = dimens.minTouchTarget / 2),
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                )
        } else {
            Modifier
        }
    val ring =
        when {
            previewFocused -> Modifier.staticFocusRing(shape = shape)
            onCheckedChange != null -> Modifier.comicFocusRing(source, shape = shape)
            else -> Modifier
        }
    Box(
        modifier = modifier.then(toggle),
        contentAlignment = Alignment.Center,
    ) {
        SwitchTrack(
            checked = checked,
            enabled = enabled,
            modifier = ring,
        )
    }
}

/**
 * A settings row: optional [icon], [title], optional [subtitle] and a [ComicSwitch]. The WHOLE row
 * is one Role.Switch toggle (at least 48dp tall) with a merged TalkBack label, a ripple and the
 * focus ring.
 *
 * With [showSurface] the row sits on a Subtle surface (1.5dp borderMuted, soft float), as the
 * stand-alone toggles in Profil; set it to false inside a card that already groups the rows.
 */
@Composable
fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    showSurface: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    ToggleRowImpl(
        title = title,
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        subtitle = subtitle,
        icon = icon,
        enabled = enabled,
        showSurface = showSurface,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun ToggleRowImpl(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    showSurface: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    previewFocused: Boolean = false,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.button
    val ring =
        if (previewFocused) {
            Modifier.staticFocusRing(shape = shape)
        } else {
            Modifier.comicFocusRing(source, shape = shape)
        }
    val surface =
        if (showSurface) {
            Modifier.subtleSurface(shape = shape, color = if (enabled) colors.surface else colors.disabledContainer)
        } else {
            Modifier
        }
    val padding =
        if (showSurface) PaddingValues(dimens.cardPaddingCompact) else PaddingValues(vertical = dimens.spaceSm)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(ring)
                .then(surface)
                .clip(shape)
                .toggleable(
                    value = checked,
                    interactionSource = source,
                    indication = ripple(),
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                ).heightIn(min = dimens.minTouchTarget)
                .padding(padding),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(dimens.iconSize),
                tint = if (enabled) colors.primaryText else colors.disabledContent,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceXxs),
        ) {
            Text(
                text = title,
                style = HeyMikaTheme.typography.labelLg,
                color = if (enabled) colors.textPrimary else colors.disabledContent,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = HeyMikaTheme.typography.caption,
                    color = colors.textMuted,
                )
            }
        }
        ComicSwitch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
        )
    }
}

@Composable
private fun SwitchTrack(
    checked: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.pill
    val inset = dimens.borderControl + dimens.spaceXxs
    val trackWidth = dimens.minTouchTarget
    val thumbSize = dimens.checkboxSize
    val thumbX by animateDpAsState(
        targetValue = if (checked) trackWidth - inset - thumbSize else inset,
        animationSpec = if (HeyMikaTheme.reducedMotion) snap() else tween(HeyMikaMotion.PRESS_MILLIS),
        label = "switchThumb",
    )
    val track =
        when {
            !enabled && checked -> colors.disabledBorder
            !enabled -> colors.disabledContainer
            checked -> colors.primary
            else -> colors.surface
        }
    val thumb =
        when {
            !enabled && checked -> colors.surface
            !enabled -> colors.disabledBorder
            checked -> colors.onPrimary
            else -> colors.textMuted
        }
    Box(
        modifier =
            modifier
                .size(width = trackWidth, height = thumbSize + inset * 2)
                .background(track, shape)
                .border(dimens.borderControl, if (enabled) colors.ink else colors.disabledBorder, shape),
    ) {
        Box(
            modifier =
                Modifier
                    .offset { IntOffset(thumbX.roundToPx(), inset.roundToPx()) }
                    .size(thumbSize)
                    .background(thumb, shape),
        )
    }
}

@Preview(name = "Switch states", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicSwitchPreview() {
    HeyMikaTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ComicSwitch(
                checked = true,
                onCheckedChange = {},
                modifier = Modifier.semantics { contentDescription = "Aktif" },
            )
            ComicSwitch(
                checked = false,
                onCheckedChange = {},
                modifier = Modifier.semantics { contentDescription = "Nonaktif" },
            )
            ComicSwitchImpl(
                checked = true,
                onCheckedChange = {},
                modifier = Modifier.semantics { contentDescription = "Fokus" },
                previewFocused = true,
            )
            ComicSwitch(
                checked = true,
                onCheckedChange = {},
                enabled = false,
                modifier = Modifier.semantics { contentDescription = "Tidak aktif, menyala" },
            )
            ComicSwitch(
                checked = false,
                onCheckedChange = {},
                enabled = false,
                modifier = Modifier.semantics { contentDescription = "Tidak aktif, mati" },
            )
        }
    }
}

@Preview(name = "Toggle rows", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ToggleRowPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ToggleRow(
                title = "Push Notifikasi Aplikasi & HP",
                checked = true,
                onCheckedChange = {},
                subtitle = "Info jadwal flashcard dan streak harian",
            )
            ToggleRowImpl(
                title = "Pengingat Sesi Belajar Harian",
                checked = false,
                onCheckedChange = {},
                subtitle = "Setiap hari pukul 19:00 WIB",
                icon = Icons.Outlined.NotificationsActive,
                previewFocused = true,
            )
            ToggleRow(
                title = "Hasil Analisis Kemajuan AI",
                checked = true,
                onCheckedChange = {},
                subtitle = "Butuh langganan Pro",
                enabled = false,
            )
            ToggleRow(
                title = "Jam Tenang (22:00 - 07:00)",
                checked = true,
                onCheckedChange = {},
                icon = Icons.Outlined.Bedtime,
                showSurface = false,
            )
        }
    }
}
