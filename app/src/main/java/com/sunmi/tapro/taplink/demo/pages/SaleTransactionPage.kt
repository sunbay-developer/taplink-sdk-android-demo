package com.sunmi.tapro.taplink.demo.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.sunmi.tapro.log.GLog
import com.sunmi.tapro.taplink.demo.components.LogConsole
import com.sunmi.tapro.taplink.demo.viewmodel.TaplinkDemoViewModel
import com.sunmi.tapro.taplink.sdk.TaplinkSDK
import com.sunmi.tapro.taplink.sdk.callback.PaymentCallback
import com.sunmi.tapro.taplink.sdk.error.PaymentError
import com.sunmi.tapro.taplink.sdk.model.common.AmountInfo
import com.sunmi.tapro.taplink.sdk.model.common.PaymentCategory
import com.sunmi.tapro.taplink.sdk.model.common.PaymentEvent
import com.sunmi.tapro.taplink.sdk.model.common.PaymentMethodId
import com.sunmi.tapro.taplink.sdk.model.common.PaymentMethodInfo
import com.sunmi.tapro.taplink.sdk.model.request.PaymentRequest
import com.sunmi.tapro.taplink.sdk.model.response.PaymentResult
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.math.BigDecimal
import java.util.UUID

/**
 * 销售交易页面
 *
 * 支持用户输入金额并发起销售交易
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleTransactionPage(
    navController: NavController,
    viewModel: TaplinkDemoViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val isConnected by viewModel.isConnected.collectAsState()
    val logMessages by viewModel.logMessages.collectAsState()

    // 输入状态
    var orderAmount by remember { mutableStateOf("100.00") }
    var tipAmount by remember { mutableStateOf("") }
    var taxAmount by remember { mutableStateOf("") }
    var surchargeAmount by remember { mutableStateOf("") }
    var cashbackAmount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("Taplink Demo Sale Transaction") }
    var isProcessing by remember { mutableStateOf(false) }

    // 支付方式选择
    var selectedPaymentCategory by remember { mutableStateOf(PaymentCategory.CARD_CREDIT) }
    var selectedSubPaymentId by remember { mutableStateOf<PaymentMethodId?>(null) }
    var showPaymentMethodMenu by remember { mutableStateOf(false) }
    var showSubPaymentMenu by remember { mutableStateOf(false) }

    // EBT-VOUCHER 特定字段
    var voucherNumber by remember { mutableStateOf("") }
    var voucherApprovalCode by remember { mutableStateOf("") }

    // 支付方式选项
    val paymentCategories = PaymentCategory.values().toList()
    val ebtSubPaymentIds = listOf(
        null,
        PaymentMethodId.SNAP,
        PaymentMethodId.VOUCHER,
        PaymentMethodId.WITHDRAW,
        PaymentMethodId.BENEFIT
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("销售交易 (SALE)") },
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
        ) {
            // 输入区域（可滚动）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 支付方式选择（一行两个）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = showPaymentMethodMenu,
                        onExpandedChange = {
                            if (!isProcessing) {
                                showPaymentMethodMenu = !showPaymentMethodMenu
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedPaymentCategory.toApiString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("支付方式") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPaymentMethodMenu) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            enabled = !isProcessing,
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = showPaymentMethodMenu,
                            onDismissRequest = { showPaymentMethodMenu = false }
                        ) {
                            paymentCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.toApiString()) },
                                    onClick = {
                                        selectedPaymentCategory = category
                                        // 如果不是EBT，清除子支付方式
                                        if (category != PaymentCategory.EBT) {
                                            selectedSubPaymentId = null
                                        }
                                        showPaymentMethodMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 如果选择了EBT，显示子支付方式选择
                    if (selectedPaymentCategory == PaymentCategory.EBT) {
                        ExposedDropdownMenuBox(
                            expanded = showSubPaymentMenu,
                            onExpandedChange = {
                                if (!isProcessing) {
                                    showSubPaymentMenu = !showSubPaymentMenu
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedSubPaymentId?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("EBT 具体支付方式 *") },
                                placeholder = { Text("请选择") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSubPaymentMenu) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                enabled = !isProcessing,
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = showSubPaymentMenu,
                                onDismissRequest = { showSubPaymentMenu = false }
                            ) {
                                ebtSubPaymentIds.forEach { subId ->
                                    DropdownMenuItem(
                                        text = { Text(subId?.name ?: "不选择") },
                                        onClick = {
                                            selectedSubPaymentId = subId
                                            showSubPaymentMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 如果选择了 EBT-VOUCHER，显示 Voucher 特定字段
                if (selectedPaymentCategory == PaymentCategory.EBT &&
                    selectedSubPaymentId == PaymentMethodId.VOUCHER
                ) {

                    // Voucher 编号输入
                    OutlinedTextField(
                        value = voucherNumber,
                        onValueChange = { voucherNumber = it },
                        label = { Text("Voucher 编号 *") },
                        placeholder = { Text("请输入 Voucher 编号") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isProcessing
                    )

                    // Voucher 授权码输入
                    OutlinedTextField(
                        value = voucherApprovalCode,
                        onValueChange = { voucherApprovalCode = it },
                        label = { Text("Voucher 授权码 *") },
                        placeholder = { Text("请输入 Voucher 授权码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isProcessing
                    )
                }

                // 订单金额输入（必填）
                OutlinedTextField(
                    value = orderAmount,
                    onValueChange = { orderAmount = it },
                    label = { Text("订单金额 (USD) *") },
                    placeholder = { Text("请输入订单金额") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = !isProcessing
                )

                // 小费金额和税额输入（一行两个）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tipAmount,
                        onValueChange = { tipAmount = it },
                        label = { Text("小费金额 (USD)") },
                        placeholder = { Text("可选") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = !isProcessing
                    )
                    OutlinedTextField(
                        value = taxAmount,
                        onValueChange = { taxAmount = it },
                        label = { Text("税额 (USD)") },
                        placeholder = { Text("可选") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = !isProcessing
                    )
                }

                // 附加费和现金返还输入（一行两个）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = surchargeAmount,
                        onValueChange = { surchargeAmount = it },
                        label = { Text("附加费 (USD)") },
                        placeholder = { Text("可选") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = !isProcessing
                    )
                    OutlinedTextField(
                        value = cashbackAmount,
                        onValueChange = { cashbackAmount = it },
                        label = { Text("现金返还 (USD)") },
                        placeholder = { Text("可选") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = !isProcessing
                    )
                }

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

                        // 验证订单金额格式
                        val orderAmountValue = try {
                            BigDecimal(orderAmount)
                        } catch (e: Exception) {
                            viewModel.addLog("错误: 订单金额格式不正确，请输入有效的数字")
                            return@Button
                        }

                        if (orderAmountValue <= BigDecimal.ZERO) {
                            viewModel.addLog("错误: 订单金额必须大于0")
                            return@Button
                        }

                        // 解析可选金额字段
                        val tipValue = tipAmount.takeIf { it.isNotBlank() }?.let {
                            try {
                                BigDecimal(it)
                            } catch (e: Exception) {
                                viewModel.addLog("错误: 小费金额格式不正确")
                                return@Button
                            }
                        }

                        val taxValue = taxAmount.takeIf { it.isNotBlank() }?.let {
                            try {
                                BigDecimal(it)
                            } catch (e: Exception) {
                                viewModel.addLog("错误: 税额格式不正确")
                                return@Button
                            }
                        }

                        val surchargeValue = surchargeAmount.takeIf { it.isNotBlank() }?.let {
                            try {
                                BigDecimal(it)
                            } catch (e: Exception) {
                                viewModel.addLog("错误: 附加费格式不正确")
                                return@Button
                            }
                        }

                        val cashbackValue = cashbackAmount.takeIf { it.isNotBlank() }?.let {
                            try {
                                BigDecimal(it)
                            } catch (e: Exception) {
                                viewModel.addLog("错误: 现金返还格式不正确")
                                return@Button
                            }
                        }

                        // EBT支付方式的子支付方式是可选的（可以不选择）

                        isProcessing = true
                        performSale(
                            orderAmount = orderAmountValue,
                            tipAmount = tipValue,
                            taxAmount = taxValue,
                            surchargeAmount = surchargeValue,
                            cashbackAmount = cashbackValue,
                            paymentCategory = selectedPaymentCategory,
                            subPaymentId = selectedSubPaymentId,
                            voucherNumber = voucherNumber.takeIf { it.isNotBlank() },
                            voucherApprovalCode = voucherApprovalCode.takeIf { it.isNotBlank() },
                            description = description,
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
                    Text(if (isProcessing) "处理中..." else "发起销售交易")
                }

                // 说明文字
                Text(
                    text = "* 必填字段\n销售交易用于刷卡消费，支持信用卡、借记卡等支付方式\n可选字段留空则默认为0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 日志控制台（固定高度）
            LogConsole(
                logMessages = logMessages,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * 执行销售交易
 */
