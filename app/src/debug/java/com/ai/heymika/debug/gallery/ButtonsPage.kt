package com.ai.heymika.debug.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ai.heymika.R
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.component.foundation.ComicIconButton
import com.ai.heymika.core.ui.theme.HeyMikaTheme

private val variantNotes =
    mapOf(
        ComicButtonVariant.Primary to "Satu aksi utama per area",
        ComicButtonVariant.Secondary to "Aksen oranye: Tanya Mika, Tambah Materi, mulai kuis",
        ComicButtonVariant.Outline to "Alternatif netral yang tegas",
        ComicButtonVariant.Tonal to "Aksi kedua yang lembut: Buka Riwayat, Lapor Bug, login Google",
        ComicButtonVariant.Ghost to "Rendah: Lupa password?, Lihat semua",
        ComicButtonVariant.Danger to "Hanya konfirmasi destruktif",
    )

/** Primary, Secondary and Outline in every size and state. */
@Composable
fun ButtonsPage(
    title: String,
    onBack: () -> Unit,
) {
    GalleryPage(title = title, onBack = onBack) {
        SpecimenLabel(stringResource(R.string.gallery_focus_hint))
        listOf(ComicButtonVariant.Primary, ComicButtonVariant.Secondary, ComicButtonVariant.Outline).forEach {
            VariantGroup(it)
        }
    }
}

/** Tonal, Ghost and Danger, the slot overload and ComicIconButton. */
@Composable
fun ButtonsMorePage(
    title: String,
    onBack: () -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    GalleryPage(title = title, onBack = onBack) {
        VariantGroup(ComicButtonVariant.Tonal)
        VariantGroup(ComicButtonVariant.Ghost)
        VariantGroup(ComicButtonVariant.Danger)
        GalleryGroup(title = "Slot & penghitung", note = "ComicButton(content = ...) untuk isi khusus") {
            var taps by remember { mutableIntStateOf(0) }
            ComicButton(onClick = { taps++ }, variant = ComicButtonVariant.Outline, fullWidth = true) {
                Icon(
                    imageVector = Icons.Filled.Pets,
                    contentDescription = null,
                    tint = HeyMikaTheme.colors.accent,
                    modifier = Modifier.size(dimens.iconSize),
                )
                Text(text = "Ditekan $taps kali", style = HeyMikaTheme.typography.button)
            }
        }
        GalleryGroup(title = "ComicIconButton", note = "Wajah persegi, target sentuh >= 48dp") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
            ) {
                ComicIconButton(icon = Icons.Filled.Notifications, contentDescription = "Notifikasi", onClick = {})
                ComicIconButton(
                    icon = Icons.Filled.Add,
                    contentDescription = "Tambah",
                    onClick = {},
                    variant = ComicButtonVariant.Primary,
                )
                ComicIconButton(
                    icon = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Kirim",
                    onClick = {},
                    variant = ComicButtonVariant.Secondary,
                    shape = HeyMikaTheme.shapes.button,
                )
                ComicIconButton(
                    icon = Icons.Filled.Close,
                    contentDescription = "Tutup",
                    onClick = {},
                    variant = ComicButtonVariant.Outline,
                    size = ComicButtonSize.Small,
                )
                ComicIconButton(
                    icon = Icons.Filled.Delete,
                    contentDescription = "Hapus",
                    onClick = {},
                    variant = ComicButtonVariant.Danger,
                )
                ComicIconButton(
                    icon = Icons.Filled.Add,
                    contentDescription = "Tambah (nonaktif)",
                    onClick = {},
                    variant = ComicButtonVariant.Primary,
                    enabled = false,
                )
            }
        }
    }
}

@Composable
private fun VariantGroup(variant: ComicButtonVariant) {
    val dimens = HeyMikaTheme.dimens
    val danger = variant == ComicButtonVariant.Danger
    GalleryGroup(title = variant.name, note = variantNotes[variant]) {
        ComicButton(
            text = if (danger) "Hapus materi" else "Mulai belajar",
            onClick = {},
            variant = variant,
            fullWidth = true,
            leadingIcon = if (danger) Icons.Filled.Delete else Icons.Filled.Star,
            trailingIcon = if (danger) null else Icons.AutoMirrored.Filled.ArrowForward,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus)) {
            ComicButton(text = "Medium", onClick = {}, variant = variant, size = ComicButtonSize.Medium)
            ComicButton(text = "Small", onClick = {}, variant = variant, size = ComicButtonSize.Small)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus)) {
            ComicButton(
                text = "Nonaktif",
                onClick = {},
                variant = variant,
                size = ComicButtonSize.Medium,
                enabled = false,
            )
            ComicButton(
                text = "Simpan",
                onClick = {},
                variant = variant,
                size = ComicButtonSize.Medium,
                loading = true,
                loadingText = "Menyimpan",
            )
        }
    }
}
