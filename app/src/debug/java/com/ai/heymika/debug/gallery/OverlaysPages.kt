package com.ai.heymika.debug.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.ComicButtonDefaults
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.component.foundation.SubtleDivider
import com.ai.heymika.core.ui.component.mascot.MikaAvatar
import com.ai.heymika.core.ui.component.overlay.BasicComicDialog
import com.ai.heymika.core.ui.component.overlay.ComicBottomSheet
import com.ai.heymika.core.ui.component.overlay.ComicDialog
import com.ai.heymika.core.ui.component.overlay.HeyMikaSnackbarTone
import com.ai.heymika.core.ui.component.overlay.LocalHeyMikaSnackbarHostState
import com.ai.heymika.core.ui.component.overlay.showHeyMikaSnackbar
import com.ai.heymika.core.ui.component.picker.ComicDatePicker
import com.ai.heymika.core.ui.component.picker.ComicTimePicker
import com.ai.heymika.core.ui.component.picker.DatePickerField
import com.ai.heymika.core.ui.component.picker.TimeRangeField
import com.ai.heymika.core.ui.component.selection.RadioButtonRow
import com.ai.heymika.core.ui.component.timer.CountdownTimerPill
import com.ai.heymika.core.ui.component.timer.ResendCooldownButton
import com.ai.heymika.core.ui.component.timer.TimerDefaults
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.core.utils.time.TimeMath
import com.ai.heymika.core.utils.time.TimeRange
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

private enum class Overlay { Dialog, DialogDanger, DialogLoading, DialogBasic, Sheet }

