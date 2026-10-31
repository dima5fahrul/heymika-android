package com.ai.heymika.debug.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ai.heymika.core.domain.text.MarkupTone
import com.ai.heymika.core.ui.component.display.Accordion
import com.ai.heymika.core.ui.component.display.AlertTone
import com.ai.heymika.core.ui.component.display.CountBadge
import com.ai.heymika.core.ui.component.display.DisplayTone
import com.ai.heymika.core.ui.component.display.DotBadge
import com.ai.heymika.core.ui.component.display.ExpandableSection
import com.ai.heymika.core.ui.component.display.FilterChipItem
import com.ai.heymika.core.ui.component.display.FilterChipRow
import com.ai.heymika.core.ui.component.display.InfoNoteCard
import com.ai.heymika.core.ui.component.display.InlineAlertBanner
import com.ai.heymika.core.ui.component.display.LinearProgress
import com.ai.heymika.core.ui.component.display.LinearProgressStyle
import com.ai.heymika.core.ui.component.display.ListItemSkeleton
import com.ai.heymika.core.ui.component.display.MetricMeter
import com.ai.heymika.core.ui.component.display.MultiSelectFilterChipRow
import com.ai.heymika.core.ui.component.display.Pill
import com.ai.heymika.core.ui.component.display.PillDot
import com.ai.heymika.core.ui.component.display.PillSize
import com.ai.heymika.core.ui.component.display.PillStyle
import com.ai.heymika.core.ui.component.display.PillTab
import com.ai.heymika.core.ui.component.display.PillTabRow
import com.ai.heymika.core.ui.component.display.ProgressRing
import com.ai.heymika.core.ui.component.display.ProgressRingSize
import com.ai.heymika.core.ui.component.display.ProgressSegment
import com.ai.heymika.core.ui.component.display.SectionHeader
import com.ai.heymika.core.ui.component.display.SegmentedLinearProgress
import com.ai.heymika.core.ui.component.display.SkeletonBlock
import com.ai.heymika.core.ui.component.display.StatTile
import com.ai.heymika.core.ui.component.display.StepIndicator
import com.ai.heymika.core.ui.component.display.StepStatus
import com.ai.heymika.core.ui.component.display.Tag
import com.ai.heymika.core.ui.component.display.TimelineStep
import com.ai.heymika.core.ui.component.display.UserAvatar
import com.ai.heymika.core.ui.text.InlineCodeChip
import com.ai.heymika.core.ui.text.RichText
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.core.utils.text.initialsOf

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DisplayPage(
    title: String,
    onBack: () -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "Pill", note = "DisplayTone × Tinted / Filled / Outline") {
            PillStyle.entries.forEach { style ->
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs),
                    verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
                ) {
                    DisplayTone.entries.forEach { tone ->
                        Pill(text = tone.name, tone = tone, style = style, size = PillSize.Small)
                    }
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceSm),
            ) {
                val centered = Modifier.align(Alignment.CenterVertically)
                Pill(
                    text = "Hero",
                    tone = DisplayTone.Cyan,
                    icon = Icons.Outlined.Star,
                    uppercase = true,
                    modifier = centered,
                )
                Pill(text = "Live", tone = DisplayTone.Success, dot = PillDot.Pulsing, modifier = centered)
                Pill(text = "Draf", tone = DisplayTone.Neutral, dot = PillDot.Static, modifier = centered)
                Tag(text = "Biologi", tone = DisplayTone.Success, modifier = centered)
                Tag(text = "Medium", tone = DisplayTone.Orange, size = PillSize.Medium, modifier = centered)
            }
        }
        GalleryGroup(title = "Badge") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceMd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CountBadge(count = 3)
                CountBadge(count = 120)
                CountBadge(count = 7, tone = DisplayTone.Primary)
                DotBadge()
                DotBadge(pulsing = true)
                DotBadge(color = HeyMikaTheme.colors.accent, ringColor = HeyMikaTheme.colors.surface)
            }
        }
        GalleryGroup(title = "FilterChipRow") {
            var subject by remember { mutableStateOf<String?>("Semua") }
            FilterChipRow(
                items =
                    listOf("Semua", "Matematika", "Biologi", "Fisika", "Sejarah").map { label ->
                        FilterChipItem(key = label, label = label)
                    },
                selectedKey = subject,
                onSelect = { subject = it },
                modifier = Modifier.fullBleed(HeyMikaTheme.dimens.screenMargin),
            )
            var tags by remember { mutableStateOf(setOf("aktif")) }
            MultiSelectFilterChipRow(
                items =
                    listOf(
                        FilterChipItem(key = "aktif", label = "Aktif", count = 4),
                        FilterChipItem(key = "arsip", label = "Diarsipkan", count = 2),
                        FilterChipItem(key = "favorit", label = "Favorit", icon = Icons.Outlined.Star),
                    ),
                selectedKeys = tags,
                onToggle = { key -> tags = if (key in tags) tags - key else tags + key },
                modifier = Modifier.fullBleed(HeyMikaTheme.dimens.screenMargin),
            )
        }
        GalleryGroup(title = "PillTabRow") {
            var tab by remember { mutableIntStateOf(0) }
            PillTabRow(
                tabs =
                    listOf(
                        PillTab("Ringkasan AI", icon = Icons.Outlined.AutoStories),
                        PillTab("Flashcard", icon = Icons.Outlined.Style, count = 24),
                        PillTab("Kuis", icon = Icons.Outlined.Quiz, count = 3),
                    ),
                selectedIndex = tab,
                onSelect = { tab = it },
                modifier = Modifier.fullBleed(HeyMikaTheme.dimens.screenMargin),
            )
        }
        GalleryGroup(title = "Accordion & ExpandableSection") {
            Accordion(
                title = "Apa itu siklus Calvin?",
                subtitle = "Ketuk untuk membuka",
                icon = Icons.Outlined.AutoStories,
            ) {
                Text(
                    "Reaksi gelap fotosintesis yang mengubah CO₂ menjadi gula.",
                    style = HeyMikaTheme.typography.bodySm,
                )
            }
            Accordion(title = "Sudah terbuka", initiallyExpanded = true) {
                Text("Isi accordion yang sedang terbuka.", style = HeyMikaTheme.typography.bodySm)
            }
            Accordion(title = "Nonaktif", enabled = false) {}
            ExpandableSection(title = "Bagian inline", initiallyExpanded = true) {
                Text("ExpandableSection tanpa kartu, untuk daftar padat.", style = HeyMikaTheme.typography.bodySm)
            }
        }
        GalleryGroup(title = "UserAvatar", note = "Inisial dari initialsOf() (core/utils)") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UserAvatar(initials = initialsOf("Budi Santoso"), size = dimens.avatarXs)
                UserAvatar(initials = initialsOf("Siti Nurhaliza"), tone = HeyMikaTheme.tones.orange)
                UserAvatar(initials = initialsOf("mika"), size = dimens.avatarMd, presenceDot = true)
                UserAvatar(
                    initials = initialsOf("Ani Rahma Putri"),
                    size = dimens.avatarLg,
                    tone = HeyMikaTheme.tones.purple,
                    ringColor = HeyMikaTheme.colors.primary,
                )
            }
        }
    }
}

