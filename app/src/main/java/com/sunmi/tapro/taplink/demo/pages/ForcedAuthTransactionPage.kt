package com.sunmi.tapro.taplink.demo.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sunmi.tapro.taplink.demo.components.LogConsole
import com.sunmi.tapro.taplink.demo.viewmodel.TaplinkDemoViewModel
import com.sunmi.tapro.taplink.sdk.TaplinkSDK
import com.sunmi.tapro.taplink.sdk.callback.PaymentCallback
import com.sunmi.tapro.taplink.sdk.error.PaymentError
import com.sunmi.tapro.taplink.sdk.model.common.AmountInfo
import com.sunmi.tapro.taplink.sdk.model.common.PaymentEvent
import com.sunmi.tapro.taplink.sdk.model.request.PaymentRequest
import com.sunmi.tapro.taplink.sdk.model.response.PaymentResult
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.math.BigDecimal
import java.util.UUID

/**
 * 强制授权页面
 * 
 * 支持用户输入授权金额和授权码并发起强制授权交易
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForcedAuthTransactionPage(
    navController: NavController,
    viewModel: TaplinkDemoViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val isConnected by viewModel.isConnected.collectAsState()
    val logMessages by viewModel.logMessages.collectAsState()
    
    // 输入状态
    var amount by remember { mutableStateOf("500.00") }
    var authCode by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("强制授权交易") }
    var isProcessing by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("强制授权 (FORCED_AUTH)") },
                navigationIcon = {
                    Button(onClick = { navController.popBackStack() }) {
                        Text("返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 授权金额输入
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("授权金额 (USD) *") },
                placeholder = { Text("请输入授权金额") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !isProcessing
            )
            
            // 授权码输入
            OutlinedTextField(
                value = authCode,
                onValueChange = { authCode = it },
                label = { Text("授权码 *") },
                placeholder = { Text("请输入授权码") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isProcessing
            )
            
            // 订单描述输入
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("订单描述") },
                placeholder = { Text("请输入订单描述") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isProcessing
            )
            
            // 执行按钮
            Button(
                onClick = {
                    if (!isConnected) {
                        viewModel.addLog("错误: 请先连接设备")
                        return@Button
                    }
                    
                    if (authCode.isBlank()) {
                        viewModel.addLog("错误: 请输入授权码")
                        return@Button
                    }
                    
                    // 验证授权金额格式
                    val amountValue = try {
                        BigDecimal(amount)
                    } catch (e: Exception) {
                        viewModel.addLog("错误: 授权金额格式不正确，请输入有效的数字")
                        return@Button
                    }
                    
                    if (amountValue <= BigDecimal.ZERO) {
                        viewModel.addLog("错误: 授权金额必须大于0")
                        return@Button
                    }
                    
                    isProcessing = true
                    performForcedAuth(
                        amount = amountValue,
                        authCode = authCode,
                        description = description,
                        viewModel = viewModel,
                        scope = scope,
                        onComplete = {
                            isProcessing = false
                            // 不自动返回，让用户查看日志
                        }
                    )
                },
                enabled = isConnected && !isProcessing && authCode.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isProcessing) "处理中..." else "发起强制授权")
            }
            
            // 说明文字
            Text(
                text = "* 必填字段\n强制授权用于在已获得语音授权码的情况下进行授权交易",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 日志控制台
            LogConsole(
                logMessages = logMessages,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

/**
 * 执行强制授权
 */
private fun performForcedAuth(
    amount: BigDecimal,
    authCode: String,
    description: String,
    viewModel: TaplinkDemoViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onComplete: () -> Unit
) {
    viewModel.addLog("发起强制授权，金额: $$amount, 授权码: $authCode")
    
    val merchantOrderNo = "FORCED_AUTH_${System.currentTimeMillis()}"
    val transactionRequestId = UUID.randomUUID().toString().replace("-", "")
    
    val amountInfo = AmountInfo(
        orderAmount = amount,
        pricingCurrency = "USD"
    )
    
    val request = PaymentRequest(
        action = "FORCED_AUTH",
        merchantOrderNo = merchantOrderNo,
        transactionRequestId = transactionRequestId,
        description = description,
        amount = amountInfo,
        forcedAuthCode = authCode
    )
    
    TaplinkSDK.execute(request, object : PaymentCallback {
        override fun onProgress(event: PaymentEvent) {
            viewModel.addLog("强制授权进度: ${event.eventMsg}")
        }
        
        override fun onSuccess(result: PaymentResult) {
            viewModel.addLog("强制授权成功!")
            viewModel.addLog("  - 交易ID: ${result.transactionId}")
            viewModel.addLog("  - 商户订单号: ${result.merchantOrderNo}")
            viewModel.addLog("  - 交易状态: ${result.transactionStatus}")
            viewModel.addLog("  - 交易信息: ${result.transactionResultMsg}")
            result.amount?.let { amt ->
                viewModel.addLog("  - 授权金额: ${amt.orderAmount} ${amt.priceCurrency}")
            }
            // 保存交易ID和授权交易ID
            result.transactionId?.let {
                viewModel.saveTransactionId(it)
                viewModel.saveAuthTransactionId(it)
            }
            onComplete()
        }
        
        override fun onFailure(error: PaymentError) {
            viewModel.addLog("强制授权失败: ${error.message}")
            viewModel.addLog("  - 错误码: ${error.code}")
            onComplete()
        }
    })
}

