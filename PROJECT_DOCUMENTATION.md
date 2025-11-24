# Twitter/X 账号批量注册系统 - Kotlin JVM 实现

基于 Kotlin 的企业级 Twitter/X 账号批量注册系统，支持 Email Plus 模式、自定义域名邮箱 API、代理管理和批量控制。

## 🎯 核心功能

### 1. Twitter/X 注册流程 ✅
完整实现 X 账户注册的全流程：
- ✅ **邮箱注册模式** - 默认使用"Use email instead"方式
- ✅ **基本信息填充** - 姓名、邮箱、出生日期
- ✅ **数字图片验证码** - X 自有的 NumberImageMatch 机制（不依赖 Arkose Labs/ReCAPTCHA）
- ✅ **邮箱验证码处理** - 自动获取和验证
- ✅ **密码设置** - 安全的密码配置

### 2. Email Plus 模式 ✅
支持 Gmail/Outlook 的 Plus 寻址功能：
```
原邮箱: user@gmail.com
Plus模式: user+test01@gmail.com, user+test02@gmail.com, ...
所有验证码仍发送到: user@gmail.com
```
**优势**：高域名权重，无需额外注册新邮箱

**配置** (`Config.kt`):
```kotlin
EmailConfig(
    enablePlusMode = true,
    plusSuffix = "+test"
)
```

### 3. 自定义域名邮箱 API ✅ (关键功能)
完全支持自建域名邮箱集成：

**支持的邮箱类型** (`EmailProvider`):
- `GMAIL` - Gmail 标准集成
- `OUTLOOK` - Outlook 标准集成
- `CUSTOM` - 自定义 IMAP/SMTP 服务器
- `SELF_HOSTED` - 自建域名邮箱 API

**配置示例**:
```kotlin
EmailConfig(
    provider = "SELF_HOSTED",
    customApiUrl = "https://mail.yourdomain.com/api",
    customApiKey = "your-api-key",
    imapHost = "mail.yourdomain.com",
    imapPort = 993,
    smtpHost = "mail.yourdomain.com",
    smtpPort = 587
)
```

### 4. 批量控制引擎 ✅
- ✅ **并发控制** - 可配置的最大并发数（默认5）
- ✅ **任务管理** - 创建、启动、暂停、恢复、停止
- ✅ **状态持久化** - 任务进度跟踪
- ✅ **实时监控** - 进度百分比、完成/失败计数

**基本用法**:
```kotlin
val batchManager = BatchManager(maxConcurrency = 5)
val task = batchManager.createBatchTask("batch-1", accounts)
batchManager.startBatchTask("batch-1")
// 暂停任务
batchManager.pauseBatchTask("batch-1")
// 恢复任务
batchManager.resumeBatchTask("batch-1")
// 停止任务
batchManager.stopBatchTask("batch-1")
```

### 5. 数据管理 ✅
支持多格式的账户数据管理：

#### JSON 格式
```json
[
  {
    "email": "user@gmail.com",
    "name": "John Doe",
    "password": "secure_password",
    "dateOfBirth": "1990-01-15",
    "username": "johndoe",
    "phone": "+1234567890"
  }
]
```

#### CSV 格式
```csv
email,name,password,dateOfBirth,username,phone
user@gmail.com,John Doe,secure_password,1990-01-15,johndoe,+1234567890
```

#### Excel 格式
支持 .xlsx 文件，包含自动列宽调整

**导入/导出 API**:
```kotlin
// 导入
val accounts = DataManager.importAccountsFromJson("accounts.json")
val accounts = DataManager.importAccountsFromCsv("accounts.csv")
val accounts = DataManager.importAccountsFromExcel("accounts.xlsx")

// 导出
DataManager.exportAccountsToJson(accounts, "output.json")
DataManager.exportAccountsToCsv(accounts, "output.csv")
DataManager.exportAccountsToExcel(accounts, "output.xlsx")
DataManager.exportResultsToJson(results, "results.json")
```

## 🛠️ 技术配置

### 验证码系统 (`Config.kt`)

**CaptchaMode 枚举**:
```kotlin
enum class CaptchaMode {
    AUTO,           // 自动模式（默认）- LLM优先，失败则手动
    MANUAL,         // 手动输入
    THIRD_PARTY,    // 第三方服务（2Captcha, AntiCaptcha等）
    LLM             // 大语言模型识别（OpenAI GPT-4 Vision等）
}
```

**配置**:
```kotlin
val config = AppConfig(
    captchaMode = "AUTO",      // 默认自动模式
    captchaTimeout = 300000    // 5分钟超时
)
```

**验证码处理** (`CaptchaHandler.kt`):
- NumberImageMatch 识别（X 特有）
- 多模式自动切换
- LLM 集成支持
- 第三方服务集成
- 验证码图片调试保存

