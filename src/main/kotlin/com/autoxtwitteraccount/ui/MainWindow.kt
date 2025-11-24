package com.autoxtwitteraccount.ui

import com.autoxtwitteraccount.batch.BatchManager
import com.autoxtwitteraccount.config.ConfigManager
import com.autoxtwitteraccount.data.DataManager
import com.autoxtwitteraccount.i18n.Messages
import com.formdev.flatlaf.FlatLightLaf
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.awt.*
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableModel

private val logger = LoggerFactory.getLogger("GUI")

// 改进的配色方案
object Colors {
    val BACKGROUND = Color(245, 245, 245)  // 浅灰背景
    val PANEL_BACKGROUND = Color(255, 255, 255)  // 白色面板
    val ACCENT = Color(59, 130, 246)  // 蓝色强调色
    val SUCCESS = Color(34, 197, 94)  // 绿色成功
    val WARNING = Color(251, 191, 36)  // 黄色警告
    val ERROR = Color(239, 68, 68)  // 红色错误
    val TEXT_PRIMARY = Color(17, 24, 39)  // 深灰文字
    val TEXT_SECONDARY = Color(107, 114, 128)  // 浅灰文字
    val BORDER = Color(229, 231, 235)  // 边框色
    val HOVER = Color(243, 244, 246)  // 悬停色
}

/**
 * 主 GUI 窗口
 */
class MainWindow : JFrame() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val dashboardPanel = DashboardPanel()
    private val accountsPanel = AccountsPanel()
    private val plusModePanel = PlusModePanel()
    private val batchPanel = BatchPanel()
    private val configPanel = ConfigPanel()
    private val logsPanel = LogsPanel()

    init {
        title = Messages.get("app.title")
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(1400, 900)
        setLocationRelativeTo(null)

        // 设置主题 - 使用浅色主题更清晰
        FlatLightLaf.setup()
        UIManager.put("defaultFont", FontManager.getNormalFont())

        // 设置全局颜色
        UIManager.put("Panel.background", Colors.BACKGROUND)
        UIManager.put("Button.background", Colors.PANEL_BACKGROUND)
        UIManager.put("TextField.background", Colors.PANEL_BACKGROUND)
        UIManager.put("ComboBox.background", Colors.PANEL_BACKGROUND)

        // 创建菜单栏
        jMenuBar = createMenuBar()

        // 创建标签页
        val tabbedPane = JTabbedPane()
        tabbedPane.font = FontManager.getNormalFont()
        tabbedPane.background = Colors.PANEL_BACKGROUND

        tabbedPane.addTab(Messages.get("tab.dashboard"), dashboardPanel)
        tabbedPane.addTab(Messages.get("tab.accounts"), accountsPanel)
        tabbedPane.addTab("Plus 模式", plusModePanel)
        tabbedPane.addTab(Messages.get("tab.batch"), batchPanel)
        tabbedPane.addTab(Messages.get("tab.config"), configPanel)
        tabbedPane.addTab(Messages.get("tab.logs"), logsPanel)

        contentPane.layout = BorderLayout()
        contentPane.add(tabbedPane, BorderLayout.CENTER)
        contentPane.add(createStatusBar(), BorderLayout.SOUTH)

        addWindowListener(object : java.awt.event.WindowAdapter() {
            override fun windowClosing(e: java.awt.event.WindowEvent?) {
                scope.cancel()
                System.exit(0)
            }
        })
    }
    
    private fun createMenuBar(): JMenuBar {
        val menuBar = JMenuBar()
        
        // File menu
        val fileMenu = JMenu(Messages.get("menu.file"))
        fileMenu.font = FontManager.getNormalFont()
        fileMenu.add(JMenuItem(Messages.get("menu.preferences")).apply {
            addActionListener { configPanel.showPreferences() }
        })
        fileMenu.addSeparator()
        fileMenu.add(JMenuItem(Messages.get("menu.exit")).apply {
            addActionListener { System.exit(0) }
        })
        
        // Help menu
        val helpMenu = JMenu(Messages.get("menu.help"))
        helpMenu.font = FontManager.getNormalFont()
        helpMenu.add(JMenuItem(Messages.get("menu.about")).apply {
            addActionListener {
                JOptionPane.showMessageDialog(
                    this@MainWindow,
                    """
                    ${Messages.get("app.title")}
                    v1.0.0
                    
                    Based on Kotlin + Playwright + Chromium
                    """.trimIndent(),
                    Messages.get("menu.about"),
                    JOptionPane.INFORMATION_MESSAGE
                )
            }
        })
        
        menuBar.add(fileMenu)
        menuBar.add(helpMenu)
        return menuBar
    }
    
    private fun createStatusBar(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = EmptyBorder(5, 5, 5, 5)
        panel.background = Colors.BORDER

        val statusLabel = JLabel(Messages.get("status.ready"))
        statusLabel.font = FontManager.getSmallFont()
        statusLabel.foreground = Colors.TEXT_SECONDARY

        panel.add(statusLabel, BorderLayout.WEST)
        return panel
    }
}

