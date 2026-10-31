package com.ai.heymika.debug.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.ui.component.display.DisplayTone
import com.ai.heymika.core.ui.component.display.Pill
import com.ai.heymika.core.ui.component.display.PillSize
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.mascot.MikaAvatar
import com.ai.heymika.core.ui.component.navigation.BackPillButton
import com.ai.heymika.core.ui.component.navigation.FloatingInputBar
import com.ai.heymika.core.ui.component.navigation.FloatingInputBarStyle
import com.ai.heymika.core.ui.component.navigation.HeyMikaBottomNavBar
import com.ai.heymika.core.ui.component.navigation.HeyMikaLargeTopBar
import com.ai.heymika.core.ui.component.navigation.HeyMikaTabId
import com.ai.heymika.core.ui.component.navigation.HeyMikaTabs
import com.ai.heymika.core.ui.component.navigation.HeyMikaTopBar
import com.ai.heymika.core.ui.component.navigation.HeyMikaTopBarStyle
import com.ai.heymika.core.ui.component.navigation.StickyBottomActionBar
import com.ai.heymika.core.ui.component.navigation.TopBarIconButton
import com.ai.heymika.core.ui.theme.HeyMikaTheme

private val NoInsets = WindowInsets(0, 0, 0, 0)

@Composable
fun NavigationPage(
    title: String,
    onBack: () -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "HeyMikaTopBar") {
            SpecimenLabel("Akar tab: logo + aksi dengan badge")
            HeyMikaTopBar(
                title = null,
                windowInsets = NoInsets,
                navigationContent = { GalleryLogo() },
                actions = {
                    TopBarIconButton(icon = Icons.Outlined.Search, contentDescription = "Cari", onClick = {})
                    TopBarIconButton(
                        icon = Icons.Outlined.Notifications,
                        contentDescription = "Notifikasi",
                        onClick = {},
                        showBadgeDot = true,
                    )
                },
            )
            SpecimenLabel("Halaman dorong: kembali + judul")
            HeyMikaTopBar(
                title = "Detail Workspace",
                onBack = {},
                windowInsets = NoInsets,
                actions = {
                    TopBarIconButton(
                        icon = Icons.Outlined.MoreVert,
                        contentDescription = "Lainnya",
                        onClick = {},
                    )
                },
            )
            SpecimenLabel("Pill kembali, gaya transparan")
            HeyMikaTopBar(
                title = "Lupa Password",
                onBack = {},
                backLabel = "Kembali",
                style = HeyMikaTopBarStyle.Transparent,
                windowInsets = NoInsets,
            )
        }
        GalleryGroup(title = "HeyMikaLargeTopBar") {
            HeyMikaLargeTopBar(
                title = "Materimu",
                subtitle = "4 workspace aktif · 2 diarsipkan",
                eyebrow = {
                    Pill(
                        text = "Semester 2",
                        tone = DisplayTone.Cyan,
                        size = PillSize.Small,
                        uppercase = true,
                    )
                },
                actions = { TopBarIconButton(icon = Icons.Outlined.Search, contentDescription = "Cari", onClick = {}) },
            )
        }
        GalleryGroup(title = "BackPillButton") {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
                BackPillButton(label = "Kembali", onClick = {})
                BackPillButton(label = "Nonaktif", onClick = {}, enabled = false)
            }
        }
        GalleryGroup(title = "HeyMikaBottomNavBar", note = "Beranda / Materi / Kuis / Tanya Mika / Profil") {
            var selected by remember { mutableStateOf(HeyMikaTabId.BERANDA) }
            HeyMikaBottomNavBar(
                tabs = HeyMikaTabs,
                selectedId = selected,
                onSelect = { selected = it },
                windowInsets = NoInsets,
            )
            SpecimenLabel("Tanya Mika terpilih, badge di Kuis")
            HeyMikaBottomNavBar(
                tabs = HeyMikaTabs.map { if (it.id == HeyMikaTabId.KUIS) it.copy(showBadgeDot = true) else it },
                selectedId = HeyMikaTabId.TANYA_MIKA,
                onSelect = {},
                windowInsets = NoInsets,
            )
        }
        GalleryGroup(title = "StickyBottomActionBar") {
            StickyBottomActionBar(
                windowInsets = NoInsets,
                caption = {
                    Text(
                        text = "Langkah 2 dari 3",
                        style = HeyMikaTheme.typography.caption,
                        color = HeyMikaTheme.colors.textMuted,
                    )
                },
            ) {
                ComicButton(text = "Lanjutkan", onClick = {}, fullWidth = true)
            }
        }
        GalleryGroup(title = "FloatingInputBar", note = "Docked (chat) dan Floating") {
            var docked by remember { mutableStateOf("") }
            FloatingInputBar(
                value = docked,
                onValueChange = { docked = it },
                onSend = { docked = "" },
                windowInsets = NoInsets,
                leading = { MikaAvatar(pose = MikaPose.HAPPY, size = dimens.avatarXs) },
            )
            var floating by remember { mutableStateOf("Kenapa langit biru?") }
            FloatingInputBar(
                value = floating,
                onValueChange = { floating = it },
                onSend = {},
                style = FloatingInputBarStyle.Floating,
                windowInsets = NoInsets,
                sending = true,
            )
        }
    }
}

/** Placeholder brand lockup (avatar + wordmark) until the real logo asset lands. */
@Composable
private fun GalleryLogo() {
    val dimens = HeyMikaTheme.dimens
    Row(
        modifier = Modifier.padding(start = dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs),
    ) {
        MikaAvatar(pose = MikaPose.WAVING, size = dimens.avatarXs)
        Text(text = "HeyMika", style = HeyMikaTheme.typography.headlineSm, color = HeyMikaTheme.colors.primaryText)
    }
}
