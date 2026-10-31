package com.ai.heymika.debug.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.component.mascot.HalftoneBackground
import com.ai.heymika.core.ui.component.mascot.HalftoneCorner
import com.ai.heymika.core.ui.component.mascot.HalftoneDecoration
import com.ai.heymika.core.ui.component.mascot.MikaAvatar
import com.ai.heymika.core.ui.component.mascot.MikaAvatarFrame
import com.ai.heymika.core.ui.component.mascot.MikaBubbleTail
import com.ai.heymika.core.ui.component.mascot.MikaCallout
import com.ai.heymika.core.ui.component.mascot.MikaCalloutPreset
import com.ai.heymika.core.ui.component.mascot.MikaSpeechBubble
import com.ai.heymika.core.ui.component.mascot.SpeedLinesDecoration
import com.ai.heymika.core.ui.component.mascot.SpeedLinesStyle
import com.ai.heymika.core.ui.modifier.comicSurface
import com.ai.heymika.core.ui.theme.HeyMikaTheme

private const val POSES_PER_ROW = 4

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MascotPage(
    title: String,
    onBack: () -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "MikaPose", note = "Vektor placeholder; ganti di rememberMikaPainter") {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
                maxItemsInEachRow = POSES_PER_ROW,
            ) {
                MikaPose.entries.forEach { pose ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        MikaAvatar(pose = pose, size = dimens.avatarLg)
                        Text(
                            text = pose.name.lowercase(),
                            style = HeyMikaTheme.typography.caption,
                            color = HeyMikaTheme.colors.textMuted,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                    }
                }
                // Pad the last row so its cells keep the same width as the others.
                val remainder = MikaPose.entries.size % POSES_PER_ROW
                if (remainder != 0) repeat(POSES_PER_ROW - remainder) { Box(modifier = Modifier.weight(1f)) }
            }
        }
        GalleryGroup(title = "MikaAvatar", note = "Bingkai None / Circle / RoundedTile, badge") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceMd),
                verticalAlignment = Alignment.Bottom,
            ) {
                MikaAvatar(pose = MikaPose.HAPPY, size = dimens.avatarXl, frame = MikaAvatarFrame.None)
                MikaAvatar(pose = MikaPose.HAPPY, size = dimens.avatarXl)
                MikaAvatar(
                    pose = MikaPose.HAPPY,
                    size = dimens.avatarXl,
                    frame = MikaAvatarFrame.RoundedTile,
                    containerColor = HeyMikaTheme.colors.badgeCyan,
                )
                MikaAvatar(pose = MikaPose.WAVING, size = dimens.avatarXl, badge = "nyaa~")
            }
            MikaAvatar(
                pose = MikaPose.WAVING_PENCIL,
                size = dimens.avatarHero,
                badge = "nyaa~",
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
fun MascotSpeechPage(
    title: String,
    onBack: () -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "MikaSpeechBubble", note = "Ekor Start / End / Top / Bottom, border ink 3dp") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MikaAvatar(pose = MikaPose.WAVING, size = dimens.avatarMd)
                MikaSpeechBubble(text = "Ayo belajar bareng, meong!", tail = MikaBubbleTail.Start)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MikaSpeechBubble(text = "Jawabanmu benar!", tail = MikaBubbleTail.End)
                MikaAvatar(pose = MikaPose.HAPPY, size = dimens.avatarMd)
            }
            MikaSpeechBubble(
                text = "Coba ingat lagi rumus luas lingkaran, ya.",
                tail = MikaBubbleTail.Top,
                tailBias = 0.2f,
                speaker = "Mika",
                speakerIcon = Icons.Outlined.AutoAwesome,
                tag = "Petunjuk",
                modifier = Modifier.fillMaxWidth(),
            )
            MikaSpeechBubble(
                text = "Mau Mika buatkan 5 flashcard dari bab ini?",
                tail = MikaBubbleTail.Bottom,
                tailBias = 0.8f,
                modifier = Modifier.fillMaxWidth(),
                footer = {
                    Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
                        ComicButton(text = "Buatkan", onClick = {}, size = ComicButtonSize.Small)
                        ComicButton(
                            text = "Nanti",
                            onClick = {},
                            size = ComicButtonSize.Small,
                            variant = ComicButtonVariant.Ghost,
                        )
                    }
                },
            )
        }
        GalleryGroup(title = "MikaCallout", note = "Preset Hero / Tip / Guide / Whisper / Error") {
            MikaCalloutPreset.entries.forEach { preset ->
                MikaCallout(
                    text =
                        when (preset) {
                            MikaCalloutPreset.Hero -> "Hari ini kita taklukkan Biologi bab 3!"
                            MikaCalloutPreset.Tip -> "Ulangi flashcard yang salah sebelum tidur."
                            MikaCalloutPreset.Guide -> "Pilih jenjang supaya materinya pas."
                            MikaCalloutPreset.Whisper -> "Psst, ada kuis baru di Materi."
                            MikaCalloutPreset.Error -> "Ups, unggahannya gagal. Coba lagi, ya."
                        },
                    preset = preset,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        GalleryGroup(title = "Halftone & speed lines", note = "Dekoratif, tanpa semantik") {
            HalftoneBackground(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(dimens.avatarHero)
                        .comicSurface(shape = HeyMikaTheme.shapes.cardLarge)
                        .clip(HeyMikaTheme.shapes.cardLarge),
            ) {
                Text(
                    text = "HalftoneBackground",
                    style = HeyMikaTheme.typography.labelLg,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceMd)) {
                DecorationBox("Halftone pojok", Modifier.weight(1f)) {
                    HalftoneDecoration(corner = HalftoneCorner.TopEnd, modifier = Modifier.fillMaxSize())
                    HalftoneDecoration(corner = HalftoneCorner.BottomStart, modifier = Modifier.fillMaxSize())
                }
                DecorationBox("Radial", Modifier.weight(1f)) {
                    SpeedLinesDecoration(modifier = Modifier.fillMaxSize())
                }
                DecorationBox("Diagonal", Modifier.weight(1f)) {
                    SpeedLinesDecoration(style = SpeedLinesStyle.Diagonal, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun DecorationBox(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(HeyMikaTheme.dimens.avatarXxl)
                    .comicSurface(shape = HeyMikaTheme.shapes.card, borderWidth = HeyMikaTheme.dimens.borderSubtle)
                    .clip(HeyMikaTheme.shapes.card),
        ) {
            content()
        }
        // The hard shadow draws outside the box, so the caption starts below it.
        SpecimenLabel(
            label,
            modifier = Modifier.padding(top = HeyMikaTheme.dimens.comicShadowOffset + HeyMikaTheme.dimens.spaceXs),
        )
    }
}
