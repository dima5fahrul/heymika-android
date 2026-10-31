package com.ai.heymika.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ai.heymika.core.R
import com.ai.heymika.core.domain.model.Material
import com.ai.heymika.core.ui.component.foundation.ComicButtonDefaults
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.component.foundation.ComicCard
import com.ai.heymika.core.ui.component.foundation.ComicCardVariant
import com.ai.heymika.core.ui.component.foundation.ComicIconButton
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * The one row used wherever legacy materials are listed, so the catalogue and the favourites
 * feature cannot drift apart visually. A Subtle [ComicCard] (list surface) whose whole face opens
 * the material; the heart is its own button. Replaced by the workspace cards in P3.
 */
@Composable
fun MaterialCard(
    material: Material,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = HeyMikaTheme.dimens
    val colors = HeyMikaTheme.colors
    val typography = HeyMikaTheme.typography
    ComicCard(
        modifier = modifier.fillMaxWidth(),
        variant = ComicCardVariant.Subtle,
        onClick = onClick,
        contentPadding = PaddingValues(dimens.cardPaddingCompact),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Cover(coverUrl = material.coverUrl, title = material.title)
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = dimens.spaceSmPlus),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
            ) {
                Text(
                    text = material.subject.uppercase(),
                    style = typography.labelSm,
                    color = colors.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = material.title,
                    style = typography.labelLg,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = material.description,
                    style = typography.bodySm,
                    color = colors.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(dimens.iconSizeSmall),
                    )
                    Text(
                        text = stringResource(R.string.core_read_duration, material.durationMinutes),
                        style = typography.caption,
                        color = colors.textMuted,
                    )
                }
            }
            FavoriteButton(
                isFavorite = material.isFavorite,
                title = material.title,
                onClick = onFavoriteClick,
            )
        }
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    title: String,
    onClick: () -> Unit,
) {
    val colors = HeyMikaTheme.colors
    val ghost = ComicButtonDefaults.colors(ComicButtonVariant.Ghost)
    ComicIconButton(
        icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
        contentDescription =
            stringResource(
                if (isFavorite) R.string.core_remove_from_favorites else R.string.core_add_to_favorites,
                title,
            ),
        onClick = onClick,
        colors = ghost.copy(content = if (isFavorite) colors.danger else colors.textMuted),
    )
}

@Composable
private fun Cover(
    coverUrl: String?,
    title: String,
) {
    val dimens = HeyMikaTheme.dimens
    val tone = HeyMikaTheme.tones.sky
    Box(
        modifier =
            Modifier
                .width(dimens.avatarLg)
                .height(dimens.avatarXl)
                .clip(HeyMikaTheme.shapes.button)
                .background(tone.container),
        contentAlignment = Alignment.Center,
    ) {
        if (coverUrl.isNullOrBlank()) {
            // No artwork from the backend: fall back to a glyph rather than a broken image,
            // which reads as a loading failure.
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                tint = tone.content,
                modifier = Modifier.size(dimens.iconSizeLarge),
            )
        } else {
            AsyncImage(
                model = coverUrl,
                contentDescription = stringResource(R.string.core_cover_of, title),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private val previewMaterial =
    Material(
        id = "1",
        title = "Fotosintesis dan siklus Calvin",
        subject = "Biologi",
        description = "Bagaimana tumbuhan mengubah cahaya menjadi gula, langkah demi langkah.",
        body = "",
        coverUrl = null,
        durationMinutes = 12,
        isFavorite = true,
    )

@Preview(showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun MaterialCardPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MaterialCard(material = previewMaterial, onClick = {}, onFavoriteClick = {})
            MaterialCard(
                material = previewMaterial.copy(id = "2", isFavorite = false, title = "Hukum Newton"),
                onClick = {},
                onFavoriteClick = {},
            )
        }
    }
}
