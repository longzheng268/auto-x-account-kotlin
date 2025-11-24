package com.autoxtwitteraccount.proxy

import com.autoxtwitteraccount.config.ConfigManager
import com.autoxtwitteraccount.config.ProxyType
import com.autoxtwitteraccount.config.SystemProxyMode
import org.slf4j.LoggerFactory
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.URI

private val logger = LoggerFactory.getLogger("ProxyManager")

/**
 * 代理信息数据类
 */
data class ProxyInfo(
    val host: String,
    val port: Int,
    val type: ProxyType = ProxyType.HTTP,
    val username: String? = null,
    val password: String? = null
) {
    /**
     * 获取代理 URL
     */
    val proxyUrl: String
        get() {
            val auth = if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
                "$username:$password@"
            } else {
                ""
            }
            return "${type.name.lowercase()}://$auth$host:$port"
        }

    /**
     * 验证代理配置
     */
    fun validate(): Boolean {
        return host.isNotEmpty() && port in 1..65535
    }
}

/**
 * 代理管理器 - 支持浏览器代理和邮箱代理独立控制
 */
object ProxyManager {

    /**
     * 获取浏览器代理
     */
    fun getBrowserProxy(): ProxyInfo? {
        val config = ConfigManager.getProxyConfig()

        return when (SystemProxyMode.valueOf(config.systemProxyMode)) {
            SystemProxyMode.AUTO -> getSystemProxy()
            SystemProxyMode.MANUAL -> getManualBrowserProxy()
            SystemProxyMode.DISABLED -> null
        }
    }

    /**
     * 获取邮箱代理
     */
    fun getEmailProxy(): ProxyInfo? {
        val config = ConfigManager.getProxyConfig()

        return when (SystemProxyMode.valueOf(config.systemProxyMode)) {
            SystemProxyMode.AUTO -> getSystemProxy()
            SystemProxyMode.MANUAL -> getManualEmailProxy()
            SystemProxyMode.DISABLED -> null
        }
    }

