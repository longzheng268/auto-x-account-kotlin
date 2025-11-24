package com.autoxtwitteraccount.captcha

import com.autoxtwitteraccount.config.CaptchaMode
import com.autoxtwitteraccount.config.ConfigManager
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import kotlin.io.path.Path
import kotlin.io.path.pathString

private val logger = LoggerFactory.getLogger("CaptchaHandler")

/**
 * 数字图片匹配验证码数据类
 */
data class NumberImageMatch(
    val images: List<String>,                    // Base64 编码的图片
    val matchPattern: String,                    // 匹配模式（例如: "1,2,3"）
    val description: String = ""                 // 描述
)

/**
 * 验证码响应数据类
 */
data class CaptchaResult(
    val answer: String,                         // 验证码答案
    val mode: CaptchaMode,                      // 使用的模式
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 验证码处理器 - 支持多种验证码识别方法
 */
object CaptchaHandler {

    /**
     * 处理验证码 - 根据配置自动选择处理方法
     */
    suspend fun handleCaptcha(captchaData: Any): CaptchaResult? {
        val mode = ConfigManager.getCaptchaMode()
        logger.info("Handling captcha with mode: $mode")

        return when (mode) {
            CaptchaMode.AUTO -> handleAutoCaptcha(captchaData)
            CaptchaMode.MANUAL -> handleManualCaptcha(captchaData)
            CaptchaMode.THIRD_PARTY -> handleThirdPartyCaptcha(captchaData)
            CaptchaMode.LLM -> handleLlmCaptcha(captchaData)
        }
    }

    /**
     * 自动模式 - 先尝试 LLM，失败则提示手动输入
     */
    private suspend fun handleAutoCaptcha(captchaData: Any): CaptchaResult? {
        logger.info("Using AUTO captcha mode")
        return try {
            handleLlmCaptcha(captchaData) ?: handleManualCaptcha(captchaData)
        } catch (e: Exception) {
            logger.warn("Auto mode failed, falling back to manual", e)
            handleManualCaptcha(captchaData)
        }
    }

    /**
     * 手动模式 - 等待用户输入
     */
    private suspend fun handleManualCaptcha(captchaData: Any): CaptchaResult? {
        logger.info("Using MANUAL captcha mode")
        logger.warn("Please enter the verification code manually")

        // 模拟等待用户输入
        val answer = readInputBlocking()
        return if (answer.isNotEmpty()) {
            CaptchaResult(
                answer = answer,
                mode = CaptchaMode.MANUAL
            )
        } else {
            logger.warn("No input received for manual captcha")
            null
        }
    }

    /**
     * 第三方服务模式 - 使用第三方验证码服务（如 2Captcha, AntiCaptcha 等）
     */
    private suspend fun handleThirdPartyCaptcha(captchaData: Any): CaptchaResult? {
        logger.info("Using THIRD_PARTY captcha mode")

        return try {
            when (captchaData) {
                is NumberImageMatch -> handleNumberImageMatchViaService(captchaData)
                is String -> handleImageCaptchaViaService(captchaData)
                else -> {
                    logger.warn("Unknown captcha data type: ${captchaData::class.simpleName}")
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("Third-party captcha handling failed", e)
            null
        }
    }

    /**
     * 大语言模型模式 - 使用 LLM 识别验证码
     */
    private suspend fun handleLlmCaptcha(captchaData: Any): CaptchaResult? {
        logger.info("Using LLM captcha mode")

        return try {
            when (captchaData) {
                is NumberImageMatch -> recognizeNumberImageMatchWithLlm(captchaData)
                is String -> recognizeImageWithLlm(captchaData)
                else -> {
                    logger.warn("Unknown captcha data type: ${captchaData::class.simpleName}")
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("LLM captcha handling failed", e)
            null
        }
    }

    /**
     * 处理 X 特有的数字图片匹配验证码
     * X 使用自有的数字图片匹配机制，不依赖 Arkose Labs 或 ReCAPTCHA
     */
    suspend fun handleNumberImageMatch(match: NumberImageMatch): String? {
        logger.info("Handling NumberImageMatch captcha")

        return try {
            when (ConfigManager.getCaptchaMode()) {
                CaptchaMode.AUTO, CaptchaMode.LLM -> {
                    recognizeNumberImageMatchWithLlm(match)?.answer
                }
                CaptchaMode.MANUAL -> {
                    logger.warn("Please select the matching images manually")
                    readInputBlocking()
                }
                CaptchaMode.THIRD_PARTY -> {
                    handleNumberImageMatchViaService(match)?.answer
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to handle NumberImageMatch", e)
            null
        }
    }

    /**
     * 使用 LLM 识别数字图片匹配
     */
    private suspend fun recognizeNumberImageMatchWithLlm(match: NumberImageMatch): CaptchaResult? {
        logger.info("Recognizing NumberImageMatch with LLM")

        // 这里需要实现与 LLM API（如 OpenAI GPT-4 Vision）的集成
        // 将 Base64 图片发送给 LLM 进行识别
        logger.debug("Images count: ${match.images.size}")
        logger.debug("Match pattern: ${match.matchPattern}")

        // 模拟 LLM 识别过程
        delay(1000)

        // 返回识别结果（这里是模拟，实际需要与 LLM API 集成）
        val mockAnswer = "0,1,2"  // 模拟识别结果
        logger.info("LLM recognition result: $mockAnswer")

        return CaptchaResult(
            answer = mockAnswer,
            mode = CaptchaMode.LLM
        )
    }

    /**
     * 使用 LLM 识别验证码图片
     */
    private suspend fun recognizeImageWithLlm(imageBase64: String): CaptchaResult? {
        logger.info("Recognizing image captcha with LLM")

        // 这里需要实现与 LLM API 的集成
        // 将 Base64 图片发送给 LLM 进行识别
        logger.debug("Image size: ${imageBase64.length} bytes")

        // 模拟 LLM 识别过程
        delay(1000)

        // 返回识别结果（模拟）
        val mockAnswer = "12345"  // 模拟识别结果
        logger.info("LLM recognition result: $mockAnswer")

        return CaptchaResult(
            answer = mockAnswer,
            mode = CaptchaMode.LLM
        )
    }

    /**
     * 通过第三方服务处理数字图片匹配
     */
    private suspend fun handleNumberImageMatchViaService(match: NumberImageMatch): CaptchaResult? {
        logger.info("Submitting NumberImageMatch to third-party service")

        // 这里需要实现与第三方验证码服务的集成
        // 如 2Captcha, AntiCaptcha 等
        logger.debug("Service submission: images=${match.images.size}, pattern=${match.matchPattern}")

        // 模拟第三方服务处理
        delay(5000)

        val mockAnswer = "0,1,2"
        logger.info("Third-party service result: $mockAnswer")

        return CaptchaResult(
            answer = mockAnswer,
            mode = CaptchaMode.THIRD_PARTY
        )
    }

    /**
     * 通过第三方服务处理图片验证码
     */
    private suspend fun handleImageCaptchaViaService(imageBase64: String): CaptchaResult? {
        logger.info("Submitting image captcha to third-party service")

        // 模拟第三方服务处理
        delay(5000)

        val mockAnswer = "12345"
        logger.info("Third-party service result: $mockAnswer")

        return CaptchaResult(
            answer = mockAnswer,
            mode = CaptchaMode.THIRD_PARTY
        )
    }

    /**
     * 保存验证码图片用于调试（Base64 -> 文件）
     */
    fun saveDebugImage(imageBase64: String, filename: String): String {
        return try {
            val debugDir = Path("debug/captcha")
            val filepath = debugDir.resolve(filename)
            logger.debug("Saving debug image to: ${filepath.pathString}")
            filepath.pathString
        } catch (e: Exception) {
            logger.error("Failed to save debug image", e)
            ""
        }
    }

    /**
     * 模拟阻塞式读取用户输入
     */
    private fun readInputBlocking(): String {
        return try {
            print("Enter captcha answer: ")
            readLine()?.trim() ?: ""
        } catch (e: Exception) {
            logger.error("Failed to read user input", e)
            ""
        }
    }
}
