package com.ai.heymika.core.domain.text

/** Emphasis that can stack on one run of text. */
enum class MarkupEmphasis {
    BOLD,
    ITALIC,
    HIGHLIGHT,
}

/**
 * Semantic colour of a `[text]{.tone}` span, named after the markup key (lowercase). The UI maps each
 * tone onto a HeyMika colour token; the domain never knows the actual colours.
 */
enum class MarkupTone {
    PRIMARY,
    SUCCESS,
    DANGER,
    WARNING,
    ;

    companion object {
        /** The tone for a markup key such as `danger`, ignoring case; null when unknown. */
        fun fromKey(key: String): MarkupTone? = entries.firstOrNull { it.name.equals(key, ignoreCase = true) }
    }
}

/** One run of parsed AI text. Adjacent [Text] runs never share the same emphasis and tone. */
sealed interface MarkupSegment {
    /** The visible text, with every markup marker removed. */
    val text: String

    /** The innermost enclosing tone, or null. */
    val tone: MarkupTone?

    data class Text(
        override val text: String,
        val emphasis: Set<MarkupEmphasis> = emptySet(),
        override val tone: MarkupTone? = null,
    ) : MarkupSegment

    /** Inline code: rendered as a chip, never wraps markup inside. */
    data class Code(
        override val text: String,
        override val tone: MarkupTone? = null,
    ) : MarkupSegment

    /** An inline formula (`$...$` or `$$...$$`), kept as the raw source between the dollars. */
    data class Formula(
        override val text: String,
        override val tone: MarkupTone? = null,
    ) : MarkupSegment
}

/** The text without any markup: for copy-to-clipboard, previews and accessibility labels. */
fun List<MarkupSegment>.toPlainText(): String = joinToString(separator = "") { it.text }