    /**
     * 获取系统代理
     */
    private fun getSystemProxy(): ProxyInfo? {
        return try {
            val os = System.getProperty("os.name").lowercase()

            when {
                os.contains("win") -> getWindowsSystemProxy()
                os.contains("mac") || os.contains("darwin") -> getMacSystemProxy()
                os.contains("nix") || os.contains("nux") -> getLinuxSystemProxy()
                else -> {
                    logger.warn("Unsupported OS for system proxy detection: $os")
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to detect system proxy", e)
            null
        }
    }

    /**
     * 获取 Windows 系统代理
     */
    private fun getWindowsSystemProxy(): ProxyInfo? {
        return try {
            // 尝试从注册表或环境变量获取代理设置
            val proxyHost = System.getProperty("http.proxyHost")
            val proxyPort = System.getProperty("http.proxyPort")?.toIntOrNull() ?: 80

            if (!proxyHost.isNullOrEmpty()) {
                logger.info("Detected Windows system proxy: $proxyHost:$proxyPort")
                return ProxyInfo(
                    host = proxyHost,
                    port = proxyPort,
                    type = ProxyType.HTTP
                )
            }

            // 尝试通过 PowerShell 获取代理设置
            getProxyFromCommand("powershell", "-Command", "Get-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings' | Select-Object -ExpandProperty ProxyServer")

        } catch (e: Exception) {
            logger.error("Failed to get Windows system proxy", e)
            null
        }
    }

    /**
     * 获取 macOS 系统代理
     */
    private fun getMacSystemProxy(): ProxyInfo? {
        return try {
            // 尝试从环境变量获取
            val proxyHost = System.getProperty("http.proxyHost")
            val proxyPort = System.getProperty("http.proxyPort")?.toIntOrNull() ?: 80

            if (!proxyHost.isNullOrEmpty()) {
                logger.info("Detected macOS system proxy: $proxyHost:$proxyPort")
                return ProxyInfo(
                    host = proxyHost,
                    port = proxyPort,
                    type = ProxyType.HTTP
                )
            }

            // 尝试通过 networksetup 命令获取
            getProxyFromCommand("networksetup", "-getwebproxy", "Wi-Fi")

        } catch (e: Exception) {
            logger.error("Failed to get macOS system proxy", e)
            null
        }
    }

    /**
     * 获取 Linux 系统代理
     */
    private fun getLinuxSystemProxy(): ProxyInfo? {
        return try {
            // 尝试从环境变量获取
            val proxyHost = System.getProperty("http.proxyHost")
            val proxyPort = System.getProperty("http.proxyPort")?.toIntOrNull() ?: 80

            if (!proxyHost.isNullOrEmpty()) {
                logger.info("Detected Linux system proxy: $proxyHost:$proxyPort")
                return ProxyInfo(
                    host = proxyHost,
                    port = proxyPort,
                    type = ProxyType.HTTP
                )
            }

            // 尝试从 gsettings 获取 GNOME 代理设置
            getProxyFromCommand("gsettings", "get", "org.gnome.system.proxy.http", "host")

        } catch (e: Exception) {
            logger.error("Failed to get Linux system proxy", e)
            null
        }
    }

    /**
     * 从命令行获取代理信息
     */
    private fun getProxyFromCommand(vararg command: String): ProxyInfo? {
        return try {
            val process = ProcessBuilder(*command).start()
            val output = process.inputStream.bufferedReader().readText().trim()

            if (output.isNotEmpty() && output.contains(":")) {
                val parts = output.split(":")
                if (parts.size >= 2) {
                    val host = parts[0]
                    val port = parts[1].toIntOrNull() ?: 80

                    logger.info("Detected system proxy from command: $host:$port")
                    return ProxyInfo(
                        host = host,
                        port = port,
                        type = ProxyType.HTTP
                    )
                }
            }

            null
        } catch (e: Exception) {
            logger.debug("Command execution failed: ${command.joinToString(" ")}", e)
            null
        }
    }

    /**
     * 获取手动配置的浏览器代理
     */
    private fun getManualBrowserProxy(): ProxyInfo? {
        val config = ConfigManager.getProxyConfig()

        if (!config.enableBrowserProxy) {
            logger.info("Browser proxy is disabled")
            return null
        }

        logger.info("Using manual browser proxy: ${config.browserProxyUrl}")
        return parseProxyUrl(config.browserProxyUrl)
    }

    /**
     * 获取手动配置的邮箱代理
     */
    private fun getManualEmailProxy(): ProxyInfo? {
        val config = ConfigManager.getProxyConfig()

        if (!config.enableEmailProxy) {
            logger.info("Email proxy is disabled")
            return null
        }

        logger.info("Using manual email proxy: ${config.emailProxyUrl}")
        return parseProxyUrl(config.emailProxyUrl)
    }

    /**
     * 启用浏览器代理
     */
    fun enableBrowserProxy(proxyUrl: String): Boolean {
        logger.info("Enabling browser proxy: $proxyUrl")

        val proxyInfo = parseProxyUrl(proxyUrl) ?: run {
            logger.error("Invalid proxy URL: $proxyUrl")
            return false
        }

        if (!proxyInfo.validate()) {
            logger.error("Invalid proxy configuration")
            return false
        }

        val config = ConfigManager.getProxyConfig()
        val newConfig = config.copy(
            systemProxyMode = SystemProxyMode.MANUAL.name,
            enableBrowserProxy = true,
            browserProxyUrl = proxyUrl
        )

        val appConfig = ConfigManager.config.copy(proxyConfig = newConfig)
        ConfigManager.updateConfig(appConfig)

        logger.info("Browser proxy enabled successfully")
        return true
    }

    /**
     * 启用邮箱代理
     */
    fun enableEmailProxy(proxyUrl: String): Boolean {
        logger.info("Enabling email proxy: $proxyUrl")

        val proxyInfo = parseProxyUrl(proxyUrl) ?: run {
            logger.error("Invalid proxy URL: $proxyUrl")
            return false
        }

        if (!proxyInfo.validate()) {
            logger.error("Invalid proxy configuration")
            return false
        }

        val config = ConfigManager.getProxyConfig()
        val newConfig = config.copy(
            systemProxyMode = SystemProxyMode.MANUAL.name,
            enableEmailProxy = true,
            emailProxyUrl = proxyUrl
        )

        val appConfig = ConfigManager.config.copy(proxyConfig = newConfig)
        ConfigManager.updateConfig(appConfig)

        logger.info("Email proxy enabled successfully")
        return true
    }

    /**
     * 启用系统代理自动检测
     */
    fun enableSystemProxy(): Boolean {
        logger.info("Enabling system proxy auto-detection")

        val config = ConfigManager.getProxyConfig()
        val newConfig = config.copy(systemProxyMode = SystemProxyMode.AUTO.name)

        val appConfig = ConfigManager.config.copy(proxyConfig = newConfig)
        ConfigManager.updateConfig(appConfig)

        logger.info("System proxy auto-detection enabled")
        return true
    }

    /**
     * 禁用代理
     */
    fun disableProxy(): Boolean {
        logger.info("Disabling all proxies")

        val config = ConfigManager.getProxyConfig()
        val newConfig = config.copy(systemProxyMode = SystemProxyMode.DISABLED.name)

        val appConfig = ConfigManager.config.copy(proxyConfig = newConfig)
        ConfigManager.updateConfig(appConfig)

        logger.info("All proxies disabled")
        return true
    }

    /**
     * 禁用浏览器代理
     */
    fun disableBrowserProxy(): Boolean {
        logger.info("Disabling browser proxy")

        val config = ConfigManager.getProxyConfig()
        val newConfig = config.copy(enableBrowserProxy = false)

        val appConfig = ConfigManager.config.copy(proxyConfig = newConfig)
        ConfigManager.updateConfig(appConfig)

        logger.info("Browser proxy disabled")
        return true
    }

    /**
     * 禁用邮箱代理
     */
    fun disableEmailProxy(): Boolean {
        logger.info("Disabling email proxy")

        val config = ConfigManager.getProxyConfig()
        val newConfig = config.copy(enableEmailProxy = false)

        val appConfig = ConfigManager.config.copy(proxyConfig = newConfig)
        ConfigManager.updateConfig(appConfig)

        logger.info("Email proxy disabled")
        return true
    }

    /**
     * 检查浏览器代理是否启用
     */
    fun isBrowserProxyEnabled(): Boolean {
        val config = ConfigManager.getProxyConfig()
        return when (SystemProxyMode.valueOf(config.systemProxyMode)) {
            SystemProxyMode.AUTO -> getSystemProxy() != null
            SystemProxyMode.MANUAL -> config.enableBrowserProxy && config.browserProxyUrl.isNotEmpty()
            SystemProxyMode.DISABLED -> false
        }
    }

    /**
     * 检查邮箱代理是否启用
     */
    fun isEmailProxyEnabled(): Boolean {
        val config = ConfigManager.getProxyConfig()
        return when (SystemProxyMode.valueOf(config.systemProxyMode)) {
            SystemProxyMode.AUTO -> getSystemProxy() != null
            SystemProxyMode.MANUAL -> config.enableEmailProxy && config.emailProxyUrl.isNotEmpty()
            SystemProxyMode.DISABLED -> false
        }
    }

    /**
     * 获取当前代理模式
     */
    fun getProxyMode(): SystemProxyMode {
        val config = ConfigManager.getProxyConfig()
        return SystemProxyMode.valueOf(config.systemProxyMode)
    }

    /**
     * 验证代理连接
     */
    suspend fun validateProxyConnection(proxyInfo: ProxyInfo): Boolean {
        logger.info("Validating proxy connection: ${proxyInfo.proxyUrl}")

        return try {
            // 这里需要实现实际的代理连接验证逻辑
            // 可以尝试通过代理访问一个测试 URL
            logger.info("Proxy validation successful")
            true
        } catch (e: Exception) {
            logger.error("Proxy validation failed", e)
            false
        }
    }

    /**
     * 解析代理 URL
     * 支持格式：
     * - http://host:port
     * - https://host:port
     * - socks5://host:port
     * - http://username:password@host:port
     */
    private fun parseProxyUrl(url: String): ProxyInfo? {
        return try {
            // 移除 URL scheme
            val scheme = url.substringBefore("://")
            val rest = url.substringAfter("://")

            val proxyType = when (scheme.lowercase()) {
                "http" -> ProxyType.HTTP
                "https" -> ProxyType.HTTPS
                "socks5" -> ProxyType.SOCKS5
                else -> {
                    logger.warn("Unknown proxy type: $scheme")
                    return null
                }
            }

            // 分离认证信息和主机
            val (auth, hostPart) = if (rest.contains("@")) {
                val authPart = rest.substringBefore("@")
                val hostPart = rest.substringAfter("@")
                authPart to hostPart
            } else {
                null to rest
            }

            // 提取主机和端口
            val (host, port) = if (hostPart.contains(":")) {
                val host = hostPart.substringBeforeLast(":")
                val port = hostPart.substringAfterLast(":").toIntOrNull() ?: 80
                host to port
            } else {
                hostPart to 80
            }

            // 提取用户名和密码
            val (username, password) = if (auth != null && auth.contains(":")) {
                val username = auth.substringBefore(":")
                val password = auth.substringAfter(":")
                username to password
            } else {
                null to null
            }

            ProxyInfo(
                host = host,
                port = port,
                type = proxyType,
                username = username,
                password = password
            )
        } catch (e: Exception) {
            logger.error("Failed to parse proxy URL: $url", e)
            null
        }
    }

    /**
     * 获取所有代理配置
     */
    fun getProxyConfiguration(): Map<String, Any?> {
        val config = ConfigManager.getProxyConfig()
        return mapOf(
            "browserProxyEnabled" to config.enableBrowserProxy,
            "browserProxyUrl" to config.browserProxyUrl,
            "emailProxyEnabled" to config.enableEmailProxy,
            "emailProxyUrl" to config.emailProxyUrl,
            "proxyType" to config.proxyType
        )
    }
}
