package io.github.abdulroufsidhu.tasveer.operations

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class ConnectionListener(context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    fun startListening(onAvailable: ()->Unit, onUnavailable: ()->Unit) {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if (isConnectedToInternet(network)) {
                    onAvailable()
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                onUnavailable()
                // Add your code here to handle internet connection loss
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun stopListening() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun isConnectedToInternet(network: Network): Boolean {
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}