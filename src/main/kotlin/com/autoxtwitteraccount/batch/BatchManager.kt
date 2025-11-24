package com.autoxtwitteraccount.batch

import com.autoxtwitteraccount.twitter.RegistrationResult
import com.autoxtwitteraccount.twitter.RegistrationStatus
import com.autoxtwitteraccount.twitter.TwitterAccount
import com.autoxtwitteraccount.twitter.TwitterRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

private val logger = LoggerFactory.getLogger("BatchManager")

/**
 * 批任务状态枚举
 */
enum class BatchTaskStatus {
    PENDING,
    RUNNING,
    PAUSED,
    RESUMED,
    STOPPED,
    COMPLETED,
    FAILED
}

/**
 * 批任务数据类
 */
data class BatchTask(
    val id: String,
    val accounts: List<TwitterAccount>,
    val status: BatchTaskStatus = BatchTaskStatus.PENDING,
    val completedCount: Int = 0,
    val failedCount: Int = 0,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val results: List<RegistrationResult> = emptyList()
) {
    /**
     * 获取进度百分比
     */
    val progress: Int
        get() {
            val total = accounts.size
            return if (total > 0) (completedCount * 100) / total else 0
        }

    /**
     * 获取耗时（毫秒）
     */
    val duration: Long
        get() {
            val start = startTime ?: return 0
            val end = endTime ?: System.currentTimeMillis()
            return end - start
        }
}

/**
 * 批量注册管理器
 * 支持：
 * - 并发控制
 * - 暂停/恢复/停止
 * - 状态持久化
 * - 进度跟踪
 */
