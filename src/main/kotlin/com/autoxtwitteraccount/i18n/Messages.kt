package com.autoxtwitteraccount.i18n

import java.util.*

/**
 * 国际化消息管理
 */
object Messages {
    private val locale = Locale.getDefault()
    private val messages = mutableMapOf<String, String>()
    
    init {
        loadMessages()
    }
    
    private fun loadMessages() {
        when {
            locale.language == "zh" -> loadChineseMessages()
            else -> loadEnglishMessages()
        }
    }
    
    private fun loadChineseMessages() {
        messages.apply {
            // 主窗口
            put("app.title", "推特/X 账号批量注册系统")
            put("app.subtitle", "Twitter/X Batch Registration System")
            
            // 菜单
            put("menu.file", "文件")
            put("menu.edit", "编辑")
            put("menu.view", "查看")
            put("menu.tools", "工具")
            put("menu.help", "帮助")
            put("menu.exit", "退出")
            put("menu.preferences", "设置")
            put("menu.about", "关于")
            
            // 按钮
            put("btn.import", "导入")
            put("btn.export", "导出")
            put("btn.start", "启动")
            put("btn.pause", "暂停")
            put("btn.resume", "继续")
            put("btn.stop", "停止")
            put("btn.save", "保存")
            put("btn.cancel", "取消")
            put("btn.ok", "确定")
            put("btn.reset", "重置")
            
            // 选项卡
            put("tab.dashboard", "仪表板")
            put("tab.accounts", "账号管理")
            put("tab.batch", "批量操作")
            put("tab.config", "配置")
            put("tab.logs", "日志")
            
            // 仪表板
            put("dashboard.title", "仪表板")
            put("dashboard.total", "总数")
            put("dashboard.success", "成功")
            put("dashboard.failed", "失败")
            put("dashboard.pending", "待处理")
            put("dashboard.progress", "进度")
            
            // 账号管理
            put("accounts.title", "账号管理")
            put("accounts.import", "导入账号")
            put("accounts.export", "导出账号")
            put("accounts.add", "添加账号")
            put("accounts.delete", "删除")
            put("accounts.email", "邮箱")
            put("accounts.password", "密码")
            put("accounts.status", "状态")
            put("accounts.importSuccess", "成功导入 %d 个账号")
            put("accounts.importFailed", "导入失败: %s")
            
            // 批量操作
            put("batch.title", "批量操作")
            put("batch.concurrency", "并发数")
            put("batch.delayMs", "延迟(毫秒)")
            put("batch.emailProvider", "邮箱提供商")
            put("batch.captchaMode", "验证码模式")
            put("batch.proxyUrl", "代理 URL")
            put("batch.startBatch", "启动批处理")
            put("batch.status", "状态")
            put("batch.statusPending", "待处理")
            put("batch.statusRunning", "运行中")
            put("batch.statusPaused", "已暂停")
            put("batch.statusStopped", "已停止")
            put("batch.statusCompleted", "已完成")
            
            // 配置
            put("config.title", "配置")
            put("config.general", "常规")
            put("config.email", "邮箱")
            put("config.proxy", "代理")
            put("config.browser", "浏览器")
            put("config.captcha", "验证码")
            put("config.save", "保存配置")
            put("config.reset", "重置为默认")
            put("config.gmailApp", "Gmail 应用密码")
            put("config.outlookPassword", "Outlook 密码")
            put("config.customApiUrl", "自定义 API URL")
            put("config.customApiKey", "自定义 API Key")
            put("config.enablePlusMode", "启用 Plus 模式")
            put("config.plusSuffix", "Plus 后缀")
            
            // 日志
            put("logs.title", "日志")
            put("logs.clear", "清空")
            put("logs.export", "导出")
            put("logs.level", "级别")
            put("logs.time", "时间")
            put("logs.message", "消息")
            
            // 状态消息
            put("status.ready", "就绪")
            put("status.running", "运行中")
            put("status.paused", "已暂停")
            put("status.stopped", "已停止")
            put("status.error", "错误")
            
            // 对话框
            put("dialog.confirm", "确认")
            put("dialog.error", "错误")
            put("dialog.warning", "警告")
            put("dialog.info", "信息")
            put("dialog.selectFile", "选择文件")
            put("dialog.chooseFormat", "选择格式")
            put("dialog.json", "JSON 文件")
            put("dialog.csv", "CSV 文件")
            put("dialog.excel", "Excel 文件")
            
            // 错误消息
            put("error.noFile", "未选择文件")
            put("error.invalidFile", "无效的文件")
            put("error.importFailed", "导入失败")
            put("error.exportFailed", "导出失败")
            put("error.noBrowser", "未找到浏览器")
            put("error.emailConfig", "邮箱配置错误")
            put("error.noAccounts", "未导入任何账号")
            
            // 成功消息
            put("success.imported", "导入成功")
            put("success.exported", "导出成功")
            put("success.saved", "保存成功")
        }
    }
    
