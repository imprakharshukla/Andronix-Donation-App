package com.techriz.andronix.donation.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import javax.net.SocketFactory

class ConnectivityFlow constructor(
    private val appContext: Context
) {
    private var _connection = MutableSharedFlow<Boolean>()
    val connection: SharedFlow<Boolean> = _connection


    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private val cm =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()

    private suspend fun checkValidNetworks() {
        _connection.emit(validNetworks.size > 0 )
    }

    suspend fun getNetworkStateUpdates() {
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val ckInitialNet = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm.getNetworkCapabilities(cm.activeNetwork)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            val net = cm.allNetworks.find {
                cm.getNetworkCapabilities(it)
                    ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            }
            cm.getNetworkCapabilities(net)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        }
        if (!ckInitialNet) {
            //initially there is no internet connection.
            _connection.emit(false)
        }

        cm.registerNetworkCallback(networkRequest, networkCallback)
    }


    fun unregisterNetCallback() {
        cm.unregisterNetworkCallback(networkCallback)
    }


    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {

        /*
          Called when a network is detected. If that network has internet, save it in the Set.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onAvailable(android.net.Network)
         */
        override fun onAvailable(network: Network) {
            Log.d(TAG, "onAvailable: $network")
            val networkCapabilities = cm.getNetworkCapabilities(network)
            val hasInternetCapability =
                networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            Log.d(TAG, "onAvailable: ${network}, $hasInternetCapability")
            if (hasInternetCapability == true) {
                // check if this network actually has internet
                CoroutineScope(Dispatchers.IO).launch {
                    val hasInternet = DoesNetworkHaveInternet.execute(network.socketFactory)
                    if (hasInternet) {
                        withContext(Dispatchers.Main) {
                            Log.d(TAG, "onAvailable: adding network. $network")
                            validNetworks.add(network)
                            checkValidNetworks()
                        }
                    }
                }
            }
        }

        /*
          If the callback was registered with registerNetworkCallback() it will be called for each network which no longer satisfies the criteria of the callback.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onLost(android.net.Network)
         */
        override fun onLost(network: Network) {
            Log.d(TAG, "onLost: $network")
            validNetworks.remove(network)
            CoroutineScope(Dispatchers.IO).launch {
                checkValidNetworks()
            }
        }

    }

    object DoesNetworkHaveInternet {
        // Make sure to execute this on a background thread.
        fun execute(socketFactory: SocketFactory): Boolean {
            return try {
                Log.d(TAG, "PINGING google.")
                val socket = socketFactory.createSocket() ?: throw IOException("Socket is null.")
                socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                socket.close()
                Log.d(TAG, "PING success.")
                true
            } catch (e: IOException) {
                Log.e(TAG, "No internet connection. $e")
                false
            }
        }
    }
}


sealed class ConnectionState {
    object Connected : ConnectionState()
    object Disconnected : ConnectionState()
    data class NetworkType(var type: String) : ConnectionState()
}