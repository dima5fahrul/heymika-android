package com.ai.heymika.core.domain.text

/**
 * Turns the lightweight markup Mika writes in notes, summaries and tutor replies into typed
 * [MarkupSegment]s. It is inline only and CommonMark-flavoured:
 *
 * - `**bold**` or `__bold__`, `*italic*` or `_italic_`. Underscores inside a word stay literal
 *   (`snake_case`), asterisks may sit inside a word (`**ATP**-nya`).
 * - `==highlight==`, which, like underscores, never opens or closes inside a word (`a==b`).
 * - `` `code` `` with any backtick run length. Nothing inside is parsed.
 * - `$formula$` and `$$formula$$`. Nothing inside is parsed, and `$5 dan $10` stays plain text:
 *   an opening `$` needs a non-space after it, a closing `$` a non-space before it and no digit
 *   after it.
 * - `[text]{.tone}` where tone is `primary`, `success`, `danger` or `warning`.
 * - A backslash escapes a marker (`\*`), and a blank line closes every marker still open.
 *
 * Unbalanced or crossing markers are kept as literal text; the first closer wins. The parser
 * never throws, and runs in roughly linear time on chat-sized input.
 */
object MarkupParser {
    fun parse(input: String): List<MarkupSegment> {
        if (input.isEmpty()) return emptyList()
        val tokens = Tokenizer(input.replace("\r\n", "\n")).tokenize()
        matchOpenersAndClosers(tokens)
        return render(tokens)
    }
}

private const val BRACKET = '['

/** Pairs closers with the nearest opener of the same kind; openers in between turn literal. */
private fun matchOpenersAndClosers(tokens: List<Token>) {
    val openers = OpenerStack()
    tokens.forEachIndexed { index, token ->
        when (token) {
            is Delimiter -> {
                if (token.canClose) closeDelimiter(token, tokens, openers)
                if (token.canOpen && token.remaining > 0) openers.push(index, token.char)
            }

            is BracketOpen -> {
                openers.push(index, BRACKET)
            }

            is BracketClose -> {
                val position = openers.findLast(BRACKET)
                if (position >= 0) {
                    (tokens[openers.tokenIndexAt(position)] as BracketOpen).tone = token.tone
                    token.matched = true
                    openers.truncate(position)
                }
            }

            is Barrier -> {
                openers.clear()
            }

            else -> {
                Unit
            }
        }
    }
}

private fun closeDelimiter(
    closer: Delimiter,
    tokens: List<Token>,
    openers: OpenerStack,
) {
    while (closer.remaining > 0) {
        val position = openers.findLast(closer.char)
        if (position < 0) return
        val opener = tokens[openers.tokenIndexAt(position)] as Delimiter
        val used = if (opener.remaining >= 2 && closer.remaining >= 2) 2 else 1
        val emphasis =
            when {
                closer.char == '=' -> MarkupEmphasis.HIGHLIGHT
                used == 2 -> MarkupEmphasis.BOLD
                else -> MarkupEmphasis.ITALIC
            }
        opener.remaining -= used
        closer.remaining -= used
        opener.opens += emphasis
        closer.closes += emphasis
        openers.truncate(if (opener.remaining == 0) position else position + 1)
    }
}

private fun render(tokens: List<Token>): List<MarkupSegment> {
    val collector = SegmentCollector()
    val depth = IntArray(MarkupEmphasis.entries.size)
    var emphasis: Set<MarkupEmphasis> = emptySet()
    val tones = ArrayList<MarkupTone>()
    for (token in tokens) {
        val tone = tones.lastOrNull()
        when (token) {
            is Literal -> {
                collector.text(token.text, emphasis, tone)
            }

            is Barrier -> {
                collector.text(token.text, emphasis, tone)
            }

            is CodeSpan -> {
                collector.atom(MarkupSegment.Code(token.text, tone))
            }

            is FormulaSpan -> {
                collector.atom(MarkupSegment.Formula(token.text, tone))
            }

            is Delimiter -> {
                token.closes.forEach { depth[it.ordinal]-- }
                emphasis = activeEmphasis(depth)
                collector.text(token.char.toString().repeat(token.remaining), emphasis, tone)
                token.opens.forEach { depth[it.ordinal]++ }
                emphasis = activeEmphasis(depth)
            }

            is BracketOpen -> {
                val opened = token.tone
                if (opened != null) tones += opened else collector.text("[", emphasis, tone)
            }

            is BracketClose -> {
                if (token.matched) tones.removeAt(tones.lastIndex) else collector.text(token.raw, emphasis, tone)
            }
        }
    }
    return collector.finish()
}

private fun activeEmphasis(depth: IntArray): Set<MarkupEmphasis> =
    MarkupEmphasis.entries.filter { depth[it.ordinal] > 0 }.toSet()

private sealed interface Token

private class Literal(
    val text: String,
) : Token

/** A blank line: kept as text, but no marker may stay open across it. */
private class Barrier(
    val text: String,
) : Token

private class CodeSpan(
    val text: String,
) : Token

private class FormulaSpan(
    val text: String,
) : Token

