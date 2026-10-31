# HeyMika design system: P0 foundation contract

This is the API that the P0 parallel agents build on. Everything listed here compiles; the
verification command ran green. The signatures are copied from the source. The foundation files are
**frozen** during the parallel stage. If you need a change, ask the integrator; do not edit
foundation files yourself.

Product decisions that apply to everything:
- Light theme only and phone only. There is no `darkTheme` parameter, no tablet or adaptive layout,
  and no `material3-adaptive`.
- Primary `#0075E8` is for fills, focus and active states. `primaryText #005AB5` is for links and
  small blue text on white.
- The bottom nav is Beranda / Materi / Kuis / Tanya Mika / Profil.
- There is no backend yet; data is dummy.

Toolchain: Compose BOM 2025.01.00, which gives Compose UI and foundation **1.7.6** and Material3
**1.3.1**. Material3 1.3.1 has **no** `primaryFixed*` slots, no `dropShadow`, and no expressive
typography. Use the extended tokens below instead. `ripple()` and
`minimumInteractiveComponentSize()` are available.

---

## 1. Theme (`com.ai.heymika.core.ui.theme`)

```kotlin
@Composable fun HeyMikaTheme(content: @Composable () -> Unit)

object HeyMikaTheme {                       // all getters are @Composable @ReadOnlyComposable
    val colors: HeyMikaColors
    val typography: HeyMikaTypography
    val shapes: HeyMikaShapes
    val dimens: HeyMikaDimens
    val tones: HeyMikaTones
    val reducedMotion: Boolean              // system animator scale == 0
}
```

`HeyMikaTheme` provides the following:
- `MaterialTheme(HeyMikaLightColorScheme, HeyMikaMaterialTypography, HeyMikaMaterialShapes)`.
- All the HeyMika locals.
- `LocalTonalElevationEnabled = false`, so tonal elevation never tints a surface.
- `LocalContentColor = onBackground #001B3C`, so default text is navy and never black.
- A ripple configuration with **focusedAlpha = 0**: the comic focus ring is the only focus
  indicator.

Wrap every preview in `HeyMikaTheme { }`.

Public locals: `LocalHeyMikaColors`, `LocalHeyMikaTypography`, `LocalHeyMikaShapes`,
`LocalHeyMikaDimens`, `LocalHeyMikaTones` (all `staticCompositionLocalOf`), and
`LocalReducedMotion` (`compositionLocalOf`, default false).

### 1.1 Colors

`object HeyMikaPalette` holds every raw colour (public, so tests and modifier defaults can use it;
UI code should read `HeyMikaTheme.colors` or `MaterialTheme.colorScheme` instead):

| Group | Tokens |
|---|---|
| Brand | `ink #082A52`, `primary #0075E8`, `primaryPressShadow #005FC4`, `primaryText #005AB5`, `shadowOrange #FF8A00` |
| Accents | `brandSky #16B5F5`, `brandYellow #FFD34E`, `ribbonRed #EF3E36`, `badgeCyan #C5EFFF`, `canvas #F8FCFF`, `borderMuted #D8DDF4`, `textMuted #5868A4`, `danger #BC2535`, `dangerBg #FFF0F2`, `success #167549` |
| Derived | `successBg #E8F1ED` (success 10% on white), `subtleShadow #0F082A52` (ink 6%), `focusHalo #400075E8` (primary 25%) |
| YAML slots | `surface`, `surfaceDim`, `surfaceBright`, `surfaceContainerLowest/Low/""/High/Highest`, `onSurface`, `onSurfaceVariant`, `inverseSurface`, `inverseOnSurface`, `outline`, `outlineVariant`, `surfaceTint #005CBA`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`, `inversePrimary`, `secondary`, `onSecondary`, `secondaryContainer`, `onSecondaryContainer`, `tertiary`, `onTertiary`, `tertiaryContainer`, `onTertiaryContainer`, `onError`, `primaryFixed`, `primaryFixedDim`, `onPrimaryFixed`, `onPrimaryFixedVariant`, `secondaryFixed`, `secondaryFixedDim`, `onSecondaryFixed`, `onSecondaryFixedVariant`, `tertiaryFixed`, `tertiaryFixedDim`, `onTertiaryFixed`, `onTertiaryFixedVariant`, `onBackground`, `surfaceVariant` |
| Reference only (never used) | `yamlPrimary #005AB5`, `yamlBackground #F9F9FF`, `yamlError #BA1A1A`, `yamlErrorContainer #FFDAD6`, `yamlOnErrorContainer #93000A` |

