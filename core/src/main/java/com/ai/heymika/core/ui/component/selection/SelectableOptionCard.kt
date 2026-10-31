package com.ai.heymika.core.ui.component.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.comicSurface
import com.ai.heymika.core.ui.modifier.staticFocusRing
import com.ai.heymika.core.ui.modifier.subtleSurface
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * - [Tile]: the onboarding grid card. Icon tile and indicator on top, then title and description.
 * - [Row]: the list card of Profil and Konfigurasi Kuis. Indicator, optional icon tile, then the
 *   title (with the badge) over the description.
 */
enum class OptionCardLayout { Tile, Row }

/**
 * - [Single]: one answer per group. Role.RadioButton and a radio indicator; wrap the group in
 *   `Modifier.selectableGroup()`.
 * - [Multiple]: any number of answers. Role.Checkbox and a checkbox indicator.
 */
enum class OptionSelectionMode { Single, Multiple }

/**
 * A selectable option card. Unselected it is a Subtle card (1.5dp borderMuted, soft float);
 * selected it becomes Strict (3dp ink border, 6dp orange offset shadow) and its icon tile fills
 * primary. The whole card is one control with a merged TalkBack label, a ripple and the focus ring.
 *
 * The leading tile shows [emoji] if given, otherwise [icon]; both are decorative. [badge] is an
 * uppercase orange pill ("Rekomendasi"). [isError] gives an unselected card a 2dp danger border
 * (e.g. a required group left empty); show the message with ComicFieldSupportingText under the
 * group.
 *
 * The selected shadow sits outside the bounds: keep at least 8dp between cards and at their end
 * and bottom. For equal heights in a grid row, give the Row `Modifier.height(IntrinsicSize.Min)`
 * and each card `Modifier.fillMaxHeight()`.
 */
