package com.ai.heymika.core.ui.component.display

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.modifier.comicFocusRing
import com.ai.heymika.core.ui.modifier.staticFocusRing
import com.ai.heymika.core.ui.modifier.subtleSurface
import com.ai.heymika.core.ui.theme.HeyMikaMotion
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * An expandable card: a Subtle card whose header (optional icon tile, title, subtitle and a chevron
 * that turns 180 degrees) toggles the [content] below it. Use it for checkout benefits, the
 * integrity rules on Statistik and the flashcard hint drawers.
 *
 * - Motion: the content expands and fades over 200ms. Under reduced motion (animator scale 0) it
 *   snaps open and shut, and so does the chevron.
 * - Semantics: the header is one Role.Button with the state "Diperluas"/"Diciutkan", a click label,
 *   and expand/collapse accessibility actions.
 * - Focus: the focus ring wraps the whole card.
 *
 * This overload is stateless. The other overload keeps the state with rememberSaveable.
 */
@Composable
fun Accordion(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    AccordionImpl(
        title = title,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        card = true,
        modifier = modifier,
        subtitle = subtitle,
        icon = icon,
        enabled = enabled,
        content = content,
    )
}

/** [Accordion] that remembers its own expanded state across recomposition and process death. */
@Composable
fun Accordion(
    title: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    subtitle: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    Accordion(
        title = title,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
        subtitle = subtitle,
        icon = icon,
        enabled = enabled,
        content = content,
    )
}

/**
 * The inline variant of [Accordion], with no card shell, for use inside a card or a list row ("Lihat
 * pembahasan" drawers, expandable mistake rows). It has the same header, motion and semantics; the
 * focus ring wraps only the header.
 */
@Composable
fun ExpandableSection(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    AccordionImpl(
        title = title,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        card = false,
        modifier = modifier,
        subtitle = subtitle,
        icon = icon,
        enabled = enabled,
        content = content,
    )
}

/** [ExpandableSection] that remembers its own expanded state across recomposition and process death. */
@Composable
fun ExpandableSection(
    title: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    subtitle: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    ExpandableSection(
        title = title,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
        subtitle = subtitle,
        icon = icon,
        enabled = enabled,
        content = content,
    )
}

@Composable
internal fun AccordionImpl(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    card: Boolean,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    previewFocused: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    val shapes = HeyMikaTheme.shapes
    val reducedMotion = HeyMikaTheme.reducedMotion
    val source = remember { MutableInteractionSource() }
    val ringShape = if (card) shapes.card else shapes.small
    val ring = if (previewFocused) Modifier.staticFocusRing(ringShape) else Modifier.comicFocusRing(source, ringShape)
    val shell =
        if (card) {
            ring
                .subtleSurface(shape = shapes.card)
                .clip(shapes.card)
        } else {
            Modifier
        }
    val headerShape =
        if (card) {
            Modifier
        } else {
            ring.clip(shapes.small)
        }
    Column(modifier = modifier.then(shell)) {
        AccordionHeader(
            title = title,
            subtitle = subtitle,
            icon = icon,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            enabled = enabled,
            source = source,
            contentPadding =
                if (card) {
                    PaddingValues(horizontal = dimens.cardPaddingCompact, vertical = dimens.spaceSm)
                } else {
                    PaddingValues(horizontal = dimens.spaceXs, vertical = dimens.spaceXs)
                },
            modifier = headerShape,
        )
        AnimatedVisibility(
            visible = expanded,
            enter =
                if (reducedMotion) {
                    EnterTransition.None
                } else {
                    expandVertically(
                        animationSpec = tween(HeyMikaMotion.EXPAND_MILLIS),
                        expandFrom = Alignment.Top,
                    ) + fadeIn(animationSpec = tween(HeyMikaMotion.EXPAND_MILLIS))
                },
            exit =
                if (reducedMotion) {
                    ExitTransition.None
                } else {
                    shrinkVertically(
                        animationSpec = tween(HeyMikaMotion.EXPAND_MILLIS),
                        shrinkTowards = Alignment.Top,
                    ) + fadeOut(animationSpec = tween(HeyMikaMotion.EXPAND_MILLIS))
                },
            label = "accordionContent",
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = if (card) dimens.cardPaddingCompact else dimens.spaceXs,
                            end = if (card) dimens.cardPaddingCompact else dimens.spaceXs,
                            top = dimens.spaceXs,
                            bottom = if (card) dimens.cardPaddingCompact else dimens.spaceXs,
                        ),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceSm),
                content = content,
            )
        }
    }
}

