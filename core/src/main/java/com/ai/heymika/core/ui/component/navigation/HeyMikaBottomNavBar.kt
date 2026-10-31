package com.ai.heymika.core.ui.component.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.component.foundation.SubtleDivider
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.staticFocusRing
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * - [Standard]: selected = filled primary icon and a heavy primaryText label.
 * - [Mika]: the Tanya Mika tab (paw icon); selected = the brand secondary (#914C00) instead of blue.
 */
enum class HeyMikaTabEmphasis { Standard, Mika }

/**
 * One bottom-nav tab. [id] is what [HeyMikaBottomNavBar] reports through `onSelect`;
 * [showBadgeDot] draws the orange "new" dot on the icon and adds "ada yang baru" for TalkBack.
 */
@Immutable
data class HeyMikaTabItem(
    val id: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val emphasis: HeyMikaTabEmphasis = HeyMikaTabEmphasis.Standard,
    val showBadgeDot: Boolean = false,
)

/** Ids of the [HeyMikaTabs]. */
object HeyMikaTabId {
    const val BERANDA = "beranda"
    const val MATERI = "materi"
    const val KUIS = "kuis"
    const val TANYA_MIKA = "tanya_mika"
    const val PROFIL = "profil"
}

/**
 * The v1 shell tabs, in the Beranda mockup order: Beranda / Materi / Kuis / Tanya Mika / Profil.
 * Badges are off; turn one on with `copy(showBadgeDot = true)`.
 */
val HeyMikaTabs: List<HeyMikaTabItem> =
    listOf(
        HeyMikaTabItem(
            id = HeyMikaTabId.BERANDA,
            labelRes = R.string.chrome_tab_beranda,
            icon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home,
        ),
        HeyMikaTabItem(
            id = HeyMikaTabId.MATERI,
            labelRes = R.string.chrome_tab_materi,
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        ),
        HeyMikaTabItem(
            id = HeyMikaTabId.KUIS,
            labelRes = R.string.chrome_tab_kuis,
            icon = Icons.AutoMirrored.Outlined.Assignment,
            selectedIcon = Icons.AutoMirrored.Filled.Assignment,
        ),
        HeyMikaTabItem(
            id = HeyMikaTabId.TANYA_MIKA,
            labelRes = R.string.chrome_tab_tanya_mika,
            icon = Icons.Outlined.Pets,
            selectedIcon = Icons.Filled.Pets,
            emphasis = HeyMikaTabEmphasis.Mika,
        ),
        HeyMikaTabItem(
            id = HeyMikaTabId.PROFIL,
            labelRes = R.string.chrome_tab_profil,
            icon = Icons.Outlined.Person,
            selectedIcon = Icons.Filled.Person,
        ),
    )

private const val MIN_VISIBLE_TABS = 2

/**
 * Stateless bottom navigation: renders exactly the [tabs] it is given, marks [selectedId] and
 * reports taps (including a re-tap of the selected tab) through [onSelect]. Nothing is drawn when
 * fewer than two tabs are passed. White surface with a top hairline, padded for the navigation
 * bar; each item is a Role.Tab in a selectable group with the comic focus ring.
 */
@Composable
fun HeyMikaBottomNavBar(
    tabs: List<HeyMikaTabItem>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.navigationBars,
) {
    if (tabs.size < MIN_VISIBLE_TABS) return
    val dimens = HeyMikaTheme.dimens
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(HeyMikaTheme.colors.surface)
                .pointerInput(Unit) {},
    ) {
        SubtleDivider()
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(windowInsets)
                    .height(dimens.bottomNavHeight)
                    .padding(horizontal = dimens.spaceXs)
                    .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                HeyMikaBottomNavItem(
                    tab = tab,
                    selected = tab.id == selectedId,
                    onClick = { onSelect(tab.id) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
internal fun HeyMikaBottomNavItem(
    tab: HeyMikaTabItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewFocused: Boolean = false,
) {
    val source = remember { MutableInteractionSource() }
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.button
    val label = stringResource(tab.labelRes)
    val badgeDescription = stringResource(R.string.chrome_with_badge, label)
    val (activeIconColor, activeTextColor) =
        when (tab.emphasis) {
            HeyMikaTabEmphasis.Standard -> colors.primary to colors.primaryText
            HeyMikaTabEmphasis.Mika -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.secondary
        }
    val ring = if (previewFocused) Modifier.staticFocusRing(shape) else Modifier.comicFocusRing(source, shape)
    val badgeSemantics =
        if (tab.showBadgeDot) Modifier.semantics { contentDescription = badgeDescription } else Modifier
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .padding(horizontal = dimens.spaceXxs, vertical = dimens.spaceSm)
                .then(ring)
                .clip(shape)
                .selectable(
                    selected = selected,
                    interactionSource = source,
                    indication = ripple(),
                    role = Role.Tab,
                    onClick = onClick,
                ).then(badgeSemantics),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.spaceXs, Alignment.CenterVertically),
    ) {
        Box {
            Icon(
                imageVector = if (selected) tab.selectedIcon else tab.icon,
                contentDescription = null,
                tint = if (selected) activeIconColor else colors.textMuted,
                modifier = Modifier.size(dimens.iconSize),
            )
            if (tab.showBadgeDot) {
                BadgeDot(
                    color = colors.accent,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = dimens.spaceXs, y = -dimens.spaceXs),
                )
            }
        }
        Text(
            text = label,
            style =
                HeyMikaTheme.typography.caption.copy(
                    fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                ),
            color = if (selected) activeTextColor else colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "Beranda selected, Mika badge", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun HeyMikaBottomNavBarPreview() {
    HeyMikaTheme {
        HeyMikaBottomNavBar(
            tabs =
                HeyMikaTabs.map {
                    if (it.id == HeyMikaTabId.TANYA_MIKA) it.copy(showBadgeDot = true) else it
                },
            selectedId = HeyMikaTabId.BERANDA,
            onSelect = {},
        )
    }
}

@Preview(name = "Each tab selected", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun HeyMikaBottomNavBarSelectionPreview() {
    HeyMikaTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HeyMikaTabs.forEach { tab ->
                HeyMikaBottomNavBar(tabs = HeyMikaTabs, selectedId = tab.id, onSelect = {})
            }
        }
    }
}

@Preview(name = "Focused item", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun HeyMikaBottomNavItemFocusPreview() {
    HeyMikaTheme {
        Row(
            modifier =
                Modifier
                    .background(HeyMikaTheme.colors.surface)
                    .height(HeyMikaTheme.dimens.bottomNavHeight)
                    .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeyMikaTabs.forEachIndexed { index, tab ->
                HeyMikaBottomNavItem(
                    tab = tab,
                    selected = index == 0,
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    previewFocused = index == 1,
                )
            }
        }
    }
}