private fun performSale(
    orderAmount: BigDecimal,
    tipAmount: BigDecimal?,
    taxAmount: BigDecimal?,
    surchargeAmount: BigDecimal?,
    cashbackAmount: BigDecimal?,
    paymentCategory: PaymentCategory,
    subPaymentId: PaymentMethodId?,
    voucherNumber: String?,
    voucherApprovalCode: String?,
    description: String,
    viewModel: TaplinkDemoViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onComplete: () -> Unit
) {
    // 构建日志信息
    val logParts = mutableListOf("订单金额: $$orderAmount")
    tipAmount?.let { logParts.add("小费: $$it") }
    taxAmount?.let { logParts.add("税额: $$it") }
    surchargeAmount?.let { logParts.add("附加费: $$it") }
    cashbackAmount?.let { logParts.add("现金返还: $$it") }
    val paymentInfo =
        if (subPaymentId != null) "${paymentCategory.toApiString()}-${subPaymentId.toApiString()}" else paymentCategory.toApiString()
    viewModel.addLog("发起销售交易 [$paymentInfo] - ${logParts.joinToString(", ")}")

    // 生成订单号和交易请求ID
    val merchantOrderNo = "ORDER_${System.currentTimeMillis()}"
    val transactionRequestId = UUID.randomUUID().toString().replace("-", "")

    // 构建金额信息
    val amountInfo = AmountInfo(
        orderAmount = orderAmount,
        pricingCurrency = "USD",
        tipAmount = tipAmount,
        taxAmount = taxAmount,
        surchargeAmount = surchargeAmount,
        cashbackAmount = cashbackAmount
    )

    // 构建支付方式信息
    val paymentMethodInfo = PaymentMethodInfo(
        category = paymentCategory,
        id = subPaymentId
    )

    // 构建支付请求
    val request = PaymentRequest(
        action = "SALE",
        merchantOrderNo = merchantOrderNo,
        transactionRequestId = transactionRequestId,
        description = description,
        amount = amountInfo,
        paymentMethod = paymentMethodInfo,
        voucherNumber = voucherNumber,
        voucherApprovalCode = voucherApprovalCode
    )

    // 保存交易请求ID用于后续查询
    viewModel.saveTransactionRequestId(transactionRequestId)

    TaplinkSDK.execute(request, object : PaymentCallback {
        override fun onProgress(event: PaymentEvent) {
            viewModel.addLog("交易进度: ${event.eventMsg}")
        }

        override fun onSuccess(result: PaymentResult) {
            viewModel.addLog("交易成功!")
            viewModel.addLog("  - 交易ID: ${result.transactionId}")
            viewModel.addLog("  - 商户订单号: ${result.merchantOrderNo}")
            viewModel.addLog("  - 交易状态: ${result.transactionStatus}")
            viewModel.addLog("  - 交易信息: ${result.transactionResultMsg}")
            result.amount?.let { amt ->
                viewModel.addLog("  - 订单金额: ${amt.orderAmount} ${amt.priceCurrency}")
                viewModel.addLog("  - 交易金额: ${amt.transAmount} ${amt.priceCurrency}")
                amt.tipAmount?.let { viewModel.addLog("  - 小费: $it ${amt.priceCurrency}") }
                amt.taxAmount?.let { viewModel.addLog("  - 税额: $it ${amt.priceCurrency}") }
                amt.surchargeAmount?.let { viewModel.addLog("  - 附加费: $it ${amt.priceCurrency}") }
                amt.cashbackAmount?.let { viewModel.addLog("  - 现金返还: $it ${amt.priceCurrency}") }
            }
            // 保存交易ID用于查询
            result.transactionId?.let { viewModel.saveTransactionId(it) }
            onComplete()
        }

        override fun onFailure(error: PaymentError) {
            GLog.e("SaleTransaction", "Payment failed:$error")
            viewModel.addLog("交易失败: ${error.message}")
            viewModel.addLog("  - 错误码: ${error.code}")
            onComplete()
        }
    })
}

