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
 * 退款交易页面
 * 
 * 支持用户输入原交易ID和退款金额并发起退款交易
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundTransactionPage(
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
    var refundAmount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("Customer request") }
    var isProcessing by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("退款交易 (REFUND)") },
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
                placeholder = { Text("请输入要退款的交易ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isProcessing
            )
            
            // 退款金额输入
            OutlinedTextField(
                value = refundAmount,
                onValueChange = { refundAmount = it },
                label = { Text("退款金额 (USD) *") },
                placeholder = { Text("请输入退款金额") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !isProcessing
            )
            
            // 退款原因输入
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("退款原因") },
                placeholder = { Text("请输入退款原因") },
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
                    
                    // 验证退款金额格式
                    val amountValue = try {
                        BigDecimal(refundAmount)
                    } catch (e: Exception) {
                        viewModel.addLog("错误: 退款金额格式不正确，请输入有效的数字")
                        return@Button
                    }
                    
                    if (amountValue <= BigDecimal.ZERO) {
                        viewModel.addLog("错误: 退款金额必须大于0")
                        return@Button
                    }
                    
                    isProcessing = true
                    performRefund(
                        transactionId = transactionId,
                        refundAmount = amountValue,
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
                Text(if (isProcessing) "处理中..." else "发起退款")
            }
            
            // 说明文字
            Text(
                text = "退款交易用于退还已完成的交易金额，支持全额或部分退款",
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
 * 执行退款交易
 */
private fun performRefund(
    transactionId: String,
    refundAmount: BigDecimal,
    reason: String,
    viewModel: TaplinkDemoViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onComplete: () -> Unit
) {
    viewModel.addLog("发起退款交易，退款金额: $$refundAmount")
    
    val merchantOrderNo = "REFUND_${System.currentTimeMillis()}"
    val merchantRefundNo = "REFUND_NO_${System.currentTimeMillis()}"
    val transactionRequestId = UUID.randomUUID().toString().replace("-", "")
    
    val amountInfo = AmountInfo(
        orderAmount = refundAmount,
        pricingCurrency = "USD"
    )
    
    val request = PaymentRequest(
        action = "REFUND",
        merchantOrderNo = merchantOrderNo,
        transactionRequestId = transactionRequestId,
        description = "退款交易",
        amount = amountInfo,
        originalTransactionId = transactionId,
        merchantRefundNo = merchantRefundNo,
        reason = reason
    )
    
    TaplinkSDK.execute(request, object : PaymentCallback {
        override fun onProgress(event: PaymentEvent) {
            viewModel.addLog("退款进度: ${event.eventMsg}")
        }
        
        override fun onSuccess(result: PaymentResult) {
            viewModel.addLog("退款响应成功!")
            viewModel.addLog("  - 交易ID: ${result.transactionId}")
            viewModel.addLog("  - 商户订单号: ${result.merchantOrderNo}")
            viewModel.addLog("  - 交易状态: ${result.transactionStatus}")
            viewModel.addLog("  - 交易信息: ${result.transactionResultMsg}")
            result.amount?.let { amt ->
                viewModel.addLog("  - 退款金额: ${amt.orderAmount} ${amt.priceCurrency}")
            }
            onComplete()
        }
        
        override fun onFailure(error: PaymentError) {
            viewModel.addLog("退款失败: ${error.message}")
            viewModel.addLog("  - 错误码: ${error.code}")
            onComplete()
        }
    })
}