/** A run of `*`, `_` or `==`. [remaining] characters that end up unmatched are rendered literally. */
private class Delimiter(
    val char: Char,
    length: Int,
    val canOpen: Boolean,
    val canClose: Boolean,
) : Token {
    var remaining: Int = length
    val opens = ArrayList<MarkupEmphasis>()
    val closes = ArrayList<MarkupEmphasis>()
}

private class BracketOpen : Token {
    /** Set when a `]{.tone}` closes this bracket; a bracket left without a tone renders as `[`. */
    var tone: MarkupTone? = null
}

private class BracketClose(
    val tone: MarkupTone,
    val raw: String,
) : Token {
    var matched: Boolean = false
}

/** Openers still waiting for a closer, with a per-kind count so a hopeless search is O(1). */
private class OpenerStack {
    private val tokenIndices = ArrayList<Int>()
    private val kinds = ArrayList<Char>()
    private val counts = HashMap<Char, Int>()

    fun push(
        tokenIndex: Int,
        kind: Char,
    ) {
        tokenIndices += tokenIndex
        kinds += kind
        counts[kind] = (counts[kind] ?: 0) + 1
    }

    fun tokenIndexAt(position: Int): Int = tokenIndices[position]

    /** Stack position of the topmost opener of [kind], or -1. */
    fun findLast(kind: Char): Int {
        if ((counts[kind] ?: 0) == 0) return -1
        for (position in kinds.indices.reversed()) {
            if (kinds[position] == kind) return position
        }
        return -1
    }

    /** Drops the opener at [position] and every opener above it. */
    fun truncate(position: Int) {
        while (kinds.size > position) {
            val kind = kinds.removeAt(kinds.lastIndex)
            tokenIndices.removeAt(tokenIndices.lastIndex)
            counts[kind] = (counts[kind] ?: 1) - 1
        }
    }

    fun clear() = truncate(0)
}

/** Builds the output, merging neighbouring text that shares emphasis and tone. */
private class SegmentCollector {
    private val segments = ArrayList<MarkupSegment>()
    private val pending = StringBuilder()
    private var pendingEmphasis: Set<MarkupEmphasis> = emptySet()
    private var pendingTone: MarkupTone? = null

    fun text(
        value: String,
        emphasis: Set<MarkupEmphasis>,
        tone: MarkupTone?,
    ) {
        if (value.isEmpty()) return
        if (pending.isNotEmpty() && (emphasis != pendingEmphasis || tone != pendingTone)) flush()
        pendingEmphasis = emphasis
        pendingTone = tone
        pending.append(value)
    }

    fun atom(segment: MarkupSegment) {
        flush()
        segments += segment
    }

    fun finish(): List<MarkupSegment> {
        flush()
        return segments
    }

    private fun flush() {
        if (pending.isEmpty()) return
        segments += MarkupSegment.Text(pending.toString(), pendingEmphasis, pendingTone)
        pending.clear()
    }
}

