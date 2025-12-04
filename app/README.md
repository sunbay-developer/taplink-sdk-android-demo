# Taplink Demo Application

Taplink SDK 演示应用，用于展示如何使用 Taplink SDK 进行支付交易。

## 功能特性

- ✅ SDK 初始化
- ✅ 设备连接/断开
- ✅ 销售交易演示
- ✅ 连接状态显示
- ✅ 交易日志记录

## 项目结构

```
taplink_demo/
├── build.gradle.kts          # 构建配置
├── proguard-rules.pro        # ProGuard 规则
├── src/
│   └── main/
│       ├── AndroidManifest.xml
│       ├── java/
│       │   └── com/sunmi/tapro/taplink/demo/
│       │       ├── TaplinkDemoApplication.kt  # Application 类
│       │       └── MainActivity.kt            # 主界面
│       └── res/
│           ├── layout/
│           │   └── activity_main.xml         # 主界面布局
│           └── values/
│               ├── strings.xml                # 字符串资源
│               ├── colors.xml                 # 颜色资源
│               └── themes.xml                 # 主题资源
└── README.md
```

## 使用方法

### 1. 初始化 SDK

在 `TaplinkDemoApplication` 中，SDK 会在应用启动时自动初始化：

```kotlin
val config = TaplinkConfig(
    appId = "taplink_demo_app",
    clientSecret = "demo_secret_key",
    logEnabled = true,
    logLevel = LogLevel.DEBUG,
    timeout = 60
)
TaplinkSDK.init(config)
```

### 2. 连接设备

点击"连接"按钮，使用本地模式连接到支付终端：

```kotlin
val protocol = "local://com.sunmi.tapro.taplink.demo/transaction"
TaplinkSDK.connect(protocol, callback)
```

### 3. 执行交易

连接成功后，点击"销售交易"按钮执行一笔 $100.00 的销售交易：

```kotlin
val request = SaleRequest(
    orderNo = "ORDER_${System.currentTimeMillis()}",
    priceCurrency = "USD",
    orderAmount = 10000, // $100.00
    description = "Taplink Demo Sale",
    timeExpire = 60
)
TaplinkSDK.sale(request, callback)
```

## 界面说明

- **连接状态显示**：显示当前与支付终端的连接状态
- **连接/断开按钮**：控制设备连接
- **销售交易按钮**：执行一笔示例交易
- **日志区域**：显示所有操作和交易的日志信息

## 依赖项

- `lib_taplink_sdk`：Taplink SDK 核心库
- `lib_log`：日志库
- AndroidX 核心库

## 注意事项

1. 确保在运行前已正确配置 Taplink SDK
2. 需要相应的权限才能连接设备
3. 交易功能需要实际的支付终端支持

## 开发说明

这是一个演示应用，用于展示 Taplink SDK 的基本用法。在实际项目中，请根据业务需求进行相应的调整和优化。


