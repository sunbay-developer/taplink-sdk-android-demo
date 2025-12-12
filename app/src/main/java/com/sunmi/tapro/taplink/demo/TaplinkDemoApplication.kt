package com.sunmi.tapro.taplink.demo

import android.app.Application
import android.util.Log
import com.sunmi.tapro.taplink.sdk.TaplinkSDK
import com.sunmi.tapro.taplink.sdk.config.TaplinkConfig
import com.sunmi.tapro.taplink.sdk.enums.ConnectionMode
import com.sunmi.tapro.taplink.sdk.enums.LogLevel

/**
 * Taplink Demo Application Class
 * Responsible for global application initialization, including Taplink SDK initialization
 */
class TaplinkDemoApplication : Application() {

    companion object {
        private const val TAG = "TaplinkDemoApp"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Taplink SDK
        initializeTaplinkSDK()
    }

    /**
     * Initialize Taplink SDK
     * Complete SDK initialization when the application starts, subsequent calls only need TaplinkSDK.connect
     */
    private fun initializeTaplinkSDK() {
        try {
            Log.d(TAG, "=== Taplink SDK Initialization Started ===")

            // Read configuration from resource files
            val appId = getString(R.string.taplink_app_id)
            val merchantId = getString(R.string.taplink_merchant_id)
            val secretKey = getString(R.string.taplink_secret_key)

            // Log configuration parameters (mask sensitive data)
            Log.d(TAG, "=== SDK Init Request Parameters ===")
            Log.d(TAG, "App ID: $appId")
            Log.d(TAG, "Merchant ID: $merchantId")
            Log.d(TAG, "Secret Key: ${secretKey.take(4)}****${secretKey.takeLast(4)}")
            Log.d(TAG, "Connection Mode: APP_TO_APP")
            Log.d(TAG, "Log Level: DEBUG")
            Log.d(TAG, "Log Enabled: true")

            // Create SDK configuration (using document-standard chain call style)
            val config = TaplinkConfig(
                appId = appId,
                merchantId = merchantId,
                secretKey = secretKey
            ).setLogEnabled(true).setLogLevel(LogLevel.DEBUG).setConnectionMode(ConnectionMode.APP_TO_APP)  // Set to App-to-App mode

            Log.d(TAG, "=== SDK Config Object Created ===")
            Log.d(TAG, "Config: $config")

            // Initialize SDK
            Log.d(TAG, "=== Calling TaplinkSDK.init() ===")
            TaplinkSDK.init(this, config)

            Log.d(TAG, "=== Taplink SDK Initialization Response ===")
            Log.d(TAG, "Status: SUCCESS")
            Log.d(TAG, "Mode: App-to-App")
            Log.d(TAG, "SDK Ready for connection")

        } catch (e: Exception) {
            Log.e(TAG, "=== Taplink SDK Initialization Response ===")
            Log.e(TAG, "Status: FAILURE")
            Log.e(TAG, "Error Type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Error Message: ${e.message}")
            Log.e(TAG, "Full Exception: ", e)
        }
    }
}
