package com.ai.heymika.setting

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ai.heymika.core.navigation.FeatureEntry
import com.ai.heymika.setting.di.settingModule
import com.ai.heymika.setting.presentation.SettingRoute
import org.koin.core.context.loadKoinModules

/**
 * Entry point of the `:setting` dynamic feature.
 *
 * `:app` never links against this class. It is instantiated reflectively once Play Feature
 * Delivery has installed the module:
 *
 * ```
 * Class.forName("com.ai.heymika.setting.SettingEntry").getDeclaredConstructor().newInstance()
 * ```
 *
 * Therefore the class must stay public, with a no-arg constructor, at this exact
 * fully-qualified name. See `proguard-rules.pro`.
 */
class SettingEntry : FeatureEntry {
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
            SettingRoute(onNavigateUp = { navController.navigateUp() })
        }
    }

    companion object {
        const val ROUTE: String = "setting"

        @Volatile
        private var koinModulesLoaded: Boolean = false

        private fun ensureKoinModulesLoaded() {
            if (koinModulesLoaded) return
            synchronized(this) {
                if (koinModulesLoaded) return
                loadKoinModules(settingModule)
                koinModulesLoaded = true
            }
        }
    }
}
