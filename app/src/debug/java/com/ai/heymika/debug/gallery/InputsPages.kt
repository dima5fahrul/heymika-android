package com.ai.heymika.debug.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import com.ai.heymika.core.domain.password.PasswordPolicy
import com.ai.heymika.core.ui.component.input.ComicFieldIconButton
import com.ai.heymika.core.ui.component.input.ComicTextField
import com.ai.heymika.core.ui.component.input.PasswordField
import com.ai.heymika.core.ui.component.input.PasswordStrengthChecklist
import com.ai.heymika.core.ui.component.input.SearchField
import com.ai.heymika.core.ui.component.input.TextAreaField
import com.ai.heymika.core.ui.component.input.toLevel
import com.ai.heymika.core.ui.component.input.toRuleItems
import com.ai.heymika.core.ui.component.selection.CheckboxRow
import com.ai.heymika.core.ui.component.selection.ComicCheckbox
import com.ai.heymika.core.ui.component.selection.ComicRadioButton
import com.ai.heymika.core.ui.component.selection.ComicSwitch
import com.ai.heymika.core.ui.component.selection.OptionCardLayout
import com.ai.heymika.core.ui.component.selection.OptionSelectionMode
import com.ai.heymika.core.ui.component.selection.RadioButtonRow
import com.ai.heymika.core.ui.component.selection.SegmentedControl
import com.ai.heymika.core.ui.component.selection.SegmentedControlStyle
import com.ai.heymika.core.ui.component.selection.SegmentedOption
import com.ai.heymika.core.ui.component.selection.SelectableOptionCard
import com.ai.heymika.core.ui.component.selection.ToggleRow
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/** ComicTextField and PasswordField with the strength checklist. */
@Composable
fun InputsPage(
    title: String,
    onBack: () -> Unit,
) {
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "ComicTextField") {
            var email by remember { mutableStateOf("") }
            ComicTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "nama@sekolah.id",
                leadingIcon = Icons.Outlined.Mail,
                required = true,
                helperText = "Kami kirim tautan verifikasi ke sini.",
            )
            var name by remember { mutableStateOf("Budi Santoso") }
            ComicTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nama panggilan",
                leadingIcon = Icons.Outlined.Person,
                labelTrailingText = "Opsional",
                trailing = {
                    if (name.isNotEmpty()) {
                        ComicFieldIconButton(
                            icon = Icons.Filled.Close,
                            contentDescription = "Hapus teks",
                            onClick = { name = "" },
                        )
                    }
                },
            )
            var wrong by remember { mutableStateOf("budi@") }
            ComicTextField(
                value = wrong,
                onValueChange = { wrong = it },
                label = "Email",
                leadingIcon = Icons.Outlined.Mail,
                errorText = "Format email belum benar. Contoh: nama@sekolah.id",
            )
            ComicTextField(
                value = "kelas-12@heymika.id",
                onValueChange = {},
                label = "Email (nonaktif)",
                leadingIcon = Icons.Outlined.Mail,
                enabled = false,
            )
            ComicTextField(
                value = "Paket Gratis",
                onValueChange = {},
                label = "Paket (hanya baca)",
                readOnly = true,
            )
        }
        GalleryGroup(title = "PasswordField + checklist", note = "Aturan dari PasswordPolicy (core/domain)") {
            var password by remember { mutableStateOf("belajar") }
            val evaluation = remember(password) { PasswordPolicy.evaluate(password) }
            PasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Kata sandi baru",
                required = true,
                errorText = if (password.isNotEmpty() && !evaluation.isAcceptable) "Minimal 12 karakter." else null,
            )
            PasswordStrengthChecklist(
                rules = evaluation.toRuleItems(),
                strength = evaluation.strength.toLevel(),
            )
            PasswordField(value = "rahasia", onValueChange = {}, label = "Kata sandi (nonaktif)", enabled = false)
        }
    }
}

/** SearchField and TextAreaField. */
@Composable
fun InputsMorePage(
    title: String,
    onBack: () -> Unit,
) {
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "SearchField") {
            var query by remember { mutableStateOf("") }
            SearchField(query = query, onQueryChange = { query = it })
            var filled by remember { mutableStateOf("Hukum Newton") }
            SearchField(
                query = filled,
                onQueryChange = { filled = it },
                placeholder = "Cari rumus, flashcard, atau modul",
            )
            SearchField(query = "", onQueryChange = {}, enabled = false)
        }
        GalleryGroup(title = "TextAreaField", note = "Penghitung karakter dan batas maksimum") {
            var note by remember { mutableStateOf("Mika, tolong jelaskan siklus Calvin dengan bahasa sederhana.") }
            TextAreaField(
                value = note,
                onValueChange = { note = it },
                label = "Pertanyaan untuk Mika",
                placeholder = "Tulis pertanyaanmu…",
                maxLength = 200,
                helperText = "Tanpa data pribadi, ya.",
            )
            TextAreaField(
                value = "",
                onValueChange = {},
                label = "Catatan",
                errorText = "Catatan tidak boleh kosong.",
                required = true,
            )
        }
    }
}

