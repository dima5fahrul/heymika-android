package com.ai.heymika.core.ui.component.input

import com.ai.heymika.core.R
import com.ai.heymika.core.domain.password.PasswordRule
import com.ai.heymika.core.domain.password.PasswordStrength
import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordStrengthMappingTest {
    @Test
    fun `each strength lights one more segment`() {
        PasswordStrength.entries.forEachIndexed { index, strength ->
            assertEquals(index, strength.toLevel().filledSegments)
        }
    }

    @Test
    fun `strength maps to the level of the same name`() {
        assertEquals(PasswordStrengthLevel.None, PasswordStrength.NONE.toLevel())
        assertEquals(PasswordStrengthLevel.Weak, PasswordStrength.WEAK.toLevel())
        assertEquals(PasswordStrengthLevel.Fair, PasswordStrength.FAIR.toLevel())
        assertEquals(PasswordStrengthLevel.Strong, PasswordStrength.STRONG.toLevel())
        assertEquals(PasswordStrengthLevel.VeryStrong, PasswordStrength.VERY_STRONG.toLevel())
    }

    @Test
    fun `every rule has its own label`() {
        assertEquals(R.string.controls_password_rule_min_length, PasswordRule.MIN_LENGTH.labelRes())
        assertEquals(R.string.controls_password_rule_mixed_case, PasswordRule.MIXED_CASE.labelRes())
        assertEquals(R.string.controls_password_rule_digit_or_symbol, PasswordRule.DIGIT_OR_SYMBOL.labelRes())
    }
}
