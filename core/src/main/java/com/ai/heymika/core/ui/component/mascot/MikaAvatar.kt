package com.ai.heymika.core.ui.component.mascot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/** How [MikaAvatar] frames the character. */
enum class MikaAvatarFrame {
    /** The bare character, for callouts and heroes that place Mika next to a speech bubble. */
    None,

    /** A tinted circle with an ink ring: avatars, state heroes, the chat header. */
    Circle,

    /** A tinted 16dp tile with an ink ring: list rows and cards. */
    RoundedTile,
}

/**
 * The single pose -> art mapping. Today every pose is a placeholder vector; when the real
 * illustrations land, return `painterResource(R.drawable.mika_<pose>)` here and nothing else
 * changes.
 */
@Composable
fun rememberMikaPainter(pose: MikaPose): Painter {
    val colors = HeyMikaTheme.colors
    val vector = remember(pose, colors) { MikaPlaceholderArt.build(pose, colors) }
    return rememberVectorPainter(vector)
}

/**
 * Mika in a given [pose], [size] square.
 *
 * The avatar is decorative by default (a speech bubble or title next to it carries the message),
 * so TalkBack skips it, badge included. Pass [contentDescription], for example
 * `stringResource(pose.contentDescriptionRes())`, when Mika stands alone. [badge] is a short
 * sticker such as "nyaa~" or "Lv.12" pinned to the bottom-end corner.
 */
@Composable
fun MikaAvatar(
    pose: MikaPose,
    modifier: Modifier = Modifier,
    size: Dp = HeyMikaTheme.dimens.avatarXl,
    frame: MikaAvatarFrame = MikaAvatarFrame.Circle,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    badge: String? = null,
    contentDescription: String? = null,
) {
    val dimens = HeyMikaTheme.dimens
    val shape =
        when (frame) {
            MikaAvatarFrame.None -> RectangleShape
            MikaAvatarFrame.Circle -> HeyMikaTheme.shapes.pill
            MikaAvatarFrame.RoundedTile -> HeyMikaTheme.shapes.card
        }
    val framed = frame != MikaAvatarFrame.None
    val ringWidth = if (size >= dimens.avatarXxl) dimens.borderInk else dimens.borderControl
    val semantics =
        if (contentDescription == null) {
            Modifier.clearAndSetSemantics {}
        } else {
            Modifier.semantics {
                this.contentDescription = contentDescription
                role = Role.Image
            }
        }
    Box(modifier = modifier.size(size).then(semantics)) {
        val face =
            if (framed) {
                Modifier
                    .clip(shape)
                    .background(containerColor, shape)
                    .border(ringWidth, HeyMikaTheme.colors.ink, shape)
                    .padding(size * FRAME_INSET)
            } else {
                Modifier
            }
        Image(
            painter = rememberMikaPainter(pose),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().then(face),
        )
        if (badge != null) {
            MikaAvatarBadge(text = badge, modifier = Modifier.align(Alignment.BottomEnd))
        }
    }
}

@Composable
private fun MikaAvatarBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.pill
    Text(
        text = text,
        modifier =
            modifier
                .background(HeyMikaTheme.colors.yellow, shape)
                .border(dimens.borderSubtle, HeyMikaTheme.colors.ink, shape)
                .padding(horizontal = dimens.spaceSm, vertical = dimens.spaceXxs),
        style = HeyMikaTheme.typography.labelSm,
        color = HeyMikaTheme.colors.onSecondaryFixed,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Inset of the art inside a frame, as a fraction of the avatar size, so props stay inside the circle. */
private const val FRAME_INSET = 0.1f

@Preview(name = "All poses", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun MikaAvatarPosesPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MikaPose.entries.chunked(4).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { pose ->
                        Column(
                            modifier = Modifier.width(72.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            MikaAvatar(pose = pose, size = HeyMikaTheme.dimens.avatarLg)
                            Text(
                                text = pose.name.lowercase(),
                                style = HeyMikaTheme.typography.caption,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Frames, sizes and badge", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun MikaAvatarFramesPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                MikaAvatar(pose = MikaPose.WAVING, size = HeyMikaTheme.dimens.avatarXs)
                MikaAvatar(pose = MikaPose.WAVING, size = HeyMikaTheme.dimens.avatarSm)
                MikaAvatar(pose = MikaPose.WAVING, size = HeyMikaTheme.dimens.avatarMd)
                MikaAvatar(
                    pose = MikaPose.HAPPY,
                    size = HeyMikaTheme.dimens.avatarLg,
                    frame = MikaAvatarFrame.RoundedTile,
                    containerColor = HeyMikaTheme.colors.badgeCyan,
                )
                MikaAvatar(
                    pose = MikaPose.WAVING_PENCIL,
                    size = HeyMikaTheme.dimens.avatarXl,
                    frame = MikaAvatarFrame.None,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MikaAvatar(
                    pose = MikaPose.CONFUSED_MAP,
                    size = HeyMikaTheme.dimens.avatarHero,
                    badge = stringResource(R.string.mascot_badge_nyaa),
                )
                MikaAvatar(
                    pose = MikaPose.TROPHY,
                    size = HeyMikaTheme.dimens.avatarXxl,
                    badge = "Lv.12",
                    contentDescription = stringResource(MikaPose.TROPHY.contentDescriptionRes()),
                )
            }
        }
    }
}
