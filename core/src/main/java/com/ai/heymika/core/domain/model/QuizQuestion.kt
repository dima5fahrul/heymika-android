package com.ai.heymika.core.domain.model

/** A single multiple-choice question produced by a [GenerationKind.QUIZ] run. */
data class QuizQuestion(
    val id: String,
    val prompt: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanation: String,
)
