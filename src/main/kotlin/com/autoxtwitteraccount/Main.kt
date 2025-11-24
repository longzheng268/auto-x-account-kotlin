package com.autoxtwitteraccount

import com.autoxtwitteraccount.ui.MainWindow
import com.autoxtwitteraccount.ui.FontManager
import org.slf4j.LoggerFactory
import javax.swing.SwingUtilities

private val logger = LoggerFactory.getLogger("Main")

/**
 * 主应用程序入口
 */
fun main(args: Array<String>) {
    logger.info("Starting Twitter X Account Registration System")
    logger.info("JVM Version: ${System.getProperty("java.version")}")
    logger.info("Kotlin Batch Registration System v1.0.0")
    
    // 初始化字体
    logger.info("Initializing fonts...")
    FontManager  // 触发初始化
    
    // 在 EDT 上启动 GUI
    SwingUtilities.invokeLater {
        val window = MainWindow()
        window.isVisible = true
        logger.info("Main window opened")
    }
}