@Composable
private fun AccordionHeader(
    title: String,
    subtitle: String?,
    icon: ImageVector?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    enabled: Boolean,
    source: MutableInteractionSource,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val colors = HeyMikaTheme.colors
    val dimens = HeyMikaTheme.dimens
    val stateLabel =
        stringResource(if (expanded) R.string.display_accordion_expanded else R.string.display_accordion_collapsed)
    val actionLabel =
        stringResource(if (expanded) R.string.display_accordion_collapse else R.string.display_accordion_expand)
    val rotation by animateFloatAsState(
        targetValue = if (expanded) CHEVRON_EXPANDED_DEGREES else 0f,
        animationSpec = if (HeyMikaTheme.reducedMotion) snap() else tween(HeyMikaMotion.EXPAND_MILLIS),
        label = "accordionChevron",
    )
    val titleColor = if (enabled) colors.textPrimary else colors.disabledContent
    val mutedColor = if (enabled) colors.textMuted else colors.disabledContent
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = dimens.minTouchTarget)
                .clickable(
                    interactionSource = source,
                    indication = ripple(),
                    enabled = enabled,
                    onClickLabel = actionLabel,
                    role = Role.Button,
                    onClick = { onExpandedChange(!expanded) },
                ).semantics {
                    stateDescription = stateLabel
                    if (enabled) {
                        if (expanded) {
                            collapse {
                                onExpandedChange(false)
                                true
                            }
                        } else {
                            expand {
                                onExpandedChange(true)
                                true
                            }
                        }
                    }
                }.padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                modifier =
                    Modifier
                        .size(dimens.iconSize + dimens.spaceSmPlus)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, HeyMikaTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(dimens.iconSize),
                    tint = if (enabled) colors.primaryText else colors.disabledContent,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = titleColor,
                style = HeyMikaTheme.typography.labelLg,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = mutedColor,
                    style = HeyMikaTheme.typography.caption,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ExpandMore,
            contentDescription = null,
            modifier =
                Modifier
                    .size(dimens.iconSizeLarge)
                    .graphicsLayer { rotationZ = rotation },
            tint = mutedColor,
        )
    }
}

private const val CHEVRON_EXPANDED_DEGREES = 180f

@Composable
private fun PreviewRules() {
    listOf(
        "Akurasi Mandiri Murni" to "Skor kuis hanya dihitung dari percobaan pertama tanpa bantuan.",
        "Flashcard Terpisah dari Kuis" to "Self-assessment hafalan dipisah dari akurasi objektif.",
    ).forEach { (title, body) ->
        Column {
            Text(text = title, style = HeyMikaTheme.typography.labelMd)
            Text(text = body, style = HeyMikaTheme.typography.bodySm, color = HeyMikaTheme.colors.textMuted)
        }
    }
}

@Preview(name = "Accordion card", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun AccordionPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Accordion(
                title = "Prinsip Integritas Akademik",
                subtitle = "4 Aturan Transparansi Skor HeyMika",
                icon = Icons.Outlined.Policy,
                expanded = false,
                onExpandedChange = {},
            ) { PreviewRules() }
            Accordion(
                title = "Prinsip Integritas Akademik",
                subtitle = "4 Aturan Transparansi Skor HeyMika",
                icon = Icons.Outlined.Policy,
                expanded = true,
                onExpandedChange = {},
            ) { PreviewRules() }
            Accordion(title = "Benefit Paket Pro", initiallyExpanded = false) { PreviewRules() }
            Accordion(title = "Tidak aktif", expanded = false, onExpandedChange = {}, enabled = false) {
                PreviewRules()
            }
        }
    }
}

@Preview(name = "ExpandableSection and focus", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun ExpandableSectionPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ExpandableSection(
                title = "Lihat pembahasan",
                icon = Icons.Outlined.School,
                expanded = true,
                onExpandedChange = {},
            ) {
                Text(text = "Dijkstra berprinsip greedy, Bellman-Ford memakai dynamic programming.")
            }
            ExpandableSection(title = "Lihat pembahasan", expanded = false, onExpandedChange = {}) {
                Text(text = "Isi tersembunyi")
            }
            AccordionImpl(
                title = "Fokus (keyboard)",
                expanded = false,
                onExpandedChange = {},
                card = true,
                previewFocused = true,
            ) { PreviewRules() }
            AccordionImpl(
                title = "Fokus inline",
                expanded = false,
                onExpandedChange = {},
                card = false,
                previewFocused = true,
            ) { PreviewRules() }
        }
    }
}