`val HeyMikaLightColorScheme: ColorScheme` is filled from every slot that Material3 1.3 has, with
these overrides:
- `primary = #0075E8`
- `background = canvas #F8FCFF`
- `error = danger #BC2535`, `errorContainer = dangerBg #FFF0F2`, `onErrorContainer = danger`
- `surfaceTint = #005CBA`, as in the YAML

`@Immutable class HeyMikaColors(...)` holds the tokens that have no M3 slot, read as
`HeyMikaTheme.colors.x`:

```
ink, primary, onPrimary, primaryText, primaryPressShadow,
accent (#FF8A00), onAccent (ink), accentPressShadow (#914C00), comicShadow (#FF8A00),
sky, yellow, ribbonRed, badgeCyan, canvas, surface (#FFFFFF), textPrimary (#001B3C), textMuted, borderMuted,
danger, onDanger, dangerBg, dangerPressShadow (#6F3900), inverseDanger (#FFB4AB, error icon on the inverse-surface toast), success, successBg,
warningBg (#FFDCC4), warningContent (#914C00), info (badgeCyan), onInfo (ink),
focusRing (#0075E8), focusHalo (primary 25%), subtleShadow (ink 6%),
disabledContainer (#E7EEFF), disabledContent (textMuted), disabledBorder (borderMuted),
primaryFixed, primaryFixedDim, onPrimaryFixed, onPrimaryFixedVariant,
secondaryFixed, secondaryFixedDim, onSecondaryFixed, onSecondaryFixedVariant,
tertiaryFixed, tertiaryFixedDim, onTertiaryFixed, onTertiaryFixedVariant
```

Contrast pairs to respect:
- Blue text on white is `primaryText` (6.7:1), never `primary` (4.46:1).
- Labels on orange use `onAccent` = ink (6:1). White on `#FF8A00` is only 2.4:1, so never use it
  for text.
- `ribbonRed` is for the unread dot, the mascot bow and urgency. It is never used for errors or for
  white text.
- Errors always use `danger` / `dangerBg`.

### 1.2 Tones

```kotlin
@Immutable data class ToneSpec(val container: Color, val content: Color, val accent: Color)
@Immutable class HeyMikaTones(
    blue, teal, orange, emerald, rose, purple, slate, sky, yellow,
    danger, success, warning, info, outline, primaryContainer, ribbonRed,
    tertiaryFixedDim, secondaryFixedDim,   // all ToneSpec
)
```

- `container` is the tinted background.
- `content` is the text and icon colour on `container`. Every pair is at least 4.5:1.
- `accent` is the saturated colour for a progress fill, dot, border or hard shadow.

Domain enums such as WorkspaceCategory and MasteryBand are mapped onto these names in the phase that
introduces the enum. P0 components take a `ToneSpec` parameter rather than an enum.

### 1.3 Typography

- `val NunitoSans: FontFamily` uses the 6 bundled weights, 400 to 900.
- `@Immutable class HeyMikaTypography` is read as `HeyMikaTheme.typography.x`:

