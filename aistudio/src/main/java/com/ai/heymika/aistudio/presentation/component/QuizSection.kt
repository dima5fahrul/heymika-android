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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ai.heymika.aistudio.R
import com.ai.heymika.core.domain.model.QuizQuestion

/**
 * Interactive quiz: one card per question, a single selectable option per question, a
 * per-question reveal and a running score.
 */
internal fun LazyListScope.quizSection(
    questions: List<QuizQuestion>,
    selectedAnswers: Map<String, Int>,
    revealedQuestions: Set<String>,
    correctCount: Int,
    answeredCount: Int,
    allRevealed: Boolean,
    onOptionSelected: (String, Int) -> Unit,
    onRevealAnswer: (String) -> Unit,
    onRevealAllAnswers: () -> Unit,
    onResetQuiz: () -> Unit,
) {
    item(key = "quiz-score") {
        QuizScoreCard(
            correctCount = correctCount,
            answeredCount = answeredCount,
            totalCount = questions.size,
            allRevealed = allRevealed,
            onRevealAllAnswers = onRevealAllAnswers,
            onResetQuiz = onResetQuiz,
        )
    }
    itemsIndexed(items = questions, key = { _, question -> question.id }) { index, question ->
        QuizQuestionCard(
            number = index + 1,
            question = question,
            selectedIndex = selectedAnswers[question.id],
            isRevealed = question.id in revealedQuestions,
            onOptionSelected = { optionIndex -> onOptionSelected(question.id, optionIndex) },
            onRevealAnswer = { onRevealAnswer(question.id) },
        )
    }
}

@Composable
private fun QuizScoreCard(
    correctCount: Int,
    answeredCount: Int,
    totalCount: Int,
    allRevealed: Boolean,
    onRevealAllAnswers: () -> Unit,
    onResetQuiz: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.aistudio_quiz_score, correctCount, totalCount),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.aistudio_quiz_progress, answeredCount, totalCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = {
                    if (totalCount == 0) 0f else answeredCount.toFloat() / totalCount.toFloat()
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = onRevealAllAnswers,
                    enabled = !allRevealed,
                ) {
                    Text(text = stringResource(R.string.aistudio_quiz_reveal_all))
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onResetQuiz,
                    enabled = answeredCount > 0 || allRevealed,
                ) {
                    Text(text = stringResource(R.string.aistudio_quiz_reset))
                }
            }
        }
    }
}

@Composable
private fun QuizQuestionCard(
    number: Int,
    question: QuizQuestion,
    selectedIndex: Int?,
    isRevealed: Boolean,
    onOptionSelected: (Int) -> Unit,
    onRevealAnswer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.aistudio_quiz_question_number, number),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = question.prompt,
                style = MaterialTheme.typography.titleSmall,
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                question.options.forEachIndexed { index, option ->
                    QuizOptionRow(
                        text = option,
                        selected = selectedIndex == index,
                        optionState =
                            optionStateOf(
                                index = index,
                                answerIndex = question.answerIndex,
                                selectedIndex = selectedIndex,
                                isRevealed = isRevealed,
                            ),
                        enabled = !isRevealed,
                        onClick = { onOptionSelected(index) },
                    )
                }
            }
            QuizQuestionFooter(
                question = question,
                selectedIndex = selectedIndex,
                isRevealed = isRevealed,
                onRevealAnswer = onRevealAnswer,
            )
        }
    }
}

@Composable
private fun QuizQuestionFooter(
    question: QuizQuestion,
    selectedIndex: Int?,
    isRevealed: Boolean,
    onRevealAnswer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (!isRevealed) {
            TextButton(
                onClick = onRevealAnswer,
                enabled = selectedIndex != null,
            ) {
                Text(text = stringResource(R.string.aistudio_quiz_reveal))
            }
            return@Column
        }
        val isCorrect = selectedIndex == question.answerIndex
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                contentDescription = null,
                tint =
                    if (isCorrect) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text =
                    if (isCorrect) {
                        stringResource(R.string.aistudio_quiz_correct)
                    } else {
                        stringResource(R.string.aistudio_quiz_incorrect)
                    },
                style = MaterialTheme.typography.labelLarge,
                color =
                    if (isCorrect) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )
        }
        if (question.explanation.isNotBlank()) {
            Text(
                text = stringResource(R.string.aistudio_quiz_explanation, question.explanation),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QuizOptionRow(
    text: String,
    selected: Boolean,
    optionState: QuizOptionState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = optionState.containerColor(),
        contentColor = optionState.contentColor(),
        shape = MaterialTheme.shapes.small,
        modifier =
            modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick,
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                enabled = enabled,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** How a single option should read once the answer is on screen. */
private enum class QuizOptionState { NEUTRAL, SELECTED, CORRECT, WRONG }

private fun optionStateOf(
    index: Int,
    answerIndex: Int,
    selectedIndex: Int?,
    isRevealed: Boolean,
): QuizOptionState =
    when {
        isRevealed && index == answerIndex -> QuizOptionState.CORRECT
        isRevealed && index == selectedIndex -> QuizOptionState.WRONG
        index == selectedIndex -> QuizOptionState.SELECTED
        else -> QuizOptionState.NEUTRAL
    }

@Composable
private fun QuizOptionState.containerColor(): Color =
    when (this) {
        QuizOptionState.NEUTRAL -> MaterialTheme.colorScheme.surface
        QuizOptionState.SELECTED -> MaterialTheme.colorScheme.secondaryContainer
        QuizOptionState.CORRECT -> MaterialTheme.colorScheme.primaryContainer
        QuizOptionState.WRONG -> MaterialTheme.colorScheme.errorContainer
    }

@Composable
private fun QuizOptionState.contentColor(): Color =
    when (this) {
        QuizOptionState.NEUTRAL -> MaterialTheme.colorScheme.onSurface
        QuizOptionState.SELECTED -> MaterialTheme.colorScheme.onSecondaryContainer
        QuizOptionState.CORRECT -> MaterialTheme.colorScheme.onPrimaryContainer
        QuizOptionState.WRONG -> MaterialTheme.colorScheme.onErrorContainer
    }
