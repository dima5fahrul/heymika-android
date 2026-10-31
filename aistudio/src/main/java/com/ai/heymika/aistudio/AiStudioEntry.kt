package com.ai.heymika.aistudio

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ai.heymika.aistudio.di.aiStudioModule
import com.ai.heymika.aistudio.presentation.AiStudioRoute
import com.ai.heymika.core.domain.model.GenerationKind
import com.ai.heymika.core.navigation.FeatureEntry
import org.koin.core.context.loadKoinModules

/**
 * Entry point of the `:aistudio` dynamic feature.
 *
 * `:app` never links against this class. It is instantiated reflectively once Play Feature
 * Delivery has installed the module:
 *
 * ```
 * Class.forName("com.ai.heymika.aistudio.AiStudioEntry").getDeclaredConstructor().newInstance()
 * ```
 *
 * Therefore the class must stay public, with a no-arg constructor, at this exact
 * fully-qualified name. See `proguard-rules.pro`.
 */
class AiStudioEntry : FeatureEntry {
    init {
        // The Koin modules of a feature installed at runtime cannot be part of the
        // application's startup module list, so they are registered the moment the entry
        // is constructed - i.e. before `register` can put the screen on the back stack.
        ensureKoinModulesLoaded()
    }

    /**
     * The contract route `:app` navigates to. `materialId` is the only mandatory argument, so
     * `navigate("aistudio/$id")` keeps working; the optional `kind` query argument is declared
     * on the registered pattern and defaults to [GenerationKind.QUIZ].
     */
    override val route: String = ROUTE

    override fun register(
        builder: NavGraphBuilder,
        navController: NavHostController,
    ) {
        builder.composable(
            route = ROUTE_PATTERN,
            arguments =
                listOf(
                    navArgument(ARG_MATERIAL_ID) {
                        type = NavType.StringType
                    },
                    navArgument(ARG_KIND) {
                        type = NavType.StringType
                        defaultValue = GenerationKind.QUIZ.name
                    },
                ),
        ) { backStackEntry ->
            val arguments = backStackEntry.arguments
            AiStudioRoute(
                materialId = arguments?.getString(ARG_MATERIAL_ID).orEmpty(),
                kind = generationKindOf(arguments?.getString(ARG_KIND)),
                onNavigateUp = { navController.navigateUp() },
            )
        }
    }

    companion object {
        const val ARG_MATERIAL_ID: String = "materialId"
        const val ARG_KIND: String = "kind"

        /** The route as `:app` knows it. */
        const val ROUTE: String = "aistudio/{materialId}"

        /** The same destination, plus the optional `kind` query argument. */
        const val ROUTE_PATTERN: String = "aistudio/{materialId}?kind={kind}"

        /**
         * Builds a deep link for this destination. `:app` may also simply navigate to
         * `route` with `{materialId}` substituted, which resolves to a QUIZ.
         */
        fun routeFor(
            materialId: String,
            kind: GenerationKind,
        ): String = "aistudio/$materialId?kind=${kind.name}"

        private fun generationKindOf(raw: String?): GenerationKind =
            GenerationKind.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
                ?: GenerationKind.QUIZ

        @Volatile
        private var koinModulesLoaded: Boolean = false

        private fun ensureKoinModulesLoaded() {
            if (koinModulesLoaded) return
            synchronized(this) {
                if (koinModulesLoaded) return
                loadKoinModules(aiStudioModule)
                koinModulesLoaded = true
            }
        }
    }
}
