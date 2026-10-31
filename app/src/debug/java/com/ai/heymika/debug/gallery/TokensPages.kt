package com.ai.heymika.debug.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.ui.component.foundation.ComicCard
import com.ai.heymika.core.ui.component.foundation.ComicCardVariant
import com.ai.heymika.core.ui.component.foundation.ComicDivider
import com.ai.heymika.core.ui.component.foundation.SubtleDivider
import com.ai.heymika.core.ui.modifier.comicSurface
import com.ai.heymika.core.ui.modifier.subtleSurface
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.core.ui.theme.ToneSpec

@Composable
fun TokensPage(
    title: String,
    onBack: () -> Unit,
) {
    val c = HeyMikaTheme.colors
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "HeyMikaColors", note = "HeyMikaTheme.colors.*") {
            SwatchGrid(
                listOf(
                    "ink" to c.ink,
                    "primary" to c.primary,
                    "onPrimary" to c.onPrimary,
                    "primaryText" to c.primaryText,
                    "primaryPressShadow" to c.primaryPressShadow,
                    "accent" to c.accent,
                    "onAccent" to c.onAccent,
                    "accentPressShadow" to c.accentPressShadow,
                    "comicShadow" to c.comicShadow,
                    "sky" to c.sky,
                    "yellow" to c.yellow,
                    "ribbonRed" to c.ribbonRed,
                    "badgeCyan" to c.badgeCyan,
                    "canvas" to c.canvas,
                    "surface" to c.surface,
                    "textPrimary" to c.textPrimary,
                    "textMuted" to c.textMuted,
                    "borderMuted" to c.borderMuted,
                    "danger" to c.danger,
                    "onDanger" to c.onDanger,
                    "dangerBg" to c.dangerBg,
                    "dangerPressShadow" to c.dangerPressShadow,
                    "success" to c.success,
                    "successBg" to c.successBg,
                    "warningBg" to c.warningBg,
                    "warningContent" to c.warningContent,
                    "info" to c.info,
                    "onInfo" to c.onInfo,
                    "focusRing" to c.focusRing,
                    "focusHalo" to c.focusHalo,
                    "subtleShadow" to c.subtleShadow,
                    "disabledContainer" to c.disabledContainer,
                    "disabledContent" to c.disabledContent,
                    "disabledBorder" to c.disabledBorder,
                    "primaryFixed" to c.primaryFixed,
                    "primaryFixedDim" to c.primaryFixedDim,
                    "onPrimaryFixed" to c.onPrimaryFixed,
                    "onPrimaryFixedVariant" to c.onPrimaryFixedVariant,
                    "secondaryFixed" to c.secondaryFixed,
                    "secondaryFixedDim" to c.secondaryFixedDim,
                    "onSecondaryFixed" to c.onSecondaryFixed,
                    "onSecondaryFixedVariant" to c.onSecondaryFixedVariant,
                    "tertiaryFixed" to c.tertiaryFixed,
                    "tertiaryFixedDim" to c.tertiaryFixedDim,
                    "onTertiaryFixed" to c.onTertiaryFixed,
                    "onTertiaryFixedVariant" to c.onTertiaryFixedVariant,
                ),
            )
        }
    }
}

@Composable
fun TonesPage(
    title: String,
    onBack: () -> Unit,
) {
    val t = HeyMikaTheme.tones
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "HeyMikaTones", note = "container / content / accent, setiap pasangan >= 4,5:1") {
            ToneGrid(
                listOf(
                    "blue" to t.blue,
                    "teal" to t.teal,
                    "orange" to t.orange,
                    "emerald" to t.emerald,
                    "rose" to t.rose,
                    "purple" to t.purple,
                    "slate" to t.slate,
                    "sky" to t.sky,
                    "yellow" to t.yellow,
                    "danger" to t.danger,
                    "success" to t.success,
                    "warning" to t.warning,
                    "info" to t.info,
                    "outline" to t.outline,
                    "primaryContainer" to t.primaryContainer,
                    "ribbonRed" to t.ribbonRed,
                    "tertiaryFixedDim" to t.tertiaryFixedDim,
                    "secondaryFixedDim" to t.secondaryFixedDim,
                ),
            )
        }
    }
}

