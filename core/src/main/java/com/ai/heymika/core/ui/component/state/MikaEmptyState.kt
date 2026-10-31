package com.ai.heymika.core.ui.component.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.component.foundation.ComicCard
import com.ai.heymika.core.ui.component.foundation.ComicCardVariant
import com.ai.heymika.core.ui.component.mascot.MikaAvatar
import com.ai.heymika.core.ui.component.mascot.MikaBubbleTail
import com.ai.heymika.core.ui.component.mascot.MikaSpeechBubble
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * An empty list or shelf, in the same voice as [MikaErrorState]: Mika in [pose], an optional
 * [speech] bubble, a [title], a [body] and an optional [action] (usually one ComicButton that
 * fills the gap, such as "Tambah Materi").
 */
@Composable
fun MikaEmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    pose: MikaPose = MikaPose.WAVING,
    variant: MikaStateVariant = MikaStateVariant.FullScreen,
    speech: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val dimens = HeyMikaTheme.dimens
    when (variant) {
        MikaStateVariant.FullScreen -> {
            Column(
                modifier =
                    modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = dimens.screenMargin, vertical = dimens.sectionGap),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimens.spaceMd, Alignment.CenterVertically),
            ) {
                MikaAvatar(pose = pose, size = dimens.avatarHero)
                if (speech != null) {
                    MikaSpeechBubble(
                        text = speech,
                        modifier = Modifier.fillMaxWidth(),
                        tail = MikaBubbleTail.Top,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.semantics { heading() },
                        style = HeyMikaTheme.typography.headlineMd,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = body,
                        style = HeyMikaTheme.typography.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                action?.invoke()
            }
        }

        MikaStateVariant.Inline -> {
            ComicCard(
                modifier = modifier.fillMaxWidth(),
                variant = ComicCardVariant.Subtle,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MikaAvatar(pose = pose, size = dimens.avatarLg)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.semantics { heading() },
                            style = HeyMikaTheme.typography.headlineSm,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = body,
                            style = HeyMikaTheme.typography.bodySm,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (action != null) {
                    Spacer(modifier = Modifier.height(dimens.spaceMd))
                    action()
                }
            }
        }
    }
}

@Preview(name = "Full screen", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360, heightDp = 720)
@Composable
private fun MikaEmptyStateFullScreenPreview() {
    HeyMikaTheme {
        MikaEmptyState(
            title = "Belum Ada Materi",
            body = "Unggah PDF rangkuman atau foto catatanmu, nanti Mika bantu rapikan jadi materi belajar.",
            speech = "“Rak bukunya masih kosong nih, yuk isi bareng Mika!”",
            action = {
                ComicButton(
                    text = "Tambah Materi",
                    onClick = {},
                    variant = ComicButtonVariant.Secondary,
                    leadingIcon = Icons.Filled.Add,
                )
            },
        )
    }
}

@Preview(
    name = "Full screen, no speech or action",
    showBackground = true,
    backgroundColor = 0xFFF8FCFF,
    widthDp = 360,
    heightDp = 560,
)
@Composable
private fun MikaEmptyStateMinimalPreview() {
    HeyMikaTheme {
        MikaEmptyState(
            title = "Belum Ada Notifikasi",
            body = "Kabar terbaru dari Mika akan muncul di sini.",
            pose = MikaPose.SLEEPING,
        )
    }
}

@Preview(name = "Inline", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun MikaEmptyStateInlinePreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MikaEmptyState(
                title = "Belum ada favorit",
                body = "Tandai materi dengan bintang supaya mudah ditemukan lagi.",
                pose = MikaPose.THINKING,
                variant = MikaStateVariant.Inline,
            )
            MikaEmptyState(
                title = "Kartu hari ini beres!",
                body = "Semua flashcard sudah diulang. Istirahat dulu, besok lanjut lagi.",
                pose = MikaPose.HAPPY,
                variant = MikaStateVariant.Inline,
                action = {
                    ComicButton(
                        text = "Lihat Rencana",
                        onClick = {},
                        variant = ComicButtonVariant.Outline,
                        size = ComicButtonSize.Small,
                        fullWidth = true,
                    )
                },
            )
        }
    }
}
