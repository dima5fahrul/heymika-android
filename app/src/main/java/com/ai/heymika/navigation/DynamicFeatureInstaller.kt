package com.ai.heymika.navigation

import android.app.Activity
import android.content.Context
import android.util.Log
import com.ai.heymika.core.navigation.FeatureEntry
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap

/**
 * The bridge between `:app` and the three Play Feature Delivery modules.
 *
 * There is no official `navigation-dynamic-features-compose` artifact, so the three jobs that
 * library would do are wired by hand here:
 *
 *  1. **Is it there?** [isInstalled] asks `SplitInstallManager.installedModules`.
 *  2. **Get it.** [install] starts a split-install session; [installStates] reports
 *     not-installed / pending / downloading-with-progress / installing / installed / failed as
 *     a [StateFlow] the Compose layer collects.
 *  3. **Use it.** [entryFor] loads the module's `FeatureEntry` *by reflection* and caches it,
 *     so [installedEntries] can be folded straight into the `NavHost` graph.
 *
 * Instances are app-scoped (see `com.ai.heymika.di.appModule`): the listener registered in [init]
 * lives for the whole process and the entry cache must be shared.
 */
class DynamicFeatureInstaller(
    context: Context,
) {
    private val splitInstallManager: SplitInstallManager = SplitInstallManagerFactory.create(context)

    private val _installStates =
        MutableStateFlow(
            DynamicFeature.entries.associate { feature -> feature.moduleName to currentStateOf(feature) },
        )

    /** Install state of every [DynamicFeature], keyed by [DynamicFeature.moduleName]. */
    val installStates: StateFlow<Map<String, FeatureInstallState>> = _installStates.asStateFlow()

    /** Reflectively constructed entry points, one per module, created at most once. */
    private val entryCache = ConcurrentHashMap<String, FeatureEntry>()

    /** sessionId -> module names, so a progress callback without module names can be attributed. */
    private val sessionModules = ConcurrentHashMap<Int, List<String>>()

    private val listener = SplitInstallStateUpdatedListener { state -> onSessionStateUpdated(state) }

    init {
        splitInstallManager.registerListener(listener)
    }

    fun isInstalled(feature: DynamicFeature): Boolean = feature.moduleName in splitInstallManager.installedModules

    fun stateOf(feature: DynamicFeature): FeatureInstallState =
        _installStates.value[feature.moduleName] ?: FeatureInstallState.NotInstalled

    /**
     * Requests [feature] from Play. Safe to call repeatedly: an already installed module or an
     * in-flight session is a no-op.
     */
    fun install(feature: DynamicFeature) {
        if (isInstalled(feature)) {
            publish(feature.moduleName, FeatureInstallState.Installed)
            return
        }
        if (stateOf(feature).isInFlight()) return

        publish(feature.moduleName, FeatureInstallState.Pending)
        val request =
            SplitInstallRequest
                .newBuilder()
                .addModule(feature.moduleName)
                .build()
        splitInstallManager
            .startInstall(request)
            .addOnSuccessListener { sessionId -> sessionModules[sessionId] = listOf(feature.moduleName) }
            .addOnFailureListener { throwable ->
                Log.e(TAG, "startInstall(${feature.moduleName}) was rejected", throwable)
                publish(
                    feature.moduleName,
                    FeatureInstallState.Failed(FeatureInstallState.FailureReason.START_REJECTED),
                )
            }
    }

    /** Shows Play's own confirmation dialog for a session that is waiting on the user. */
    @Suppress("DEPRECATION")
    fun startUserConfirmation(
        activity: Activity,
        state: FeatureInstallState.RequiresConfirmation,
    ) {
        runCatching {
            splitInstallManager.startConfirmationDialogForResult(
                state.sessionState,
                activity,
                REQUEST_CODE_CONFIRMATION,
            )
        }.onFailure { error ->
            Log.e(TAG, "Could not show the Play confirmation dialog", error)
        }
    }

    /**
     * The [FeatureEntry] published by [feature], or `null` while the split is not on the device.
     */
    fun entryFor(feature: DynamicFeature): FeatureEntry? {
        if (!isInstalled(feature)) return null
        entryCache[feature.moduleName]?.let { return it }

        // ---------------------------------------------------------------------------------
        // REFLECTION IS LOAD-BEARING HERE. DO NOT "SIMPLIFY" IT INTO `AiStudioEntry()`.
        //
        // :app declares :aistudio, :favorite and :setting in `dynamicFeatures`, and each of
        // those modules depends on :app (that is how Play Feature Delivery is wired — the
        // dependency arrow points from feature to app). So :app has no compile-time view of
        // AiStudioEntry / FavoriteEntry / SettingEntry at all; adding a Gradle dependency the
        // other way round would be a cycle and AGP rejects it.
        //
        // The shared contract is therefore `FeatureEntry`, declared in :core, which both sides
        // can see. The only way to get from the class *name* to an instance is Class.forName.
        //
        // Consequences to keep in mind:
        //  * The FQCNs live in DynamicFeature.entryClassName — renaming an entry class is a
        //    silent runtime break, not a compile error.
        //  * R8 cannot see these classes either, hence the explicit -keep rules in
        //    app/proguard-rules.pro.
        //  * Each entry class must keep a public no-argument constructor.
        // ---------------------------------------------------------------------------------
        val loaded =
            runCatching {
                Class
                    .forName(feature.entryClassName)
                    .getDeclaredConstructor()
                    .newInstance() as FeatureEntry
            }.getOrElse { error ->
                Log.e(TAG, "Could not instantiate ${feature.entryClassName} by reflection", error)
                publish(
                    feature.moduleName,
                    FeatureInstallState.Failed(FeatureInstallState.FailureReason.ENTRY_UNAVAILABLE),
                )
                null
            }
        return loaded?.also { entry -> entryCache[feature.moduleName] = entry }
    }

    /**
     * Entry points of every module currently on the device. The `NavHost` graph is keyed on the
     * result, so a module that finishes installing shows up without an app restart.
     */
    fun installedEntries(): List<FeatureEntry> = DynamicFeature.entries.mapNotNull(::entryFor)

    private fun onSessionStateUpdated(state: SplitInstallSessionState) {
        val modules =
            state
                .moduleNames()
                ?.takeIf { it.isNotEmpty() }
                ?: sessionModules[state.sessionId()]
                ?: return

        val mapped: FeatureInstallState? =
            when (state.status()) {
                SplitInstallSessionStatus.PENDING -> {
                    FeatureInstallState.Pending
                }

                SplitInstallSessionStatus.DOWNLOADING -> {
                    FeatureInstallState.Downloading(
                        bytesDownloaded = state.bytesDownloaded(),
                        totalBytes = state.totalBytesToDownload(),
                    )
                }

                SplitInstallSessionStatus.DOWNLOADED,
                SplitInstallSessionStatus.INSTALLING,
                -> {
                    FeatureInstallState.Installing
                }

                SplitInstallSessionStatus.INSTALLED -> {
                    FeatureInstallState.Installed
                }

                SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                    FeatureInstallState.RequiresConfirmation(state)
                }

                SplitInstallSessionStatus.FAILED -> {
                    FeatureInstallState.Failed(
                        reason = FeatureInstallState.FailureReason.PLAY_ERROR,
                        errorCode = state.errorCode(),
                    )
                }

                SplitInstallSessionStatus.CANCELING,
                SplitInstallSessionStatus.CANCELED,
                -> {
                    FeatureInstallState.NotInstalled
                }

                else -> {
                    null
                }
            }
        if (mapped == null) return

        if (mapped is FeatureInstallState.Installed || mapped is FeatureInstallState.Failed) {
            sessionModules.remove(state.sessionId())
        }
        _installStates.update { current ->
            current.toMutableMap().apply {
                modules.forEach { moduleName -> put(moduleName, mapped) }
            }
        }
    }

    private fun currentStateOf(feature: DynamicFeature): FeatureInstallState =
        if (feature.moduleName in splitInstallManager.installedModules) {
            FeatureInstallState.Installed
        } else {
            FeatureInstallState.NotInstalled
        }

    private fun publish(
        moduleName: String,
        state: FeatureInstallState,
    ) {
        _installStates.update { current -> current + (moduleName to state) }
    }

    private fun FeatureInstallState.isInFlight(): Boolean =
        this is FeatureInstallState.Pending ||
            this is FeatureInstallState.Downloading ||
            this is FeatureInstallState.Installing ||
            this is FeatureInstallState.RequiresConfirmation

    private companion object {
        const val TAG = "DynamicFeature"
        const val REQUEST_CODE_CONFIRMATION = 9_001
    }
}
