package com.ai.heymika.core.domain.text

import com.ai.heymika.core.domain.text.ScriptPosition.BASELINE
import com.ai.heymika.core.domain.text.ScriptPosition.SUBSCRIPT
import com.ai.heymika.core.domain.text.ScriptPosition.SUPERSCRIPT
import org.junit.Assert.assertEquals
import org.junit.Test

class FormulaScriptTest {
    @Test
    fun `empty formula gives no runs`() {
        assertEquals(emptyList<FormulaRun>(), FormulaScript.split(""))
    }

    @Test
    fun `formula without markers is one baseline run`() {
        assertEquals(listOf(run("E = mc")), FormulaScript.split("E = mc"))
    }

    @Test
    fun `underscore lowers the next character only`() {
        assertEquals(
            listOf(run("6CO"), run("2", SUBSCRIPT), run(" + 6H"), run("2", SUBSCRIPT), run("O")),
            FormulaScript.split("6CO_2 + 6H_2O"),
        )
    }

    @Test
    fun `caret raises the next character`() {
        assertEquals(listOf(run("E = mc"), run("2", SUPERSCRIPT)), FormulaScript.split("E = mc^2"))
    }

    @Test
    fun `braces group several characters`() {
        assertEquals(
            listOf(run("C"), run("6", SUBSCRIPT), run("H"), run("12", SUBSCRIPT), run("O"), run("6", SUBSCRIPT)),
            FormulaScript.split("C_6H_{12}O_6"),
        )
        assertEquals(listOf(run("e"), run("-x", SUPERSCRIPT)), FormulaScript.split("e^{-x}"))
    }

    @Test
    fun `adjacent runs at the same position merge`() {
        assertEquals(listOf(run("x"), run("ab", SUBSCRIPT)), FormulaScript.split("x_a_b"))
    }

    @Test
    fun `sub and superscript can follow each other`() {
        assertEquals(
            listOf(run("x"), run("i", SUBSCRIPT), run("2", SUPERSCRIPT)),
            FormulaScript.split("x_i^2"),
        )
    }

    @Test
    fun `escaped markers stay literal`() {
        assertEquals(listOf(run("snake_case^2")), FormulaScript.split("snake\\_case\\^2"))
    }

    @Test
    fun `marker without a usable script stays literal`() {
        assertEquals(listOf(run("a_")), FormulaScript.split("a_"))
        assertEquals(listOf(run("a_ b")), FormulaScript.split("a_ b"))
        assertEquals(listOf(run("a^{}")), FormulaScript.split("a^{}"))
        assertEquals(listOf(run("a_{12")), FormulaScript.split("a_{12"))
    }

    @Test
    fun `plain text of the runs drops only the markers`() {
        val runs = FormulaScript.split("H_2SO_{4} + x^{n+1}")
        assertEquals("H2SO4 + xn+1", runs.joinToString(separator = "") { it.text })
    }

    private fun run(
        text: String,
        position: ScriptPosition = BASELINE,
    ) = FormulaRun(text, position)
}
