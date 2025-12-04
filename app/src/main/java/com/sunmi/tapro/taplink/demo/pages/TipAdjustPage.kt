package com.sunmi.tapro.taplink.demo.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
 * 小费调整页面
 * 
 * 支持用户输入原交易ID和小费金额，并提供快捷百分比按钮
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipAdjustPage(
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
    var tipAmount by remember { mutableStateOf("15.00") }
    var isProcessing by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("小费调整 (TIP_ADJUST)") },
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
                placeholder = { Text("请输入原交易ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isProcessing
            )
            
            // 小费金额输入
            OutlinedTextField(
                value = tipAmount,
                onValueChange = { tipAmount = it },
                label = { Text("小费金额 (USD)") },
                placeholder = { Text("请输入小费金额") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !isProcessing
            )
            
            // 快捷小费百分比按钮
            Text(
                text = "快捷小费金额:",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { tipAmount = "10.00" },
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("$10")
                }
                OutlinedButton(
                    onClick = { tipAmount = "15.00" },
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("$15")
                }
                OutlinedButton(
                    onClick = { tipAmount = "20.00" },
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("$20")
                }
                OutlinedButton(
                    onClick = { tipAmount = "25.00" },
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("$25")
                }
            }
            
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
                    
                    // 验证小费金额格式
                    val tipValue = try {
                        BigDecimal(tipAmount)
                    } catch (e: Exception) {
                        viewModel.addLog("错误: 小费金额格式不正确，请输入有效的数字")
                        return@Button
                    }
                    
                    if (tipValue < BigDecimal.ZERO) {
                        viewModel.addLog("错误: 小费金额不能为负数")
                        return@Button
                    }
                    
                    isProcessing = true
                    performTipAdjust(
                        transactionId = transactionId,
                        tipAmount = tipValue,
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
                Text(if (isProcessing) "处理中..." else "调整小费")
            }
            
            // 说明文字
            Text(
                text = "小费调整用于修改已完成交易的小费金额",
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
 * 执行小费调整
 */
private fun performTipAdjust(
    transactionId: String,
    tipAmount: BigDecimal,
    viewModel: TaplinkDemoViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onComplete: () -> Unit
) {
    viewModel.addLog("发起小费调整，小费: $$tipAmount")
    
    val merchantOrderNo = "TIP_${System.currentTimeMillis()}"
    val transactionRequestId = UUID.randomUUID().toString().replace("-", "")
    
    // TIP_ADJUST 不需要新的订单金额，但 PaymentRequest 要求 amount 参数，使用最小金额
    val amountInfo = AmountInfo(
        orderAmount = BigDecimal.ZERO,
        pricingCurrency = "USD"
    )
    
    val request = PaymentRequest(
        action = "TIP_ADJUST",
        merchantOrderNo = merchantOrderNo,
        transactionRequestId = transactionRequestId,
        description = "小费调整",
        amount = amountInfo,
        originalTransactionId = transactionId,
        tipAmount = tipAmount
    )
    
    TaplinkSDK.execute(request, object : PaymentCallback {
        override fun onProgress(event: PaymentEvent) {
            viewModel.addLog("小费调整进度: ${event.eventMsg}")
        }
        
        override fun onSuccess(result: PaymentResult) {
            viewModel.addLog("小费调整成功!")
            viewModel.addLog("  - 交易ID: ${result.transactionId}")
            viewModel.addLog("  - 商户订单号: ${result.merchantOrderNo}")
            result.amount?.let { amt ->
                viewModel.addLog("  - 小费金额: ${amt.tipAmount} ${amt.priceCurrency}")
                viewModel.addLog("  - 总金额: ${amt.transAmount} ${amt.priceCurrency}")
            }
            onComplete()
        }
        
        override fun onFailure(error: PaymentError) {
            viewModel.addLog("小费调整失败: ${error.message}")
            viewModel.addLog("  - 错误码: ${error.code}")
            onComplete()
        }
    })
}

