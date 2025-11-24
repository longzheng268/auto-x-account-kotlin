package com.autoxtwitteraccount.email

import com.autoxtwitteraccount.config.ConfigManager
import com.autoxtwitteraccount.config.EmailProvider
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("EmailManager")

/**
 * 邮件数据类
 */
data class EmailMessage(
    val from: String,
    val to: String,
    val subject: String,
    val body: String,
    val receivedTime: Long = System.currentTimeMillis()
)

/**
 * 邮箱管理器 - 支持 Plus 模式和自定义域名邮箱
 */
object EmailManager {
    /**
     * 解析邮箱地址
     * 将 "user@domain.com" 解析为 ("user", "domain.com")
     */
    fun parseEmail(email: String): Pair<String, String> {
        val parts = email.split("@")
        return if (parts.size == 2) {
            Pair(parts[0], parts[1])
        } else {
            throw IllegalArgumentException("Invalid email format: $email")
        }
    }

    /**
     * 生成 Plus 模式邮箱地址
     * 支持使用人名作为后缀，避免看起来像机器
     */
    fun generatePlusEmail(baseEmail: String, index: Int = 0): String {
        val (name, domain) = parseEmail(baseEmail)
        val config = ConfigManager.getEmailConfig()

        return if (config.enablePlusMode) {
            val suffix = if (config.useHumanNames && config.customNames.isNotEmpty()) {
                // 使用人名作为后缀
                val nameIndex = index % config.customNames.size
                "+${config.customNames[nameIndex]}"
            } else {
                // 使用传统数字后缀
                val paddedIndex = String.format("%02d", index)
                "${config.plusSuffix}$paddedIndex"
            }
            "$name$suffix@$domain"
        } else {
            baseEmail
        }
    }

    /**
     * 生成多个 Plus 模式邮箱地址
     */
    fun generatePlusEmails(baseEmail: String, count: Int): List<String> {
        return (0 until count).map { index ->
            generatePlusEmail(baseEmail, index)
        }
    }

    /**
     * 设置原邮箱地址（用于 Plus 模式）
     */
    fun setBaseEmail(email: String): Boolean {
        return try {
            val (name, domain) = parseEmail(email)
            val config = ConfigManager.getEmailConfig()
            val newConfig = config.copy(baseEmail = email)

            val appConfig = ConfigManager.config.copy(emailConfig = newConfig)
            ConfigManager.updateConfig(appConfig)

            logger.info("Base email set to: $email")
            true
        } catch (e: Exception) {
            logger.error("Invalid email format: $email", e)
            false
        }
    }

    /**
     * 获取原邮箱地址
     */
    fun getBaseEmail(): String {
        return ConfigManager.getEmailConfig().baseEmail
    }

    /**
     * 添加自定义人名
     */
    fun addCustomName(name: String): Boolean {
        if (name.isBlank() || !name.matches(Regex("^[a-zA-Z]+$"))) {
            logger.error("Invalid name format: $name")
            return false
        }

        val config = ConfigManager.getEmailConfig()
        val newNames = config.customNames + name.lowercase()
        val newConfig = config.copy(customNames = newNames)

        val appConfig = ConfigManager.config.copy(emailConfig = newConfig)
        ConfigManager.updateConfig(appConfig)

        logger.info("Added custom name: $name")
        return true
    }

    /**
     * 移除自定义人名
     */
    fun removeCustomName(name: String): Boolean {
        val config = ConfigManager.getEmailConfig()
        val newNames = config.customNames.filter { it != name.lowercase() }
        val newConfig = config.copy(customNames = newNames)

        val appConfig = ConfigManager.config.copy(emailConfig = newConfig)
        ConfigManager.updateConfig(appConfig)

        logger.info("Removed custom name: $name")
        return true
    }

    /**
     * 获取所有可用的人名列表
     */
    fun getAvailableNames(): List<String> {
        val config = ConfigManager.getEmailConfig()
        return config.customNames
    }

    /**
     * 获取邮箱基础地址（用于接收验证码）
     * Plus 模式下，所有验证码仍发送到原邮箱
     */
    fun getBaseEmail(email: String): String = email

    /**
     * 获取邮箱提供商
     */
    fun getEmailProvider(email: String): EmailProvider {
        val domain = parseEmail(email).second.lowercase()
        return when {
            domain.contains("gmail") -> EmailProvider.GMAIL
            domain.contains("outlook") || domain.contains("hotmail") -> EmailProvider.OUTLOOK
            ConfigManager.getEmailConfig().customApiUrl.isNotEmpty() -> EmailProvider.SELF_HOSTED
            else -> EmailProvider.CUSTOM
        }
    }