@Composable
fun DisplayDataPage(
    title: String,
    onBack: () -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    val colors = HeyMikaTheme.colors
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "LinearProgress") {
            SpecimenLabel("Comic 35%")
            LinearProgress(progress = 0.35f)
            SpecimenLabel("Comic 100%, warna sukses")
            LinearProgress(progress = 1f, color = colors.success)
            SpecimenLabel("Plain 70%")
            LinearProgress(progress = 0.7f, style = LinearProgressStyle.Plain)
            SpecimenLabel("Segmented: dikuasai / dipelajari / belum")
            SegmentedLinearProgress(
                segments =
                    listOf(
                        ProgressSegment(0.45f, colors.success),
                        ProgressSegment(0.3f, colors.accent),
                        ProgressSegment(0.1f, colors.ribbonRed),
                    ),
            )
        }
        GalleryGroup(title = "ProgressRing") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceLg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProgressRing(progress = 0.25f)
                ProgressRing(progress = 0.6f, size = ProgressRingSize.Medium, color = colors.accent)
                ProgressRing(progress = 0.85f, size = ProgressRingSize.Large, label = "17/20", sublabel = "Benar")
            }
        }
        GalleryGroup(title = "StepIndicator & TimelineStep") {
            StepIndicator(steps = listOf(StepStatus.Done, StepStatus.Active, StepStatus.Pending, StepStatus.Pending))
            TimelineStep(number = 1, title = "Unggah berkas", status = StepStatus.Done, meta = "09.41")
            TimelineStep(
                number = 2,
                title = "Mika membaca materi",
                status = StepStatus.Active,
                description = "Halaman 12 dari 30",
            )
            TimelineStep(number = 3, title = "Ekstraksi rumus", status = StepStatus.Failed, description = "Coba lagi")
            TimelineStep(number = 4, title = "Ringkasan siap", status = StepStatus.Pending, showConnector = false)
        }
        GalleryGroup(title = "StatTile") {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
            ) {
                StatTile(
                    value = "12",
                    unit = "hari",
                    label = "Streak belajar",
                    icon = Icons.Outlined.LocalFireDepartment,
                    iconTone = DisplayTone.Orange,
                    delta = "+2",
                    deltaIcon = Icons.AutoMirrored.Outlined.TrendingUp,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                StatTile(
                    value = "86",
                    unit = "%",
                    label = "Akurasi kuis",
                    icon = Icons.Outlined.WorkspacePremium,
                    caption = "Lihat detail",
                    captionTone = DisplayTone.Primary,
                    onClick = {},
                    onClickLabel = "Buka statistik",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
        GalleryGroup(title = "MetricMeter") {
            MetricMeter(
                label = "Target harian",
                value = "45 / 60 menit",
                icon = Icons.Outlined.Timer,
                status = "Hampir!",
                progress = 0.75f,
            )
            MetricMeter(
                label = "Kuota AI",
                value = "20 / 20",
                icon = Icons.Outlined.Bolt,
                iconTone = DisplayTone.Orange,
                status = "Habis",
                statusTone = DisplayTone.Danger,
            )
        }
        GalleryGroup(title = "SectionHeader") {
            SectionHeader(title = "Lanjutkan belajar", onAction = {})
            SectionHeader(
                title = "Rekomendasi Mika",
                subtitle = "Berdasarkan kuis terakhirmu",
                kicker = "Untukmu",
                icon = Icons.Outlined.Star,
            )
            SectionHeader(title = "Ringkas", trailingText = "3 item", compact = true)
        }
    }
}

@Composable
fun DisplayFeedbackPage(
    title: String,
    onBack: () -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "InlineAlertBanner") {
            InlineAlertBanner(
                tone = AlertTone.Info,
                title = "Sinkronisasi berjalan",
                body = "Catatan baru akan muncul.",
            )
            InlineAlertBanner(tone = AlertTone.Success, title = "Materi tersimpan", onDismiss = {})
            InlineAlertBanner(
                tone = AlertTone.Warning,
                title = "Kuota hampir habis",
                body = "Sisa 2 pertanyaan hari ini.",
                actionLabel = "Upgrade",
                onAction = {},
            )
            InlineAlertBanner(
                tone = AlertTone.Error,
                title = "Unggahan gagal",
                body = "Berkas lebih dari 20 MB.",
                actionLabel = "Pilih ulang",
                onAction = {},
                onDismiss = {},
            )
        }
        GalleryGroup(title = "InfoNoteCard") {
            InfoNoteCard(title = "Tips Mika", body = "Ulangi flashcard sebelum tidur supaya lebih nempel.")
            InfoNoteCard(
                body = "Data belajarmu tersimpan lokal saat offline.",
                tone = DisplayTone.Success,
                actionLabel = "Pelajari",
                onAction = {},
            )
        }
        GalleryGroup(title = "Skeleton") {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(dimens.avatarXl), shape = HeyMikaTheme.shapes.card)
            ListItemSkeleton()
            ListItemSkeleton(showAvatar = false, lines = 3, shimmer = false)
        }
        GalleryGroup(title = "RichText & InlineCodeChip", note = "MarkupParser (core/domain) → RichText (core/ui)") {
            RichText(
                text =
                    "**ATP**-nya dihasilkan di ==membran tilakoid==. Rumus: \$6CO_2 + 6H_2O\$, " +
                        "fungsi `calvin()` dan [benar]{.success} vs [keliru]{.danger}. *Italic* juga bisa.",
            )
            Box {
                Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
                    InlineCodeChip(text = "O(n log n)")
                    InlineCodeChip(text = "PRIMARY", tone = MarkupTone.PRIMARY)
                    InlineCodeChip(text = "OK", tone = MarkupTone.SUCCESS)
                    InlineCodeChip(text = "ERR", tone = MarkupTone.DANGER)
                }
            }
        }
    }
}
