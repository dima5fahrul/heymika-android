package com.ai.heymika.core.domain.text

/** Where a [FormulaRun] sits relative to the baseline. */
enum class ScriptPosition { BASELINE, SUBSCRIPT, SUPERSCRIPT }

/** One run of a formula after [FormulaScript.split]: [text] without the `_`/`^` markers. */
data class FormulaRun(
    val text: String,
    val position: ScriptPosition = ScriptPosition.BASELINE,
)

/**
 * Splits the raw source of a [MarkupSegment.Formula] into baseline, subscript and superscript runs,
 * TeX style: `H_2O`, `x^2`, `x_{10}`, `e^{-x}`. Without braces only the next character is lifted.
 * `\_` and `\^` stay literal, and a marker with nothing usable after it (a space, the end of the
 * formula, `{}` or an unclosed `{`) is kept as text. Adjacent runs never share a position.
 */
object FormulaScript {
    fun split(formula: String): List<FormulaRun> {
        val runs = mutableListOf<FormulaRun>()
        val baseline = StringBuilder()

        fun flushBaseline() {
            if (baseline.isNotEmpty()) {
                runs.add(FormulaRun(baseline.toString()))
                baseline.clear()
            }
        }

        var index = 0
        while (index < formula.length) {
            val char = formula[index]
            if (char == '\\' && index + 1 < formula.length && formula[index + 1].isScriptMarker()) {
                baseline.append(formula[index + 1])
                index += 2
                continue
            }
            val script = if (char.isScriptMarker()) scriptAt(formula, index) else null
            if (script == null) {
                baseline.append(char)
                index++
                continue
            }
            flushBaseline()
            val position = if (char == '_') ScriptPosition.SUBSCRIPT else ScriptPosition.SUPERSCRIPT
            val last = runs.lastOrNull()
            if (last != null && last.position == position) {
                runs[runs.lastIndex] = last.copy(text = last.text + script.text)
            } else {
                runs.add(FormulaRun(script.text, position))
            }
            index = script.end
        }
        flushBaseline()
        return runs
    }

    private class Script(
        val text: String,
        val end: Int,
    )

    private fun Char.isScriptMarker(): Boolean = this == '_' || this == '^'

    private fun scriptAt(
        formula: String,
        markerIndex: Int,
    ): Script? {
        val start = markerIndex + 1
        if (start >= formula.length) return null
        val next = formula[start]
        if (next.isWhitespace()) return null
        if (next != '{') return Script(next.toString(), start + 1)
        val closer = formula.indexOf('}', startIndex = start + 1)
        if (closer <= start + 1) return null
        return Script(formula.substring(start + 1, closer), closer + 1)
    }
}
