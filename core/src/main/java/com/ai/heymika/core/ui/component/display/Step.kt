package com.ai.heymika.core.ui.component.display

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/** The state of one step in a [StepIndicator] or a [TimelineStep]. */
enum class StepStatus { Pending, Active, Done, Failed }

/**
 * A compact horizontal stepper: numbered 24dp markers joined by 2dp lines, such as onboarding
 * "Langkah 1 dari 3" or checkout "1 - 2".
 * - A Done marker is a success check; its line to the next step turns success.
 * - The Active marker is primary with a soft halo.
 * - A Pending marker is a muted outline.
 * - A Failed marker is a danger cross.
 *
 * TalkBack reads one summary ("Langkah 2 dari 3, Berjalan") instead of every marker.
 */
@Composable
fun StepIndicator(
    steps: List<StepStatus>,
    modifier: Modifier = Modifier,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val currentIndex =
        steps
            .indexOfFirst { it == StepStatus.Active || it == StepStatus.Failed }
            .takeIf { it >= 0 }
            ?: steps.count { it == StepStatus.Done }.coerceAtMost(steps.lastIndex)
    val position = stringResource(R.string.display_step_position, currentIndex + 1, steps.size)
    val status = steps.getOrNull(currentIndex)?.let { stepStatusLabel(it) }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clearAndSetSemantics {
                    contentDescription = position
                    if (status != null) stateDescription = status
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        steps.forEachIndexed { index, step ->
            StepMarker(number = index + 1, status = step)
            if (index < steps.lastIndex) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(horizontal = dimens.spaceXs)
                            .height(dimens.borderControl)
                            .background(if (step == StepStatus.Done) colors.success else colors.borderMuted),
                )
            }
        }
    }
}

/**
 * One row of a vertical processing timeline ("Status Pemrosesan"): a status marker, a bold [title]
 * with an uppercase status tag, an optional mono [meta] (a timestamp) and a [description]. The
 * optional [content] slot holds extras such as a mini progress bar under the active step.
 *
 * - The Active step sits on a tinted panel.
 * - Pending steps are muted.
 * - A 2dp connector runs down to the next row while [showConnector] is set; set it to false on the
 *   last step.
 *
 * TalkBack reads the step as one item: "Langkah n, title, meta, description", with the status as its
 * state.
 */
@Composable
fun TimelineStep(
    number: Int,
    title: String,
    status: StepStatus,
    modifier: Modifier = Modifier,
    description: String? = null,
    meta: String? = null,
    showConnector: Boolean = true,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val typography = HeyMikaTheme.typography
    val statusLabel = stepStatusLabel(status)
    val numberLabel = stringResource(R.string.display_step_number, number)
    val markerSize = dimens.iconSizeLarge
    val connectorColor = if (status == StepStatus.Done) colors.success else colors.borderMuted
    val connectorWidth = dimens.borderControl
    val connectorGap = dimens.spaceXs
    val active = status == StepStatus.Active
    val panel =
        if (active) {
            Modifier
                .background(PillDefaults.toneSpec(DisplayTone.Primary).container, HeyMikaTheme.shapes.input)
                .border(dimens.borderSubtle, colors.primaryFixedDim, HeyMikaTheme.shapes.input)
                .padding(dimens.spaceSm)
        } else {
            Modifier
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { stateDescription = statusLabel }
                .drawBehind {
                    if (showConnector) {
                        val half = markerSize.toPx() / 2f
                        val x = if (layoutDirection == LayoutDirection.Rtl) size.width - half else half
                        drawLine(
                            color = connectorColor,
                            start = Offset(x, markerSize.toPx() + connectorGap.toPx()),
                            end = Offset(x, size.height),
                            strokeWidth = connectorWidth.toPx(),
                        )
                    }
                }.padding(bottom = if (showConnector) dimens.spaceMd else 0.dp),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
    ) {
        StepMarker(
            number = number,
            status = status,
            modifier = Modifier.clearAndSetSemantics { contentDescription = numberLabel },
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .then(panel),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceXxs),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs + dimens.spaceXxs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f, fill = false),
                        color =
                            when (status) {
                                StepStatus.Pending -> colors.textMuted
                                StepStatus.Active -> colors.primaryText
                                StepStatus.Done -> colors.textPrimary
                                StepStatus.Failed -> colors.danger
                            },
                        style = typography.labelMd,
                    )
                    StepStatusTag(status = status, label = statusLabel)
                }
                if (meta != null) {
                    Text(
                        text = meta,
                        color = if (active) colors.primaryText else colors.textMuted,
                        style =
                            typography.mono.copy(
                                fontSize = typography.caption.fontSize,
                                lineHeight = typography.caption.lineHeight,
                            ),
                        maxLines = 1,
                    )
                }
            }
            if (description != null) {
                Text(
                    text = description,
                    color =
                        if (status == StepStatus.Pending) {
                            colors.textMuted
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    style = typography.bodySm,
                )
            }
            if (content != null) {
                Column(
                    modifier = Modifier.padding(top = dimens.spaceXs),
                    content = content,
                )
            }
        }
    }
}

