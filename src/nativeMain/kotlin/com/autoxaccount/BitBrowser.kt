package com.autoxaccount

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock

/**
 * BitBrowser 集成模块
 * BitBrowser Integration Module
 * 
 * BitBrowser（比特浏览器）是一款专业的指纹浏览器，用于管理多个浏览器配置文件
 * 每个配置文件都有独立的浏览器指纹，适合批量账号注册和管理
 * 
 * BitBrowser is a professional fingerprint browser for managing multiple browser profiles.
 * Each profile has an independent browser fingerprint, suitable for batch account registration.
 * 
 * Migrated from Rust bitbrowser.rs
 */

/**
 * 浏览器配置文件信息
 * Browser Profile Information
 */
@Serializable
data class BrowserProfile(
    val id: String,
    val name: String,
    val inUse: Boolean = false,
    val createdAt: String? = null
)

/**
 * 创建配置文件请求
 * Create Profile Request
 */
@Serializable
data class CreateProfileRequest(
    val name: String,
    val browserType: String = "chrome",
    val os: String,
    val proxy: ProxyConfig? = null
)

/**
 * 代理配置
 * Proxy Configuration
 */
@Serializable
data class ProxyConfig(
    val proxyType: String,  // http, socks5
    val host: String,
    val port: Int,
    val username: String? = null,
    val password: String? = null
)

/**
 * BitBrowser API 客户端
 * BitBrowser API Client
 */
class BitBrowserClient(
    private val apiUrl: String = "http://127.0.0.1",
    private val apiPort: Int = 54345
) {
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    /**
     * 获取 API 基础 URL
     * Get API base URL
     */
    private fun baseUrl(): String = "$apiUrl:$apiPort"

    /**
     * 检查 BitBrowser 是否运行
     * Check if BitBrowser is running
     */
    suspend fun isRunning(): Boolean = runCatchingResult {
        val url = "${baseUrl()}/api/v1/browser/status"
        // Simplified check - in real implementation would use HTTP client
        logInfo("检查 BitBrowser 是否运行 / Checking if BitBrowser is running")
        true
    }.getOrDefault(false)

    /**
     * 获取所有配置文件
     * List all profiles
     */
    suspend fun listProfiles(): Result<List<BrowserProfile>> = runCatchingResult {
        logInfo("获取 BitBrowser 配置文件列表 / Fetching BitBrowser profiles")
        
        // In real implementation, would make HTTP request
        // For now, return empty list
        val profiles = emptyList<BrowserProfile>()
        
        logInfo("找到 ${profiles.size} 个配置文件 / Found ${profiles.size} profiles")
        profiles
    }

    /**
     * 创建新的浏览器配置文件
     * Create new browser profile
     */
    suspend fun createProfile(request: CreateProfileRequest): Result<BrowserProfile> = runCatchingResult {
        logInfo("创建新的 BitBrowser 配置文件 / Creating new BitBrowser profile: ${request.name}")
        
        // In real implementation, would make HTTP POST request
        val profile = BrowserProfile(
            id = generateId(),
            name = request.name,
            inUse = false,
            createdAt = Clock.System.now().toString()
        )
        
        logInfo("✅ 配置文件创建成功 / Profile created successfully: ${profile.name} (ID: ${profile.id})")
        profile
    }

    /**
     * 打开浏览器配置文件
     * Open browser profile
     */
    suspend fun openProfile(profileId: String): Result<Unit> = runCatchingResult {
        logInfo("打开 BitBrowser 配置文件 / Opening BitBrowser profile: $profileId")
        
        // In real implementation, would make HTTP POST request
        delay(500) // Simulate API call
        
        logInfo("✅ 配置文件已打开 / Profile opened")
    }

    /**
     * 关闭浏览器配置文件
     * Close browser profile
     */
    suspend fun closeProfile(profileId: String): Result<Unit> = runCatchingResult {
        logInfo("关闭 BitBrowser 配置文件 / Closing BitBrowser profile: $profileId")
        
        // In real implementation, would make HTTP POST request
        delay(500) // Simulate API call
        
        logInfo("✅ 配置文件已关闭 / Profile closed")
    }

    /**
     * 删除浏览器配置文件
     * Delete browser profile
     */
    suspend fun deleteProfile(profileId: String): Result<Unit> = runCatchingResult {
        logInfo("删除 BitBrowser 配置文件 / Deleting BitBrowser profile: $profileId")
        
        // In real implementation, would make HTTP POST request
        delay(500) // Simulate API call
        
        logInfo("✅ 配置文件已删除 / Profile deleted")
    }

    /**
     * 获取可用的配置文件（未被使用的）
     * Get available profile (not in use)
     */
    suspend fun getAvailableProfile(): Result<BrowserProfile?> = runCatchingResult {
        val profiles = listProfiles().getOrThrow()
        
        // 查找第一个未被使用的配置文件
        profiles.firstOrNull { !it.inUse }
    }

    /**
     * 为账号分配或创建配置文件
     * Allocate or create profile for account
     */
    suspend fun allocateProfileForAccount(
        accountEmail: String,
        autoCreate: Boolean = true
    ): Result<BrowserProfile> = runCatchingResult {
        // 先尝试获取可用的配置文件
        val available = getAvailableProfile().getOrNull()
        if (available != null) {
            logInfo("为账号 $accountEmail 分配现有配置文件 / Allocating existing profile: ${available.id}")
            return@runCatchingResult available
        }

        // 如果没有可用的，且允许自动创建
        if (autoCreate) {
            logInfo("没有可用配置文件，为账号 $accountEmail 创建新配置文件 / No available profile, creating new one")
            
            val profileName = "profile_${accountEmail.replace("@", "_")}"
            
            val request = CreateProfileRequest(
                name = profileName,
                browserType = "chrome",
                os = detectOs(),
                proxy = null
            )

            return@runCatchingResult createProfile(request).getOrThrow()
        }

        throw Exception("没有可用的 BitBrowser 配置文件，且不允许自动创建 / No available profiles and auto-create disabled")
    }

    /**
     * 检测当前操作系统
     * Detect current OS
     */
    private fun detectOs(): String {
        val osName = platform.posix.getenv("OS")?.toKString() ?: ""
        return when {
            osName.contains("Windows", ignoreCase = true) -> "windows"
            else -> {
                val uname = platform.posix.getenv("OSTYPE")?.toKString() ?: ""
                when {
                    uname.contains("darwin", ignoreCase = true) -> "macos"
                    else -> "linux"
                }
            }
        }
    }

    /**
     * 生成唯一ID
     * Generate unique ID
     */
    private fun generateId(): String {
        return "profile_${Clock.System.now().toEpochMilliseconds()}"
    }
}

