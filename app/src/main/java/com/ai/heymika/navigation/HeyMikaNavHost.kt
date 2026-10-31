package com.ai.heymika.navigation

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ai.heymika.R
import com.ai.heymika.core.ui.component.navigation.HeyMikaBottomNavBar
import com.ai.heymika.core.ui.component.navigation.HeyMikaTabItem
import com.ai.heymika.detail.MaterialDetailScreen
import com.ai.heymika.home.HomeScreen

/**
 * The single navigation graph of the app: the `:app` destinations (home, detail) plus one
 * contribution per *installed* dynamic feature module. Hosted by `com.ai.heymika.HeyMikaRoot`,
 * which owns the frame (offline banner, snackbar host, bottom bar) around it.
 *
 * A route whose module is still missing never reaches the graph: [FeatureLauncher] starts a split
 * install and this composable raises [FeatureInstallDialog] instead, then navigates once the
 * module reports [FeatureInstallState.Installed].
 */
@Composable
fun HeyMikaNavHost(
    navController: NavHostController,
    installer: DynamicFeatureInstaller,
    launcher: FeatureLauncher,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val installStates by installer.installStates.collectAsStateWithLifecycle()

    // Re-resolved whenever any module changes state, which is what makes a freshly installed
    // feature appear in the graph below without restarting the app.
    val featureEntries = remember(installStates) { installer.installedEntries() }

    // Keyed on featureEntries so it re-runs in the same frame the new destinations are added.
    LaunchedEffect(featureEntries, launcher.pending) { launcher.completePending() }

    // The builder captures `featureEntries`, so a new graph is produced whenever a module
    // finishes installing. NavGraph implements equals(), so an unchanged rebuild is a cheap
    // no-op inside NavController.setGraph() and the back stack survives it.
    NavHost(
        navController = navController,
        startDestination = Route.HOME,
        modifier = modifier,
    ) {
        val navGraphBuilder = this
        composable(route = Route.HOME) {
            HomeScreen(
                onMaterialClick = { materialId ->
                    navController.navigate(Route.detail(materialId))
                },
            )
        }
        composable(route = Route.DETAIL) { backStackEntry ->
            val materialId =
                backStackEntry.arguments
                    ?.getString(Route.ARG_MATERIAL_ID)
                    .orEmpty()
            MaterialDetailScreen(
                materialId = materialId,
                onBack = { navController.popBackStack() },
                onGenerate = { kind ->
                    launcher.open(DynamicFeature.AI_STUDIO, Route.aiStudio(materialId, kind))
                },
            )
        }
        // Each installed module contributes its own destinations. `:app` knows nothing
        // about them beyond the FeatureEntry contract declared in `:core`.
        featureEntries.forEach { entry -> entry.register(navGraphBuilder, navController) }
    }

    val request = launcher.pending
    if (request != null) {
        val state = installStates[request.feature.moduleName] ?: FeatureInstallState.NotInstalled
        if (state !is FeatureInstallState.Installed) {
            FeatureInstallDialog(
                featureLabel = featureLabel(request.feature),
                state = state,
                onRetry = { installer.install(request.feature) },
                onConfirm = {
                    val activity = context as? Activity
                    if (activity != null && state is FeatureInstallState.RequiresConfirmation) {
                        installer.startUserConfirmation(activity, state)
                    }
                },
                onDismiss = launcher::dismiss,
            )
        }
    }
}

/**
 * The legacy bottom navigation (Beranda / Favorit / Pengaturan), drawn with the design-system
 * [HeyMikaBottomNavBar] until P3 replaces it with the Beranda / Materi / Kuis / Tanya Mika /
 * Profil shell. [currentRoute] marks the selected tab; a tab served by a module goes through
 * [launcher] so it is installed first.
 */
@Composable
fun LegacyBottomBar(
    currentRoute: String?,
    navController: NavHostController,
    launcher: FeatureLauncher,
    modifier: Modifier = Modifier,
) {
    val tabs = remember { BottomTab.entries.map(BottomTab::toTabItem) }
    HeyMikaBottomNavBar(
        tabs = tabs,
        selectedId = BottomTab.entries.firstOrNull { it.route == currentRoute }?.route,
        onSelect = { id ->
            val tab = BottomTab.entries.first { it.route == id }
            val feature = tab.feature
            if (feature == null) {
                navController.navigate(tab.route) {
                    popUpTo(Route.HOME)
                    launchSingleTop = true
                }
            } else {
                launcher.open(feature, tab.route)
            }
        },
        modifier = modifier,
    )
}

/**
 * Bottom navigation items. [feature] is `null` for destinations that live in `:app` and
 * non-null for the ones served by an on-demand module. The route doubles as the tab id.
 */
private enum class BottomTab(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val feature: DynamicFeature?,
) {
    HOME(Route.HOME, R.string.tab_home, Icons.Outlined.Home, Icons.Filled.Home, null),
    FAVORITE(
        Route.FAVORITE,
        R.string.tab_favorite,
        Icons.Outlined.FavoriteBorder,
        Icons.Filled.Favorite,
        DynamicFeature.FAVORITE,
    ),
    SETTING(
        Route.SETTING,
        R.string.tab_setting,
        Icons.Outlined.Settings,
        Icons.Filled.Settings,
        DynamicFeature.SETTING,
    ),
    ;

    fun toTabItem(): HeyMikaTabItem =
        HeyMikaTabItem(id = route, labelRes = labelRes, icon = icon, selectedIcon = selectedIcon)
}

@Composable
private fun featureLabel(feature: DynamicFeature): String =
    when (feature) {
        DynamicFeature.AI_STUDIO -> stringResource(R.string.feature_aistudio)
        DynamicFeature.FAVORITE -> stringResource(R.string.feature_favorite)
        DynamicFeature.SETTING -> stringResource(R.string.feature_setting)
    }
