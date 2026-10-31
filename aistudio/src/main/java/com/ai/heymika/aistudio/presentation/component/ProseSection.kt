package com.ai.heymika.aistudio.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ai.heymika.aistudio.R
import com.ai.heymika.core.domain.model.GeneratedContent

/**
 * Renders SUMMARY and STORY output - and QUIZ output that could not be read as questions,
 * in which case [unparsableQuiz] explains why the raw text is being shown.
 */
internal fun LazyListScope.proseSection(
    content: GeneratedContent,
    unparsableQuiz: Boolean,
) {
    if (unparsableQuiz) {
        item(key = "prose-warning") {
            Text(
                text = stringResource(R.string.aistudio_quiz_unparsable),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    item(key = "prose-${content.id}") {
        ProseCard(body = content.body)
    }
}

@Composable
private fun ProseCard(
    body: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        SelectionContainer {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val paragraphs =
                    body
                        .trim()
                        .split(paragraphDelimiter)
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                if (paragraphs.isEmpty()) {
                    Text(
                        text = stringResource(R.string.aistudio_empty_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    return@Column
                }
                paragraphs.forEach { paragraph ->
                    Text(
                        text = paragraph,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

private val paragraphDelimiter = Regex("""\n\s*\n""")
