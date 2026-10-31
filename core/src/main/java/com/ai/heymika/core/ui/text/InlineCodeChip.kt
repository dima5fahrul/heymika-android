package com.ai.heymika.core.ui.text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.domain.text.MarkupTone
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * Inline code on a tag-shaped tint: bold monospace in primaryText, or in the [tone] colour (the
 * workspace mockup shows a slower complexity such as `O(n)` in danger). Decorative container, plain
 * text semantics; [RichText] embeds it as inline content and clears its semantics there, because the
 * paragraph already reads the code.
 */
@Composable
fun InlineCodeChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: MarkupTone? = null,
    textStyle: TextStyle = HeyMikaTheme.typography.mono,
    colors: RichTextColors = RichTextDefaults.colors(),
) {
    Box(
        modifier =
            modifier
                .background(color = colors.codeContainer, shape = HeyMikaTheme.shapes.tag)
                .padding(horizontal = HeyMikaTheme.dimens.spaceXs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = tone?.let(colors::tone) ?: colors.codeContent,
            style = textStyle,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
        )
    }
}

@Preview(name = "Inline code chips", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun InlineCodeChipPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InlineCodeChip(text = "O(log n)")
                InlineCodeChip(text = "[45, 20, 60]")
                InlineCodeChip(text = "/verify-email")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InlineCodeChip(text = "O(n)", tone = MarkupTone.DANGER)
                InlineCodeChip(text = "+2 ATP", tone = MarkupTone.SUCCESS)
                InlineCodeChip(text = "-1 ATP", tone = MarkupTone.WARNING)
                InlineCodeChip(text = "PFK-1", tone = MarkupTone.PRIMARY)
            }
        }
    }
}
