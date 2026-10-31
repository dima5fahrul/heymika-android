# HeyMika — Rencana Implementasi Mockup (29 layar)

Sumber: Stitch mockup `stitch_heymika_login_page/` + `DESIGN.md`. Audit struktur vs `CapstoneAndroidExpert`. Dibuat 2026-10-28. Status: DISETUJUI dengan keputusan di bagian 0.

## 0. Keputusan product owner (2026-10-28) — override isi di bawah

1. **Fitur cerita (STORY) dibuang.** Tidak ada mockup. `GenerationKind.STORY` dan semua jejaknya dihapus (P3).
2. **Belum ada backend.** Semua data lewat *fake/dummy data source* di `:core/data` yang mengimplementasikan interface yang sama dengan remote asli (clean architecture: ViewModel -> UseCase -> Repository -> DataSource). Ganti ke backend nyata nanti cukup swap binding Koin. Konsekuensi: certificate pinning, `assetlinks.json`, FCM, Google sign-in, dan payment gateway ditunda sampai backend ada; App Link diuji dengan host placeholder via `adb`; push disimulasikan dengan notifikasi lokal dari fake job.
3. **Bottom nav ikut mockup Beranda:** `Beranda / Materi / Kuis / Tanya Mika / Profil`. Flashcard dan Rencana bukan tab (dibuka dari Beranda/Detail/Kuis). Label final: "Materi", "Profil".
4. **Warna:** primary `#0075E8` untuk fill/focus/active, `primaryText #005AB5` untuk link & teks kecil di atas putih.
5. **Pembayaran di akhir.** Semua layar desain mockup dikerjakan dulu; checkout + status transaksi tetap P11 dengan data dummy, gateway diputuskan nanti.
6. **v2 (tidak masuk v1):** layout tablet/adaptive (split shell, two-pane), voice input / Voice Tutor, lampiran gambar, palet rumus, TTS + Zen mode flashcard, edit catatan (Mode Sunting), ekspor data, avatar kolaborator. v1 = phone-only, light-only.

## 1. Audit struktur vs Capstone reference

Verdict: yes. HeyMika's structure follows the CapstoneAndroidExpert architecture, with Jetpack Compose standing in for XML views. I read every build file, source tree, manifest and proguard file on both sides. The pieces that match:
- The module graph: app base, core library, dynamic features depending on :app and :core.
- The clean-architecture layering: data/domain/di/ui/utils, I*Repository in domain, UseCase + Interactor pairs, the DataMapper object, Resource, and a line-for-line NetworkBoundResource.
- The Koin split, and R8. Its keep rules for the reflectively loaded FeatureEntry classes are verified in app/build/outputs/mapping/release/mapping.txt.
- BuildConfig loaded from local.properties, the ktlint plugin, LeakCanary, and the same toolchain versions.

The Compose-driven differences are deliberate and sound. FeatureEntry + reflection + SplitInstallManager replaces navigation-dynamic-features-fragment, Coil replaces Glide, StateFlow replaces LiveData, and a single activity with route arguments replaces Activities and intent extras.

What is missing is the whole security and quality layer the reference added for its star-5 submission:
- **SQLCipher DB encryption (high).** Do not port the reference SecureKeyManager as written: AndroidKeyStore keys return null from getEncoded(), so it throws on first DB open.
- **Certificate pinning and network_security_config (high).** This waits on a real backend host; the base URL is still https://api.example.com/.
- **CI workflow (high).** No .github directory exists.
- **Unit tests (high).** There are zero test files; the dependencies are declared but unused.
- **RootBeer root detection (medium).**
- **JaCoCo (medium).** Under AGP 9 built-in Kotlin the classes are in build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes. The reference's build/tmp/kotlin-classes/debug path does not exist here, so a straight copy would report zero classes.
- **OWASP dependency-check (medium).**

Two further medium risks:
- ktlint has never run on Kotlin sources (only empty script-check reports exist), and formatting differs between core and app/features.
- All three features use on-demand delivery while the reference uses install-time, so the tabs fail on sideloaded installs.

The low-severity item is leftover AiLearn naming. Both projects also let domain interfaces import core.data.Resource; this matches the reference, so it is optional to fix.

