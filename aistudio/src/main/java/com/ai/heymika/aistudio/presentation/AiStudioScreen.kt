package com.ai.heymika.aistudio.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ai.heymika.aistudio.R
import com.ai.heymika.aistudio.presentation.component.historySection
import com.ai.heymika.aistudio.presentation.component.labelRes
import com.ai.heymika.aistudio.presentation.component.proseSection
import com.ai.heymika.aistudio.presentation.component.quizSection
import com.ai.heymika.core.data.Resource
import com.ai.heymika.core.domain.model.AppError
import com.ai.heymika.core.domain.model.GenerationKind
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.ui.component.state.MikaEmptyState
import com.ai.heymika.core.ui.component.state.MikaErrorState
import com.ai.heymika.core.ui.component.state.MikaLoadingState
import com.ai.heymika.core.ui.component.state.MikaStateVariant
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Stateful entry of the AI Studio screen.
 *
 * The [AiStudioViewModel] comes from the Koin module the feature registered when its
 * [com.ai.heymika.aistudio.AiStudioEntry] was constructed; `materialId` and `kind` are handed over
 * as Koin runtime parameters. The `key` keeps one ViewModel per material and generation kind,
 * so navigating to a different material does not reuse the previous generation.
 */
@Composable
fun AiStudioRoute(
    materialId: String,
    kind: GenerationKind,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AiStudioViewModel =
        koinViewModel(key = "aistudio/$materialId/${kind.name}") {
            parametersOf(materialId, kind)
        }
    val state by viewModel.state.collectAsStateWithLifecycle()
    AiStudioScreen(
        state = state,
        onOptionSelected = viewModel::selectOption,
        onRevealAnswer = viewModel::revealAnswer,
        onRevealAllAnswers = viewModel::revealAllAnswers,
        onResetQuiz = viewModel::resetQuiz,
        onRegenerate = viewModel::generate,
        onNavigateUp = onNavigateUp,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStudioScreen(
    state: AiStudioUiState,
    onOptionSelected: (String, Int) -> Unit,
    onRevealAnswer: (String) -> Unit,
    onRevealAllAnswers: () -> Unit,
    onResetQuiz: () -> Unit,
    onRegenerate: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = stringResource(R.string.aistudio_screen_title))
                        Text(
                            text = stringResource(state.kind.labelRes()),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.aistudio_navigate_up),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRegenerate) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.aistudio_regenerate),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val generation = state.generation
        val content = generation.data
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                // No content yet: the whole screen reflects the Resource.
                content == null && generation is Resource.Loading -> {
                    MikaLoadingState(message = stringResource(R.string.aistudio_generating))
                }

                content == null && generation is Resource.Error -> {
                    MikaErrorState(
                        error = generation.error,
                        onPrimary = onRegenerate,
                        onSecondary = onNavigateUp,
                    )
                }

                content == null -> {
                    MikaEmptyState(
                        title = stringResource(R.string.aistudio_empty_title),
                        body = stringResource(R.string.aistudio_empty_body),
                        pose = MikaPose.SLEEPING,
                    )
                }

                // Content is on screen; a later Loading/Error only annotates it.
                else -> {
                    AiStudioContent(
                        state = state,
                        isRefreshing = generation is Resource.Loading,
                        staleError = (generation as? Resource.Error)?.error,
                        onRetry = onRegenerate,
                        onOptionSelected = onOptionSelected,
                        onRevealAnswer = onRevealAnswer,
                        onRevealAllAnswers = onRevealAllAnswers,
                        onResetQuiz = onResetQuiz,
                    )
                }
            }
        }
    }
}

@Composable
private fun AiStudioContent(
    state: AiStudioUiState,
    isRefreshing: Boolean,
    staleError: AppError?,
    onRetry: () -> Unit,
    onOptionSelected: (String, Int) -> Unit,
    onRevealAnswer: (String) -> Unit,
    onRevealAllAnswers: () -> Unit,
    onResetQuiz: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = state.generation.data ?: return
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isRefreshing) {
            item(key = "refreshing") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.aistudio_refreshing),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
        if (staleError != null) {
            item(key = "stale-error") {
                MikaErrorState(
                    error = staleError,
                    onPrimary = onRetry,
                    variant = MikaStateVariant.Inline,
                )
            }
        }
        if (state.showsQuiz) {
            quizSection(
                questions = state.questions,
                selectedAnswers = state.selectedAnswers,
                revealedQuestions = state.revealedQuestions,
                correctCount = state.correctCount,
                answeredCount = state.answeredCount,
                allRevealed = state.allRevealed,
                onOptionSelected = onOptionSelected,
                onRevealAnswer = onRevealAnswer,
                onRevealAllAnswers = onRevealAllAnswers,
                onResetQuiz = onResetQuiz,
            )
        } else {
            proseSection(
                content = content,
                unparsableQuiz = state.isQuiz,
            )
        }
        historySection(
            history = state.history,
            currentContentId = content.id,
        )
    }
}
