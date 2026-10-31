package com.ai.heymika.core.domain.password

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordPolicyTest {
    @Test
    fun `empty password scores none and meets nothing`() {
        val result = PasswordPolicy.evaluate("")
        assertEquals(PasswordStrength.NONE, result.strength)
        assertTrue(result.metRules.isEmpty())
        assertFalse(result.isAcceptable)
    }

    @Test
    fun `short lowercase password is weak`() {
        val result = PasswordPolicy.evaluate("mika")
        assertEquals(PasswordStrength.WEAK, result.strength)
        assertFalse(result.isAcceptable)
    }

    @Test
    fun `twelve lowercase letters are fair and acceptable`() {
        val result = PasswordPolicy.evaluate("abcdefghijkl")
        assertEquals(PasswordStrength.FAIR, result.strength)
        assertEquals(setOf(PasswordRule.MIN_LENGTH), result.metRules)
        assertTrue(result.isAcceptable)
    }

    @Test
    fun `eleven characters do not meet the length rule`() {
        assertFalse(PasswordPolicy.evaluate("abcdefghijk").isMet(PasswordRule.MIN_LENGTH))
    }

    @Test
    fun `long mixed case password is strong`() {
        val result = PasswordPolicy.evaluate("BelajarBarengMika")
        assertEquals(PasswordStrength.STRONG, result.strength)
        assertTrue(result.isMet(PasswordRule.MIXED_CASE))
        assertFalse(result.isMet(PasswordRule.DIGIT_OR_SYMBOL))
    }

    @Test
    fun `long mixed case password with a digit is very strong`() {
        val result = PasswordPolicy.evaluate("BelajarBareng2025")
        assertEquals(PasswordStrength.VERY_STRONG, result.strength)
        assertEquals(PasswordRule.entries.toSet(), result.metRules)
    }

    @Test
    fun `a symbol counts like a digit but whitespace does not`() {
        assertTrue(PasswordPolicy.evaluate("halo!").isMet(PasswordRule.DIGIT_OR_SYMBOL))
        assertFalse(PasswordPolicy.evaluate("halo mika").isMet(PasswordRule.DIGIT_OR_SYMBOL))
    }

    @Test
    fun `only uppercase is not mixed case`() {
        assertFalse(PasswordPolicy.evaluate("MIKAMIKAMIKA").isMet(PasswordRule.MIXED_CASE))
    }

    @Test
    fun `length counts code points so emoji count once`() {
        val elevenEmoji = "😀".repeat(11)
        assertFalse(PasswordPolicy.evaluate(elevenEmoji).isMet(PasswordRule.MIN_LENGTH))
        assertTrue(PasswordPolicy.evaluate(elevenEmoji + "a").isMet(PasswordRule.MIN_LENGTH))
    }
}
