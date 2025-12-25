# LAN模式连接设置功能

## 功能说明

当用户选择LAN连接模式且尚未配置IP地址时，会自动弹出一个对话框，要求输入TaPro设备的IP地址和端口号。

## 实现细节

### 1. ConnectionActivity 新增功能

- `checkLanConfiguration()`: 检查LAN模式是否需要配置IP和端口
- `showLanSetupDialog()`: 显示LAN设置对话框
- `validateLanInput()`: 验证用户输入的IP和端口

### 2. 对话框布局

新增布局文件：`app/src/main/res/layout/dialog_lan_setup.xml`

包含以下输入项：
- IP地址输入框
- 端口号输入框
- TLS加密开关

## 用户体验流程

1. 用户在ConnectionActivity中选择LAN连接模式
2. 系统检查是否已配置LAN的IP地址
3. 如果未配置，自动弹出"LAN连接设置"对话框
4. 用户可以选择：
   - **确认**：输入IP和端口，保存LAN配置
   - **取消**：取消设置，自动切换回APP_TO_APP模式
5. 对话框不可取消（必须选择确认或取消）
6. 输入验证：
   - IP地址格式验证（正则表达式）
   - 端口号范围验证（1-65535）
   - 空值检查

## 配置保存

如果用户选择确认并输入有效的IP和端口：
- 保存LAN配置（IP、端口、TLS状态）
- 保持LAN连接模式
- 更新UI显示新的LAN配置

如果用户选择取消：
- 不保存任何配置
- 自动切换回APP_TO_APP模式
- 更新UI显示APP_TO_APP配置

## 触发条件

对话框只在以下情况下弹出：
1. 用户主动选择LAN连接模式（点击LAN单选按钮）
2. 且当前没有配置LAN的IP地址（`ConnectionPreferences.getLanIp()`返回null或空字符串）

## 测试方法

### 清除LAN配置（用于测试）

可以通过以下方式清除LAN配置，重新触发LAN设置对话框：

1. 在代码中调用 `ConnectionPreferences.clearLanConfig(context)`
2. 或调用 `ConnectionPreferences.clearAll(context)` 清除所有配置

### 验证功能

1. 启动应用，打开ConnectionActivity
2. 选择LAN连接模式
3. 如果没有配置IP，确认弹出LAN设置对话框
4. 测试输入验证：
   - 空IP地址
   - 无效IP格式（如：999.999.999.999）
   - 空端口号
   - 无效端口号（如：0、99999）
5. 输入有效配置并确认
6. 验证配置已保存且UI已更新
7. 再次选择LAN模式
8. 确认不再弹出设置对话框（因为已有配置）
9. 测试取消功能：清除LAN配置后选择LAN模式，点击取消，确认切换回APP_TO_APP模式

## LAN连接实现

### 1. AppToAppPaymentService 扩展

- 添加了 `connect(listener, connectionConfig)` 重载方法
- 支持传入 `ConnectionConfig` 来配置LAN连接参数
- 保持向后兼容，原有的 `connect(listener)` 方法仍然可用

### 2. ConnectionActivity 连接逻辑

- `connectWithSelectedMode()`: 根据选择的连接模式调用相应的连接方法
- `connectAppToApp()`: App-to-App模式连接
- `connectLan()`: LAN模式连接
- `connectLanWithConfig()`: 使用具体的IP和端口配置进行LAN连接

### 3. LAN连接配置

使用 `ConnectionConfig` 类配置LAN连接：
```kotlin
val connectionConfig = ConnectionConfig()
    .setHost(ip)
    .setPort(port)
```

## 注意事项

- 对话框设置为不可取消（`setCancelable(false)`），确保用户必须做出选择
- 只针对LAN模式，其他连接模式不会触发此对话框
- 如果已有LAN配置，选择LAN模式时不会弹出对话框
- 用户可以随时在ConnectionActivity的LAN配置区域手动修改IP和端口
- 取消设置会自动切换回APP_TO_APP模式，避免用户停留在未配置的LAN模式
- LAN连接支持IP地址和端口配置，TLS配置需要确认SDK API支持情况
