package com.ai.heymika.core.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.ai.heymika.core.domain.connectivity.ConnectivityObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf

/**
 * [ConnectivityObserver] backed by a [ConnectivityManager.NetworkCallback] (needs the normal
 * ACCESS_NETWORK_STATE permission). Online means at least one network with the INTERNET
 * capability; VALIDATED is not required, so a network the system has not probed yet does not
 * flash the offline banner. Each collector registers its own callback and unregisters it when it
 * stops collecting.
 */
class NetworkConnectivityObserver(
    context: Context,
) : ConnectivityObserver {
    private val connectivityManager: ConnectivityManager? =
        context.getSystemService(ConnectivityManager::class.java)

    override val isOnline: Flow<Boolean> =
        connectivityManager?.let(::observe) ?: flowOf(true)

    private fun observe(manager: ConnectivityManager): Flow<Boolean> =
        callbackFlow {
            val callback =
                object : ConnectivityManager.NetworkCallback() {
                    // Callbacks arrive on one ConnectivityThread, so this set needs no locking.
                    private val networks = mutableSetOf<Network>()

                    override fun onAvailable(network: Network) {
                        networks += network
                        trySend(true)
                    }

                    override fun onLost(network: Network) {
                        networks -= network
                        trySend(networks.isNotEmpty())
                    }
                }
            val request =
                NetworkRequest
                    .Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
            // The snapshot goes out BEFORE registering: the callback then reports every matching
            // network right away, so a late snapshot can never overwrite a newer callback value.
            trySend(runCatching { manager.isCurrentlyOnline() }.getOrDefault(true))
            val registered =
                try {
                    manager.registerNetworkCallback(request, callback)
                    true
                } catch (e: RuntimeException) {
                    // SecurityException without the permission, or too many callbacks for this app:
                    // fall back to "online" so a monitoring failure never blocks the UI.
                    Log.w(TAG, "Cannot observe connectivity", e)
                    trySend(true)
                    false
                }
            awaitClose {
                if (registered) manager.unregisterNetworkCallback(callback)
            }
        }.conflate().distinctUntilChanged()

    private fun ConnectivityManager.isCurrentlyOnline(): Boolean =
        activeNetwork
            ?.let { getNetworkCapabilities(it) }
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

    private companion object {
        const val TAG = "ConnectivityObserver"
    }
}