| Token | sp / line / weight / tracking | Use |
|---|---|---|
| `displayHero` | 32/40/900/-0.01em | screen hero ("Materimu") |
| `headlineLg` | 26/34/800/-0.01em | auth and page titles |
| `headlineMd` | 24/32/800 | |
| `headlineSm` | 20/28/700 | card and dialog titles |
| `sectionTitle` | 20/28/800/-0.005em | section headers |
| `bodyLg` | 18/28/400 | lead paragraph |
| `bodyMd` | 16/24/400 | body, input text |
| `bodySm` | 14/20/400 | secondary copy |
| `labelLg` | 16/22/700 | list titles |
| `labelMd` | 14/18/700/+0.02em | field labels, alert titles, chips, tabs |
| `labelSm` | 12/16/700/+0.05em | uppercase pills (call `.uppercase()` on the text) |
| `overline` | 12/16/800/+0.06em | uppercase kicker above a title |
| `caption` | 12/16/500 | helper text, timestamps |
| `button` | 16/22/700 | Large and Medium buttons |
| `buttonSmall` | 14/18/700 | Small buttons |
| `mono` | JetBrains Mono (bundled) 14/20/700 | timers, IDs, code chips |
| `displayHeroLarge` / `headlineLgLarge` | 48/56/900, 36/44/800 | tablet only; not used in v1 |

12sp is the floor. The mockups' 10-11px labels round up to 12.

`val HeyMikaMaterialTypography: Typography` maps the tokens onto the M3 slots, so stock M3
components render in Nunito Sans:

| M3 slot | Token |
|---|---|
| displayLarge | displayHeroLarge |
| displayMedium | headlineLgLarge |
| displaySmall | displayHero |
| headlineLarge | headlineLg |
| headlineMedium | headlineMd |
| headlineSmall, titleLarge | headlineSm |
| titleMedium, labelLarge | labelLg |
| titleSmall, labelMedium | labelMd |
| bodyLarge | bodyMd (16) |
| bodyMedium | bodySm (14) |
| bodySmall | caption |
| labelSmall | labelSm |

### 1.4 Shapes

`@Immutable class HeyMikaShapes` (all `CornerBasedShape`):

| Shape | Radius | Use |
|---|---|---|
| `tag` | 6dp | tags, code badges |
| `small` | 8dp | small chips |
| `input` | 14dp | text fields, pickers |
| `button` | 14dp | buttons, selectable rows |
| `card` | 16dp | cards and nested panels |
| `cardLarge` | 24dp | hero cards |
| `shell` | 28dp | auth and hero shells |
| `dialog` | 28dp | ComicDialog |
| `sheet` | 28dp, top corners only | bottom sheets |
| `speechBubble` | 16dp | speech bubble body |
| `pill` | `CircleShape` | pills, chips, avatars, status badges |

`val HeyMikaMaterialShapes: Shapes` maps them onto M3: extraSmall 6, small 8, medium 16, large 24,
extraLarge 28.

### 1.5 Dimens

`@Immutable class HeyMikaDimens` (all `Dp`):

| Group | Tokens |
|---|---|
| Borders | `borderInk 3`, `borderControl 2`, `borderSubtle 1.5` |
| Shadows | `comicShadowOffset 6` (x and y), `buttonShadow 4`, `buttonShadowPressed 2`, `pressTranslate 2`, `subtleFloatBlur 16`, `subtleFloatOffsetY 4` |
| Controls | `buttonHeightLarge 56`, `buttonHeightMedium 48`, `buttonHeightSmall 40`, `inputHeight 56`, `minTouchTarget 48`, `checkboxSize 20`, `progressTrackHeight 8` |
| Icons | `iconSizeSmall 16`, `iconSize 20`, `iconSizeLarge 24` |
| Chrome | `topBarHeight 64`, `bottomNavHeight 64` (Beranda mockup: flat five-tab bar), `bottomNavCenterTab 56` |
| Avatars | `avatarXs 32`, `avatarSm 40`, `avatarMd 56`, `avatarLg 64`, `avatarXl 80`, `avatarXxl 96`, `avatarHero 144` |
| Radii | `radiusTag 6`, `radiusSmall 8`, `radiusControl 14`, `radiusCard 16`, `radiusCardLarge 24`, `radiusShell 28` |
| Spacing | `spaceXxs 2`, `spaceXs 4`, `spaceSm 8`, `spaceSmPlus 12`, `spaceMd 16`, `spaceMdPlus 20`, `spaceLg 24`, `spaceXl 32`, `spaceXxl 48` |
| Layout | `screenMargin 16`, `sectionGap 24`, `cardPadding 16`, `cardPaddingCompact 12`, `shellPadding 20` |
| Focus | `focusRingGap 2`, `focusRingWidth 3` |

