package com.sunmi.tapro.taplink.demo.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sunmi.tapro.taplink.demo.components.LogConsole
import com.sunmi.tapro.taplink.demo.viewmodel.TaplinkDemoViewModel
import com.sunmi.tapro.taplink.sdk.TaplinkSDK
import com.sunmi.tapro.taplink.sdk.callback.PaymentCallback
import com.sunmi.tapro.taplink.sdk.error.PaymentError
import com.sunmi.tapro.taplink.sdk.model.common.PaymentEvent
import com.sunmi.tapro.taplink.sdk.model.request.QueryRequest
import com.sunmi.tapro.taplink.sdk.model.response.PaymentResult
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * 查询交易页面
 * 
 * 支持通过交易ID或交易请求ID查询交易状态
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueryTransactionPage(
    navController: NavController,
    viewModel: TaplinkDemoViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val isConnected by viewModel.isConnected.collectAsState()
    val lastTransactionId by viewModel.lastTransactionId.collectAsState()
    val lastTransactionRequestId by viewModel.lastTransactionRequestId.collectAsState()
    val logMessages by viewModel.logMessages.collectAsState()
    
    // 查询类型：0=交易ID, 1=交易请求ID
    var queryType by remember { mutableStateOf(0) }
    
    // 输入状态
    var transactionId by remember(lastTransactionId) { 
        mutableStateOf(lastTransactionId ?: "") 
    }
    var transactionRequestId by remember(lastTransactionRequestId) { 
        mutableStateOf(lastTransactionRequestId ?: "") 
    }
    var isProcessing by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("查询交易 (QUERY)") },
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
            // 查询类型选择
            Text(
                text = "查询方式:",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = queryType == 0,
                    onClick = { queryType = 0 },
                    enabled = !isProcessing
                )
                Text("交易ID", modifier = Modifier.padding(end = 16.dp))
                
                RadioButton(
                    selected = queryType == 1,
                    onClick = { queryType = 1 },
                    enabled = !isProcessing
                )
                Text("交易请求ID")
            }
            
            // 根据查询类型显示不同的输入框
            if (queryType == 0) {
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = { transactionId = it },
                    label = { Text("交易ID") },
                    placeholder = { Text("请输入交易ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isProcessing
                )
            } else {
                OutlinedTextField(
                    value = transactionRequestId,
                    onValueChange = { transactionRequestId = it },
                    label = { Text("交易请求ID") },
                    placeholder = { Text("请输入交易请求ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isProcessing
                )
            }
            
            // 执行按钮
            Button(
                onClick = {
                    if (!isConnected) {
                        viewModel.addLog("错误: 请先连接设备")
                        return@Button
                    }
                    
                    val queryRequest = if (queryType == 0) {
                        if (transactionId.isBlank()) {
                            viewModel.addLog("错误: 请输入交易ID")
                            return@Button
                        }
                        QueryRequest.byTransactionId(transactionId)
                    } else {
                        if (transactionRequestId.isBlank()) {
                            viewModel.addLog("错误: 请输入交易请求ID")
                            return@Button
                        }
                        QueryRequest.byTransactionRequestId(transactionRequestId)
                    }
                    
                    isProcessing = true
                    performQuery(
                        queryRequest = queryRequest,
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
                Text(if (isProcessing) "查询中..." else "查询交易")
            }
            
            // 说明文字
            Text(
                text = "查询交易用于获取交易的当前状态和详细信息",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (lastTransactionId != null || lastTransactionRequestId != null) {
                Text(
                    text = "提示: 已自动填充最后一笔交易的ID",
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
 * 执行查询交易
 */
private fun performQuery(
    queryRequest: QueryRequest,
    viewModel: TaplinkDemoViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onComplete: () -> Unit
) {
    viewModel.addLog("发起交易查询...")
    
    TaplinkSDK.query(queryRequest, object : PaymentCallback {
        override fun onProgress(event: PaymentEvent) {
            viewModel.addLog("查询进度: ${event.eventMsg}")
        }
        
        override fun onSuccess(result: PaymentResult) {
            viewModel.addLog("查询成功!")
            viewModel.addLog("  - 交易ID: ${result.transactionId}")
            viewModel.addLog("  - 交易请求ID: ${result.transactionRequestId}")
            viewModel.addLog("  - 商户订单号: ${result.merchantOrderNo}")
            viewModel.addLog("  - 交易状态: ${result.transactionStatus}")
            viewModel.addLog("  - 交易信息: ${result.transactionResultMsg}")
            result.amount?.let { amt ->
                viewModel.addLog("  - 订单金额: ${amt.orderAmount} ${amt.priceCurrency}")
            }
            onComplete()
        }
        
        override fun onFailure(error: PaymentError) {
            viewModel.addLog("查询失败: ${error.message}")
            viewModel.addLog("  - 错误码: ${error.code}")
            onComplete()
        }
    })
}

