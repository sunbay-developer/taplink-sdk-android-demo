# Taplink Demo 重构总结

## 重构目标
将单页面 Demo 应用重构为多页面架构，每个交易类型使用独立页面，支持不同的输入参数。

## 新增文件结构

```
lib_taplink/taplink_demo/src/main/java/com/sunmi/tapro/taplink/demo/
├── MainActivity.kt                          # 简化为应用入口
├── TaplinkDemoApplication.kt                # 添加 Koin 依赖注入
├── Navigation.kt                            # 导航配置
├── pages/                                   # 页面目录
│   ├── MainMenuPage.kt                     # 主菜单页面
│   ├── SaleTransactionPage.kt              # 销售交易页面
│   ├── AuthTransactionPage.kt              # 预授权页面
│   ├── VoidTransactionPage.kt              # 撤销交易页面
│   ├── RefundTransactionPage.kt            # 退款交易页面
│   ├── TipAdjustPage.kt                    # 小费调整页面
│   ├── PostAuthTransactionPage.kt          # 预授权完成页面
│   ├── ForcedAuthTransactionPage.kt        # 强制授权页面
│   ├── BatchCloseTransactionPage.kt        # 批次关闭页面
│   └── QueryTransactionPage.kt             # 查询交易页面
└── viewmodel/                               # ViewModel 目录
    └── TaplinkDemoViewModel.kt             # 共享 ViewModel
```

## 各页面功能

### 1. MainMenuPage（主菜单页面）
- **功能**：
  - 显示连接状态卡片
  - 提供连接/断开按钮
  - 显示所有交易类型按钮
  - 显示日志控制台
- **特点**：
  - 集中管理连接状态
  - 统一的日志显示
  - 根据连接状态和交易ID启用/禁用按钮

### 2. SaleTransactionPage（销售交易页面）
- **输入参数**：
  - 交易金额（必填，支持小数）
  - 订单描述（可选）
- **验证规则**：
  - 金额必须是有效数字
  - 金额必须大于 0
- **特点**：
  - 数字键盘输入
  - 实时验证
  - 处理中状态禁用输入

### 3. AuthTransactionPage（预授权页面）
- **输入参数**：
  - 授权金额（必填，默认 $500.00）
  - 订单描述（可选）
- **验证规则**：
  - 金额必须是有效数字
  - 金额必须大于 0
- **特点**：
  - 保存授权交易ID供后续操作使用

### 4. VoidTransactionPage（撤销交易页面）
- **输入参数**：
  - 原交易ID（必填，自动填充最后一笔交易ID）
  - 撤销原因（可选）
- **验证规则**：
  - 交易ID不能为空
- **特点**：
  - 自动填充最后一笔交易ID
  - 提供友好的错误提示（C12、C11 等）

### 5. TipAdjustPage（小费调整页面）
- **输入参数**：
  - 原交易ID（必填，自动填充最后一笔交易ID）
  - 小费金额（必填）
- **快捷操作**：
  - 提供 $10、$15、$20、$25 快捷按钮
- **验证规则**：
  - 交易ID不能为空
  - 小费金额必须是有效数字
  - 小费金额不能为负数
- **特点**：
  - 快捷金额选择
  - 支持自定义小费金额

### 6. QueryTransactionPage（查询交易页面）
- **查询方式**：
  - 通过交易ID查询
  - 通过商户订单号查询
- **输入参数**：
  - 交易ID 或 商户订单号（二选一）
- **验证规则**：
  - 查询参数不能为空
- **特点**：
  - 单选按钮切换查询方式
  - 自动填充最后一笔交易ID

## TaplinkDemoViewModel（共享 ViewModel）

### 管理的状态
- **连接状态**：
  - `isConnected`: 是否已连接
  - `connectionMode`: 连接模式
  - `deviceId`: 设备ID
  - `taproVersion`: Tapro 版本
  - `isConnecting`: 是否正在连接

- **交易ID**：
  - `lastTransactionId`: 最后一笔交易ID
  - `lastTransactionRequestId`: 最后一笔交易请求ID
  - `lastAuthTransactionId`: 最后一笔授权交易ID

- **日志**：
  - `logMessages`: 日志消息列表

### 提供的方法
- `connectToDevice()`: 连接设备
- `disconnectFromDevice()`: 断开连接
- `addLog(message: String)`: 添加日志
- `clearLogs()`: 清空日志
- `saveTransactionId()`: 保存交易ID
- `saveTransactionRequestId()`: 保存交易请求ID
- `saveAuthTransactionId()`: 保存授权交易ID

## 导航配置

### 路由常量
```kotlin
const val PAGE_MAIN_MENU = "page_main_menu"
const val PAGE_SALE = "page_sale"
const val PAGE_AUTH = "page_auth"
const val PAGE_VOID = "page_void"
const val PAGE_TIP_ADJUST = "page_tip_adjust"
const val PAGE_QUERY = "page_query"
```

### 导航流程
```
MainMenuPage (主菜单)
    ├─> SaleTransactionPage (销售交易)
    ├─> AuthTransactionPage (预授权)
    ├─> VoidTransactionPage (撤销交易)
    ├─> TipAdjustPage (小费调整)
    └─> QueryTransactionPage (查询交易)
```

## 依赖注入配置

使用 Koin 进行依赖注入：
```kotlin
val demoModule = module {
    viewModel { TaplinkDemoViewModel() }
}
```

在 `TaplinkDemoApplication` 中初始化 Koin。

## 优势

### 1. 职责分离
- 每个页面只负责一种交易类型
- 输入参数清晰明确
- 易于维护和扩展

### 2. 用户体验
- 每个交易有专门的输入界面
- 提供针对性的提示和验证
- 快捷操作（如小费快捷按钮）

### 3. 代码组织
- 模块化设计
- 代码复用（共享 ViewModel）
- 易于测试

### 4. 可扩展性
- 添加新交易类型只需创建新页面
- 不影响现有功能
- 统一的导航管理

## 已实现的所有交易页面

✅ **SaleTransactionPage** - 销售交易（支持金额明细、支付方式选择）
✅ **AuthTransactionPage** - 预授权
✅ **VoidTransactionPage** - 撤销交易
✅ **RefundTransactionPage** - 退款交易
✅ **TipAdjustPage** - 小费调整（含快捷按钮）
✅ **PostAuthTransactionPage** - 预授权完成
✅ **ForcedAuthTransactionPage** - 强制授权（需要授权码）
✅ **BatchCloseTransactionPage** - 批次关闭（含警告提示）
✅ **QueryTransactionPage** - 查询交易（支持交易ID或订单号）

## 后续可扩展

如需添加增量授权页面：
- `IncrementalAuthPage`: 增量授权页面（原授权交易ID + 增量金额）

每个页面遵循相同的模式：
1. 定义输入参数
2. 添加验证逻辑
3. 调用 SDK 执行交易
4. 处理回调结果
5. 完成后返回主菜单

## 注意事项

1. **状态管理**：所有全局状态（连接状态、交易ID、日志）都通过 `TaplinkDemoViewModel` 管理
2. **导航**：使用 Navigation Compose 进行页面导航
3. **依赖注入**：使用 Koin 注入 ViewModel，确保状态共享
4. **错误处理**：每个页面都有完善的输入验证和错误提示
5. **用户体验**：处理中状态禁用输入，防止重复提交

---

*重构完成时间: 2025-12-02*
*版本: v1.0*