### 1.6 Motion

```kotlin
object HeyMikaMotion {
    const val PRESS_MILLIS = 150
    const val EXPAND_MILLIS = 200
    const val PULSE_MILLIS = 1_200
    const val STRIPE_LOOP_MILLIS = 1_000
}
@Composable fun rememberReducedMotion(): Boolean   // HeyMikaTheme already provides it; read HeyMikaTheme.reducedMotion
```

When `HeyMikaTheme.reducedMotion` is true:
- Infinite loops (pulse, stripes) must stop.
- Expand, collapse and dialogs must snap (use `snap()` instead of `tween`).

---

## 2. Modifiers (`com.ai.heymika.core.ui.modifier`)

```kotlin
fun Modifier.comicSurface(
    shape: Shape = ComicSurfaceDefaults.shape,                 // card 16dp
    color: Color = ComicSurfaceDefaults.color,                 // white
    borderColor: Color = ComicSurfaceDefaults.borderColor,     // ink
    borderWidth: Dp = ComicSurfaceDefaults.borderWidth,        // 3dp
    shadowColor: Color = ComicSurfaceDefaults.shadowColor,     // #FF8A00
    shadowOffset: DpOffset = ComicSurfaceDefaults.shadowOffset, // 6dp, 6dp
): Modifier
```

`comicSurface` draws a hard offset shadow with no blur, BEHIND a solid face, with the border ON TOP
of the content.
- **The shadow is outside the bounds.** Reserve at least the offset at the end and bottom (8dp+
  spacing between siblings, or `contentPadding` at the end of a LazyColumn).
- Add `Modifier.clip(shape)` after it for full-bleed images.
- Recipes:
  - Borderless login shell: `borderWidth = 0.dp`.
  - Danger zone: `color = dangerBg, shadowColor = danger, borderWidth = 0.dp, shadowOffset = DpOffset(4.dp, 4.dp)`.
  - Beranda "ledge" card: `shadowOffset = DpOffset(0.dp, 4.dp), shadowColor = borderMuted, borderColor = borderMuted, borderWidth = 1.5.dp`.
  - Navy offset badge: `shadowColor = ink, shadowOffset = DpOffset(2.dp, 2.dp)`.

```kotlin
fun Modifier.subtleSurface(
    shape: Shape = SubtleSurfaceDefaults.shape,                // card 16dp
    color: Color = SubtleSurfaceDefaults.color,                // white
    borderColor: Color = SubtleSurfaceDefaults.borderColor,    // borderMuted
    borderWidth: Dp = SubtleSurfaceDefaults.borderWidth,       // 1.5dp
    floatColor: Color = SubtleSurfaceDefaults.floatColor,      // ink 6%
    floatBlur: Dp = SubtleSurfaceDefaults.floatBlur,           // 16dp
    floatOffsetY: Dp = SubtleSurfaceDefaults.floatOffsetY,     // 4dp
): Modifier
```

`subtleSurface` is the "Subtle float": a soft ambient shadow (CSS `0 4px 16px rgba(8,42,82,.06)`), a
solid face and a thin border. It is the surface for list, feed and utility cards.

```kotlin
fun Modifier.comicFocusRing(
    interactionSource: InteractionSource,
    shape: Shape = ComicFocusRingDefaults.shape,   // button 14dp
    color: Color = ComicFocusRingDefaults.color,   // #0075E8
    width: Dp = ComicFocusRingDefaults.width,      // 3dp
    gap: Dp = ComicFocusRingDefaults.gap,          // 2dp
): Modifier

fun Modifier.comicFocusRing(                       // focus-event driven: place BEFORE the focusable/clickable it watches
    shape: Shape = ComicFocusRingDefaults.shape,
    color: Color = ComicFocusRingDefaults.color,
    width: Dp = ComicFocusRingDefaults.width,
    gap: Dp = ComicFocusRingDefaults.gap,
): Modifier
// ComicFocusRingDefaults also exposes `halo` (primary 25%).
```

