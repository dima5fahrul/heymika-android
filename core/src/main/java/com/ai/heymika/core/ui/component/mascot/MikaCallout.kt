package com.ai.heymika.core.ui.component.mascot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * Ready-made Mika + bubble rows:
 * - [Hero]: large greeting on onboarding and auth banners ("Sapaan Mika").
 * - [Tip]: a study tip on a cyan bubble ("Tips Mika").
 * - [Guide]: step-by-step guidance inside a flow ("Panduan Mika").
 * - [Whisper]: a quiet aside on a muted bubble ("Mika Berbisik").
 * - [Error]: a gentle problem note on a danger-tinted bubble ("Kata Mika").
 */
enum class MikaCalloutPreset { Hero, Tip, Guide, Whisper, Error }

object MikaCalloutDefaults {
    fun pose(preset: MikaCalloutPreset): MikaPose =
        when (preset) {
            MikaCalloutPreset.Hero -> MikaPose.WAVING
            MikaCalloutPreset.Tip -> MikaPose.THINKING
            MikaCalloutPreset.Guide -> MikaPose.WAVING_PENCIL
            MikaCalloutPreset.Whisper -> MikaPose.HAPPY
            MikaCalloutPreset.Error -> MikaPose.WORRIED
        }

    @Composable
    @ReadOnlyComposable
    fun speaker(preset: MikaCalloutPreset): String =
        stringResource(
            when (preset) {
                MikaCalloutPreset.Hero -> R.string.mascot_speaker_greeting
                MikaCalloutPreset.Tip -> R.string.mascot_speaker_tip
                MikaCalloutPreset.Guide -> R.string.mascot_speaker_guide
                MikaCalloutPreset.Whisper -> R.string.mascot_speaker_whisper
                MikaCalloutPreset.Error -> R.string.mascot_speaker_error
            },
        )

    @Composable
    @ReadOnlyComposable
    fun avatarSize(preset: MikaCalloutPreset): Dp =
        when (preset) {
            MikaCalloutPreset.Hero -> HeyMikaTheme.dimens.avatarXxl
            MikaCalloutPreset.Tip, MikaCalloutPreset.Guide, MikaCalloutPreset.Error -> HeyMikaTheme.dimens.avatarLg
            MikaCalloutPreset.Whisper -> HeyMikaTheme.dimens.avatarMd
        }
}

/**
 * Mika next to a speech bubble whose tail points back at her. The mascot is decorative; TalkBack
 * reads the bubble (speaker, [text], [footer]) as one node.
 */
@Composable
fun MikaCallout(
    text: String,
    modifier: Modifier = Modifier,
    preset: MikaCalloutPreset = MikaCalloutPreset.Guide,
    pose: MikaPose = MikaCalloutDefaults.pose(preset),
    speaker: String? = MikaCalloutDefaults.speaker(preset),
    tag: String? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val style = calloutStyle(preset)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeyMikaTheme.dimens.spaceXs),
    ) {
        MikaAvatar(
            pose = pose,
            size = MikaCalloutDefaults.avatarSize(preset),
            frame = MikaAvatarFrame.None,
        )
        MikaSpeechBubble(
            text = text,
            modifier = Modifier.weight(1f),
            tail = MikaBubbleTail.Start,
            color = style.color,
            borderColor = style.borderColor,
            borderWidth = style.borderWidth,
            textStyle = style.textStyle,
            speaker = speaker,
            speakerIcon = style.speakerIcon,
            speakerColor = style.speakerColor,
            tag = tag,
            footer = footer,
        )
    }
}

private data class CalloutStyle(
    val color: Color,
    val borderColor: Color,
    val borderWidth: Dp,
    val speakerColor: Color,
    val speakerIcon: ImageVector,
    val textStyle: TextStyle,
)

@Composable
@ReadOnlyComposable
private fun calloutStyle(preset: MikaCalloutPreset): CalloutStyle {
    val c = HeyMikaTheme.colors
    val d = HeyMikaTheme.dimens
    val t = HeyMikaTheme.typography
    return when (preset) {
        MikaCalloutPreset.Hero -> {
            CalloutStyle(c.surface, c.ink, d.borderInk, c.primaryText, Icons.Filled.ChatBubble, t.labelLg)
        }

        MikaCalloutPreset.Tip -> {
            CalloutStyle(c.badgeCyan, c.ink, d.borderInk, c.primaryText, Icons.Filled.Lightbulb, t.bodyMd)
        }

        MikaCalloutPreset.Guide -> {
            CalloutStyle(c.surface, c.ink, d.borderInk, c.primaryText, Icons.Filled.ChatBubble, t.bodyMd)
        }

        MikaCalloutPreset.Whisper -> {
            CalloutStyle(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                borderColor = c.borderMuted,
                borderWidth = d.borderSubtle,
                speakerColor = c.warningContent,
                speakerIcon = Icons.Filled.Pets,
                textStyle = t.bodySm.copy(fontStyle = FontStyle.Italic),
            )
        }

        MikaCalloutPreset.Error -> {
            CalloutStyle(c.dangerBg, c.danger, d.borderControl, c.danger, Icons.Filled.ErrorOutline, t.bodyMd)
        }
    }
}

@Preview(name = "Presets", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun MikaCalloutPresetsPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MikaCallout(
                text = "\"Halo calon juara! Siapa nama panggilan kerenmu?\"",
                modifier = Modifier.fillMaxWidth(),
                preset = MikaCalloutPreset.Hero,
                footer = {
                    Text(
                        text = "Tips: Belajar rutin 15–30 menit sehari lebih awet nempel di otak!",
                        style = HeyMikaTheme.typography.caption,
                        color = HeyMikaTheme.colors.textMuted,
                    )
                },
            )
            MikaCallout(
                text = "Ulangi kartu yang salah besok pagi, ingatanmu lebih segar.",
                modifier = Modifier.fillMaxWidth(),
                preset = MikaCalloutPreset.Tip,
            )
            MikaCallout(
                text = "Cek inbox atau spam ya! Mika udah kirim surat magis buat kamu.",
                modifier = Modifier.fillMaxWidth(),
                preset = MikaCalloutPreset.Guide,
                pose = MikaPose.ENVELOPE,
            )
            MikaCallout(
                text = "“Tanya saja, jangan ragu! Nggak ada pertanyaan yang bodoh di sini, Meow!”",
                modifier = Modifier.fillMaxWidth(),
                preset = MikaCalloutPreset.Whisper,
                tag = "Fokus 100%",
            )
            MikaCallout(
                text = "Email atau password belum cocok. Coba cek lagi pelan-pelan, ya.",
                modifier = Modifier.fillMaxWidth(),
                preset = MikaCalloutPreset.Error,
            )
        }
    }
}
