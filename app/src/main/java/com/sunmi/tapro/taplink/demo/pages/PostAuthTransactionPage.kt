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
 * 预授权完成页面
 * 
 * 支持用户输入原授权交易ID和完成金额并发起预授权完成交易
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostAuthTransactionPage(
    navController: NavController,
    viewModel: TaplinkDemoViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val isConnected by viewModel.isConnected.collectAsState()
    val lastAuthTransactionId by viewModel.lastAuthTransactionId.collectAsState()
    val logMessages by viewModel.logMessages.collectAsState()
    
    // 输入状态
    var authTransactionId by remember(lastAuthTransactionId) { 
        mutableStateOf(lastAuthTransactionId ?: "") 
    }
    var completionAmount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("预授权完成") }
    var isProcessing by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("预授权完成 (POST_AUTH)") },
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
            // 原授权交易ID输入
            OutlinedTextField(
                value = authTransactionId,
                onValueChange = { authTransactionId = it },
                label = { Text("原授权交易ID") },
                placeholder = { Text("请输入原授权交易ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isProcessing
            )
            
            // 完成金额输入
            OutlinedTextField(
                value = completionAmount,
                onValueChange = { completionAmount = it },
                label = { Text("完成金额 (USD) *") },
                placeholder = { Text("请输入完成金额") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    
                    if (authTransactionId.isBlank()) {
                        viewModel.addLog("错误: 请输入原授权交易ID")
                        return@Button
                    }
                    
                    // 验证完成金额格式
                    val amountValue = try {
                        BigDecimal(completionAmount)
                    } catch (e: Exception) {
                        viewModel.addLog("错误: 完成金额格式不正确，请输入有效的数字")
                        return@Button
                    }
                    
                    if (amountValue <= BigDecimal.ZERO) {
                        viewModel.addLog("错误: 完成金额必须大于0")
                        return@Button
                    }
                    
                    isProcessing = true
                    performPostAuth(
                        authTransactionId = authTransactionId,
                        completionAmount = amountValue,
                        description = description,
                        viewModel = viewModel,
                        scope = scope,
                        onComplete = {
                            isProcessing = false
                            // 不自动返回，让用户查看日志
                        }
                    )
                },
                enabled = isConnected && !isProcessing && authTransactionId.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isProcessing) "处理中..." else "完成预授权")
            }
            
            // 说明文字
            Text(
                text = "预授权完成用于完成之前的预授权交易，实际扣款",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (lastAuthTransactionId != null) {
                Text(
                    text = "提示: 已自动填充最后一笔授权交易ID",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
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
 * 执行预授权完成
 */
private fun performPostAuth(
    authTransactionId: String,
    completionAmount: BigDecimal,
    description: String,
    viewModel: TaplinkDemoViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onComplete: () -> Unit
) {
    viewModel.addLog("发起预授权完成，完成金额: $$completionAmount")
    
    val merchantOrderNo = "POST_AUTH_${System.currentTimeMillis()}"
    val transactionRequestId = UUID.randomUUID().toString().replace("-", "")
    
    val amountInfo = AmountInfo(
        orderAmount = completionAmount,
        pricingCurrency = "USD"
    )
    
    val request = PaymentRequest(
        action = "POST_AUTH",
        merchantOrderNo = merchantOrderNo,
        transactionRequestId = transactionRequestId,
        description = description,
        originalTransactionId = authTransactionId,
        amount = amountInfo
    )
    
    TaplinkSDK.execute(request, object : PaymentCallback {
        override fun onProgress(event: PaymentEvent) {
            viewModel.addLog("预授权完成进度: ${event.eventMsg}")
        }
        
        override fun onSuccess(result: PaymentResult) {
            viewModel.addLog("预授权完成成功!")
            viewModel.addLog("  - 交易ID: ${result.transactionId}")
            viewModel.addLog("  - 商户订单号: ${result.merchantOrderNo}")
            viewModel.addLog("  - 交易状态: ${result.transactionStatus}")
            viewModel.addLog("  - 交易信息: ${result.transactionResultMsg}")
            result.amount?.let { amt ->
                viewModel.addLog("  - 完成金额: ${amt.orderAmount} ${amt.priceCurrency}")
            }
            onComplete()
        }
        
        override fun onFailure(error: PaymentError) {
            viewModel.addLog("预授权完成失败: ${error.message}")
            viewModel.addLog("  - 错误码: ${error.code}")
            onComplete()
        }
    })
}