private class Tokenizer(
    private val input: String,
) {
    private val tokens = ArrayList<Token>()
    private val literal = StringBuilder()

    // A closer search that failed from position p and stopped at s also fails from any later
    // position before s, so each value remembers s per search kind.
    private val failedCodeSearch = HashMap<Int, Int>()
    private var failedInlineFormulaSearch = -1
    private var failedDisplayFormulaSearch = -1

    fun tokenize(): List<Token> {
        var index = 0
        while (index < input.length) {
            index =
                when (input[index]) {
                    '\\' -> escape(index)
                    '`' -> code(index)
                    '$' -> formula(index)
                    '*', '_' -> delimiter(index, strictIntraword = input[index] == '_')
                    '=' -> highlight(index)
                    '[' -> add(BracketOpen(), index + 1)
                    ']' -> bracketClose(index)
                    '\n' -> newline(index)
                    else -> appendLiteral(index, index + 1)
                }
        }
        flushLiteral()
        return tokens
    }

    private fun escape(index: Int): Int {
        val next = input.getOrNull(index + 1)
        if (next == null || next !in ESCAPABLE) return appendLiteral(index, index + 1)
        literal.append(next)
        return index + 2
    }

    private fun code(index: Int): Int {
        val run = runLength(index, '`')
        val contentStart = index + run
        val closer = findCodeCloser(contentStart, run)
        if (closer < 0) return appendLiteral(index, contentStart)
        return add(CodeSpan(normalizeCode(input.substring(contentStart, closer))), closer + run)
    }

    private fun findCodeCloser(
        from: Int,
        run: Int,
    ): Int {
        val failedUntil = failedCodeSearch[run]
        if (failedUntil != null && from <= failedUntil) return -1
        var position = from
        while (position < input.length) {
            when {
                input[position] == '`' -> {
                    val length = runLength(position, '`')
                    if (length == run) return position
                    position += length
                }

                isBlankLineAt(position) -> {
                    break
                }

                else -> {
                    position++
                }
            }
        }
        failedCodeSearch[run] = position
        return -1
    }

    private fun normalizeCode(content: String): String {
        val flat = content.replace('\n', ' ')
        val padded = flat.length >= 2 && flat.first() == ' ' && flat.last() == ' ' && flat.isNotBlank()
        return if (padded) flat.substring(1, flat.length - 1) else flat
    }

    private fun formula(index: Int): Int {
        if (input.startsWith("$$", index)) return displayFormula(index)
        val next = input.getOrNull(index + 1)
        if (next == null || next.isWhitespace()) return appendLiteral(index, index + 1)
        val closer = findInlineFormulaCloser(index + 1)
        if (closer < 0) return appendLiteral(index, index + 1)
        return add(FormulaSpan(input.substring(index + 1, closer).replace('\n', ' ')), closer + 1)
    }

    private fun findInlineFormulaCloser(from: Int): Int {
        if (from <= failedInlineFormulaSearch) return -1
        var position = from
        while (position < input.length) {
            when {
                input[position] == '\\' -> {
                    position += 2
                }

                input[position] == '$' -> {
                    val before = input[position - 1]
                    val after = input.getOrNull(position + 1)
                    if (!before.isWhitespace() && (after == null || !after.isDigit())) return position
                    position++
                }

                isBlankLineAt(position) -> {
                    break
                }

                else -> {
                    position++
                }
            }
        }
        failedInlineFormulaSearch = position
        return -1
    }

    private fun displayFormula(index: Int): Int {
        val contentStart = index + 2
        val closer = findDisplayFormulaCloser(contentStart)
        val content = if (closer < 0) "" else input.substring(contentStart, closer).trim()
        if (content.isEmpty()) return appendLiteral(index, contentStart)
        return add(FormulaSpan(content.replace('\n', ' ')), closer + 2)
    }

    private fun findDisplayFormulaCloser(from: Int): Int {
        if (from <= failedDisplayFormulaSearch) return -1
        var position = from
        while (position < input.length) {
            when {
                input.startsWith("$$", position) -> return position
                isBlankLineAt(position) -> break
                else -> position++
            }
        }
        failedDisplayFormulaSearch = position
        return -1
    }

    private fun delimiter(
        index: Int,
        strictIntraword: Boolean,
    ): Int {
        val char = input[index]
        val run = runLength(index, char)
        return addDelimiter(index, run, char, strictIntraword)
    }

    private fun highlight(index: Int): Int {
        val run = runLength(index, '=')
        if (run != 2) return appendLiteral(index, index + run)
        return addDelimiter(index, run, '=', strictIntraword = true)
    }

    private fun addDelimiter(
        index: Int,
        run: Int,
        char: Char,
        strictIntraword: Boolean,
    ): Int {
        val before = input.getOrNull(index - 1)
        val after = input.getOrNull(index + run)
        val leftFlanking = after != null && !after.isWhitespace()
        val rightFlanking = before != null && !before.isWhitespace()
        val canOpen = leftFlanking && !(strictIntraword && before?.isLetterOrDigit() == true)
        val canClose = rightFlanking && !(strictIntraword && after?.isLetterOrDigit() == true)
        if (!canOpen && !canClose) return appendLiteral(index, index + run)
        return add(Delimiter(char, run, canOpen, canClose), index + run)
    }

    private fun bracketClose(index: Int): Int {
        val nameStart = index + TONE_PREFIX.length
        if (input.startsWith(TONE_PREFIX, index)) {
            var nameEnd = nameStart
            while (nameEnd < input.length && input[nameEnd].isLetter()) nameEnd++
            val tone = MarkupTone.fromKey(input.substring(nameStart, nameEnd))
            if (tone != null && input.getOrNull(nameEnd) == '}') {
                return add(BracketClose(tone, input.substring(index, nameEnd + 1)), nameEnd + 1)
            }
        }
        return appendLiteral(index, index + 1)
    }

    private fun newline(index: Int): Int {
        val end = blankLineEnd(index)
        if (end < 0) return appendLiteral(index, index + 1)
        return add(Barrier(input.substring(index, end)), end)
    }

    private fun isBlankLineAt(index: Int): Boolean = input[index] == '\n' && blankLineEnd(index) >= 0

    /** For a `\n` at [index] that starts a blank line, the index just past its closing `\n`; else -1. */
    private fun blankLineEnd(index: Int): Int {
        var position = index + 1
        while (position < input.length && (input[position] == ' ' || input[position] == '\t')) position++
        return if (position < input.length && input[position] == '\n') position + 1 else -1
    }

    private fun runLength(
        index: Int,
        char: Char,
    ): Int {
        var end = index
        while (end < input.length && input[end] == char) end++
        return end - index
    }

    private fun appendLiteral(
        start: Int,
        end: Int,
    ): Int {
        literal.append(input, start, end)
        return end
    }

    private fun add(
        token: Token,
        next: Int,
    ): Int {
        flushLiteral()
        tokens += token
        return next
    }

    private fun flushLiteral() {
        if (literal.isEmpty()) return
        tokens += Literal(literal.toString())
        literal.clear()
    }

    private companion object {
        const val ESCAPABLE = "\\`*_=\$[]{}"
        const val TONE_PREFIX = "]{."
    }
}
