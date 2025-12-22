package com.sunmi.tapro.taplink.demo.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.util.regex.Pattern

/**
 * Network utility class
 * 
 * Provides network-related validation functions for LAN mode configuration
 */
object NetworkUtils {
    
    /**
     * Check if device is connected to network
     * 
     * @param context Android Context
     * @return true if network is connected, false otherwise
     */
    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
            (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
             networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
             networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }
    
    /**
     * Get network connection type
     * 
     * @param context Android Context
     * @return Network type string (WIFI, CELLULAR, ETHERNET, NONE)
     */
    fun getNetworkType(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return "NONE"
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return "NONE"
            
            when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
                else -> "UNKNOWN"
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            when (networkInfo?.type) {
                ConnectivityManager.TYPE_WIFI -> "WIFI"
                ConnectivityManager.TYPE_MOBILE -> "CELLULAR"
                ConnectivityManager.TYPE_ETHERNET -> "ETHERNET"
                else -> "NONE"
            }
        }
    }
    
    /**
     * Validate IP address format
     * 
     * @param ip IP address string
     * @return true if valid IPv4 address format, false otherwise
     */
    fun isValidIpAddress(ip: String): Boolean {
        if (ip.isBlank()) return false
        
        val pattern = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        )
        return pattern.matcher(ip).matches()
    }
    
    /**
     * Validate port number range
     * 
     * @param port Port number
     * @return true if port is in valid range (1-65535), false otherwise
     */
    fun isPortValid(port: Int): Boolean {
        return port in 1..65535
    }
    
    /**
     * Validate port number string
     * 
     * @param portStr Port number string
     * @return true if port string is valid and in range, false otherwise
     */
    fun isPortValid(portStr: String): Boolean {
        return try {
            val port = portStr.toInt()
            isPortValid(port)
        } catch (e: NumberFormatException) {
            false
        }
    }
}