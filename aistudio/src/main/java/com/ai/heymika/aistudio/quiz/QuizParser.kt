package com.ai.heymika.aistudio.quiz

import com.ai.heymika.core.domain.model.GeneratedContent
import com.ai.heymika.core.domain.model.QuizQuestion
import org.json.JSONArray
import org.json.JSONObject

/**
 * Turns the free-form [GeneratedContent.body] produced for `GenerationKind.QUIZ` into a list
 * of [QuizQuestion].
 *
 * The AI contract exposed by `:core` returns a single `GeneratedContent` whose `body` is a
 * `String`, so the structure the quiz UI needs has to be recovered here. Two shapes are
 * understood, and anything unrecognised yields an empty list so the screen can fall back to
 * rendering the raw output as prose:
 *
 * 1. JSON - either a top-level array of question objects or an object with a `questions`
 *    (or `quiz` / `items` / `data`) array. Recognised keys per question: `id`,
 *    `prompt`/`question`/`text`, `options`/`choices`/`answers`,
 *    `answerIndex`/`answer_index`/`correctIndex` (zero based),
 *    `answer`/`correctAnswer`/`correct` (index, letter or full option text) and
 *    `explanation`/`reason`/`rationale`.
 * 2. Plain text, one question per block:
 *
 *    ```
 *    1. Which layer owns the AI contract?
 *    A) presentation
 *    B) domain
 *    C) data
 *    Answer: B
 *    Explanation: use cases live in the domain layer.
 *    ```
 *
 *    Digit-prefixed lines start a new question, letter- or bullet-prefixed lines are options.
 *    In the text shape an `Answer:` number is read as one based, which is how prose output
 *    numbers its options.
 */
internal object QuizParser {
    private const val MIN_OPTIONS = 2
    private const val MIN_PREFIX_LENGTH = 2

    private val promptRegex =
        Regex("""^(?:\d{1,2}\s*[).:\-]|q(?:uestion)?\s*\d{0,2}\s*[).:\-])\s*(.+)$""", RegexOption.IGNORE_CASE)
    private val optionRegex =
        Regex("""^(?:\(?([a-h])\s*[).:\-]|[\-*•▪])\s*(.+)$""", RegexOption.IGNORE_CASE)
    private val answerRegex =
        Regex("""^(?:answer|correct(?:\s+answer)?|key)\s*[:=\-]\s*(.+)$""", RegexOption.IGNORE_CASE)
    private val explanationRegex =
        Regex("""^(?:explanation|rationale|reason|why)\s*[:=\-]\s*(.+)$""", RegexOption.IGNORE_CASE)
    private val answerLetterRegex = Regex("""^\(?([a-h])\)?$""", RegexOption.IGNORE_CASE)
    private val nonAlphanumericRegex = Regex("""[^a-z0-9]""")

    private val questionArrayKeys = listOf("questions", "quiz", "items", "data", "results")
    private val promptKeys = listOf("prompt", "question", "text", "title")
    private val optionsKeys = listOf("options", "choices", "answers", "alternatives")
    private val answerIndexKeys = listOf("answerIndex", "answer_index", "correctIndex", "correct_index")
    private val answerKeys = listOf("answer", "correctAnswer", "correct_answer", "correct", "key")
    private val explanationKeys = listOf("explanation", "rationale", "reason", "why")

    fun parse(content: GeneratedContent): List<QuizQuestion> {
        val body = content.body.trim()
        if (body.isEmpty()) return emptyList()
        return parseAsJson(content.id, body) ?: parseAsText(content.id, body)
    }

    // region JSON

