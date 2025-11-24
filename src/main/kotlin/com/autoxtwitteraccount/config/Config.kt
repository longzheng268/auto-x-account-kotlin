package com.autoxtwitteraccount.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 验证码模式枚举
 */
enum class CaptchaMode {
    @SerialName("auto")
    AUTO,                    // 自动模式（默认）
    @SerialName("manual")
    MANUAL,                  // 手动输入
    @SerialName("third_party")
    THIRD_PARTY,            // 第三方验证码服务
    @SerialName("llm")
    LLM                     // 大语言模型识别
}

/**
 * 邮箱提供商类型
 */
enum class EmailProvider {
    GMAIL,                  // Gmail
    OUTLOOK,                // Outlook
    CUSTOM,                 // 自定义 IMAP/SMTP
    SELF_HOSTED            // 自建域名邮箱 API
}

/**
 * 代理类型
 */
enum class ProxyType {
    HTTP,
    HTTPS,
    SOCKS4,
    SOCKS5
}

/**
 * 系统代理检测模式
 */
enum class SystemProxyMode {
    AUTO,           // 自动检测系统代理
    MANUAL,         // 手动配置
    DISABLED        // 禁用代理
}

/**
 * 邮箱配置数据类
 */
@Serializable
data class EmailConfig(
    val provider: String = "GMAIL",                    // 邮箱提供商
    val emailAddress: String = "",                     // 邮箱地址
    val password: String = "",                         // 邮箱密码
    val enablePlusMode: Boolean = true,               // 是否启用 Plus 模式
    val plusSuffix: String = "+test",                 // Plus 模式后缀
    val baseEmail: String = "",                       // 原邮箱地址（用于 Plus 模式）
    val useHumanNames: Boolean = true,                // 是否使用人名作为后缀
    val customNames: List<String> = listOf(           // 自定义人名列表
        "john", "mary", "david", "lisa", "michael", "sarah", "james", "emma",
        "william", "olivia", "benjamin", "ava", "lucas", "mia", "henry", "charlotte"
    ),
    val imapHost: String = "imap.gmail.com",          // IMAP 服务器
    val imapPort: Int = 993,                          // IMAP 端口
    val smtpHost: String = "smtp.gmail.com",          // SMTP 服务器
    val smtpPort: Int = 587,                          // SMTP 端口
    val customApiUrl: String = "",                    // 自定义 API URL
    val customApiKey: String = ""                     // 自定义 API 密钥
)

/**
 * 代理配置数据类
 */
@Serializable
data class ProxyConfig(
    val systemProxyMode: String = "AUTO",             // 系统代理模式：AUTO/MANUAL/DISABLED
    val enableBrowserProxy: Boolean = false,           // 是否启用浏览器代理
    val enableEmailProxy: Boolean = false,             // 是否启用邮箱代理
    val browserProxyUrl: String = "",                  // 浏览器代理 URL
    val emailProxyUrl: String = "",                    // 邮箱代理 URL
    val proxyType: String = "HTTP",                    // 代理类型
    val autoDetectSystemProxy: Boolean = true          // 是否自动检测系统代理
)

/**
 * BitBrowser 配置数据类
 */
@Serializable
data class BitBrowserConfig(
    val apiUrl: String = "http://localhost:54345",    // BitBrowser API URL
    val apiPort: Int = 54345,                         // BitBrowser API 端口
    val profileId: String = "",                       // 浏览器配置 ID
    val enabled: Boolean = false                       // 是否启用 BitBrowser
)

/**
 * 获取应用程序配置目录
 * 遵循各操作系统的软件数据存储规范
 */
private fun getConfigDirectory(): String {
    val appName = "AutoXAccount"
    val os = System.getProperty("os.name").lowercase()

    return when {
        os.contains("win") -> {
            // Windows: %APPDATA%\AppName\
            val appData = System.getenv("APPDATA")
            if (appData != null) {
                Paths.get(appData, appName).toString()
            } else {
                Paths.get(System.getProperty("user.home"), "AppData", "Roaming", appName).toString()
            }
        }
        os.contains("mac") -> {
            // macOS: ~/Library/Application Support/AppName/
            Paths.get(System.getProperty("user.home"), "Library", "Application Support", appName).toString()
        }
        else -> {
            // Linux/Unix: ~/.config/AppName/
            val xdgConfigHome = System.getenv("XDG_CONFIG_HOME")
            if (xdgConfigHome != null) {
                Paths.get(xdgConfigHome, appName).toString()
            } else {
                Paths.get(System.getProperty("user.home"), ".config", appName).toString()
            }
        }
    }
}

/**
 * 获取配置文件路径
 */
private fun getConfigFilePath(): String {
    return Paths.get(getConfigDirectory(), "config.json").toString()
}

/**
 * JSON 序列化器配置
 */
private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * 加载配置文件
 */
private fun loadConfig(): AppConfig {
    val configFile = File(getConfigFilePath())
    return try {
        if (configFile.exists()) {
            val jsonText = configFile.readText()
            json.decodeFromString<AppConfig>(jsonText)
        } else {
            // 配置文件不存在，创建默认配置
            val defaultConfig = AppConfig()
            saveConfig(defaultConfig)
            defaultConfig
        }
    } catch (e: Exception) {
        LoggerFactory.getLogger("ConfigManager").warn("Failed to load config, using defaults: ${e.message}")
        AppConfig()
    }
}

/**
 * 保存配置文件
 */
private fun saveConfig(config: AppConfig) {
    try {
        val configDir = File(getConfigDirectory())
        if (!configDir.exists()) {
            configDir.mkdirs()
        }

        val configFile = File(getConfigFilePath())
        val jsonText = json.encodeToString(AppConfig.serializer(), config)
        configFile.writeText(jsonText)
    } catch (e: Exception) {
        LoggerFactory.getLogger("ConfigManager").error("Failed to save config: ${e.message}")
    }
}

/**
 * 全局配置管理器
 */
object ConfigManager {
    private val logger = LoggerFactory.getLogger("ConfigManager")
    private var _config: AppConfig

    init {
        logger.info("Initializing ConfigManager...")
        _config = loadConfig()
        logger.info("Config loaded from: ${getConfigFilePath()}")
    }

    /**
     * 获取当前配置
     */
    val config: AppConfig
        get() = _config

    /**
     * 更新配置（自动保存到文件）
     */
    fun updateConfig(newConfig: AppConfig) {
        _config = newConfig
        saveConfig(newConfig)
        logger.debug("Configuration updated and saved")
    }

    /**
     * 获取邮箱配置
     */
    fun getEmailConfig(): EmailConfig = _config.emailConfig

    /**
     * 获取代理配置
     */
    fun getProxyConfig(): ProxyConfig = _config.proxyConfig

    /**
     * 获取 BitBrowser 配置
     */
    fun getBitBrowserConfig(): BitBrowserConfig = _config.bitBrowserConfig

    /**
     * 获取验证码模式
     */
    fun getCaptchaMode(): CaptchaMode = CaptchaMode.valueOf(_config.captchaMode)

    /**
     * 检查是否启用 Plus 模式
     */
    fun isPlusModeEnabled(): Boolean = _config.emailConfig.enablePlusMode

    /**
     * 检查是否启用浏览器代理
     */
    fun isBrowserProxyEnabled(): Boolean = _config.proxyConfig.enableBrowserProxy

    /**
     * 检查是否启用邮箱代理
     */
    fun isEmailProxyEnabled(): Boolean = _config.proxyConfig.enableEmailProxy
}