private fun overlayFor(open: String?): Overlay? =
    when (open) {
        "dialog" -> Overlay.Dialog
        "dialog_danger" -> Overlay.DialogDanger
        "dialog_loading" -> Overlay.DialogLoading
        "dialog_basic" -> Overlay.DialogBasic
        "sheet" -> Overlay.Sheet
        else -> null
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlaysPage(
    title: String,
    onBack: () -> Unit,
    open: String?,
) {
    val dimens = HeyMikaTheme.dimens
    var overlay by rememberSaveable(open) { mutableStateOf(overlayFor(open)) }
    val snackbarHost = LocalHeyMikaSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val toast: (String, HeyMikaSnackbarTone, String?, Boolean) -> Unit = { message, tone, action, dismiss ->
        scope.launch {
            snackbarHost.showHeyMikaSnackbar(
                message = message,
                tone = tone,
                actionLabel = action,
                withDismissAction = dismiss,
            )
        }
    }
    LaunchedEffect(open) {
        if (open == "snackbar") {
            snackbarHost.showHeyMikaSnackbar(
                message = "Materi tersimpan ke Koleksi.",
                tone = HeyMikaSnackbarTone.Success,
                actionLabel = "Lihat Koleksi",
            )
        }
    }
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "ComicDialog", note = "Animasi dialog langsung (snap) saat reduced motion") {
            ComicButton(text = "Dialog konfirmasi", onClick = { overlay = Overlay.Dialog }, fullWidth = true)
            ComicButton(
                text = "Dialog hapus (Danger)",
                onClick = { overlay = Overlay.DialogDanger },
                variant = ComicButtonVariant.Outline,
                fullWidth = true,
            )
            ComicButton(
                text = "Dialog memuat",
                onClick = { overlay = Overlay.DialogLoading },
                variant = ComicButtonVariant.Outline,
                fullWidth = true,
            )
            ComicButton(
                text = "BasicComicDialog (isi bebas)",
                onClick = { overlay = Overlay.DialogBasic },
                variant = ComicButtonVariant.Outline,
                fullWidth = true,
            )
        }
        GalleryGroup(title = "ComicBottomSheet") {
            ComicButton(
                text = "Buka bottom sheet",
                onClick = { overlay = Overlay.Sheet },
                variant = ComicButtonVariant.Secondary,
                fullWidth = true,
            )
        }
        GalleryGroup(title = "HeyMikaSnackbar", note = "Dipasang sekali di HeyMikaRootFrame") {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
                ComicButton(
                    text = "Info",
                    onClick = { toast("Sinkronisasi dimulai.", HeyMikaSnackbarTone.Info, null, false) },
                    variant = ComicButtonVariant.Outline,
                    size = ComicButtonSize.Small,
                )
                ComicButton(
                    text = "Sukses",
                    onClick = {
                        toast("Materi tersimpan ke Koleksi.", HeyMikaSnackbarTone.Success, "Lihat Koleksi", false)
                    },
                    variant = ComicButtonVariant.Outline,
                    size = ComicButtonSize.Small,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
                ComicButton(
                    text = "Peringatan",
                    onClick = { toast("Kuota AI tinggal 2.", HeyMikaSnackbarTone.Warning, null, true) },
                    variant = ComicButtonVariant.Outline,
                    size = ComicButtonSize.Small,
                )
                ComicButton(
                    text = "Error",
                    onClick = { toast("Gagal mengunggah berkas.", HeyMikaSnackbarTone.Error, "Coba lagi", true) },
                    variant = ComicButtonVariant.Outline,
                    size = ComicButtonSize.Small,
                )
            }
        }
    }

    var loading by remember { mutableStateOf(true) }
    val close = { overlay = null }
    when (overlay) {
        Overlay.Dialog -> {
            ComicDialog(
                title = "Mulai kuis sekarang?",
                body = "Ada 10 soal Biologi. Timer 15 menit mulai saat kamu menekan Mulai.",
                confirmLabel = "Mulai",
                onConfirm = close,
                onDismissRequest = close,
                mascot = { MikaAvatar(pose = MikaPose.HAPPY, size = dimens.avatarXl) },
            )
        }

        Overlay.DialogDanger -> {
            ComicDialog(
                title = "Hapus workspace?",
                body = "Semua catatan, flashcard dan riwayat kuis di dalamnya ikut terhapus.",
                confirmLabel = "Hapus",
                onConfirm = close,
                onDismissRequest = close,
                confirmVariant = ComicButtonVariant.Danger,
            )
        }

        Overlay.DialogLoading -> {
            ComicDialog(
                title = "Menyimpan perubahan",
                body = "Tunggu sebentar, Mika sedang menyimpan.",
                confirmLabel = "Simpan",
                onConfirm = { loading = !loading },
                onDismissRequest = close,
                confirmLoading = loading,
                confirmLoadingLabel = "Menyimpan",
            )
        }

        Overlay.DialogBasic -> {
            BasicComicDialog(onDismissRequest = close, paneTitle = "Urutkan materi") {
                Text(text = "Urutkan materi", style = HeyMikaTheme.typography.headlineSm)
                var sort by remember { mutableStateOf(0) }
                Column {
                    listOf("Terbaru", "Nama A-Z", "Tanggal ujian").forEachIndexed { index, label ->
                        RadioButtonRow(selected = sort == index, onClick = { sort = index }, label = label)
                    }
                }
                ComicButton(text = "Terapkan", onClick = close, fullWidth = true)
            }
        }

        Overlay.Sheet -> {
            ComicBottomSheet(onDismissRequest = close, title = "Aksi workspace") {
                val ghost = ComicButtonDefaults.colors(ComicButtonVariant.Ghost)
                listOf(
                    Triple("Ubah nama", Icons.Outlined.Edit, ghost),
                    Triple("Bagikan", Icons.Outlined.Share, ghost),
                    Triple("Hapus", Icons.Outlined.Delete, ghost.copy(content = HeyMikaTheme.colors.danger)),
                ).forEach { (label, icon, buttonColors) ->
                    ComicButton(
                        text = label,
                        onClick = close,
                        variant = ComicButtonVariant.Ghost,
                        leadingIcon = icon,
                        fullWidth = true,
                        colors = buttonColors,
                    )
                }
                SubtleDivider()
                ComicButton(text = "Tutup", onClick = close, variant = ComicButtonVariant.Outline, fullWidth = true)
            }
        }

        null -> {
            Unit
        }
    }
}

private enum class PickerDemo { Time, Date }