`comicFocusRing` draws a ring outside the bounds while the element is focused, so layout never
shifts.
- **Pass the SAME `MutableInteractionSource`** as your `clickable`, `toggleable`, `selectable` or
  text field.
- Apply it **before** any `clip`.
- For a clickable, the ring shows only for keyboard, D-pad or TAB focus; touch never focuses a
  clickable. For a text field it shows while editing.
- Text-field recipe (DESIGN "border turns primary + 3px halo"): set the field's own border to
  `primary`, plus
  `Modifier.comicFocusRing(source, shape = shapes.input, color = ComicFocusRingDefaults.halo, gap = 0.dp)`.

```kotlin
@Composable
fun animatePressTravel(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    travel: Dp = HeyMikaTheme.dimens.pressTranslate,          // 2dp
): State<Dp>                                                  // 150ms tween; snaps under reduced motion

fun Modifier.pressTranslate(travel: () -> Dp): Modifier       // placement-phase offset; no recomposition per frame

fun Modifier.comicPressSurface(
    travel: () -> Dp,
    color: Color,
    baseColor: Color,
    shape: Shape = PressSurfaceDefaults.shape,                // button 14dp
    baseHeight: Dp = PressSurfaceDefaults.baseHeight,         // 4dp
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
): Modifier
// PressSurfaceDefaults also exposes `travel` (2dp).
```

`comicPressSurface` is the raised-button look: a face on a hard base.
- Its bounds are face + `baseHeight`, so the base is inside the layout.
- The face sinks by `travel()`, clamped to `0..baseHeight`. The base never moves, so the visible
  ledge goes from 4dp to 2dp when pressed.
- Apply `clickable`, `comicFocusRing` and semantics **before** it.
- `baseColor = Color.Transparent` gives a flat face.
- Typical use (send FAB, active segment, selected option card):

```kotlin
val source = remember { MutableInteractionSource() }
val travel by animatePressTravel(source)
Box(
    Modifier
        .comicFocusRing(source, shape)
        .clickable(source, indication = null, role = Role.Button, onClick = onClick)
        .comicPressSurface({ travel }, color = c.primary, baseColor = c.primaryPressShadow, shape = shape)
)
```

---

## 3. Foundation components (`com.ai.heymika.core.ui.component.foundation`)

```kotlin
enum class ComicButtonVariant { Primary, Secondary, Outline, Tonal, Ghost, Danger }
enum class ComicButtonSize { Large, Medium, Small }             // face 56 / 48 / 40dp

@Immutable
data class ComicButtonColors(
    val container: Color, val content: Color, val border: Color, val base: Color,
    val disabledContainer: Color, val disabledContent: Color, val disabledBorder: Color, val disabledBase: Color,
)

object ComicButtonDefaults {                                    // all @Composable @ReadOnlyComposable
    fun colors(variant: ComicButtonVariant): ComicButtonColors
    fun borderWidth(variant: ComicButtonVariant): Dp            // Secondary 3dp, others 2dp (drawn only if border != Transparent)
    fun height(size: ComicButtonSize): Dp
    fun contentPadding(size: ComicButtonSize): PaddingValues    // horizontal 24 / 20 / 12
    fun iconSize(size: ComicButtonSize): Dp                     // 20 / 20 / 16
    fun textStyle(size: ComicButtonSize): TextStyle             // button / button / buttonSmall
}

@Composable
fun ComicButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ComicButtonVariant = ComicButtonVariant.Primary,
    size: ComicButtonSize = ComicButtonSize.Large,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingText: String? = null,
    fullWidth: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    colors: ComicButtonColors = ComicButtonDefaults.colors(variant),
    interactionSource: MutableInteractionSource? = null,
)

@Composable
fun ComicButton(                                               // slot version: Google "G", trailing pill, custom content
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ComicButtonVariant = ComicButtonVariant.Primary,
    size: ComicButtonSize = ComicButtonSize.Large,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
    colors: ComicButtonColors = ComicButtonDefaults.colors(variant),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
)
```

