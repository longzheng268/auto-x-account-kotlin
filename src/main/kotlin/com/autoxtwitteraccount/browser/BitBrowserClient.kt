package com.autoxtwitteraccount.browser

import com.autoxtwitteraccount.config.ConfigManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("BitBrowserClient")

/**
 * BitBrowser 配置数据类
 */
@Serializable
data class BitBrowserProfile(
    val profileId: String,
    val name: String,
    val browserId: String? = null,
    val proxyUrl: String? = null
)

/**
 * BitBrowser API 响应数据类
 */
@Serializable
data class BitBrowserApiResponse(
    val success: Boolean,
    val msg: String? = null,
    val data: String? = null
)

/**
 * BitBrowser 集成客户端
 * 支持通过 BitBrowser API 控制浏览器行为
 */
class BitBrowserClient(
    private val apiUrl: String = "http://localhost:54345",
    private val apiPort: Int = 54345
) {
    private val fullApiUrl = "$apiUrl:$apiPort"
    private val json = Json { ignoreUnknownKeys = true }
    private val httpClient = HttpClient(OkHttp) {
        install(Logging) {
            level = LogLevel.INFO
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    org.slf4j.LoggerFactory.getLogger("BitBrowserHttp").info(message)
                }
            }
        }
    }

    /**
     * 初始化 BitBrowser 连接
     */
    suspend fun initialize(): Boolean {
        logger.info("Initializing BitBrowser connection to $fullApiUrl")

        return try {
            val response = httpClient.get("$fullApiUrl/api/v1/browser/version")
            response.status.value == 200
        } catch (e: Exception) {
            logger.error("Failed to initialize BitBrowser connection", e)
            false
        }
    }

    /**
     * 创建浏览器配置
     */
    suspend fun createProfile(
        name: String,
        browserType: String = "chrome",
        proxyUrl: String? = null
    ): BitBrowserProfile? {
        logger.info("Creating BitBrowser profile: $name")

        return try {
            val requestBody = mapOf(
                "name" to name,
                "browserType" to browserType,
                "proxyUrl" to (proxyUrl ?: "")
            )

            val response = httpClient.post("$fullApiUrl/api/v1/profile/create") {
                // 这里需要添加请求头和请求体
            }

            if (response.status.value == 200) {
                val responseText = response.bodyAsText()
                logger.info("Profile created successfully: $name")
                // 解析响应并返回配置信息
                null  // 需要实现 JSON 解析
            } else {
                logger.error("Failed to create profile: ${response.status}")
                null
            }
        } catch (e: Exception) {
            logger.error("Failed to create profile", e)
            null
        }
    }

    /**
     * 删除浏览器配置
     */
    suspend fun deleteProfile(profileId: String): Boolean {
        logger.info("Deleting BitBrowser profile: $profileId")

        return try {
            val response = httpClient.post("$fullApiUrl/api/v1/profile/delete") {
                // 添加请求体
            }

            response.status.value == 200
        } catch (e: Exception) {
            logger.error("Failed to delete profile", e)
            false
        }
    }

    /**
     * 启动浏览器
     */
    suspend fun startBrowser(profileId: String): Boolean {
        logger.info("Starting browser with profile: $profileId")

        return try {
            val response = httpClient.post("$fullApiUrl/api/v1/browser/start") {
                // 添加请求体
            }

            if (response.status.value == 200) {
                logger.info("Browser started successfully")
                true
            } else {
                logger.error("Failed to start browser: ${response.status}")
                false
            }
        } catch (e: Exception) {
            logger.error("Failed to start browser", e)
            false
        }
    }

    /**
     * 关闭浏览器
     */
    suspend fun closeBrowser(profileId: String): Boolean {
        logger.info("Closing browser with profile: $profileId")

        return try {
            val response = httpClient.post("$fullApiUrl/api/v1/browser/close") {
                // 添加请求体
            }

            if (response.status.value == 200) {
                logger.info("Browser closed successfully")
                true
            } else {
                logger.error("Failed to close browser: ${response.status}")
                false
            }
        } catch (e: Exception) {
            logger.error("Failed to close browser", e)
            false
        }
    }

    /**
     * 获取浏览器状态
     */
    suspend fun getBrowserStatus(profileId: String): Map<String, Any>? {
        logger.info("Getting browser status for profile: $profileId")

        return try {
            val response = httpClient.get("$fullApiUrl/api/v1/browser/status?profileId=$profileId")

            if (response.status.value == 200) {
                val responseText = response.bodyAsText()
                logger.info("Browser status retrieved")
                // 解析响应并返回状态信息
                mapOf("status" to "running")  // 模拟返回
            } else {
                logger.error("Failed to get browser status: ${response.status}")
                null
            }
        } catch (e: Exception) {
            logger.error("Failed to get browser status", e)
            null
        }
    }

    /**
     * 获取调试 WebSocket 端口
     */
    suspend fun getDebugPort(profileId: String): Int? {
        logger.info("Getting debug port for profile: $profileId")

        return try {
            val response = httpClient.get("$fullApiUrl/api/v1/browser/debugPort?profileId=$profileId")

            if (response.status.value == 200) {
                val responseText = response.bodyAsText()
                logger.info("Debug port retrieved")
                // 解析响应并返回端口号
                54321  // 模拟返回
            } else {
                logger.error("Failed to get debug port: ${response.status}")
                null
            }
        } catch (e: Exception) {
            logger.error("Failed to get debug port", e)
            null
        }
    }

    /**
     * 列出所有配置
     */
    suspend fun listProfiles(): List<BitBrowserProfile> {
        logger.info("Listing BitBrowser profiles")

        return try {
            val response = httpClient.get("$fullApiUrl/api/v1/profile/list")

            if (response.status.value == 200) {
                val responseText = response.bodyAsText()
                logger.info("Profiles listed")
                // 解析响应并返回配置列表
                emptyList()  // 模拟返回
            } else {
                logger.error("Failed to list profiles: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            logger.error("Failed to list profiles", e)
            emptyList()
        }
    }

    /**
     * 清理资源
     */
    suspend fun close() {
        logger.info("Closing BitBrowser client")
        try {
            httpClient.close()
            logger.info("BitBrowser client closed successfully")
        } catch (e: Exception) {
            logger.error("Error closing BitBrowser client", e)
        }
    }
}

/**
 * BitBrowser 管理器
 */
object BitBrowserManager {
    private var client: BitBrowserClient? = null

    /**
     * 初始化 BitBrowser
     */
    suspend fun initialize(): Boolean {
        logger.info("Initializing BitBrowser manager")

        val config = ConfigManager.getBitBrowserConfig()
        if (!config.enabled) {
            logger.warn("BitBrowser is not enabled")
            return false
        }

        client = BitBrowserClient(config.apiUrl, config.apiPort)
        return client?.initialize() ?: false
    }

    /**
     * 获取客户端实例
     */
    fun getClient(): BitBrowserClient? = client

    /**
     * 关闭连接
     */
    suspend fun close() {
        logger.info("Closing BitBrowser manager")
        client?.close()
        client = null
    }
}