@Composable
private fun SwatchGrid(swatches: List<Pair<String, Color>>) {
    val dimens = HeyMikaTheme.dimens
    GalleryGrid(items = swatches, columns = 2) { (name, color), cellModifier ->
        Row(
            modifier = cellModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(dimens.avatarXs)
                        .background(color, HeyMikaTheme.shapes.small)
                        .border(dimens.borderSubtle, HeyMikaTheme.colors.borderMuted, HeyMikaTheme.shapes.small),
            )
            Column {
                Text(
                    text = name,
                    style = HeyMikaTheme.typography.caption,
                    color = HeyMikaTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = color.toHex(),
                    style = HeyMikaTheme.typography.caption,
                    color = HeyMikaTheme.colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun ToneGrid(tones: List<Pair<String, ToneSpec>>) {
    val dimens = HeyMikaTheme.dimens
    GalleryGrid(items = tones, columns = 2) { (name, tone), cellModifier ->
        Row(
            modifier =
                cellModifier
                    .background(tone.container, HeyMikaTheme.shapes.small)
                    .padding(dimens.spaceSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(dimens.iconSize)
                        .background(tone.accent, HeyMikaTheme.shapes.pill),
            )
            Text(text = name, style = HeyMikaTheme.typography.labelMd, color = tone.content, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SurfacesPage(
    title: String,
    onBack: () -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    val shapes = HeyMikaTheme.shapes
    val colors = HeyMikaTheme.colors
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "Radius", note = "HeyMikaTheme.shapes.*") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
            ) {
                listOf(
                    "tag 6" to shapes.tag,
                    "small 8" to shapes.small,
                    "input 14" to shapes.input,
                    "button 14" to shapes.button,
                    "card 16" to shapes.card,
                    "cardLarge 24" to shapes.cardLarge,
                    "shell 28" to shapes.shell,
                    "sheet 28" to shapes.sheet,
                    "bubble 16" to shapes.speechBubble,
                    "pill" to shapes.pill,
                ).forEach { (name, shape) -> ShapeSpecimen(name = name, shape = shape) }
            }
        }
        GalleryGroup(title = "Border", note = "borderInk 3 · borderControl 2 · borderSubtle 1,5") {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus)) {
                BorderSpecimen("ink 3dp", dimens.borderInk, colors.ink, Modifier.weight(1f))
                BorderSpecimen("control 2dp", dimens.borderControl, colors.ink, Modifier.weight(1f))
                BorderSpecimen("subtle 1,5dp", dimens.borderSubtle, colors.borderMuted, Modifier.weight(1f))
            }
        }
        GalleryGroup(title = "Bayangan", note = "comicSurface (keras, tanpa blur) dan subtleSurface (float lembut)") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceLg),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceLg),
            ) {
                SurfaceSpecimen("Strict 6dp", Modifier.comicSurface())
                SurfaceSpecimen("Shell tanpa border", Modifier.comicSurface(shape = shapes.shell, borderWidth = 0.dp))
                SurfaceSpecimen(
                    "Zona bahaya",
                    Modifier.comicSurface(
                        color = colors.dangerBg,
                        shadowColor = colors.danger,
                        borderWidth = 0.dp,
                        shadowOffset = DpOffset(dimens.spaceXs, dimens.spaceXs),
                    ),
                )
                SurfaceSpecimen(
                    "Ledge Beranda",
                    Modifier.comicSurface(
                        shadowOffset = DpOffset(0.dp, dimens.spaceXs),
                        shadowColor = colors.borderMuted,
                        borderColor = colors.borderMuted,
                        borderWidth = dimens.borderSubtle,
                    ),
                )
                SurfaceSpecimen(
                    "Badge navy",
                    Modifier.comicSurface(
                        shape = shapes.pill,
                        shadowColor = colors.ink,
                        shadowOffset = DpOffset(dimens.spaceXxs, dimens.spaceXxs),
                    ),
                )
                SurfaceSpecimen("Subtle float", Modifier.subtleSurface())
            }
        }
    }
}

@Composable
fun SurfacesMorePage(
    title: String,
    onBack: () -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "ComicCard", note = "Strict untuk hero/terpilih, Subtle untuk yang berulang") {
            ComicCard(modifier = Modifier.fillMaxWidth()) {
                Text("Strict · hero Mika", style = HeyMikaTheme.typography.headlineSm)
                Text("Border ink 3dp dan bayangan oranye 6dp.", style = HeyMikaTheme.typography.bodySm)
            }
            ComicCard(modifier = Modifier.fillMaxWidth(), variant = ComicCardVariant.Subtle, onClick = {}) {
                Text("Subtle · bisa diklik", style = HeyMikaTheme.typography.labelLg)
                Text("Ripple, skala tekan 0,99 dan focus ring.", style = HeyMikaTheme.typography.bodySm)
            }
            ComicCard(
                modifier = Modifier.fillMaxWidth(),
                variant = ComicCardVariant.Subtle,
                onClick = {},
                enabled = false,
            ) {
                Text("Subtle · nonaktif", style = HeyMikaTheme.typography.labelLg)
            }
        }
        GalleryGroup(title = "Divider") {
            SpecimenLabel("ComicDivider (hanya di dalam shell Strict)")
            ComicDivider()
            SpecimenLabel("SubtleDivider")
            SubtleDivider()
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceMd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SpecimenLabel("Vertikal")
                ComicDivider(orientation = Orientation.Vertical)
                SubtleDivider(orientation = Orientation.Vertical)
                SpecimenLabel("butuh tinggi terbatas")
            }
        }
        GalleryGroup(title = "Spasi", note = "HeyMikaTheme.dimens.space*") {
            listOf(
                "xxs 2" to dimens.spaceXxs,
                "xs 4" to dimens.spaceXs,
                "sm 8" to dimens.spaceSm,
                "smPlus 12" to dimens.spaceSmPlus,
                "md 16" to dimens.spaceMd,
                "mdPlus 20" to dimens.spaceMdPlus,
                "lg 24" to dimens.spaceLg,
                "xl 32" to dimens.spaceXl,
                "xxl 48" to dimens.spaceXxl,
            ).forEach { (name, size) -> SpaceSpecimen(name, size) }
        }
    }
}