@Composable
private fun StepMarker(
    number: Int,
    status: StepStatus,
    modifier: Modifier = Modifier,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val shape = HeyMikaTheme.shapes.pill
    val container =
        when (status) {
            StepStatus.Pending -> MaterialTheme.colorScheme.surfaceContainer
            StepStatus.Active -> colors.primary
            StepStatus.Done -> colors.success
            StepStatus.Failed -> colors.danger
        }
    val content = if (status == StepStatus.Pending) colors.textMuted else colors.surface
    val halo = colors.primaryFixed
    val haloWidth = dimens.spaceXs
    Box(
        modifier =
            modifier
                .drawBehind {
                    if (status == StepStatus.Active) {
                        drawCircle(color = halo, radius = size.minDimension / 2f + haloWidth.toPx())
                    }
                }.size(dimens.iconSizeLarge)
                .background(container, shape)
                .then(
                    if (status == StepStatus.Pending) {
                        Modifier.border(dimens.borderSubtle, colors.borderMuted, shape)
                    } else {
                        Modifier
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        when (status) {
            StepStatus.Done, StepStatus.Failed -> {
                Icon(
                    imageVector = if (status == StepStatus.Done) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(dimens.iconSizeSmall),
                    tint = content,
                )
            }

            StepStatus.Pending, StepStatus.Active -> {
                Text(
                    text = number.toString(),
                    color = content,
                    style = HeyMikaTheme.typography.labelSm,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

@Composable
private fun StepStatusTag(
    status: StepStatus,
    label: String,
) {
    Tag(
        text = label,
        modifier = Modifier.clearAndSetSemantics {},
        tone =
            when (status) {
                StepStatus.Pending -> DisplayTone.Neutral
                StepStatus.Active -> DisplayTone.Primary
                StepStatus.Done -> DisplayTone.Success
                StepStatus.Failed -> DisplayTone.Danger
            },
        style = if (status == StepStatus.Active) PillStyle.Filled else PillStyle.Tinted,
        uppercase = true,
    )
}

@Composable
@ReadOnlyComposable
private fun stepStatusLabel(status: StepStatus): String =
    stringResource(
        when (status) {
            StepStatus.Pending -> R.string.display_step_pending
            StepStatus.Active -> R.string.display_step_active
            StepStatus.Done -> R.string.display_step_done
            StepStatus.Failed -> R.string.display_step_failed
        },
    )

@Preview(name = "StepIndicator", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun StepIndicatorPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            StepIndicator(steps = listOf(StepStatus.Active, StepStatus.Pending, StepStatus.Pending))
            StepIndicator(steps = listOf(StepStatus.Done, StepStatus.Active, StepStatus.Pending))
            StepIndicator(steps = listOf(StepStatus.Done, StepStatus.Done, StepStatus.Failed))
            StepIndicator(steps = listOf(StepStatus.Done, StepStatus.Done))
        }
    }
}

@Preview(name = "TimelineStep", showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360)
@Composable
private fun TimelineStepPreview() {
    HeyMikaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            TimelineStep(
                number = 1,
                title = "Unggah File",
                status = StepStatus.Done,
                meta = "14:32:05",
                description = "File 8.4 MB berhasil diterima dan diamankan.",
            )
            TimelineStep(
                number = 2,
                title = "Indexing & Pemetaan Konsep",
                status = StepStatus.Active,
                meta = "14:34:10",
                description = "Menghubungkan istilah kunci ke memori Mika...",
            ) {
                LinearProgress(progress = 0.75f, style = LinearProgressStyle.Plain, color = HeyMikaTheme.colors.accent)
            }
            TimelineStep(
                number = 3,
                title = "Penyusunan Ringkasan & Flashcard",
                status = StepStatus.Pending,
                meta = "--:--",
                description = "Ringkasan eksekutif, 15 flashcard, dan 5 kuis adaptif.",
            )
            TimelineStep(
                number = 4,
                title = "Validasi Format",
                status = StepStatus.Failed,
                description = "Halaman 12 tidak terbaca. Coba unggah ulang.",
                showConnector = false,
            )
        }
    }
}
