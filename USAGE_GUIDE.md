# 使用指南

## 目录结构

```
auto-x-account-kotlin/
├── src/main/kotlin/             # 源代码
├── build/                       # 编译输出
├── config/                      # 配置文件
│   └── config.example.json      # 配置示例
├── examples/                    # 示例数据
│   └── sample_accounts.json     # 示例账户
├── build.gradle.kts             # Gradle 配置
├── gradlew                      # Gradle 包装器（Unix）
├── gradlew.bat                  # Gradle 包装器（Windows）
└── README.md                    # 项目说明
```

## 安装步骤

### 1. 环境要求

- **Java**: JDK 20 或更高版本
- **Gradle**: 8.5（包含在 gradlew 中）
- **Kotlin**: 1.9.21（由 Gradle 自动管理）

### 2. 验证环境

```bash
java -version
# 应输出 JDK 20+

gradle --version
# 或使用 gradlew
./gradlew --version
```

### 3. 构建项目

```bash
cd auto-x-account-kotlin

# 清理和构建
./gradlew clean build

# 或仅构建
./gradlew build

# Windows 用户
.\gradlew.bat clean build
```

### 4. 生成可执行包

```bash
# 生成可运行的 JAR
./gradlew build

# JAR 位置: build/libs/auto-x-account-kotlin-1.0.0.jar
```

## 配置文件

### 1. 创建配置文件

```bash
# 从示例复制
cp config/config.example.json config/config.json

# 编辑配置
nano config/config.json  # Linux/Mac
# 或使用你喜欢的编辑器
```

### 2. 邮箱配置

#### Gmail（推荐）

```json
{
  "provider": "GMAIL",
  "emailAddress": "your-email@gmail.com",
  "password": "your-app-password",
  "imapHost": "imap.gmail.com",
  "imapPort": 993,
  "smtpHost": "smtp.gmail.com",
  "smtpPort": 587,
  "enablePlusMode": true,
  "plusSuffix": "+test"
}
```

**获取 Gmail App Password**:
1. 登录 https://myaccount.google.com/
2. 左侧菜单 → 安全
3. 启用 2FA（如未启用）
4. 搜索 "App password"
5. 生成密码并复制

#### Outlook

```json
{
  "provider": "OUTLOOK",
  "emailAddress": "your-email@outlook.com",
  "password": "your-password",
  "imapHost": "imap-mail.outlook.com",
  "imapPort": 993,
  "smtpHost": "smtp-mail.outlook.com",
  "smtpPort": 587
}
```

#### 自建域名邮箱（SELF_HOSTED）

```json
{
  "provider": "SELF_HOSTED",
  "customApiUrl": "https://mail.yourdomain.com/api",
  "customApiKey": "your-api-key",
  "imapHost": "mail.yourdomain.com",
  "imapPort": 993,
  "smtpHost": "mail.yourdomain.com",
  "smtpPort": 587,
  "enablePlusMode": true
}
```

### 3. 代理配置

#### 启用浏览器代理

```json
{
  "proxyConfig": {
    "enableBrowserProxy": true,
    "browserProxyUrl": "http://proxy-host:8080",
    "enableEmailProxy": false,
    "proxyType": "HTTP"
  }
}
```

#### 启用邮箱代理

```json
{
  "proxyConfig": {
    "enableBrowserProxy": false,
    "enableEmailProxy": true,
    "emailProxyUrl": "socks5://proxy-host:1080",
    "proxyType": "SOCKS5"
  }
}
```

#### 代理认证

```
http://username:password@proxy-host:8080
socks5://username:password@proxy-host:1080
```

### 4. 验证码配置

```json
{
  "captchaMode": "AUTO",
  "captchaTimeout": 300000
}
```

**模式说明**:
- `AUTO` - 自动模式（推荐）：先用 LLM，失败则手动
- `MANUAL` - 手动输入
- `THIRD_PARTY` - 第三方服务（需配置）
- `LLM` - LLM 识别（需配置 API）

### 5. BitBrowser 配置

```json
{
  "bitBrowserConfig": {
    "apiUrl": "http://localhost:54345",
    "apiPort": 54345,
    "profileId": "your-profile-id",
    "enabled": true
  }
}
```

## 准备数据

### 1. 创建账户文件

#### JSON 格式（推荐）

```json
[
  {
    "email": "user@gmail.com",
    "name": "User Name",
    "password": "StrongPassword123!",
    "dateOfBirth": "1990-01-15",
    "username": "username",
    "phone": "+1-234-567-8900"
  }
]
```

#### CSV 格式

```csv
email,name,password,dateOfBirth,username,phone
user@gmail.com,User Name,StrongPassword123!,1990-01-15,username,+1-234-567-8900
```

#### Excel 格式

使用 .xlsx 文件，包含以下列：
- email
- name
- password
- dateOfBirth (格式: YYYY-MM-DD)
- username
- phone

### 2. 使用 Plus 模式