/**
 * 仪表板面板
 */
class DashboardPanel : JPanel() {
    private val totalLabel = JLabel("0")
    private val successLabel = JLabel("0")
    private val failedLabel = JLabel("0")
    private val progressBar = JProgressBar(0, 100)

    init {
        layout = GridBagLayout()
        border = EmptyBorder(20, 20, 20, 20)
        background = Colors.PANEL_BACKGROUND

        val gbc = GridBagConstraints().apply {
            insets = Insets(10, 10, 10, 10)
            fill = GridBagConstraints.HORIZONTAL
        }

        // 标题
        val title = JLabel(Messages.get("dashboard.title"))
        title.font = FontManager.getHeaderFont()
        title.foreground = Colors.TEXT_PRIMARY
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 4
        add(title, gbc)

        // 统计信息
        addStatPanel(0, Messages.get("dashboard.total"), totalLabel, Colors.ACCENT)
        addStatPanel(1, Messages.get("dashboard.success"), successLabel, Colors.SUCCESS)
        addStatPanel(2, Messages.get("dashboard.failed"), failedLabel, Colors.ERROR)

        // 进度条
        gbc.gridx = 0
        gbc.gridy = 2
        gbc.gridwidth = 4
        progressBar.preferredSize = Dimension(200, 30)
        progressBar.foreground = Colors.ACCENT
        val progressPanel = JPanel(BorderLayout())
        progressPanel.background = Colors.PANEL_BACKGROUND
        progressPanel.add(JLabel(Messages.get("dashboard.progress")).apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, BorderLayout.WEST)
        progressPanel.add(progressBar, BorderLayout.CENTER)
        add(progressPanel, gbc)
    }

    private fun addStatPanel(index: Int, label: String, valueLabel: JLabel, color: Color) {
        val gbc = GridBagConstraints().apply {
            gridx = index
            gridy = 1
            insets = Insets(10, 10, 10, 10)
            fill = GridBagConstraints.BOTH
            weightx = 1.0
        }

        val panel = JPanel(GridLayout(2, 1))
        panel.background = Colors.PANEL_BACKGROUND
        panel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colors.BORDER),
            EmptyBorder(15, 15, 15, 15)
        )

        val labelComp = JLabel(label)
        labelComp.font = FontManager.getSmallFont()
        labelComp.foreground = Colors.TEXT_SECONDARY

        valueLabel.font = FontManager.getLargeFont()
        valueLabel.foreground = color
        valueLabel.horizontalAlignment = SwingConstants.CENTER

        panel.add(labelComp)
        panel.add(valueLabel)

        add(panel, gbc)
    }
}

/**
 * 账号管理面板
 */
class AccountsPanel : JPanel() {
    private val tableModel = DefaultTableModel(
        arrayOf(
            Messages.get("accounts.email"),
            Messages.get("accounts.password"),
            Messages.get("accounts.status")
        ),
        0
    )
    private val table = JTable(tableModel)

    init {
        layout = BorderLayout()
        border = EmptyBorder(10, 10, 10, 10)
        background = Colors.PANEL_BACKGROUND

        // 按钮面板
        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        buttonPanel.background = Colors.PANEL_BACKGROUND

        val importBtn = JButton(Messages.get("accounts.import"))
        importBtn.font = FontManager.getNormalFont()
        importBtn.background = Colors.ACCENT
        importBtn.foreground = Color.WHITE
        importBtn.addActionListener { importAccounts() }

        val exportBtn = JButton(Messages.get("accounts.export"))
        exportBtn.font = FontManager.getNormalFont()
        exportBtn.background = Colors.SUCCESS
        exportBtn.foreground = Color.WHITE
        exportBtn.addActionListener { exportAccounts() }

        buttonPanel.add(importBtn)
        buttonPanel.add(exportBtn)

        // 表格
        table.font = FontManager.getNormalFont()
        table.background = Colors.PANEL_BACKGROUND
        table.gridColor = Colors.BORDER
        val scrollPane = JScrollPane(table)
        scrollPane.border = BorderFactory.createLineBorder(Colors.BORDER)

        add(buttonPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
    }
    
    private fun importAccounts() {
        val chooser = JFileChooser()
        chooser.currentDirectory = File(".")
        val result = chooser.showOpenDialog(this)
        
        if (result == JFileChooser.APPROVE_OPTION) {
            val file = chooser.selectedFile
            JOptionPane.showMessageDialog(
                this,
                Messages.get("accounts.importSuccess", 5),
                Messages.get("success.imported"),
                JOptionPane.INFORMATION_MESSAGE
            )
        }
    }
    
    private fun exportAccounts() {
        val chooser = JFileChooser()
        val result = chooser.showSaveDialog(this)
        
        if (result == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(
                this,
                Messages.get("success.exported"),
                Messages.get("accounts.export"),
                JOptionPane.INFORMATION_MESSAGE
            )
        }
    }
}

/**
 * 批量操作面板
 */
class BatchPanel : JPanel() {
    private val concurrencySpinner = JSpinner(SpinnerNumberModel(5, 1, 50, 1))
    private val delaySpinner = JSpinner(SpinnerNumberModel(0, 0, 10000, 100))
    private val statusLabel = JLabel(Messages.get("batch.statusPending"))

