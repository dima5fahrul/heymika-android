package com.ai.heymika.core.ui.component.display

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.core.ui.theme.ToneSpec

/**
 * A round user avatar. [initials] ("DF") show on a [tone] disc until [imageUrl] loads through Coil,
 * and stay if it fails or is null.
 * - Initials are computed by the caller (a `core/utils` helper), not here.
 * - [ringColor] adds a 2dp ring, for example white on a coloured header.
 * - [presenceDot] adds a success dot at the bottom end.
 * - [size] takes the avatar tokens: avatarXs 32 up to avatarHero 144.
 *
 * With [contentDescription] the avatar is announced as an image; without it, it is decorative
 * (the name is usually next to it).
 */
@Composable
fun UserAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    size: Dp = HeyMikaTheme.dimens.avatarSm,
    tone: ToneSpec = HeyMikaTheme.tones.sky,
    ringColor: Color = Color.Transparent,
    presenceDot: Boolean = false,
    contentDescription: String? = null,
) {
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.pill
    val ringWidth = if (ringColor.alpha > 0f) dimens.borderControl else 0.dp
    val crossfade = !HeyMikaTheme.reducedMotion
    var loaded by remember(imageUrl) { mutableStateOf(false) }
    val semantics =
        if (contentDescription != null) {
            Modifier.clearAndSetSemantics {
                this.contentDescription = contentDescription
                role = Role.Image
            }
        } else {
            Modifier.clearAndSetSemantics {}
        }
    Box(
        modifier =
            modifier
                .then(semantics)
                .size(size),
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .then(if (ringWidth > 0.dp) Modifier.border(ringWidth, ringColor, shape) else Modifier)
                    .padding(ringWidth)
                    .clip(shape)
                    .background(tone.container),
            contentAlignment = Alignment.Center,
        ) {
            if (!loaded) {
                Text(
                    text = initials,
                    color = tone.content,
                    style = initialsStyle(size),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
            if (imageUrl != null) {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(crossfade)
                            .build(),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    onLoading = { loaded = false },
                    onSuccess = { loaded = true },
                    onError = { loaded = false },
                    contentScale = ContentScale.Crop,
                )
            }
        }
        if (presenceDot) {
            DotBadge(
                modifier = Modifier.align(Alignment.BottomEnd),
                color = HeyMikaTheme.colors.success,
                size = if (size >= dimens.avatarMd) dimens.spaceSmPlus else dimens.spaceSm,
                ringColor = HeyMikaTheme.colors.surface,
            )
        }
    }
}

@Composable
@ReadOnlyComposable
private fun initialsStyle(size: Dp): TextStyle {
    val dimens = HeyMikaTheme.dimens
    val typography = HeyMikaTheme.typography
    return when {
        size >= dimens.avatarHero -> typography.displayHeroLarge
        size >= dimens.avatarXxl -> typography.displayHero
        size >= dimens.avatarXl -> typography.headlineMd
        size >= dimens.avatarMd -> typography.sectionTitle
        size >= dimens.avatarSm -> typography.labelLg
        else -> typography.labelSm
    }
}

@Preview(name = "UserAvatar", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun UserAvatarPreview() {
    HeyMikaTheme {
        val dimens = HeyMikaTheme.dimens
        val tones = HeyMikaTheme.tones
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(initials = "DF", size = dimens.avatarXs)
            UserAvatar(initials = "AR", tone = tones.orange, presenceDot = true)
            UserAvatar(initials = "MS", size = dimens.avatarMd, tone = tones.yellow, presenceDot = true)
            Box(
                modifier =
                    Modifier
                        .background(HeyMikaTheme.colors.primary, HeyMikaTheme.shapes.small)
                        .padding(8.dp),
            ) {
                UserAvatar(
                    initials = "RP",
                    size = dimens.avatarLg,
                    tone = tones.blue,
                    ringColor = HeyMikaTheme.colors.surface,
                    contentDescription = "Foto profil Rina Putri",
                )
            }
            UserAvatar(
                initials = "DF",
                size = dimens.avatarXl,
                imageUrl = "https://example.invalid/avatar.png",
            )
        }
    }
}
