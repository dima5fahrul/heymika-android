package com.ai.heymika.core.domain.model

/**
 * One AI generation result for a material.
 *
 * For [GenerationKind.QUIZ], [body] holds a JSON array of questions that
 * `com.ai.heymika.aistudio.quiz.QuizParser` turns into [QuizQuestion]s. For the other
 * kinds it is plain text.
 */
data class GeneratedContent(
    val id: String,
    val materialId: String,
    val kind: GenerationKind,
    val body: String,
    val createdAt: Long,
)
