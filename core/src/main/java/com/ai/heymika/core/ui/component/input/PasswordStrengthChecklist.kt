package com.ai.heymika.core.ui.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.core.ui.theme.ToneSpec

/**
 * One password rule as the checklist shows it. The caller (ViewModel) evaluates the password and
 * passes the result; this package never checks passwords itself.
 */
@Immutable
data class PasswordRuleItem(
    val label: String,
    val satisfied: Boolean,
)

/**
 * The visual strength levels of the 4-segment meter. Mapping a password onto a level is domain
 * logic and lives outside `ui/`; [filledSegments] is how many bars light up.
 */
enum class PasswordStrengthLevel(
    val filledSegments: Int,
) {
    None(0),
    Weak(1),
    Fair(2),
    Strong(3),
    VeryStrong(4),
}

/**
 * The password strength panel from register and reset password: an optional meter ("Kekuatan
 * Kata Sandi:" with a coloured level label over 4 bars) and the [rules] checklist, each rule a
 * success check or an empty circle.
 *
 * Pass [strength] = null to show the checklist only. [columns] = 2 lays the rules out as the
 * compact grid of the reset-password screen. TalkBack reads each rule with "Terpenuhi" or "Belum
 * terpenuhi" and announces level changes politely.
 */
@Composable
fun PasswordStrengthChecklist(
    rules: List<PasswordRuleItem>,
    modifier: Modifier = Modifier,
    strength: PasswordStrengthLevel? = null,
    columns: Int = 1,
) {
    val dimens = HeyMikaTheme.dimens
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow, HeyMikaTheme.shapes.input)
                .padding(dimens.spaceSmPlus),
        verticalArrangement = Arrangement.spacedBy(dimens.spaceSm),
    ) {
        if (strength != null) StrengthMeter(strength)
        val perRow = columns.coerceAtLeast(1)
        rules.chunked(perRow).forEach { rowRules ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
            ) {
                rowRules.forEach { rule ->
                    PasswordRuleRow(
                        rule = rule,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(perRow - rowRules.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun StrengthMeter(strength: PasswordStrengthLevel) {
    val dimens = HeyMikaTheme.dimens
    val tone = strengthTone(strength)
    val levelText = stringResource(strengthLabelRes(strength))
    val noneDescription = stringResource(R.string.controls_password_strength_none_description)
    Column(verticalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {},
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.controls_password_strength_title),
                modifier = Modifier.weight(1f),
                style = HeyMikaTheme.typography.labelSm,
                color = HeyMikaTheme.colors.textPrimary,
            )
            Text(
                text = levelText,
                modifier =
                    Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                        if (strength == PasswordStrengthLevel.None) contentDescription = noneDescription
                    },
                style = HeyMikaTheme.typography.overline,
                color = tone.content,
            )
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(dimens.progressTrackHeight)
                    .clearAndSetSemantics {},
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs),
        ) {
            repeat(PasswordStrengthLevel.VeryStrong.filledSegments) { index ->
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color =
                                    if (index < strength.filledSegments) {
                                        tone.accent
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                shape = HeyMikaTheme.shapes.pill,
                            ),
                )
            }
        }
    }
}

@Composable
private fun PasswordRuleRow(
    rule: PasswordRuleItem,
    modifier: Modifier = Modifier,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val state =
        stringResource(
            if (rule.satisfied) R.string.controls_password_rule_met else R.string.controls_password_rule_unmet,
        )
    Row(
        modifier = modifier.semantics(mergeDescendants = true) { stateDescription = state },
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = if (rule.satisfied) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            modifier = Modifier.size(dimens.iconSizeSmall),
            tint = if (rule.satisfied) colors.success else colors.textMuted,
        )
        Text(
            text = rule.label,
            style = HeyMikaTheme.typography.caption,
            color = colors.textMuted,
        )
    }
}

// Weak uses danger rather than the mockup's ribbon red: ribbonRed is never an error tone and its
// text fails contrast on the panel. Fair text uses warning content (#914C00), not orange (2.4:1).
@Composable
@ReadOnlyComposable
private fun strengthTone(strength: PasswordStrengthLevel): ToneSpec {
    val colors = HeyMikaTheme.colors
    val tones = HeyMikaTheme.tones
    return when (strength) {
        PasswordStrengthLevel.None -> ToneSpec(colors.surface, colors.textMuted, colors.borderMuted)
        PasswordStrengthLevel.Weak -> tones.danger
        PasswordStrengthLevel.Fair -> tones.warning
        PasswordStrengthLevel.Strong -> ToneSpec(colors.surface, colors.primaryText, colors.primary)
        PasswordStrengthLevel.VeryStrong -> tones.success
    }
}

private fun strengthLabelRes(strength: PasswordStrengthLevel): Int =
    when (strength) {
        PasswordStrengthLevel.None -> R.string.controls_password_strength_none
        PasswordStrengthLevel.Weak -> R.string.controls_password_strength_weak
        PasswordStrengthLevel.Fair -> R.string.controls_password_strength_fair
        PasswordStrengthLevel.Strong -> R.string.controls_password_strength_strong
        PasswordStrengthLevel.VeryStrong -> R.string.controls_password_strength_very_strong
    }

private val PreviewRules =
    listOf(
        PasswordRuleItem("Minimal 12 karakter aman (Wajib)", satisfied = true),
        PasswordRuleItem("Kombinasi huruf besar (A-Z) & huruf kecil (a-z)", satisfied = true),
        PasswordRuleItem("Mengandung angka (0-9) atau simbol unik", satisfied = false),
    )

@Preview(name = "Register: meter and checklist", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun PasswordStrengthChecklistPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PasswordStrengthChecklist(
                rules = PreviewRules,
                strength = PasswordStrengthLevel.Fair,
            )
            PasswordStrengthChecklist(
                rules = PreviewRules.map { it.copy(satisfied = false) },
                strength = PasswordStrengthLevel.None,
            )
        }
    }
}

@Preview(name = "All levels", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun PasswordStrengthLevelsPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PasswordStrengthLevel.entries.forEach { level ->
                PasswordStrengthChecklist(
                    rules = emptyList(),
                    strength = level,
                )
            }
        }
    }
}

@Preview(name = "Reset password grid", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun PasswordStrengthGridPreview() {
    HeyMikaTheme {
        PasswordStrengthChecklist(
            rules =
                listOf(
                    PasswordRuleItem("Min. 12 Karakter", satisfied = true),
                    PasswordRuleItem("Huruf Besar & Kecil", satisfied = true),
                    PasswordRuleItem("Angka (0-9)", satisfied = false),
                    PasswordRuleItem("Simbol (!@#\$%^&*)", satisfied = false),
                    PasswordRuleItem("Tidak sama dengan sandi lama", satisfied = true),
                ),
            modifier = Modifier.padding(16.dp),
            strength = PasswordStrengthLevel.Strong,
            columns = 2,
        )
    }
}
