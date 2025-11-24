package com.autoxtwitteraccount.browser

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.slf4j.LoggerFactory
import java.nio.file.Paths

private val logger = LoggerFactory.getLogger("ChromiumBrowser")

/**
 * Chromium 浏览器控制器 - 基于 Playwright
 */
class ChromiumBrowser(
    private val headless: Boolean = false,
    private val proxyUrl: String? = null
) {
    private var playwright: Playwright? = null
    private var browser: Browser? = null
    private val pages = mutableMapOf<String, Page>()
    
    /**
     * 启动浏览器
     */
    fun launch(): Boolean {
        return try {
            logger.info("Launching Chromium browser...")
            
            playwright = Playwright.create()
            
            val launchOptions = BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setArgs(listOf("--disable-blink-features=AutomationControlled"))
            
            if (proxyUrl != null) {
                // 代理 URL 配置
                logger.info("Configuring proxy: $proxyUrl")
            }
            
            browser = playwright!!.chromium().launch(launchOptions)
            logger.info("Chromium browser launched successfully")
            true
        } catch (e: Exception) {
            logger.error("Failed to launch Chromium browser", e)
            false
        }
    }
    
    /**
     * 创建新页面
     */
    fun createPage(pageId: String = "default"): Page? {
        return try {
            if (browser == null) {
                logger.error("Browser not launched")
                return null
            }
            
            val context = browser!!.newContext()
            val page = context.newPage()
            
            // 设置视口大小
            page.setViewportSize(1920, 1080)
            
            // 模拟真实浏览器
            page.evaluate("""
                Object.defineProperty(navigator, 'webdriver', {
                  get: () => false,
                });
            """.trimIndent())
            
            pages[pageId] = page
            logger.info("Created page: $pageId")
            page
        } catch (e: Exception) {
            logger.error("Failed to create page", e)
            null
        }
    }
    
    /**
     * 获取页面
     */
    fun getPage(pageId: String = "default"): Page? = pages[pageId]
    
    /**
     * 导航到 URL
     */
    fun navigateTo(pageId: String = "default", url: String): Boolean {
        return try {
            val page = pages[pageId] ?: return false
            page.navigate(url)
            logger.info("Navigated to $url")
            true
        } catch (e: Exception) {
            logger.error("Failed to navigate to $url", e)
            false
        }
    }
    
    /**
     * 填充文本字段
     */
    fun fillText(pageId: String = "default", selector: String, text: String): Boolean {
        return try {
            val page = pages[pageId] ?: return false
            page.fill(selector, text)
            logger.info("Filled text in selector: $selector")
            true
        } catch (e: Exception) {
            logger.error("Failed to fill text", e)
            false
        }
    }
    
    /**
     * 点击元素
     */
    fun click(pageId: String = "default", selector: String): Boolean {
        return try {
            val page = pages[pageId] ?: return false
            page.click(selector)
            logger.info("Clicked element: $selector")
            true
        } catch (e: Exception) {
            logger.error("Failed to click element", e)
            false
        }
    }
    
    /**
     * 获取文本
     */
    fun getText(pageId: String = "default", selector: String): String? {
        return try {
            val page = pages[pageId] ?: return null
            page.textContent(selector)
        } catch (e: Exception) {
            logger.error("Failed to get text", e)
            null
        }
    }
    
    /**
     * 执行 JavaScript
     */
    fun executeJavaScript(pageId: String = "default", script: String): Any? {
        return try {
            val page = pages[pageId] ?: return null
            page.evaluate(script)
        } catch (e: Exception) {
            logger.error("Failed to execute JavaScript", e)
            null
        }
    }
    
    /**
     * 等待导航
     */
    fun waitForNavigation(pageId: String = "default", timeout: Int = 30000): Boolean {
        return try {
            val page = pages[pageId] ?: return false
            page.waitForLoadState()
            true
        } catch (e: Exception) {
            logger.error("Navigation timeout", e)
            false
        }
    }
    
    /**
     * 等待元素出现
     */
    fun waitForElement(pageId: String = "default", selector: String, timeout: Int = 30000): Boolean {
        return try {
            val page = pages[pageId] ?: return false
            page.waitForSelector(selector, Page.WaitForSelectorOptions().setTimeout(timeout.toDouble()))
            true
        } catch (e: Exception) {
            logger.error("Element wait timeout for selector: $selector", e)
            false
        }
    }
    
    /**
     * 保存截图
     */
    fun screenshot(pageId: String = "default", filePath: String): Boolean {
        return try {
            val page = pages[pageId] ?: return false
            page.screenshot(Page.ScreenshotOptions().setPath(Paths.get(filePath)))
            logger.info("Screenshot saved to: $filePath")
            true
        } catch (e: Exception) {
            logger.error("Failed to save screenshot", e)
            false
        }
    }
    
    /**
     * 获取 Cookie
     */
    fun getCookies(pageId: String = "default"): List<Map<String, Any>>? {
        return try {
            val page = pages[pageId] ?: return null
            page.context().cookies().map { cookie ->
                mapOf(
                    "name" to cookie.name,
                    "value" to cookie.value,
                    "domain" to cookie.domain,
                    "path" to cookie.path
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to get cookies", e)
            null
        }
    }
    
    /**
     * 关闭页面
     */
    fun closePage(pageId: String = "default") {
        try {
            pages[pageId]?.close()
            pages.remove(pageId)
            logger.info("Closed page: $pageId")
        } catch (e: Exception) {
            logger.error("Failed to close page", e)
        }
    }
    
    /**
     * 关闭浏览器
     */
    fun close() {
        try {
            pages.values.forEach { it.close() }
            pages.clear()
            browser?.close()
            playwright?.close()
            logger.info("Chromium browser closed")
        } catch (e: Exception) {
            logger.error("Error closing browser", e)
        }
    }
}

/**
 * Chromium 浏览器管理器单例
 */
object ChromiumManager {
    private var browser: ChromiumBrowser? = null
    
    fun initialize(headless: Boolean = true, proxyUrl: String? = null): Boolean {
        browser = ChromiumBrowser(headless, proxyUrl)
        return browser?.launch() ?: false
    }
    
    fun getBrowser(): ChromiumBrowser? = browser
    
    fun close() {
        browser?.close()
        browser = null
    }
}
