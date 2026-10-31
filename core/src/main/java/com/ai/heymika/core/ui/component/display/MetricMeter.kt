package com.ai.heymika.core.ui.component.display

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import kotlin.math.roundToInt

/**
 * A compact one-line meter, such as the Beranda "Target Harian: 25/30 menit" strip. It shows an
 * [icon] tile ([iconTone]), a small muted [label] above a bold [value], an optional 64dp mini
 * [progress] bar in [progressColor], and an optional [status] pill ("Hampir!", [statusTone]).
 *
 * It is one TalkBack item: "label, value, status", with the progress percent as its state.
 */
@Composable
fun MetricMeter(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Timer,
    iconTone: DisplayTone = DisplayTone.Primary,
    status: String? = null,
    statusTone: DisplayTone = DisplayTone.Success,
    progress: Float? = null,
    progressColor: Color = HeyMikaTheme.colors.primary,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val typography = HeyMikaTheme.typography
    val shape = HeyMikaTheme.shapes.input
    val percent =
        progress?.let {
            val fraction = if (it.isNaN()) 0f else it.coerceIn(0f, 1f)
            stringResource(R.string.display_percent, (fraction * METER_PERCENT).roundToInt())
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    if (percent != null) stateDescription = percent
                }.background(colors.surface, shape)
                .border(dimens.borderSubtle, colors.borderMuted, shape)
                .padding(horizontal = dimens.spaceSmPlus, vertical = dimens.spaceSm),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatIconTile(icon = icon, tone = iconTone)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = colors.textMuted,
                style = typography.caption.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                color = colors.textPrimary,
                style = typography.labelLg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (progress != null) {
            LinearProgress(
                progress = progress,
                modifier =
                    Modifier
                        .width(MiniMeterWidth)
                        .clearAndSetSemantics {},
                color = progressColor,
                style = LinearProgressStyle.Plain,
            )
        }
        if (status != null) {
            Pill(
                text = status,
                tone = statusTone,
                size = PillSize.Small,
            )
        }
    }
}

private val MiniMeterWidth = 64.dp
private const val METER_PERCENT = 100

@Preview(name = "MetricMeter", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun MetricMeterPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricMeter(
                label = "Target Harian",
                value = "25/30 menit",
                status = "Hampir!",
                progress = 25f / 30f,
            )
            MetricMeter(
                label = "Streak Belajar",
                value = "7 hari",
                icon = Icons.Outlined.LocalFireDepartment,
                iconTone = DisplayTone.Orange,
                status = "Rekor",
                statusTone = DisplayTone.Orange,
            )
            MetricMeter(
                label = "Target Harian",
                value = "5/30 menit",
                progress = 5f / 30f,
                progressColor = HeyMikaTheme.colors.accent,
                status = "Ayo mulai",
                statusTone = DisplayTone.Neutral,
            )
        }
    }
}