    private fun loadEnglishMessages() {
        messages.apply {
            // Main Window
            put("app.title", "Twitter/X Batch Registration System")
            put("app.subtitle", "Automated Account Creation")
            
            // Menu
            put("menu.file", "File")
            put("menu.edit", "Edit")
            put("menu.view", "View")
            put("menu.tools", "Tools")
            put("menu.help", "Help")
            put("menu.exit", "Exit")
            put("menu.preferences", "Preferences")
            put("menu.about", "About")
            
            // Buttons
            put("btn.import", "Import")
            put("btn.export", "Export")
            put("btn.start", "Start")
            put("btn.pause", "Pause")
            put("btn.resume", "Resume")
            put("btn.stop", "Stop")
            put("btn.save", "Save")
            put("btn.cancel", "Cancel")
            put("btn.ok", "OK")
            put("btn.reset", "Reset")
            
            // Tabs
            put("tab.dashboard", "Dashboard")
            put("tab.accounts", "Accounts")
            put("tab.batch", "Batch")
            put("tab.config", "Config")
            put("tab.logs", "Logs")
            
            // Dashboard
            put("dashboard.title", "Dashboard")
            put("dashboard.total", "Total")
            put("dashboard.success", "Success")
            put("dashboard.failed", "Failed")
            put("dashboard.pending", "Pending")
            put("dashboard.progress", "Progress")
            
            // Accounts
            put("accounts.title", "Account Management")
            put("accounts.import", "Import Accounts")
            put("accounts.export", "Export Accounts")
            put("accounts.add", "Add Account")
            put("accounts.delete", "Delete")
            put("accounts.email", "Email")
            put("accounts.password", "Password")
            put("accounts.status", "Status")
            put("accounts.importSuccess", "Successfully imported %d accounts")
            put("accounts.importFailed", "Import failed: %s")
            
            // Batch
            put("batch.title", "Batch Processing")
            put("batch.concurrency", "Concurrency")
            put("batch.delayMs", "Delay (ms)")
            put("batch.emailProvider", "Email Provider")
            put("batch.captchaMode", "Captcha Mode")
            put("batch.proxyUrl", "Proxy URL")
            put("batch.startBatch", "Start Batch")
            put("batch.status", "Status")
            put("batch.statusPending", "Pending")
            put("batch.statusRunning", "Running")
            put("batch.statusPaused", "Paused")
            put("batch.statusStopped", "Stopped")
            put("batch.statusCompleted", "Completed")
            
            // Config
            put("config.title", "Configuration")
            put("config.general", "General")
            put("config.email", "Email")
            put("config.proxy", "Proxy")
            put("config.browser", "Browser")
            put("config.captcha", "Captcha")
            put("config.save", "Save Configuration")
            put("config.reset", "Reset to Default")
            put("config.gmailApp", "Gmail App Password")
            put("config.outlookPassword", "Outlook Password")
            put("config.customApiUrl", "Custom API URL")
            put("config.customApiKey", "Custom API Key")
            put("config.enablePlusMode", "Enable Plus Mode")
            put("config.plusSuffix", "Plus Suffix")
            
            // Logs
            put("logs.title", "Logs")
            put("logs.clear", "Clear")
            put("logs.export", "Export")
            put("logs.level", "Level")
            put("logs.time", "Time")
            put("logs.message", "Message")
            
            // Status
            put("status.ready", "Ready")
            put("status.running", "Running")
            put("status.paused", "Paused")
            put("status.stopped", "Stopped")
            put("status.error", "Error")
            
            // Dialogs
            put("dialog.confirm", "Confirm")
            put("dialog.error", "Error")
            put("dialog.warning", "Warning")
            put("dialog.info", "Information")
            put("dialog.selectFile", "Select File")
            put("dialog.chooseFormat", "Choose Format")
            put("dialog.json", "JSON Files")
            put("dialog.csv", "CSV Files")
            put("dialog.excel", "Excel Files")
            
            // Error Messages
            put("error.noFile", "No file selected")
            put("error.invalidFile", "Invalid file")
            put("error.importFailed", "Import failed")
            put("error.exportFailed", "Export failed")
            put("error.noBrowser", "Browser not found")
            put("error.emailConfig", "Email configuration error")
            put("error.noAccounts", "No accounts imported")
            
            // Success Messages
            put("success.imported", "Import successful")
            put("success.exported", "Export successful")
            put("success.saved", "Save successful")
        }
    }
    
    fun get(key: String, vararg args: Any): String {
        val template = messages[key] ?: key
        return if (args.isNotEmpty()) {
            String.format(template, *args)
        } else {
            template
        }
    }
    
    fun getString(key: String) = get(key)
}
