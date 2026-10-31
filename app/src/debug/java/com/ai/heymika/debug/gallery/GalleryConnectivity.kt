package com.ai.heymika.debug.gallery

import com.ai.heymika.core.domain.connectivity.ConnectivityObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/** The real observer, plus a gallery switch that forces "offline" without airplane mode. */
class GalleryConnectivity(
    real: ConnectivityObserver,
) : ConnectivityObserver {
    val forcedOffline = MutableStateFlow(false)

    override val isOnline: Flow<Boolean> =
        combine(real.isOnline, forcedOffline) { online, forced -> online && !forced }.distinctUntilChanged()
}