/** Checkbox, radio and switch rows plus the bare controls. */
@Composable
fun SelectionPage(
    title: String,
    onBack: () -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "CheckboxRow") {
            var terms by remember { mutableStateOf(false) }
            val linkStyle =
                TextLinkStyles(SpanStyle(color = HeyMikaTheme.colors.primaryText, fontWeight = FontWeight.Bold))
            CheckboxRow(
                checked = terms,
                onCheckedChange = { terms = it },
                label =
                    buildAnnotatedString {
                        append("Saya setuju dengan ")
                        withLink(LinkAnnotation.Url("https://example.com/ketentuan", linkStyle)) {
                            append("Ketentuan Layanan")
                        }
                        append(" dan ")
                        withLink(LinkAnnotation.Url("https://example.com/privasi", linkStyle)) {
                            append("Kebijakan Privasi")
                        }
                    },
            )
            var rememberMe by remember { mutableStateOf(true) }
            CheckboxRow(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it },
                label = "Ingat saya",
                description = "Tetap masuk di perangkat ini.",
            )
            CheckboxRow(checked = false, onCheckedChange = {}, label = "Wajib dicentang", isError = true)
            CheckboxRow(checked = true, onCheckedChange = {}, label = "Nonaktif", enabled = false)
        }
        GalleryGroup(title = "RadioButtonRow") {
            var level by remember { mutableIntStateOf(1) }
            Column(modifier = Modifier.selectableGroup()) {
                listOf("SMP", "SMA", "Kuliah").forEachIndexed { index, label ->
                    RadioButtonRow(selected = level == index, onClick = { level = index }, label = label)
                }
                RadioButtonRow(selected = false, onClick = {}, label = "Lainnya (nonaktif)", enabled = false)
            }
        }
        GalleryGroup(title = "ToggleRow & kontrol tunggal") {
            var reminder by remember { mutableStateOf(true) }
            ToggleRow(
                title = "Pengingat harian",
                subtitle = "Pukul 19.00 setiap hari",
                checked = reminder,
                onCheckedChange = { reminder = it },
                icon = Icons.Outlined.NotificationsActive,
            )
            ToggleRow(title = "Nonaktif", checked = false, onCheckedChange = {}, enabled = false)
            var a by remember { mutableStateOf(true) }
            var b by remember { mutableStateOf(true) }
            var s by remember { mutableStateOf(false) }
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs)) {
                ComicCheckbox(checked = a, onCheckedChange = { a = it })
                ComicCheckbox(checked = false, onCheckedChange = {}, isError = true)
                ComicCheckbox(checked = true, onCheckedChange = {}, enabled = false)
                ComicRadioButton(selected = b, onClick = { b = !b })
                ComicRadioButton(selected = false, onClick = {}, isError = true)
                ComicSwitch(checked = s, onCheckedChange = { s = it })
                ComicSwitch(checked = true, onCheckedChange = {}, enabled = false)
            }
        }
    }
}

/** SelectableOptionCard and SegmentedControl. */
@Composable
fun SelectionMorePage(
    title: String,
    onBack: () -> Unit,
) {
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "SelectableOptionCard", note = "Terpilih = Strict, belum = Subtle") {
            var goal by remember { mutableIntStateOf(0) }
            Row(horizontalArrangement = Arrangement.spacedBy(HeyMikaTheme.dimens.spaceSmPlus)) {
                SelectableOptionCard(
                    selected = goal == 0,
                    onClick = { goal = 0 },
                    title = "Ujian sekolah",
                    description = "UTS & UAS",
                    icon = Icons.Outlined.School,
                    badge = "Populer",
                    modifier = Modifier.weight(1f),
                )
                SelectableOptionCard(
                    selected = goal == 1,
                    onClick = { goal = 1 },
                    title = "Masuk PTN",
                    description = "SNBT",
                    emoji = "🎯",
                    modifier = Modifier.weight(1f),
                )
            }
            var subjects by remember { mutableStateOf(setOf(0)) }
            listOf(
                Triple("Matematika", "Aljabar, kalkulus", Icons.Outlined.Calculate),
                Triple("Biologi", "Sel, genetika", Icons.Outlined.Science),
            ).forEachIndexed { index, (label, description, icon) ->
                SelectableOptionCard(
                    selected = index in subjects,
                    onClick = { subjects = if (index in subjects) subjects - index else subjects + index },
                    title = label,
                    description = description,
                    icon = icon,
                    layout = OptionCardLayout.Row,
                    selectionMode = OptionSelectionMode.Multiple,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            SelectableOptionCard(
                selected = false,
                onClick = {},
                title = "Pilih minimal satu",
                layout = OptionCardLayout.Row,
                isError = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        GalleryGroup(title = "SegmentedControl") {
            var track by remember { mutableIntStateOf(0) }
            SegmentedControl(
                options = listOf(SegmentedOption("Harian"), SegmentedOption("Mingguan"), SegmentedOption("Bulanan")),
                selectedIndex = track,
                onSelect = { track = it },
            )
            var separated by remember { mutableIntStateOf(1) }
            SegmentedControl(
                options = listOf(SegmentedOption("Bulanan", "Rp49.000"), SegmentedOption("Tahunan", "Hemat 30%")),
                selectedIndex = separated,
                onSelect = { separated = it },
                style = SegmentedControlStyle.Separated,
            )
            Text(text = "Nonaktif", style = HeyMikaTheme.typography.caption, color = HeyMikaTheme.colors.textMuted)
            SegmentedControl(
                options = listOf(SegmentedOption("A"), SegmentedOption("B")),
                selectedIndex = 0,
                onSelect = {},
                enabled = false,
            )
        }
    }
}