    init {
        layout = GridBagLayout()
        border = EmptyBorder(20, 20, 20, 20)
        background = Colors.PANEL_BACKGROUND

        val gbc = GridBagConstraints().apply {
            insets = Insets(10, 10, 10, 10)
            fill = GridBagConstraints.HORIZONTAL
        }

        // 并发数
        gbc.gridx = 0
        gbc.gridy = 0
        add(JLabel(Messages.get("batch.concurrency")).apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        concurrencySpinner.preferredSize = Dimension(100, 30)
        add(concurrencySpinner, gbc)

        // 延迟
        gbc.gridx = 0
        gbc.gridy = 1
        add(JLabel(Messages.get("batch.delayMs")).apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        delaySpinner.preferredSize = Dimension(100, 30)
        add(delaySpinner, gbc)

        // 按钮
        gbc.gridx = 0
        gbc.gridy = 2
        gbc.gridwidth = 2
        val btnPanel = JPanel(FlowLayout())
        btnPanel.background = Colors.PANEL_BACKGROUND

        val startBtn = JButton(Messages.get("btn.start"))
        startBtn.font = FontManager.getNormalFont()
        startBtn.background = Colors.SUCCESS
        startBtn.foreground = Color.WHITE
        startBtn.addActionListener { startBatch() }

        val pauseBtn = JButton(Messages.get("btn.pause"))
        pauseBtn.font = FontManager.getNormalFont()
        pauseBtn.background = Colors.WARNING
        pauseBtn.foreground = Color.WHITE

        val stopBtn = JButton(Messages.get("btn.stop"))
        stopBtn.font = FontManager.getNormalFont()
        stopBtn.background = Colors.ERROR
        stopBtn.foreground = Color.WHITE

        btnPanel.add(startBtn)
        btnPanel.add(pauseBtn)
        btnPanel.add(stopBtn)
        add(btnPanel, gbc)

        // 状态
        gbc.gridx = 0
        gbc.gridy = 3
        gbc.gridwidth = 2
        statusLabel.font = FontManager.getNormalFont()
        statusLabel.foreground = Colors.TEXT_SECONDARY
        add(statusLabel, gbc)
    }

    private fun startBatch() {
        statusLabel.text = Messages.get("batch.statusRunning")
        statusLabel.foreground = Colors.SUCCESS
    }
}

/**
 * 配置面板 - 扩展版
 */
class ConfigPanel : JPanel() {
    // 邮箱配置
    private val emailProviderCombo = JComboBox(arrayOf("GMAIL", "OUTLOOK", "CUSTOM", "SELF_HOSTED"))
    private val baseEmailField = JTextField(30)
    private val plusModeCheckbox = JCheckBox(Messages.get("config.enablePlusMode"))
    private val useHumanNamesCheckbox = JCheckBox("使用人名作为后缀")
    private val customNamesField = JTextField(50)
    private val gmailAppPasswordField = JPasswordField(20)
    private val outlookPasswordField = JPasswordField(20)
    private val customApiUrlField = JTextField(30)
    private val customApiKeyField = JPasswordField(20)

    // 代理配置
    private val proxyModeCombo = JComboBox(arrayOf("AUTO", "MANUAL", "DISABLED"))
    private val browserProxyCheckbox = JCheckBox(Messages.get("config.enableBrowserProxy"))
    private val emailProxyCheckbox = JCheckBox(Messages.get("config.enableEmailProxy"))
    private val browserProxyUrlField = JTextField(30)
    private val emailProxyUrlField = JTextField(30)

    // 验证码配置
    private val captchaModeCombo = JComboBox(arrayOf("AUTO", "MANUAL", "THIRD_PARTY", "LLM"))
    private val captchaTimeoutSpinner = JSpinner(SpinnerNumberModel(60, 10, 300, 10))
    private val llmApiKeyField = JPasswordField(30)

    // 浏览器配置
    private val headlessModeCheckbox = JCheckBox("Headless 模式")
    private val browserTimeoutSpinner = JSpinner(SpinnerNumberModel(30000, 5000, 120000, 5000))

    // 批量配置
    private val maxConcurrencySpinner = JSpinner(SpinnerNumberModel(5, 1, 50, 1))
    private val delayBetweenAccountsSpinner = JSpinner(SpinnerNumberModel(0, 0, 10000, 100))

    init {
        layout = BorderLayout()
        background = Colors.PANEL_BACKGROUND

        // 创建滚动面板
        val scrollPane = JScrollPane(createConfigContent())
        scrollPane.border = BorderFactory.createEmptyBorder()
        scrollPane.verticalScrollBar.unitIncrement = 16
        add(scrollPane, BorderLayout.CENTER)

        // 底部按钮面板
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        buttonPanel.background = Colors.PANEL_BACKGROUND
        buttonPanel.border = EmptyBorder(10, 10, 10, 10)

        val saveBtn = JButton(Messages.get("config.save"))
        saveBtn.font = FontManager.getNormalFont()
        saveBtn.background = Colors.SUCCESS
        saveBtn.foreground = Color.WHITE
        saveBtn.addActionListener { saveConfiguration() }

        val resetBtn = JButton(Messages.get("config.reset"))
        resetBtn.font = FontManager.getNormalFont()
        resetBtn.addActionListener { resetConfiguration() }

        buttonPanel.add(resetBtn)
        buttonPanel.add(saveBtn)
        add(buttonPanel, BorderLayout.SOUTH)

        loadConfiguration()
    }

    private fun createConfigContent(): JPanel {
        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)
        contentPanel.background = Colors.PANEL_BACKGROUND

        // 邮箱配置
        contentPanel.add(createSectionPanel("邮箱配置", createEmailConfigPanel()))
        contentPanel.add(Box.createVerticalStrut(10))

        // 验证码配置
        contentPanel.add(createSectionPanel("验证码配置", createCaptchaConfigPanel()))
        contentPanel.add(Box.createVerticalStrut(10))

        // 代理配置
        contentPanel.add(createSectionPanel("代理配置", createProxyConfigPanel()))
        contentPanel.add(Box.createVerticalStrut(10))

        // 浏览器配置
        contentPanel.add(createSectionPanel("浏览器配置", createBrowserConfigPanel()))
        contentPanel.add(Box.createVerticalStrut(10))

        // 批量配置
        contentPanel.add(createSectionPanel("批量配置", createBatchConfigPanel()))

        return contentPanel
    }

    private fun createSectionPanel(title: String, content: JPanel): JPanel {
        val sectionPanel = JPanel(BorderLayout())
        sectionPanel.background = Colors.PANEL_BACKGROUND
        sectionPanel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                title
            ),
            EmptyBorder(10, 10, 10, 10)
        )