For the design phase the user asked about: /Users/linkit/Project/heymika/core/src/main/java/com/ai/heymika/core/ui/theme/{Color,Theme,Type}.kt is still a placeholder indigo/teal/amber Material3 scheme with FontFamily.Default. It needs replacing with the DESIGN.md tokens (navy #082A52 3px borders, #FF8A00 hard offset shadows, Nunito Sans, pill radii), and core/ui/component needs comic-style components before the mockup pages are built.

Key paths: the reference is at /Users/linkit/Downloads/CapstoneAndroidExpert (its spec is docs/superpowers/specs/2026-06-06-capstone-expert-submission-design.md); the current project is at /Users/linkit/Project/heymika. The project was audited read-only: no Gradle tasks were run.

### Sama dengan reference

- **Module graph** — /Users/linkit/Project/heymika/settings.gradle.kts includes :app, :core, :aistudio, :favorite, :setting. app/build.gradle.kts:16 has dynamicFeatures += setOf(":aistudio", ":favorite", ":setting"). Every feature uses the dynamic-feature plugin and implementation(project(":app")) + implementation(project(":core")). Same shape, plus one extra feature.
- **Core package layout** — Same tree under /Users/linkit/Project/heymika/core/src/main/java/com/ai/heymika/core. Repositories sit in data/repository/, DataStore in data/source/local/datastore/, request DTOs in data/source/remote/request/, and there is an extra navigation/FeatureEntry.kt
- **Repository interfaces in domain, implementations in data** — domain/repository/IMaterialRepository.kt, IAiRepository.kt and ISettingRepository.kt, implemented by data/repository/MaterialRepository.kt, AiRepository.kt and SettingRepository.kt
- **Use case interface + Interactor** — MaterialUseCase/MaterialInteractor, AiUseCase/AiInteractor and SettingUseCase/SettingInteractor in core/domain/usecase. ViewModels depend only on the use cases (grep: no presentation file imports a repository or data source)
- **Resource wrapper** — core/data/Resource.kt: the same three states. Error overrides message as non-null, which is an improvement for Compose smart casts
- **NetworkBoundResource** — core/data/NetworkBoundResource.kt has the same algorithm line for line. Used by MaterialRepository.getAllMaterials(), and saveCallResult keeps the favourite flags across a REPLACE
- **ApiResponse + RemoteDataSource** — Identical pattern in core/data/source/remote/network/ApiResponse.kt and RemoteDataSource.kt
- **Mappers** — core/utils/DataMapper.kt object covering Material (response/entity/domain both ways) and GeneratedContent, with a tolerant enum fallback
- **Koin module split** — core: databaseModule (DB, DAOs, LocalDataSource, SettingPreferences), networkModule (OkHttp, ApiService, RemoteDataSource), repositoryModule, useCaseModule. app: appModule (DynamicFeatureInstaller + ViewModels). Features: aiStudioModule/favoriteModule/settingModule loaded through loadKoinModules. AiLearnApp.kt calls startKoin with androidLogger(Level.NONE). Use cases moved from app into core, which is fine
- **Settings persistence** — core/data/source/local/datastore/SettingPreferences.kt goes through ISettingRepository and SettingUseCase. This is cleaner than the reference
- **R8 / obfuscation** — The same flags in app/build.gradle.kts:33-40 and core/build.gradle.kts:38-49. core/consumer-rules.pro covers the same libraries. The app and feature proguard-rules.pro files keep the reflective FeatureEntry classes. Checked in app/build/outputs/mapping/release/mapping.txt: AiStudioEntry, FavoriteEntry, SettingEntry and FeatureEntry all map to their own names, and app-release.aab exists
- **BuildConfig from local.properties** — core/build.gradle.kts:21-36 uses the same mechanism for API_BASE_URL (fallback https://api.example.com/). local.properties is gitignored and local.properties.example is provided
- **ktlint plugin + .editorconfig** — Same subprojects block in /Users/linkit/Project/heymika/build.gradle.kts:12-14, same plugin version, byte-identical .editorconfig
- **LeakCanary** — app/build.gradle.kts:98 debugImplementation(libs.leakcanary), version 2.14
- **Test dependencies declared** — The same libraries and versions are declared in app and core build files. Only the declarations exist (see gaps)
- **Toolchain baseline** — Same AGP and Gradle wrapper (9.4.1), SDK levels, Java target and library versions. Kotlin 2.2.10, KSP 2.2.10-2.0.2, Room 2.8.5
- **Screen placement** — HomeScreen + MaterialDetailScreen in :app, FavoriteScreen in :favorite, SettingScreen in :setting, plus AiStudioScreen in :aistudio

### Gap

- **[high] Database encryption (SQLCipher)** — None of it exists (grep for sqlcipher/SupportFactory/KeyStore finds nothing). /Users/linkit/Project/heymika/core/src/main/java/com/ai/heymika/core/di/DatabaseModule.kt builds a plaintext Room DB named "AiLearn.db". It caches materials and AI output (generated_content).
  - Rekomendasi: Add net.zetetic:sqlcipher-android (4.6.1 or newer; the reference's android-database-sqlcipher is the deprecated artifact) plus androidx.sqlite:sqlite-ktx aligned with Room 2.8.5. Call System.loadLibrary("sqlcipher") and .openHelperFactory(SupportOpenHelperFactory(passphrase)). Do NOT copy the reference SecureKeyManager as written: it returns (keyStore.getKey(alias,null) as SecretKey).encoded. AndroidKeyStore keys cannot be exported, so getEncoded() returns null and the non-null ByteArray return throws an NPE on first DB open. Instead, generate 32 random bytes with SecureRandom, wrap them with a Keystore AES/GCM key, and store the ciphertext + IV. Exclude that file from app/src/main/res/xml/backup_rules.xml and data_extraction_rules.xml, which currently include all of domain="sharedpref". Also rename the DB file (or delete AiLearn.db), because SQLCipher cannot open the existing plaintext file and fallbackToDestructiveMigration only covers version bumps.
- **[high] Certificate pinning + network security config** — /Users/linkit/Project/heymika/core/src/main/java/com/ai/heymika/core/di/NetworkModule.kt has no CertificatePinner. There is no app/src/main/res/xml/network_security_config.xml, and app/src/main/AndroidManifest.xml has no networkSecurityConfig attribute. Cleartext is blocked only by the API 28+ platform default. The base URL is still the placeholder https://api.example.com/
  - Rekomendasi: Once the real backend host is fixed, compute pins for the leaf and an intermediate/backup key with the openssl pipeline the reference spec documents. Add a CertificatePinner keyed on the host parsed from BuildConfig.API_BASE_URL. Add network_security_config.xml with base-config cleartextTrafficPermitted="false" and a domain-config pin-set that has an expiration date, then reference it from the manifest. Optionally add debug-overrides for a local proxy.
- **[high] CI pipeline** — No /Users/linkit/Project/heymika/.github directory at all
  - Rekomendasi: Port the reference ci.yml. Change the local.properties step to write ailearn.api.base.url (optional, since core/build.gradle.kts falls back to a default). Keep JDK 17+. Consider bundleRelease next to assembleRelease, since the on-demand features ship in the AAB. Add the jacoco and owasp tasks below before enabling those jobs.
- **[high] Unit tests** — No src/test or src/androidTest directory in any module (find returned nothing). The test dependencies are declared but unused
  - Rekomendasi: Add core/src/test/.../utils/DataMapperTest.kt (material round-trip, favoriteIds preservation, the unknown-kind -> SUMMARY fallback, quiz encoding), domain/usecase/{Material,Ai,Setting}InteractorTest.kt (mockk the I*Repository), data/repository/MaterialRepositoryTest.kt (NetworkBoundResource fetch, cache, error and favourite-preservation paths), data/repository/AiRepositoryTest.kt (Success, Empty and Error mapping plus the Room insert). Add tests for the aistudio QuizParser.parse tolerant reader. Add app/src/test HomeViewModelTest.kt (filter, subject toggle, retry) and MaterialDetailViewModelTest.kt (toggleFavorite), using Dispatchers.setMain and turbine.
- **[medium] Root detection** — No rootbeer in libs.versions.toml and no RootDetectionUtil. /Users/linkit/Project/heymika/app/src/main/java/com/ai/heymika/MainActivity.kt calls setContent unconditionally
  - Rekomendasi: Add com.scottyab:rootbeer-lib and core/utils/RootDetectionUtil.kt. In MainActivity, check before the NavHost and render a Compose AlertDialog that calls finish(). The reference sets android.enableJetifier=true and HeyMika does not, so confirm the dependency resolves cleanly without it.
- **[medium] Coverage (JaCoCo)** — No jacoco in any HeyMika build file
  - Rekomendasi: Add the task, but fix the class directory. Under AGP 9 built-in Kotlin, HeyMika's classes land in core/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes. build/tmp/kotlin-classes/debug (the reference's path) does not exist here, so a verbatim copy reports zero classes. Also exclude Compose artifacts (**/ComposableSingletons*, **/ui/**). The alternative is AGP's own enableUnitTestCoverage = true + createDebugUnitTestCoverageReport.
- **[medium] OWASP dependency check** — No owasp version or plugin in /Users/linkit/Project/heymika/gradle/libs.versions.toml or build.gradle.kts
  - Rekomendasi: Add the plugin alias to the catalog and the root plugins block, and run dependencyCheckAnalyze in CI with continue-on-error, as the reference does. An NVD API key secret makes it practical.
- **[medium] ktlint actually enforced on Kotlin sources** — The plugin is applied, but only ktlintKotlinScriptCheck reports exist in */build/reports/ktlint (all empty). No main-source-set check has ever run. Formatting is inconsistent: core uses ktlint_official wrapping (`val databaseModule =\n    module {`), while app and features use `val appModule = module {` and blank first lines in class bodies (AiLearnApp.kt, MainActivity.kt, DynamicFeatureInstaller.kt). Likely to fail ktlintCheck; not run, because the project is read-only for this audit
  - Rekomendasi: Run ./gradlew ktlintFormat, then ktlintCheck, and commit the result before adding the CI lint job. Pin the ktlint engine version in a ktlint {} block if the plugin default cannot parse Kotlin 2.2 syntax.
- **[medium] Dynamic feature delivery mode** — aistudio, favorite and setting manifests all use <dist:on-demand />. Outside Play (sideloaded APK, reviewer device), SplitInstallManager.startInstall fails, and those tabs only work if Android Studio deployed every split. DynamicFeatureInstaller handles the failure, but the feature is then unreachable
  - Rekomendasi: Use <dist:install-time /> for :favorite and :setting (matches the reference and the reviewer flow; the FeatureEntry + reflection wiring keeps working unchanged). Keep on-demand only for :aistudio if it is wanted, and test it with bundletool --local-testing.
- **[low] Naming / housekeeping** — Leftover 'AiLearn' names in a HeyMika app: AiLearnApp (both the Application class and the root composable, with an explanatory comment in MainActivity), AiLearnTheme, AiLearnDatabase, AiLearn.db, ailearn_settings, ailearn.api.base.url. No androidTest sources despite the declared deps. ACCESS_NETWORK_STATE is declared in app/AndroidManifest.xml but never used (no ConnectivityManager usage). No gradle-daemon-jvm.properties
  - Rekomendasi: Rename to HeyMika* (do the DB rename together with the SQLCipher change), drop or use ACCESS_NETWORK_STATE, and optionally add a Compose UI smoke test.

### Beda yang disengaja (karena Compose)

- **XML views + ViewBinding + RecyclerView adapter -> Jetpack Compose** — The reference's core/ui/MovieAdapter.kt + item_movie.xml and every fragment_*.xml/activity_*.xml become composables: core/ui/component/{MaterialCard, EmptyState, ErrorState, LoadingState}.kt and *Screen.kt files. buildFeatures.viewBinding is replaced by compose = true plus the kotlin.compose plugin. The shared UI still lives in :core/ui, so the layering is the same.
- **Fragment navigation + navigation-dynamic-features-fragment -> navigation-compose + FeatureEntry + reflection** — navigation-dynamic-features-fragment (nav_graph.xml with app:moduleName, BottomNavigationView menu) can only host Fragment destinations, and there is no official compose dynamic-features artifact. HeyMika puts the FeatureEntry interface in :core (core/navigation/FeatureEntry.kt). app/navigation/DynamicFeatureInstaller.kt loads each entry with Class.forName after a SplitInstallManager install, and SplitCompat.install runs in AiLearnApp/MainActivity. Each entry registers its composable destinations on the single NavHost. The required keep rules exist and were verified in mapping.txt.
- **Glide -> Coil** — Glide targets ImageView. coil-compose AsyncImage is the Compose-native loader (used in core/ui/component/MaterialCard.kt).
- **LiveData (asLiveData/observe) -> StateFlow + collectAsStateWithLifecycle** — Compose consumes Flow directly. ViewModels use stateIn(WhileSubscribed(5000)) and screens collect with lifecycle-runtime-compose, so lifecycle-livedata-ktx is unnecessary.
- **AppCompatDelegate.setDefaultNightMode -> theme parameter** — MainActivity collects SettingUseCase.darkMode and passes it to AiLearnTheme(darkTheme). appcompat, material-components and constraintlayout are dropped, and the window theme uses an android:Theme.Material parent only for the pre-first-frame window.
- **Single Activity; Serializable Movie intent extra -> route argument** — DetailActivity + intent.getSerializableExtra(EXTRA_DATA) become the Compose destination detail/{materialId}, and the detail ViewModel re-reads the row from Room by id. kotlin-parcelize is declared at the root but not needed in modules.
- **Feature Koin loading moved from Fragment.onCreateView to FeatureEntry init** — Without a Fragment lifecycle, the reflective entry constructor is the first feature code to run. A @Volatile + synchronized guard stops loadKoinModules from running twice, which the reference risks on every onCreateView.
- **AppExecutors.diskIO -> suspend DAO + coroutines** — The reference runs setFavoriteMovie on an Executor. HeyMika uses suspend Room DAOs and suspend use-case functions. This modernizes the code (not strictly required by Compose) and removes the Executor plumbing from tests.
- **No API token in BuildConfig** — The reference ships TMDB_API_TOKEN in BuildConfig. HeyMika ships only API_BASE_URL, because its backend proxies Anthropic and holds the key server-side. Anything in the APK can be extracted, so this is the safer design.
- **HTTP body logging only in debug** — The reference always logs at Level.BODY. HeyMika's NetworkModule uses BODY only when BuildConfig.DEBUG and NONE in release. This hardens the reference.
- **Extra :aistudio dynamic feature + extra domain (Material, GeneratedContent, QuizQuestion, GenerationKind)** — Required by the product (AI quiz/summary/story generation). It follows the same data/domain/usecase pattern and does not break the reference architecture.

## 2. Tahapan (phases)

### P0 — Design-system foundation, system states & quality gates  (size XL, depends on -)

**Goal:** Replace the placeholder indigo/teal theme with the DESIGN.md comic system in :core/ui. Ship every shared component, the adaptive and focus policy, and the AppError-driven state components, and put CI gates in front of all later phases.

**Screens:** `system_states_error_handling_heymika_mobile`

**Foundation work:**

- Make the visual decision once: strict DESIGN.md (3dp ink + orange offset) for shells, heroes and selected content, and Subtle float for list and feed cards. Primary #0075E8, primaryText #005AB5. Document the decision in the gallery
- Rewrite core/ui/theme (Color.kt, Type.kt, Theme.kt) as HeyMikaTheme. Fill the M3 scheme from EVERY YAML slot, including tertiaryFixedDim, onPrimaryFixedVariant, inversePrimary, primaryFixedDim, surfaceDim, surfaceBright, surfaceVariant and the on-fixed variants. Keep surfaceTint at the YAML #005CBA and force tonalElevation 0 in all wrappers. Add HeyMikaColors, Type, Shapes, Dimens and Layout CompositionLocals, and the named-tone palette only (enum tone maps are added by later phases). Add ColorTokensTest. Rename AiLearnTheme and update MainActivity
- Light-only decision for v1: MainActivity stops collecting SettingUseCase.darkMode, HeyMikaTheme drops the darkTheme parameter, and the dark-mode row is removed from SettingScreen now, so no toggle does nothing. The darkMode DataStore key is left in place, unread
- Bundle Nunito Sans (400-900) and JetBrains Mono in core/src/main/res/font. Build the compact and expanded type ramps (expanded: displayHero 48/56, headlineLg 36/44)
- Adaptive policy: add material3-adaptive (exact pinned version, e.g. 1.0.0, verified against BOM 2025.01) and define HeyMikaWindowClass (compact <600dp, medium 600-1199dp, expanded >=1200dp) and LocalHeyMikaLayout (shadow 6/8/14dp, shell padding 16/32/64dp, split null/0.5/0.53, type ramp). Build AuthSplitShell and TwoPaneLayout
- Implement Modifier.comicSurface, the ComicCard variants, ComicDivider (3dp ink) and SubtleDivider (1.5dp borderMuted) with the documented usage rule, and Modifier.comicFocusRing. Wire the focus ring into every interactive component
- Build ComicButton with press-translate, the input family, Checkbox/Radio/ToggleRow, SelectableOptionCard, SegmentedControl, pills and tags, FilterChipRow/PillTabRow, Accordion/ExpandableSection (animated chevron, expanded semantics with expand/collapse actions, reduced motion), ComicTimePicker, TimeRangeField, DatePickerField, the progress set, StatTile, SectionHeader and InlineAlertBanner/InfoNoteCard
- Put MarkupParser in core/domain/text (plain Kotlin) and add RichText and InlineCodeChip in core/ui/text as thin mappers. Rule from now on: only composables live under ui/, and every parser and mapper lives in domain/, data/ or utils/
- Build the mascot set: MikaAvatar (pose enum, placeholder vectors), MikaSpeechBubble (GenericShape tail with ink border), the MikaCallout presets, HalftoneBackground/Decoration and SpeedLinesDecoration (peripheral mask, cached paths, decorative semantics)
- Build the top-bar variants, HeyMikaBottomNavBar (stateless, rendering only the shipped tabs it is given), StickyBottomActionBar/FloatingInputBar with height reporting, HeyMikaSnackbarHost, ComicDialog, ComicBottomSheet, ResendCooldownButton and CountdownTimerPill
- Add AppError and the minimal Quota type in core/domain, ErrorMapper in core/data/remote, and Resource.Error carrying AppError. Add ConnectivityObserver (NetworkCallback, which finally uses the declared ACCESS_NETWORK_STATE)
- Create HeyMikaRoot (app): the root composable wrapping the NavHost, which mounts OfflineBanner and the global snackbar host for every route, auth included. It stays the host in all later phases
- Implement MikaErrorState (FullScreen, Inline) with per-state copy for the six catalog states, plus MikaEmptyState and SkeletonBlock. They replace core/ui/component EmptyState, ErrorState and LoadingState. Restyle MaterialCard so the legacy screens compile until P3
- Debug-only gallery: app/src/debug holds DesignGalleryActivity (its own LAUNCHER entry 'HeyMika DS') with Design System, System States and a Sesi panel (sign-out, mock-backend toggles). The catalog's state pills and the 'ALL SYSTEMS OPERATIONAL' ribbon live only there. DebugHooks has the same FQCN in app/src/debug and app/src/release (release is a no-op) and is the only reference from main to debug code
- Quality gates: run ktlintFormat once, then commit a clean ktlintCheck. Add .github/workflows/ci.yml with lint, test (testDebugUnitTest plus the :core JaCoCo report using the AGP 9 class dir build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes, excluding R, BuildConfig, *_Impl*, di/**, ComposableSingletons* and ui/**) and build (assembleRelease and bundleRelease, artifacts uploaded) jobs. Add a release-dex check with apkanalyzer. Add a MainDispatcherRule test harness
- Rename the leftover AiLearn classes (the Application, the root composable, AiLearnNavHost) to HeyMika*
- Definition of Done for every later phase: each new screen has loading, empty, error and offline states via MikaErrorState/Skeleton; strings live in values/strings.xml (Indonesian); @Previews cover every mockup state, replacing the HTML 'SIMULASI' controls, which never ship; compact and medium widths are previewed; interactive elements show the focus ring; interactors, mappers and ViewModels get unit tests; every Room schema bump ships a Migration with a MigrationTestHelper test (from P1); CI is green

**Deliverable / acceptance:** A debug build adds a second launcher icon, 'HeyMika DS'. Its Design System page shows every component in default, pressed, focused, disabled, error and loading states. Repeated `adb shell input keyevent KEYCODE_TAB` moves a visible 2dp + 3dp ring across buttons, option cards, chips, tabs, accordion headers and nav items. The comic time picker, time-range field (22:00-07:00 across midnight) and date picker open in ComicDialog. The accordion animates and becomes instant at animator duration scale 0. On a Pixel Tablet AVD the page shows the 50/50 split shell with 8dp shadows in portrait and 53/47 with 14dp shadows and the 48/56 display style in landscape. System States renders 404, 403, Sesi Habis, Offline, Maintenance and Batas Kuota full-screen and inline. Airplane mode shows the OfflineBanner from HeyMikaRoot in both the gallery and the main app. The legacy Home/Detail/AiStudio/Favorite/Setting screens run on the light HeyMikaTheme, and Setting has no dark-mode switch. A PR runs CI green: ktlintCheck; unit tests (ErrorMapperTest, MarkupParserTest, ColorTokensTest); a JaCoCo report in which ErrorMapper and MarkupParser show non-zero coverage; assembleRelease and bundleRelease; and `apkanalyzer dex packages` on the release APK listing no com.ai.heymika.debug.gallery classes.

### P1 — Navigation contract, secure session, sign-in, verification & onboarding  (size XL, depends on [0])

**Goal:** Replace the rebuild-on-install navigation with a stable graph. Make the app an authenticated product: fresh installs land on Login, accounts are created and verified, onboarding personalizes Mika, and the session and database are encrypted.

**Screens:** `login_heymika_mobile`, `register_heymika_mobile`, `verifikasi_email_heymika_mobile`, `onboarding_heymika_mobile`

**Foundation work:**

- Delivery: switch :aistudio, :favorite and :setting to <dist:install-time/>. New modules (:planner, :quiz, :flashcard, :insight, :payment) are created only in the phase that ships their first screen, each with its DynamicFeature entry, keep rule and test. Each feature module gets an EntryContractTest unit test (a feature can see :app's DynamicFeature enum): Class.forName(entryClassName) loads, implements FeatureEntry, and every RouteSpec it owns is handled by its Content
- Stable navigation (fixes the HIGH back-stack finding): add HeyMikaRoutes, a RouteSpec registry in :core/navigation (pattern, args, owner, shellTab, deepLinkPaths, requiresAuth, shipped). Reshape FeatureEntry to { moduleName; @Composable Content(spec, entry, navigator) } and port AiStudioEntry, FavoriteEntry and SettingEntry. HeyMikaNavHost builds the graph once from the registry, rendering feature specs through FeatureDestinationHost, which shows FeatureInstallPanel in place while a split is missing. Delete the remember(installStates){ installedEntries() } rebuild and FeatureInstallDialog. Add the HeyMikaNavigator interface
- Inject SplitInstallManager into DynamicFeatureInstaller behind a FeatureInstaller interface (Koin), so tests can supply a fake or FakeSplitInstallManager
- GraphStabilityTest (androidTest, TestNavHostController plus a fake installer): with a 3-deep stack and a saved tab, flip a feature from NotInstalled through Downloading to Installed; assert navController.graph is the same instance and the back stack and saved state are intact
- StartupGate with the SplashScreen API, plus the auth, onboarding and main graphs. The legacy Home, Detail and old 3-tab bar stay temporarily inside main
- DeepLinkRouter and App Links: add a manifestPlaceholder appLinkHost from local.properties heymika.applink.host (default heymika.test for the mock backend). Add autoVerify https intent filters for /verify-email and /reset-password, and an assetlinks.json template for the backend (debug, release and Play signing SHA-256). Export no custom-scheme filters. Pending links are replayed after auth
- Auth domain and data: User, AuthSession, LearnerProfile, PasswordPolicy/PasswordStrengthCalculator in core/domain (one canonical rule set), AuthApi (login, google, register, me, resend verification, change email, logout), AuthRepository, AuthUseCase and OnboardingUseCase
- SessionStore: DataStore with a Tink AEAD serializer under an AndroidKeyStore master key (EncryptedSharedPreferences is deprecated). OkHttp AuthInterceptor plus a single-flight refresh Authenticator that emits SessionExpired
- Credential Manager 'Sign in with Google' (androidx.credentials plus googleid) and autofill content types
- SQLCipher: net.zetetic sqlcipher-android with SupportOpenHelperFactory. The passphrase is 32 SecureRandom bytes wrapped by a Keystore AES/GCM key (not the reference SecureKeyManager, whose getEncoded() returns null). Create HeyMikaDatabase v1 with the existing entities and context.deleteDatabase('AiLearn.db'). Set exportSchema=true with the KSP arg room.schemaLocation=$projectDir/schemas, and commit the schemas. Remove fallbackToDestructiveMigration; from now on every bump ships a Migration or AutoMigration plus a MigrationTestHelper test
- Rename ailearn_settings to heymika_settings and ailearn.api.base.url to heymika.api.base.url. Exclude the wrapped DB key and the session store in backup_rules.xml and data_extraction_rules.xml
- Add network_security_config.xml with base-config cleartextTrafficPermitted=false and reference it from the manifest
- Mock backend: FakeRemoteDataSource with JSON fixtures behind the Gradle property heymika.backend=mock. Tokens 'ok', 'valid' and 'expired' drive the verify and reset states, so this and every later phase can be demoed before the real API exists
- Onboarding graph: a ViewModel scoped to the graph, progress persisted. Steps 2 'Target & Topik' (dailyTargetMinutes, topics) and 3 are derived from shared components (flagged for PO review). The nickname is prefilled. Auth screens use AuthSplitShell on medium and expanded widths
- Verify screen: four states, resend cooldown from the server's sentAt, a change-email ComicDialog (replaces window.prompt), opening the email app via CATEGORY_APP_EMAIL, and a re-check on ON_RESUME
- Drop the prototype panels (login 'Uji Status Akun', register 'Dev Preview', verify 'SIMULASI', the '/verify-email' code chip) and cover those states with @Previews
- Tests: PasswordStrengthCalculatorTest, AuthInteractorTest, a LoginViewModel routing-rule test, a SessionAuthenticator test with MockWebServer, an OnboardingViewModel resume test, DeepLinkRouter parsing and allow-list tests, an androidTest for startup routing, and GraphStabilityTest

**Deliverable / acceptance:** On a fresh install Login appears; on a Pixel Tablet AVD it is the 50/50 split shell. Register opens Verifikasi Email, and the resend cooldown survives rotation. `adb shell am start -W -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d 'https://heymika.test/verify-email?token=ok' com.ai.heymika` flips the screen to 'Verifikasi Berhasil!'. After `adb shell pm set-app-links --package com.ai.heymika 2 all`, the same command works without the trailing package. Onboarding steps 1-3 lead to the legacy home. Kill and relaunch lands on home. An account that is signed out through the gallery's Sesi panel and then signed in unverified lands on verify. A wrong password shows the inline 'Email atau Kata Sandi Belum Sesuai' alert. Google sign-in works through Credential Manager on a Play-services emulator. `adb exec-out run-as com.ai.heymika cat databases/HeyMika.db > hm.db && sqlite3 hm.db .tables` fails with 'file is not a database', and the v1 schema JSON is committed. The legacy AiStudio opened from Detail renders through FeatureDestinationHost. GraphStabilityTest and the three EntryContractTests pass, the release mapping.txt keeps all 3 FeatureEntry classes, and CI is green.

### P2 — Password recovery  (size M, depends on [1])

**Goal:** Complete the auth surface with privacy-preserving forgot-password and token-based reset, built on the P1 App Link and password-policy infrastructure.

**Screens:** `lupa_password_heymika_mobile`, `reset_password_heymika_mobile`

**Foundation work:**

- Add POST /auth/password/forgot (always a generic 200), GET /auth/password/reset/{token} and POST /auth/password/reset to AuthApi, AuthUseCase and the mock backend
- Forgot-password screen: form and sent state in one ViewModel; EmailSentConfirmation with 'Buka Aplikasi Email'; a ResendCooldownButton driven by server time
- Reset screen: a validating state on open; a CountdownTimerPill from expiresAt that switches to the expired state at 00:00; the PasswordPolicy Grid2x2 checklist and match indicator; success clears the local session (other sessions are revoked server-side); SpeedLines on the success card
- Reset opened while signed in: confirm sign-out first (ComicDialog)
- Copy fixes: replace the English 'Forgot Password' title and footer with Indonesian; no avatar on unauthenticated screens; one link-validity value (pending PO)
- Drop the 'SIMULASI TAMPILAN' and 'SIMULASI KONDISI ALUR' panels
- Tests: ForgotPasswordViewModel (validation, sent, cooldown) and ResetPasswordViewModel (validating, active, mismatch, countdown expired, success, submit error)

**Deliverable / acceptance:** From Login, 'Lupa password?' leads to submit, then the sent state with a working resend cooldown and an email-app intent. `adb shell am start -W -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d 'https://heymika.test/reset-password?token=valid' com.ai.heymika` opens the reset form with a live countdown. token=expired shows 'Tautan Tidak Valid atau Kedaluwarsa!' and leads to a new link request. Opening the valid link while signed in first asks to sign out. Matching strong passwords reach 'Kata Sandi Berhasil Diperbarui!' and then Login. Unit tests are green in CI.

### P3 — App shell, workspace library & material pipeline  (size XL, depends on [1])

**Goal:** Give signed-in users the real app frame and the core value: organize workspaces, upload PDF/TXT/pasted text, watch Mika process it, and land on a workspace detail with the structured summary preview. In the same phase, retire every legacy screen and model that the reshape breaks.

**Screens:** `materimu_heymika_workspace_mobile`, `tambah_materi_baru_heymika_mobile`, `status_pemrosesan_bahan_belajar_heymika_mobile`, `detail_workspace_heymika_mobile`

**Foundation work:**

- AppShell Scaffold driven by ShellTab and the RouteSpec shipped flags. Materimu is the only shipped tab in this phase, so it is the start tab and the bar stays hidden. Add saveState/restoreState and hide the nav on pushed routes. The old 3-tab BottomTab is deleted
- Domain and data: Workspace and WorkspaceCategory (plus their tone map), the reshaped Material, ProcessingJob/Step (plus JobStepStatus tone), UploadSession, the NoteSummary preview with the sealed NoteSection, ExamReadiness with MasteryBand (bandFor() plus tone; cut-offs pending PO), the UserProgress read model for the top-bar pills, and the Entitlement/AiQuota read model extending the P0 Quota. Add WorkspaceApi, MaterialApi and JobApi with NetworkBoundResource and Room. NoteSectionMapper lives in core/data/mapper (tolerant, unit-tested, counted by JaCoCo)
- Bookmark entity, defined HERE: table, BookmarkRepository and BookmarkUseCase with type WORKSPACE now, and the enum already lists NOTE, FORMULA, CONVERSATION, CARD and QUESTION for later phases. Workspace.isBookmarked derives from it
- Room: Migration(1,2) drops the legacy materials and generated_content tables (a server cache, so dropping it is safe) and creates the new tables. MigrationTestHelper test 1 to 2. No destructive fallback
- Legacy removal, in the same phase so CI stays green: delete HomeScreen/HomeViewModel, MaterialDetailScreen/ViewModel, MaterialCard, AiStudioScreen/AiStudioViewModel/AiStudioUiState, QuizSection, HistorySection, both QuizParsers (their tolerant cases are saved as JSON fixtures under core/src/test/resources/legacy-quiz/ for the P8 mapper tests), GeneratedContent, GenerationKind, QuizQuestion, GeneratedContentEntity/Dao, AiRepository, IAiRepository, AiUseCase, AiInteractor, GenerationRequest, GenerationResponse and QuizQuestionResponse. Reshape ListMaterialResponse/MaterialResponse into WorkspaceListResponse, WorkspaceResponse and MaterialResponse. Replace ApiService with the per-area APIs. Delete the legacy SettingScreen/SettingViewModel (Profil replaces it in P4). ProseSection's logic moves into the core/ui/summary TextBlock. :aistudio and :setting keep their FeatureEntry with no shipped specs
- Reshape :favorite into 'Koleksi Tersimpan': FavoriteViewModel reads BookmarkUseCase, and FavoriteScreen lists Bookmark rows with shared components (no MaterialCard, no Material.isFavorite). It opens from Detail's bookmark snackbar action 'Lihat Koleksi'
- Upload: SAF ACTION_OPEN_DOCUMENT with EXTRA_MIME_TYPES [application/pdf, text/plain]. Because providers can mislabel files, validate after picking: MIME, extension, %PDF magic bytes, size <= 20 MB, PdfRenderer page count <= 100, and a SecurityException means encrypted. Errors use the DESIGN.md error state. Add the 'Tempel Teks' text-area mode (derived) and SavedStateHandle form state
- Workers live in core/data/worker. Add koin-androidx-workmanager: call workManagerFactory() in startKoin and remove androidx.work.WorkManagerInitializer from the InitializationProvider (tools:node='remove'). Feature Koin modules may only contain ViewModels and UI mappers, so a background-started process never needs a FeatureEntry
- UploadWorker (work-runtime-ktx 2.10.x, exact version pinned): setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST), so the expedited-quota fallback is explicit. getForegroundInfo() returns an ongoing progress notification with ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC, which API 30 needs for expedited work. The manifest adds FOREGROUND_SERVICE and FOREGROUND_SERVICE_DATA_SYNC and overrides androidx.work.impl.foreground.SystemForegroundService with android:foregroundServiceType='dataSync' (tools:node='merge'). The upload is chunked and resumable through the UploadSession, so a stop for STOP_REASON_FOREGROUND_SERVICE_TIMEOUT (the Android 15 dataSync limit) or for quota retries from the last acknowledged chunk
- Job tracking: a repository Flow polling with backoff (SSE when the backend offers it) plus JobWatcherWorker (non-expedited, CONNECTED constraint) that keeps tracking after the user leaves. The completion notification uses a default channel and a 'Ding' sound channel, with an explicit PendingIntent carrying the route key for Status. POST_NOTIFICATIONS is requested at the first upload with a rationale; if it is denied, the UI says status is in-app only. Cancel goes through ComicDialog. The 'Kuota Aman' refund copy comes from the server's failure payload
- Derived sheets (no mockup): create workspace (DatePickerField for the exam date), workspace picker, card overflow (edit, archive, delete) and sort
- Summary renderers in core/ui/summary, including the Canvas BinaryTreeDiagram and ComplexityTable
- Detail: header, metric tiles, sync banner, sticky PillTabRow. Ringkasan AI renders the preview. 'Sumber Materi' is an inline list (derived). The Flashcard and Kuis tabs show counts and navigate once P9 and P8 ship. 'Belajar bersama Mika' is gated until P7. The bookmark toggle uses Bookmark
- Two-pane: Materimu and Detail side by side on medium and expanded widths via TwoPaneLayout
- Keep 'Terminal Log Teknis' behind BuildConfig.DEBUG; drop the 'DEMO SIMULATOR STATUS' bar
- Tests: WorkspaceRepositoryTest (cache, fetch, error), UploadValidatorTest (size, pages, encrypted, magic bytes), a job-state reducer test, NoteSectionMapperTest (valid, partial, garbage), BookmarkRepositoryTest, MigrationTest 1 to 2, UploadWorker tests with WorkManager TestDriver (foreground info, resume after stop), and ViewModel tests

**Deliverable / acceptance:** After login or onboarding the app opens Materimu (the only shipped tab, so there is no bottom nav yet). It lists workspaces with search, the Semua/Aktif/Diarsipkan filters, sort and an offline cache, and can create a workspace with an exam date picked in the comic date picker. In Tambah Materi the picker offers only PDF and TXT files. A 25 MB PDF, a 120-page PDF and a password-protected PDF are each rejected with an inline error. A valid PDF uploads with an ongoing progress notification. Swiping the app away mid-upload does not stop it, and it completes. Status Pemrosesan advances the 5-step timeline live, and SUCCESS's 'Buka Catatan & Latihan Soal' opens Detail. With the app backgrounded, the completion notification still arrives and opens Status. FAILED shows 'Coba Unggah Ulang' prefilled. Detail renders the structured Ringkasan AI preview, including the tree diagram and complexity table. Bookmarking it shows a snackbar whose 'Lihat Koleksi' opens Koleksi Tersimpan with the workspace listed. On a Pixel Tablet AVD Materimu and Detail sit side by side. MigrationTest 1 to 2 is green, the project compiles with every legacy class deleted, and CI is green.

### P4 — Beranda, Profil & study-time tracking  (size XL, depends on [3])

**Goal:** Add the home dashboard with working actions and real study minutes, plus the complete account and settings center, built on the workspace data from P3.

**Screens:** `beranda_heymika_mobile`, `profil_dan_pengaturan_heymika_mobile`

**Foundation work:**

- ActiveTimeTracker in :core: rules in core/domain (60s idle cutoff, pause in background, day rollover in device local time) and wiring in HeyMikaRoot (ProcessLifecycleOwner plus a root pointer and key interaction hook that records timestamps only, never content). Writes ActiveTimeLog to Room (Migration 2 to 3, tested); ActiveTimeSyncWorker in core/data. This is the data source for Beranda's 'Target Harian' and 'Fokus Hari Ini' from day one, and later for Statistik
- Domain: TodayDashboard (the server DailyRecommendation and StudyTasks cached in Room; DailyTarget computed locally from ActiveTimeLog against StudyPreferences.dailyTargetMinutes, taking the server value when it is higher for multi-device), StudyTask plus the StudyTaskType tone map, and StudyTaskTargetResolver in core/domain (READ opens Detail now and Catatan from P6; NEW_MATERIAL opens Tambah Materi; QUIZ, FLASHCARD_REVIEW and MISTAKE_REMEDIAL resolve to null until P8/P9, so those tasks are hidden)
- Beranda (start tab from now on): greeting card from UserProgress with '+ Tambah Materi' ('Tanya Mika' is hidden until P7). Search leads to Materimu in search mode. Mika hero: 'Mulai Sesi Belajar Sekarang' launches the first incomplete resolvable StudyTask, or Tambah Materi when there is none (it never targets Rencana), and '3 tugas paling penting' scrolls to the checklist. The DailyTarget meter is a plain meter until P10 makes it open Statistik. The Fokus Hari Ini card uses the tracker. The SRS card is hidden until P9 (the scheduler lands there). Checklist completion is a PATCH. Recent-material cards open Detail. Upload banner. The Ask-Mika bar is hidden until P7 and the bell until P5
- Shell: tabs Beranda, Materimu and Profil; the avatar in top bars opens the Profil tab
- Profil in install-time :setting: a LazyColumn with sticky anchor tabs; avatar via PickVisualMedia; profile fields; change email leading into the verify flow; change password with PasswordPolicy (never prefilled); study preferences (dailyTargetMinutes immediately changes the Beranda target); tutor preferences; NotificationPreferences (push, daily reminder with ComicTimePicker, quiet hours with TimeRangeField, AI report) as the single source of truth in DataStore and on the server; font size via a LocalDensity fontScale override; plan card and quota meters read-only ('Kelola Tagihan' and 'Riwayat Invoice' hidden until P11); a 'Koleksi Tersimpan' row; data export request; danger zone (delete account, logout, logout from all devices) through ComicDialog, which clears Room, DataStore, the session and WorkManager user work and goes to auth/login. Save model: a global save bar for profile and preference fields, per-section buttons for email and password (pending PO)
- DailyReminderWorker in core/data: periodic, rescheduled on time change, honors quiet hours and the once-a-day limit, requests POST_NOTIFICATIONS
- Reshape SettingPreferences into UserPreferences (heymika_settings) with study, tutor, notification, accessibility and app preferences. dailyReminder becomes NotificationPreferences.dailyReminderEnabled; darkMode stays unread
- Tests: ActiveTimeTrackerTest (idle cutoff, background pause, day rollover), StudyTaskTargetResolverTest (shipped and unshipped specs), BerandaViewModelTest, ProfileViewModelTest (dirty and revert, validation), ReminderSchedulerTest (quiet hours across midnight), MigrationTest 2 to 3, and an androidTest for back-stack restoration when switching tabs

**Deliverable / acceptance:** After login, Beranda is the start tab and the bottom nav shows Beranda, Materimu and Profil. Reading a Detail for 3 minutes and then idling for 2 raises 'Target Harian' by 3-4 minutes, not 5. Backgrounding the app pauses the count. 'Mulai Sesi Belajar Sekarang' opens the Detail of the first READ task, and a new account with no tasks lands on Tambah Materi. Recent-material cards open Detail. In airplane mode the cached dashboard renders with the OfflineBanner. The Profil tab opens with no install dialog. Setting the reminder to 19:30 in the comic time picker reschedules it, which `adb shell am broadcast -a androidx.work.diagnostics.REQUEST_DIAGNOSTICS -p com.ai.heymika` shows in logcat. Changing the daily target in Profil updates Beranda's meter. Logout returns to Login with the back stack cleared. CI is green.

### P5 — Pusat Notifikasi & push  (size L, depends on [3, 4])

**Goal:** Add the notification inbox and push delivery, so that job, reminder, quota and account events reach the user and deep-link safely.

**Screens:** `pusat_notifikasi_heymika_mobile`

**Foundation work:**

- Domain and data: AppNotification, NotificationFilter, DateGroup, the NotificationCategory/variant tone map, Room storage (Migration 3 to 4, tested) and NotificationApi
- Push: if Firebase is approved, HeyMikaMessagingService in :app (base, so it resolves at process start) with token registration; otherwise a NotificationPollWorker in core/data. The data payload carries a route key and args. DeepLinkRouter validates them against the RouteSpec allow-list and builds an explicit PendingIntent to MainActivity; unknown keys fall back to Pusat Notifikasi. Channels per category. Quiet hours suppress heads-up display but still store the item in the inbox
- Inbox: read-state SegmentedControl with counts, category chips, sort, date groups, mark one or all read, delete with an undo snackbar, snooze 1 hour (server plus local), the quota meter card. Actions are hidden while their RouteSpec has not shipped
- The 'Tips dari Mika' quiet-hours and daily-limit toggles edit the same NotificationPreferences as Profil (P4)
- Bell with the unread count on every tab top bar
- The P3 job-completion events also land in the inbox, with 'Buka Workspace' and 'Unggah Ulang Dokumen' actions
- Tests: NotificationsViewModelTest (filters, counts, sort, grouping, undo), payload-to-route allow-list tests, quiet-hours display suppression, MigrationTest 3 to 4

**Deliverable / acceptance:** The bell with an unread dot appears on Beranda, Materimu and Profil. Pusat Notifikasi filters by read state and category, sorts, groups by date, marks all read, and deletes with undo. A finished P3 job appears as an item whose 'Buka Workspace' opens Detail. A test message from the FCM console (or the mock poller) shows a system notification that opens the right destination. A payload with an unknown route key opens Pusat Notifikasi instead. During quiet hours there is no heads-up, but the item is stored. Toggling quiet hours in the inbox card is reflected in Profil. CI is green.

### P6 — AI notes & citation viewer (:aistudio)  (size L, depends on [3])

**Goal:** Turn processed material into the full structured study note, and let learners check every AI claim against the original source page.

**Screens:** `catatan_ringkasan_ai_heymika_mobile`, `viewer_sumber_sitasi_ai_heymika_mobile`

**Foundation work:**

- Ship the first :aistudio RouteSpecs (notes/{materialId}?section=, viewer/{materialId}?citation=&page=), rendered through the AiStudioEntry Content contract
- Domain and data: full NoteSummary sections, SourceDocument, DocumentPage and Citation with pdfPage and printPage, the CitationKind tone map (NOTE orange, FLASHCARD sky, QUIZ success), NoteApi and SourceApi, Room tables (Migration 4 to 5, tested). Bookmark types NOTE and FORMULA ('Simpan Rumus' and the header bookmark) now show in Koleksi Tersimpan
- Note screen: breadcrumb, status pill, quick-action chips, every section renderer, the Mika assist strip and the sticky bottom bar. 'Tanya Mika' (P7), 'Buat Flashcard' (P9) and 'Uji Diri' / 'Kuis Cepat' (P8) are gated by shipped specs. 'Mode Sunting' is deferred pending PO
- Viewer: OCR-text mode is primary (citations rendered as blocks); PDF mode uses PdfRenderer on the downloaded file (app-private cache) or on server page images; page stepper with bounds; kind filter chips with counts; citation inspector as a ComicBottomSheet; copy with a 'Tersalin!' state; 'Pindai Ulang OCR' starts a job and pushes Status
- Re-route the earlier entry points: StudyTaskTargetResolver sends READ tasks to Catatan at the saved section, Detail's summary header opens Catatan, the job-success CTA opens Catatan, and the notification 'Lihat Sitasi Sumber' opens the viewer
- Make the compact control strip work at 360dp (single-line stepper label). Catatan and Viewer use TwoPaneLayout on medium and expanded widths
- Tests: citation filter and count ViewModel tests, page-stepper bounds, route-argument parsing, bookmark and formula toggles, MigrationTest 4 to 5

**Deliverable / acceptance:** From Detail's summary header or the job-success CTA, Catatan & Ringkasan renders every section type from the mock note, also from Room in airplane mode. Tapping 'Snippet' opens the viewer at the cited page with the inspector sheet. The PDF/OCR toggle, the page stepper (disabled at its bounds) and the kind filters work, and copying shows 'Tersalin!'. The note bookmark and 'Simpan Rumus' survive a restart and appear in Koleksi Tersimpan. A Beranda READ task now opens Catatan at the saved section. On a Pixel Tablet AVD note and viewer sit side by side. CI is green.

### P7 — Tanya Mika AI tutor (:aistudio)  (size L, depends on [3, 6])

**Goal:** Fill the center tab with the tutor: start conversations from prompts or any material context and get streamed, structured, citation-backed Socratic answers.

**Screens:** `mika_ai_tutor_heymika_mobile`, `percakapan_ai_tutor_heymika_mobile`

**Foundation work:**

- Ship the main/tutor tab spec (raised center tab) and tutor/conversation/{id}
- Domain and data: TutorConversation, ChatMessage, the sealed MessageBlock list, QuickPrompt, TutorStyle and StudyContext. TutorApi with a streaming transport (okhttp-sse or chunked; one-shot fallback if the PO rules out streaming). Messages persist in Room (Migration 5 to 6, tested), and failed sends can be retried
- Quota: check AiQuota before sending and show the QuotaExceeded state; the upsell CTA stays hidden until P11
- Tutor home: status row with quota chip, study context card (material picker sheet for 'Ganti Sumber', 'Mode Bebas'), hero, 4 PromptCards and the full ChatComposer
- Conversation: reverse LazyColumn, the compact composer with imePadding, suggestion pills, inline CheckQuestion answering with feedback (wrong-answer state derived), citation blocks that open the P6 viewer, a bookmark (type CONVERSATION, shown in Koleksi), and the 'Riwayat Obrolan' sheet built from shared components (HistorySection was deleted in P3)
- Unhide the entry points: the Beranda Ask-Mika bar and 'Tanya Mika' button, Detail's 'Belajar bersama Mika' and prompt chips, the note's 'Tanya Mika' and 'Analogi Mika', and the viewer's 'Tanya Mika Tentang Kalimat Ini' (quote context). Profil's tutor preferences are applied to requests
- Optional: SpeechRecognizer voice input. Image attach and the Σ palette are deferred
- Tests: streaming reducer (partial and complete blocks, error mid-stream), CheckQuestion evaluation, SSE parsing with MockWebServer, ViewModels with turbine, MigrationTest 5 to 6

**Deliverable / acceptance:** The Tanya Mika center tab appears and opens instantly. 'Gunakan Prompt' or typed text starts a conversation scoped to the chosen material. Mika's reply streams into structured blocks: analogy, steps, a citation, and an inline check question with feedback. 'Periksa di Dokumen Asli' opens the viewer at the quote. Sending from Beranda's Ask-Mika bar opens the conversation with the first message already streaming. The history sheet reopens past chats after a restart. With the mock quota set to 50/50, send is disabled and the QuotaExceeded state shows. CI is green.

### P8 — Quiz engine (:quiz)  (size XL, depends on [3, 6, 7])

**Goal:** Deliver the Kuis tab end to end: AI quiz generation from selected documents, a robust timed player that works offline, and an evaluative results page that links back to sources and the tutor.

**Screens:** `daftar_kuis_heymika_mobile`, `konfigurasi_kuis_baru_heymika_mobile`, `mengerjakan_kuis_heymika_mobile`, `hasil_kuis_evaluasi_heymika_mobile`

**Foundation work:**

- Create the install-time :quiz module: DynamicFeature entry, keep rule, EntryContractTest, QuizEntry Content, and the main/quiz tab spec plus pushed specs
- Domain and data: QuizSummary and stats, QuizGenerationConfig, Question, Hint, QuizAttempt, AnswerSubmission, AttemptResult, QuestionReview and SubtopicMastery (using MasteryBand). Tone maps for QuizStatus and Difficulty. QuestionDto mapper in core/data, tested against the legacy-quiz fixtures saved in P3. Room tables (Migration 6 to 7, tested)
- Generation: POST generate returns a job id and reuses the P3 job tracker. Show the credit cost and ETA, then open the player, or list the quiz as 'Draft AI'
- Player: a server-authoritative deadline with a local countdown. Answers are autosaved to Room and synced by AnswerSyncWorker, which lives in core/data and depends only on startup Koin modules, so a process WorkManager starts cold can run it. Answers after the deadline are rejected. Flag 'ragu-ragu' (flag icon, not bookmark), a Kisi Soal grid sheet, navigator pills with 48dp targets that auto-scroll, a hints sheet whose availability follows the mode rules (pending PO), submit and exit ComicDialogs, auto-submit on time-up, and resume after process death
- Result: score hero with ScoreDonut and SpeedLines, stat tiles, Mika feedback with the reward, subtopic mastery, the recommendation, a lazily filtered review list, citation rows that open the viewer, 'Bahas dengan Mika' and 'Tanya Mika Soal Ini' that open the tutor, and the Android share sheet. 'Simpan ke Bank Kesalahan' POSTs and shows a snackbar (the bank screen ships in P9)
- Entry points unhidden: the Kuis tab, Beranda QUIZ tasks (resolver), the note's 'Uji Diri' and 'Kuis Cepat', notification session reminders, and Detail's Kuis tab. Daftar Kuis and Hasil use TwoPaneLayout on medium and expanded widths
- Tests: QuestionDto mapper tests (legacy fixtures), AttemptTimer (deadline, background, process death), the offline answer queue (including rejection after the deadline), result filter counts, ViewModels, MigrationTest 6 to 7

**Deliverable / acceptance:** The Kuis tab lists quizzes with stats, the Semua/Siap/Draft AI/Selesai filters and recent attempts. 'Buat Kuis Baru' configures (workspace, docs, keywords, 10 questions, Sedang, Latihan), generates, and opens the player. Answering, flagging and hints work. Answers given in airplane mode sync after reconnect, even when the backgrounded process was killed first (`adb shell am kill com.ai.heymika`): WorkManager restarts it and the mock server log shows the answers, without the NavHost ever being created. Rotating, or force-stopping and relaunching, keeps the timer and the answers. Submit opens Hasil, with the breakdown, the mastery map and filtered reviews that link to the viewer and the tutor. A Beranda QUIZ task opens the player. CI is green.

### P9 — Remedial & spaced review (bank kesalahan + flashcards)  (size L, depends on [6, 8])

**Goal:** Close the learning loop: turn mistakes into targeted remedial practice, and schedule flashcard reviews that work fully offline.

**Screens:** `bank_kesalahan_heymika_mobile`, `daftar_flashcard_heymika_mobile`, `review_flashcard_heymika_mobile`

**Foundation work:**

- Bank (in :quiz): MistakeItem and diagnostics, the MistakePriority and RootCause tone maps, the review toggle, root-cause filters, expandable cards (Accordion Row), and a remedial generation request that opens the P8 player. Room cache. Entry points: a card on Daftar Kuis, links from Hasil, and Beranda MISTAKE_REMEDIAL tasks
- Create the install-time :flashcard module (DynamicFeature entry, keep rule, EntryContractTest). Domain: FlashcardDeck, Flashcard, SrsState, one Rating enum and label set (plus its tone map), ReviewLog and ReviewSession. The SRS scheduler is a pure :core domain use case (FSRS by default, pending PO) with interval previews computed locally. Decks, cards and logs are offline-first in Room (Migration 7 to 8, tested). ReviewLogSyncWorker lives in core/data
- Review session: question face, the Mika hint Accordion, reveal, rating grid, next card; a session-complete summary (derived); Sesi Kilat across decks; one 'favorite' concept via the star (Bookmark type CARD; the duplicate bookmark buttons are dropped); optional TextToSpeech (id-ID); FSRS internals hidden
- Unhide the dependents: Beranda's SRS card (due counts from the :core scheduler) and FLASHCARD_REVIEW tasks, the note's 'Buat 12 Flashcards', the Hasil 'Flashcard' link, Detail's Flashcard tab, and the Offline state's 'Buka Flashcard Offline'. Question bookmarks (type QUESTION) appear in Koleksi
- Tests: SrsSchedulerTest (intervals per rating, lapses, due ordering), ReviewSession ViewModel (reveal, rate, next, last card, summary), mistake filters and the review toggle, offline review-log sync, MigrationTest 7 to 8

**Deliverable / acceptance:** Bank Kesalahan lists the mistakes saved from P8 attempts, with diagnostics, filters and 'Tandai Review'. 'Latih (5 Soal)' opens a remedial quiz in the player. Daftar Flashcard shows due counts and retention bands. A review session reveals cards, shows interval previews and reschedules. A full session works in airplane mode and its logs sync after reconnect, even when WorkManager restarts a killed process. Beranda's SRS card now shows the scheduler's due count and opens review. The Offline state's 'Buka Flashcard Offline' opens the cached decks. CI is green.

### P10 — Study planner (:planner, install-time) & progress analytics (:insight, on-demand)  (size L, depends on [4, 8, 9])

**Goal:** Add adaptive planning that ties quizzes, flashcards and mistakes together, and works offline. Add honest progress measurement in the first on-demand module, proving that installing a split never resets navigation.

**Screens:** `rencana_belajar_heymika_mobile`, `statistik_progres_belajar_heymika_mobile`

**Foundation work:**

- Create the install-time :planner module (Rencana) and the on-demand :insight module (Statistik only), each with a DynamicFeature entry, keep rule and EntryContractTest. Both specs are declared in the registry, so :insight's destination exists in the stable graph before its split is installed
- Planner domain: StudyPlan, DayPlan (plus the DayPlanStatus tone map), ExamTarget, ScoreTarget, WeeklyGoal and MikaPlanAdvice, cached in Room (Migration 8 to 9, tested) so Rencana opens offline. minutesDone comes from the P4 ActiveTimeLog
- Rencana: a week strip (LazyRow, fits 360dp); StudyTaskCards routed through StudyTaskTargetResolver to the bank, flashcards and the player; the Pintasan Kilat quick quiz; the weekly goal ring; a locked tomorrow card (device local time); the plan editor sheet (derived; DatePickerField for the exam date, TimeRangeField-free minute steppers); 'Sinkronkan AI' regenerate. Entry points: 'Lihat Rencana' in Beranda's task section header, Materimu's weekly-goal row, Profil study preferences, notifications, and the tab slot if the PO picks the Rencana nav
- Statistik: the 7/30/90 range and the workspace picker; a StackedBarChart Canvas with an accessible table alternative; the priority-topic carousel; mastery cards (MasteryBand) whose CTAs generate a topic quiz with a prefilled QuizGenerationConfig; the recall distribution from ReviewLog; the integrity rules in an Accordion Card; 'Mulai Sesi Fokus' uses the same first-StudyTask resolver as Beranda's hero. Entry points: Beranda's target meter and focus card, the Profil level pill, the notification 'Cek Progres Belajar' and Hasil 'Riwayat'
- FeatureInstallPanel on the comic dialog styling for the first real on-demand split. Verify with `bundletool build-apks --local-testing` and `install-apks`, and document the steps for reviewers
- OnDemandInstallBackStackTest (androidTest; fake FeatureInstaller plus a test FeatureEntry standing in for :insight): open Kuis, push Hasil, switch to Profil, tap the level pill, step the fake through Pending, Downloading and Installed. Assert that Statistik renders, that back returns to Profil, that switching to Kuis restores Hasil, and that navController.graph is the same instance before and after
- Tests: stats range switching, MasteryBand thresholds, day-chip state mapping, plan regenerate states, MigrationTest 8 to 9

**Deliverable / acceptance:** Rencana opens from Beranda's 'Lihat Rencana', from Materimu's weekly-goal row and from a notification, with no install dialog, and also in airplane mode from the cached plan. Its week strip fits a 360dp screen. Its tasks open the bank, flashcards and the player, and the plan editor's date picker sets the exam date. On a `bundletool build-apks --local-testing` plus `install-apks` install, tapping the Profil level pill shows Statistik's in-place install panel with progress, then Statistik. System back returns to Profil, and the Kuis tab still shows the Hasil Kuis that was open before. OnDemandInstallBackStackTest asserts the same with a fake installer. Statistik switches between 7, 30 and 90 days, toggles chart and table, and its mastery CTAs generate a topic quiz. Its active minutes match Beranda's Target Harian for today. CI is green.

### P11 — Subscription & payment (:payment, on-demand)  (size L, depends on [3, 4])

**Goal:** Let users upgrade their plan and see trustworthy transaction status and invoices, and unhide every upsell entry point from the earlier phases.

**Screens:** `checkout_pembelian_paket_heymika_mobile`, `status_transaksi_invoice_heymika_mobile`

**Foundation work:**

- Decision gate first: Google Play Billing vs Midtrans under Play policy. Create the on-demand :payment module (DynamicFeature entry, keep rule, EntryContractTest). Implement a PurchaseGateway interface with PlayBillingGateway and/or MidtransGateway
- Domain and data: SubscriptionTier, PlanPeriodOption, PaymentMethod, Voucher, PriceQuote (tax and total computed on the server; no client math), Order with an idempotencyKey (safe on double-tap), Transaction (five statuses, plus the TransactionStatus tone map) and Invoice. IDR formatting for the id-ID locale
- Checkout: the tier segment and period cards actually re-quote prices on a tier switch, a benefits Accordion Card per tier, payment methods (Midtrans only), voucher validation with an inline error (no alert()), the billing summary with a ComicDivider total rule, and a sticky dock with a loading CTA. Drop the 'Simulator State Checkout'
- Midtrans return: the finish URL is the verified App Link https://${appLinkHost}/payment/status/{orderId} (path added to the base manifest's autoVerify filter; assetlinks.json on the backend). DeepLinkRouter routes it to the stable payment/status placeholder destination, which installs :payment in place if needed. The link is ONLY a trigger: the screen ignores any query params, fetches GET /orders/{orderId} and renders the server status. Poll while PENDING, plus push
- Play Billing path: there is no redirect. PurchasesUpdatedListener receives the purchaseToken, the backend verifies it with the Play Developer API and grants the entitlement, then the client acknowledges and navigates to payment/status/{orderId}. Pending purchases are handled through queryPurchasesAsync on resume
- After PAID: refresh Entitlement so quota pills update app-wide, and pop the checkout (popUpTo checkout inclusive)
- Status screen: per-status hero (SpeedLines and ComicDivider on PAID), ribbon and CTA variants (PAID: 'Buka Workspace Belajar'; PENDING: countdown and 'Lanjutkan Pembayaran'; FAILED: 'Coba Lagi'; EXPIRED: 'Buat Pesanan Baru'; REFUNDED: info). Invoice PDF via DownloadManager or share; a WhatsApp CS intent; legal links in Custom Tabs; backend jargon moved into a 'Detail teknis' Accordion; status chips removed
- Unhide the upsell entry points: Profil 'Kelola Tagihan' and 'Riwayat Invoice', Tambah 'Coba Pro', the QuotaExceeded 'Upgrade ke HeyMika Pro', the notifications 'Tambah Kuota' and 'Lihat Invoice', and tutor and quiz credit exhaustion
- Tests: quote and voucher state mapping, presentation mapping for all five statuses, idempotent create-order, a status-link test proving URL params are ignored (Midtrans), PurchasesUpdatedListener handling with a fake BillingClient (Play), and entitlement refresh after PAID

**Deliverable / acceptance:** From Profil or a QuotaExceeded state, :payment installs in place (bundletool local-testing) and the back stack survives. Checkout re-quotes from the server on tier, period and voucher changes, and a double-tap on 'Lanjutkan ke Pembayaran Aman' creates exactly one order. Midtrans path: the sandbox payment returns through the App Link to Status Transaksi. `adb shell am start -W -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d 'https://heymika.test/payment/status/ORD-1?status=PAID' com.ai.heymika` still shows PENDING, because the mock server says ORD-1 is pending, which proves the URL is not trusted. Play Billing path: on an internal-test-track build with a license tester, a test-instrument purchase reaches Status after server verification. A PAID transaction updates the entitlement shown in Profil and Tambah Materi. Mock PENDING, FAILED, EXPIRED and REFUNDED orders each render the correct CTA. CI is green.

### P12 — Hardening, accessibility & release  (size M, depends on [5, 10, 11])

**Goal:** Close the remaining security and quality gaps from the structure audit and ship a reviewer-ready release bundle.

**Screens:** -

**Foundation work:**

- Certificate pinning: an OkHttp CertificatePinner with leaf and backup SHA-256 pins for the real BuildConfig host (enabled by BuildConfig.PINNING_ENABLED in release and in a pinningCheck build type that initWith(debug)), plus a network_security_config pin-set with an expiration date. debug-overrides trust user CAs for local proxies. ErrorMapper maps SSLPeerUnverifiedException to a logged pinning failure
- Root detection: RootBeer via core/utils/RootDetectionUtil, enforced in release builds only (debug and pinningCheck skip it, so the pinning check can run on an emulator), checked in MainActivity before the NavHost, with a non-cancelable ComicDialog that calls finish()
- OWASP dependency-check plugin (root alias) and a CI security job (continue-on-error, NVD API key secret, cached data, report artifact)
- CI coverage gate on JaCoCo (target agreed with the team, e.g. at least 60% for core domain and data, which now hold every parser and mapper)
- Accessibility pass: contentDescription for icon-only and emoji controls, 48dp targets, TalkBack order and the accordion/picker announcements, focus-ring D-pad traversal on every screen, an overflow check of pills and top bars at font scale 1.3-2.0, a contrast check of white on #0075E8 labels, and two-pane layouts on the Pixel Tablet AVD
- Koleksi Tersimpan: apply the PO decision (keep as is, or delete :favorite and its keep rule and FeatureEntry)
- Clean-up: a LeakCanary pass across the main flows, an optional Baseline Profile for startup, and removal of any RouteSpec still flagged unshipped
- Release checks: mapping.txt keeps all FeatureEntry classes (aistudio, favorite, setting, quiz, flashcard, planner, insight, payment); README with the bundletool --local-testing reviewer steps for the on-demand modules and the App Link verification commands

**Deliverable / acceptance:** The CI release pipeline passes lint, unit tests with the coverage gate, the OWASP report and assembleRelease/bundleRelease. The pinning check uses the pinningCheck build on a non-rooted emulator with mitmproxy's CA installed as a user CA (trusted through debug-overrides). Every API call fails and logcat shows `javax.net.ssl.SSLPeerUnverifiedException: Certificate pinning failure!`. A control debug build with pinning disabled, through the same proxy, has its traffic decrypted in mitmproxy, which proves the CA is trusted and only the pinner blocks it. The release build on a rooted Google APIs emulator (after `adb root`) shows the security dialog and finishes. A TalkBack walkthrough of login, home, upload, quiz and result succeeds at 200% font scale without clipped pills, and D-pad traversal shows the focus ring on every control. A bundletool local-testing install exercises both on-demand modules without losing the back stack, and mapping.txt keeps every FeatureEntry.

## 3. Module map

### `:app` (base)

Screens: `onboarding_heymika_mobile`, `login_heymika_mobile`, `register_heymika_mobile`, `lupa_password_heymika_mobile`, `reset_password_heymika_mobile`, `verifikasi_email_heymika_mobile`, `beranda_heymika_mobile`, `pusat_notifikasi_heymika_mobile`, `materimu_heymika_workspace_mobile`, `detail_workspace_heymika_mobile`, `tambah_materi_baru_heymika_mobile`, `status_pemrosesan_bahan_belajar_heymika_mobile`

The base APK is always present at first launch. It holds MainActivity and HeyMikaRoot, the root composable that owns the OfflineBanner, the global snackbar host and the tracker's input hook. It also holds HeyMikaNavHost, which is built once from the :core route registry, plus StartupGate, AppShell, DeepLinkRouter, FeatureDestinationHost with FeatureInstallPanel, and the FCM service (P5). Auth and onboarding gate app start, and the https App Links for verify-email, reset-password and payment return must resolve before any split exists, so they live here. Beranda is the start tab. Pusat Notifikasi opens from push. The workspace pipeline lives here too: every later feature reads Workspace/Material, Materimu is a tab root, and job notifications open Status. WorkManager workers are deliberately NOT in :app or in any feature: they live in :core/data, so a process that WorkManager starts in the background resolves them from the startup Koin modules. Packages: com.ai.heymika.{startup, auth, onboarding, shell, home, notification, workspace, navigation}.

### `:core` (library)

Screens: `system_states_error_handling_heymika_mobile`

Shared library, keeping the reference capstone layering. ui/ holds composables only: the theme, components, core/ui/summary renderers, MikaErrorState and the adaptive layouts. Everything with logic lives outside ui/ so JaCoCo counts it: domain/ (models, enums, AppError, Quota, PasswordPolicy, MarkupParser, StudyTaskTargetResolver, ActiveTimeTracker rules, SRS scheduler), data/ (repositories, Room + SQLCipher, DataStore, Retrofit APIs, ErrorMapper, NoteSectionMapper and other DTO mappers, and every WorkManager worker), di/, navigation/ (the HeyMikaRoutes RouteSpec registry, FeatureEntry, HeyMikaNavigator) and utils/. system_states maps here because it ships as the reusable AppError-driven MikaErrorState. Its catalog page exists only in the debug gallery. Dynamic features import :core and :app and never each other.

### `:aistudio` (dynamic-feature)

Screens: `mika_ai_tutor_heymika_mobile`, `percakapan_ai_tutor_heymika_mobile`, `catatan_ringkasan_ai_heymika_mobile`, `viewer_sumber_sitasi_ai_heymika_mobile`

Existing module, switched to install-time in P1, because Tanya Mika is a bottom-nav tab root and notes must open offline. Its legacy AiStudio screens are deleted in P3, in the same phase that removes the Detail action that opened them, so the module is never unreachable dead code. From P3 to P5 its entry registers no shipped specs. It ships notes and the citation viewer in P6 and the tutor in P7. Keeping the name avoids churn in DynamicFeature, the keep rules and the Koin wiring.

### `:quiz` (dynamic-feature)

Screens: `daftar_kuis_heymika_mobile`, `konfigurasi_kuis_baru_heymika_mobile`, `mengerjakan_kuis_heymika_mobile`, `hasil_kuis_evaluasi_heymika_mobile`, `bank_kesalahan_heymika_mobile`

New install-time module, created in P8 when its first screen ships. Kuis is the proposed slot-4 tab, and remedial quizzes must work offline. It owns the whole assessment loop. The mistake bank (P9) launches remedial sets through the same player. The answer-review components shared with the tutor live in :core. Its AnswerSyncWorker lives in :core/data, not here.

### `:flashcard` (dynamic-feature)

Screens: `daftar_flashcard_heymika_mobile`, `review_flashcard_heymika_mobile`

New install-time module, created in P9. The Offline system state promises 'Buka Flashcard Offline', and an on-demand split cannot be downloaded offline. Flashcard is also the slot-4 tab in five mockups. The SRS scheduler is a pure :core domain use case, so Beranda's SRS card and Statistik read due counts without this module. ReviewLogSyncWorker lives in :core/data.

### `:setting` (dynamic-feature)

Screens: `profil_dan_pengaturan_heymika_mobile`

Existing module, switched to install-time in P1, because Profil is a tab root and must open instantly (this matches the reference capstone). The dark-mode row is hidden in P0. The legacy SettingScreen is deleted in P3 together with the old tab bar, and Profil & Pengaturan Akun ships here in P4. The module name and FeatureEntry are kept.

### `:planner` (dynamic-feature)

Screens: `rencana_belajar_heymika_mobile`

New INSTALL-TIME module, created in P10. Rencana is a tab root in its own mockup (Beranda / Materi / Rencana / Kuis / Profil), and Beranda, Materimu's weekly-goal row and notifications link to it, so it must open offline and on sideloaded or reviewer installs. Beranda's hero CTA 'Mulai Sesi Belajar Sekarang' does NOT depend on this module: it launches the first resolvable StudyTask, which lands in :app, :aistudio, :quiz or :flashcard. StudyTask and StudyTaskCard live in :core and are shared with Beranda. Because it is already install-time, picking the Rencana bottom-nav variant needs no delivery change.

### `:insight` (dynamic-feature)

Screens: `statistik_progres_belajar_heymika_mobile`

New ON-DEMAND module (P10), now holding only Statistik. It is optional, network-bound analytics with heavy custom Canvas charts. It is never a tab in any mockup and never the target of a primary CTA, and it is reached only by push: the Beranda target meter and focus card, the Profil level pill, the notification 'Cek Progres Belajar' and Hasil 'Riwayat'. It installs in place inside its stable placeholder destination, so the back stack survives. Offline, the install panel shows the Offline state with retry, which is honest because Statistik needs the network anyway. It is the first module to exercise the on-demand flow. Making Statistik a tab would force it to install-time.

### `:payment` (dynamic-feature)

Screens: `checkout_pembelian_paket_heymika_mobile`, `status_transaksi_invoice_heymika_mobile`

New ON-DEMAND module (P11). It is rarely used, and the gateway decision (Play Billing vs Midtrans) is still open, so the purchase SDK and its risk stay out of the base APK. The Midtrans finish URL is a verified https App Link declared on the base manifest. It routes to the stable status placeholder destination, which installs :payment in place and then fetches the order status from the server. With Play Billing there is no redirect at all.

### `:favorite` (dynamic-feature)

Screens: -

Existing module, switched to install-time in P1. It owns no mockup screen. In P3, the same phase that removes Material.isFavorite and MaterialCard, FavoriteScreen/FavoriteViewModel are reshaped into 'Koleksi Tersimpan' over the Bookmark entity, so CI stays green. It shows workspace bookmarks first, then notes and formulas (P6), conversations (P7), and cards and questions (P9). It opens from Detail's bookmark snackbar ('Lihat Koleksi') and, from P4, from a Profil row. It is kept because the capstone rubric expects a favorite feature in a dynamic module. If the PO declines, it is deleted in P12.

## 4. Navigation graph

**Start destination:** startup: the StartupGate route in :app. It keeps the Android 12 SplashScreen on screen (keepOnScreenCondition) while SessionUseCase resolves the stored session and GET /users/me (cached). It then navigates with popUpTo(startup){inclusive=true} to auth/login (no session), auth/verify-email (session, email not verified), onboarding/step1 (onboarding incomplete) or main. Inside main, the start tab is the first shipped ShellTab: Materimu during P3, Beranda (main/home) from P4 on. StartupGate never stays in the back stack. A pending App Link captured before sign-in is replayed after routing.

**Auth flow:**

- No session goes to auth/login, the root of the nested 'auth' graph. It has no back button; system back exits the app.
- After a successful login, routing follows the rules shown by the mockup's demo chips. emailVerified=false goes to auth/verify-email. onboardingCompleted=false goes to onboarding/step1. Otherwise the user goes to main with popUpTo('auth'){inclusive=true}. A wrong password stays on the screen and shows the inline error alert.
- 'Lanjutkan dengan Google' uses Credential Manager and POST /auth/google. A new user (isNewUser) goes to onboarding/step1, skipping verification if the PO confirms. A returning user follows the same routing rules.
- auth/login leads to auth/register. A successful email registration goes to auth/verify-email?email=. 'Masuk di sini', the back pill or system back pops to login.
- auth/login leads to auth/forgot-password. 'Kirim link reset' swaps to the sent state in place (no new destination). The verified App Link https://${appLinkHost}/reset-password?token= opens auth/reset-password, which validates the token. An expired token leads to forgot-password. Success leads to auth/login, or to main if the backend returns a session. If the user is signed in when the link opens, a ComicDialog asks them to confirm sign-out first.
- auth/verify-email has four states: waiting, resent, verified and expired. It is reached after register, after an unverified login, after an email change in Profil, and from the session-expired state. It flips to verified through the App Link https://${appLinkHost}/verify-email?token= or through an ON_RESUME refresh of /users/me. 'Mulai Belajar Sekarang' then goes to onboarding/step1, or to main if onboarding is already done. Back signs out and returns to auth/login.
- The nested 'onboarding' graph runs step1 'Profil Diri' (mocked), then step2 'Target & Topik' (sets StudyPreferences.dailyTargetMinutes, which Beranda's tracker target uses) and step3 (both derived from shared components), then main with popUpTo('onboarding'){inclusive=true}. An OnboardingViewModel scoped to the graph's back-stack entry persists progress to DataStore so the wizard can resume. Back on step1 signs out after confirmation.
- Anywhere in main, a 401 that survives the single-flight token refresh in the OkHttp Authenticator emits SessionExpired. The root then clears the session and WorkManager user work, navigates to auth/login with popUpTo(0), and shows a 'Sesi Habis' snackbar. Logout and logout-all from Profil (P4) take the same path. Until P4, debug builds can sign out from the gallery's Sesi panel.

**Bottom tabs:**

- Beranda (main/home, :app): ships in P4 and becomes the start tab then. Mockup: Beranda.
- Materimu (main/workspace, :app): ships in P3 and is the only shipped tab during P3, so the bar stays hidden in that phase. The label is 'Materimu'; the mockups split between Materi (4), Materimu (2) and Materiku (5), so this is an open question.
- Tanya Mika (main/tutor, :aistudio, install-time): ships in P7 as the raised center tab. It appears in 10 of the 11 mockup navs.
- Slot 4, proposed Kuis (main/quiz, :quiz, install-time): ships in P8. Evidence is a 5-vs-5 tie. Kuis appears in Beranda, Profil, Pusat Notifikasi, Rencana and Statistik. Flashcard appears in Materimu, Tanya Mika, Daftar Kuis, Daftar Flashcard and Bank Kesalahan, and Daftar Kuis itself has no Kuis tab. Rencana appears only in Rencana, and 'Sesi Belajar' only in Tambah Materi. Kuis, Flashcard and Rencana are all install-time modules, so the PO can choose any of them with no delivery change. Only making Statistik a tab would force :insight to install-time.
- Profil (main/profile, :setting, install-time): ships in P4. Mockup: Profil & Pengaturan. The tab label is 'Profil' (4 navs) rather than 'Akun' (3 navs).

**Notes:**

- STABLE GRAPH (fix for the back-stack reset): HeyMikaNavHost builds the NavGraph exactly ONCE from the HeyMikaRoutes registry in :core. Every destination is declared up front, including those owned by on-demand splits. A base spec renders its :app composable. A feature spec renders FeatureDestinationHost(spec): when the split is present, it resolves the module's FeatureEntry by reflection and calls entry.Content(spec, backStackEntry, navigator); when it is missing, it shows FeatureInstallPanel in place and swaps to the content once installed. The old remember(installStates){ installedEntries() } rebuild is deleted, so NavController.setGraph runs once and an install can never clear saved tab stacks or pop to the start destination (navigation-compose 2.8.8 does both on a non-equal setGraph). GraphStabilityTest (P1) and OnDemandInstallBackStackTest (P10) lock this in.
- Contract: a RouteSpec in :core declares pattern, arguments, owner (Base or Feature(moduleName)), shellTab, deepLinkPaths, requiresAuth and a shipped flag. FeatureEntry becomes { val moduleName; @Composable fun Content(spec, entry, navigator) } and loses register(NavGraphBuilder). HeyMikaNavigator (navigate(routeSpec, args), back, switchTab, openExternal) is the only navigation API features see, so they never touch NavHostController. Specs flagged with a shellTab are placed inside the nested navigation(route='main') graph; the others are pushed destinations in the same nested graph.
- Root graph: startup, then the nested 'auth', 'onboarding' and 'main' graphs. Switching between auth and main always clears the back stack, so system back never returns to login after sign-in.
- Tabs navigate with popUpTo(main start){saveState=true}, launchSingleTop and restoreState=true, so each tab keeps its scroll position and back stack. The bottom nav shows only on the five tab roots and only when at least two tabs have shipped. Every other destination is a full-screen push without the nav: notifications, workspace detail, tambah materi, job status, notes, viewer, conversation, quiz config, player, result, bank, flashcard list and review, planner (unless it becomes a tab), stats, checkout, invoice and Koleksi Tersimpan. This also fixes the Tambah Materi and Bank mockups, which show a tab bar with no back affordance.
- Shipped gating: a CTA, tab, notification action or StudyTask whose RouteSpec has shipped=false is hidden. The shipped flag is flipped in the phase that ships the screen, so every phase is demoable. The first external release is after P12; no release build ships before P4, because Profil (logout) arrives in P4.
- Deep links: the only exported entry points are https App Links with android:autoVerify=true on host ${appLinkHost}. That is a manifestPlaceholder read from local.properties heymika.applink.host, with heymika.test as the mock default. Paths: /verify-email?token= opens auth/verify-email, /reset-password?token= opens auth/reset-password, and /payment/status/{orderId} opens payment/status/{orderId} (the Midtrans finish URL, P11). The backend serves assetlinks.json with the debug, release and Play app-signing SHA-256 fingerprints. No custom-scheme intent filter is exported. Notification and FCM taps use explicit PendingIntents to MainActivity that carry a route key and arguments. DeepLinkRouter validates every key against the RouteSpec allow-list (unknown keys fall back to Pusat Notifikasi), applies the auth gate (stores and replays the link after sign-in) and calls navigator.navigate. On-demand targets install in place inside their placeholder destination.
- Guarded exits: the quiz player's back and 'Keluar Sementara' show a confirm dialog (answers are autosaved). Status Pemrosesan's 'Batalkan' confirms, then pops to Detail. Checkout pops itself (popUpTo checkout inclusive) once the server reports PAID, so back from Status Transaksi does not reopen a stale checkout. Verify-email back signs out.
- Beranda's hero CTA 'Mulai Sesi Belajar Sekarang' launches the first incomplete StudyTask that StudyTaskTargetResolver can resolve to a shipped spec: READ opens Detail (P4), and Catatan from P6. NEW_MATERIAL opens Tambah Materi, QUIZ the player (P8), and FLASHCARD_REVIEW and MISTAKE_REMEDIAL open review and bank (P9). With no resolvable task it pushes Tambah Materi. '3 tugas paling penting' scrolls to the checklist. Rencana is never a hero target; it is reached from 'Lihat Rencana' in the task section header (P10), Materimu's weekly-goal row and notifications.
- Cross-cutting UI: HeyMikaRoot hosts OfflineBanner (since P0), the global snackbar host and the ActiveTimeTracker's interaction hook (P4), above the NavHost, so they cover auth, shell and pushed screens alike. QuotaExceeded from any AI call shows the MikaErrorState sheet; its 'Upgrade' leads to payment/checkout from P11 and is hidden before then. The Ask-Mika bars (Beranda, Statistik) and Detail's prompt chips create a tutor conversation and push tutor/conversation/{id} with the first message already sent (P7).
- Adaptive layout never changes the graph. List-detail pairs (Materimu+Detail, Catatan+Viewer, Daftar Kuis+Hasil, Daftar Flashcard+deck preview) render as TwoPaneLayout inside one destination on medium and expanded widths. On compact the same destination shows one pane and pushes the detail spec.
- The design-system gallery and the system-states catalog are NOT in the production graph. They live in a debug-only DesignGalleryActivity (app/src/debug, with its own launcher alias) that has its own small NavHost. Main code references debug code only through a DebugHooks object that has the same FQCN in app/src/debug and app/src/release (the release copy is a no-op), so release builds compile and contain no gallery classes.

## 5. Design system
### Tokens

- DECISION primary: colorScheme.primary = #0075E8 (DESIGN.md prose; the brand blue in the Beranda/Materimu HTML) for fills, focus rings and active states, with primaryPressShadow #005FC4. The YAML primary #005AB5 is kept as extended token primaryText for links, small text and icons on white (6.7:1 contrast vs 4.46:1 for #0075E8). primaryContainer #0072E3, onPrimary #FFFFFF, onPrimaryContainer #FEFCFF
- primary family, complete from the YAML: primaryFixed #D7E3FF, primaryFixedDim #AAC7FF, onPrimaryFixed #001B3E, onPrimaryFixedVariant #00458E (shadow of the active font-size segment in profil, the user chat bubble and the send FAB), inversePrimary #AAC7FF (links on the inverse-surface snackbar)
- surfaceTint = #005CBA, exactly the YAML value. It does NOT follow the #0075E8 override. HeyMika never uses tonal elevation: every wrapper passes tonalElevation = 0.dp and comic shadows replace elevation. The tint therefore only shows on stray M3 defaults and stays inside the token family
- ink #082A52: 3dp comic borders, speech-bubble outline and tail, ComicDivider, headings, secondary-button border
- secondary family: secondaryContainer #FF8A00 (accent: comic offset shadow, accent buttons, streak), secondary #914C00 (accent base shadow, brown text), onSecondary #FFFFFF, onSecondaryContainer #613100, secondaryFixed #FFDCC4, secondaryFixedDim #FFB77F, onSecondaryFixed #2F1500, onSecondaryFixedVariant #6F3900 (danger-button base shadow in profil)
- tertiary family: extended sky #16B5F5 (halftone, speed lines, progress, sparkles); tertiary #006388, onTertiary #FFFFFF, tertiaryContainer #007DAB, onTertiaryContainer #FCFCFF, tertiaryFixed #C5E7FF, tertiaryFixedDim #7FD0FF (Draft AI quiz shadow), onTertiaryFixed #001E2D, onTertiaryFixedVariant #004C6A
- named accents: badgeCyan #C5EFFF (hero pills, info), brandYellow #FFD34E (rewards, stickers, 'Mika' tags), ribbonRed #EF3E36 (unread dot, mascot bow, urgency; never used for errors)
- surfaces: canvas #F8FCFF is colorScheme.background and every screen ground. The YAML background #F9F9FF and surfaceBright #F9F9FF are declared as tokens, but canvas wins for screen grounds. surface #FFFFFF (cards), surfaceDim #C8DBFF, surfaceVariant #D6E3FF, surfaceContainerLowest #FFFFFF, surfaceContainerLow #F0F3FF, surfaceContainer #E7EEFF, surfaceContainerHigh #DEE8FF, surfaceContainerHighest #D6E3FF, inverseSurface #113059 (snackbar), inverseOnSurface #ECF1FF, onSurface/onBackground #001B3C, onSurfaceVariant #414754
- lines: borderMuted #D8DDF4, outline #717785, outlineVariant #C1C6D6, textMuted #5868A4 (captions, leading icons)
- dividers, two tokens with a fixed usage rule. ComicDivider is a 3dp ink stroke used ONLY inside Comic-variant shells, per DESIGN.md 'interior panel dividers utilize matching 3px navy strokes': the auth split-shell seam, the mascot banner/form seam on mobile auth cards, the score hero's sections, the checkout 'Ringkasan Tagihan' total rule and the transaction status hero. SubtleDivider is 1.5dp borderMuted, used in Subtle cards, list rows, forms, dialogs, bottom sheets and nested panels, per DESIGN.md 'Interior Tonal Stacking'
- status: danger #BC2535 and dangerBg #FFF0F2 are mapped onto M3 error/errorContainer, with onError #FFFFFF. The YAML error #BA1A1A, errorContainer #FFDAD6 and onErrorContainer #93000A are recorded as reference only and never used. success #167549 with successBg at 10%; warning = secondaryFixed tint + secondary text; info = badgeCyan
- tones: P0 defines only the mechanism, a ToneSpec(container, content, shadow?) and a palette of NAMED tones: blue, teal, orange, emerald, rose, slate, sky, yellow, danger, success, outline, primaryContainer, ribbonRed, tertiaryFixedDim, secondaryFixedDim. Each enum-to-tone map is added in the phase that introduces its enum: WorkspaceCategory, JobStepStatus and MasteryBand in P3; StudyTaskType in P4; NotificationCategory in P5; CitationKind in P6; QuizStatus and Difficulty in P8; Rating, MistakePriority and RootCause in P9; DayPlanStatus in P10; TransactionStatus in P11
- ColorTokensTest (P0, unit): every YAML color and every named accent has an asserted value in HeyMikaColors or the M3 scheme, so a token cannot be dropped silently
- type.family: Nunito Sans TTFs bundled in core/src/main/res/font (OFL; weights 400, 500, 600, 700, 800, 900). There are no downloadable fonts, so the first frame and offline rendering are stable. Mono: JetBrains Mono (bundled) for timers, order and job IDs, and code blocks. The Kalam comic accent is deferred
- type.scale compact, used below 600dp and also on medium width, where the split panels are about phone-width (sp/lineHeight/weight/tracking): displayHeroMobile 32/40/900/-0.01em (M3 displaySmall); headlineLgMobile 26/34/800/-0.01em (headlineLarge); headlineMd 24/32/800 (headlineMedium); headlineSm 20/28/700 (headlineSmall, titleLarge); bodyLg 18/28/400; bodyMd 16/24/400; bodySm 14/20/400; labelLg 16/22/700 (button label); labelMd 14/18/700/+0.02em; labelSm 12/16/700/+0.05em (uppercase pills); caption 12/16/500. The floor is 12sp: the mockups' 10-11sp labels round up
- type.scale expanded, used at 1200dp and wider (DESIGN.md desktop): displayHero 48/56/900/-0.02em and headlineLg 36/44/800/-0.015em replace the two mobile styles. The other styles are unchanged. HeyMikaTypography exposes compact() and expanded(), and HeyMikaTheme picks one from the window class
- shape: control 14dp (buttons; inputs 12-14dp), innerCard 16dp (DESIGN lg), speechBubble 16dp plus tail, shell 28dp (hero and auth shells; DESIGN 28-32dp; the YAML xl of 24dp is ignored for shells), sheet and dialog 28dp top corners, pill = CircleShape. The HTML's Tailwind overrides (lg=8, xl=12) are ignored
- spacing: xs 4dp, sm 8dp, md 16dp, lg 24dp, xl 32dp. Screen side margin 16dp on compact. Shell internal padding is 16-20dp on compact, 32dp on medium and 64dp on expanded. Section gap 24dp
- border: ink 3dp (comic shells, speech bubbles, accent-button outline, ComicDivider); control 2dp (borderMuted inputs, ink secondary button, checkbox and radio); subtle 1.5dp borderMuted (utility and feed cards, SubtleDivider)
- shadow.comic: a hard-offset rounded rect in #FF8A00 with no blur, drawn in Modifier.drawBehind (BOM 2025.01 has no dropShadow API). Offset by window class: 6dp compact, 8dp medium, 14dp expanded. Danger variant #BC2535. Layouts reserve the offset so nothing clips
- shadow.press: 4dp y-only base in #005FC4 for primary, #914C00 for accent and #082A52 for secondary/ink. Pressed: translateY 2dp and base 2dp, a 150ms tween driven by collectIsPressedAsState
- shadow.subtleFloat: 0/4/16 rgba(8,42,82,0.06), via Modifier.shadow(spotColor/ambientColor = ink at 6%), plus a 1.5dp borderMuted border, for list and feed cards
- size: primary button 56dp (compact in-card 40dp, still a 48dp touch target), input 56dp, checkbox and radio 20dp, icons 20-24dp, top bar 64dp, bottom nav 64dp (flat five-tab bar as in the Beranda mockup, PO decision 3; the 56dp raised center tab token is kept but unused), avatar steps 32/40/56/64/80/96/144dp
- focus: a 2dp #0075E8 border plus a 3dp outer ring at rgba(0,117,232,0.25), applied by Modifier.comicFocusRing to EVERY focusable control (buttons, clickable cards, option cards, chips, tabs, segmented controls, nav items, checkboxes, switches, accordion headers, picker fields), not only text fields. Error: 2dp danger border, alert icon and a dangerBg callout
- motion: press 150ms, pulsing live dot 1.2s, striped progress 1s loop, accordion expand 200ms with a rotating chevron. All motion is disabled when the system animator duration scale is 0 (reduced motion): loops stop and the accordion and dialogs switch to instant
- decoration: halftone is a 1.5dp dot on a 14-20dp grid in sky or surfaceContainerHighest at 8-15% alpha. Speed lines are radial or diagonal ink/sky strokes at 6-12% alpha. Both stay strictly in peripheral zones, are never placed behind body text, and use clearAndSetSemantics
- breakpoints and adaptive policy: HeyMikaWindowClass is derived from material3-adaptive currentWindowAdaptiveInfo with DESIGN-aligned cut-offs. Compact (<600dp) is a single column with the mobile rules. Medium (600-1199dp; tablets portrait, foldables, phone landscape at 600dp+) gives auth a 50/50 split shell, 32dp internal padding, 8dp comic offset and list-detail two-pane where defined. Expanded (>=1200dp) gives a 53/47 split, 64dp padding, 14dp offset, the expanded type ramp and a 1380dp max content width. Short heights (phone landscape) always scroll and never split vertically. Exposed as LocalHeyMikaLayout { windowClass, shadowOffset, shellPadding, splitRatio, typeRamp }
- iconography: material-icons-extended (already an api dependency of :core) plus bundled vector drawables for the Material Symbols it lacks (chat_spark, neurology, cottage, psychology_alt) and the multicolor Google G. Emoji in copy stays in string resources. Emoji used as icons are replaced by vectors that have a contentDescription

### Components

- {"name": "HeyMikaTheme (HeyMikaColors, HeyMikaTypography, HeyMikaShapes, HeyMikaDimens, LocalHeyMikaLayout)", "spec": "Replaces the placeholder core/ui/theme (the Indigo/Teal/Amber ramps, FontFamily.Default and AiLearnTheme). It is an M3 lightColorScheme filled from every YAML slot, with primary overridden to #0075E8, error mapped to danger, background set to canvas and surfaceTint left at the YAML #005CBA. Non-M3 tokens (ink, accent, sky, yellow, badgeCyan, borderMuted, textMuted, dangerBg, success, the named-tone palette, and border, shadow and focus dimens) go through staticCompositionLocalOf and are read as HeyMikaTheme.colors/.type/.dimens/.layout. The typography ramp and shadow offset follow HeyMikaWindowClass. Dynamic color is off. v1 is light-only (decided in P0): the darkTheme parameter is removed and MainActivity no longer reads the darkMode preference.", "used_by": ["all 29 screens"]}
- {"name": "Modifier.comicSurface + ComicCard(variant)", "spec": "drawBehind paints the hard offset rounded rect first, then the white surface and its border. The offset comes from LocalHeyMikaLayout (6/8/14dp). Variants: Comic (3dp ink + orange offset; shells, heroes, selected content, auth cards), Subtle (1.5dp borderMuted + ambient float; list, feed and utility cards), Danger (danger offset on dangerBg; danger zone), Muted (archived: outlineVariant border, no shadow, 90% alpha), Ribbon(tone) (6dp leading accent bar; mistake cards) and StatusShadow(tone) (quiz status cards; tones mapped in P8). Parameters: radius (16 inner / 28 shell), borderWidth, and an onClick with a 0.99 press scale and comicFocusRing. The Comic-vs-Subtle choice per surface is made once in P0 and documented in the gallery.", "used_by": ["login_heymika_mobile", "register_heymika_mobile", "onboarding_heymika_mobile", "beranda_heymika_mobile", "materimu_heymika_workspace_mobile", "status_pemrosesan_bahan_belajar_heymika_mobile", "daftar_kuis_heymika_mobile", "review_flashcard_heymika_mobile", "rencana_belajar_heymika_mobile", "profil_dan_pengaturan_heymika_mobile", "bank_kesalahan_heymika_mobile", "checkout_pembelian_paket_heymika_mobile"]}
- {"name": "ComicDivider + SubtleDivider", "spec": "ComicDivider is a 3dp ink stroke, horizontal or vertical, and is the only divider allowed inside a Comic-variant shell. Its uses are fixed: the auth split seam, the mascot banner/form seam, the score hero sections, the checkout total rule and the transaction hero. SubtleDivider is 1.5dp borderMuted, used in Subtle cards, list rows, forms, sheets and dialogs. Both are decorative, with no semantics.", "used_by": ["login_heymika_mobile", "register_heymika_mobile", "lupa_password_heymika_mobile", "reset_password_heymika_mobile", "verifikasi_email_heymika_mobile", "onboarding_heymika_mobile", "hasil_kuis_evaluasi_heymika_mobile", "checkout_pembelian_paket_heymika_mobile", "status_transaksi_invoice_heymika_mobile", "profil_dan_pengaturan_heymika_mobile", "pusat_notifikasi_heymika_mobile", "catatan_ringkasan_ai_heymika_mobile"]}
- {"name": "Modifier.comicFocusRing", "spec": "Observes the shared MutableInteractionSource (collectIsFocusedAsState, with onFocusChanged as a fallback) and draws a 2dp primary border plus a 3dp outer ring at 25% alpha outside the component bounds, so layout does not shift. It is applied inside every interactive component in :core, so features cannot forget it. The P0 gallery verifies it with D-pad/TAB traversal and the P12 accessibility pass re-checks it.", "used_by": ["all 29 screens"]}
- {"name": "ComicButton(variant, size, loading, leadingIcon, trailingIcon)", "spec": "Primary: #0075E8 fill, white labelLg, 14dp radius, 56dp height (Compact 40dp), 4dp y-only #005FC4 base; pressed it translates 2dp down with a 2dp base over 150ms. Accent: #FF8A00 fill, 3dp ink outline, white label, #914C00 base (celebration and gamification CTAs). Secondary: white with a 2dp ink border. Tonal: surfaceContainerLow with primaryText. Danger: danger fill. Also Text and TextLinkArrow ('Buka Deck'). Loading swaps the label ('Memverifikasi...', 'Mengunci Transaksi...'), shows a 20dp spinner and disables clicks. Optional trailing pill slot ('Ready'). Role.Button, a 48dp minimum touch target and comicFocusRing.", "used_by": ["all 29 screens"]}
- {"name": "GoogleSignInButton", "spec": "A Secondary ComicButton (white, 2dp ink border, 56dp) with the bundled multicolor G vector and a configurable label ('Lanjutkan dengan Google' / 'Daftar dengan Google'). It only emits a click. The Credential Manager call lives in the auth ViewModel/activity layer, not in :core/ui.", "used_by": ["login_heymika_mobile", "register_heymika_mobile"]}
- {"name": "HeyMikaTextField family (TextField, PasswordField, TextArea, SearchField, ClearableTextField, DropdownField, PickerField)", "spec": "Built on OutlinedTextField/BasicTextField: 56dp tall, 14dp radius, white fill, 2dp borderMuted border, 20dp textMuted leading icon. The label row holds the label, a danger-colored required asterisk and a right-aligned helper or trailing status slot ('Maks. 80 Karakter', the strength label, 'Cocok!'). Focus: primary border plus a 3dp ring. Error: danger border, alert icon and a dangerBg callout. PasswordField adds the visibility toggle. SearchField takes a trailing mic, tune or 'Cari' slot. DropdownField uses ExposedDropdownMenuBox. PickerField is a read-only field that opens a sheet. Supports KeyboardOptions, autofill content types (emailAddress, password, newPassword), maxLength and imeAction.", "used_by": ["login_heymika_mobile", "register_heymika_mobile", "lupa_password_heymika_mobile", "reset_password_heymika_mobile", "verifikasi_email_heymika_mobile", "onboarding_heymika_mobile", "profil_dan_pengaturan_heymika_mobile", "beranda_heymika_mobile", "materimu_heymika_workspace_mobile", "tambah_materi_baru_heymika_mobile", "konfigurasi_kuis_baru_heymika_mobile", "daftar_kuis_heymika_mobile", "bank_kesalahan_heymika_mobile", "daftar_flashcard_heymika_mobile", "checkout_pembelian_paket_heymika_mobile", "system_states_error_handling_heymika_mobile"]}
- {"name": "ComicTimePicker + TimeRangeField + DatePickerField", "spec": "Themed wrappers so the pickers never fall back to stock M3. ComicTimePicker is an M3 TimePicker (24h, id-ID) inside a ComicDialog shell. TimePickerDefaults.colors maps the clock dial to surfaceContainerLow, the selector to primary and the period/hour chips to badgeCyan with ink text. Buttons are ComicButtons. TimeRangeField is a PickerField showing 'HH:mm - HH:mm' that opens two sequential ComicTimePicker steps ('Mulai' / 'Selesai') and validates ranges that cross midnight, such as quiet hours 22:00-07:00. DatePickerField is a PickerField that opens an M3 DatePicker in a ComicDialog, with DatePickerDefaults.colors mapped to tokens, selectableDates limited to today or later for exam targets, and id-ID formatting ('14 Juli 2025'). All values go out as java.time types. Every field has comicFocusRing and announces its value to TalkBack.", "used_by": ["profil_dan_pengaturan_heymika_mobile", "pusat_notifikasi_heymika_mobile", "materimu_heymika_workspace_mobile", "detail_workspace_heymika_mobile", "rencana_belajar_heymika_mobile"]}
- {"name": "Accordion / ExpandableSection", "spec": "A header row (icon, title, optional subtitle, trailing chevron that rotates 0 to 180 degrees) over AnimatedVisibility content (expandVertically plus fade, 200ms). With animator scale 0 it switches to an instant snap. Semantics: Role.Button, stateDescription 'Diperluas'/'Diciutkan', and expand()/collapse() accessibility actions, so TalkBack reads and toggles the state. The expanded state is hoisted (rememberSaveable by default). Variants: Inline (in-card hint drawers), Card (Subtle card shell: checkout benefits, integrity rules) and Row (expandable list cards such as mistake items).", "used_by": ["checkout_pembelian_paket_heymika_mobile", "review_flashcard_heymika_mobile", "statistik_progres_belajar_heymika_mobile", "bank_kesalahan_heymika_mobile", "status_transaksi_invoice_heymika_mobile"]}
- {"name": "HeyMikaCheckbox / HeyMikaRadio / ToggleRow", "spec": "A 20dp rounded square or circle with a 2dp ink border. When checked it fills with primary and shows a white check vector. Labels accept an AnnotatedString with LinkAnnotation (Ketentuan Layanan / Kebijakan Privasi). ToggleRow is an icon tile, title and caption with a themed M3 Switch on a surfaceContainerLow row, at least 48dp tall, with comicFocusRing.", "used_by": ["login_heymika_mobile", "register_heymika_mobile", "pusat_notifikasi_heymika_mobile", "profil_dan_pengaturan_heymika_mobile", "status_pemrosesan_bahan_belajar_heymika_mobile", "konfigurasi_kuis_baru_heymika_mobile", "beranda_heymika_mobile", "bank_kesalahan_heymika_mobile", "checkout_pembelian_paket_heymika_mobile"]}
- {"name": "SelectableOptionCard + SegmentedControl", "spec": "Radio-group option card: a leading indicator (letter tile, check badge, radio or icon tile), title, sublines, a trailing badge or price, and an optional corner ribbon ('REKOMENDASI', 'Terpilih'). Selected state is a primary fill (onboarding) or a tint plus a comic or hard bottom shadow (profil, checkout). Uses Modifier.selectableGroup with Role.RadioButton and comicFocusRing. SegmentedControl has 2-3 equal segments in a tinted track; the active segment is white-raised or primary-filled; counts and sublabels are optional ('Sedang / Paling Cocok').", "used_by": ["onboarding_heymika_mobile", "profil_dan_pengaturan_heymika_mobile", "pusat_notifikasi_heymika_mobile", "tambah_materi_baru_heymika_mobile", "konfigurasi_kuis_baru_heymika_mobile", "viewer_sumber_sitasi_ai_heymika_mobile", "statistik_progres_belajar_heymika_mobile", "checkout_pembelian_paket_heymika_mobile"]}
- {"name": "Pills & tags (BrandHeroPill, StatusPill, CountPill, StepChip, SubjectTag, StickerLabel, GamificationPill, CodeBadge)", "spec": "Full-radius pills. BrandHeroPill is badgeCyan with ink uppercase labelSm and a leading sparkle. StatusPill has an optional pulsing live dot and a ToneSpec. SubjectTag reads its tone from the enum map added in the owning phase. StickerLabel is yellow with an ink border and a -1 to -3 degree rotation ('WORKSPACE', 'GANBATTE!'). GamificationPill covers Streak, XP, Level and Quota, each with an onClick. All are single-line with ellipsis (fixing the wrapping bugs visible in the PNGs) and have no fixed height.", "used_by": ["all 29 screens"]}
- {"name": "FilterChipRow / PillTabRow", "spec": "A LazyRow of single-select pills with optional count badges and a per-chip tone. The active pill is primary-filled and the row auto-scrolls to the selection. PillTabRow adds icons and can be pinned with stickyHeader (Detail tabs; Profil section anchors that follow the scroll position). Every chip has comicFocusRing.", "used_by": ["beranda_heymika_mobile", "pusat_notifikasi_heymika_mobile", "profil_dan_pengaturan_heymika_mobile", "materimu_heymika_workspace_mobile", "detail_workspace_heymika_mobile", "viewer_sumber_sitasi_ai_heymika_mobile", "daftar_kuis_heymika_mobile", "hasil_kuis_evaluasi_heymika_mobile", "bank_kesalahan_heymika_mobile", "daftar_flashcard_heymika_mobile", "system_states_error_handling_heymika_mobile"]}
- {"name": "MikaAvatar + MikaSpeechBubble + MikaCallout presets", "spec": "MikaAvatar(pose, size, frame Circle|RoundedTile, badge, glow). Poses: WAVING, WAVING_PENCIL, ENVELOPE, HAPPY, WORRIED, THINKING, plus six error poses (CONFUSED_MAP, PADLOCK, YAWN_HOURGLASS, UNPLUGGED, HARDHAT, TROPHY). Badges: 'AI', 'Lv.n', '2 HINT', star, paw. Each pose ships as a placeholder vector until the real assets arrive. MikaSpeechBubble is white with a 3dp ink border, 16dp radius and a triangular tail (Start/End/Top/Bottom) built as one GenericShape, so the border follows the tail. It has an optional speaker row ('SAPAAN MIKA', 'Mika Berbisik') with a tag pill, a RichText body and a footer slot. Presets: Hero, Tip, Guide, Whisper and Error.", "used_by": ["onboarding_heymika_mobile", "login_heymika_mobile", "register_heymika_mobile", "lupa_password_heymika_mobile", "reset_password_heymika_mobile", "verifikasi_email_heymika_mobile", "beranda_heymika_mobile", "profil_dan_pengaturan_heymika_mobile", "system_states_error_handling_heymika_mobile", "materimu_heymika_workspace_mobile", "detail_workspace_heymika_mobile", "tambah_materi_baru_heymika_mobile", "status_pemrosesan_bahan_belajar_heymika_mobile", "catatan_ringkasan_ai_heymika_mobile", "viewer_sumber_sitasi_ai_heymika_mobile", "mika_ai_tutor_heymika_mobile", "percakapan_ai_tutor_heymika_mobile", "daftar_kuis_heymika_mobile", "konfigurasi_kuis_baru_heymika_mobile", "mengerjakan_kuis_heymika_mobile", "hasil_kuis_evaluasi_heymika_mobile", "bank_kesalahan_heymika_mobile", "daftar_flashcard_heymika_mobile", "review_flashcard_heymika_mobile", "rencana_belajar_heymika_mobile", "statistik_progres_belajar_heymika_mobile", "checkout_pembelian_paket_heymika_mobile", "status_transaksi_invoice_heymika_mobile"]}
- {"name": "HalftoneBackground / HalftoneDecoration / SpeedLinesDecoration", "spec": "HalftoneBackground is the canvas ground with a drawBehind dot grid and optional sparkle vectors at the edges; HalftoneDecoration is the corner variant for cards. SpeedLinesDecoration is a Canvas of 12-24 tapered strokes, radial from an off-screen focal point or as diagonal bursts, in sky or ink at 6-12% alpha, drawn only in peripheral bands (an outer 20% inset mask). It frames celebration and hero moments: verify success, reset success, job success, the quiz score hero, transaction PAID, the onboarding header and the auth split-shell mascot panel. Paths are cached with remember, so nothing is allocated per frame. All three are decorative (clearAndSetSemantics), never sit behind body text, and are static under reduced motion.", "used_by": ["login_heymika_mobile", "lupa_password_heymika_mobile", "reset_password_heymika_mobile", "verifikasi_email_heymika_mobile", "onboarding_heymika_mobile", "status_pemrosesan_bahan_belajar_heymika_mobile", "daftar_kuis_heymika_mobile", "hasil_kuis_evaluasi_heymika_mobile", "daftar_flashcard_heymika_mobile", "status_transaksi_invoice_heymika_mobile"]}
- {"name": "Adaptive layouts: AuthSplitShell + TwoPaneLayout", "spec": "AuthSplitShell: on compact it is a single Comic card with the mascot banner on top, a ComicDivider and the form below. On medium it is a 50/50 split with 32dp padding and an 8dp offset. On expanded it is a 53/47 split (mascot panel / form) with 64dp padding, a 14dp offset and a vertical ComicDivider seam. TwoPaneLayout: on compact it shows one pane. On medium and expanded it shows a list and a detail pane (40/60), used inside a SINGLE destination for list-detail pairs (Materimu+Detail, Catatan+Viewer, Daftar Kuis+Hasil, Daftar Flashcard+deck preview), so the nav graph never changes with window size. It is built on material3-adaptive window info only, with no ListDetailPaneScaffold dependency.", "used_by": ["login_heymika_mobile", "register_heymika_mobile", "lupa_password_heymika_mobile", "reset_password_heymika_mobile", "verifikasi_email_heymika_mobile", "onboarding_heymika_mobile", "materimu_heymika_workspace_mobile", "detail_workspace_heymika_mobile", "catatan_ringkasan_ai_heymika_mobile", "viewer_sumber_sitasi_ai_heymika_mobile", "daftar_kuis_heymika_mobile", "hasil_kuis_evaluasi_heymika_mobile", "daftar_flashcard_heymika_mobile"]}
- {"name": "HeyMikaTopBar (Tab, Stack, Auth) + NotificationBellButton + ProfileAvatarButton", "spec": "Tab variant: the logo image only (the duplicate 'HeyMika' wordmark text, which clips in every PNG, is dropped), trailing GamificationPills, a bell with a ribbonRed unread dot (hidden until P5) and a 32dp avatar in a 44dp target (hidden until P4). Stack variant: back arrow, a single-line headlineSm title with no logo (this fixes the truncated titles) and up to two actions. Auth variant: leading BackPill ('Login'), centered logo, trailing ActionPill ('Bantuan'), and no avatar. All variants are 64dp, translucent white with a bottom hairline, and padded for the statusBars inset. The fake iOS status bar and home indicator from the mockups are never drawn.", "used_by": ["all 29 screens"]}
- {"name": "HeyMikaBottomNavBar", "spec": "Stateless. The tabs come from a ShellTab enum in the order Beranda, Materimu, Tanya Mika (raised 56dp center tile with an ink border, hard base shadow and the Mika face), slot 4 (Kuis by default, configurable pending the PO decision) and Profil. The active tab gets a primary icon, a bold label and an orange dot; badge dots are supported. Only tabs whose RouteSpec is shipped are rendered, and the bar is hidden when fewer than two tabs have shipped (P3). It is hidden on pushed routes and padded for the navigationBars inset. Items have comicFocusRing.", "used_by": ["beranda_heymika_mobile", "materimu_heymika_workspace_mobile", "mika_ai_tutor_heymika_mobile", "daftar_kuis_heymika_mobile", "profil_dan_pengaturan_heymika_mobile"]}
- {"name": "StickyBottomActionBar / FloatingInputBar", "spec": "A Scaffold bottomBar container: white at 95% with a top hairline, padded for navigationBars and the IME, holding a caption row and 1-2 buttons. FloatingInputBar is the Ask-Mika bar and the unsaved-changes 'Simpan' bar, pinned above the nav. Both report their measured height so the scrolling content can add matching bottom padding (fixes the content hidden under the bar in the PNGs).", "used_by": ["onboarding_heymika_mobile", "beranda_heymika_mobile", "profil_dan_pengaturan_heymika_mobile", "catatan_ringkasan_ai_heymika_mobile", "viewer_sumber_sitasi_ai_heymika_mobile", "percakapan_ai_tutor_heymika_mobile", "konfigurasi_kuis_baru_heymika_mobile", "mengerjakan_kuis_heymika_mobile", "hasil_kuis_evaluasi_heymika_mobile", "statistik_progres_belajar_heymika_mobile", "checkout_pembelian_paket_heymika_mobile", "status_transaksi_invoice_heymika_mobile"]}
- {"name": "Progress set (ComicLinearProgress, SegmentedProgressBar, StripedProgressBar, ProgressRing, ScoreDonut)", "spec": "A rounded track with a fill whose tone is passed in as a ToneSpec. Callers map MasteryBand (introduced in P3) to a tone, so P0 does not depend on that enum. The segmented variant takes (fraction, tone) pairs. The striped variant animates and stops under reduced motion. ProgressRing shows a centered label. ScoreDonut draws multiple arcs on a Canvas. Every variant exposes progressBarRangeInfo semantics.", "used_by": ["onboarding_heymika_mobile", "beranda_heymika_mobile", "pusat_notifikasi_heymika_mobile", "profil_dan_pengaturan_heymika_mobile", "materimu_heymika_workspace_mobile", "detail_workspace_heymika_mobile", "tambah_materi_baru_heymika_mobile", "status_pemrosesan_bahan_belajar_heymika_mobile", "mika_ai_tutor_heymika_mobile", "daftar_kuis_heymika_mobile", "hasil_kuis_evaluasi_heymika_mobile", "bank_kesalahan_heymika_mobile", "daftar_flashcard_heymika_mobile", "review_flashcard_heymika_mobile", "rencana_belajar_heymika_mobile", "statistik_progres_belajar_heymika_mobile"]}
- {"name": "StatTile / MetricTile + SectionHeader (Tag, Numbered, DateGroup)", "spec": "StatTile: a tinted icon square, a caption label, a headline value with unit, an optional delta or status chip, an optional caption or mini bar, and a ToneSpec; laid out in a 2-column grid. SectionHeader variants: tag, title and trailing count or action ('3 tersisa', 'Lihat Rencana'); a numbered badge, title and trailing pill (Profil 01-09, checkout 1-2); and a dot, uppercase label and hairline (notification date groups).", "used_by": ["beranda_heymika_mobile", "pusat_notifikasi_heymika_mobile", "profil_dan_pengaturan_heymika_mobile", "detail_workspace_heymika_mobile", "tambah_materi_baru_heymika_mobile", "daftar_kuis_heymika_mobile", "hasil_kuis_evaluasi_heymika_mobile", "daftar_flashcard_heymika_mobile", "rencana_belajar_heymika_mobile", "statistik_progres_belajar_heymika_mobile", "checkout_pembelian_paket_heymika_mobile"]}
- {"name": "InlineAlertBanner / InfoNoteCard", "spec": "A rounded tinted callout: icon, bold title, body and an optional action. Variants: Error (dangerBg/danger), Info (badgeCyan), Success (successBg) and Warning (secondaryFixed). Used for login errors, verify status, privacy, security and data-safety notes, capacity warnings and the voucher-applied banner.", "used_by": ["login_heymika_mobile", "register_heymika_mobile", "lupa_password_heymika_mobile", "reset_password_heymika_mobile", "verifikasi_email_heymika_mobile", "pusat_notifikasi_heymika_mobile", "profil_dan_pengaturan_heymika_mobile", "detail_workspace_heymika_mobile", "konfigurasi_kuis_baru_heymika_mobile", "bank_kesalahan_heymika_mobile", "rencana_belajar_heymika_mobile", "checkout_pembelian_paket_heymika_mobile", "status_transaksi_invoice_heymika_mobile"]}
- {"name": "MikaErrorState / MikaEmptyState / OfflineBanner / SkeletonBlock", "spec": "Driven by the AppError sealed type in :core/domain: NotFound, Forbidden, SessionExpired, Offline, Maintenance(etaMinutes), QuotaExceeded(quota: Quota, minimal type defined in P0), Validation, Server and Unknown. Renders a StatusCodePill with a code badge, a per-error MikaAvatar pose with a top-tail bubble, a title, a description, an optional inline search slot (404) and primary, secondary and 'Lapor Bug' actions passed in by the caller, so navigation stays out of :core. FullScreen and Inline variants. MikaEmptyState is the same layout for empty lists. OfflineBanner is driven by ConnectivityObserver and mounted once in HeyMikaRoot, above the NavHost, from P0 onward; it also appears in the debug gallery activity. SkeletonBlock is a static-tint loading placeholder. Copy follows the catalog principles (non-judgemental, always offers a way out).", "used_by": ["system_states_error_handling_heymika_mobile", "beranda_heymika_mobile", "pusat_notifikasi_heymika_mobile", "materimu_heymika_workspace_mobile", "daftar_kuis_heymika_mobile", "daftar_flashcard_heymika_mobile", "mika_ai_tutor_heymika_mobile", "tambah_materi_baru_heymika_mobile", "statistik_progres_belajar_heymika_mobile"]}
- {"name": "HeyMikaSnackbarHost + ComicDialog + ComicBottomSheet", "spec": "The snackbar uses inverseSurface #113059, a 16dp radius, a brandYellow leading icon and an optional action (inversePrimary text; Undo for delete or snooze). It is hosted in HeyMikaRoot and replaces every alert() and prompt() from the HTML. ComicDialog: a comic shell with a mascot slot and primary/secondary actions (cancel job, exit or submit quiz, delete account, logout from all devices, change email); it also hosts the time and date pickers. ComicBottomSheet is a themed ModalBottomSheet (tonalElevation 0) for the workspace and material pickers, create workspace, the kisi soal grid, filter and sort, tutor style, the citation inspector and the plan editor.", "used_by": ["login_heymika_mobile", "lupa_password_heymika_mobile", "verifikasi_email_heymika_mobile", "pusat_notifikasi_heymika_mobile", "profil_dan_pengaturan_heymika_mobile", "materimu_heymika_workspace_mobile", "tambah_materi_baru_heymika_mobile", "status_pemrosesan_bahan_belajar_heymika_mobile", "viewer_sumber_sitasi_ai_heymika_mobile", "mengerjakan_kuis_heymika_mobile", "rencana_belajar_heymika_mobile"]}
- {"name": "ResendCooldownButton + CountdownTimerPill", "spec": "Timers are derived in the ViewModel from server timestamps (sentAt + cooldown, token expiresAt, attempt deadline), never from a local counter, so rotation and process death do not reset them. They render mm:ss in the mono style. The button is disabled with the countdown, then enabled ('Bisa dikirim ulang' / 'Siap'). The pill has a pulsing live dot and fires onExpired at 00:00.", "used_by": ["lupa_password_heymika_mobile", "verifikasi_email_heymika_mobile", "reset_password_heymika_mobile", "mengerjakan_kuis_heymika_mobile", "status_transaksi_invoice_heymika_mobile"]}
- {"name": "RichText (composable) over core/domain/text MarkupParser", "spec": "MarkupParser is plain Kotlin in core/domain/text, so it is unit-tested and counted by JaCoCo. It turns constrained markup (**bold**, *italic*, ==highlight==, `code`, and the tone spans {danger}/{success}/{primary}) into a Markup span tree. RichText in core/ui/text only maps that tree to an AnnotatedString, and InlineCodeChip renders `code` spans as tinted mono chips with a good or bad tone. HTML is never rendered.", "used_by": ["beranda_heymika_mobile", "detail_workspace_heymika_mobile", "catatan_ringkasan_ai_heymika_mobile", "percakapan_ai_tutor_heymika_mobile", "mengerjakan_kuis_heymika_mobile", "hasil_kuis_evaluasi_heymika_mobile", "bank_kesalahan_heymika_mobile", "review_flashcard_heymika_mobile", "rencana_belajar_heymika_mobile", "daftar_flashcard_heymika_mobile"]}
- {"name": "SummaryBlock renderers (core/ui/summary)", "spec": "Composables only, one per NoteSection type: KeyConceptCard, MetricItemTile, RegulatorRow, MikaTipCallout ('Simpan Rumus'), ReactionStepRow with EnergyDeltaBadge, ComplexityTable (per-cell tone, horizontal scroll), GlossaryTermCard, SourceReferenceRow ('Snippet'), VisualNoteCard with a Canvas BinaryTreeDiagram and TraversalSequenceRow, ExamReadinessCard, and a TextBlock fallback (absorbs the old ProseSection). Parsing lives in core/data/mapper/NoteSectionMapper (P3), which is tolerant: unknown or malformed sections become Text. The renderers live in :core because Detail (base) shows a preview and Catatan (:aistudio) shows the full note.", "used_by": ["detail_workspace_heymika_mobile", "catatan_ringkasan_ai_heymika_mobile", "percakapan_ai_tutor_heymika_mobile"]}
- {"name": "CitationQuoteCard", "spec": "Tinted from the CitationKind tone map added in P6. It shows a kind label pill ('Rujukan Mika #1'), a verification tag, an italic quote, a 'used in' caption, a page locator (pdfPage plus printPage) and actions: Konteks AI, Buka Kartu, Periksa di Dokumen Asli, and Salin with a 'Tersalin!' state.", "used_by": ["viewer_sumber_sitasi_ai_heymika_mobile", "percakapan_ai_tutor_heymika_mobile", "hasil_kuis_evaluasi_heymika_mobile", "bank_kesalahan_heymika_mobile", "catatan_ringkasan_ai_heymika_mobile"]}
- {"name": "ChatComposer (Full, Compact) + QuickPromptChip + PromptCard", "spec": "Full: an attached-source chip, a tutor-style dropdown chip, a multiline field, a toolbar (citation, Σ, mic) and Kirim. Compact: attach, a pill field with mic, and a circular send FAB with a hard shadow and press-translate. Send is disabled while the field is empty or the quota is exhausted. Handles imePadding. PromptCard has an icon tile, tag, title, a 2-line prompt and 'Gunakan Prompt'.", "used_by": ["beranda_heymika_mobile", "detail_workspace_heymika_mobile", "mika_ai_tutor_heymika_mobile", "percakapan_ai_tutor_heymika_mobile", "statistik_progres_belajar_heymika_mobile"]}
- {"name": "Answer components (AnswerOptionRow, AnswerComparisonBox, FeedbackCallout, MikaNoteBox)", "spec": "AnswerOptionRow: a 28dp letter tile, rich text, and the states unselected, selected ('Pilihanmu'), correct, wrong and disabled. AnswerComparisonBox stacks 'Jawaban Kamu' (danger) and 'Kunci Jawaban' (success), with an inline compact variant. FeedbackCallout comes in success or error tone. MikaNoteBox holds a mnemonic or analogy with a source reference. These live in :core because both :aistudio (the inline Socratic check) and :quiz use them.", "used_by": ["percakapan_ai_tutor_heymika_mobile", "mengerjakan_kuis_heymika_mobile", "hasil_kuis_evaluasi_heymika_mobile", "bank_kesalahan_heymika_mobile"]}
- {"name": "StudyTaskCard", "spec": "Task-type chip (tone map added in P4), course label, duration chip, title, description, a status row (urgency dot, progress n/m, target or memory level), an optional completion checkbox, and a primary or outline CTA. The CTA destination comes from StudyTaskTargetResolver (core/domain), and the card hides its CTA when the resolved RouteSpec has not shipped. It lives in :core because Beranda (base) and Rencana (:planner) both render it.", "used_by": ["beranda_heymika_mobile", "rencana_belajar_heymika_mobile"]}
- {"name": "FeatureInstallPanel (app/navigation)", "spec": "Rendered by FeatureDestinationHost inside a feature's stable placeholder destination while its split is missing, in place of the old FeatureInstallDialog. It shows MikaAvatar with a speech bubble, progress (Pending, Downloading with a fraction, Installing), Play's user-confirmation button, Retry, and 'Kembali' (navigator.back). When offline it shows the Offline MikaErrorState. When the split reports Installed, the same destination swaps to the feature content without any graph change.", "used_by": ["statistik_progres_belajar_heymika_mobile", "checkout_pembelian_paket_heymika_mobile", "status_transaksi_invoice_heymika_mobile"]}
- {"name": "Feature-local components (not in :core)", "spec": "Kept inside the owning module so :core stays lean. :app: WorkspaceDeckCard and RecentMaterialCard (with an archived variant), UploadDropzone, SelectedFileCard, QuotaCard, ProcessingTimeline (DONE/RUNNING/PENDING/FAILED step items) with JobSuccessPanel (SpeedLines) and JobFailurePanel, NotificationCard. :aistudio: PageStepper, ZoomPill, DocumentPageCard. :quiz: QuestionNavigatorStrip, CodeVisualBlock, SubtopicMasteryRow, QuizCard variants. :flashcard: ComicFlashcard, SrsRatingGrid, FlashcardDeckCard. :planner: WeekDayStrip, LockedDayPreviewCard. :insight: StackedBarChart with a table alternative, TopicMasteryCard. :payment: PlanPeriodCard, PaymentMethodOptionCard, PriceBreakdown with IDR formatting, TransactionStatusHero.", "used_by": ["materimu_heymika_workspace_mobile", "tambah_materi_baru_heymika_mobile", "status_pemrosesan_bahan_belajar_heymika_mobile", "pusat_notifikasi_heymika_mobile", "viewer_sumber_sitasi_ai_heymika_mobile", "mengerjakan_kuis_heymika_mobile", "hasil_kuis_evaluasi_heymika_mobile", "daftar_kuis_heymika_mobile", "review_flashcard_heymika_mobile", "daftar_flashcard_heymika_mobile", "rencana_belajar_heymika_mobile", "statistik_progres_belajar_heymika_mobile", "checkout_pembelian_paket_heymika_mobile", "status_transaksi_invoice_heymika_mobile"]}

## 6. Domain model

- **AppError + Quota (minimal, P0)**
  - AppError sealed: NotFound, Forbidden, SessionExpired, Offline, Maintenance(etaMinutes?), QuotaExceeded(quota: Quota), Validation(field, code), Server(code), Unknown(cause)
  - Quota { kind: DOCUMENTS|PAGES|AI_CHAT|GENERATOR_CREDITS, used: Int, limit: Int? (null = unlimited), period: DAILY|MONTHLY, resetAt: Instant? }, defined in P0 so AppError compiles and extended by Entitlement in P3
  - ErrorMapper (core/data): 401, then refresh, then SessionExpired; 403 to Forbidden; 404 to NotFound; 402/429 or a quota body to QuotaExceeded; 503 with Retry-After to Maintenance; IOException to Offline; SSLPeerUnverifiedException to Server(PINNING) (P12)
  - system_states_error_handling_heymika_mobile
- **User + AuthSession (P1)**
  - User.id
  - fullName
  - nickname
  - email
  - emailVerified: Boolean
  - onboardingCompleted: Boolean
  - authProvider: PASSWORD|GOOGLE
  - avatar: url | mikaStickerId
  - username
  - bio
  - studentCode ('#MK-8829')
  - institution / cohort label
  - lastLogin { device, location, at }
  - AuthSession { accessToken, refreshToken, expiresAt, rememberMe }, stored with Tink AEAD in DataStore and never in Room
  - login_heymika_mobile
  - register_heymika_mobile
  - verifikasi_email_heymika_mobile
  - onboarding_heymika_mobile
  - profil_dan_pengaturan_heymika_mobile
  - beranda_heymika_mobile
- **LearnerProfile + OnboardingProgress (P1)**
  - nickname (required, prefilled from fullName or the Google name)
  - learnerStatus: MAHASISWA|SISWA_SMA_SMK|PEMBELAJAR_MANDIRI|PROFESIONAL_KARIER
  - responseLanguage: ID|EN|GAUL
  - step 2 targets: dailyTargetMinutes 15|30|60, interestTopics (derived screen)
  - currentStep, totalSteps=3, xpOnComplete
  - onboarding_heymika_mobile
  - profil_dan_pengaturan_heymika_mobile
- **PasswordPolicy + PasswordStrength (core/domain use case, P1)**
  - rules: minLength=12, upperAndLower, digit, symbol (one canonical set, pending PO)
  - score 0..4
  - level: EMPTY|WEAK|FAIR|STRONG|VERY_STRONG, mapped to one Indonesian label set
  - PasswordMatch: EMPTY|MATCH|MISMATCH
  - register_heymika_mobile
  - reset_password_heymika_mobile
  - profil_dan_pengaturan_heymika_mobile
- **EmailVerification + PasswordReset (P1, P2)**
  - EmailVerification { email, status: WAITING|RESENT|VERIFIED|EXPIRED, sentAt, expiresAt, resendAvailableAt }
  - ResetLinkRequest { email, sentAt, resendAvailableAt }
  - ResetToken { token, valid, expiresAt, email? }
  - ResetResult { sessionsRevoked, xpAwarded? }
  - verifikasi_email_heymika_mobile
  - lupa_password_heymika_mobile
  - reset_password_heymika_mobile
- **UserProgress (gamification read model, introduced P3 for top-bar pills)**
  - level
  - levelTitle ('Pelajar Tangkas')
  - levelProgressPct
  - totalXp
  - streakDays
  - nextBadge { name, daysRemaining }
  - mascotLevel
  - XpReward { amount, reason }, returned by actions
  - beranda_heymika_mobile
  - pusat_notifikasi_heymika_mobile
  - profil_dan_pengaturan_heymika_mobile
  - materimu_heymika_workspace_mobile
  - mika_ai_tutor_heymika_mobile
  - daftar_kuis_heymika_mobile
  - hasil_kuis_evaluasi_heymika_mobile
  - daftar_flashcard_heymika_mobile
  - review_flashcard_heymika_mobile
  - rencana_belajar_heymika_mobile
  - statistik_progres_belajar_heymika_mobile
  - onboarding_heymika_mobile
  - reset_password_heymika_mobile
- **Entitlement + AiQuota (read model P3; refreshed after PAID in P11)**
  - plan: FREE|PRO (+ tier id)
  - planName
  - badge
  - expiresAt
  - quotas: List<Quota> (the P0 type), unifying 'Kuota', 'Kredit AI', 'Token AI' and 'prompt AI'
  - bonusCoins? (Koin AI, pending PO)
  - tambah_materi_baru_heymika_mobile
  - mika_ai_tutor_heymika_mobile
  - konfigurasi_kuis_baru_heymika_mobile
  - bank_kesalahan_heymika_mobile
  - pusat_notifikasi_heymika_mobile
  - profil_dan_pengaturan_heymika_mobile
  - system_states_error_handling_heymika_mobile
  - checkout_pembelian_paket_heymika_mobile
  - status_transaksi_invoice_heymika_mobile
- **Workspace + WorkspaceCategory (P3)**
  - id
  - title
  - description
  - category { key, label, toneKey } (WorkspaceCategory tone map added in P3)
  - courseCode
  - semester
  - tags: List<String>
  - status: ACTIVE|ARCHIVED
  - examTarget { name, date } (date set with DatePickerField; daysLeft derived)
  - counts { documents, flashcards, quizPacks }
  - progressPct
  - highlight { label, value, badge } (server/AI provided)
  - isBookmarked (derived from Bookmark type WORKSPACE)
  - aiVersion
  - updatedAt
  - memberPreview (open question)
  - materimu_heymika_workspace_mobile
  - detail_workspace_heymika_mobile
  - tambah_materi_baru_heymika_mobile
  - konfigurasi_kuis_baru_heymika_mobile
  - beranda_heymika_mobile
  - statistik_progres_belajar_heymika_mobile
- **Material (existing model, reshaped in P3 into a source document inside a Workspace)**
  - id
  - workspaceId
  - title (<= 80 chars)
  - sourceType: PDF|TXT|PASTE
  - language: ID|EN|JA
  - fileName
  - sizeBytes
  - pageCount
  - indexStatus: PROCESSING|INDEXED|READY|DRAFT|FAILED
  - readingProgress { pct, page, totalPages, lastSectionId? }
  - lastStudiedAt
  - latestJobId
  - isPendingSync (the 'Materi baru ditambahkan' banner)
  - tambah_materi_baru_heymika_mobile
  - detail_workspace_heymika_mobile
  - beranda_heymika_mobile
  - konfigurasi_kuis_baru_heymika_mobile
  - viewer_sumber_sitasi_ai_heymika_mobile
  - mika_ai_tutor_heymika_mobile
- **ProcessingJob + ProcessingStep + UploadSession (P3)**
  - id
  - code ('#JOB-2025-089X')
  - materialId
  - workspaceName
  - status: QUEUED|RUNNING|SUCCEEDED|FAILED|CANCELLED
  - overallProgress
  - etaSeconds
  - startedAt
  - totalDurationSec
  - soundNotifyEnabled
  - steps: List<ProcessingStep { key: UPLOAD|VALIDATE_OCR|EXTRACT|INDEX|GENERATE, status: DONE|RUNNING|PENDING|FAILED (JobStepStatus tone map), timestamp, description, subProgress? }>
  - failure { stepKey, reason, quotaRefunded }
  - outputs { summaryReady, flashcardCount, quizCount }
  - waitingTip { title, markup }
  - UploadSession { sessionId, uploadedBytes, totalBytes, chunkSize } (resumable upload, so the worker can resume after a stop)
  - status_pemrosesan_bahan_belajar_heymika_mobile
  - tambah_materi_baru_heymika_mobile
  - pusat_notifikasi_heymika_mobile
  - detail_workspace_heymika_mobile
- **MasteryBand (shared threshold enum, P3)**
  - MASTERED|PROFICIENT|NEEDS_PRACTICE|CRITICAL|INSUFFICIENT_DATA
  - bandFor(pct, sampleSize, minSample=5): one function shared by exam readiness (P3), subtopic mastery (P8), deck retention (P9) and the stats matrix (P10); cut-offs pending PO
  - tone map MASTERED=success, PROFICIENT=primary, NEEDS_PRACTICE=orange, CRITICAL=danger, INSUFFICIENT_DATA=outline (added P3)
  - detail_workspace_heymika_mobile
  - hasil_kuis_evaluasi_heymika_mobile
  - daftar_flashcard_heymika_mobile
  - statistik_progres_belajar_heymika_mobile
- **NoteSummary + NoteSection (P3 preview, P6 full; replaces GeneratedContent SUMMARY)**
  - id
  - materialId/workspaceId
  - version
  - savedState
  - aiModelLabel
  - examTag
  - title
  - estReadMinutes
  - wordCount
  - citationCount
  - validSourceCount
  - isBookmarked (Bookmark type NOTE)
  - updatedAt
  - sections, sealed: ExecutiveSummary{body, metrics} | KeyConcepts{items{title, tag, toneKey, body}} | Regulation{body, regulators{INHIBITOR|ACTIVATOR}} | MikaTip{headline, body, savedFormula} | ReactionTable{rows{stepNo, name, transition, delta, deltaType}} | VisualNote{tree{nodes, edges}, sequences} | ComplexityTable{columns, rows{cells{text, tone}}} | Glossary{terms{term, definition, hasAnalogy}} | Sources{items: SourceRef} | ExamReadiness{overallPct, topics{name, pct, band: MasteryBand}} | Text{markup} (fallback)
  - parsed by core/data/mapper/NoteSectionMapper (tolerant, unit-tested)
  - detail_workspace_heymika_mobile
  - catatan_ringkasan_ai_heymika_mobile
- **Bookmark (P3; feeds :favorite 'Koleksi Tersimpan')**
  - id
  - type: WORKSPACE (P3) | NOTE | FORMULA (P6) | CONVERSATION (P7) | CARD | QUESTION (P9)
  - targetId
  - title
  - subtitle
  - routeKey + args (resolved through the RouteSpec registry when tapped)
  - createdAt
  - syncState: PENDING|SYNCED
  - detail_workspace_heymika_mobile
  - catatan_ringkasan_ai_heymika_mobile
  - percakapan_ai_tutor_heymika_mobile
  - review_flashcard_heymika_mobile
- **TodayDashboard + StudyTask (P4)**
  - DailyRecommendation { message markup, taskCount, estimatedMinutes, srsTopics }
  - SrsSummary { dueConcepts, isPriority } (computed by the :core SRS scheduler from P9; the card is hidden before then)
  - StudyTask { id, type: QUIZ|FLASHCARD_REVIEW|READ|MISTAKE_REMEDIAL|QUICK_QUIZ|NEW_MATERIAL (tone map P4), title, description, courseLabel, durationMinutes, urgency, progress { done, total }, accuracyTarget?, memoryLevelPct?, intervalDays?, isCompleted, targetRef { materialId|sectionId|quizId|deckId|mistakeSetId } }
  - StudyTaskTargetResolver (core/domain): type + targetRef to RouteSpec, returning null when the spec is unshipped
  - RecentMaterial { materialId, subjectTag, timeLabel source, title, description, progressPct, status: IN_PROGRESS|DONE, hasQuiz }
  - beranda_heymika_mobile
  - rencana_belajar_heymika_mobile
- **ActiveTimeLog + DailyTarget (local tracker, P4)**
  - ActiveTimeLog { sessionId, start, end, activeSeconds, contextRoute (e.g. notes/{id}), syncState }, stored in Room
  - rules: counted only while the app is foregrounded (ProcessLifecycleOwner) AND a pointer or key interaction happened within the last 60s (idleThresholdSec=60); day boundaries in device local time
  - DailyTarget { doneMinutes (today's local ActiveTimeLog sum, max'd with the server value for multi-device), targetMinutes = StudyPreferences.dailyTargetMinutes, pct, consistentDays }
  - synced by ActiveTimeSyncWorker (core/data); feeds Beranda (P4) and Statistik (P10)
  - beranda_heymika_mobile
  - statistik_progres_belajar_heymika_mobile
  - rencana_belajar_heymika_mobile
- **User preferences (reshaped SettingPreferences, P4)**
  - StudyPreferences { dailyTargetMinutes 15|30|60, activeDays, interestTopics }
  - TutorPreferences { persona: CHEERFUL_SOCRATIC|CONCISE|ACADEMIC_DEEP, analogyLevel: SIMPLE|BALANCED|TECHNICAL, sapaan, responseLanguage }
  - AccessibilityPreferences { fontScale: SMALL|NORMAL|LARGE }
  - AppPreferences { lastTab } (the legacy darkMode key is kept in DataStore but unread; v1 is light-only)
  - DataExportRequest { format: JSON|ZIP, status }
  - profil_dan_pengaturan_heymika_mobile
  - onboarding_heymika_mobile
  - mika_ai_tutor_heymika_mobile
- **AppNotification + NotificationPreferences (prefs P4, inbox P5)**
  - NotificationPreferences { pushEnabled, dailyReminderEnabled, dailyReminderTime (ComicTimePicker), aiProgressReportEnabled, quietHoursEnabled, quietStart, quietEnd (TimeRangeField), dailyLimitOnce }: ONE source of truth in DataStore plus the server, edited from Profil (P4) and the Pusat Notifikasi tip card (P5)
  - AppNotification { id, category: MATERI_AI_JOB|PENGINGAT_SESI|KUOTA_AI|PEMBAYARAN_AKUN (tone map P5), variant: DEFAULT|ERROR|SUCCESS|ACHIEVEMENT, title, body markup, createdAt, isRead, snoozedUntil?, quotaUsage?, actions: List<{ label, style, routeKey, args }> }
  - NotificationFilter { readState: ALL|UNREAD|READ, category, sort: NEWEST|OLDEST } + counts
  - DateGroup TODAY|YESTERDAY|THIS_WEEK|OLDER
  - pusat_notifikasi_heymika_mobile
  - profil_dan_pengaturan_heymika_mobile
- **SourceDocument + DocumentPage + Citation (P6)**
  - SourceDocument { id, materialId, fileName, displayTitle, subject, indexProgressPct, pdfPageCount, printPageTotal, fileUrl }
  - DocumentPage { pdfPage, printPage, chapterLabel, heading, blocks: Paragraph|CitationBlock|Figure, imageUrl?, ocrText }
  - Citation { id, index, kind: NOTE|FLASHCARD|QUIZ (CitationKind tone map P6), label, quote, verificationTag, usedInLabel, targetType, targetId, pdfPage, printPage, paragraphNo }
  - CitationCounts { all, note, flashcard, quiz, onThisPage }
  - ViewerMode: PDF|OCR
  - viewer_sumber_sitasi_ai_heymika_mobile
  - catatan_ringkasan_ai_heymika_mobile
  - percakapan_ai_tutor_heymika_mobile
  - hasil_kuis_evaluasi_heymika_mobile
  - bank_kesalahan_heymika_mobile
- **TutorConversation + ChatMessage + MessageBlock (P7)**
  - Conversation { id, studyContext { materialId?, subject, chapterTitle, fileName, page, mode: SOURCE|FREE }, tutorMode: SOCRATIC|..., style: TutorStyle, isBookmarked, createdAt }
  - ChatMessage { id, role: MIKA|USER, senderLabel, createdAt, stepBadge?, status: SENDING|STREAMING|DONE|FAILED, blocks }
  - MessageBlock sealed: RichText | InlineSuggestion | AnalogyStep | ReactionSteps | Citation(ref) | CheckQuestion { question, options, selectedKey, correctKey, feedback } | NextStep
  - QuickPrompt { id, tag, title, promptText, intent: EXPLAIN|HINT|CHECK|PLAN }
  - SuggestedPrompt { label, promptText }
  - mika_ai_tutor_heymika_mobile
  - percakapan_ai_tutor_heymika_mobile
  - detail_workspace_heymika_mobile
  - beranda_heymika_mobile
- **Quiz + QuizGenerationConfig (P8)**
  - QuizSummary { id, title, workspaceId, subjectTag, difficulty: EASY|MEDIUM|HARD (one label set; tone map P8), questionCount, durationMinutes, status: NEW|READY|DRAFT_AI|COMPLETED|NEEDS_PRACTICE (QuizStatus tone map P8: COMPLETED=primaryContainer, NEEDS_PRACTICE=ribbonRed, DRAFT_AI=tertiaryFixedDim, NEW=secondaryFixedDim), bestScore, gradeLetter, attemptCount, weakTopic?, isMikaRecommendation, draftReadinessPct?, updatedAt }
  - QuizStats { total, avgScorePct, deltaPct, readyCount, highPriorityCount, weeklyTarget { done, target } }
  - QuizGenerationConfig { workspaceId, documentIds, keywords, questionCount: 5|10|20, difficulty, mode: PRACTICE|EXAM, topicId? (prefilled from Statistik) }
  - GenerationEstimate { credits, etaSeconds }
  - daftar_kuis_heymika_mobile
  - konfigurasi_kuis_baru_heymika_mobile
  - beranda_heymika_mobile
  - catatan_ringkasan_ai_heymika_mobile
  - statistik_progres_belajar_heymika_mobile
- **Question (replaces QuizQuestion) + Hint (P8)**
  - id
  - number
  - topicTag/subtopicId
  - difficulty
  - stem: markup
  - visual? { type: CODE|ASCII_TREE|IMAGE, title, ruleLabel, content }
  - options: List<{ key: A..D, markup }>
  - correctKey (served only after submit in EXAM mode)
  - explanation
  - citationRef?
  - Hint { index, text }, hintsRemaining
  - mengerjakan_kuis_heymika_mobile
  - hasil_kuis_evaluasi_heymika_mobile
  - percakapan_ai_tutor_heymika_mobile
- **QuizAttempt + AnswerSubmission (P8)**
  - attemptId
  - quizId
  - mode
  - sessionLabel
  - startedAt
  - deadline (server-authoritative)
  - totalQuestions
  - answers: Map<questionId, { optionKey?, flagged, timeSpentSec, answeredAt, syncState: PENDING|SYNCED }> (answers after the deadline are rejected locally and server-side)
  - hintsUsed
  - autosaveStatus
  - mengerjakan_kuis_heymika_mobile
- **AttemptResult + QuestionReview + SubtopicMastery (P8)**
  - AttemptResult { attemptId, quizId, quizTitle, attemptNumber, passingGrade, passed, score, maxScore, correct, wrong, empty, accuracyPct, independentPct, durationSec, limitSec, hintsUsed, finishedAt, praiseText, reward: XpReward, streakDays, mikaFeedback, recommendation { text, targetRef } }
  - SubtopicMastery { name, correct, total, pct, band: MasteryBand, insight }
  - QuestionReview { questionId, outcome: CORRECT|WRONG|UNANSWERED, usedHint, points, timeSpentSec, isFast, userOption, correctOption, concept?, citation?, mnemonic? }
  - ReviewFilter ALL|WRONG|UNANSWERED|CORRECT|HINT_USED with counts
  - hasil_kuis_evaluasi_heymika_mobile
  - statistik_progres_belajar_heymika_mobile
- **MistakeItem + MistakeDiagnostics (P9)**
  - MistakeItem { id, subjectTag, conceptTitle, questionStem, mistakeCount, quizCount, priority: HIGH|MEDIUM|LOW (tone map P9), reviewStatus: REVIEWED|UNREVIEWED, userAnswer, correctAnswer, misconception?, rootCause: NOT_UNDERSTOOD|FORGOT_RULE|MISREAD (tone map P9), rootCauseLabel, mikaNote?, formula?, sourceRef, sourceAttemptIds }
  - MistakeDiagnostics { total, unreviewed, reviewed, criticalTopics{name, count}, masteryPct, rootCauseDistribution }
  - RemedialRequest { source: PRIORITY|mistakeIds|subtopicIds|attemptId, count }
  - bank_kesalahan_heymika_mobile
  - hasil_kuis_evaluasi_heymika_mobile
  - rencana_belajar_heymika_mobile
- **FlashcardDeck + Flashcard + SrsState + ReviewSession (P9)**
  - FlashcardDeck { id, workspaceId, subjectTag, title, status: DUE|COMPLETED_TODAY|DRAFT, dueCount, cardCount, retentionPct, band: MasteryBand, avgIntervalDays, lastStudiedAt, nextDueAt }
  - Flashcard { id, deckId, tag, difficulty, questionType, front, hint?, back, meta { complexity, category }, isStarred (Bookmark type CARD), citationRef? }
  - SrsState { stability, difficulty, due, reps, lapses, lastReviewedAt } (FSRS or SM-2 pending; the scheduler is a pure :core domain use case)
  - Rating: AGAIN|HARD|GOOD|EASY (one enum, one label set, score 1..4; tone map P9)
  - ReviewLog { cardId, rating, reviewedAt, elapsedMs, syncState }, synced by ReviewLogSyncWorker in core/data
  - ReviewSession { id, deckId? (null = Sesi Kilat), cardIds, index, countsByRating, elapsedSec }
  - FlashcardOverview { deckCount, totalCards, dueCards, masteredCards }
  - daftar_flashcard_heymika_mobile
  - review_flashcard_heymika_mobile
  - beranda_heymika_mobile
  - statistik_progres_belajar_heymika_mobile
  - catatan_ringkasan_ai_heymika_mobile
- **StudyPlan + DayPlan + ExamTarget + WeeklyGoal (P10, cached in Room for offline)**
  - StudyPlan { id, dailyTargetMinutes, weeklyTargetMinutes, activeDays: Set<DayOfWeek>, autoSync, weekendBoostMinutes }
  - DayPlan { date, status: DONE|TODAY|PLANNED|BOOSTED|REST|LOCKED (DayPlanStatus tone map P10), minutesDone (from ActiveTimeLog), minutesTarget, unlockAt? (device local time) }
  - ExamTarget { name, date (DatePickerField), phase }
  - ScoreTarget { targetScore, readinessPct, weeklyDeltaPct }
  - MikaPlanAdvice { message, protectedWeakTopics, capacityWarning? }
  - WeeklyGoal { sessionsDone, sessionsTotal, remainingMinutes }
  - QuickSessionOffer { minutes, questionCount }
  - rencana_belajar_heymika_mobile
  - profil_dan_pengaturan_heymika_mobile
  - detail_workspace_heymika_mobile
- **ProgressStats (P10)**
  - StatsQuery { range: D7|D30|D90, workspaceId? }
  - StatsSummary { focusSessions, delta, avgPerDay, activeMinutes (ActiveTimeLog), idleThresholdSec=60, streakDays, firstAttemptAccuracy }
  - DailyActivity { date, minutes, sessions, quizAccuracy, mix { quiz, notes, flashcard } }
  - PriorityTopic { rank, topicId, title, accuracy, insight, actions }
  - TopicMastery { topicId, title, band: MasteryBand, attempted, correct, accuracy?, minRequired=5 }
  - RecallDistribution { activeCards, buckets per Rating } (from ReviewLog)
  - statistik_progres_belajar_heymika_mobile
  - beranda_heymika_mobile
- **Purchase: SubscriptionTier, PlanPeriodOption, PaymentMethod, Voucher, PriceQuote, Order, Transaction, Invoice (P11)**
  - SubscriptionTier { id, displayName, benefits }
  - PlanPeriodOption { tierId, months, price, strikePrice?, perMonthLabel, savingsPct?, bonusCoins?, isRecommended }
  - PaymentMethod { id: QRIS|VA|EWALLET|CARD, title, subtitle, badge, channels } (Midtrans only)
  - Voucher { code, status: NONE|VALIDATING|APPLIED|INVALID, discount, message }
  - PriceQuote { base, discount, taxRate, tax, gatewayFee, total }, computed server-side
  - Order { orderId, idempotencyKey, gateway: PLAY_BILLING|MIDTRANS, snapToken|redirectUrl (finish URL = https://${appLinkHost}/payment/status/{orderId}) | productId/offerToken (Play), expiresAt }
  - Transaction { orderId, status: PAID|PENDING|FAILED|EXPIRED|REFUNDED (tone map P11), amount, methodLabel, gatewayRef, createdAt, paidAt?, refundAmount? }, always fetched from the server, never read from a URL
  - PlayPurchase { purchaseToken, productId, acknowledged }, verified server-side before entitlement
  - Invoice { lines, planName, validUntil, quotas, pdfUrl }
  - checkout_pembelian_paket_heymika_mobile
  - status_transaksi_invoice_heymika_mobile
  - profil_dan_pengaturan_heymika_mobile
  - pusat_notifikasi_heymika_mobile

## 7. Nasib kode yang ada

- **core/ui/theme Color.kt, Theme.kt, Type.kt (Indigo/Teal/Amber ramps, FontFamily.Default, AiLearnTheme with a dark scheme)** — Replace in P0 with HeyMikaTheme built from the full DESIGN.md token set and bundled Nunito Sans. v1 is light-only, decided in P0: the darkTheme parameter and the dark scheme are removed.
- **core/ui/component EmptyState, ErrorState, LoadingState** — Replace in P0 with MikaEmptyState, MikaErrorState (AppError-driven, full-screen and inline) and SkeletonBlock. Call sites are updated in the same phase.
- **core/ui/component MaterialCard** — Restyle in P0 so the legacy screens compile. Delete in P3, together with every user of it: HomeScreen, MaterialDetailScreen and the old FavoriteScreen, which is reshaped in the same phase.
- **Domain model Material(title, subject, description, body, coverUrl, durationMinutes, isFavorite) + MaterialEntity, MaterialDao, MaterialRepository/IMaterialRepository, MaterialUseCase/MaterialInteractor and the DataMapper material functions** — Reshape in P3 into a source document that belongs to the new Workspace (workspaceId, sourceType, language, fileName, size, pages, indexStatus, readingProgress). body moves into NoteSummary sections, subject becomes WorkspaceCategory, and isFavorite is replaced by the Bookmark entity, also defined in P3. getFavoriteMaterials/setFavorite are removed in P3, in the same commit that moves FavoriteViewModel onto BookmarkUseCase. Room Migration(1,2) drops the old table.
- **Domain model GeneratedContent + GenerationKind(QUIZ, SUMMARY, STORY) + GeneratedContentEntity/GeneratedContentDao** — Delete in P3, when their only consumer (AiStudioViewModel) is deleted. Migration(1,2) drops the generated_content table. SUMMARY's successor, NoteSummary/NoteSection, lands in P3 (preview) and P6 (full); QUIZ's successor lands in P8. STORY has no mockup and has no successor.
- **Domain model QuizQuestion(prompt, options, answerIndex, explanation)** — Delete in P3. Question, with keyed options, a rich stem, a visual, topic, difficulty, citation and Hint, is introduced in P8.
- **core/utils/QuizParser (encoder) and aistudio/quiz/QuizParser (tolerant reader)** — Delete both in P3, together with QuizSection, so no unused code waits for P8. The tolerant-reader cases (bare array, wrapper object, prose fallback) are saved as JSON fixtures under core/src/test/resources/legacy-quiz/ and replayed against the P8 QuestionDto mapper. Schema tolerance moves to the server contract.
- **AiRepository/IAiRepository and AiUseCase/AiInteractor** — Delete in P3. Their only consumer, AiStudioViewModel, is deleted in the same phase, and the useCaseModule and repositoryModule bindings go with them. Task-specific AI use cases replace them: NoteUseCase (P6), TutorUseCase (P7), QuizUseCase (P8).
- **GenerationRequest / GenerationResponse / QuizQuestionResponse (remote DTOs)** — Delete in P3, together with the ai/generate endpoint.
- **ListMaterialResponse / MaterialResponse (remote DTOs)** — Reshape in P3 into WorkspaceListResponse, WorkspaceResponse and the new MaterialResponse (workspaceId, sourceType, pages, indexStatus and so on), mapped in core/data/mapper.
- **ApiService (GET materials, GET materials/{id}, POST ai/generate proxy)** — Split into per-area Retrofit interfaces: AuthApi (P1), WorkspaceApi/MaterialApi/JobApi/UserApi (P3), DashboardApi (P4), NotificationApi (P5), NoteApi/SourceApi (P6), TutorApi (P7), QuizApi (P8), FlashcardApi/MistakeApi (P9), PlanApi/InsightApi (P10), PaymentApi (P11). ApiService itself is deleted in P3 with its legacy endpoints, not left floating until the end. The design where our backend proxies Anthropic (no model key in the APK) stays.
- **NetworkBoundResource, Resource, ApiResponse, RemoteDataSource, DataMapper, Koin module split** — Keep: this is the clean architecture that matches the capstone. Resource.Error gains an AppError (P0), and RemoteDataSource gains the ErrorMapper. DataMapper is split into per-feature mapper objects in core/data/mapper as entities grow (outside ui/, so JaCoCo counts them).
- **NetworkModule (OkHttp, logging BODY in debug only)** — Keep the debug-only logging. Add AuthInterceptor and the refresh Authenticator (P1), the network_security_config reference (P1) and the CertificatePinner (P12).
- **SettingPreferences (DataStore ailearn_settings: darkMode, dailyReminder) + ISettingRepository/SettingRepository/SettingUseCase/SettingInteractor** — P0 stops reading darkMode (light-only) and removes its UI row. P1 renames the file to heymika_settings. P4 reshapes it into UserPreferences (study, tutor, notification, accessibility and app preferences); dailyReminder becomes NotificationPreferences.dailyReminderEnabled plus an editable time. The darkMode key is kept unread in case a dark palette is approved after v1.
- **AiLearnDatabase (plaintext AiLearn.db, version 1, exportSchema=false) + DatabaseModule (fallbackToDestructiveMigration)** — Replace in P1 with SQLCipher-encrypted HeyMikaDatabase (HeyMika.db) v1 with the same entities. AiLearn.db is deleted. exportSchema=true with the KSP arg room.schemaLocation, and the schemas are committed. fallbackToDestructiveMigration is removed. Every later bump (P3 1 to 2, P4 2 to 3, and so on) ships an explicit Migration or AutoMigration with a MigrationTestHelper test. P3's migration drops only server-cache tables.
- **core/navigation/FeatureEntry (route + register(NavGraphBuilder, NavHostController))** — Reshape in P1 to { moduleName; @Composable Content(spec, backStackEntry, navigator) }, with the route, argument, tab and deep-link declarations moved into the :core HeyMikaRoutes RouteSpec registry. This lets :app declare every destination, on-demand ones included, in one stable graph, so installing a split never calls setGraph with a different graph.
- **app/navigation DynamicFeature, DynamicFeatureInstaller, FeatureInstallState** — Keep the state machine. P1 injects SplitInstallManager behind a FeatureInstaller interface (so tests can use FakeSplitInstallManager or a fake) and deletes installedEntries() as a graph input; entryFor() is called lazily by FeatureDestinationHost. The enum gains QUIZ (P8), FLASHCARD (P9), PLANNER and INSIGHT (P10) and PAYMENT (P11), each with a keep rule and an EntryContractTest.
- **app/navigation FeatureInstallDialog** — Replace in P1 with FeatureInstallPanel, rendered in place by FeatureDestinationHost. The state-to-copy logic and the Play confirmation flow are reused. It gets the comic styling when the first real on-demand split ships (P10).
- **app/navigation AiLearnNavHost + Route + BottomTab(HOME, FAVORITE, SETTING)** — Reshape in P1 into HeyMikaNavHost, built once from the RouteSpec registry (startup, auth, onboarding and main graphs), plus DeepLinkRouter. The comment claiming that a graph rebuild preserves the back stack is removed along with the rebuild. Route constants move to :core HeyMikaRoutes. BottomTab is deleted in P3 and replaced by ShellTab.
- **app/home HomeScreen + HomeViewModel** — Kept as the placeholder main/home during P1-P2, then deleted in P3 because it depends on the legacy Material list. Materimu is the start tab in P3, and BerandaScreen replaces Home as the start tab in P4.
- **app/detail MaterialDetailScreen + MaterialDetailViewModel** — Replace in P3 with DetailWorkspaceScreen. The generate-to-AI-studio action is removed in the same phase in which AiStudioScreen is deleted.
- **MainActivity + AiLearnApp Application class** — Keep the single-activity design and SplitCompat. Rename both to HeyMika* in P0 and drop the darkMode collection. Add the SplashScreen API (P1), workManagerFactory() in startKoin (P3) and the release-only root check (P12).
- **:aistudio module (AiStudioEntry, AiStudioScreen, AiStudioViewModel, AiStudioUiState, ProseSection, QuizSection, HistorySection, QuizParser)** — P1: switch to install-time and port AiStudioEntry to the Content contract. P3: delete AiStudioScreen, AiStudioViewModel, AiStudioUiState, QuizSection, HistorySection and QuizParser; ProseSection's logic moves to core/ui/summary. The entry ships no specs during P3-P5. It ships notes and the viewer in P6 and the tutor in P7.
- **:favorite module (FavoriteEntry, FavoriteScreen, FavoriteViewModel, FavoriteUiState)** — P1: switch to install-time and port the entry. P3: reshape into 'Koleksi Tersimpan' over BookmarkUseCase in the same phase that removes Material.isFavorite and MaterialCard, so CI never breaks. P6, P7 and P9 add bookmark types. P12 applies the PO's keep or delete decision.
- **:setting module (SettingEntry, SettingScreen, SettingViewModel, SettingUiState)** — P0: remove the dark-mode row. P1: switch to install-time and port the entry. P3: delete SettingScreen and SettingViewModel together with the old tab bar. P4: ship Profil & Pengaturan Akun (ProfileSettingsViewModel) under the same module and entry.
- **Build tooling: ktlint plugin, LeakCanary, R8 config with FeatureEntry keep rules, BuildConfig from local.properties** — Keep. Rename the API property to heymika.api.base.url and add heymika.applink.host for the App Link manifestPlaceholder (P1). Add keep rules for each new FeatureEntry when its module is created.
- **Declared but unused test dependencies (mockk, turbine, coroutines-test, core-testing, junit)** — Start using them in P0 with MainDispatcherRule. Every phase lands interactor, mapper, ViewModel and migration tests as part of its Definition of Done.

## 8. Structure fixes (per phase)

- P0: Run ktlintFormat once, then enforce ktlintCheck on the main sources (pin the ktlint engine version if needed for Kotlin 2.2)
- P0: Add .github/workflows/ci.yml with lint, test (plus JaCoCo artifact) and build (assembleRelease and bundleRelease) jobs; each job writes local.properties from a secret
- P0: Add a JaCoCo report task that points at the AGP 9 built_in_kotlinc class directory (not build/tmp/kotlin-classes) and excludes R, BuildConfig, *_Impl*, di/**, ComposableSingletons* and ui/**. Keep every parser and mapper outside ui/ so coverage counts them
- P0: Start real unit tests (MainDispatcherRule, mockk, turbine); per-phase interactor, mapper and ViewModel tests are mandatory in the Definition of Done
- P0: Rename the leftover AiLearn* classes (Application, root composable, NavHost, theme) to HeyMika*
- P0: Use the declared ACCESS_NETWORK_STATE through ConnectivityObserver, with the OfflineBanner mounted in HeyMikaRoot
- P0: Debug-only DesignGalleryActivity in app/src/debug, plus the DebugHooks pair across the debug and release source sets, so release builds compile with no gallery classes (CI apkanalyzer check)
- P1: SQLCipher DB encryption with a SecureRandom passphrase wrapped by a Keystore AES/GCM key (do not port the reference SecureKeyManager's getEncoded() bug); rename the DB to HeyMika.db and delete AiLearn.db
- P1: Room exportSchema=true with the KSP arg room.schemaLocation, committed schema JSON, fallbackToDestructiveMigration removed, and the MigrationTestHelper harness
- P1: Rename the DataStore (heymika_settings) and the local.properties key (heymika.api.base.url); exclude the wrapped key and the session store from backup_rules.xml and data_extraction_rules.xml
- P1: Store the session with DataStore + Tink AEAD (EncryptedSharedPreferences is deprecated); add the OkHttp AuthInterceptor and the refresh Authenticator
- P1: Add network_security_config.xml with base-config cleartextTrafficPermitted=false and reference it from the manifest
- P1: Switch :aistudio, :favorite and :setting to install-time delivery. New modules are created only in the phase that ships their first screen
- P1: Add an EntryContractTest in each feature module that loads DynamicFeature.entryClassName by reflection and checks its RouteSpecs, so renames fail CI and not production
- P1: Move route contracts into a :core HeyMikaRoutes RouteSpec registry, build the NavGraph once (no rebuild on install), and add GraphStabilityTest
- P1: App Link host as a manifestPlaceholder (heymika.applink.host, mock default heymika.test), autoVerify intent filters, an assetlinks.json template, and no exported custom-scheme filters
- P1: Optional layering clean-up: move Resource from core.data to core.domain so domain interfaces stop importing the data layer
- P1: Add a Compose androidTest smoke test for startup and auth routing (androidTest sources are currently absent)
- P3: Put every WorkManager worker in core/data/worker with a KoinWorkerFactory (koin-androidx-workmanager) and remove the default WorkManagerInitializer; feature Koin modules hold only ViewModels and UI mappers
- P3: Foreground-service setup for expedited upload work: getForegroundInfo(), FOREGROUND_SERVICE and FOREGROUND_SERVICE_DATA_SYNC permissions, a SystemForegroundService override with foregroundServiceType=dataSync, an OutOfQuotaPolicy fallback, and resumable chunks for the Android 15 timeout
- P10: Verify the on-demand flow with bundletool build-apks --local-testing / install-apks, add OnDemandInstallBackStackTest, and document both for reviewers
- P12: Certificate pinning (OkHttp CertificatePinner with leaf + backup pins, a network_security_config pin-set with an expiration date) once the real backend host replaces https://api.example.com/, verified with the user-CA pinningCheck build
- P12: RootBeer root detection in release builds, with a ComicDialog and finish() in MainActivity (confirm it resolves without Jetifier)
- P12: OWASP dependency-check plugin and a CI security job with an NVD API key
- P12: CI coverage threshold gate and a final mapping.txt check of all FeatureEntry classes in the release AAB

## 9. Risiko

- No backend contract exists yet: the base URL is still https://api.example.com/, and every phase from P1 needs endpoints (auth, workspaces, chunked uploads, jobs, structured AI output, quizzes, SRS sync, payments). Mitigation: agree an OpenAPI contract per phase before it starts, and build against the P1 FakeRemoteDataSource. Residual risk: the fixtures and the real API drift apart late.
- App Links depend on the backend domain. The https verify, reset and payment links only open the app on Android 12+ once assetlinks.json is served on ${appLinkHost} with the debug, release and Play app-signing SHA-256 fingerprints. Until then, only explicit-package adb intents work, and real email links open the browser. The same URLs need a web fallback page.
- Google Play Payments policy: selling digital subscriptions in a Play-distributed app generally requires Play Billing unless the app is enrolled in an alternative or user-choice billing program, so the Midtrans/QRIS/VA checkout as mocked may be rejected. This decision gates P11, and Play Billing removes the redirect flow entirely.
- Navigation contract cost: because on-demand destinations must exist in the graph before their split does, their RouteSpecs live in :core and the feature only supplies Content. Argument drift between a spec and its feature is not compile-checked across the reflection boundary. Mitigations: an EntryContractTest per module renders every owned spec with sample args, and GraphStabilityTest and OnDemandInstallBackStackTest protect the no-rebuild property.
- Background work limits: expedited uploads run as a dataSync foreground service on API 30, Android 15 caps dataSync time, expedited quotas can be exhausted, and Doze delays polling. Mitigations: resumable chunked uploads, OutOfQuotaPolicy fallback, FCM for completion. Denied POST_NOTIFICATIONS silently breaks the 'Mika akan ping' promise, so the UI must say status is in-app only.
- Design fidelity: almost no mockup applies DESIGN.md (3dp navy + orange offset, 56dp buttons, bordered bubbles with tails). Without the P0 decision locked, every screen reopens the 'match the PNG vs match DESIGN.md' debate across 29 screens.
- Structured AI output: notes, chat blocks, diagrams, quizzes and mastery maps all assume schema-valid typed JSON from the LLM backend. Malformed or partial output will be common. Tolerant mappers with plain-text fallbacks (NoteSectionMapper, MessageBlock, QuestionDto) and server-side validation help, but some screens will look degraded until the backend is reliable.
- Active-time tracking: the 60s idle rule is heuristic (passive reading or video can count as idle), multi-device minutes can double count without a server merge rule, and recording interaction timestamps needs a privacy note. Beranda's target and Statistik both depend on it from P4 and P10.
- Module count and reflection: 8 dynamic modules loaded by class name. A renamed class or a missing keep rule is a silent runtime break, which the EntryContractTests and the mapping.txt check mitigate. On-demand :insight and :payment fail on sideloaded APKs unless reviewers use bundletool --local-testing (documented in P10).
- Base APK size and startup: auth, shell, notifications, the workspace pipeline (PdfRenderer, WorkManager, uploads) and Nunito Sans (about 0.5-1 MB) all live in base. Watch cold start (SplashScreen + session resolve + SQLCipher open) and consider a Baseline Profile.
- SQLCipher plus the Keystore can fail on some OEM devices (key invalidation after a lock-screen change, StrongBox quirks). There must be a recovery path that wipes and re-syncs the cache. From P8 onward the DB also holds unsynced answers and review logs, so that recovery must sync or warn first.
- Dependency compatibility: adding material3-adaptive, work-runtime 2.10, koin-androidx-workmanager, Tink, credentials and SQLCipher against Compose BOM 2025.01, AGP 9.2.1 and Kotlin 2.2.10 must be verified per phase. Bumping the BOM to get a native dropShadow API risks wider breakage, so the custom drawBehind shadows stay and must not allocate per frame in long lists.
- The mockups contain conflicting data: level 4/12/14; quota 5/7/15 vs 45/50 vs 1.850 tokens vs unlimited; plan names and prices; score 75 vs 70; FSRS vs SM-2; link validity 30 min vs 24 h. The domain is server-driven, but product decisions are needed before the affected phases.
- Scope creep from UI that is required but not mocked: onboarding steps 2-3, create workspace and pickers, paste-text mode, the Kisi Soal grid, submit and exit dialogs, the session-complete summary, the plan editor, the change-email dialog, Riwayat Obrolan, Koleksi Tersimpan. Each is small, but together they are about 15-20 derived surfaces that need PO sign-off.
- Mascot and brand assets are missing or wrong in the mockups (stock photos, an off-model cat, a screenshot inside the error circle, broken images). Placeholder vectors keep work moving, but the app is not visually done without about 15 exported Mika poses and a vector logo.
- Contrast and overflow: white on #0075E8 is about 4.46:1, just under WCAG AA for normal text. Buttons rely on 16sp bold labels and links use #005AB5. Long Indonesian strings already overflow pills in the PNGs, and the Profil font-size setting makes it worse, hence single-line pills and the P12 font-scale pass.
- Quiz timing and integrity: a client-only timer can be manipulated. The plan uses a server deadline. The offline answer queue must reject answers after the deadline, and Mode Ujian must not leak correctKey before submit.
- Interim builds: from P1 to P3 a release build has no logout (Profil arrives in P4), and from P3 the start tab changes from Materimu to Beranda. No external release is cut before P4, and the first public release follows P12.
- Capstone grading: the reference rubric expects specific artifacts (a favorite feature in a dynamic module, tests, CI, encryption, pinning, root detection). Koleksi Tersimpan covers favorites from P3, but pinning and root detection still land only in P12 and must not slip.

## 10. Pertanyaan terbuka untuk product owner

1. Backend and auth: is there a real HeyMika backend (custom REST via Retrofit), or should auth use Firebase Auth or Supabase? Which domain hosts the App Links (verify-email, reset-password, payment/status) and will serve assetlinks.json? Until we know, every phase runs on the mock backend with the placeholder host heymika.test.
2. Design fidelity: should the app follow DESIGN.md strictly (3dp navy borders, hard orange shadows, 56dp buttons, speech bubbles with tails), or the softer look the PNGs actually show? Is the primary color #0075E8 (DESIGN.md prose) or #005AB5 (token table)? The proposal is DESIGN.md with #0075E8.
3. Bottom nav, slot 4. The mockups split 5 vs 5. A Kuis tab appears in Beranda, Profil, Pusat Notifikasi, Rencana and Statistik. A Flashcard tab appears in Materimu, Tanya Mika, Daftar Kuis, Daftar Flashcard and Bank Kesalahan, and Daftar Kuis itself shows 'Beranda / Materiku / Flashcards / Tanya Mika' with no Kuis tab. A Rencana tab appears only in Rencana, and 'Sesi Belajar' only in Tambah Materi. Tanya Mika is in 10 of the 11 navs. The proposal is Beranda, Materimu, Tanya Mika (center), Kuis, Profil. Kuis (:quiz), Flashcard (:flashcard) and Rencana (:planner) are all install-time, so any of them can take slot 4 with no delivery change. Only making Statistik a tab would force :insight to install-time. A 'Latihan' tab with a Kuis | Flashcard switch is another option. Which do you want?
4. Tab labels: should the tab be 'Materimu' (2 navs), 'Materi' (4) or 'Materiku' (5)? Should it be 'Profil' (4) or 'Akun' (3)?
5. Onboarding: only step 1 'Profil Diri' has a mockup. Can we design step 2 'Target & Topik' and step 3 ourselves from existing components, or will mockups come? What should back on step 1 do: sign out, or be hidden?
6. Auth rules: is email verification by link or by OTP code? Does a reset link last 30 minutes or 24 hours? After a successful reset, should the user be logged in automatically or sent to Login? Does Google signup skip email verification? Which password policy is canonical (the 3 rules from register or the 4 from reset)?
7. AI tutor and summaries: will the backend stream tutor answers (SSE or WebSocket) and return structured blocks (steps, citations, inline quiz), or one-shot markdown? Can summaries come back as typed JSON sections, and how are diagrams such as the binary tree delivered?
8. Processing and push: can we use Firebase (FCM) for push, or should the app poll? Which upload formats are supported: PDF and TXT only, or also DOCX/PPTX as the Detail sync banner suggests? The picker currently offers only PDF and TXT. Does the backend support resumable chunked uploads?
9. Payment: Google Play Billing or Midtrans (Play policy)? What is the real plan catalog, with prices and benefits (Mika Champion Pro vs Genius AI Elite vs HeyMika Pro Semesteran)? Is 'Koin AI' real? Do you need a paywall at launch? If so, the payment phase moves earlier.
10. Quota model: what are the real quota rules? Can 'Kuota', 'Kredit AI' and 'Token AI' become one name? Are quotas daily or monthly? Is tutor chat unlimited on Pro?
11. Learning rules: FSRS or SM-2 for flashcards? Are hints allowed in Mode Ujian? How is the quiz score calculated (70 vs 75)? Are the difficulty labels Mudah/Sedang/Sulit? Do remedial sets cost credits? Does the AI or the user choose a mistake's root cause? What are the MasteryBand cut-offs? The plan needs them from P3, for Detail's exam readiness.
12. Study-time tracking: is client-side idle detection (60s with no interaction, paused in the background) acceptable, with a privacy note? Should minutes from several devices be added together on the server?
13. Gamification (XP, level, streak, badges, Mika level): does the backend compute it, or should it be dropped or simplified for v1?
14. Mascot and logo assets: can we get exported Mika poses (waving, waving with pencil, envelope, happy, worried, thinking, six error poses, sticker avatar) and the HeyMika logo as vector or WebP?
15. Dark mode: v1 is light-only (decided in P0; the toggle is hidden, the stored key is kept). Do you want a dark palette derived after v1, or should we drop dark mode for good?
16. Capstone rubric: does it require a Favorite feature in a dynamic module? Koleksi Tersimpan (bookmarks) lives in :favorite from P3, without a mockup. Keep it, or delete it in P12?
17. Tablet and foldable scope: the plan builds the DESIGN.md tablet and desktop rules (split shells, 8dp and 14dp shadows, larger type) and two-pane list-detail for four screen pairs. Is that wanted for v1, or should tablets just get the stretched phone layout?
18. MVP scope: which nice-to-haves are in v1? Voice input / Voice Tutor, image attach, the Σ formula palette, TTS and Zen mode on flashcards, note editing ('Mode Sunting'), data export, collaborator avatars on workspaces.
19. Profil: one long scroll with a single 'Simpan' bar, or sub-screens? Notification preferences will be one shared source, edited from both Profil and the Pusat Notifikasi card. Is that the right placement?
20. Links: what are the Help/Support destination (mailto:support@heymika.id, WhatsApp or a help page) and the Ketentuan Layanan / Kebijakan Privasi URLs?

## 11. Critic notes (sudah direvisi ke plan di atas)

**misplaced:**
- {"id": "rencana_belajar_heymika_mobile", "issue": "It is placed in the on-demand :insight module, but the plan routes Beranda's hero CTA 'Mulai Sesi Belajar Sekarang' to it (the main action on the start tab, per beranda code.html). So the most prominent home action needs a split download: it fails offline and on sideloaded or reviewer installs. That contradicts the plan's own reason for making :flashcard install-time ('an on-demand split cannot be downloaded while offline'). The Rencana mockup's nav also makes Rencana a tab root (Beranda / Materi / Rencana / Kuis / Profil). Fix: point the hero CTA at the first StudyTask, which lands in install-time :quiz, :flashcard or :app, and keep only Statistik on-demand. Otherwise make :insight install-time. Either way, the open question about the bottom nav must note that picking the Rencana nav forces :insight to install-time."}

**ordering_problems:**
- Beranda (Phase 3) comes before workspace/material (Phase 4), which breaks the 'workspace/material before consumers' rule. In P3, Beranda's recent-material cards have no Detail to open, the new-user empty state 'pushes Tambah Materi' (P4), and the upload banner has no target, so the P3 home is mock data with no working actions. Fix: move the material pipeline before Beranda (shell + Notifikasi + Profil in P3, Beranda after P4), or split Beranda into a P3 shell version and a P4+ version.
- The Beranda 'Target Harian 35/45 Menit' and 'Fokus Hari Ini' cards (P3) have no data source until the active-time tracker ships in P9 ('it also feeds the Beranda DailyTarget'). Likewise, the :flashcard rationale says Beranda reads SRS due counts from the :core SRS scheduler, but that scheduler only lands in P8. Either schedule the tracker and scheduler with P3, or state that P3 shows only server-provided values and say where the server gets minutes before P9.
- Phase 4 breaks :favorite, and nothing repairs it until P11. FavoriteScreen imports core.ui.component.MaterialCard (deleted in P4), and FavoriteViewModel calls MaterialUseCase.getFavoriteMaterials()/setFavorite on Material.isFavorite (removed by the P4 reshape). That violates the 'CI is green' Definition of Done for P4–P10. Add an interim P4 step: stub FavoriteScreen to MikaEmptyState, or move the Koleksi Tersimpan reshape to P5, where Bookmark exists.
- Bookmark is introduced in P5 ('a Bookmark entity (note, formula)'), but P4 already maps Material.isFavorite to Bookmark and gives Workspace an isBookmarked field. Define Bookmark in P4.
- Phase 0 artifacts reference domain types that come later. AppError.QuotaExceeded(quota: Quota) is built in P0, but Quota/AiQuota only appears in P3. The P0 tone maps key on MasteryBand, Rating, QuizStatus and Citation kind, which are defined in P5/P7/P8. Either define those enums (and a minimal Quota) in :core/domain in P0, or add each tone map in the phase that introduces its enum.
- The Room schema policy contradicts itself. existing_code_fate and Phase 1 say HeyMika.db has 'exportSchema=true, real migrations from then on', but Phase 4 says 'Bump the Room schema (fresh encrypted DB) and turn on exportSchema'. Decide whether P4 is a migration or destructive, and enable exportSchema (with the KSP room.schemaLocation arg) in P1.
- P4 removes Detail's generate-to-AI-studio action, but AiStudioScreen is only deleted in P5. That leaves :aistudio unreachable dead code for a phase, and QuizSection/QuizParser sit unused until P7. Delete or move them in the same phase.

**design_system_gaps:**
- No shared Accordion/ExpandableSection component, although several screens need one: the checkout benefits accordion (expand_more/expand_less in checkout code.html), the review_flashcard 'Mika hint accordion', the statistik expanders (expand_more), the bank_kesalahan expandable mistake cards, and the 'Detail teknis' expander that Phase 10 adds to status_transaksi. Add it to Phase 0 with an animated chevron, Role/semantics expanded state, and reduced-motion support.
- No themed time or date picker. Profil shows an editable 'Pengingat Sesi Belajar Harian … 19:00 WIB' and 'Jam Tenang 22:00 - 07:00', and Pusat Notifikasi has the same quiet-hours card. NotificationPreferences makes dailyReminderTime, quietStart and quietEnd editable. ExamTarget.date is entered in the create-workspace sheet and the Rencana plan editor. Unstyled M3 TimePicker/DatePicker would break the comic look, so add a ComicTimePicker / TimeRangeField / DatePickerField to Phase 0.
- Speed lines are missing. DESIGN.md names 'energetic speed lines' and 'speedline flourishes positioned strictly in peripheral zones' as a hallmark next to halftone, but the plan covers only HalftoneBackground and sparkles.
- DESIGN.md 'Comic Cards & Panels' says 'Interior panel dividers utilize matching 3px navy vertical/horizontal strokes'. The plan defines only 1.5dp borderMuted interior dividers. Add a ComicDivider (3dp ink) token and component, and state which surfaces use which divider.
- The responsive rules from DESIGN.md are not carried over. The tablet rules (50/50 split panel, 32px internal padding, 8px offset) and the desktop type sizes (display-hero 48/56, headline-lg 36/44) are missing. Only the shadow scales at >=600dp. There is no WindowSizeClass policy for auth split shells and two-pane layouts on tablets, foldables or landscape, and no larger type ramp.
- Focus indication is specified only for text fields. DESIGN.md gives primary 'active focus rings' for interactive controls, so ComicButton, ComicCard(onClick), SelectableOptionCard, chips and tabs need a keyboard/D-pad focus ring (Modifier.onFocusChanged plus the 2dp + 3dp ring tokens).
- The token list is incomplete against the YAML. tertiaryFixedDim #7fd0ff is used by the QuizStatus tone map (DRAFT_AI) but never declared. onPrimaryFixedVariant #00458e (the active font-size segment shadow in profil code.html), inversePrimary #aac7ff, primaryFixedDim, surfaceDim #c8dbff, surfaceBright and surfaceTint #005cba are not listed. With primary overridden to #0075E8, the plan should also say whether surfaceTint follows the override.

**other_issues:**
- HIGH, on-demand install resets the back stack. The plan keeps the existing pattern of rebuilding the NavHost graph when a split installs (AiLearnNavHost: 'a new graph is produced whenever a module finishes installing') and says 'the installer logic is already correct'. In navigation-compose 2.8.8, NavController.setGraph() with a non-equal graph (new destinations means not equal) clears every saved back stack and pops to the start destination. With the new design, the first install of :insight or :payment sends the user back through StartupGate and wipes all saveState tab stacks. That loses an open quiz result ('Riwayat' → Statistik) or tutor conversation (QuotaExceeded → Upgrade → checkout). Fix: keep the graph stable by declaring the on-demand routes up front as placeholder/loader destinations that render the entry's content once it is installed, so setGraph never sees a structural change. The FeatureEntry contract (one `route` plus register()) probably needs extending for this, and for registering tab roots into the nested 'main' graph and deep-link patterns. Add an androidTest: install :insight from Profil and assert the back stack and tab state survive.
- WorkManager workers and Koin in dynamic features. Phase 7 (:quiz offline answer sync) and Phase 8 (:flashcard review-log sync) add WorkManager jobs without saying which module owns them. Feature Koin modules are loaded only when the FeatureEntry is first instantiated by reflection. A process started in the background by WorkManager never builds the NavHost, so a worker that depends on :quiz or :flashcard Koin definitions fails to resolve. The plan's own :app rationale also calls workers inside a split 'fragile'. State that every worker lives in :core/data (or :app), or load the install-time feature Koin modules in Application.onCreate.
- Phase 4 upload worker. An 'expedited multipart-upload WorkManager worker with progress' on minSdk 30 runs as a foreground service on API 30. It must implement getForegroundInfo(), or WorkManager throws for expedited work. With targetSdk 36 it also needs FOREGROUND_SERVICE + FOREGROUND_SERVICE_DATA_SYNC and a manifest override of androidx.work.impl.foreground.SystemForegroundService with foregroundServiceType="dataSync". On Android 15, the dataSync time limit and the expedited-quota fallback need handling too. None of this is in the plan.
- The Phase 1/2 deep-link deliverables cannot be checked as written. `adb shell am start -d 'https://<domain>/verify-email?token=ok'` (and the reset-password one) only reaches the app if the domain is verified with assetlinks.json, and the domain is still an open question. On Android 12+, an unverified https link opens the browser. Add a manifestPlaceholder for the host, and either give the verification command (`adb shell pm set-app-links --package com.ai.heymika 2 all` / `pm verify-app-links`) or target the package explicitly (`am start -a android.intent.action.VIEW -d <url> com.ai.heymika`). Also say what the mock backend uses as the domain.
- The Phase 11 pinning deliverable proves nothing. 'A MITM proxy with a user-installed CA cannot read API traffic' passes even with no pinning, because with targetSdk 36 user CAs are untrusted by default. Testing pinning needs a CA the app actually trusts: a debug build whose debug-overrides trust user CAs, with the OkHttp CertificatePinner still failing, or a system-store CA. The second option conflicts with the same phase's 'refuses to run on a rooted emulator'. Rewrite the check, for example: a debug build trusting the proxy CA logs a CertificatePinner failure.
- The Phase 4 deliverable 'rejects … a .docx with inline errors' contradicts the specified picker. ACTION_OPEN_DOCUMENT filtered to application/pdf and text/plain never shows a .docx. Either open the picker with */* and validate MIME and extension afterwards, or change the deliverable.
- The payment return 'App Link heymika://payment/status/{orderId}' is a custom scheme, not an App Link. Any app can register heymika:// and intercept or spoof the return. Use a verified https App Link (for example https://<domain>/payment/status/{orderId}) for the Midtrans finish URL and treat the link only as a trigger for a server status fetch. With Play Billing there is no redirect at all (PurchasesUpdatedListener), so Phase 10's deep-link return tests depend on the gateway choice.
- The bottom-nav evidence is misreported. A 'Flashcard' tab appears in 5 mockups: Materimu, Tanya Mika, Daftar Kuis, Daftar Flashcard and Bank Kesalahan (the plan cites only Materimu and Tanya Mika). 'Kuis' appears in 5: Beranda, Profil, Pusat Notifikasi, Rencana and Statistik. Daftar Kuis, the proposed Kuis tab root, itself shows 'Beranda / Materiku / Flashcards / Tanya Mika' with no Kuis tab. Rencana shows 'Rencana' as a tab, and Tambah Materi shows 'Sesi Belajar'. Restate the open question with this 5-vs-5 evidence and its module consequences (a Rencana tab means :insight must be install-time).
- The fate of some existing code is not decided: AiRepository/IAiRepository, AiUseCase/AiInteractor, GenerationRequest/GenerationResponse, and ListMaterialResponse/MaterialResponse/QuizQuestionResponse. AiStudioViewModel, the only AiUseCase consumer, is deleted in P5, so these are orphaned until the vague 'old endpoints removed in Phase 11'. Assign each one a delete or reshape phase.
- The coverage setup misses the tests the plan writes. The JaCoCo task excludes ui/**, but the P0 RichText parser (core/ui) and the P4 SummarySectionParser (core/ui/summary) are the unit-tested logic, so their coverage never counts, and the P0 'non-zero classes' check rests on ErrorMapper alone. Move parsers to core/utils or core/domain and keep only composables under ui/.
- The P0 deliverable 'Toggling airplane mode shows the OfflineBanner' does not say where the banner is mounted. AppShell, which hosts OfflineBanner, arrives in P3. Name the P0 host (gallery or root scaffold).
- Dark mode is left inconsistent. P0 forces light while the existing SettingScreen dark-mode switch stays live until P3, so for that period it is a toggle that does nothing. The Profil mockup has no dark-mode control, yet AppPreferences keeps darkMode, and the decision is deferred to P11. Decide in P0: hide the toggle until a dark palette exists.
- The debug-only DesignSystemGallery/SystemStatesCatalog routes live in app/src/debug, but HeyMikaNavHost lives in main and cannot reference debug classes. The plan needs a DebugDestinations hook with a no-op app/src/release counterpart (or a debug-only FeatureEntry). Without it, the P0 deliverable is not buildable for release.

**verdict:** needs_revision


Analisis per layar lengkap (sections, components, states, api_needs): `heymika-mockup-analysis.json`.