Button behaviour:
- **Layout height = face + 4dp base** for raised variants, so Large is 60dp and Small is 44dp. The
  touch target is always at least 48dp. Ghost is flat: its height equals the face.
- Pressing sinks the face 2dp over 150ms, and raised variants get no ripple. Ghost gets a ripple
  clipped to its shape.
- Every button has the focus ring and `Role.Button`.
- Disabled: the face is `#E7EEFF` with a `textMuted` label and a `borderMuted` ledge.
- Loading: a spinner plus `loadingText ?: text`, with icons hidden. Clicks are ignored, and the
  state description is `R.string.foundation_loading` ("Memuat…").
- Labels are single-line with an ellipsis.

| Variant | Fill / label / border / base | When to use |
|---|---|---|
| Primary | `#0075E8` / white / none / `#005FC4` | the ONE main action of a screen or card |
| Secondary | `#FF8A00` / ink / 3dp ink / `#914C00` | orange accent for celebration and gamification CTAs ("Tanya Mika", "Tambah Materi", quiz start) |
| Outline | white / ink / 2dp ink / ink | a strong neutral alternative |
| Tonal | `surfaceContainerHigh #DEE8FF` / `onSurface` / none / `borderMuted` | soft secondary actions beside a Primary: error-state "Buka Riwayat" and "Lapor Bug", Google sign-in, "Mulai Review" |
| Ghost | transparent / `primaryText` / none / none | text-like, low-emphasis ("Lupa password?", "Lihat semua") |
| Danger | `#BC2535` / white / none / `#6F3900` | destructive confirmations only |

Keep it to at most one Primary per view region. Never put two raised buttons of the same variant
side by side as a choice; use Primary with Tonal (or Outline) instead.

```kotlin
@Composable
fun ComicIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ComicButtonVariant = ComicButtonVariant.Ghost,
    size: ComicButtonSize = ComicButtonSize.Medium,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    colors: ComicButtonColors = ComicButtonDefaults.colors(variant),
    interactionSource: MutableInteractionSource? = null,
)
```

`ComicIconButton` has a square face of `size` and a touch target of at least 48dp.
- `contentDescription` comes from Indonesian strings. Pass null only when a visible label next to
  it already names the action.
- The default, Ghost circle, is the top-bar style.
- The send FAB is `variant = Secondary, shape = HeyMikaTheme.shapes.button`.

```kotlin
enum class ComicCardVariant { Strict, Subtle }

@Composable
fun ComicCard(
    modifier: Modifier = Modifier,
    variant: ComicCardVariant = ComicCardVariant.Strict,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    enabled: Boolean = true,
    shape: Shape = HeyMikaTheme.shapes.card,
    color: Color = HeyMikaTheme.colors.surface,
    contentPadding: PaddingValues = PaddingValues(HeyMikaTheme.dimens.cardPadding),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable ColumnScope.() -> Unit,
)
```

`ComicCard` lays its content out in a Column inside `contentPadding`, clipped to `shape`, with the
border drawn on top. With `onClick`, the whole card becomes one button: `Role.Button`, a merged
TalkBack label, a 0.99 press scale, a ripple and the focus ring.

```kotlin
@Composable
fun ComicDivider(
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,      // androidx.compose.foundation.gestures.Orientation
    thickness: Dp = HeyMikaTheme.dimens.borderInk,          // 3dp
    color: Color = HeyMikaTheme.colors.ink,
)

@Composable
fun SubtleDivider(
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    thickness: Dp = HeyMikaTheme.dimens.borderSubtle,       // 1.5dp
    color: Color = HeyMikaTheme.colors.borderMuted,
)
```

Both dividers are decorative and carry no semantics. A vertical divider needs a parent with a
bounded height, such as `Row(Modifier.height(IntrinsicSize.Min))`.

### 3.1 Strict vs Subtle (decided once, for every screen)