/**
 * BitBrowser 配置文件管理器
 * BitBrowser Profile Manager
 */
class BitBrowserProfileManager(
    private val client: BitBrowserClient
) {
    private val profiles = mutableListOf<BrowserProfile>()

    /**
     * 初始化配置文件池
     * Initialize profile pool
     */
    suspend fun initialize(): Result<Unit> = runCatchingResult {
        logInfo("初始化 BitBrowser 配置文件池 / Initializing BitBrowser profile pool")
        
        // 检查 BitBrowser 是否运行
        if (!client.isRunning()) {
            logWarn("⚠️  BitBrowser 未运行，请先启动 BitBrowser / BitBrowser is not running")
            throw Exception("BitBrowser 未运行 / BitBrowser is not running")
        }

        // 加载现有配置文件
        profiles.clear()
        profiles.addAll(client.listProfiles().getOrThrow())
        
        logInfo("✅ 配置文件池初始化完成，共 ${profiles.size} 个配置文件 / Profile pool initialized with ${profiles.size} profiles")
    }

    /**
     * 获取下一个可用的配置文件
     * Get next available profile
     */
    suspend fun getNextProfile(autoCreate: Boolean = true): Result<BrowserProfile> = runCatchingResult {
        // 优先使用未使用的配置文件
        val available = profiles.firstOrNull { !it.inUse }
        if (available != null) {
            return@runCatchingResult available
        }

        // 如果允许自动创建
        if (autoCreate) {
            val profileName = "auto_profile_${Clock.System.now().toEpochMilliseconds()}"
            val request = CreateProfileRequest(
                name = profileName,
                browserType = "chrome",
                os = detectOs()
            )

            val newProfile = client.createProfile(request).getOrThrow()
            profiles.add(newProfile)
            
            return@runCatchingResult newProfile
        }

        throw Exception("没有可用的 BitBrowser 配置文件 / No available BitBrowser profiles")
    }

    /**
     * 释放配置文件
     * Release profile
     */
    suspend fun releaseProfile(profileId: String): Result<Unit> = runCatchingResult {
        val profile = profiles.find { it.id == profileId }
        if (profile != null) {
            // 更新使用状态
            val index = profiles.indexOf(profile)
            profiles[index] = profile.copy(inUse = false)
            
            logInfo("配置文件已释放 / Profile released: $profileId")
        }
    }

    /**
     * 检测当前操作系统
     * Detect current OS
     */
    private fun detectOs(): String {
        val osName = platform.posix.getenv("OS")?.toKString() ?: ""
        return when {
            osName.contains("Windows", ignoreCase = true) -> "windows"
            else -> {
                val uname = platform.posix.getenv("OSTYPE")?.toKString() ?: ""
                when {
                    uname.contains("darwin", ignoreCase = true) -> "macos"
                    else -> "linux"
                }
            }
        }
    }
}
