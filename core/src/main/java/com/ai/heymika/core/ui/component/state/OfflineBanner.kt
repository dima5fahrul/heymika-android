package com.ai.heymika.core.ui.component.state

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.theme.HeyMikaMotion
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * A slim banner that slides in from the top while the device is offline ("Koneksi Internet
 * Terputus") and slides away when it reconnects; it snaps under reduced motion. TalkBack announces
 * it politely when it appears.
 *
 * It is stateless: HeyMikaRoot mounts it once above the NavHost and drives [visible] from the
 * ConnectivityObserver. It draws no window insets, so place it below the status bar. [onRetry]
 * adds a small "Coba Lagi" action.
 */
@Composable
fun OfflineBanner(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val reducedMotion = HeyMikaTheme.reducedMotion
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter =
            if (reducedMotion) {
                EnterTransition.None
            } else {
                expandVertically(tween(HeyMikaMotion.EXPAND_MILLIS), expandFrom = Alignment.Top) +
                    fadeIn(tween(HeyMikaMotion.EXPAND_MILLIS))
            },
        exit =
            if (reducedMotion) {
                ExitTransition.None
            } else {
                shrinkVertically(tween(HeyMikaMotion.EXPAND_MILLIS), shrinkTowards = Alignment.Top) +
                    fadeOut(tween(HeyMikaMotion.EXPAND_MILLIS))
            },
        label = "offlineBanner",
    ) {
        OfflineBannerContent(onRetry = onRetry)
    }
}

@Composable
private fun OfflineBannerContent(onRetry: (() -> Unit)?) {
    val dimens = HeyMikaTheme.dimens
    val tone = HeyMikaTheme.tones.sky
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(tone.container)
                .heightIn(min = dimens.minTouchTarget)
                .padding(horizontal = dimens.screenMargin, vertical = dimens.spaceXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
    ) {
        Icon(
            imageVector = Icons.Filled.WifiOff,
            contentDescription = null,
            tint = tone.content,
            modifier = Modifier.size(dimens.iconSize),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        ) {
            Text(
                text = stringResource(R.string.state_offline_title),
                style = HeyMikaTheme.typography.labelMd,
                color = tone.content,
            )
            Text(
                text = stringResource(R.string.state_offline_banner_body),
                style = HeyMikaTheme.typography.caption,
                color = tone.content,
            )
        }
        if (onRetry != null) {
            ComicButton(
                text = stringResource(R.string.state_action_retry),
                onClick = onRetry,
                variant = ComicButtonVariant.Ghost,
                size = ComicButtonSize.Small,
                leadingIcon = Icons.Filled.Sync,
            )
        }
    }
}

@Preview(name = "Visible, with and without retry", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun OfflineBannerPreview() {
    HeyMikaTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OfflineBanner(visible = true)
            OfflineBanner(visible = true, onRetry = {})
        }
    }
}

@Preview(name = "Hidden", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360, heightDp = 64)
@Composable
private fun OfflineBannerHiddenPreview() {
    HeyMikaTheme {
        OfflineBanner(visible = false, onRetry = {})
    }
}