### 代理控制 (`ProxyManager.kt`)

**独立的浏览器/邮箱代理控制**:

```kotlin
// 浏览器代理 - 独立开关
ProxyManager.enableBrowserProxy("http://proxy-host:8080")
ProxyManager.disableBrowserProxy()
ProxyManager.isBrowserProxyEnabled()

// 邮箱代理 - 独立开关
ProxyManager.enableEmailProxy("socks5://proxy-host:1080")
ProxyManager.disableEmailProxy()
ProxyManager.isEmailProxyEnabled()

// 获取代理信息
val browserProxy = ProxyManager.getBrowserProxy()
val emailProxy = ProxyManager.getEmailProxy()

// 支持的代理类型
enum class ProxyType {
    HTTP,
    HTTPS,
    SOCKS5
}
```

**代理格式**:
```
http://host:port
https://user:pass@host:port
socks5://host:port
```

### BitBrowser 集成 (`BitBrowserClient.kt`)

**支持的操作**:
```kotlin
val client = BitBrowserClient("http://localhost", 54345)

// 初始化
val initialized = client.initialize()

// 创建配置
val profile = client.createProfile(
    name = "profile-1",
    browserType = "chrome",
    proxyUrl = "http://proxy:8080"
)

// 启动/关闭浏览器
client.startBrowser(profileId)
client.closeBrowser(profileId)

// 获取状态
val status = client.getBrowserStatus(profileId)
val debugPort = client.getDebugPort(profileId)

// 列出所有配置
val profiles = client.listProfiles()

// 删除配置
client.deleteProfile(profileId)
```

**配置** (`Config.kt`):
```kotlin
BitBrowserConfig(
    apiUrl = "http://localhost:54345",
    apiPort = 54345,
    profileId = "your-profile-id",
    enabled = true
)
```

### 邮箱管理 (`EmailManager.kt`)

**Plus 模式处理**:
```kotlin
// 生成 Plus 邮箱
val plusEmail = EmailManager.generatePlusEmail("user@gmail.com", index = 0)
// 结果: user+test00@gmail.com

// 获取基础邮箱（验证码接收地址）
val baseEmail = EmailManager.getBaseEmail("user+test00@gmail.com")
// 结果: user@gmail.com
```

**验证码获取**:
```kotlin
val code = EmailManager.getVerificationCode(
    email = "user@gmail.com",
    sender = "noreply@twitter.com",
    timeout = 300000  // 5分钟
)
```

**邮箱配置验证**:
```kotlin
val valid = EmailManager.validateEmailConfiguration()
```

## 📦 项目结构

```
src/main/kotlin/com/autoxtwitteraccount/
├── config/
│   └── Config.kt              # 全局配置、枚举、ConfigManager
├── email/
│   └── EmailManager.kt        # 邮箱管理、Plus 模式、自定义 API
├── twitter/
│   └── TwitterRegistration.kt # X 注册流程、NumberImageMatch 处理
├── captcha/
│   └── CaptchaHandler.kt      # 验证码识别、多模式支持
├── batch/
│   └── BatchManager.kt        # 批量控制、并发管理、状态持久化
├── data/
│   └── DataManager.kt         # JSON/CSV/Excel 导入导出
├── proxy/
│   └── ProxyManager.kt        # 独立代理控制
├── browser/
│   └── BitBrowserClient.kt    # BitBrowser API 集成
└── Main.kt                    # 主应用入口

build.gradle.kts              # Gradle 配置（JVM 20, Kotlin 1.9.21）
```

## 🚀 快速开始

### 构建项目

```bash
# 克隆项目
git clone https://github.com/longzheng268/auto-x-account-kotlin.git
cd auto-x-account-kotlin

# 构建
./gradlew build

# 运行
./gradlew run
```

### 命令行使用

```bash
# 交互式模式
java -jar build/libs/auto-x-account-kotlin-1.0.0.jar

# 导入账户
java -jar build/libs/auto-x-account-kotlin-1.0.0.jar import json accounts.json

# 启动批量注册
java -jar build/libs/auto-x-account-kotlin-1.0.0.jar register batch-1 accounts.json json
```

### 交互式菜单选项

```
1. 从文件导入账户 (JSON/CSV/Excel)
2. 启动批量注册
3. 查看批量状态
4. 导出注册结果
5. 配置代理
6. 配置邮箱
7. 测试邮箱连接
8. 退出
```

## ⚙️ 配置示例

### config.json