- **Strict** (`ComicCardVariant.Strict` / `comicSurface`: 3dp ink border plus 6dp orange offset) is
  for shells and heroes:
  - the auth card;
  - the Mika recommendation hero;
  - the score hero;
  - the transaction status hero;
  - **selected** content (a chosen option card or plan);
  - at most ONE featured card per screen.
- **Subtle** (`ComicCardVariant.Subtle` / `subtleSurface`: 1.5dp borderMuted plus soft float) is for
  everything that repeats:
  - list, feed and library cards;
  - stat tiles and task cards;
  - notification rows and settings sections.
- **Dividers**:
  - `ComicDivider` appears ONLY inside a Strict shell: the auth mascot/form seam, the score hero
    sections, the checkout total rule and the transaction hero.
  - `SubtleDivider` is used everywhere else: subtle cards, list rows, forms, dialogs and sheets.
- Never use tonal elevation, and never use `Card(elevation = ...)`. Use these surfaces. Any M3
  wrapper you use must pass `tonalElevation = 0.dp` and `shadowElevation = 0.dp`.

---

## 4. Domain contracts (`com.ai.heymika.core.domain.model`, `com.ai.heymika.core.data`)

```kotlin
sealed class AppError {
    data object NotFound : AppError()
    data object Forbidden : AppError()
    data object SessionExpired : AppError()
    data object Offline : AppError()
    data class Maintenance(val etaMinutes: Int?) : AppError()
    data class QuotaExceeded(val quota: Quota) : AppError()
    data class Validation(val field: String, val code: String) : AppError()
    data class Server(val code: Int) : AppError()          // HTTP status
    data class Unknown(val cause: Throwable?) : AppError() // cause is for logging only
}

// Plain enum so non-UI mappers (ErrorStateCopies) can pick an illustration; the art and the
// TalkBack labels stay in core/ui/component/mascot.
enum class MikaPose { WAVING, WAVING_PENCIL, ENVELOPE, HAPPY, WORRIED, THINKING, CONFUSED, SLEEPING,
    CONFUSED_MAP, PADLOCK, YAWN_HOURGLASS, UNPLUGGED, HARDHAT, TROPHY }

enum class QuotaKind { DOCUMENTS, PAGES, AI_CHAT, GENERATOR_CREDITS }
enum class QuotaPeriod { DAILY, MONTHLY }

data class Quota(
    val kind: QuotaKind,
    val used: Int,
    val limit: Int?,          // null = unlimited
    val period: QuotaPeriod,
    val resetAt: Long?,       // epoch millis, null = unknown
) {
    val remaining: Int?       // null when unlimited, never negative
    val isExhausted: Boolean  // limit != null && used >= limit
}

// core/data/Resource.kt
class Error<T>(
    override val message: String,
    data: T? = null,
    val error: AppError = AppError.Unknown(null),
) : Resource<T>(data, message)
```

Existing call sites `Resource.Error(message)` and `Resource.Error(message, data)` still compile. New
code passes the typed `error`, and the UI branches on `resource.error`, never on `message`.

---

## 5. Rules for every agent

1. **Only composables live under `core/ui/`.** Parsers, mappers, reducers and any logic you could
   unit test go in `core/domain`, `core/data` or `core/utils`, as plain Kotlin.
2. **Every public composable has `@Preview`s that cover its states**, wrapped in `HeyMikaTheme`.
   - Use `@Preview(showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)`.
   - Make previews `private`.
   - To preview pressed or focused states, add an `internal` impl function with `previewPressed` /
     `previewFocused` flags, the way `ComicTextButton` does. (`staticFocusRing` is `internal` to
     `:core`, so `:core` components can use it.)
3. **Copy is Indonesian, in string resources.** Each agent writes only its own
   `core/src/main/res/values/strings_<area>.xml`, with every key prefixed by the area (for example
   `controls_password_show`). Preview-only sample copy may be inline Indonesian literals.