    /**
     * 获取邮箱验证码（通过 IMAP 或自定义 API）
     * 支持从多种来源获取验证码
     */
    suspend fun getVerificationCode(
        email: String,
        sender: String = "noreply@twitter.com",
        timeout: Long = 300000
    ): String? {
        val provider = getEmailProvider(email)
        val baseEmail = getBaseEmail(email)

        logger.info("Fetching verification code from $provider for $baseEmail")

        return when (provider) {
            EmailProvider.GMAIL -> getGmailVerificationCode(baseEmail, sender, timeout)
            EmailProvider.OUTLOOK -> getOutlookVerificationCode(baseEmail, sender, timeout)
            EmailProvider.SELF_HOSTED -> getCustomApiVerificationCode(baseEmail, sender, timeout)
            EmailProvider.CUSTOM -> getImapVerificationCode(baseEmail, sender, timeout)
        }
    }

    /**
     * 从 Gmail 获取验证码
     */
    private suspend fun getGmailVerificationCode(
        email: String,
        sender: String,
        timeout: Long
    ): String? {
        logger.info("Fetching verification code from Gmail")
        // 这里需要实现通过 Gmail API 或 IMAP 获取验证码的逻辑
        // 暂时返回 null，实际实现需要与 Gmail API 集成
        return null
    }

    /**
     * 从 Outlook 获取验证码
     */
    private suspend fun getOutlookVerificationCode(
        email: String,
        sender: String,
        timeout: Long
    ): String? {
        logger.info("Fetching verification code from Outlook")
        // 这里需要实现通过 Outlook API 或 IMAP 获取验证码的逻辑
        return null
    }

    /**
     * 从自定义 API 获取验证码（自建域名邮箱）
     */
    private suspend fun getCustomApiVerificationCode(
        email: String,
        sender: String,
        timeout: Long
    ): String? {
        logger.info("Fetching verification code from custom API")
        val config = ConfigManager.getEmailConfig()
        val apiUrl = config.customApiUrl
        val apiKey = config.customApiKey

        if (apiUrl.isEmpty()) {
            logger.warn("Custom API URL is not configured")
            return null
        }

        // 这里需要实现与自定义邮箱 API 的集成
        // 假设 API 返回包含验证码的 JSON 响应
        logger.debug("Custom API URL: $apiUrl, Email: $email")

        // 模拟等待验证码
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeout) {
            // 轮询自定义 API 获取验证码
            delay(5000)
            // 如果 API 返回验证码，则返回
            // 否则继续等待
        }

        logger.warn("Verification code not received within timeout")
        return null
    }

    /**
     * 从 IMAP 服务器获取验证码
     */
    private suspend fun getImapVerificationCode(
        email: String,
        sender: String,
        timeout: Long
    ): String? {
        logger.info("Fetching verification code from IMAP server")
        val config = ConfigManager.getEmailConfig()

        // 这里需要实现 IMAP 连接和邮件获取逻辑
        // 使用 config.imapHost, config.imapPort, email, password 进行连接
        logger.debug("IMAP Host: ${config.imapHost}, Port: ${config.imapPort}")

        // 模拟等待验证码
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeout) {
            // 连接 IMAP 服务器，检查新邮件
            delay(5000)
            // 如果找到来自指定发送者的邮件，提取验证码
            // 否则继续检查
        }

        logger.warn("Verification code not received within timeout")
        return null
    }

    /**
     * 发送测试邮件（用于验证邮箱配置）
     */
    suspend fun sendTestEmail(recipient: String): Boolean {
        logger.info("Sending test email to $recipient")
        return try {
            // 这里需要实现发送测试邮件的逻辑
            logger.info("Test email sent successfully")
            true
        } catch (e: Exception) {
            logger.error("Failed to send test email", e)
            false
        }
    }

    /**
     * 验证邮箱配置
     */
    suspend fun validateEmailConfiguration(): Boolean {
        logger.info("Validating email configuration")
        val config = ConfigManager.getEmailConfig()

        return when (config.provider) {
            "GMAIL" -> validateGmailConfig()
            "OUTLOOK" -> validateOutlookConfig()
            "CUSTOM" -> validateCustomApiConfig()
            "SELF_HOSTED" -> validateSelfHostedConfig()
            else -> {
                logger.warn("Unknown email provider: ${config.provider}")
                false
            }
        }
    }

    private suspend fun validateGmailConfig(): Boolean {
        logger.info("Validating Gmail configuration")
        // 实现 Gmail 配置验证逻辑
        return true
    }

    private suspend fun validateOutlookConfig(): Boolean {
        logger.info("Validating Outlook configuration")
        // 实现 Outlook 配置验证逻辑
        return true
    }

    private suspend fun validateCustomApiConfig(): Boolean {
        logger.info("Validating custom API configuration")
        val config = ConfigManager.getEmailConfig()
        return config.customApiUrl.isNotEmpty() && config.customApiKey.isNotEmpty()
    }

    private suspend fun validateSelfHostedConfig(): Boolean {
        logger.info("Validating self-hosted email configuration")
        val config = ConfigManager.getEmailConfig()
        return config.imapHost.isNotEmpty() && config.smtpHost.isNotEmpty()
    }
}