配置 Plus 模式后，系统自动生成：
```
原邮箱: user@gmail.com
自动生成:
  user+test00@gmail.com
  user+test01@gmail.com
  user+test02@gmail.com
  ...
```

所有验证码仍发送到原邮箱 `user@gmail.com`

## 运行应用

### 1. 交互式模式

```bash
# 构建并运行
./gradlew run

# 或直接运行 JAR
java -jar build/libs/auto-x-account-kotlin-1.0.0.jar
```

**菜单选项**:
```
1. 导入账户文件      (JSON/CSV/Excel)
2. 启动批量注册      (开始注册流程)
3. 查看批量状态      (进度和统计)
4. 导出注册结果      (导出成功/失败)
5. 配置代理          (浏览器/邮箱)
6. 配置邮箱          (Gmail/Outlook/自定义)
7. 测试邮箱连接      (验证配置)
8. 退出
```

### 2. 命令行模式

```bash
# 导入账户
java -jar build/libs/auto-x-account-kotlin-1.0.0.jar import json accounts.json

# 启动批量注册
java -jar build/libs/auto-x-account-kotlin-1.0.0.jar register batch-1 accounts.json json

# 参数说明
# register <任务ID> <文件路径> <格式(json|csv|excel)>
```

### 3. 在 IDE 中运行

```bash
# Android Studio / IntelliJ IDEA
# 1. 打开项目
# 2. Run → Run 'Main'

# VS Code（安装 Kotlin 扩展）
# 1. 打开 Main.kt
# 2. 右键 → Run
```

## 常见问题

### Q1: 编译失败 - "Unresolved reference"

**原因**: Gradle 同步失败

**解决**:
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Q2: 邮箱验证码获取失败

**检查项**:
1. 邮箱配置是否正确
2. 网络连接是否正常
3. 邮箱服务器是否可访问
4. IMAP/SMTP 端口是否开放

**测试**:
```bash
# 交互菜单中选择 "7. 测试邮箱连接"
```

### Q3: 代理连接失败

**检查项**:
1. 代理地址和端口是否正确
2. 代理服务器是否运行
3. 防火墙是否阻止

**格式要求**:
```
http://host:port
https://user:pass@host:port
socks5://host:port
```

### Q4: 验证码无法识别

**解决方案**:
1. 将 `captchaMode` 改为 `MANUAL`
2. 或配置第三方验证码服务
3. 或配置 LLM API（OpenAI GPT-4 Vision）

### Q5: 批量注册速度太慢

**优化**:
1. 增加 `maxConcurrency` (默认5)
2. 减少 `retryCount` 或 `retryDelay`
3. 使用代理轮换以避免 IP 限制
4. 增加注册间隔

## 性能优化

### 1. 并发控制

```json
{
  "maxConcurrency": 10,      // 最多10个并发任务
  "taskCheckInterval": 500   // 更频繁地检查任务
}
```

### 2. 超时设置

```json
{
  "requestTimeout": 20000,   // 20秒请求超时
  "captchaTimeout": 180000,  // 3分钟验证码超时
  "retryDelay": 500          // 500ms 重试延迟
}
```

### 3. 日志等级

```json
{
  "debugMode": false         // 生产环境设为 false
}
```

## 监控和日志

### 日志位置

日志输出到控制台和文件（logback 配置）

**重要日志**:
```
[INFO] Starting Twitter X Account Registration System
[INFO] Importing accounts from JSON
[INFO] Starting batch task
[INFO] Processing account: user@gmail.com
[INFO] Account registration successful
[ERROR] Account registration failed
```

### 导出结果

注册完成后导出结果：
```
选项 4 → 选择格式 (json/csv/excel) → 输入文件路径
```

## 最佳实践

### 1. 数据安全

- 不要在代码中硬编码密码
- 使用环境变量或配置文件
- 定期备份配置和结果

### 2. 账户多样性

- 混合使用不同邮箱提供商
- 使用真实的用户信息
- 分散注册时间和地点（使用代理）

### 3. 监控和错误处理

- 定期检查注册状态
- 分析失败原因
- 调整配置参数

### 4. 合规性

- 遵守 X 的服务条款
- 不进行垃圾邮件或滥用
- 使用合法的邮箱和代理
- 尊重速率限制

## 支持和反馈

如有问题或建议：

1. 检查日志输出的错误信息
2. 参考项目 README 和文档
3. 提交 GitHub Issue
4. 联系技术支持

## 下一步

1. ✅ 阅读 `PROJECT_DOCUMENTATION.md` 了解详细 API
2. ✅ 准备邮箱账户和配置
3. ✅ 准备账户数据（JSON/CSV/Excel）
4. ✅ 配置系统参数
5. ✅ 进行小规模测试（5-10 个账户）
6. ✅ 逐步扩大规模
7. ✅ 监控和优化性能

---

**版本**: 1.0.0  
**最后更新**: 2025-11-24
