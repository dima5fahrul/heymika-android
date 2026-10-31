package com.ai.heymika.aistudio.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ai.heymika.aistudio.R
import com.ai.heymika.core.domain.model.GeneratedContent
import com.ai.heymika.core.domain.model.GenerationKind
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Everything the AI has already produced for this material, newest first. */
internal fun LazyListScope.historySection(
    history: List<GeneratedContent>,
    currentContentId: String?,
) {
    item(key = "history-title") {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = stringResource(R.string.aistudio_history_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
    if (history.isEmpty()) {
        item(key = "history-empty") {
            Text(
                text = stringResource(R.string.aistudio_history_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    items(items = history, key = { "history-${it.id}" }) { entry ->
        HistoryCard(
            entry = entry,
            isCurrent = entry.id == currentContentId,
        )
    }
}

@Composable
private fun HistoryCard(
    entry: GeneratedContent,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable(entry.id) { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            if (isCurrent) {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            } else {
                CardDefaults.cardColors()
            },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = { expanded = !expanded },
                    label = { Text(text = stringResource(entry.kind.labelRes())) },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entry.createdAt.asHistoryTimestamp(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text =
                            if (expanded) {
                                stringResource(R.string.aistudio_history_collapse)
                            } else {
                                stringResource(R.string.aistudio_history_expand)
                            },
                    )
                }
            }
            Text(
                text = entry.body.trim(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private const val COLLAPSED_MAX_LINES = 2

private val historyTimestampFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

private fun Long.asHistoryTimestamp(): String =
    Instant
        .ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(historyTimestampFormatter)

internal fun GenerationKind.labelRes(): Int =
    when (this) {
        GenerationKind.QUIZ -> R.string.aistudio_kind_quiz
        GenerationKind.SUMMARY -> R.string.aistudio_kind_summary
        GenerationKind.STORY -> R.string.aistudio_kind_story
    }
