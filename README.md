# Twitter/X 账号批量注册系统 - Kotlin JVM

🚀 企业级 Kotlin/JVM 实现的 Twitter/X 账号批量注册系统，具有完整的验证码处理、Email Plus 模式、自定义域名邮箱 API 和智能代理管理。

> **合规声明**: 本项目骨架用于演示批量注册流程的结构设计。任何实际应用必须遵守 X/Twitter 服务条款和当地法律法规，不得用于垃圾账号、欺诈或滥用。

## ✨ 核心特性

### 🔐 完整的注册流程
- ✅ **邮箱注册模式** - 默认使用"Use email instead"
- ✅ **信息填写** - 姓名、邮箱、出生日期自动化
- ✅ **数字图片验证码** - X 自有 NumberImageMatch 机制（非 Arkose Labs）
- ✅ **邮箱验证码** - 自动获取和验证
- ✅ **密码设置** - 安全密码配置

### 📧 Email Plus 模式（关键功能）
```
原邮箱:     user@gmail.com
Plus模式:   user+test00@gmail.com
           user+test01@gmail.com
           user+test02@gmail.com
验证码地址: 仍为 user@gmail.com
```
**优势**: 高域名权重，无需额外注册新邮箱

### 🎯 自定义域名邮箱 API（关键功能）
完全支持自建邮箱服务集成：
- `GMAIL` - Gmail IMAP/SMTP
- `OUTLOOK` - Outlook IMAP/SMTP  
- `SELF_HOSTED` - 自建域名邮箱 API
- `CUSTOM` - 自定义 IMAP/SMTP 服务器

### ⚙️ 智能验证码处理
- **AUTO 模式** (默认) - LLM 优先，失败则手动
- **MANUAL 模式** - 手动输入
- **THIRD_PARTY 模式** - 2Captcha/AntiCaptcha 等
- **LLM 模式** - GPT-4 Vision 等大语言模型

### 🔄 批量控制引擎
- **并发管理** - 可配置并发数（默认 5）
- **任务控制** - 启动、暂停、恢复、停止
- **状态追踪** - 实时进度和统计
- **状态持久化** - 任务恢复能力

### 📊 多格式数据管理
- **JSON** - 标准格式（推荐）
- **CSV** - 电子表格格式
- **Excel** - .xlsx 文件支持

### 🌐 代理管理（独立控制）
- **浏览器代理** - 独立开关
- **邮箱代理** - 独立开关
- **支持格式** - HTTP/HTTPS/SOCKS5
- **认证支持** - 用户名密码认证

### 🔗 BitBrowser 集成
- 创建/删除浏览器配置
- 启动/关闭浏览器
- 获取调试端口
- 配置管理

## 🏗️ 项目结构

```
src/main/kotlin/com/autoxtwitteraccount/
├── config/
│   └── Config.kt                    # 全局配置、枚举、ConfigManager
├── email/
│   └── EmailManager.kt              # Email Plus、自定义 API、验证码获取
├── twitter/
│   └── TwitterRegistration.kt       # 完整注册流程实现
├── captcha/
│   └── CaptchaHandler.kt            # 多模式验证码识别
├── batch/
│   └── BatchManager.kt              # 并发控制、任务管理
├── data/
│   └── DataManager.kt               # JSON/CSV/Excel 导入导出
├── proxy/
│   └── ProxyManager.kt              # 独立代理控制
├── browser/
│   └── BitBrowserClient.kt          # BitBrowser API 集成
└── Main.kt                          # 应用程序入口

build.gradle.kts                    # Gradle 8.5 配置
PROJECT_DOCUMENTATION.md            # 详细 API 文档
USAGE_GUIDE.md                      # 完整使用指南
```

## 🚀 快速开始

### 1. 环境要求
- **Java**: JDK 20+
- **Gradle**: 8.5（包含在 gradlew）
- **Kotlin**: 1.9.21（自动管理）

### 2. 构建项目

```bash
# 克隆项目
git clone https://github.com/longzheng268/auto-x-account-kotlin.git
cd auto-x-account-kotlin

# 构建
./gradlew clean build

# Windows 用户
.\gradlew.bat clean build
```

