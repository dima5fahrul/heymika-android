package com.ai.heymika.aistudio.presentation

import com.ai.heymika.core.data.Resource
import com.ai.heymika.core.domain.model.GeneratedContent
import com.ai.heymika.core.domain.model.GenerationKind
import com.ai.heymika.core.domain.model.QuizQuestion

/** Everything [AiStudioScreen] needs in order to render. */
data class AiStudioUiState(
    val materialId: String = "",
    val kind: GenerationKind = GenerationKind.QUIZ,
    val generation: Resource<GeneratedContent> = Resource.Loading(),
    val questions: List<QuizQuestion> = emptyList(),
    val selectedAnswers: Map<String, Int> = emptyMap(),
    val revealedQuestions: Set<String> = emptySet(),
    val history: List<GeneratedContent> = emptyList(),
) {
    val isQuiz: Boolean
        get() = kind == GenerationKind.QUIZ

    /** QUIZ output that could not be read as questions still deserves to be shown, as prose. */
    val showsQuiz: Boolean
        get() = isQuiz && questions.isNotEmpty()

    val answeredCount: Int
        get() = questions.count { selectedAnswers.containsKey(it.id) }

    val correctCount: Int
        get() = questions.count { selectedAnswers[it.id] == it.answerIndex }

    val allRevealed: Boolean
        get() = questions.isNotEmpty() && revealedQuestions.size >= questions.size
}
