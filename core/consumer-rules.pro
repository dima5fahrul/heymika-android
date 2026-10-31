# Rules shipped to whatever app consumes :core. The app's R8 run is the only
# one that shrinks, so everything core needs kept has to be declared here.

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson / JSON serialization — reflection-based, so names must survive
-keep class com.ai.heymika.core.data.source.remote.request.** { *; }
-keep class com.ai.heymika.core.data.source.remote.response.** { *; }
-keep class com.ai.heymika.core.data.source.local.entity.** { *; }
-keep class com.ai.heymika.core.domain.model.** { *; }
-keepattributes SerializedName

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Koin
-keep class org.koin.** { *; }
-keepnames class * implements org.koin.core.module.Module

# Dynamic feature entry points are resolved reflectively by :app
-keep class * implements com.ai.heymika.core.navigation.FeatureEntry { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
