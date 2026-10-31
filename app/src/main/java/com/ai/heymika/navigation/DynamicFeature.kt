package com.ai.heymika.navigation

/**
 * The three Play Feature Delivery modules declared in `app/build.gradle.kts`.
 *
 * [moduleName] is the Gradle project name without the leading colon — that is exactly the
 * split name `SplitInstallManager` expects. [entryClassName] is the fully qualified name of the
 * module's `FeatureEntry`; it is a *string* on purpose, because `:app` cannot reference those
 * classes at compile time.
 */
enum class DynamicFeature(
    val moduleName: String,
    val entryClassName: String,
    val route: String,
) {
    AI_STUDIO(
        moduleName = "aistudio",
        entryClassName = "com.ai.heymika.aistudio.AiStudioEntry",
        route = Route.AISTUDIO,
    ),
    FAVORITE(
        moduleName = "favorite",
        entryClassName = "com.ai.heymika.favorite.FavoriteEntry",
        route = Route.FAVORITE,
    ),
    SETTING(
        moduleName = "setting",
        entryClassName = "com.ai.heymika.setting.SettingEntry",
        route = Route.SETTING,
    ),
    ;

    companion object {
        fun fromModuleName(moduleName: String): DynamicFeature? = entries.firstOrNull { it.moduleName == moduleName }
    }
}
