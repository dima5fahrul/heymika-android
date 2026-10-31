package com.ai.heymika.navigation

import com.google.android.play.core.splitinstall.SplitInstallSessionState

/**
 * UI-facing projection of a `SplitInstallSessionStatus`. Exposed per module by
 * [DynamicFeatureInstaller.installStates].
 */
sealed interface FeatureInstallState {
    /** The split is not on the device and no session is running. */
    data object NotInstalled : FeatureInstallState

    /** Play accepted the request but has not started transferring bytes yet. */
    data object Pending : FeatureInstallState

    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long,
    ) : FeatureInstallState {
        /** Download progress in `0f..1f`, or `0f` while the total size is unknown. */
        val fraction: Float
            get() {
                if (totalBytes <= 0L) return 0f
                return (bytesDownloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
            }
    }

    /** Downloaded, being written to the device. */
    data object Installing : FeatureInstallState

    /** The split is usable; its `FeatureEntry` can be loaded. */
    data object Installed : FeatureInstallState

    /**
     * Play wants the user to approve a large download or a mobile-data transfer. Hand the
     * [sessionState] back to [DynamicFeatureInstaller.startUserConfirmation].
     */
    data class RequiresConfirmation(
        val sessionState: SplitInstallSessionState,
    ) : FeatureInstallState

    /**
     * The install or the entry lookup failed. The UI turns [reason] into Indonesian copy; the raw
     * exception is only logged. [errorCode] is Play's `SplitInstallErrorCode` for [FailureReason.PLAY_ERROR].
     */
    data class Failed(
        val reason: FailureReason,
        val errorCode: Int? = null,
    ) : FeatureInstallState

    enum class FailureReason {
        /** Play rejected the install request before any download started. */
        START_REJECTED,

        /** Play reported the session as failed. */
        PLAY_ERROR,

        /** The split is installed but its FeatureEntry could not be loaded. */
        ENTRY_UNAVAILABLE,
    }
}
