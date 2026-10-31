package com.ai.heymika.core.domain.text

import com.ai.heymika.core.domain.text.MarkupEmphasis.BOLD
import com.ai.heymika.core.domain.text.MarkupEmphasis.HIGHLIGHT
import com.ai.heymika.core.domain.text.MarkupEmphasis.ITALIC
import com.ai.heymika.core.domain.text.MarkupTone.DANGER
import com.ai.heymika.core.domain.text.MarkupTone.PRIMARY
import com.ai.heymika.core.domain.text.MarkupTone.SUCCESS
import com.ai.heymika.core.domain.text.MarkupTone.WARNING
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MarkupParserTest {
    // region empty

    @Test
    fun `empty input gives no segments`() {
        assertEquals(emptyList<MarkupSegment>(), parse(""))
    }

    @Test
    fun `whitespace only input is kept as one text segment`() {
        assertEquals(listOf(text("  \n ")), parse("  \n "))
    }

    @Test
    fun `plain text without markers is one segment`() {
        assertEquals(listOf(text("Glukosa → G-6-P (Regulasi)")), parse("Glukosa → G-6-P (Regulasi)"))
    }

    // endregion

    // region normal

    @Test
    fun `double asterisks make bold`() {
        assertEquals(
            listOf(text("Dikatalisis oleh "), text("PFK-1", BOLD), text(".")),
            parse("Dikatalisis oleh **PFK-1**."),
        )
    }

    @Test
    fun `double underscores make bold`() {
        assertEquals(listOf(text("tebal", BOLD)), parse("__tebal__"))
    }

    @Test
    fun `single asterisk and underscore make italic`() {
        assertEquals(
            listOf(text("enzim "), text("Heksokinase", ITALIC), text(" dan "), text("committed step", ITALIC)),
            parse("enzim *Heksokinase* dan _committed step_"),
        )
    }

    @Test
    fun `double equals make highlight`() {
        assertEquals(listOf(text("Ingat "), text("+2 ATP", HIGHLIGHT), text("!")), parse("Ingat ==+2 ATP==!"))
    }

    @Test
    fun `backticks make inline code`() {
        assertEquals(
            listOf(text("Pencarian "), MarkupSegment.Code("O(log n)"), text(" rata-rata")),
            parse("Pencarian `O(log n)` rata-rata"),
        )
    }

    @Test
    fun `dollars make a formula`() {
        assertEquals(
            listOf(text("Rumus "), MarkupSegment.Formula("E = mc^2"), text(" terkenal")),
            parse("Rumus \$E = mc^2\$ terkenal"),
        )
    }

    @Test
    fun `double dollars make a formula with its content trimmed`() {
        assertEquals(listOf(MarkupSegment.Formula("\\frac{a}{b}")), parse("\$\$ \\frac{a}{b} \$\$"))
    }

    @Test
    fun `bracket with a tone suffix colours the text`() {
        assertEquals(
            listOf(text("Hasil "), text("+2 ATP", tone = PRIMARY), text(" bersih")),
            parse("Hasil [+2 ATP]{.primary} bersih"),
        )
    }

    @Test
    fun `tone keys ignore case`() {
        assertEquals(listOf(text("awas", tone = WARNING)), parse("[awas]{.Warning}"))
    }

    @Test
    fun `asterisks may close inside a word for Indonesian suffixes`() {
        assertEquals(
            listOf(text("ATP", BOLD), text("-nya dan "), text("enzim", BOLD), text("nya")),
            parse("**ATP**-nya dan **enzim**nya"),
        )
    }

    @Test
    fun `mockup note paragraph parses into toned bold spans`() {
        val input =
            "Tahap Investasi memakai [**2 ATP**]{.danger}, lalu Tahap Payoff menghasilkan " +
                "[**4 ATP**]{.success}. Jadi perolehan *BERSIH (Netto)* tetap [+2 ATP]{.primary}, bukan 4!"

        assertEquals(
            listOf(
                text("Tahap Investasi memakai "),
                text("2 ATP", BOLD, tone = DANGER),
                text(", lalu Tahap Payoff menghasilkan "),
                text("4 ATP", BOLD, tone = SUCCESS),
                text(". Jadi perolehan "),
                text("BERSIH (Netto)", ITALIC),
                text(" tetap "),
                text("+2 ATP", tone = PRIMARY),
                text(", bukan 4!"),
            ),
            parse(input),
        )
    }

    @Test
    fun `mockup workspace paragraph parses code chips with tones`() {
        assertEquals(
            listOf(
                text("Rata-rata "),
                MarkupSegment.Code("O(log n)"),
                text(", namun memburuk menjadi "),
                MarkupSegment.Code("O(n)", DANGER),
                text(" bila tak berimbang."),
            ),
            parse("Rata-rata `O(log n)`, namun memburuk menjadi [`O(n)`]{.danger} bila tak berimbang."),
        )
    }

    @Test
    fun `plain text drops every marker`() {
        val segments = parse("**Halo** [Mika]{.primary}, `kode` dan \$x\$ ==ya==")

        assertEquals("Halo Mika, kode dan x ya", segments.toPlainText())
    }

    @Test
    fun `windows line endings are normalised`() {
        assertEquals(
            listOf(text("a", BOLD), text("\n\n"), text("b", ITALIC)),
            parse("**a**\r\n\r\n*b*"),
        )
    }

    @Test
    fun `tone keys map to tones`() {
        assertEquals(DANGER, MarkupTone.fromKey("danger"))
        assertEquals(SUCCESS, MarkupTone.fromKey("SUCCESS"))
        assertNull(MarkupTone.fromKey("ungu"))
        assertNull(MarkupTone.fromKey(""))
    }

    // endregion

    // region nested

    @Test
    fun `triple asterisks make bold italic`() {
        assertEquals(listOf(text("x", BOLD, ITALIC)), parse("***x***"))
    }

    @Test
    fun `italic inside bold stacks emphasis`() {
        assertEquals(
            listOf(text("bold ", BOLD), text("and italic", BOLD, ITALIC), text(" inside", BOLD)),
            parse("**bold *and italic* inside**"),
        )
    }

    @Test
    fun `bold inside underscore italic stacks emphasis`() {
        assertEquals(listOf(text("italic with ", ITALIC), text("bold", ITALIC, BOLD)), parse("_italic with **bold**_"))
    }

    @Test
    fun `bold inside bold stays bold after the inner closer`() {
        assertEquals(listOf(text("a b c", BOLD)), parse("**a **b** c**"))
    }

    @Test
    fun `highlight wraps bold`() {
        assertEquals(listOf(text("penting", HIGHLIGHT, BOLD)), parse("==**penting**=="))
    }

    @Test
    fun `tone wraps bold text and code`() {
        assertEquals(
            listOf(
                text("Awas", BOLD, tone = WARNING),
                text(" jebakan ", tone = WARNING),
                MarkupSegment.Code("O(n)", WARNING),
            ),
            parse("[**Awas** jebakan `O(n)`]{.warning}"),
        )
    }

    @Test
    fun `innermost tone wins`() {
        assertEquals(
            listOf(text("a ", tone = SUCCESS), text("b", tone = DANGER), text(" c", tone = SUCCESS)),
            parse("[a [b]{.danger} c]{.success}"),
        )
    }

    @Test
    fun `formula takes the enclosing tone`() {
        assertEquals(listOf(MarkupSegment.Formula("x^2", PRIMARY)), parse("[\$x^2\$]{.primary}"))
    }

    @Test
    fun `code inside bold ignores the emphasis`() {
        assertEquals(
            listOf(text("lihat ", BOLD), MarkupSegment.Code("x"), text(" ya", BOLD)),
            parse("**lihat `x` ya**"),
        )
    }

    @Test
    fun `markers inside code stay literal`() {
        assertEquals(listOf(MarkupSegment.Code("**a** _b_ [c]{.danger}")), parse("`**a** _b_ [c]{.danger}`"))
    }

    @Test
    fun `markers inside a formula stay literal`() {
        assertEquals(listOf(MarkupSegment.Formula("a_1 * b_2")), parse("\$a_1 * b_2\$"))
    }

    // endregion

    // region unbalanced

    @Test
    fun `unclosed bold stays literal`() {
        assertEquals(listOf(text("**tebal tanpa penutup")), parse("**tebal tanpa penutup"))
    }

    @Test
    fun `unclosed italic stays literal`() {
        assertEquals(listOf(text("*miring")), parse("*miring"))
    }

    @Test
    fun `closer without opener stays literal`() {
        assertEquals(listOf(text("akhir**")), parse("akhir**"))
    }

    @Test
    fun `unclosed backtick stays literal`() {
        assertEquals(listOf(text("kode `belum ditutup")), parse("kode `belum ditutup"))
    }

    @Test
    fun `unclosed dollar stays literal`() {
        assertEquals(listOf(text("rumus \$x + y")), parse("rumus \$x + y"))
    }

    @Test
    fun `unclosed double dollar stays literal`() {
        assertEquals(listOf(text("\$\$x + y")), parse("\$\$x + y"))
    }

    @Test
    fun `empty double dollars stay literal`() {
        assertEquals(listOf(text("\$\$  \$\$")), parse("\$\$  \$\$"))
    }

    @Test
    fun `unclosed bracket stays literal`() {
        assertEquals(listOf(text("[kurung tanpa tutup")), parse("[kurung tanpa tutup"))
    }

    @Test
    fun `unknown tone stays literal`() {
        assertEquals(listOf(text("[nada]{.ungu}")), parse("[nada]{.ungu}"))
    }

    @Test
    fun `tone closer without opener stays literal`() {
        assertEquals(listOf(text("tanpa pembuka]{.danger}")), parse("tanpa pembuka]{.danger}"))
    }

    @Test
    fun `bracket without a tone suffix stays literal`() {
        assertEquals(
            listOf(text("lihat [1] dan [tautan](https://x.id)")),
            parse("lihat [1] dan [tautan](https://x.id)"),
        )
    }

    @Test
    fun `crossing markers keep the first closer and drop the rest`() {
        assertEquals(listOf(text("a _b", BOLD), text(" c_")), parse("**a _b** c_"))
    }

    @Test
    fun `extra closer characters stay literal after the span`() {
        assertEquals(listOf(text("a", BOLD), text("*")), parse("**a***"))
    }

    @Test
    fun `extra opener characters stay literal before the span`() {
        assertEquals(listOf(text("*"), text("a", BOLD)), parse("***a**"))
    }

    @Test
    fun `a blank line closes every open marker`() {
        assertEquals(listOf(text("**awal\n\nakhir**")), parse("**awal\n\nakhir**"))
    }

    @Test
    fun `a single newline does not close a marker`() {
        assertEquals(listOf(text("baris satu\nbaris dua", BOLD)), parse("**baris satu\nbaris dua**"))
    }

    @Test
    fun `code cannot cross a blank line`() {
        assertEquals(listOf(text("`a\n\nb`")), parse("`a\n\nb`"))
    }

    @Test
    fun `formula cannot cross a blank line`() {
        assertEquals(listOf(text("\$a\n\nb\$")), parse("\$a\n\nb\$"))
    }

    @Test
    fun `arithmetic with spaced asterisks stays literal`() {
        assertEquals(listOf(text("2 * 3 * 4 = 24")), parse("2 * 3 * 4 = 24"))
    }

    @Test
    fun `underscores inside words stay literal`() {
        assertEquals(listOf(text("nama_variabel_ini")), parse("nama_variabel_ini"))
    }

    @Test
    fun `double equals inside words stay literal`() {
        assertEquals(listOf(text("a==b dan c==d")), parse("a==b dan c==d"))
    }

    @Test
    fun `other equals runs stay literal`() {
        assertEquals(listOf(text("x = 1 === y")), parse("x = 1 === y"))
    }

    @Test
    fun `money amounts are not formulas`() {
        assertEquals(listOf(text("Harganya \$5 dan \$10")), parse("Harganya \$5 dan \$10"))
    }

    @Test
    fun `bullet asterisks at line start stay literal`() {
        assertEquals(listOf(text("* satu\n* dua")), parse("* satu\n* dua"))
    }

    // endregion

    // region escapes and code details

    @Test
    fun `backslash escapes a marker`() {
        assertEquals(listOf(text("*bukan miring*")), parse("\\*bukan miring\\*"))
    }

    @Test
    fun `backslash before a normal character is kept`() {
        assertEquals(listOf(text("C:\\Users")), parse("C:\\Users"))
    }

    @Test
    fun `trailing backslash is kept`() {
        assertEquals(listOf(text("akhir\\")), parse("akhir\\"))
    }

    @Test
    fun `escaped bracket cannot open a tone`() {
        assertEquals(listOf(text("[bukan nada]{.danger}")), parse("\\[bukan nada]{.danger}"))
    }

    @Test
    fun `longer backtick runs allow backticks inside and strip one padding space`() {
        assertEquals(listOf(MarkupSegment.Code("a `b` c")), parse("`` a `b` c ``"))
    }

    @Test
    fun `newlines inside code become spaces`() {
        assertEquals(listOf(MarkupSegment.Code("a b")), parse("`a\nb`"))
    }

    // endregion

    // region robustness

    @Test
    fun `random markup never throws and keeps the output invariants`() {
        val fragments =
            listOf(
                "*",
                "**",
                "***",
                "_",
                "__",
                "==",
                "`",
                "``",
                "\$",
                "\$\$",
                "[",
                "]",
                "]{.danger}",
                "]{.ungu}",
                "\\",
                " ",
                "\n",
                "\n\n",
                "a",
                "b1",
                "kata",
                "{",
                "}",
                ".",
            )
        val random = Random(seed = 42)
        repeat(3_000) {
            val input = buildString { repeat(random.nextInt(0, 40)) { append(fragments.random(random)) } }

            val segments = parse(input)

            assertTrue(input, segments.all { it.text.isNotEmpty() })
            assertTrue(input, segments.toPlainText().length <= input.length)
            segments.zipWithNext().forEach { (first, second) ->
                if (first is MarkupSegment.Text && second is MarkupSegment.Text) {
                    assertNotEquals(input, first.emphasis to first.tone, second.emphasis to second.tone)
                }
            }
        }
    }

    @Test(timeout = 5_000)
    fun `pathological input parses quickly`() {
        val input =
            buildString {
                repeat(20_000) { append("*a ") }
                repeat(20_000) { append("[") }
                repeat(20_000) { append("]{.danger}") }
                repeat(20_000) { append("``a`") }
                repeat(20_000) { append("\$a \$") }
                repeat(20_000) { append("_b* ") }
            }

        assertTrue(parse(input).isNotEmpty())
    }

    // endregion

    private fun parse(input: String): List<MarkupSegment> = MarkupParser.parse(input)

    private fun text(
        value: String,
        vararg emphasis: MarkupEmphasis,
        tone: MarkupTone? = null,
    ): MarkupSegment.Text = MarkupSegment.Text(value, emphasis.toSet(), tone)
}
