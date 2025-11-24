package com.autoxtwitteraccount.ui

import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.File

/**
 * 字体管理器 - 支持中文字体自动加载
 */
object FontManager {
    private var chineseFont: Font? = null
    
    init {
        loadChineseFont()
    }
    
    private fun loadChineseFont() {
        // 尝试加载 MiSans 字体
        val misansPath = "fonts/MiSans/可变字体/MiSansVF.ttf"
        if (File(misansPath).exists()) {
            try {
                val font = Font.createFont(Font.TRUETYPE_FONT, File(misansPath))
                chineseFont = font.deriveFont(12f)
                println("Successfully loaded MiSans font from $misansPath")
                return
            } catch (e: Exception) {
                println("Failed to load MiSans font: ${e.message}")
            }
        }
        
        // 根据操作系统加载默认中文字体
        chineseFont = when {
            isWindows() -> tryLoadFont(arrayOf(
                "微软雅黑",
                "Microsoft YaHei",
                "SimHei",
                "黑体",
                "宋体",
                "SimSun"
            ))
            isMacOS() -> tryLoadFont(arrayOf(
                "STHeiti",
                "SimHei",
                "PingFang SC",
                "Hiragino Sans GB"
            ))
            else -> tryLoadFont(arrayOf(
                "WenQuanYi Micro Hei",
                "SimHei",
                "DejaVu Sans"
            ))
        }
    }
    
    private fun tryLoadFont(names: Array<String>): Font {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val availableFonts = ge.availableFontFamilyNames
        
        for (name in names) {
            if (availableFonts.contains(name)) {
                println("Loaded Chinese font: $name")
                return Font(name, Font.PLAIN, 12)
            }
        }
        
        // Fallback: 使用默认字体
        println("Warning: No Chinese font found, using default font")
        return Font(Font.SANS_SERIF, Font.PLAIN, 12)
    }
    
    fun getFont(size: Float = 12f): Font {
        return chineseFont?.deriveFont(size) ?: Font(Font.SANS_SERIF, Font.PLAIN, size.toInt())
    }
    
    fun getSmallFont(): Font = getFont(11f)
    fun getNormalFont(): Font = getFont(12f)
    fun getLargeFont(): Font = getFont(14f)
    fun getHeaderFont(): Font = getFont(16f)
    fun getBoldFont(size: Float = 12f): Font {
        return chineseFont?.deriveFont(Font.BOLD, size) 
            ?: Font(Font.SANS_SERIF, Font.BOLD, size.toInt())
    }
    
    private fun isWindows() = System.getProperty("os.name").lowercase().contains("win")
    private fun isMacOS() = System.getProperty("os.name").lowercase().contains("mac")
}
