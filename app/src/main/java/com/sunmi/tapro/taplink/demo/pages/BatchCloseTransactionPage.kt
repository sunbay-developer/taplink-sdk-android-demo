package com.sunmi.tapro.taplink.demo.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
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
 * 批次关闭页面
 * 
 * 用于关闭当前批次并打印批次报表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchCloseTransactionPage(
    navController: NavController,
    viewModel: TaplinkDemoViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val isConnected by viewModel.isConnected.collectAsState()
    val logMessages by viewModel.logMessages.collectAsState()
    
    var isProcessing by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("批次关闭 (BATCH_CLOSE)") },
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
            // 警告提示卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚠️ 重要提示",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Text(
                        text = "批次关闭将结算当前批次的所有交易，此操作不可撤销。\n\n" +
                               "关闭后将生成批次报表，包含：\n" +
                               "• 交易笔数统计\n" +
                               "• 交易金额汇总\n" +
                               "• 各交易类型明细",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // 执行按钮
            Button(
                onClick = {
                    if (!isConnected) {
                        viewModel.addLog("错误: 请先连接设备")
                        return@Button
                    }
                    
                    isProcessing = true
                    performBatchClose(
                        viewModel = viewModel,
                        scope = scope,
                        onComplete = {
                            isProcessing = false
                            // 不自动返回，让用户查看日志
                        }
                    )
                },
                enabled = isConnected && !isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isProcessing) "处理中..." else "确认关闭批次")
            }
            
            // 说明文字
            Text(
                text = "批次关闭用于结算当天的所有交易，通常在营业日结束时执行",
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
 * 执行批次关闭
 */
private fun performBatchClose(
    viewModel: TaplinkDemoViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onComplete: () -> Unit
) {
    viewModel.addLog("发起批次关闭...")
    
    val merchantOrderNo = "BATCH_CLOSE_${System.currentTimeMillis()}"
    val transactionRequestId = UUID.randomUUID().toString().replace("-", "")
    
    // 批次关闭不需要金额，但 PaymentRequest 要求 amount 参数，使用最小金额
    val amountInfo = AmountInfo(
        orderAmount = BigDecimal.ZERO,
        pricingCurrency = "USD"
    )
    
    val request = PaymentRequest(
        action = "BATCH_CLOSE",
        merchantOrderNo = merchantOrderNo,
        transactionRequestId = transactionRequestId,
        description = "批次关闭",
        amount = amountInfo
    )
    
    TaplinkSDK.execute(request, object : PaymentCallback {
        override fun onProgress(event: PaymentEvent) {
            viewModel.addLog("批次关闭进度: ${event.eventMsg}")
        }
        
        override fun onSuccess(result: PaymentResult) {
            if (result.transactionStatus == "SUCCESS") {
                viewModel.addLog("批次关闭成功!")
                viewModel.addLog("  - 批次号: ${result.batchNo}")
                result.batchCloseInfo?.let { batchInfo ->
                    viewModel.addLog("  - 交易笔数: ${batchInfo.totalCount ?: 0}")
                    batchInfo.totalAmount?.let {
                        viewModel.addLog("  - 总金额: $it")
                    }
                    batchInfo.totalTip?.let {
                        viewModel.addLog("  - 总小费: $it")
                    }
                    batchInfo.totalTax?.let {
                        viewModel.addLog("  - 总税费: $it")
                    }
                    batchInfo.totalSurchargeAmount?.let {
                        viewModel.addLog("  - 总附加费: $it")
                    }
                    batchInfo.totalServiceFee?.let {
                        viewModel.addLog("  - 总服务费: $it")
                    }
                    batchInfo.cashDiscount?.let {
                        viewModel.addLog("  - 现金优惠: $it")
                    }
                    batchInfo.closeTime?.let {
                        viewModel.addLog("  - 关闭时间: $it")
                    }
                } ?: run {
                    viewModel.addLog("  - 批次信息: 无")
                }
            } else {
                viewModel.addLog("批次关闭失败!")
                viewModel.addLog(result.transactionResultMsg ?: "Unknown")
            }
            onComplete()
        }
        
        override fun onFailure(error: PaymentError) {
            viewModel.addLog("批次关闭失败: ${error.message}")
            viewModel.addLog("  - 错误码: ${error.code}")
            onComplete()
        }
    })
}

