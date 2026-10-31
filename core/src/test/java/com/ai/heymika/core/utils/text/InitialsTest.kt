package com.ai.heymika.core.utils.text

import org.junit.Assert.assertEquals
import org.junit.Test

class InitialsTest {
    @Test
    fun `two words give two initials`() {
        assertEquals("BS", initialsOf("Budi Santoso"))
    }

    @Test
    fun `more than two words use the first and the last`() {
        assertEquals("BP", initialsOf("Budi Santoso Putra"))
    }

    @Test
    fun `one word gives one uppercase initial`() {
        assertEquals("M", initialsOf("mika"))
    }

    @Test
    fun `blank and empty names give nothing`() {
        assertEquals("", initialsOf(""))
        assertEquals("", initialsOf("   "))
    }

    @Test
    fun `extra spaces are ignored`() {
        assertEquals("SN", initialsOf("  siti   nurhaliza  "))
    }

    @Test
    fun `punctuation before a letter is skipped and symbol-only words are dropped`() {
        assertEquals("AR", initialsOf("(Ani) - Rahma"))
    }

    @Test
    fun `digits count as initials`() {
        assertEquals("K1", initialsOf("Kelas 12"))
    }
}