```json
{
  "captchaMode": "AUTO",
  "captchaTimeout": 300000,
  "emailConfig": {
    "provider": "SELF_HOSTED",
    "emailAddress": "noreply@yourdomain.com",
    "enablePlusMode": true,
    "plusSuffix": "+test",
    "customApiUrl": "https://mail.yourdomain.com/api",
    "customApiKey": "your-api-key",
    "imapHost": "mail.yourdomain.com",
    "imapPort": 993,
    "smtpHost": "mail.yourdomain.com",
    "smtpPort": 587
  },
  "proxyConfig": {
    "enableBrowserProxy": true,
    "browserProxyUrl": "http://proxy-host:8080",
    "enableEmailProxy": false,
    "emailProxyUrl": "",
    "proxyType": "HTTP"
  },
  "bitBrowserConfig": {
    "apiUrl": "http://localhost:54345",
    "apiPort": 54345,
    "profileId": "your-profile-id",
    "enabled": false
  },
  "maxConcurrency": 5,
  "requestTimeout": 30000,
  "retryCount": 3,
  "debugMode": false
}
```

## 📝 数据格式

### accounts.json

```json
[
  {
    "email": "user1@gmail.com",
    "name": "User One",
    "password": "SecurePass123!",
    "dateOfBirth": "1990-05-15",
    "username": "userone",
    "phone": "+1234567890"
  },
  {
    "email": "user2@outlook.com",
    "name": "User Two",
    "password": "SecurePass456!",
    "dateOfBirth": "1991-07-20",
    "username": "usertwo",
    "phone": "+1234567891"
  }
]
```

### accounts.csv

```csv
email,name,password,dateOfBirth,username,phone
user1@gmail.com,User One,SecurePass123!,1990-05-15,userone,+1234567890
user2@outlook.com,User Two,SecurePass456!,1991-07-20,usertwo,+1234567891
```

## 🎨 Kotlin 风格约定

项目严格遵循惯用 Kotlin 风格：

```kotlin
// ✅ 优先使用 val（不可变性）
val config: AppConfig = ConfigManager.config

// ✅ 使用 data class
data class TwitterAccount(
    val email: String,
    val name: String,
    val password: String
)

// ✅ 安全调用符和智能转换
val result = parseEmail(email)?.let { (name, domain) -> 
    generatePlusEmail(email, 0)
}

// ✅ 对象表达式和扩展函数
object ConfigManager {
    val config: AppConfig
        get() = _config
}

// ✅ 协程支持
suspend fun registerTwitterAccount(account: TwitterAccount): RegistrationResult
```

## 📚 依赖库

- **Kotlin**: 1.9.21
- **Kotlinx Coroutines**: 1.7.3 - 异步编程
- **Kotlinx Serialization**: 1.6.2 - JSON 序列化
- **Ktor Client**: 2.3.6 - HTTP 客户端
- **Apache Commons CSV**: 1.10.0 - CSV 处理
- **Apache POI**: 5.0.0 - Excel 处理
- **Gson**: 2.10.1 - JSON 解析
- **SLF4J + Logback**: 日志记录
- **JDK**: 20+

## 🔐 安全注意事项

1. **密码存储**: 建议使用环境变量或加密配置文件存储
2. **API 密钥**: 从环境变量而非硬编码读取
3. **代理认证**: 支持用户名密码认证
4. **邮箱验证码**: 自动清理超时的验证码缓存

## 🧪 测试

```bash
# 运行所有测试
./gradlew test

# 仅测试特定类
./gradlew test --tests "com.autoxtwitteraccount.email.*"
```

## 🤝 扩展指南

### 添加新的邮箱提供商

```kotlin
// 1. 在 EmailProvider 枚举中添加
enum class EmailProvider {
    CUSTOM_PROVIDER
}

// 2. 在 EmailManager 中实现获取验证码
private suspend fun getCustomProviderVerificationCode(...): String? {
    // 实现获取逻辑
}

// 3. 更新 getEmailProvider() 方法
```

### 添加新的验证码识别方式

```kotlin
// 1. 在 CaptchaMode 中添加
enum class CaptchaMode {
    CUSTOM_METHOD
}

// 2. 在 CaptchaHandler 中实现
private suspend fun handleCustomMethod(captchaData: Any): CaptchaResult? {
    // 实现识别逻辑
}
```

## 📄 许可证

MIT License - 详见 LICENSE 文件

## 👨‍💻 贡献

欢迎提交 Issue 和 Pull Request！

## 📞 支持

- 文档: https://github.com/longzheng268/auto-x-account-kotlin/wiki
- Issues: https://github.com/longzheng268/auto-x-account-kotlin/issues
- Email: support@yourdomain.com

---

**版本**: 1.0.0  
**最后更新**: 2025-11-24  
**JVM 版本**: 20+  
**Kotlin 版本**: 1.9.21