@Composable
private fun ShapeSpecimen(
    name: String,
    shape: Shape,
) {
    val dimens = HeyMikaTheme.dimens
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .size(dimens.avatarLg)
                    .background(HeyMikaTheme.colors.badgeCyan, shape)
                    .border(dimens.borderControl, HeyMikaTheme.colors.ink, shape),
        )
        SpecimenLabel(name)
    }
}

@Composable
private fun BorderSpecimen(
    name: String,
    width: Dp,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(HeyMikaTheme.dimens.avatarSm)
                    .background(HeyMikaTheme.colors.surface, HeyMikaTheme.shapes.button)
                    .border(width, color, HeyMikaTheme.shapes.button),
        )
        SpecimenLabel(name)
    }
}

@Composable
private fun SurfaceSpecimen(
    name: String,
    surface: Modifier,
) {
    val dimens = HeyMikaTheme.dimens
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .width(dimens.avatarXxl)
                    .height(dimens.avatarLg)
                    .then(surface),
        )
        SpecimenLabel(name, modifier = Modifier.padding(top = dimens.spaceSm))
    }
}

@Composable
private fun SpaceSpecimen(
    name: String,
    size: Dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeyMikaTheme.dimens.spaceSm),
    ) {
        SpecimenLabel(name, modifier = Modifier.width(HeyMikaTheme.dimens.avatarXl))
        Box(
            modifier =
                Modifier
                    .width(size)
                    .height(HeyMikaTheme.dimens.spaceMd)
                    .background(HeyMikaTheme.colors.primary),
        )
    }
}

@Composable
fun TypographyPage(
    title: String,
    onBack: () -> Unit,
) {
    val t = HeyMikaTheme.typography
    GalleryPage(title = title, onBack = onBack) {
        GalleryGroup(title = "Nunito Sans", note = "12sp adalah batas bawah") {
            listOf(
                Triple("displayHero", "32/40 · 900", t.displayHero),
                Triple("headlineLg", "26/34 · 800", t.headlineLg),
                Triple("headlineMd", "24/32 · 800", t.headlineMd),
                Triple("headlineSm", "20/28 · 700", t.headlineSm),
                Triple("sectionTitle", "20/28 · 800", t.sectionTitle),
                Triple("bodyLg", "18/28 · 400", t.bodyLg),
                Triple("bodyMd", "16/24 · 400", t.bodyMd),
                Triple("bodySm", "14/20 · 400", t.bodySm),
                Triple("labelLg", "16/22 · 700", t.labelLg),
                Triple("labelMd", "14/18 · 700", t.labelMd),
                Triple("labelSm", "12/16 · 700 · uppercase", t.labelSm),
                Triple("overline", "12/16 · 800 · uppercase", t.overline),
                Triple("caption", "12/16 · 500", t.caption),
                Triple("button", "16/22 · 700", t.button),
                Triple("buttonSmall", "14/18 · 700", t.buttonSmall),
                Triple("mono", "14/20 · 700 · timer 14:28", t.mono),
            ).forEach { (name, spec, style) -> TypeSpecimen(name, spec, style) }
        }
    }
}

@Composable
private fun TypeSpecimen(
    name: String,
    spec: String,
    style: TextStyle,
) {
    val upper = name == "labelSm" || name == "overline"
    Column {
        SpecimenLabel("$name · $spec")
        val sample = if (name == "mono") "14:28 · ID-2025-0042" else "Ayo belajar bareng Mika!"
        Text(
            text = if (upper) sample.uppercase() else sample,
            style = style,
            color = HeyMikaTheme.colors.textPrimary,
        )
    }
}
