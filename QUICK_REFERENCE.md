# 快速参考卡

## 🚀 快速启动

```bash
# 构建
./gradlew build

# 运行（交互式）
./gradlew run

# 运行 JAR
java -jar build/libs/auto-x-account-kotlin-1.0.0.jar

# 命令行
java -jar build/libs/auto-x-account-kotlin-1.0.0.jar register batch-1 accounts.json json
```

## 📦 项目文件

| 文件 | 说明 |
|------|------|
| `Config.kt` | 全局配置、枚举、ConfigManager |
| `EmailManager.kt` | Email Plus、验证码、自定义 API |
| `CaptchaHandler.kt` | 4 种验证码模式、NumberImageMatch |
| `TwitterRegistration.kt` | 完整 5 步注册流程 |
| `BatchManager.kt` | 批量控制、并发、暂停/恢复 |
| `DataManager.kt` | JSON/CSV/Excel 导入导出 |
| `ProxyManager.kt` | 代理管理（浏览器/邮箱独立） |
| `BitBrowserClient.kt` | BitBrowser API 集成 |
| `Main.kt` | 应用入口、交互菜单 |

## ⚙️ 配置快速指南

### Gmail + Plus 模式
```json
{
  "emailConfig": {
    "provider": "GMAIL",
    "emailAddress": "your-email@gmail.com",
    "password": "your-app-password",
    "enablePlusMode": true,
    "plusSuffix": "+test"
  }
}
```

### 自定义域名邮箱
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

### 代理配置
```json
{
  "proxyConfig": {
    "enableBrowserProxy": true,
    "browserProxyUrl": "http://proxy-host:8080",
    "enableEmailProxy": true,
    "emailProxyUrl": "socks5://proxy-host:1080"
  }
}
```

## 💻 常用代码片段

### Email Plus 处理
```kotlin
val plusEmail = EmailManager.generatePlusEmail("user@gmail.com", 0)
val baseEmail = EmailManager.getBaseEmail(plusEmail)
```

### 批量注册
```kotlin
val batchManager = BatchManager(maxConcurrency = 5)
val task = batchManager.createBatchTask("batch-1", accounts)
batchManager.startBatchTask("batch-1")
```

### 数据导入导出
```kotlin
val accounts = DataManager.importAccountsFromJson("accounts.json")
DataManager.exportResultsToExcel(results, "results.xlsx")
```

### 代理管理
```kotlin
ProxyManager.enableBrowserProxy("http://proxy:8080")
ProxyManager.enableEmailProxy("socks5://proxy:1080")
```

## 📝 数据格式

### JSON 账户
```json
[
  {
    "email": "user@gmail.com",
    "name": "User Name",
    "password": "Password123!",
    "dateOfBirth": "1990-01-15",
    "username": "username",
    "phone": "+1-555-001"
  }
]
```

### CSV 账户
```csv
email,name,password,dateOfBirth,username,phone
user@gmail.com,User Name,Password123!,1990-01-15,username,+1-555-001
```

## 🎯 菜单选项

```
1. 导入账户        (JSON/CSV/Excel)
2. 批量注册        (启动注册流程)
3. 查看状态        (进度统计)
4. 导出结果        (结果导出)
5. 配置代理        (浏览器/邮箱)
6. 配置邮箱        (Gmail/Outlook/自定义)
7. 测试邮箱        (连接验证)
8. 退出
```

## 🔑 关键类和对象

| 类/对象 | 作用 |
|---------|------|
| `ConfigManager` | 全局配置管理 |
| `EmailManager` | 邮箱管理 |
| `CaptchaHandler` | 验证码处理 |
| `TwitterRegistration` | 注册流程 |
| `BatchManager` | 批量控制 |
| `DataManager` | 数据管理 |
| `ProxyManager` | 代理管理 |
| `BitBrowserManager` | BitBrowser 管理 |

## ✅ 验证码模式

```kotlin
enum class CaptchaMode {
    AUTO,           // LLM 优先，失败则手动
    MANUAL,         // 手动输入
    THIRD_PARTY,    // 第三方服务
    LLM             // 大语言模型
}
```

## 📧 邮箱提供商

```kotlin
enum class EmailProvider {
    GMAIL,          // Gmail
    OUTLOOK,        // Outlook
    CUSTOM,         // 自定义 IMAP/SMTP
    SELF_HOSTED     // 自建域名邮箱
}
```

## 🌐 代理类型

```kotlin
enum class ProxyType {
    HTTP,           // HTTP 代理
    HTTPS,          // HTTPS 代理
    SOCKS5          // SOCKS5 代理
}
```

## 📊 批量任务状态

```kotlin
enum class BatchTaskStatus {
    PENDING,        // 待处理
    RUNNING,        // 运行中
    PAUSED,         // 已暂停
    RESUMED,        // 已恢复
    STOPPED,        // 已停止
    COMPLETED,      // 已完成
    FAILED          // 失败
}
```

## 🔐 注册流程步骤

1. 切换到邮箱注册模式
2. 填写基本信息（姓名、邮箱、出生日期）
3. 解决数字图片匹配验证码
4. 验证邮箱验证码
5. 设置密码

## 📚 文档导航

- **快速开始**: 本文件
- **详细文档**: `PROJECT_DOCUMENTATION.md`
- **使用指南**: `USAGE_GUIDE.md`
- **完成总结**: `PROJECT_COMPLETION_SUMMARY.md`
- **项目说明**: `README.md`

## 🆘 常见问题速解

| 问题 | 解决 |
|------|------|
| 编译失败 | `./gradlew clean build --refresh-dependencies` |
| 邮箱连接失败 | 检查配置、IMAP/SMTP 端口、网络 |
| 代理不工作 | 验证代理 URL 格式和连接 |
| 验证码失败 | 切换到 MANUAL 模式或使用 LLM |
| 批量慢 | 增加 `maxConcurrency` 参数 |

## 💡 最佳实践

- 使用 Plus 模式以避免注册新邮箱
- 使用代理轮换以避免 IP 限制
- 定期备份配置和结果
- 从小规模测试开始
- 监控和调整 `maxConcurrency`
- 尊守平台速率限制

## 🎓 技术栈

- **语言**: Kotlin 1.9.21
- **JVM**: Java 20+
- **构建**: Gradle 8.5
- **HTTP**: Ktor Client 2.3.6
- **数据**: Gson 2.10.1, Apache POI 5.0.0
- **异步**: Kotlinx Coroutines 1.7.3
- **日志**: SLF4J + Logback

## 🚀 性能优化参数

```json
{
  "maxConcurrency": 10,        // 并发数
  "requestTimeout": 20000,     // 请求超时
  "captchaTimeout": 180000,    // 验证码超时
  "retryCount": 3,             // 重试次数
  "retryDelay": 500            // 重试延迟
}
```

## 📞 支持

- 📖 [完整文档](./PROJECT_DOCUMENTATION.md)
- 📚 [使用指南](./USAGE_GUIDE.md)
- 🐛 [问题报告](https://github.com/longzheng268/auto-x-account-kotlin/issues)

---

**版本**: 1.0.0 | **JVM**: 20+ | **Kotlin**: 1.9.21
