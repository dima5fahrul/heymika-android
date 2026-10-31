# HeyMika quality gates and CI

Three gates run on every push and pull request to `main`: ktlint, unit tests with JaCoCo coverage
for `:core`, and the release build. The workflow is `.github/workflows/ci.yml` and is modeled on the
CapstoneAndroidExpert reference.

## Tasks

| Gate | Command | Output |
| --- | --- | --- |
| Lint | `./gradlew ktlintCheck` | `*/build/reports/ktlint/` (plain + checkstyle) |
| Fix formatting (local only) | `./gradlew ktlintFormat` | rewrites sources in place |
| Unit tests | `./gradlew testDebugUnitTest` | `*/build/reports/tests/testDebugUnitTest/` |
| Coverage | `./gradlew :core:jacocoDebugTestReport` | `core/build/reports/jacoco/jacocoDebugTestReport/` (`html/` + `.xml`) |
| Release build | `./gradlew assembleRelease bundleRelease` | `*/build/outputs/apk/release/*.apk`, `app/build/outputs/bundle/release/*.aab` |
| Release-dex check | `scripts/ci/check-release-apk.sh app/build/outputs/apk/release/app-release-unsigned.apk` | fails if `apkanalyzer` finds `com.ai.heymika.debug` or `DesignGallery` classes or manifest entries, or more than one LAUNCHER |

`ktlintCheck` depends on one task per source set in every module, for example
`:core:ktlintMainSourceSetCheck`, `:core:ktlintTestSourceSetCheck`,
`:core:ktlintAndroidTestSourceSetCheck` and `:core:ktlintKotlinScriptCheck`. Any of them can run on
its own. The root project only has the `*.kts` check, which covers `build.gradle.kts` and
`settings.gradle.kts`.

## ktlint

### Problem

With `org.jlleitschuh.gradle.ktlint` 12.1.1, `./gradlew :core:tasks --all` listed only
`ktlintKotlinScriptCheck`. No `.kt` file had ever been linted, and the only reports were empty
script-check reports.

### Cause

Version 12.x adds Android source-set tasks only from
`plugins.withId("org.jetbrains.kotlin.android")`. HeyMika uses the Kotlin that is built into AGP 9,
and it must not apply `org.jetbrains.kotlin.android`, so that callback never ran. Only the
`*.kts` tasks, which do not depend on a Kotlin plugin, were registered.

### Options considered