### 3. 运行应用

```bash
# 交互式模式（推荐）
./gradlew run

# 或直接运行 JAR
java -jar build/libs/auto-x-account-kotlin-1.0.0.jar

# 命令行模式
java -jar build/libs/auto-x-account-kotlin-1.0.0.jar register batch-1 accounts.json json
```

### 4. 配置系统

```bash
# 复制示例配置
cp config/config.example.json config/config.json

# 编辑配置（邮箱、代理、验证码模式等）
nano config/config.json
```

### 5. 准备数据

```bash
# 复制示例账户
cp examples/sample_accounts.json accounts.json

# 编辑账户信息
nano accounts.json
```

## 📖 完整文档

| 文档 | 说明 |
|------|------|
| [PROJECT_DOCUMENTATION.md](./PROJECT_DOCUMENTATION.md) | 🔧 详细 API 和配置文档 |
| [USAGE_GUIDE.md](./USAGE_GUIDE.md) | 📚 完整使用指南和最佳实践 |
| [examples/](./examples/) | 📝 示例文件（JSON、配置等） |

## 🎯 快速配置示例

### Gmail + Plus 模式

```json
{
  "emailConfig": {
    "provider": "GMAIL",
    "emailAddress": "your-email@gmail.com",
    "password": "your-app-password",
    "enablePlusMode": true,
    "plusSuffix": "+test"
  },
  "captchaMode": "AUTO",
  "maxConcurrency": 5
}
```

### 自建域名邮箱

```json
{
  "emailConfig": {
    "provider": "SELF_HOSTED",
    "customApiUrl": "https://mail.yourdomain.com/api",
    "customApiKey": "your-api-key",
    "imapHost": "mail.yourdomain.com",
    "imapPort": 993
  }
}
```

## 💡 关键代码示例

### Email Plus 模式

```kotlin
// 自动生成 Plus 邮箱
val plusEmail = EmailManager.generatePlusEmail("user@gmail.com", 0)
// 结果: user+test00@gmail.com

// 获取基础邮箱（验证码地址）
val baseEmail = EmailManager.getBaseEmail("user+test00@gmail.com")
// 结果: user@gmail.com
```

### 批量控制

```kotlin
val batchManager = BatchManager(maxConcurrency = 5)
val task = batchManager.createBatchTask("batch-1", accounts)
batchManager.startBatchTask("batch-1")

// 暂停
batchManager.pauseBatchTask("batch-1")

// 恢复
batchManager.resumeBatchTask("batch-1")

// 查看进度
val status = batchManager.getTask("batch-1")
println("Progress: ${status?.progress}%")
```

## 🎨 Kotlin 风格特点

本项目严格遵循惯用 Kotlin 风格：
- ✅ 优先使用 `val`（不可变性）
- ✅ 使用 `data class` 和 `object`
- ✅ 安全调用符 `?.` 和智能转换
- ✅ 协程支持 (`suspend` 函数)
- ✅ 函数式编程风格

## 📦 核心依赖

```gradle
Kotlin: 1.9.21
Kotlinx Coroutines: 1.7.3
Ktor Client: 2.3.6
Apache Commons CSV: 1.10.0
Apache POI: 5.0.0
Gson: 2.10.1
SLF4J + Logback
```

## 🔒 安全建议

1. **密码管理** - 使用环境变量或加密配置
2. **API 密钥** - 从配置文件而非代码读取
3. **代理认证** - 支持用户名密码
4. **数据隐私** - 合规处理用户数据

## ⚖️ 合规声明

本项目提供的是技术框架。使用者必须确保：
- ✅ 遵守 X/Twitter 服务条款
- ✅ 遵守所有适用的法律法规
- ✅ 不用于垃圾邮件、欺诈或滥用

## 📞 支持

- 📖 [详细文档](./PROJECT_DOCUMENTATION.md)
- 📚 [使用指南](./USAGE_GUIDE.md)
- 🐛 [Issue 跟踪](https://github.com/longzheng268/auto-x-account-kotlin/issues)

---

**版本**: 1.0.0 | **JVM**: 20+ | **Kotlin**: 1.9.21 | **更新**: 2025-11-24