class BatchManager(
    private val maxConcurrency: Int = 5,
    private val taskCheckInterval: Long = 1000
) {
    private val tasks = ConcurrentHashMap<String, BatchTask>()
    private val isRunning = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)
    private val activeTasks = AtomicInteger(0)
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * 创建新的批量任务
     */
    fun createBatchTask(id: String, accounts: List<TwitterAccount>): BatchTask {
        logger.info("Creating batch task: $id with ${accounts.size} accounts")
        val task = BatchTask(id = id, accounts = accounts)
        tasks[id] = task
        return task
    }

    /**
     * 启动批量任务
     */
    suspend fun startBatchTask(taskId: String): Boolean {
        logger.info("Starting batch task: $taskId")
        val task = tasks[taskId] ?: run {
            logger.error("Task not found: $taskId")
            return false
        }

        isRunning.set(true)
        isPaused.set(false)

        val updatedTask = task.copy(
            status = BatchTaskStatus.RUNNING,
            startTime = System.currentTimeMillis()
        )
        tasks[taskId] = updatedTask

        try {
            processBatchTask(taskId)
            tasks[taskId] = updatedTask.copy(status = BatchTaskStatus.COMPLETED)
            return true
        } catch (e: Exception) {
            logger.error("Batch task failed: $taskId", e)
            tasks[taskId] = updatedTask.copy(status = BatchTaskStatus.FAILED)
            return false
        }
    }

    /**
     * 暂停批量任务
     */
    fun pauseBatchTask(taskId: String): Boolean {
        logger.info("Pausing batch task: $taskId")
        val task = tasks[taskId] ?: return false

        if (task.status != BatchTaskStatus.RUNNING) {
            logger.warn("Cannot pause task with status: ${task.status}")
            return false
        }

        isPaused.set(true)
        tasks[taskId] = task.copy(status = BatchTaskStatus.PAUSED)
        return true
    }

    /**
     * 恢复批量任务
     */
    suspend fun resumeBatchTask(taskId: String): Boolean {
        logger.info("Resuming batch task: $taskId")
        val task = tasks[taskId] ?: return false

        if (task.status != BatchTaskStatus.PAUSED) {
            logger.warn("Cannot resume task with status: ${task.status}")
            return false
        }

        isPaused.set(false)
        tasks[taskId] = task.copy(status = BatchTaskStatus.RUNNING)

        try {
            processBatchTask(taskId)
            return true
        } catch (e: Exception) {
            logger.error("Error resuming task: $taskId", e)
            return false
        }
    }

    /**
     * 停止批量任务
     */
    fun stopBatchTask(taskId: String): Boolean {
        logger.info("Stopping batch task: $taskId")
        val task = tasks[taskId] ?: return false

        isRunning.set(false)
        tasks[taskId] = task.copy(
            status = BatchTaskStatus.STOPPED,
            endTime = System.currentTimeMillis()
        )
        return true
    }

    /**
     * 处理批量任务的核心逻辑
     */
    private suspend fun processBatchTask(taskId: String) {
        val task = tasks[taskId] ?: return
        val results = mutableListOf<RegistrationResult>()
        var completedCount = 0
        var failedCount = 0

        // 并发处理账户
        val chunkedAccounts = task.accounts.chunked(maxConcurrency)

        for (chunk in chunkedAccounts) {
            // 检查是否暂停
            while (isPaused.get()) {
                delay(taskCheckInterval)
            }

            // 检查是否停止
            if (!isRunning.get()) {
                logger.info("Task stopped: $taskId")
                break
            }

            // 并发处理这一批账户
            val jobs = chunk.map { account ->
                scope.async {
                    try {
                        activeTasks.incrementAndGet()
                        logger.info("Processing account: ${account.email}")
                        TwitterRegistration.registerTwitterAccount(account)
                    } catch (e: Exception) {
                        logger.error("Error processing account: ${account.email}", e)
                        RegistrationResult(
                            email = account.email,
                            status = RegistrationStatus.FAILED,
                            errorMessage = e.message
                        )
                    } finally {
                        activeTasks.decrementAndGet()
                    }
                }
            }

            // 等待所有任务完成
            val batchResults = jobs.awaitAll()
            results.addAll(batchResults)

            // 更新进度
            for (result in batchResults) {
                if (result.status == RegistrationStatus.COMPLETED) {
                    completedCount++
                } else {
                    failedCount++
                }
            }

            // 更新任务进度
            tasks[taskId] = task.copy(
                completedCount = completedCount,
                failedCount = failedCount,
                results = results
            )

            logger.info("Batch progress: $taskId - $completedCount/${task.accounts.size}")
        }

        // 更新最终状态
        tasks[taskId] = task.copy(
            completedCount = completedCount,
            failedCount = failedCount,
            results = results,
            endTime = System.currentTimeMillis()
        )

        isRunning.set(false)
        logger.info("Batch task completed: $taskId - Completed: $completedCount, Failed: $failedCount")
    }

    /**
     * 获取任务信息
     */
    fun getTask(taskId: String): BatchTask? = tasks[taskId]

    /**
     * 获取所有任务
     */
    fun getAllTasks(): List<BatchTask> = tasks.values.toList()

    /**
     * 删除任务
     */
    fun deleteTask(taskId: String): Boolean {
        val task = tasks[taskId] ?: return false
        if (task.status == BatchTaskStatus.RUNNING) {
            logger.warn("Cannot delete running task: $taskId")
            return false
        }
        tasks.remove(taskId)
        return true
    }

    /**
     * 获取统计信息
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "totalTasks" to tasks.size,
        "runningTasks" to tasks.values.count { it.status == BatchTaskStatus.RUNNING },
        "pausedTasks" to tasks.values.count { it.status == BatchTaskStatus.PAUSED },
        "completedTasks" to tasks.values.count { it.status == BatchTaskStatus.COMPLETED },
        "totalAccounts" to tasks.values.sumOf { it.accounts.size },
        "successfulAccounts" to tasks.values.sumOf { it.completedCount },
        "failedAccounts" to tasks.values.sumOf { it.failedCount },
        "activeTasks" to activeTasks.get()
    )
}
