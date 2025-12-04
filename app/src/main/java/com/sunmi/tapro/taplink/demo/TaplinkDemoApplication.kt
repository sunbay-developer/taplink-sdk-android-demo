package com.sunmi.tapro.taplink.demo

import android.app.Application
import com.sunmi.tapro.log.CLog
import com.sunmi.tapro.taplink.demo.viewmodel.TaplinkDemoViewModel
import com.sunmi.tapro.taplink.sdk.TaplinkSDK
import com.sunmi.tapro.taplink.sdk.config.TaplinkConfig
import com.sunmi.tapro.taplink.sdk.enums.LogLevel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Taplink Demo Application
 *
 * @author TaPro Team
 * @since 2025-01-XX
 */
class TaplinkDemoApplication : Application() {

    private val TAG = "TaplinkDemoApplication"

    override fun onCreate() {
        super.onCreate()
        CLog.d(TAG, "TaplinkDemoApplication onCreate")

        // 初始化 Koin
        startKoin {
            androidContext(this@TaplinkDemoApplication)
            modules(demoModule)
        }

        // 初始化 Taplink SDK
        initTaplinkSDK()
    }

    /**
     * 初始化 Taplink SDK
     */
    private fun initTaplinkSDK() {
        try {
            val config = TaplinkConfig(
                appId = "taplink_demo_app",
                merchantId = "demo_merchant_id",
                secretKey = "demo_secret_key"
            )
                .setLogEnabled(true)
                .setLogLevel(LogLevel.DEBUG)

            TaplinkSDK.init(this, config)
            CLog.d(TAG, "Taplink SDK initialized successfully")
        } catch (e: Exception) {
            CLog.e(TAG, "Failed to initialize Taplink SDK: ${e.message}")
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        CLog.d(TAG, "TaplinkDemoApplication onTerminate")
    }
}

/**
 * Koin 依赖注入模块
 */
val demoModule = module {
    viewModel { TaplinkDemoViewModel() }
}



