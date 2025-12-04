package com.sunmi.tapro.taplink.demo

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

/**
 * Taplink Demo MainActivity
 *
 * 使用 Jetpack Compose 实现的 Demo 应用
 * 展示如何使用新的 TaplinkSDK API
 *
 * @author TaPro Team
 * @since 2025-01-XX
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaplinkDemoNavigation()
                }
            }
        }
    }
}
