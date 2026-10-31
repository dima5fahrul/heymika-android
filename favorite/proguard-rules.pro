# :app resolves this module's entry point reflectively after Play Feature
# Delivery installs the module:
#
#     Class.forName("com.ai.heymika.favorite.FavoriteEntry")
#         .getDeclaredConstructor()
#         .newInstance()
#
# Reflection is the ONLY reference to the class, so R8 would otherwise see it as
# unused. Keep the class under its original name together with its no-arg
# constructor.
-keep class com.ai.heymika.favorite.FavoriteEntry {
    public <init>();
}

# The interface the entry is cast to must keep its name as well.
-keepnames interface com.ai.heymika.core.navigation.FeatureEntry
