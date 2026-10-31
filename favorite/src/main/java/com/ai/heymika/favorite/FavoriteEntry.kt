package com.ai.heymika.favorite

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ai.heymika.core.navigation.FeatureEntry
import com.ai.heymika.favorite.di.favoriteModule
import com.ai.heymika.favorite.presentation.FavoriteRoute
import org.koin.core.context.loadKoinModules

/**
 * Entry point of the `:favorite` dynamic feature.
 *
 * `:app` never links against this class. It is instantiated reflectively once Play Feature
 * Delivery has installed the module:
 *
 * ```
 * Class.forName("com.ai.heymika.favorite.FavoriteEntry").getDeclaredConstructor().newInstance()
 * ```
 *
 * Therefore the class must stay public, with a no-arg constructor, at this exact
 * fully-qualified name. See `proguard-rules.pro`.
 */
class FavoriteEntry : FeatureEntry {
    init {
        // The Koin modules of a feature installed at runtime cannot be part of the
        // application's startup module list, so they are registered the moment the entry
        // is constructed - i.e. before `register` can put the screen on the back stack.
        ensureKoinModulesLoaded()
    }

    override val route: String = ROUTE

    override fun register(
        builder: NavGraphBuilder,
        navController: NavHostController,
    ) {
        builder.composable(route = ROUTE) {
            FavoriteRoute(
                onMaterialClick = { materialId ->
                    navController.navigate("$DETAIL_ROUTE_PREFIX$materialId")
                },
                onNavigateUp = { navController.navigateUp() },
            )
        }
    }

    companion object {
        const val ROUTE: String = "favorite"

        /** Detail destination owned by `:app`; the full route is `detail/{materialId}`. */
        private const val DETAIL_ROUTE_PREFIX: String = "detail/"

        @Volatile
        private var koinModulesLoaded: Boolean = false

        private fun ensureKoinModulesLoaded() {
            if (koinModulesLoaded) return
            synchronized(this) {
                if (koinModulesLoaded) return
                loadKoinModules(favoriteModule)
                koinModulesLoaded = true
            }
        }
    }
}