@Composable
fun PickersPage(
    title: String,
    onBack: () -> Unit,
    open: String?,
) {
    val dimens = HeyMikaTheme.dimens
    var demo by rememberSaveable(open) {
        mutableStateOf(
            when (open) {
                "time" -> PickerDemo.Time
                "date" -> PickerDemo.Date
                else -> null
            },
        )
    }
    var pickedTime by remember { mutableStateOf(LocalTime.of(19, 0)) }
    var pickedDate by remember { mutableStateOf<LocalDate?>(LocalDate.of(2025, 7, 14)) }
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "DatePickerField") {
            var exam by remember { mutableStateOf<LocalDate?>(null) }
            DatePickerField(
                value = exam,
                onValueChange = { exam = it },
                label = "Tanggal ujian",
                minDate = LocalDate.of(2025, 1, 1),
                supportingText = "Mika menyusun rencana mundur dari tanggal ini.",
            )
            DatePickerField(value = pickedDate, onValueChange = { pickedDate = it }, label = "Terisi")
            DatePickerField(
                value = null,
                onValueChange = {},
                label = "Error",
                isError = true,
                supportingText = "Tanggal ujian wajib diisi.",
            )
            DatePickerField(value = pickedDate, onValueChange = {}, label = "Nonaktif", enabled = false)
        }
        GalleryGroup(
            title = "TimeRangeField",
            note = "Dua langkah ComicTimePicker; 22:00-07:00 melewati tengah malam",
        ) {
            var quiet by remember { mutableStateOf(TimeRange(LocalTime.of(22, 0), LocalTime.of(7, 0))) }
            TimeRangeField(value = quiet, onValueChange = { quiet = it }, label = "Jam tenang")
            var school by remember { mutableStateOf(TimeRange(LocalTime.of(7, 0), LocalTime.of(14, 30))) }
            TimeRangeField(
                value = school,
                onValueChange = { school = it },
                label = "Jam sekolah (tanpa lewat tengah malam)",
                allowCrossMidnight = false,
            )
        }
        GalleryGroup(title = "Picker langsung") {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
                ComicButton(
                    text = "Jam ${TimeMath.formatClock(pickedTime)}",
                    onClick = { demo = PickerDemo.Time },
                    variant = ComicButtonVariant.Outline,
                    size = ComicButtonSize.Medium,
                )
                ComicButton(
                    text = "Tanggal",
                    onClick = { demo = PickerDemo.Date },
                    variant = ComicButtonVariant.Outline,
                    size = ComicButtonSize.Medium,
                )
            }
        }
        GalleryGroup(title = "CountdownTimerPill", note = "Normal, peringatan < 60 dtk, habis") {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
                CountdownTimerPill(remainingSeconds = 868)
                CountdownTimerPill(remainingSeconds = 45)
                CountdownTimerPill(remainingSeconds = 0)
            }
            CountdownTimerPill(
                remainingSeconds = 868,
                label = "tersisa",
                tone = HeyMikaTheme.tones.orange,
                showLiveDot = false,
            )
            val deadline = remember { TimerDefaults.wallClock.nowMillis() + 75_000L }
            CountdownTimerPill(deadlineMillis = deadline, onExpired = {}, label = "langsung")
        }
        GalleryGroup(title = "ResendCooldownButton") {
            ResendCooldownButton(onResend = {}, cooldownSeconds = 30)
            ResendCooldownButton(onResend = {}, enabled = false)
        }
    }
    when (demo) {
        PickerDemo.Time -> {
            ComicTimePicker(
                initialTime = pickedTime,
                onConfirm = {
                    pickedTime = it
                    demo = null
                },
                onDismissRequest = { demo = null },
            )
        }

        PickerDemo.Date -> {
            ComicDatePicker(
                initialDate = pickedDate,
                onConfirm = {
                    pickedDate = it
                    demo = null
                },
                onDismissRequest = { demo = null },
                minDate = LocalDate.of(2025, 1, 1),
            )
        }

        null -> {
            Unit
        }
    }
}