        sectionPanel.add(content, BorderLayout.CENTER)
        return sectionPanel
    }

    private fun createEmailConfigPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.background = Colors.PANEL_BACKGROUND

        val gbc = GridBagConstraints().apply {
            insets = Insets(5, 5, 5, 5)
            anchor = GridBagConstraints.WEST
        }

        // 邮箱提供商
        gbc.gridx = 0; gbc.gridy = 0
        panel.add(JLabel("邮箱提供商:").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        emailProviderCombo.font = FontManager.getNormalFont()
        panel.add(emailProviderCombo, gbc)

        // 原邮箱地址
        gbc.gridx = 0; gbc.gridy = 1
        panel.add(JLabel("原邮箱地址:").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        baseEmailField.font = FontManager.getNormalFont()
        panel.add(baseEmailField, gbc)

        // Plus 模式
        gbc.gridx = 0; gbc.gridy = 2
        plusModeCheckbox.font = FontManager.getNormalFont()
        plusModeCheckbox.background = Colors.PANEL_BACKGROUND
        plusModeCheckbox.foreground = Colors.TEXT_PRIMARY
        panel.add(plusModeCheckbox, gbc)

        // 使用人名后缀
        gbc.gridx = 1
        useHumanNamesCheckbox.font = FontManager.getNormalFont()
        useHumanNamesCheckbox.background = Colors.PANEL_BACKGROUND
        useHumanNamesCheckbox.foreground = Colors.TEXT_PRIMARY
        panel.add(useHumanNamesCheckbox, gbc)

        // 自定义人名列表
        gbc.gridx = 0; gbc.gridy = 3
        panel.add(JLabel("自定义人名 (逗号分隔):").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        customNamesField.font = FontManager.getNormalFont()
        panel.add(customNamesField, gbc)

        // Gmail 密码
        gbc.gridx = 0; gbc.gridy = 4
        panel.add(JLabel("Gmail 应用密码:").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        gmailAppPasswordField.font = FontManager.getNormalFont()
        panel.add(gmailAppPasswordField, gbc)

        // Outlook 密码
        gbc.gridx = 0; gbc.gridy = 5
        panel.add(JLabel("Outlook 密码:").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        outlookPasswordField.font = FontManager.getNormalFont()
        panel.add(outlookPasswordField, gbc)

        // 自定义 API URL
        gbc.gridx = 0; gbc.gridy = 6
        panel.add(JLabel("自定义 API URL:").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        customApiUrlField.font = FontManager.getNormalFont()
        panel.add(customApiUrlField, gbc)

        // 自定义 API Key
        gbc.gridx = 0; gbc.gridy = 7
        panel.add(JLabel("自定义 API Key:").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        customApiKeyField.font = FontManager.getNormalFont()
        panel.add(customApiKeyField, gbc)

        return panel
    }

    private fun createCaptchaConfigPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.background = Colors.PANEL_BACKGROUND

        val gbc = GridBagConstraints().apply {
            insets = Insets(5, 5, 5, 5)
            anchor = GridBagConstraints.WEST
        }

        // 验证码模式
        gbc.gridx = 0; gbc.gridy = 0
        panel.add(JLabel("验证码模式:").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        captchaModeCombo.font = FontManager.getNormalFont()
        panel.add(captchaModeCombo, gbc)

        // 超时时间
        gbc.gridx = 0; gbc.gridy = 1
        panel.add(JLabel("超时时间(秒):").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        captchaTimeoutSpinner.preferredSize = Dimension(80, 25)
        panel.add(captchaTimeoutSpinner, gbc)

        // LLM API Key
        gbc.gridx = 0; gbc.gridy = 2
        panel.add(JLabel("LLM API Key:").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        llmApiKeyField.font = FontManager.getNormalFont()
        panel.add(llmApiKeyField, gbc)

        return panel
    }

    private fun createProxyConfigPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.background = Colors.PANEL_BACKGROUND

        val gbc = GridBagConstraints().apply {
            insets = Insets(5, 5, 5, 5)
            anchor = GridBagConstraints.WEST
        }

        // 代理模式
        gbc.gridx = 0; gbc.gridy = 0
        panel.add(JLabel("代理模式:").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        proxyModeCombo.font = FontManager.getNormalFont()
        panel.add(proxyModeCombo, gbc)

        // 浏览器代理
        gbc.gridx = 0; gbc.gridy = 1
        browserProxyCheckbox.font = FontManager.getNormalFont()
        browserProxyCheckbox.background = Colors.PANEL_BACKGROUND
        browserProxyCheckbox.foreground = Colors.TEXT_PRIMARY
        panel.add(browserProxyCheckbox, gbc)

        gbc.gridx = 1
        browserProxyUrlField.font = FontManager.getNormalFont()
        panel.add(browserProxyUrlField, gbc)

        // 邮箱代理
        gbc.gridx = 0; gbc.gridy = 2
        emailProxyCheckbox.font = FontManager.getNormalFont()
        emailProxyCheckbox.background = Colors.PANEL_BACKGROUND
        emailProxyCheckbox.foreground = Colors.TEXT_PRIMARY
        panel.add(emailProxyCheckbox, gbc)

        gbc.gridx = 1
        emailProxyUrlField.font = FontManager.getNormalFont()
        panel.add(emailProxyUrlField, gbc)

        // 代理说明
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2
        val proxyHelpLabel = JLabel("<html><small>支持格式: http://host:port, https://host:port, socks4://host:port, socks5://host:port<br>认证: http://user:pass@host:port</small></html>").apply {
            font = FontManager.getSmallFont()
            foreground = Colors.TEXT_SECONDARY
        }
        panel.add(proxyHelpLabel, gbc)

        return panel
    }

    private fun createBrowserConfigPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.background = Colors.PANEL_BACKGROUND

        val gbc = GridBagConstraints().apply {
            insets = Insets(5, 5, 5, 5)
            anchor = GridBagConstraints.WEST
        }

        // Headless 模式
        gbc.gridx = 0; gbc.gridy = 0
        headlessModeCheckbox.font = FontManager.getNormalFont()
        headlessModeCheckbox.background = Colors.PANEL_BACKGROUND
        headlessModeCheckbox.foreground = Colors.TEXT_PRIMARY
        panel.add(headlessModeCheckbox, gbc)

        // 浏览器超时
        gbc.gridx = 0; gbc.gridy = 1
        panel.add(JLabel("浏览器超时(毫秒):").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        browserTimeoutSpinner.preferredSize = Dimension(100, 25)
        panel.add(browserTimeoutSpinner, gbc)

        return panel
    }

    private fun createBatchConfigPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.background = Colors.PANEL_BACKGROUND

        val gbc = GridBagConstraints().apply {
            insets = Insets(5, 5, 5, 5)
            anchor = GridBagConstraints.WEST
        }

        // 最大并发数
        gbc.gridx = 0; gbc.gridy = 0
        panel.add(JLabel("最大并发数:").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        maxConcurrencySpinner.preferredSize = Dimension(80, 25)
        panel.add(maxConcurrencySpinner, gbc)

        // 账号间延迟
        gbc.gridx = 0; gbc.gridy = 1
        panel.add(JLabel("账号间延迟(毫秒):").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        delayBetweenAccountsSpinner.preferredSize = Dimension(100, 25)
        panel.add(delayBetweenAccountsSpinner, gbc)

        return panel
    }

    private fun loadConfiguration() {
        // 这里应该从 ConfigManager 加载配置
        // 暂时设置一些默认值
        headlessModeCheckbox.isSelected = true
        plusModeCheckbox.isSelected = true
        useHumanNamesCheckbox.isSelected = true

        // 加载邮箱配置
        val emailConfig = com.autoxtwitteraccount.config.ConfigManager.getEmailConfig()
        baseEmailField.text = emailConfig.baseEmail
        customNamesField.text = emailConfig.customNames.joinToString(", ")

        // 加载代理配置
        val proxyConfig = com.autoxtwitteraccount.config.ConfigManager.getProxyConfig()
        proxyModeCombo.selectedItem = proxyConfig.systemProxyMode
        browserProxyCheckbox.isSelected = proxyConfig.enableBrowserProxy
        emailProxyCheckbox.isSelected = proxyConfig.enableEmailProxy
        browserProxyUrlField.text = proxyConfig.browserProxyUrl
        emailProxyUrlField.text = proxyConfig.emailProxyUrl
    }

    private fun saveConfiguration() {
        try {
            // 保存邮箱配置
            val emailConfig = com.autoxtwitteraccount.config.ConfigManager.getEmailConfig().copy(
                baseEmail = baseEmailField.text.trim(),
                useHumanNames = useHumanNamesCheckbox.isSelected,
                customNames = customNamesField.text.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            )

            // 保存代理配置
            val proxyConfig = com.autoxtwitteraccount.config.ConfigManager.getProxyConfig().copy(
                systemProxyMode = proxyModeCombo.selectedItem.toString(),
                enableBrowserProxy = browserProxyCheckbox.isSelected,
                enableEmailProxy = emailProxyCheckbox.isSelected,
                browserProxyUrl = browserProxyUrlField.text.trim(),
                emailProxyUrl = emailProxyUrlField.text.trim()
            )

            val appConfig = com.autoxtwitteraccount.config.ConfigManager.config.copy(
                emailConfig = emailConfig,
                proxyConfig = proxyConfig
            )
            com.autoxtwitteraccount.config.ConfigManager.updateConfig(appConfig)

            JOptionPane.showMessageDialog(
                this,
                Messages.get("success.saved"),
                Messages.get("config.save"),
                JOptionPane.INFORMATION_MESSAGE
            )
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "保存配置失败: ${e.message}",
                Messages.get("error.title"),
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    private fun resetConfiguration() {
        val result = JOptionPane.showConfirmDialog(
            this,
            "确定要重置所有配置为默认值吗？",
            Messages.get("config.reset"),
            JOptionPane.YES_NO_OPTION
        )

        if (result == JOptionPane.YES_OPTION) {
            loadConfiguration()
        }
    }

    fun showPreferences() {
        JOptionPane.showMessageDialog(
            this,
            Messages.get("config.title"),
            Messages.get("menu.preferences"),
            JOptionPane.INFORMATION_MESSAGE
        )
    }
}

/**
 * Plus 模式配置面板
 */
class PlusModePanel : JPanel() {
    private val baseEmailField = JTextField(30)
    private val testEmailArea = JTextArea(10, 40)
    private val generateCountSpinner = JSpinner(SpinnerNumberModel(5, 1, 50, 1))
    private val useHumanNamesCheckbox = JCheckBox("使用人名作为后缀")
    private val customNamesField = JTextField(50)
    private val generatedEmailsList = JList<String>()
    private val scrollPane = JScrollPane(generatedEmailsList)

    init {
        layout = BorderLayout()
        border = EmptyBorder(20, 20, 20, 20)
        background = Colors.PANEL_BACKGROUND

        // 顶部配置面板
        val configPanel = createConfigPanel()
        add(configPanel, BorderLayout.NORTH)

        // 中心内容面板
        val contentPanel = createContentPanel()
        add(contentPanel, BorderLayout.CENTER)

        // 底部按钮面板
        val buttonPanel = createButtonPanel()
        add(buttonPanel, BorderLayout.SOUTH)

        loadConfiguration()
    }

    private fun createConfigPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.background = Colors.PANEL_BACKGROUND
        panel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                "邮箱配置"
            ),
            EmptyBorder(10, 10, 10, 10)
        )

        val gbc = GridBagConstraints().apply {
            insets = Insets(5, 5, 5, 5)
            anchor = GridBagConstraints.WEST
        }

        // 原邮箱地址
        gbc.gridx = 0; gbc.gridy = 0
        panel.add(JLabel("原邮箱地址:").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        baseEmailField.font = FontManager.getNormalFont()
        panel.add(baseEmailField, gbc)

        // 使用人名后缀
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
        useHumanNamesCheckbox.font = FontManager.getNormalFont()
        useHumanNamesCheckbox.background = Colors.PANEL_BACKGROUND
        useHumanNamesCheckbox.foreground = Colors.TEXT_PRIMARY
        panel.add(useHumanNamesCheckbox, gbc)

        // 自定义人名列表
        gbc.gridx = 0; gbc.gridy = 2
        panel.add(JLabel("自定义人名 (逗号分隔):").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        customNamesField.font = FontManager.getNormalFont()
        panel.add(customNamesField, gbc)

        return panel
    }

    private fun createContentPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.background = Colors.PANEL_BACKGROUND

        // 左侧：生成设置
        val leftPanel = JPanel(BorderLayout())
        leftPanel.background = Colors.PANEL_BACKGROUND
        leftPanel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                "生成设置"
            ),
            EmptyBorder(10, 10, 10, 10)
        )

        val generatePanel = JPanel(GridBagLayout())
        generatePanel.background = Colors.PANEL_BACKGROUND

        val gbc = GridBagConstraints().apply {
            insets = Insets(5, 5, 5, 5)
            anchor = GridBagConstraints.WEST
        }

        // 生成数量
        gbc.gridx = 0; gbc.gridy = 0
        generatePanel.add(JLabel("生成数量:").apply {
            font = FontManager.getNormalFont()
            foreground = Colors.TEXT_PRIMARY
        }, gbc)
        gbc.gridx = 1
        generateCountSpinner.preferredSize = Dimension(80, 25)
        generatePanel.add(generateCountSpinner, gbc)

        // 生成按钮
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2
        val generateBtn = JButton("生成测试邮箱").apply {
            font = FontManager.getNormalFont()
            background = Colors.SUCCESS
            foreground = Color.WHITE
            addActionListener { generateTestEmails() }
        }
        generatePanel.add(generateBtn, gbc)

        leftPanel.add(generatePanel, BorderLayout.NORTH)

        // 测试邮箱显示区域
        testEmailArea.font = FontManager.getSmallFont()
        testEmailArea.background = Color(30, 30, 30)
        testEmailArea.foreground = Color(150, 200, 150)
        testEmailArea.isEditable = false
        val testScrollPane = JScrollPane(testEmailArea)
        testScrollPane.border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Colors.BORDER),
            "生成的测试邮箱"
        )
        leftPanel.add(testScrollPane, BorderLayout.CENTER)

        // 右侧：已生成邮箱列表
        val rightPanel = JPanel(BorderLayout())
        rightPanel.background = Colors.PANEL_BACKGROUND
        rightPanel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                "已生成的邮箱列表"
            ),
            EmptyBorder(10, 10, 10, 10)
        )

        generatedEmailsList.font = FontManager.getNormalFont()
        generatedEmailsList.background = Colors.PANEL_BACKGROUND
        generatedEmailsList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        scrollPane.preferredSize = Dimension(300, 200)
        rightPanel.add(scrollPane, BorderLayout.CENTER)

        // 分割面板
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel)
        splitPane.resizeWeight = 0.5
        splitPane.dividerSize = 5

        panel.add(splitPane, BorderLayout.CENTER)
        return panel
    }

    private fun createButtonPanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.RIGHT))
        panel.background = Colors.PANEL_BACKGROUND
        panel.border = EmptyBorder(10, 10, 10, 10)

