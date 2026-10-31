package com.ai.heymika.core.ui.component.input

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.ai.heymika.core.R
import com.ai.heymika.core.domain.password.PasswordEvaluation
import com.ai.heymika.core.domain.password.PasswordPolicy
import com.ai.heymika.core.domain.password.PasswordRule
import com.ai.heymika.core.domain.password.PasswordStrength

/** The meter level [PasswordStrengthChecklist] shows for a [PasswordStrength]. */
fun PasswordStrength.toLevel(): PasswordStrengthLevel =
    when (this) {
        PasswordStrength.NONE -> PasswordStrengthLevel.None
        PasswordStrength.WEAK -> PasswordStrengthLevel.Weak
        PasswordStrength.FAIR -> PasswordStrengthLevel.Fair
        PasswordStrength.STRONG -> PasswordStrengthLevel.Strong
        PasswordStrength.VERY_STRONG -> PasswordStrengthLevel.VeryStrong
    }

/** The checklist label of a [PasswordRule]; MIN_LENGTH takes [PasswordPolicy.MIN_LENGTH] as `%1$d`. */
@StringRes
fun PasswordRule.labelRes(): Int =
    when (this) {
        PasswordRule.MIN_LENGTH -> R.string.controls_password_rule_min_length
        PasswordRule.MIXED_CASE -> R.string.controls_password_rule_mixed_case
        PasswordRule.DIGIT_OR_SYMBOL -> R.string.controls_password_rule_digit_or_symbol
    }

/**
 * The checklist rows for this evaluation, one per [PasswordRule] in declaration order, so every
 * auth screen shows the same labels as the design gallery.
 */
@Composable
@ReadOnlyComposable
fun PasswordEvaluation.toRuleItems(): List<PasswordRuleItem> =
    PasswordRule.entries.map { rule ->
        val label =
            if (rule == PasswordRule.MIN_LENGTH) {
                stringResource(rule.labelRes(), PasswordPolicy.MIN_LENGTH)
            } else {
                stringResource(rule.labelRes())
            }
        PasswordRuleItem(label = label, satisfied = isMet(rule))
    }