4. **Accessibility**:
   - Touch targets are at least 48dp: use `Modifier.minimumInteractiveComponentSize()`.
   - Set a proper `Role` (Button, Checkbox, RadioButton, Switch, Tab) and give icons a
     `contentDescription`, or `null` when decorative.
   - Decorations use `clearAndSetSemantics {}`.
   - Use `stateDescription` and expand/collapse actions where they apply.
   - **Put `comicFocusRing` on every interactive element**, sharing that element's interaction
     source.
5. **Colors and sizes come from tokens only.** Use `HeyMikaTheme.*` and
   `MaterialTheme.colorScheme`. The only hex literals allowed are tone values that a later phase
   adds to `HeyMikaTones` through the integrator.
6. **No new third-party libraries.** Icons come from `material-icons-extended` (already an `api`
   dependency of `:core`).
7. **Code style**:
   - ktlint official, max line 120, trailing commas, explicit imports, sparse comments.
   - Multiline expressions start on a new line after `=`.
   - Functions with two or more parameters put one parameter per line.
8. **Verify with**
   `./gradlew :app:assembleDebug :aistudio:compileDebugKotlin :favorite:compileDebugKotlin :setting:compileDebugKotlin --console=plain`.
   `assembleDebug` alone does NOT compile the dynamic features.

---

## 6. File ownership map (parallel stage)

Each agent writes only its own directories and its own `core/src/main/res/values/strings_<area>.xml`.
All paths below are under `core/src/main/java/com/ai/heymika/core/` unless stated otherwise.

| Agent | Owns |
|---|---|
| **foundation** (frozen; changes go through the integrator) | `ui/theme/`, `ui/modifier/`, `ui/component/foundation/`, `domain/model/AppError.kt`, `domain/model/Quota.kt`, `data/Resource.kt`, `strings_foundation.xml` |
| **controls** | `ui/component/input/`, `ui/component/selection/`, `strings_controls.xml` |
| **chrome** | `ui/component/navigation/`, `ui/component/overlay/`, `ui/component/picker/`, `ui/component/timer/`, `strings_chrome.xml` |
| **display** | `ui/component/display/`, `strings_display.xml` |
| **mascot-states** | `ui/component/mascot/`, `ui/component/state/`, `strings_mascot.xml` (states may also use `strings_state.xml`) |
| **logic** | `domain/text/`, `ui/text/`, `data/error/`, `domain/connectivity/`, `data/connectivity/`, `core/src/test/`, `di/` (connectivity and error bindings only), `data/source/remote/RemoteDataSource.kt`, `strings_logic.xml` |
| **quality** | build files (`build.gradle.kts`, `settings.gradle.kts`, module build files, `.editorconfig`), `.github/`, `gradle/libs.versions.toml`, `docs/ci.md` |

Shared or legacy files that nobody edits during the parallel stage, left to the integrator:
- `ui/component/{EmptyState,ErrorState,LoadingState,MaterialCard}.kt`
- `core/src/main/res/values/strings.xml`
- `navigation/`
- everything in `:app` and the feature modules
- the `AiLearnApp`, `AiLearnNavHost` and `AiLearnDatabase` names (the integrator renames them)

---

## 7. Notes for the quality and logic agents

- **ktlint does not run on Kotlin sources today.** Under AGP 9 built-in Kotlin, ktlint-gradle 12.1.1
  creates no `ktlint*SourceSetCheck` tasks for `src/main/java`; only the `KotlinScript` tasks exist.
  The foundation files were linted with the ktlint 1.0.1 CLI (the plugin's engine) and are clean,
  apart from `standard:function-naming` on `@Composable` functions. That rule needs
  `ktlint_function_naming_ignore_when_annotated_with = Composable` in `.editorconfig` (quality
  owns that file).
- `ColorTokensTest` (logic, `core/src/test/`) can assert against `HeyMikaPalette`,
  `HeyMikaLightColorScheme` and `HeyMikaColors()` directly; all three are public and non-composable.
- `HeyMikaTheme` uses `LocalRippleConfiguration` / `RippleConfiguration`, which are
  `@ExperimentalMaterial3Api` in Material3 1.3.1. The opt-in sits at that one place only. Re-check
  it when the BOM is bumped.
