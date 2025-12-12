package com.naptune.lullabyandstory.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InternetConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    // StateFlow for real-time network state
    private val _isNetworkAvailable = MutableStateFlow(checkCurrentNetworkState())
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isNetworkAvailable.value = true
        }
        
        override fun onLost(network: Network) {
            _isNetworkAvailable.value = false
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            // ✅ FIXED: Only check INTERNET capability and transport type, not VALIDATED
            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || 
                             networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                             networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
            _isNetworkAvailable.value = hasInternet
        }
    }
    
    init {
        // Register network callback for real-time monitoring
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        } catch (e: Exception) {
            // Fallback for older API levels
        }
    }
    
    private fun checkCurrentNetworkState(): Boolean {
        return try {
            val network = connectivityManager.activeNetwork
            Log.d("InternetConnectionManager", "📡 Active network: $network")
            
            if (network == null) {
                Log.d("InternetConnectionManager", "❌ No active network found")
                return false
            }
            
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            Log.d("InternetConnectionManager", "🔧 Network capabilities: $networkCapabilities")
            
            if (networkCapabilities == null) {
                Log.d("InternetConnectionManager", "❌ No network capabilities found")
                return false
            }
            
            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val hasWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val hasCellular = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            val hasEthernet = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            
            Log.d("InternetConnectionManager", "🔍 Capabilities - Internet: $hasInternet, WiFi: $hasWifi, Cellular: $hasCellular, Ethernet: $hasEthernet")
            
            val result = hasInternet && (hasWifi || hasCellular || hasEthernet)
            Log.d("InternetConnectionManager", "🎯 Network state result: $result")
            
            result
        } catch (e: Exception) {
            Log.e("InternetConnectionManager", "💥 Network check failed: ${e.message}", e)
            false
        }
    }
    
    fun isCurrentlyConnected(): Boolean {
        return _isNetworkAvailable.value
    }
    
    fun showNoConnectionToast() {
        Toast.makeText(
            context,
            "No internet connection. Please check your network.",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun checkNetworkConnection(): Boolean {
        return checkCurrentNetworkState()
    }
    
    fun checkNetworkAndShowToast(): Boolean {
        val currentState = isCurrentlyConnected()
        Log.d("InternetConnectionManager", "🌐 Network check - StateFlow value: $currentState")
        
        // ✅ ADDITIONAL CHECK: Also verify current network state directly
        val directCheck = checkCurrentNetworkState()
        Log.d("InternetConnectionManager", "🔍 Direct network check: $directCheck")
        
        // ✅ Use the more reliable direct check
        val finalResult = directCheck
        Log.d("InternetConnectionManager", "🎯 Final network result: $finalResult")
        
        return if (finalResult) {
            Log.d("InternetConnectionManager", "✅ Network available - proceeding with operation")
            true
        } else {
            Log.d("InternetConnectionManager", "❌ No network - showing toast")
            showNoConnectionToast()
            false
        }
    }
}