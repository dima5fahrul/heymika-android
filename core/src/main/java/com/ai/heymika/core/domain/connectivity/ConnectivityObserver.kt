package com.ai.heymika.core.domain.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Whether the device has a network that offers internet access. Drives the global OfflineBanner;
 * a request can still fail offline while this says true (captive portal, flaky link), which the
 * data layer reports as `AppError.Offline`.
 */
interface ConnectivityObserver {
    /** Cold flow: the current state first, then every change, never the same value twice in a row. */
    val isOnline: Flow<Boolean>
}
