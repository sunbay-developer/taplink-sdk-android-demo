package com.sunmi.tapro.taplink.demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunmi.tapro.log.CLog
import com.sunmi.tapro.taplink.sdk.TaplinkSDK
import com.sunmi.tapro.taplink.sdk.callback.ConnectionListener
import com.sunmi.tapro.taplink.sdk.config.ConnectionConfig
import com.sunmi.tapro.taplink.sdk.error.ConnectionError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Taplink Demo 共享 ViewModel
 *
 * 管理连接状态、日志、交易ID等全局状态
 */
class TaplinkDemoViewModel : ViewModel() {

    private val TAG = "TaplinkDemoViewModel"

    // 连接状态
    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _connectionMode = MutableStateFlow<String?>(null)
    val connectionMode = _connectionMode.asStateFlow()

    private val _deviceId = MutableStateFlow<String?>(null)
    val deviceId = _deviceId.asStateFlow()

    private val _taproVersion = MutableStateFlow<String?>(null)
    val taproVersion = _taproVersion.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()

    // 日志列表
    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages = _logMessages.asStateFlow()

    // 交易ID
    private val _lastTransactionId = MutableStateFlow<String?>(null)
    val lastTransactionId = _lastTransactionId.asStateFlow()

    private val _lastTransactionRequestId = MutableStateFlow<String?>(null)
    val lastTransactionRequestId = _lastTransactionRequestId.asStateFlow()

    private val _lastAuthTransactionId = MutableStateFlow<String?>(null)
    val lastAuthTransactionId = _lastAuthTransactionId.asStateFlow()

    init {
        updateConnectionStatus()
    }

    /**
     * 添加日志
     *
     * 使用 viewModelScope.launch(Dispatchers.Main) 确保在主线程更新 StateFlow，
     * 这样 Compose 才能正确响应状态变化并触发重组
     */
    fun addLog(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
            _logMessages.value = _logMessages.value + "[$timestamp] $message"
        }
    }

    /**
     * 清空日志
     */
    fun clearLogs() {
        _logMessages.value = emptyList()
    }

    /**
     * 更新连接状态
     */
    fun updateConnectionStatus() {
        _isConnected.value = TaplinkSDK.isConnected()
        _connectionMode.value = TaplinkSDK.getConnectionMode()?.name
    }

    /**
     * 连接设备
     */
    fun connectToDevice() {
        if (_isConnecting.value || _isConnected.value) {
            addLog("错误: 正在连接或已连接")
            return
        }

        _isConnecting.value = true
        addLog("开始连接...")

        val config = ConnectionConfig.createDefault()

        TaplinkSDK.connect(config, object : ConnectionListener {

            override fun onConnected(deviceId: String, taproVersion: String) {
                viewModelScope.launch {
                    _isConnecting.value = false
                    _deviceId.value = deviceId
                    _taproVersion.value = taproVersion
                    updateConnectionStatus()
                    addLog("连接成功!")
                    addLog("  - 连接模式: ${_connectionMode.value}")
                    addLog("  - 设备ID: ${_deviceId.value}")
                    addLog("  - Tapro版本: ${_taproVersion.value}")
                }
            }

            override fun onDisconnected(reason: String) {
                viewModelScope.launch {
                    _isConnecting.value = false
                    updateConnectionStatus()
                    addLog("连接已断开")
                }
            }
            override fun onError(error: ConnectionError) {
                viewModelScope.launch {
                    _isConnecting.value = false
                    addLog("连接失败: ${error.message}")
                    addLog("  - 错误码: ${error.code}")
                }
            }
        })
    }

    /**
     * 断开连接
     */
    fun disconnectFromDevice() {
        TaplinkSDK.disconnect()
        updateConnectionStatus()
        addLog("已断开连接")
    }

    /**
     * 保存交易ID
     */
    fun saveTransactionId(transactionId: String?) {
        _lastTransactionId.value = transactionId
    }

    /**
     * 保存交易请求ID
     */
    fun saveTransactionRequestId(transactionRequestId: String?) {
        _lastTransactionRequestId.value = transactionRequestId
    }

    /**
     * 保存授权交易ID
     */
    fun saveAuthTransactionId(authTransactionId: String?) {
        _lastAuthTransactionId.value = authTransactionId
    }
}

