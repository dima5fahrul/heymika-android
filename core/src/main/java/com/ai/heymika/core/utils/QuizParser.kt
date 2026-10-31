package com.ai.heymika.core.utils

import com.ai.heymika.core.data.source.remote.response.QuizQuestionResponse
import com.ai.heymika.core.domain.model.QuizQuestion
import com.google.gson.Gson
import java.util.UUID

/**
 * Write side of the quiz round-trip: turns the backend's structured questions
 * into the JSON stored in [com.ai.heymika.core.domain.model.GeneratedContent.body].
 *
 * Reading is deliberately NOT here. A generation may come back as anything the
 * model produced — a bare array, a wrapper object, or plain prose — so the
 * tolerant reader lives with the screen that has to cope with it, in
 * `com.ai.heymika.aistudio.quiz.QuizParser`. That reader accepts everything this
 * encoder emits.
 */
object QuizParser {
    private val gson = Gson()

    fun encode(questions: List<QuizQuestion>): String = gson.toJson(questions)

    fun fromResponses(input: List<QuizQuestionResponse>): List<QuizQuestion> =
        input.mapNotNull { response ->
            val prompt = response.prompt?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val options = response.options.orEmpty()
            if (options.isEmpty()) return@mapNotNull null
            QuizQuestion(
                id = response.id ?: UUID.randomUUID().toString(),
                prompt = prompt,
                options = options,
                answerIndex = response.answerIndex?.takeIf { it in options.indices } ?: 0,
                explanation = response.explanation.orEmpty(),
            )
        }
}