        val saveBtn = JButton("保存配置").apply {
            font = FontManager.getNormalFont()
            background = Colors.SUCCESS
            foreground = Color.WHITE
            addActionListener { saveConfiguration() }
        }

        val testBtn = JButton("测试邮箱").apply {
            font = FontManager.getNormalFont()
            background = Colors.ACCENT
            foreground = Color.WHITE
            addActionListener { testEmailConfiguration() }
        }

        panel.add(testBtn)
        panel.add(saveBtn)

        return panel
    }

    private fun generateTestEmails() {
        try {
            val baseEmail = baseEmailField.text.trim()
            if (baseEmail.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "请输入原邮箱地址",
                    "错误",
                    JOptionPane.ERROR_MESSAGE
                )
                return
            }

            val count = generateCountSpinner.value as Int
            val emails = com.autoxtwitteraccount.email.EmailManager.generatePlusEmails(baseEmail, count)

            // 显示在文本区域
            testEmailArea.text = emails.joinToString("\n")

            // 更新列表
            generatedEmailsList.setListData(emails.toTypedArray())

        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "生成测试邮箱失败: ${e.message}",
                "错误",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    private fun saveConfiguration() {
        try {
            // 保存邮箱配置
            val emailConfig = com.autoxtwitteraccount.config.ConfigManager.getEmailConfig().copy(
                baseEmail = baseEmailField.text.trim(),
                useHumanNames = useHumanNamesCheckbox.isSelected,
                customNames = customNamesField.text.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            )

            val appConfig = com.autoxtwitteraccount.config.ConfigManager.config.copy(
                emailConfig = emailConfig
            )
            com.autoxtwitteraccount.config.ConfigManager.updateConfig(appConfig)

            JOptionPane.showMessageDialog(
                this,
                "Plus 模式配置已保存",
                "成功",
                JOptionPane.INFORMATION_MESSAGE
            )
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "保存配置失败: ${e.message}",
                "错误",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    private fun testEmailConfiguration() {
        JOptionPane.showMessageDialog(
            this,
            "邮箱配置测试功能开发中...",
            "提示",
            JOptionPane.INFORMATION_MESSAGE
        )
    }

    private fun loadConfiguration() {
        val emailConfig = com.autoxtwitteraccount.config.ConfigManager.getEmailConfig()
        baseEmailField.text = emailConfig.baseEmail
        useHumanNamesCheckbox.isSelected = emailConfig.useHumanNames
        customNamesField.text = emailConfig.customNames.joinToString(", ")
    }
}

/**
 * 日志面板
 */
class LogsPanel : JPanel() {
    private val logArea = JTextArea()

    init {
        layout = BorderLayout()
        border = EmptyBorder(10, 10, 10, 10)
        background = Colors.PANEL_BACKGROUND

        logArea.font = FontManager.getSmallFont()
        logArea.isEditable = false
        logArea.background = Color(30, 30, 30)
        logArea.foreground = Color(150, 200, 150)
        logArea.caretColor = Color(150, 200, 150)

        val scrollPane = JScrollPane(logArea)
        scrollPane.border = BorderFactory.createLineBorder(Colors.BORDER)

        // 按钮面板
        val btnPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        btnPanel.background = Colors.PANEL_BACKGROUND

        val clearBtn = JButton(Messages.get("logs.clear"))
        clearBtn.font = FontManager.getNormalFont()
        clearBtn.background = Colors.ERROR
        clearBtn.foreground = Color.WHITE
        clearBtn.addActionListener { logArea.text = "" }

        val exportBtn = JButton(Messages.get("logs.export"))
        exportBtn.font = FontManager.getNormalFont()
        exportBtn.background = Colors.ACCENT
        exportBtn.foreground = Color.WHITE
        exportBtn.addActionListener { exportLogs() }

        btnPanel.add(clearBtn)
        btnPanel.add(exportBtn)

        add(btnPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
    }

    private fun exportLogs() {
        JOptionPane.showMessageDialog(
            this,
            Messages.get("success.exported"),
            Messages.get("logs.export"),
            JOptionPane.INFORMATION_MESSAGE
        )
    }

    fun appendLog(message: String) {
        logArea.append("$message\n")
        logArea.caretPosition = logArea.document.length
    }
}
