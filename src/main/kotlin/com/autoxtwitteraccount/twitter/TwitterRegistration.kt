package com.autoxtwitteraccount.twitter

import com.autoxtwitteraccount.captcha.CaptchaHandler
import com.autoxtwitteraccount.captcha.NumberImageMatch
import com.autoxtwitteraccount.config.ConfigManager
import com.autoxtwitteraccount.email.EmailManager
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger = LoggerFactory.getLogger("TwitterRegistration")!! 

/**
 * Twitter 账号数据类
 */
data class TwitterAccount(
    val email: String,
    val name: String,
    val password: String,
    val dateOfBirth: LocalDate,
    val username: String = "",
    val phone: String? = null
)

/**
 * 注册状态枚举
 */
enum class RegistrationStatus {
    PENDING,
    VERIFYING_EMAIL,
    SOLVING_CAPTCHA,
    VERIFYING_CODE,
    SETTING_PASSWORD,
    COMPLETED,
    FAILED
}

/**
 * 注册结果数据类
 */
data class RegistrationResult(
    val email: String,
    val status: RegistrationStatus,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long = 0  // 注册耗时（毫秒）
)

/**
 * Twitter/X 注册流程管理器
 */
object TwitterRegistration {

    /**
     * 执行完整的 Twitter 注册流程
     * 1. 默认切换到邮箱注册（Use email instead）
     * 2. 填写 姓名、邮箱、出生日期
     * 3. 处理 X 自有数字图片匹配验证
     * 4. 处理 邮箱验证码
     * 5. 设置 密码
     */
    suspend fun registerTwitterAccount(account: TwitterAccount): RegistrationResult {
        val startTime = System.currentTimeMillis()
        logger.info("Starting registration for email: ${account.email}")

        return try {
            // Step 1: 切换到邮箱注册模式
            logger.info("Step 1: Switching to email registration mode")
            switchToEmailRegistration()

            // Step 2: 填写基本信息
            logger.info("Step 2: Filling account information")
            if (!fillAccountInformation(account)) {
                return RegistrationResult(
                    email = account.email,
                    status = RegistrationStatus.FAILED,
                    errorMessage = "Failed to fill account information",
                    duration = System.currentTimeMillis() - startTime
                )
            }

            // Step 3: 处理验证码（X 的数字图片匹配验证）
            logger.info("Step 3: Solving NumberImageMatch captcha")
            if (!solveCaptcha()) {
                return RegistrationResult(
                    email = account.email,
                    status = RegistrationStatus.SOLVING_CAPTCHA,
                    errorMessage = "Failed to solve captcha",
                    duration = System.currentTimeMillis() - startTime
                )
            }

            // Step 4: 获取并验证邮箱验证码
            logger.info("Step 4: Verifying email with verification code")
            if (!verifyEmailCode(account.email)) {
                return RegistrationResult(
                    email = account.email,
                    status = RegistrationStatus.VERIFYING_CODE,
                    errorMessage = "Failed to verify email code",
                    duration = System.currentTimeMillis() - startTime
                )
            }

            // Step 5: 设置密码
            logger.info("Step 5: Setting password")
            if (!setPassword(account.password)) {
                return RegistrationResult(
                    email = account.email,
                    status = RegistrationStatus.SETTING_PASSWORD,
                    errorMessage = "Failed to set password",
                    duration = System.currentTimeMillis() - startTime
                )
            }

            // 注册成功
            logger.info("Account registration successful for: ${account.email}")
            RegistrationResult(
                email = account.email,
                status = RegistrationStatus.COMPLETED,
                duration = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            logger.error("Account registration failed for: ${account.email}", e)
            RegistrationResult(
                email = account.email,
                status = RegistrationStatus.FAILED,
                errorMessage = e.message,
                duration = System.currentTimeMillis() - startTime
            )
        }
    }

    /**
     * Step 1: 切换到邮箱注册模式
     * 点击 "Use email instead" 按钮
     */
    private suspend fun switchToEmailRegistration(): Boolean {
        logger.info("Switching to email registration")
        // 这里需要实现与浏览器交互的逻辑，点击 "Use email instead" 按钮
        delay(1000)
        return true
    }

    /**
     * Step 2: 填写账户信息
     * 填写：姓名、邮箱、出生日期
     */
    private suspend fun fillAccountInformation(account: TwitterAccount): Boolean {
        logger.info("Filling account information")

        return try {
            // 获取基础邮箱或 Plus 模式邮箱
            val emailToUse = if (ConfigManager.isPlusModeEnabled()) {
                EmailManager.generatePlusEmail(account.email, 0)
            } else {
                account.email
            }

            logger.info("Using email: $emailToUse for registration")

            // 这里需要实现与浏览器交互的逻辑，填写表单
            // 1. 填写 姓名
            fillField("name", account.name)

            // 2. 填写 邮箱
            fillField("email", emailToUse)

            // 3. 填写 出生日期
            val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
            val formattedDate = account.dateOfBirth.format(dateFormatter)
            fillField("date_of_birth", formattedDate)

            logger.info("Account information filled successfully")
            true
        } catch (e: Exception) {
            logger.error("Failed to fill account information", e)
            false
        }
    }

    /**
     * Step 3: 解决 X 数字图片匹配验证码
     * X 使用自有的数字图片匹配机制，不使用 Arkose Labs 或 ReCAPTCHA
     */
    private suspend fun solveCaptcha(): Boolean {
        logger.info("Solving X NumberImageMatch captcha")

        return try {
            // 这里需要实现获取验证码图片的逻辑
            // 1. 截取验证码区域
            val captchaImages = getCaptchaImages()

            if (captchaImages.isEmpty()) {
                logger.warn("No captcha images found")
                return true  // 可能没有出现验证码
            }

            // 2. 创建 NumberImageMatch 对象
            val numberImageMatch = NumberImageMatch(
                images = captchaImages,
                matchPattern = "",
                description = "X NumberImageMatch captcha"
            )

            // 3. 使用 CaptchaHandler 解决验证码
            val answer = CaptchaHandler.handleNumberImageMatch(numberImageMatch)

            if (answer.isNullOrEmpty()) {
                logger.warn("Failed to solve captcha")
                return false
            }

            // 4. 点击匹配的图片或提交答案
            submitCaptchaAnswer(answer)

            logger.info("Captcha solved successfully: $answer")
            true
        } catch (e: Exception) {
            logger.error("Failed to solve captcha", e)
            false
        }
    }

    /**
     * Step 4: 验证邮箱验证码
     * 获取来自 X 的邮箱验证码，并填写到表单
     */
    private suspend fun verifyEmailCode(email: String): Boolean {
        logger.info("Verifying email with code")

        return try {
            // 获取基础邮箱（Plus 模式下仍发送到原邮箱）
            val baseEmail = EmailManager.getBaseEmail(email)

            logger.info("Fetching verification code from: $baseEmail")

            // 获取验证码（超时 5 分钟）
            val verificationCode = EmailManager.getVerificationCode(
                email = baseEmail,
                sender = "noreply@twitter.com",
                timeout = 300000
            )

            if (verificationCode.isNullOrEmpty()) {
                logger.warn("Failed to get verification code")
                return false
            }

            logger.info("Verification code received: ${verificationCode.take(3)}***")

            // 填写验证码到表单
            fillField("verification_code", verificationCode)

            logger.info("Email verified successfully")
            true
        } catch (e: Exception) {
            logger.error("Failed to verify email", e)
            false
        }
    }

    /**
     * Step 5: 设置密码
     */
    private suspend fun setPassword(password: String): Boolean {
        logger.info("Setting account password")

        return try {
            // 这里需要实现填写密码字段的逻辑
            fillField("password", password)
            fillField("password_confirm", password)

            // 提交表单
            submitRegistration()

            logger.info("Password set successfully")
            true
        } catch (e: Exception) {
            logger.error("Failed to set password", e)
            false
        }
    }

    /**
     * 填写表单字段（模拟实现）
     */
    private suspend fun fillField(fieldName: String, value: String) {
        logger.debug("Filling field: $fieldName = $value")
        // 这里需要实现实际的浏览器交互逻辑
        // 使用 Playwright, Selenium 或其他自动化工具填写表单
        delay(500)
    }

    /**
     * 获取验证码图片（模拟实现）
     */
    private suspend fun getCaptchaImages(): List<String> {
        logger.debug("Getting captcha images from page")
        // 这里需要实现获取验证码图片的逻辑
        // 截取页面上的验证码区域，转换为 Base64
        delay(1000)
        return emptyList()  // 返回空列表，表示可能没有验证码
    }

    /**
     * 提交验证码答案（模拟实现）
     */
    private suspend fun submitCaptchaAnswer(answer: String) {
        logger.debug("Submitting captcha answer: $answer")
        // 这里需要实现提交验证码答案的逻辑
        // 可能是点击相应的图片，或填写答案字段
        delay(500)
    }

    /**
     * 提交注册表单（模拟实现）
     */
    private suspend fun submitRegistration() {
        logger.debug("Submitting registration form")
        // 这里需要实现提交表单的逻辑
        // 点击 "完成" 或 "提交" 按钮
        delay(1000)
    }
}
