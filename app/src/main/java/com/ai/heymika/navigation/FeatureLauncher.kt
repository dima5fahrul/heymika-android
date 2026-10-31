package com.ai.heymika.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController

/** A tab or action the user asked for before its module was on the device. */
data class PendingFeature(
    val feature: DynamicFeature,
    val targetRoute: String,
)

/**
 * Opens a route served by a dynamic feature module. When the module is installed it navigates
 * right away; otherwise it starts the split install and remembers the request as [pending], which
 * [HeyMikaNavHost] completes (or shows progress for) once the module reports installed.
 */
@Stable
class FeatureLauncher(
    private val navController: NavHostController,
    private val installer: DynamicFeatureInstaller,
) {
    var pending: PendingFeature? by mutableStateOf(null)
        private set

    fun open(
        feature: DynamicFeature,
        targetRoute: String,
    ) {
        if (installer.isInstalled(feature)) {
            navController.navigate(targetRoute)
        } else {
            pending = PendingFeature(feature = feature, targetRoute = targetRoute)
            installer.install(feature)
        }
    }

    /** Navigates to the pending route once its module is installed; true when it did. */
    fun completePending(): Boolean {
        val request = pending ?: return false
        if (!installer.isInstalled(request.feature)) return false
        // runCatching: if the module's FeatureEntry has not been folded into the graph yet,
        // swallow it and retry on the next recomposition rather than crash the app.
        val navigated = runCatching { navController.navigate(request.targetRoute) }.isSuccess
        if (navigated) pending = null
        return navigated
    }

    fun dismiss() {
        pending = null
    }
}

@Composable
fun rememberFeatureLauncher(
    navController: NavHostController,
    installer: DynamicFeatureInstaller,
): FeatureLauncher = remember(navController, installer) { FeatureLauncher(navController, installer) }