    private fun parseAsJson(
        contentId: String,
        body: String,
    ): List<QuizQuestion>? {
        if (!body.startsWith("[") && !body.startsWith("{")) return null
        return runCatching { jsonQuestions(contentId, body) }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun jsonQuestions(
        contentId: String,
        body: String,
    ): List<QuizQuestion> {
        val array =
            if (body.startsWith("[")) {
                JSONArray(body)
            } else {
                val root = JSONObject(body)
                questionArrayKeys.firstNotNullOfOrNull { root.optJSONArray(it) }
                    ?: return emptyList()
            }
        val questions = mutableListOf<QuizQuestion>()
        for (index in 0 until array.length()) {
            val element = array.optJSONObject(index) ?: continue
            questions += jsonQuestion(contentId, index, element) ?: continue
        }
        return questions
    }

    private fun jsonQuestion(
        contentId: String,
        index: Int,
        element: JSONObject,
    ): QuizQuestion? {
        val prompt = promptKeys.firstNotNullOfOrNull { element.stringOrNull(it) } ?: return null
        val optionsArray = optionsKeys.firstNotNullOfOrNull { element.optJSONArray(it) } ?: return null
        val options =
            (0 until optionsArray.length())
                .map { optionsArray.optString(it).trim() }
                .filter { it.isNotEmpty() }
        if (options.size < MIN_OPTIONS) return null
        return QuizQuestion(
            id = element.stringOrNull("id") ?: "$contentId-${index + 1}",
            prompt = prompt,
            options = options,
            answerIndex = jsonAnswerIndex(element, options),
            explanation = explanationKeys.firstNotNullOfOrNull { element.stringOrNull(it) }.orEmpty(),
        )
    }

    private fun jsonAnswerIndex(
        element: JSONObject,
        options: List<String>,
    ): Int {
        answerIndexKeys.forEach { key ->
            val value = element.opt(key) ?: return@forEach
            zeroBasedOrNull(value, options)?.let { return it }
        }
        answerKeys.forEach { key ->
            val value = element.opt(key) ?: return@forEach
            zeroBasedOrNull(value, options)?.let { return it }
            answerFromText(value.toString(), options, oneBasedFirst = false)?.let { return it }
        }
        return 0
    }

    private fun zeroBasedOrNull(
        value: Any?,
        options: List<String>,
    ): Int? =
        when (value) {
            is Number -> value.toInt().takeIf { it in options.indices }
            else -> null
        }

    private fun JSONObject.stringOrNull(key: String): String? =
        optString(key).trim().takeIf { it.isNotEmpty() && it != "null" }

    // endregion

    // region plain text

    private fun parseAsText(
        contentId: String,
        body: String,
    ): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()
        val draft = QuestionDraft()

        fun flush() {
            val prompt = draft.prompt?.trim()?.takeIf { it.isNotEmpty() }
            if (prompt != null && draft.options.size >= MIN_OPTIONS) {
                val options = draft.options.toList()
                questions +=
                    QuizQuestion(
                        id = "$contentId-${questions.size + 1}",
                        prompt = prompt,
                        options = options,
                        answerIndex =
                            answerFromText(
                                raw = draft.answer.orEmpty(),
                                options = options,
                                oneBasedFirst = true,
                            ) ?: 0,
                        explanation = draft.explanation?.trim().orEmpty(),
                    )
            }
            draft.clear()
        }

        body.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach

            answerRegex.matchEntire(line)?.let { match ->
                draft.answer = match.groupValues[1]
                return@forEach
            }
            explanationRegex.matchEntire(line)?.let { match ->
                draft.explanation = match.groupValues[1]
                return@forEach
            }
            val optionMatch = optionRegex.matchEntire(line)
            if (optionMatch != null && draft.prompt != null) {
                draft.options += optionMatch.groupValues[2].trim()
                return@forEach
            }
            val promptMatch = promptRegex.matchEntire(line)
            if (promptMatch != null) {
                flush()
                draft.prompt = promptMatch.groupValues[1].trim()
                return@forEach
            }
            when {
                draft.prompt == null -> draft.prompt = line
                draft.options.isEmpty() -> draft.prompt = "${draft.prompt} $line"
                else -> draft.explanation = listOfNotNull(draft.explanation, line).joinToString(" ")
            }
        }
        flush()
        return questions
    }

    /** Mutable accumulator for one question block of the plain-text shape. */
    private class QuestionDraft {
        var prompt: String? = null
        var answer: String? = null
        var explanation: String? = null
        val options: MutableList<String> = mutableListOf()

        fun clear() {
            prompt = null
            answer = null
            explanation = null
            options.clear()
        }
    }

    // endregion

    /**
     * Resolves an answer written as a number, a letter or the option text itself.
     *
     * @param oneBasedFirst prose numbers its options from 1, machine output from 0.
     */
    private fun answerFromText(
        raw: String,
        options: List<String>,
        oneBasedFirst: Boolean,
    ): Int? {
        val text = raw.trim().trimEnd('.', ')', ',').trim()
        if (text.isEmpty()) return null

        text.toIntOrNull()?.let { number ->
            val candidates =
                if (oneBasedFirst) {
                    listOf(number - 1, number)
                } else {
                    listOf(number, number - 1)
                }
            return candidates.firstOrNull { it in options.indices }
        }

        answerLetterRegex.matchEntire(text)?.let { match ->
            val index = match.groupValues[1].lowercase().first() - 'a'
            if (index in options.indices) return index
        }

        val normalized = normalize(text)
        if (normalized.isEmpty()) return null
        options
            .indexOfFirst { normalize(it) == normalized }
            .takeIf { it >= 0 }
            ?.let { return it }
        return options
            .indexOfFirst { normalized.length > MIN_PREFIX_LENGTH && normalize(it).startsWith(normalized) }
            .takeIf { it >= 0 }
    }

    private fun normalize(value: String): String = nonAlphanumericRegex.replace(value.lowercase(), "")
}
