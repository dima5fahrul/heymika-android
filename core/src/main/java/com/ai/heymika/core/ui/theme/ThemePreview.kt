package com.ai.heymika.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "Type scale", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun TypeScalePreview() {
    HeyMikaTheme {
        val type = HeyMikaTheme.typography
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf<Pair<String, TextStyle>>(
                "displayHero 32" to type.displayHero,
                "headlineLg 26" to type.headlineLg,
                "headlineMd 24" to type.headlineMd,
                "headlineSm 20" to type.headlineSm,
                "sectionTitle 20" to type.sectionTitle,
                "bodyLg 18" to type.bodyLg,
                "bodyMd 16" to type.bodyMd,
                "bodySm 14" to type.bodySm,
                "labelLg 16" to type.labelLg,
                "labelMd 14" to type.labelMd,
                "LABELSM 12" to type.labelSm,
                "OVERLINE 12" to type.overline,
                "caption 12" to type.caption,
                "00:59 mono" to type.mono,
            ).forEach { (label, style) -> Text(text = label, style = style) }
        }
    }
}

@Preview(name = "Colour tokens and tones", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ColorTokensPreview() {
    HeyMikaTheme {
        val c = HeyMikaTheme.colors
        val t = HeyMikaTheme.tones
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SwatchRow(listOf(c.ink, c.primary, c.primaryText, c.primaryPressShadow, c.accent, c.accentPressShadow))
            SwatchRow(listOf(c.sky, c.yellow, c.ribbonRed, c.badgeCyan, c.canvas, c.borderMuted))
            SwatchRow(listOf(c.textMuted, c.danger, c.dangerBg, c.success, c.successBg, c.warningBg))
            listOf(t.blue, t.teal, t.orange, t.emerald, t.rose, t.purple, t.slate, t.sky, t.yellow).forEach { tone ->
                Row(
                    modifier = Modifier.background(tone.container).padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.size(10.dp).background(tone.accent))
                    Text(text = "Tone teks", color = tone.content, style = HeyMikaTheme.typography.labelMd)
                }
            }
        }
    }
}

@Composable
private fun SwatchRow(colors: List<Color>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.forEach { color ->
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(color)
                        .border(1.dp, HeyMikaTheme.colors.borderMuted),
            )
        }
    }
}
