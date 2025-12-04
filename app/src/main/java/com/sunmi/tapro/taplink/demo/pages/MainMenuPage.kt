package com.sunmi.tapro.taplink.demo.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sunmi.tapro.taplink.demo.components.LogConsole
import com.sunmi.tapro.taplink.demo.PAGE_AUTH
import com.sunmi.tapro.taplink.demo.PAGE_BATCH_CLOSE
import com.sunmi.tapro.taplink.demo.PAGE_FORCED_AUTH
import com.sunmi.tapro.taplink.demo.PAGE_POST_AUTH
import com.sunmi.tapro.taplink.demo.PAGE_QUERY
import com.sunmi.tapro.taplink.demo.PAGE_REFUND
import com.sunmi.tapro.taplink.demo.PAGE_SALE
import com.sunmi.tapro.taplink.demo.PAGE_TIP_ADJUST
import com.sunmi.tapro.taplink.demo.PAGE_VOID
import com.sunmi.tapro.taplink.demo.viewmodel.TaplinkDemoViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * 主菜单页面
 *
 * 显示连接状态、交易按钮和日志控制台
 */
@Composable
fun MainMenuPage(
    navController: NavController,
    viewModel: TaplinkDemoViewModel = koinViewModel()
) {
    val isConnected by viewModel.isConnected.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()
    val connectionMode by viewModel.connectionMode.collectAsState()
    val deviceId by viewModel.deviceId.collectAsState()
    val taproVersion by viewModel.taproVersion.collectAsState()
    val logMessages by viewModel.logMessages.collectAsState()
    val lastTransactionId by viewModel.lastTransactionId.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Text(
            text = "Taplink SDK Demo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 状态卡片
        StatusCard(
            isConnected = isConnected,
            connectionMode = connectionMode,
            deviceId = deviceId,
            taproVersion = taproVersion,
            modifier = Modifier.fillMaxWidth()
        )

        // 连接按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.connectToDevice() },
                enabled = !isConnecting && !isConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isConnecting) "连接中..." else "连接")
            }

            Button(
                onClick = { viewModel.disconnectFromDevice() },
                enabled = isConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text("断开")
            }
        }

        // 交易按钮组
        Text(
            text = "交易操作:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // 第一行：销售、预授权
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { navController.navigate(PAGE_SALE) },
                enabled = isConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text("销售交易")
            }

            Button(
                onClick = { navController.navigate(PAGE_AUTH) },
                enabled = isConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text("预授权")
            }
        }

        // 第二行：撤销、退款
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { navController.navigate(PAGE_VOID) },
                enabled = isConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text("撤销交易")
            }
            
            Button(
                onClick = { navController.navigate(PAGE_REFUND) },
                enabled = isConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text("退款交易")
            }
        }
        
        // 第三行：小费调整、预授权完成
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { navController.navigate(PAGE_TIP_ADJUST) },
                enabled = isConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text("小费调整")
            }
            
            Button(
                onClick = { navController.navigate(PAGE_POST_AUTH) },
                enabled = isConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text("预授权完成")
            }
        }
        
        // 第四行：强制授权、批次关闭
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { navController.navigate(PAGE_FORCED_AUTH) },
                enabled = isConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text("强制授权")
            }
            
            Button(
                onClick = { navController.navigate(PAGE_BATCH_CLOSE) },
                enabled = isConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text("批次关闭")
            }
        }
        
        // 第五行：查询、清空日志
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { navController.navigate(PAGE_QUERY) },
                enabled = isConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text("查询交易")
            }
            
            Button(
                onClick = { viewModel.clearLogs() },
                modifier = Modifier.weight(1f)
            ) {
                Text("清空日志")
            }
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

/**
 * 状态卡片组件
 */
@Composable
fun StatusCard(
    isConnected: Boolean,
    connectionMode: String?,
    deviceId: String?,
    taproVersion: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "连接状态",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "状态: ${if (isConnected) "已连接" else "未连接"}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (isConnected) {
                connectionMode?.let {
                    Text(
                        text = "连接模式: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                deviceId?.let {
                    Text(
                        text = "设备ID: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                taproVersion?.let {
                    Text(
                        text = "Tapro版本: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


