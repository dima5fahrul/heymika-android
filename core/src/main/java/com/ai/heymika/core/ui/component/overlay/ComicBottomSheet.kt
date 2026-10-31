package com.ai.heymika.core.ui.component.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.SubtleDivider
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import kotlinx.coroutines.launch

/**
 * A Material3 ModalBottomSheet in comic dress: white, 28dp top corners with a 3dp ink edge along
 * the top and sides, an ink drag handle, and no tonal elevation. [title] becomes a headlineSm
 * heading above [content]; content is padded by the screen margin and sits above the navigation
 * bar and keyboard ([windowInsets]). By default it opens fully expanded, so short action sheets never
 * hide their last button below the screen edge; pass a [sheetState] to allow a half-open state.
 *
 * The drag handle keeps the Material accessibility actions (dismiss, expand, collapse), so
 * TalkBack users can operate the sheet without dragging.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    title: String? = null,
    windowInsets: WindowInsets = BottomSheetDefaults.windowInsets,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = HeyMikaTheme.colors
    val scope = rememberCoroutineScope()
    val dismissLabel = stringResource(R.string.chrome_sheet_dismiss)
    val expandLabel = stringResource(R.string.chrome_sheet_expand)
    val collapseLabel = stringResource(R.string.chrome_sheet_collapse)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = HeyMikaTheme.shapes.sheet,
        containerColor = colors.surface,
        contentColor = colors.textPrimary,
        tonalElevation = 0.dp,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        ComicSheetFrame(
            title = title,
            windowInsets = windowInsets,
            handleSemantics = {
                dismiss(dismissLabel) {
                    scope
                        .launch { sheetState.hide() }
                        .invokeOnCompletion { if (!sheetState.isVisible) onDismissRequest() }
                    true
                }
                if (sheetState.currentValue == SheetValue.PartiallyExpanded) {
                    expand(expandLabel) {
                        scope.launch { sheetState.expand() }
                        true
                    }
                } else if (sheetState.hasPartiallyExpandedState) {
                    collapse(collapseLabel) {
                        scope.launch { sheetState.partialExpand() }
                        true
                    }
                }
            },
            content = content,
        )
    }
}

/**
 * Everything inside the sheet: the comic edge, the drag handle and the padded content. It fills
 * the whole sheet (the Material drag handle and content insets are turned off), so the edge it
 * draws lines up with the sheet's outline.
 */
@Composable
internal fun ComicSheetFrame(
    title: String?,
    windowInsets: WindowInsets,
    handleSemantics: SemanticsPropertyReceiver.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val shapes = HeyMikaTheme.shapes
    val handleLabel = stringResource(R.string.chrome_sheet_handle)
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .comicSheetEdge(shape = shapes.sheet, color = colors.ink, width = dimens.borderInk)
                .windowInsetsPadding(windowInsets),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .semantics(mergeDescendants = true) {
                        contentDescription = handleLabel
                        handleSemantics()
                    }.padding(vertical = (dimens.minTouchTarget - dimens.spaceXs) / 2)
                    .size(width = dimens.spaceXl, height = dimens.spaceXs)
                    .background(colors.ink, shapes.pill),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = dimens.screenMargin, end = dimens.screenMargin, bottom = dimens.spaceLg),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceMd),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = HeyMikaTheme.typography.headlineSm,
                    color = colors.textPrimary,
                    modifier = Modifier.semantics { heading() },
                )
            }
            content()
        }
    }
}

/**
 * Draws the ink edge of the sheet along its top corners and sides. The outline continues below
 * the frame so the bottom edge never shows; the stroke is doubled and centred on the edge, and the
 * sheet clips its outer half to the sheet shape, leaving exactly [width] inside.
 */
private fun Modifier.comicSheetEdge(
    shape: Shape,
    color: Color,
    width: Dp,
): Modifier =
    drawWithCache {
        val stroke = width.toPx()
        val outline = shape.createOutline(Size(size.width, size.height + stroke * 2), layoutDirection, this)
        onDrawWithContent {
            drawContent()
            drawOutline(outline = outline, color = color, style = Stroke(width = stroke * 2))
        }
    }

@Preview(name = "Sheet with title", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicBottomSheetPreview() {
    HeyMikaTheme {
        // The real sheet lives in its own window; this renders the same frame inside the sheet clip.
        Column(
            modifier =
                Modifier
                    .padding(top = 24.dp)
                    .clip(HeyMikaTheme.shapes.sheet)
                    .background(HeyMikaTheme.colors.surface),
        ) {
            ComicSheetFrame(
                title = "Urutkan materi",
                windowInsets = WindowInsets(0, 0, 0, 0),
                handleSemantics = {},
            ) {
                Text(text = "Terbaru dipelajari", style = HeyMikaTheme.typography.labelLg)
                SubtleDivider()
                Text(text = "Abjad A-Z", style = HeyMikaTheme.typography.labelLg)
                SubtleDivider()
                Text(text = "Progres terendah", style = HeyMikaTheme.typography.labelLg)
                ComicButton(text = "Terapkan", onClick = {}, fullWidth = true)
            }
        }
    }
}

@Preview(name = "Sheet without title", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ComicBottomSheetUntitledPreview() {
    HeyMikaTheme {
        Column(
            modifier =
                Modifier
                    .padding(top = 24.dp)
                    .clip(HeyMikaTheme.shapes.sheet)
                    .background(HeyMikaTheme.colors.surface),
        ) {
            ComicSheetFrame(title = null, windowInsets = WindowInsets(0, 0, 0, 0), handleSemantics = {}) {
                Text(
                    text = "Pilih workspace tujuan untuk materi baru.",
                    style = HeyMikaTheme.typography.bodyMd,
                )
            }
        }
    }
}