1. **A newer ktlint-gradle (chosen).** Release 14.1.0 added support for AGP built-in Kotlin
   ([#1016](https://github.com/JLLeitschuh/ktlint-gradle/pull/1016)). It hooks
   `com.android.base` together with `KotlinBaseApiPlugin`, and from there it reads
   `android.sourceSets.*.kotlin`, which holds `src/<set>/java` and `src/<set>/kotlin`. This is a
   one-line version bump, and it keeps the task names and the `.editorconfig` setup.
2. Registering `KtLintCheckTask` per source set by hand. This relies on the plugin's internal
   wiring (classpath configurations, reporters, baseline), so it breaks on any plugin update.
3. Moving to `com.diffplug.spotless` with ktlint. This works, but it brings a new plugin, new task
   names (`spotlessCheck`) and a second configuration surface for the same rules.

### Decision

- `ktlint` (the plugin) is 14.2.0 and `ktlintEngine` (the CLI) is 1.8.0 in
  `gradle/libs.versions.toml`. Engine 1.7.0 moved to the Kotlin 2.2 parser (for example, context
  parameters). The plugin's default engine, 1.5.0, still uses the Kotlin 2.1 parser.
- The plugin is applied in the root `build.gradle.kts` through `allprojects { }`, with the
  plain and checkstyle reporters.
- The rules still come only from the root `.editorconfig`: `ktlint_official` code style (the
  ktlint 1.x default), max line length 120, and wildcard imports allowed. There is no rule
  configuration in Gradle, so Android Studio and the CLI read the same rules.
- Compose needs one entry under `[*.kt]` in `.editorconfig`, which the integrator adds:
  `ktlint_function_naming_ignore_when_annotated_with = Composable`. Without it, every PascalCase
  `@Composable` function fails `function-naming`, and `ktlintFormat` cannot auto-fix that rule.

### Proof

`./gradlew :core:tasks --all` and `./gradlew :app:tasks --all` now list, per module:

```
ktlintCheck
ktlintMainSourceSetCheck        ktlintTestSourceSetCheck        ktlintAndroidTestSourceSetCheck
ktlintDebugSourceSetCheck       ktlintReleaseSourceSetCheck     ktlintTestDebugSourceSetCheck
ktlintTestReleaseSourceSetCheck ktlintAndroidTestDebugSourceSetCheck
ktlintAndroidTestReleaseSourceSetCheck ktlintTestFixtures*SourceSetCheck
ktlintKotlinScriptCheck
```

Each task also has a matching `runKtlintCheckOver<Set>SourceSet` and `*Format` task. The main
check tasks see `src/main/java` in every module. When this was checked, core had 62 files, app 13,
aistudio 9, favorite 5 and setting 5, and none of them were generated code. The test tasks point at `src/test/java` and
`src/test/kotlin`.

### First run

Code written before this change has never been formatted. Run `./gradlew ktlintFormat` once, review
the diff, fix anything it cannot auto-correct (line length, naming), and commit before you rely on
the CI lint job.

## Coverage (JaCoCo, `:core`)

- `core/build.gradle.kts` applies the `jacoco` plugin and sets `enableUnitTestCoverage = true` on
  `debug`. AGP then runs `testDebugUnitTest` with the JaCoCo agent and writes
  `core/build/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec`.
- The JaCoCo version is pinned to `jacoco = "0.8.14"` (the AGP 9.2.1 default) through both
  `android.testCoverage.jacocoVersion` and `jacoco.toolVersion`, so the agent and the report use the
  same version.
- `jacocoDebugTestReport` (a `JacocoReport`, group `verification`) depends on
  `testDebugUnitTest`. It reads classes from the AGP 9 built-in Kotlin output
  `core/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes` plus
  `core/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes`. The
  `build/tmp/kotlin-classes/debug` path used by the reference and by older guides does not exist
  under AGP 9.
- Excluded: `R`, `R$*`, `BuildConfig`, `Manifest*`, `*_Impl*` (Room), `di/**` (Koin modules),
  `ComposableSingletons*` and `ui/**`. Coverage therefore counts only `data`, `domain`, `utils` and
  `navigation`. Parsers, mappers and rules belong outside `ui/` so that they are counted.
- AGP's own `createDebugUnitTestCoverageReport` still exists, but it cannot take excludes. Use
  `jacocoDebugTestReport`.
- If no unit tests exist yet, there is no `.exec` file and the report task is skipped. The CI upload
  only warns in that case.

## CI workflow

- Triggers: push and pull request to `main`, plus a manual run (`workflow_dispatch`). A newer run on
  the same ref cancels the older one. The token is read-only (`contents: read`).
- Jobs run in a chain, `lint -> test -> build`, on `ubuntu-latest` with Temurin JDK 17. The Gradle
  cache comes from `actions/setup-java` (`cache: gradle`), keyed on the Gradle files and
  `libs.versions.toml`. The actions are the Node 24 majors: `checkout@v7`, `setup-java@v6` and
  `upload-artifact@v7`.
- Each job writes `local.properties`. `heymika.api.base.url` is written only when the optional
  repository secret `HEYMIKA_API_BASE_URL` is set, and the build falls back to
  `https://api.example.com/` otherwise. There is no backend yet, so no secret is required. The
  Android SDK comes from the runner's `ANDROID_HOME`.
  `core/build.gradle.kts` reads `heymika.api.base.url` first and falls back to the pre-rename
  `ailearn.api.base.url` key, so older local files keep working.
- Artifacts:
  - `ktlint-reports`, on failure
  - `coverage-report`
  - `unit-test-reports`, on failure
  - `release-apk`, the base and feature APKs (unsigned)
  - `release-aab`

  The features use on-demand delivery, so a sideloaded base APK cannot open the feature tabs. The
  AAB is the real deliverable.
- After the release build, `scripts/ci/check-release-apk.sh` runs `apkanalyzer dex packages` and
  `apkanalyzer manifest print` on the base APK. The debug design gallery lives only in
  `app/src/debug`; the check fails the build if any of it, or a second launcher, reaches release.

## Later (not in this stage)

- OWASP dependency-check plugin and a `security` CI job (`continue-on-error`, cached NVD data, an
  NVD API key secret). Plan P12.
- Certificate pinning and `network_security_config`. These wait for a real backend host.
- A coverage gate (for example, at least 60% on `core` data and domain) once the P0 tests are in.
  Plan P12.
- Release signing from CI secrets; until then the outputs are unsigned.
- Gradle wrapper validation (`gradle/actions/wrapper-validation`) and Android Lint (`lintDebug`) as
  extra CI steps.
