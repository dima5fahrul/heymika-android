package com.ai.heymika.core.domain.password

/** One requirement shown in the password checklist. */
enum class PasswordRule {
    /** At least [PasswordPolicy.MIN_LENGTH] characters; the only rule sign-up enforces. */
    MIN_LENGTH,

    /** Both an uppercase and a lowercase letter. */
    MIXED_CASE,

    /** A digit or a symbol (anything that is not a letter or whitespace). */
    DIGIT_OR_SYMBOL,
}

/** Overall strength, one step per point of [PasswordPolicy.evaluate]'s score. */
enum class PasswordStrength {
    NONE,
    WEAK,
    FAIR,
    STRONG,
    VERY_STRONG,
}

/** The outcome of [PasswordPolicy.evaluate]. */
data class PasswordEvaluation(
    val metRules: Set<PasswordRule>,
    val strength: PasswordStrength,
) {
    /** True when the password may be submitted. */
    val isAcceptable: Boolean
        get() = PasswordRule.MIN_LENGTH in metRules

    fun isMet(rule: PasswordRule): Boolean = rule in metRules
}

/**
 * The sign-up and reset password policy. Scoring follows the mockups: one point for a non-empty
 * password and one per met [PasswordRule], so 0 points is [PasswordStrength.NONE] and 4 is
 * [PasswordStrength.VERY_STRONG]. Length counts code points, so an emoji is one character.
 */
object PasswordPolicy {
    const val MIN_LENGTH = 12

    fun evaluate(password: String): PasswordEvaluation {
        val codePoints = password.codePoints().toArray()
        val met = mutableSetOf<PasswordRule>()
        if (codePoints.size >= MIN_LENGTH) met += PasswordRule.MIN_LENGTH
        if (codePoints.any(Character::isUpperCase) && codePoints.any(Character::isLowerCase)) {
            met += PasswordRule.MIXED_CASE
        }
        if (codePoints.any { Character.isDigit(it) || isSymbol(it) }) met += PasswordRule.DIGIT_OR_SYMBOL
        val score = (if (codePoints.isNotEmpty()) 1 else 0) + met.size
        return PasswordEvaluation(metRules = met, strength = PasswordStrength.entries[score])
    }

    private fun isSymbol(codePoint: Int): Boolean = !Character.isLetter(codePoint) && !Character.isWhitespace(codePoint)
}
