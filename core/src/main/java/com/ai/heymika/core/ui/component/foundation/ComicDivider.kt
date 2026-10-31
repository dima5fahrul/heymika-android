package com.ai.heymika.core.ui.component.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * 3dp ink stroke: the ONLY divider allowed inside a Strict (comic) shell. Fixed uses: the auth
 * mascot banner/form seam, the score hero sections, the checkout total rule and the transaction
 * hero. Decorative, no semantics. A vertical divider needs a parent with a bounded height
 * (e.g. `Row(Modifier.height(IntrinsicSize.Min))`).
 */
@Composable
fun ComicDivider(
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    thickness: Dp = HeyMikaTheme.dimens.borderInk,
    color: Color = HeyMikaTheme.colors.ink,
) {
    DividerLine(modifier, orientation, thickness, color)
}

/**
 * 1.5dp borderMuted stroke for Subtle cards, list rows, forms, dialogs and bottom sheets
 * (DESIGN.md "Interior Tonal Stacking"). Decorative, no semantics.
 */
@Composable
fun SubtleDivider(
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    thickness: Dp = HeyMikaTheme.dimens.borderSubtle,
    color: Color = HeyMikaTheme.colors.borderMuted,
) {
    DividerLine(modifier, orientation, thickness, color)
}

@Composable
private fun DividerLine(
    modifier: Modifier,
    orientation: Orientation,
    thickness: Dp,
    color: Color,
) {
    val sized =
        when (orientation) {
            Orientation.Horizontal -> modifier.fillMaxWidth().height(thickness)
            Orientation.Vertical -> modifier.fillMaxHeight().width(thickness)
        }
    Box(modifier = sized.background(color))
}

@Preview(name = "Dividers", showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360)
@Composable
private fun DividerPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "ComicDivider", style = HeyMikaTheme.typography.labelMd)
            ComicDivider()
            Text(text = "SubtleDivider", style = HeyMikaTheme.typography.labelMd)
            SubtleDivider()
            Row(
                modifier = Modifier.height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(text = "Kiri")
                ComicDivider(orientation = Orientation.Vertical)
                Text(text = "Tengah")
                SubtleDivider(orientation = Orientation.Vertical)
                Text(text = "Kanan")
            }
        }
    }
}