@Composable
fun SelectableOptionCard(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    emoji: String? = null,
    badge: String? = null,
    layout: OptionCardLayout = OptionCardLayout.Tile,
    selectionMode: OptionSelectionMode = OptionSelectionMode.Single,
    enabled: Boolean = true,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    SelectableOptionCardImpl(
        selected = selected,
        onClick = onClick,
        title = title,
        modifier = modifier,
        description = description,
        icon = icon,
        emoji = emoji,
        badge = badge,
        layout = layout,
        selectionMode = selectionMode,
        enabled = enabled,
        isError = isError,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun SelectableOptionCardImpl(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    emoji: String? = null,
    badge: String? = null,
    layout: OptionCardLayout = OptionCardLayout.Tile,
    selectionMode: OptionSelectionMode = OptionSelectionMode.Single,
    enabled: Boolean = true,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    previewFocused: Boolean = false,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.button
    val surface =
        when {
            !enabled -> {
                Modifier.subtleSurface(
                    shape = shape,
                    color = colors.disabledContainer,
                    borderColor = colors.disabledBorder,
                    floatColor = Color.Transparent,
                )
            }

            selected -> {
                Modifier.comicSurface(shape = shape)
            }

            isError -> {
                Modifier.subtleSurface(
                    shape = shape,
                    borderColor = colors.danger,
                    borderWidth = dimens.borderControl,
                )
            }

            else -> {
                Modifier.subtleSurface(shape = shape)
            }
        }
    val selection =
        when (selectionMode) {
            OptionSelectionMode.Single -> {
                Modifier.selectable(
                    selected = selected,
                    interactionSource = source,
                    indication = ripple(),
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
            }

            OptionSelectionMode.Multiple -> {
                Modifier.toggleable(
                    value = selected,
                    interactionSource = source,
                    indication = ripple(),
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = { onClick() },
                )
            }
        }
    val ring =
        if (previewFocused) {
            Modifier.staticFocusRing(shape = shape)
        } else {
            Modifier.comicFocusRing(source, shape = shape)
        }
    val indicator: @Composable () -> Unit = {
        OptionIndicator(
            selected = selected,
            selectionMode = selectionMode,
            enabled = enabled,
            isError = isError,
        )
    }
    Box(
        modifier =
            modifier
                .then(ring)
                .then(surface)
                .clip(shape)
                .then(selection)
                .padding(dimens.cardPaddingCompact),
    ) {
        when (layout) {
            OptionCardLayout.Tile -> {
                Column(verticalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OptionLeadingTile(
                            icon = icon,
                            emoji = emoji,
                            selected = selected,
                            enabled = enabled,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (badge != null) OptionBadge(badge)
                        indicator()
                    }
                    OptionTexts(
                        title = title,
                        description = description,
                        enabled = enabled,
                    )
                }
            }

            OptionCardLayout.Row -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
                    verticalAlignment = Alignment.Top,
                ) {
                    indicator()
                    OptionLeadingTile(
                        icon = icon,
                        emoji = emoji,
                        selected = selected,
                        enabled = enabled,
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(dimens.spaceXxs),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = title,
                                modifier = Modifier.weight(1f, fill = false),
                                style = HeyMikaTheme.typography.labelLg,
                                color = if (enabled) colors.textPrimary else colors.disabledContent,
                            )
                            if (badge != null) OptionBadge(badge)
                        }
                        if (description != null) {
                            Text(
                                text = description,
                                style = HeyMikaTheme.typography.bodySm,
                                color = colors.textMuted,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionIndicator(
    selected: Boolean,
    selectionMode: OptionSelectionMode,
    enabled: Boolean,
    isError: Boolean,
) {
    when (selectionMode) {
        OptionSelectionMode.Single -> {
            ComicRadioButton(
                selected = selected,
                onClick = null,
                enabled = enabled,
                isError = isError && !selected,
            )
        }

        OptionSelectionMode.Multiple -> {
            ComicCheckbox(
                checked = selected,
                onCheckedChange = null,
                enabled = enabled,
                isError = isError && !selected,
            )
        }
    }
}

@Composable
private fun OptionLeadingTile(
    icon: ImageVector?,
    emoji: String?,
    selected: Boolean,
    enabled: Boolean,
) {
    if (icon == null && emoji == null) return
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val container =
        when {
            !enabled -> colors.disabledBorder
            selected -> colors.primary
            else -> HeyMikaTheme.tones.blue.container
        }
    val content =
        when {
            !enabled -> colors.disabledContent
            selected -> colors.onPrimary
            else -> colors.primaryText
        }
    Box(
        modifier =
            Modifier
                .size(dimens.avatarSm)
                .background(container, HeyMikaTheme.shapes.small)
                .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        when {
            emoji != null -> {
                Text(text = emoji, style = HeyMikaTheme.typography.headlineSm)
            }

            icon != null -> {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(dimens.iconSize),
                    tint = content,
                )
            }
        }
    }
}

@Composable
private fun OptionBadge(text: String) {
    Text(
        text = text.uppercase(),
        modifier =
            Modifier
                .background(HeyMikaTheme.colors.accent, HeyMikaTheme.shapes.pill)
                .padding(horizontal = HeyMikaTheme.dimens.spaceSm, vertical = HeyMikaTheme.dimens.spaceXxs),
        style = HeyMikaTheme.typography.labelSm,
        color = HeyMikaTheme.colors.onAccent,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun OptionTexts(
    title: String,
    description: String?,
    enabled: Boolean,
) {
    val colors = HeyMikaTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(HeyMikaTheme.dimens.spaceXxs)) {
        Text(
            text = title,
            style = HeyMikaTheme.typography.labelLg,
            color = if (enabled) colors.textPrimary else colors.disabledContent,
        )
        if (description != null) {
            Text(
                text = description,
                style = HeyMikaTheme.typography.caption,
                color = colors.textMuted,
            )
        }
    }
}

@Preview(name = "Onboarding tiles", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun SelectableOptionCardTilePreview() {
    HeyMikaTheme {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SelectableOptionCard(
                    selected = true,
                    onClick = {},
                    title = "Mahasiswa",
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    description = "Kuliah, Tugas, Skripsi",
                    icon = Icons.Outlined.AccountBalance,
                )
                SelectableOptionCard(
                    selected = false,
                    onClick = {},
                    title = "Siswa SMA / SMK",
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    description = "SNBT, UTBK, Ujian",
                    icon = Icons.Outlined.Backpack,
                )
            }
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SelectableOptionCardImpl(
                    selected = false,
                    onClick = {},
                    title = "Pembelajar Mandiri",
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    description = "Otodidak, Sertifikasi",
                    icon = Icons.Outlined.RocketLaunch,
                    previewFocused = true,
                )
                SelectableOptionCard(
                    selected = false,
                    onClick = {},
                    title = "Profesional Karier",
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    description = "Upskilling & Kerja",
                    icon = Icons.Outlined.WorkOutline,
                    isError = true,
                )
            }
        }
    }
}

@Preview(name = "Rows and states", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun SelectableOptionCardRowPreview() {
    HeyMikaTheme {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SelectableOptionCard(
                selected = false,
                onClick = {},
                title = "15 Menit / Hari",
                modifier = Modifier.fillMaxWidth(),
                description = "Santai & Ringan (Review kilat 5 kartu)",
                emoji = "🌱",
                layout = OptionCardLayout.Row,
            )
            SelectableOptionCard(
                selected = true,
                onClick = {},
                title = "30 Menit / Hari",
                modifier = Modifier.fillMaxWidth(),
                description = "Konsisten & Ideal untuk pemahaman mendalam",
                emoji = "⚡",
                badge = "Rekomendasi",
                layout = OptionCardLayout.Row,
            )
            SelectableOptionCard(
                selected = false,
                onClick = {},
                title = "Mode Ujian (Simulasi Realistis)",
                modifier = Modifier.fillMaxWidth(),
                description = "Timer hitung mundur ketat tanpa hint. Hadir setelah kuis pertamamu.",
                icon = Icons.Outlined.Timer,
                layout = OptionCardLayout.Row,
                enabled = false,
            )
        }
    }
}

@Preview(name = "Multiple selection", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun SelectableOptionCardMultiplePreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SelectableOptionCard(
                selected = true,
                onClick = {},
                title = "Modul 04 - Binary Search Tree & AVL",
                modifier = Modifier.fillMaxWidth(),
                description = "PDF • 42 halaman • Index Terproses 100%",
                layout = OptionCardLayout.Row,
                selectionMode = OptionSelectionMode.Multiple,
            )
            SelectableOptionCardImpl(
                selected = false,
                onClick = {},
                title = "Catatan Kuliah Pertemuan 7 - Graph Traversal",
                modifier = Modifier.fillMaxWidth(),
                description = "Ringkasan AI • 8 halaman • Siap Diuji",
                layout = OptionCardLayout.Row,
                selectionMode = OptionSelectionMode.Multiple,
                previewFocused = true,
            )
            SelectableOptionCard(
                selected = false,
                onClick = {},
                title = "Slide Dosen - Dynamic Programming",
                modifier = Modifier.fillMaxWidth(),
                description = "PPTX • 28 slide • Belum terindeks penuh",
                layout = OptionCardLayout.Row,
                selectionMode = OptionSelectionMode.Multiple,
                isError = true,
            )
        }
    }
}
