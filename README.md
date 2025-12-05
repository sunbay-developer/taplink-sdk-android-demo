# TaplinkDemo

一个基于 Jetpack Compose 构建的 Taplink SDK 演示应用，展示如何集成和使用商米 Taplink SDK 进行支付交易处理。

## 项目简介

TaplinkDemo 是商米（Sunmi）提供的支付 SDK 集成示例应用，演示了如何在 Android 应用中使用 Taplink SDK 实现各种支付交易功能。该应用采用现代化的 Android 开发技术栈，使用 Jetpack Compose 构建 UI，Kotlin 协程处理异步操作，Koin 进行依赖注入。

## 功能特性

### 支付交易功能
- **销售交易（Sale）** - 标准支付交易
- **授权交易（Auth）** - 预授权交易
- **强制授权交易（Forced Auth）** - 强制授权
- **授权后交易（Post Auth）** - 授权完成后的交易
- **退款交易（Refund）** - 退款处理
- **撤销交易（Void）** - 交易撤销
- **查询交易（Query）** - 交易查询
- **批次结算（Batch Close）** - 批次关闭
- **小费调整（Tip Adjust）** - 小费金额调整

### 技术特性
- 基于 Jetpack Compose 的现代化 UI
- MVVM 架构模式
- Kotlin 协程异步处理
- Koin 依赖注入
- 实时日志控制台

## 技术栈

### 开发环境
- **Kotlin**: 2.2.21
- **Android Gradle Plugin**: 8.13.1
- **Compile SDK**: 35
- **Min SDK**: 25
- **Target SDK**: 35
- **JVM Target**: 11

### 核心依赖
- **Jetpack Compose**: 2024.11.00 (BOM)
- **Material3**: 1.3.2
- **Kotlin Coroutines**: 1.8.1
- **Koin**: 3.5.6
- **Navigation Compose**: 2.4.0
- **Lifecycle Runtime**: 2.9.2
- **Taplink SDK**: 1.0.0

## 项目结构

```
app/src/main/java/com/sunmi/tapro/taplink/demo/
├── MainActivity.kt                    # 主 Activity
├── TaplinkDemoApplication.kt          # Application 类
├── Navigation.kt                      # 导航配置
├── components/                        # UI 组件
│   └── LogConsole.kt                 # 日志控制台组件
├── pages/                            # 页面
│   ├── MainMenuPage.kt               # 主菜单页面
│   ├── SaleTransactionPage.kt        # 销售交易页面
│   ├── AuthTransactionPage.kt        # 授权交易页面
│   ├── ForcedAuthTransactionPage.kt  # 强制授权页面
│   ├── PostAuthTransactionPage.kt    # 授权后交易页面
│   ├── RefundTransactionPage.kt      # 退款页面
│   ├── VoidTransactionPage.kt        # 撤销页面
│   ├── QueryTransactionPage.kt       # 查询页面
│   ├── BatchCloseTransactionPage.kt  # 批次结算页面
│   └── TipAdjustPage.kt              # 小费调整页面
├── viewmodel/                        # ViewModel
│   └── TaplinkDemoViewModel.kt       # 主 ViewModel
└── ui/                               # UI 主题
    └── theme/                        # 主题配置
```

## 快速开始

### 环境要求
- Android Studio Ladybug | 2024.2.1 或更高版本
- JDK 11 或更高版本
- Android SDK 35
- Gradle 8.x

### 构建步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd TaplinkDemo
```

2. **配置本地环境**
确保 `local.properties` 文件包含正确的 SDK 路径：
```properties
sdk.dir=/path/to/your/Android/sdk
```

3. **同步依赖**
在 Android Studio 中打开项目，等待 Gradle 同步完成。

4. **构建项目**
```bash
./gradlew build
```

5. **运行应用**
```bash
./gradlew installDebug
```
或在 Android Studio 中直接运行。

## 权限说明

应用需要以下权限：
- `INTERNET` - 网络访问
- `ACCESS_NETWORK_STATE` - 网络状态检测
- `ACCESS_WIFI_STATE` - WiFi 状态检测
- `CHANGE_WIFI_STATE` - WiFi 状态修改
- `BLUETOOTH` - 蓝牙访问
- `BLUETOOTH_ADMIN` - 蓝牙管理
- `USB_PERMISSION` - USB 设备访问

## 使用说明

### 初始化 SDK
在 `TaplinkDemoApplication` 中初始化 Taplink SDK：
```kotlin
class TaplinkDemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化 Taplink SDK
        // TaplinkSDK.init(this)
    }
}
```

### 执行交易
通过主菜单选择相应的交易类型，填写必要的交易参数，点击执行按钮即可发起交易。交易结果会实时显示在日志控制台中。

## 构建变体

项目支持多种构建变体，配置在 `build-variants.gradle` 文件中。

## 混淆配置

Release 构建的混淆规则定义在 `app/proguard-rules.pro` 文件中。

## 依赖管理

项目使用 Gradle Version Catalog 管理依赖，所有依赖版本定义在 `gradle/libs.versions.toml` 文件中。

## 开发指南

### 添加新的交易类型
1. 在 `pages/` 目录下创建新的页面文件
2. 在 `Navigation.kt` 中添加路由配置
3. 在 `MainMenuPage.kt` 中添加菜单入口
4. 在 `TaplinkDemoViewModel.kt` 中添加相应的业务逻辑

### 自定义主题
修改 `ui/theme/` 目录下的主题文件以自定义应用外观。

## 测试

### 运行单元测试
```bash
./gradlew test
```

### 运行 UI 测试
```bash
./gradlew connectedAndroidTest
```

## 常见问题

### Q: SDK 初始化失败？
A: 请确保已正确添加 Taplink SDK 依赖，并检查网络连接。

### Q: 交易失败？
A: 检查设备是否正确连接，参数是否填写正确，查看日志控制台获取详细错误信息。

### Q: 编译错误？
A: 确保使用正确的 JDK 版本（11+），清理项目后重新构建：
```bash
./gradlew clean build
```

## 版本历史

### v1.0.0
- 初始版本发布
- 支持基础支付交易功能
- 集成 Taplink SDK 1.0.0

## 许可证

Copyright © 2025 Sunmi Technology Co., Ltd. All rights reserved.

## 联系方式

- **开发团队**: TaPro Team
- **技术支持**: [support@sunmi.com](mailto:support@sunmi.com)
- **官方网站**: [https://www.sunmi.com](https://www.sunmi.com)

## 相关资源

- [Taplink SDK 文档](https://docs.sunmi.com/taplink)
- [商米开发者中心](https://developer.sunmi.com)
- [Android 开发指南](https://developer.android.com)
- [Jetpack Compose 文档](https://developer.android.com/jetpack/compose)
