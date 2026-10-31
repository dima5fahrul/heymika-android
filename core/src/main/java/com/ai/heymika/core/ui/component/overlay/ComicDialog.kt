package com.ai.heymika.core.ui.component.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.ComicButtonSize
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.modifier.comicSurface
import com.ai.heymika.core.ui.theme.HeyMikaTheme

object ComicDialogDefaults {
    /** Full width minus the screen margins: the comic shell draws its own frame and shadow. */
    val properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false)

    /** [properties] with back press and outside taps switched off while [loading]. */
    fun lockedWhileLoading(
        properties: DialogProperties,
        loading: Boolean,
    ): DialogProperties =
        if (!loading) {
            properties
        } else {
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                securePolicy = properties.securePolicy,
                usePlatformDefaultWidth = properties.usePlatformDefaultWidth,
                decorFitsSystemWindows = properties.decorFitsSystemWindows,
            )
        }
}

/**
 * A confirmation dialog in a Strict comic shell (3dp ink border, orange hard shadow, 28dp
 * corners): optional [mascot] on top, a centred [title] heading, an optional [body], an optional
 * [content] slot, then the confirm button above the dismiss button, both full width.
 *
 * Use [confirmVariant] Danger for destructive actions (delete account, log out everywhere) and
 * Secondary for celebratory ones (submit quiz). [dismissLabel] null hides the dismiss button.
 * While [confirmLoading] the confirm button spins, the dismiss button is disabled and back press
 * or an outside tap no longer dismisses the dialog. Long content scrolls inside the shell.
 */
@Composable
fun ComicDialog(
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    body: String? = null,
    dismissLabel: String? = stringResource(R.string.chrome_dialog_cancel),
    onDismiss: () -> Unit = onDismissRequest,
    confirmVariant: ComicButtonVariant = ComicButtonVariant.Primary,
    dismissVariant: ComicButtonVariant = ComicButtonVariant.Outline,
    confirmEnabled: Boolean = true,
    confirmLoading: Boolean = false,
    confirmLoadingLabel: String? = null,
    mascot: (@Composable () -> Unit)? = null,
    properties: DialogProperties = ComicDialogDefaults.properties,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = { if (!confirmLoading) onDismissRequest() },
        properties = ComicDialogDefaults.lockedWhileLoading(properties, confirmLoading),
    ) {
        ComicDialogContent(
            title = title,
            confirmLabel = confirmLabel,
            onConfirm = onConfirm,
            modifier = modifier,
            body = body,
            dismissLabel = dismissLabel,
            onDismiss = onDismiss,
            confirmVariant = confirmVariant,
            dismissVariant = dismissVariant,
            confirmEnabled = confirmEnabled,
            confirmLoading = confirmLoading,
            confirmLoadingLabel = confirmLoadingLabel,
            mascot = mascot,
            content = content,
        )
    }
}

/**
 * The bare comic dialog shell for custom layouts (the time and date pickers). [paneTitle] is what
 * TalkBack announces when the dialog opens. Turn [scrollable] off when [content] scrolls itself
 * vertically.
 */
@Composable
fun BasicComicDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    paneTitle: String? = null,
    properties: DialogProperties = ComicDialogDefaults.properties,
    scrollable: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(HeyMikaTheme.dimens.shellPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        ComicDialogSurface(
            modifier = modifier,
            paneTitle = paneTitle,
            scrollable = scrollable,
            contentPadding = contentPadding,
            content = content,
        )
    }
}

/** The comic shell without a window, so previews and the gallery can render it inline. */
@Composable
internal fun ComicDialogSurface(
    modifier: Modifier = Modifier,
    paneTitle: String? = null,
    scrollable: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(HeyMikaTheme.dimens.shellPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.dialog
    val scrollState = rememberScrollState()
    val pane = paneTitle
    Column(
        modifier =
            modifier
                .padding(horizontal = dimens.screenMargin, vertical = dimens.spaceLg)
                .fillMaxWidth()
                .comicSurface(shape = shape)
                .clip(shape)
                .semantics { if (pane != null) this.paneTitle = pane }
                .then(if (scrollable) Modifier.verticalScroll(scrollState) else Modifier)
                .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.spaceMd),
        content = content,
    )
}

