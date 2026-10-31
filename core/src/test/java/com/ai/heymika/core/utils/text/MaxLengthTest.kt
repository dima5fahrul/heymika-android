package com.ai.heymika.core.utils.text

import org.junit.Assert.assertEquals
import org.junit.Test

class MaxLengthTest {
    @Test
    fun `an edit that fits passes unchanged`() {
        assertEquals("abcd", clampEditToMaxLength(old = "abc", new = "abcd", maxLength = 5))
    }

    @Test
    fun `deleting always passes, even above the limit`() {
        assertEquals("abcdef", clampEditToMaxLength(old = "abcdefg", new = "abcdef", maxLength = 3))
    }

    @Test
    fun `a paste at the end is cut to the room left`() {
        assertEquals("abcXY", clampEditToMaxLength(old = "abc", new = "abcXYZ", maxLength = 5))
    }

    @Test
    fun `a paste in the middle keeps the text after the cursor`() {
        assertEquals("abXYcd", clampEditToMaxLength(old = "abcd", new = "abXYZcd", maxLength = 6))
    }

    @Test
    fun `typing at the limit inserts nothing`() {
        assertEquals("abc", clampEditToMaxLength(old = "abc", new = "aXbc", maxLength = 3))
    }

    @Test
    fun `replacing a selection counts the removed text as room`() {
        assertEquals("aXYd", clampEditToMaxLength(old = "abcd", new = "aXYZWd", maxLength = 4))
    }

    @Test
    fun `repeated characters are matched as prefix first`() {
        assertEquals("aaaa", clampEditToMaxLength(old = "aa", new = "aaaaaa", maxLength = 4))
    }
}
