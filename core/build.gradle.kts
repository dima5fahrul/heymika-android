import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "com.ai.heymika.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // The base URL of OUR OWN backend, which proxies Anthropic server side.
        // The Anthropic API key lives on that backend and never ships in the APK:
        // anything bundled in an app can be extracted from it.
        val props = Properties()
        val propsFile = rootProject.file("local.properties")
        if (propsFile.exists()) {
            props.load(propsFile.inputStream())
        }
        // ailearn.api.base.url is the pre-rename key, still read so older local.properties keep working.
        val apiBaseUrl =
            props.getProperty("heymika.api.base.url")
                ?: props.getProperty("ailearn.api.base.url", "https://api.example.com/")
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        debug {
            // Runs testDebugUnitTest with the JaCoCo agent; jacocoDebugTestReport reads its .exec file.
            enableUnitTestCoverage = true
        }
        release {
            // Library modules must not enable minification; the consuming app
            // module's R8 run handles shrinking. Enabling it here causes the
            // app's R8 to fail with "Missing class" errors because core classes
            // are already obfuscated/removed before the app links against them.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testCoverage {
        jacocoVersion = libs.versions.jacoco.get()
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)

    // Compose — exposed as `api` so :app and the dynamic feature modules
    // inherit the BOM-aligned versions from here.
    val composeBom = platform(libs.androidx.compose.bom)
    api(composeBom)
    androidTestImplementation(composeBom)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navigation — FeatureEntry exposes NavGraphBuilder/NavHostController in
    // its signature, so this has to be part of core's public API surface.
    api(libs.androidx.navigation.compose)

    // Lifecycle
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.lifecycle.viewmodel.compose)

    // Koin
    api(libs.koin.android)
    api(libs.koin.androidx.compose)

    // Coroutines
    api(libs.kotlinx.coroutines.android)

    // Images
    api(libs.coil.compose)

    // Retrofit
    api(libs.retrofit)
    api(libs.retrofit.gson)
    api(libs.okhttp.logging)

    // Room
    api(libs.room.runtime)
    api(libs.room.ktx)
    ksp(libs.room.compiler)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Coverage counts logic only: composables (ui/**), Koin wiring (di/**) and generated code are
// excluded. AGP 9 built-in Kotlin compiles to built_in_kotlinc, not build/tmp/kotlin-classes.
val coverageExcludes =
    listOf(
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*_Impl*",
        "**/di/**",
        "**/ComposableSingletons*",
        "**/ui/**",
    )

tasks.register<JacocoReport>("jacocoDebugTestReport") {
    group = "verification"
    description = "Writes the JaCoCo HTML and XML coverage report for the :core debug unit tests."
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
            exclude(coverageExcludes)
        },
        fileTree(layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")) {
            exclude(coverageExcludes)
        },
    )
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("outputs/unit_test_code_coverage/debugUnitTest/*.exec")
        },
    )
}
