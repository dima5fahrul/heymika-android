package com.ai.heymika.debug.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ai.heymika.R
import com.ai.heymika.core.domain.model.AppError
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.domain.model.Quota
import com.ai.heymika.core.domain.model.QuotaKind
import com.ai.heymika.core.domain.model.QuotaPeriod
import com.ai.heymika.core.ui.component.display.DisplayTone
import com.ai.heymika.core.ui.component.display.FilterChipItem
import com.ai.heymika.core.ui.component.display.FilterChipRow
import com.ai.heymika.core.ui.component.display.InfoNoteCard
import com.ai.heymika.core.ui.component.display.Pill
import com.ai.heymika.core.ui.component.display.PillDot
import com.ai.heymika.core.ui.component.display.PillSize
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.component.input.SearchField
import com.ai.heymika.core.ui.component.navigation.HeyMikaTopBar
import com.ai.heymika.core.ui.component.state.MikaEmptyState
import com.ai.heymika.core.ui.component.state.MikaErrorState
import com.ai.heymika.core.ui.component.state.MikaLoadingState
import com.ai.heymika.core.ui.component.state.MikaStateVariant
import com.ai.heymika.core.ui.component.state.OfflineBanner
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/** One entry of the system-state catalog; [error] is null for the loading and empty states. */
private enum class CatalogState(
    val id: String,
    val label: String,
    val error: AppError?,
) {
    NOT_FOUND("not_found", "404 Tidak Ditemukan", AppError.NotFound),
    FORBIDDEN("forbidden", "403 Terbatas", AppError.Forbidden),
    SESSION_EXPIRED("session_expired", "Sesi Habis", AppError.SessionExpired),
    OFFLINE("offline", "Offline", AppError.Offline),
    MAINTENANCE("maintenance", "Pemeliharaan", AppError.Maintenance(etaMinutes = 15)),
    QUOTA(
        "quota",
        "Batas Kuota",
        AppError.QuotaExceeded(Quota(QuotaKind.AI_CHAT, used = 20, limit = 20, QuotaPeriod.DAILY, resetAt = null)),
    ),
    SERVER("server", "Server 502", AppError.Server(code = 502)),
    VALIDATION("validation", "Validasi", AppError.Validation(field = "email", code = "INVALID")),
    UNKNOWN("unknown", "Tak dikenal", AppError.Unknown(cause = null)),
    LOADING("loading", "Memuat", null),
    EMPTY("empty", "Kosong", null),
    ;

    companion object {
        fun fromId(id: String?): CatalogState = entries.firstOrNull { it.id == id } ?: NOT_FOUND
    }
}

@Composable
fun StatesPage(
    title: String,
    onBack: () -> Unit,
    initialState: String?,
) {
    val dimens = HeyMikaTheme.dimens
    var selected by rememberSaveable(initialState) { mutableStateOf(CatalogState.fromId(initialState)) }
    Column(modifier = Modifier.fillMaxSize()) {
        HeyMikaTopBar(
            title = title,
            onBack = onBack,
            backLabel = stringResource(R.string.gallery_back),
            actions = {
                Pill(
                    text = "Normal",
                    tone = DisplayTone.Success,
                    size = PillSize.Small,
                    dot = PillDot.Pulsing,
                    uppercase = true,
                    modifier = Modifier.padding(end = dimens.spaceSm),
                )
            },
        )
        FilterChipRow(
            items = CatalogState.entries.map { FilterChipItem(key = it, label = it.label) },
            selectedKey = selected,
            onSelect = { selected = it },
            modifier = Modifier.padding(vertical = dimens.spaceSm),
        )
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) {
            FullScreenState(state = selected)
        }
    }
}

@Composable
private fun FullScreenState(state: CatalogState) {
    val error = state.error
    when {
        error != null -> {
            MikaErrorState(
                error = error,
                onPrimary = {},
                onSecondary = {},
                onReportBug = {},
                searchContent = if (error == AppError.NotFound) ({ NotFoundSearch() }) else null,
            )
        }

        state == CatalogState.LOADING -> {
            MikaLoadingState(message = "Mika sedang menyiapkan ringkasanmu…")
        }

        else -> {
            MikaEmptyState(
                title = "Belum ada materi",
                body = "Unggah PDF atau tempel teks, nanti Mika ubah jadi catatan dan kuis.",
                speech = "Rak bukumu masih kosong, meong!",
                pose = MikaPose.SLEEPING,
                action = { ComicButton(text = "Tambah Materi", onClick = {}, variant = ComicButtonVariant.Secondary) },
            )
        }
    }
}

@Composable
private fun NotFoundSearch() {
    var query by rememberSaveable { mutableStateOf("") }
    Row(
        horizontalArrangement = Arrangement.spacedBy(HeyMikaTheme.dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchField(
            query = query,
            onQueryChange = { query = it },
            placeholder = stringResource(com.ai.heymika.core.R.string.state_not_found_search_placeholder),
            modifier = Modifier.weight(1f),
        )
        ComicButton(
            text = stringResource(com.ai.heymika.core.R.string.state_not_found_search_action),
            onClick = {},
            size = ComicButtonSize.Medium,
        )
    }
}

@Composable
fun StatesInlinePage(
    title: String,
    onBack: () -> Unit,
    initialState: String?,
) {
    val errorStates = CatalogState.entries.filter { it.error != null }
    var selected by rememberSaveable(initialState) {
        mutableStateOf(CatalogState.fromId(initialState).takeIf { it.error != null } ?: CatalogState.NOT_FOUND)
    }
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "OfflineBanner", note = "Di aplikasi dipasang oleh HeyMikaRootFrame") {
            OfflineBanner(visible = true, onRetry = {})
        }
        GalleryGroup(title = "MikaErrorState · Inline", note = "Pilih status; satu kartu tampil sekaligus") {
            FilterChipRow(
                items = errorStates.map { FilterChipItem(key = it, label = it.label) },
                selectedKey = selected,
                onSelect = { selected = it },
                modifier = Modifier.fullBleed(HeyMikaTheme.dimens.screenMargin),
            )
            val error = selected.error ?: AppError.NotFound
            MikaErrorState(
                error = error,
                onPrimary = {},
                onSecondary = {},
                variant = MikaStateVariant.Inline,
            )
        }
        GalleryGroup(title = "Loading & empty · Inline") {
            MikaLoadingState(variant = MikaStateVariant.Inline)
            MikaEmptyState(
                title = "Belum ada flashcard",
                body = "Buat dari catatan ringkasan atau tambah manual.",
                pose = MikaPose.THINKING,
                variant = MikaStateVariant.Inline,
                action = {
                    ComicButton(
                        text = "Buat Flashcard",
                        onClick = {},
                        variant = ComicButtonVariant.Outline,
                        size = ComicButtonSize.Medium,
                    )
                },
            )
        }
        InfoNoteCard(
            title = "Prinsip Penanganan Error HeyMika",
            body =
                "1. Tanpa menghakimi: bahasa ramah, tidak menyalahkan pengguna. " +
                    "2. Jalur keluar jelas: setiap error punya aksi. " +
                    "3. Data aman: draf dan progres tersimpan lokal saat offline.",
            icon = Icons.Outlined.Verified,
        )
    }
}
