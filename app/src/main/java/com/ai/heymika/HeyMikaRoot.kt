package com.ai.heymika

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ai.heymika.core.domain.connectivity.ConnectivityObserver
import com.ai.heymika.core.ui.component.overlay.HeyMikaSnackbarHost
import com.ai.heymika.core.ui.component.overlay.LocalHeyMikaSnackbarHostState
import com.ai.heymika.core.ui.component.state.OfflineBanner
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.navigation.DynamicFeatureInstaller
import com.ai.heymika.navigation.HeyMikaNavHost
import com.ai.heymika.navigation.LegacyBottomBar
import com.ai.heymika.navigation.rememberFeatureLauncher
import org.koin.compose.koinInject

/**
 * Root composable, hosted by [MainActivity] inside `HeyMikaTheme`. It stays the host in every
 * later phase: the [HeyMikaRootFrame] around the NavHost gives every route, auth included, the
 * offline banner and the global snackbar host.
 */
@Composable
fun HeyMikaRoot(modifier: Modifier = Modifier) {
    val installer: DynamicFeatureInstaller = koinInject()
    val navController = rememberNavController()
    val launcher = rememberFeatureLauncher(navController, installer)
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    HeyMikaRootFrame(
        modifier = modifier,
        bottomBar = {
            LegacyBottomBar(
                currentRoute = currentBackStackEntry?.destination?.route,
                navController = navController,
                launcher = launcher,
            )
        },
    ) {
        HeyMikaNavHost(
            navController = navController,
            installer = installer,
            launcher = launcher,
        )
    }
}

/**
 * The app frame shared by [HeyMikaRoot] and the debug design gallery:
 * - the [OfflineBanner] slides in under the status bar while [ConnectivityObserver] reports no
 *   internet;
 * - [HeyMikaSnackbarHost] shows [snackbarHostState], which is also provided as
 *   [LocalHeyMikaSnackbarHostState] so any screen (feature modules included) can post a toast;
 * - [bottomBar] sits below the content and the snackbar above it.
 *
 * The frame applies the system-bar insets it pads for and consumes them, so screens inside must
 * not pad for the status or navigation bar again (Scaffold and the HeyMika bars already respect
 * consumed insets).
 */
@Composable
fun HeyMikaRootFrame(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    connectivityObserver: ConnectivityObserver = koinInject(),
    bottomBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val isOnline by connectivityObserver.isOnline.collectAsStateWithLifecycle(initialValue = true)
    CompositionLocalProvider(LocalHeyMikaSnackbarHostState provides snackbarHostState) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                OfflineBanner(
                    visible = !isOnline,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                )
            },
            bottomBar = bottomBar,
            snackbarHost = { HeyMikaSnackbarHost(hostState = snackbarHostState) },
            containerColor = HeyMikaTheme.colors.canvas,
            contentColor = HeyMikaTheme.colors.textPrimary,
        ) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
            ) {
                content()
            }
        }
    }
}