@Composable
internal fun ComicDialogContent(
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    body: String? = null,
    dismissLabel: String? = null,
    onDismiss: () -> Unit = {},
    confirmVariant: ComicButtonVariant = ComicButtonVariant.Primary,
    dismissVariant: ComicButtonVariant = ComicButtonVariant.Outline,
    confirmEnabled: Boolean = true,
    confirmLoading: Boolean = false,
    confirmLoadingLabel: String? = null,
    mascot: (@Composable () -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    ComicDialogSurface(modifier = modifier, paneTitle = title) {
        mascot?.invoke()
        Text(
            text = title,
            style = HeyMikaTheme.typography.headlineSm,
            color = HeyMikaTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
        )
        if (body != null) {
            Text(
                text = body,
                style = HeyMikaTheme.typography.bodyMd,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        content?.invoke(this)
        ComicDialogButtons(
            confirmLabel = confirmLabel,
            onConfirm = onConfirm,
            modifier = Modifier.padding(top = HeyMikaTheme.dimens.spaceXs),
            dismissLabel = dismissLabel,
            onDismiss = onDismiss,
            confirmVariant = confirmVariant,
            dismissVariant = dismissVariant,
            confirmEnabled = confirmEnabled,
            confirmLoading = confirmLoading,
            confirmLoadingLabel = confirmLoadingLabel,
        )
    }
}

/**
 * Dialog actions. [stacked] puts confirm above dismiss at full width (default, room for long
 * Indonesian labels); otherwise dismiss and confirm share one row (pickers), confirm on the end.
 */
@Composable
internal fun ComicDialogButtons(
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String? = null,
    onDismiss: () -> Unit = {},
    confirmVariant: ComicButtonVariant = ComicButtonVariant.Primary,
    dismissVariant: ComicButtonVariant = ComicButtonVariant.Outline,
    confirmEnabled: Boolean = true,
    confirmLoading: Boolean = false,
    confirmLoadingLabel: String? = null,
    stacked: Boolean = true,
) {
    val spacing = HeyMikaTheme.dimens.spaceSm
    if (stacked) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            ComicButton(
                text = confirmLabel,
                onClick = onConfirm,
                variant = confirmVariant,
                enabled = confirmEnabled,
                loading = confirmLoading,
                loadingText = confirmLoadingLabel,
                fullWidth = true,
            )
            if (dismissLabel != null) {
                ComicButton(
                    text = dismissLabel,
                    onClick = onDismiss,
                    variant = dismissVariant,
                    enabled = !confirmLoading,
                    fullWidth = true,
                )
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            if (dismissLabel != null) {
                ComicButton(
                    text = dismissLabel,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    variant = dismissVariant,
                    size = ComicButtonSize.Medium,
                    enabled = !confirmLoading,
                    fullWidth = true,
                )
            }
            ComicButton(
                text = confirmLabel,
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                variant = confirmVariant,
                size = ComicButtonSize.Medium,
                enabled = confirmEnabled,
                loading = confirmLoading,
                loadingText = confirmLoadingLabel,
                fullWidth = true,
            )
        }
    }
}

@Composable
private fun PreviewMascot() {
    Box(
        modifier =
            Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(HeyMikaTheme.colors.yellow),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Pets,
            contentDescription = null,
            tint = HeyMikaTheme.colors.ink,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Preview(name = "Confirm with mascot", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicDialogPreview() {
    HeyMikaTheme {
        ComicDialogContent(
            title = "Kumpulkan kuis sekarang?",
            body = "Kamu sudah menjawab 7 dari 10 soal. Soal yang belum dijawab dihitung salah.",
            confirmLabel = "Ya, Kumpulkan",
            onConfirm = {},
            dismissLabel = "Lanjut Mengerjakan",
            confirmVariant = ComicButtonVariant.Secondary,
            mascot = { PreviewMascot() },
        )
    }
}

@Preview(name = "Destructive and loading", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicDialogDangerPreview() {
    HeyMikaTheme {
        Column {
            ComicDialogContent(
                title = "Keluar dari semua perangkat?",
                body = "Sesi di perangkat lain akan berakhir dan perlu masuk lagi.",
                confirmLabel = "Keluar Semua",
                onConfirm = {},
                dismissLabel = "Batal",
                confirmVariant = ComicButtonVariant.Danger,
            )
            ComicDialogContent(
                title = "Hapus akun?",
                confirmLabel = "Hapus Akun",
                onConfirm = {},
                dismissLabel = "Batal",
                confirmVariant = ComicButtonVariant.Danger,
                confirmLoading = true,
                confirmLoadingLabel = "Menghapus…",
            )
        }
    }
}

@Preview(name = "Custom content, row buttons", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun BasicComicDialogPreview() {
    HeyMikaTheme {
        ComicDialogSurface(paneTitle = "Ganti email") {
            Text(
                text = "Ganti email",
                style = HeyMikaTheme.typography.headlineSm,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = "Tautan verifikasi baru akan dikirim ke alamat yang baru.",
                style = HeyMikaTheme.typography.bodySm,
                textAlign = TextAlign.Center,
            )
            ComicDialogButtons(
                confirmLabel = "Kirim Tautan",
                onConfirm = {},
                dismissLabel = "Batal",
                stacked = false,
            )
        }
    }
}
