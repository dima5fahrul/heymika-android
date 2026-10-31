package com.ai.heymika.aistudio.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.heymika.aistudio.quiz.QuizParser
import com.ai.heymika.core.data.Resource
import com.ai.heymika.core.domain.model.AppError
import com.ai.heymika.core.domain.model.GeneratedContent
import com.ai.heymika.core.domain.model.GenerationKind
import com.ai.heymika.core.domain.usecase.AiUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the AI Studio screen for one material and one [GenerationKind].
 *
 * `materialId` and `kind` are navigation arguments and are injected as Koin runtime parameters;
 * see [com.ai.heymika.aistudio.di.aiStudioModule].
 */
class AiStudioViewModel(
    private val materialId: String,
    private val kind: GenerationKind,
    private val aiUseCase: AiUseCase,
) : ViewModel() {
    private val mutableState =
        MutableStateFlow(AiStudioUiState(materialId = materialId, kind = kind))
    val state: StateFlow<AiStudioUiState> = mutableState.asStateFlow()

    private var generateJob: Job? = null

    init {
        observeHistory()
        generate()
    }

    /** Runs the AI generation; also used as the retry and the regenerate action. */
    fun generate() {
        generateJob?.cancel()
        generateJob =
            viewModelScope.launch {
                aiUseCase
                    .generate(materialId, kind)
                    .onStart {
                        mutableState.update { current ->
                            current.copy(generation = Resource.Loading(current.generation.data))
                        }
                    }.catch { throwable ->
                        mutableState.update { current ->
                            current.copy(
                                generation =
                                    Resource.Error(
                                        message = throwable.toString(),
                                        data = current.generation.data,
                                        error = AppError.Unknown(throwable),
                                    ),
                            )
                        }
                    }.collect { resource ->
                        mutableState.update { current -> current.reduce(resource) }
                    }
            }
    }

    fun selectOption(
        questionId: String,
        optionIndex: Int,
    ) {
        mutableState.update { current ->
            if (questionId in current.revealedQuestions) {
                current
            } else {
                current.copy(selectedAnswers = current.selectedAnswers + (questionId to optionIndex))
            }
        }
    }

    fun revealAnswer(questionId: String) {
        mutableState.update { current ->
            current.copy(revealedQuestions = current.revealedQuestions + questionId)
        }
    }

    fun revealAllAnswers() {
        mutableState.update { current ->
            current.copy(revealedQuestions = current.questions.mapTo(mutableSetOf()) { it.id })
        }
    }

    fun resetQuiz() {
        mutableState.update { current ->
            current.copy(selectedAnswers = emptyMap(), revealedQuestions = emptySet())
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            aiUseCase
                .getHistory(materialId)
                .catch { emit(emptyList()) }
                .collect { history ->
                    mutableState.update { current -> current.copy(history = history) }
                }
        }
    }

    private fun AiStudioUiState.reduce(resource: Resource<GeneratedContent>): AiStudioUiState {
        val content = resource.data
        val isNewContent = content != null && content.id != generation.data?.id
        val parsed =
            when {
                kind != GenerationKind.QUIZ -> emptyList()
                content == null -> questions
                else -> QuizParser.parse(content)
            }
        return copy(
            generation = resource,
            questions = parsed,
            selectedAnswers = if (isNewContent) emptyMap() else selectedAnswers,
            revealedQuestions = if (isNewContent) emptySet() else revealedQuestions,
        )
    }
}
