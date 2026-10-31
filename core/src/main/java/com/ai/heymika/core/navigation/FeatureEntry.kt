package com.ai.heymika.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

/**
 * Contract every dynamic feature module implements so :app can graft the
 * feature's destinations onto the single NavHost without a compile-time
 * dependency on the feature module.
 */
interface FeatureEntry {
    val route: String

    fun register(
        builder: NavGraphBuilder,
        navController: NavHostController,
    )
}
