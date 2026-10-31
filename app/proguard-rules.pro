# Readable stack traces for crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes *Annotation*

# ---------------------------------------------------------------------------
# Dynamic feature entry points — MUST be kept.
#
# :app cannot reference the classes below at compile time: :aistudio,
# :favorite and :setting depend on :app, not the other way round. The only
# thing that ever touches them is
#   Class.forName(fqcn).getDeclaredConstructor().newInstance() as FeatureEntry
# inside com.ai.heymika.navigation.DynamicFeatureInstaller.
#
# R8 therefore sees NO reference to these classes. Without these rules it
# removes them (or just their no-argument constructor, or renames them), and
# the release build crashes with ClassNotFoundException /
# NoSuchMethodException the first time a feature tab is opened. Do not delete
# these rules, and do not narrow them to -keepnames.
# ---------------------------------------------------------------------------
-keep class com.ai.heymika.aistudio.AiStudioEntry {
    public <init>();
    public *;
}
-keep class com.ai.heymika.favorite.FavoriteEntry {
    public <init>();
    public *;
}
-keep class com.ai.heymika.setting.SettingEntry {
    public <init>();
    public *;
}

# The contract itself, plus any other implementation added later.
-keep interface com.ai.heymika.core.navigation.FeatureEntry { *; }
-keep class * implements com.ai.heymika.core.navigation.FeatureEntry {
    public <init>();
    public *;
}

# ---------------------------------------------------------------------------
# Koin — definitions are explicit, but the constructor DSL and scope lookups
# resolve types by name, so keep the DI surface and the ViewModels.
# ---------------------------------------------------------------------------
-keep class com.ai.heymika.di.** { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# ---------------------------------------------------------------------------
# Public API of :core consumed by :app and by the dynamic feature modules.
# ---------------------------------------------------------------------------
-keep class com.ai.heymika.core.data.Resource { *; }
-keep class com.ai.heymika.core.data.Resource$* { *; }
-keep class com.ai.heymika.core.domain.model.** { *; }
-keep class com.ai.heymika.core.domain.usecase.** { *; }

# Play Feature Delivery / SplitCompat.
-keep class com.google.android.play.core.splitcompat.** { *; }
-dontwarn com.google.android.play.core.**

# Kotlin coroutines / serialization helpers that R8 cannot see.
-dontwarn kotlinx.coroutines.**
-dontwarn org.jetbrains.annotations.**
