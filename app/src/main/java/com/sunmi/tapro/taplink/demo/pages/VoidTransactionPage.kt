package com.sunmi.tapro.taplink.demo.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
 * 撤销交易页面
 * 
 * 支持用户输入原交易ID并发起撤销交易
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoidTransactionPage(
    navController: NavController,
    viewModel: TaplinkDemoViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val isConnected by viewModel.isConnected.collectAsState()
    val lastTransactionId by viewModel.lastTransactionId.collectAsState()
    val logMessages by viewModel.logMessages.collectAsState()
    
    // 输入状态
    var transactionId by remember(lastTransactionId) { 
        mutableStateOf(lastTransactionId ?: "") 
    }
    var reason by remember { mutableStateOf("Demo Void Transaction") }
    var isProcessing by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("撤销交易 (VOID)") },
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
            // 原交易ID输入
            OutlinedTextField(
                value = transactionId,
                onValueChange = { transactionId = it },
                label = { Text("原交易ID") },
                placeholder = { Text("请输入要撤销的交易ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isProcessing
            )
            
            // 撤销原因输入
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("撤销原因") },
                placeholder = { Text("请输入撤销原因") },
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
                    
                    if (transactionId.isBlank()) {
                        viewModel.addLog("错误: 请输入原交易ID")
                        return@Button
                    }
                    
                    isProcessing = true
                    performVoid(
                        transactionId = transactionId,
                        reason = reason,
                        viewModel = viewModel,
                        scope = scope,
                        onComplete = {
                            isProcessing = false
                            // 不自动返回，让用户查看日志
                        }
                    )
                },
                enabled = isConnected && !isProcessing && transactionId.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isProcessing) "处理中..." else "发起撤销")
            }
            
            // 说明文字
            Text(
                text = "撤销交易用于取消当天的交易，仅限当前批次内的交易",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (lastTransactionId != null) {
                Text(
                    text = "提示: 已自动填充最后一笔交易ID",
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
 * 执行撤销交易
 */
private fun performVoid(
    transactionId: String,
    reason: String,
    viewModel: TaplinkDemoViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onComplete: () -> Unit
) {
    viewModel.addLog("发起撤销交易，原交易ID: $transactionId")
    
    val merchantOrderNo = "VOID_${System.currentTimeMillis()}"
    val transactionRequestId = UUID.randomUUID().toString().replace("-", "")
    
    // VOID 不需要金额，但 PaymentRequest 要求 amount 参数，使用最小金额
    val amountInfo = AmountInfo(
        orderAmount = BigDecimal.ZERO,
        pricingCurrency = "USD"
    )
    
    val request = PaymentRequest(
        action = "VOID",
        merchantOrderNo = merchantOrderNo,
        transactionRequestId = transactionRequestId,
        description = "撤销交易",
        amount = amountInfo,
        originalTransactionId = transactionId,
        reason = reason
    )
    
    TaplinkSDK.execute(request, object : PaymentCallback {
        override fun onProgress(event: PaymentEvent) {
            viewModel.addLog("撤销进度: ${event.eventMsg}")
        }
        
        override fun onSuccess(result: PaymentResult) {
            viewModel.addLog("撤销响应成功!")
            viewModel.addLog("  - 交易ID: ${result.transactionId}")
            viewModel.addLog("  - 商户订单号: ${result.merchantOrderNo}")
            viewModel.addLog("  - 交易状态: ${result.transactionStatus}")
            viewModel.addLog("  - 交易信息: ${result.transactionResultMsg}")
            onComplete()
        }
        
        override fun onFailure(error: PaymentError) {
            viewModel.addLog("撤销失败: ${error.message}")
            viewModel.addLog("  - 错误码: ${error.code}")
            when (error.code) {
                "C12" -> viewModel.addLog("  - 订单不存在，请检查交易ID")
                "C11" -> viewModel.addLog("  - 订单已关闭，这是前一天的交易，请使用 REFUND 代替")
            }
            onComplete()
        }
    })
}

