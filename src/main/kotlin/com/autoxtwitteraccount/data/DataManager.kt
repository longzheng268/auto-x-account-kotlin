package com.autoxtwitteraccount.data

import com.autoxtwitteraccount.twitter.TwitterAccount
import com.autoxtwitteraccount.twitter.RegistrationResult
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.serialization.json.Json
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVParser
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path

private val logger = LoggerFactory.getLogger("DataManager")

/**
 * 数据管理器 - 支持 JSON, CSV, Excel 格式的导入和导出
 */
object DataManager {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
        .create()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // ==================== JSON 操作 ====================

    /**
     * 从 JSON 文件导入账户列表
     */
    fun importAccountsFromJson(filePath: String): List<TwitterAccount> {
        logger.info("Importing accounts from JSON: $filePath")

        return try {
            val file = File(filePath)
            if (!file.exists()) {
                logger.warn("File not found: $filePath")
                return emptyList()
            }

            val json = file.readText()
            val accounts = gson.fromJson(json, Array<TwitterAccount>::class.java).toList()
            logger.info("Imported ${accounts.size} accounts from JSON")
            accounts
        } catch (e: Exception) {
            logger.error("Failed to import accounts from JSON", e)
            emptyList()
        }
    }

    /**
     * 将账户列表导出为 JSON 文件
     */
    fun exportAccountsToJson(accounts: List<TwitterAccount>, filePath: String): Boolean {
        logger.info("Exporting ${accounts.size} accounts to JSON: $filePath")

        return try {
            val json = gson.toJson(accounts)
            File(filePath).writeText(json)
            logger.info("Successfully exported accounts to JSON")
            true
        } catch (e: Exception) {
            logger.error("Failed to export accounts to JSON", e)
            false
        }
    }

    /**
     * 将注册结果导出为 JSON 文件
     */
    fun exportResultsToJson(results: List<RegistrationResult>, filePath: String): Boolean {
        logger.info("Exporting ${results.size} results to JSON: $filePath")

        return try {
            val json = gson.toJson(results)
            File(filePath).writeText(json)
            logger.info("Successfully exported results to JSON")
            true
        } catch (e: Exception) {
            logger.error("Failed to export results to JSON", e)
            false
        }
    }

    // ==================== CSV 操作 ====================

    /**
     * 从 CSV 文件导入账户列表
     */
    fun importAccountsFromCsv(filePath: String): List<TwitterAccount> {
        logger.info("Importing accounts from CSV: $filePath")

        return try {
            val file = File(filePath)
            if (!file.exists()) {
                logger.warn("File not found: $filePath")
                return emptyList()
            }

            val accounts = mutableListOf<TwitterAccount>()
            val format = CSVFormat.DEFAULT.withFirstRecordAsHeader()
            val parser = CSVParser(FileReader(file), format)

            for (record in parser) {
                try {
                    val account = TwitterAccount(
                        email = record.get("email") ?: "",
                        name = record.get("name") ?: "",
                        password = record.get("password") ?: "",
                        dateOfBirth = parseDate(record.get("dateOfBirth") ?: ""),
                        username = record.get("username") ?: "",
                        phone = record.get("phone")
                    )
                    accounts.add(account)
                } catch (e: Exception) {
                    logger.warn("Skipping invalid record: ${e.message}")
                }
            }

            parser.close()
            logger.info("Imported ${accounts.size} accounts from CSV")
            accounts
        } catch (e: Exception) {
            logger.error("Failed to import accounts from CSV", e)
            emptyList()
        }
    }

    /**
     * 将账户列表导出为 CSV 文件
     */
    fun exportAccountsToCsv(accounts: List<TwitterAccount>, filePath: String): Boolean {
        logger.info("Exporting ${accounts.size} accounts to CSV: $filePath")

        return try {
            val file = File(filePath)
            val format = CSVFormat.DEFAULT
                .withHeader("email", "name", "password", "dateOfBirth", "username", "phone")

            val writer = FileWriter(file)
            val printer = CSVPrinter(writer, format)

            for (account in accounts) {
                printer.printRecord(
                    account.email,
                    account.name,
                    account.password,
                    account.dateOfBirth.format(dateFormatter),
                    account.username,
                    account.phone ?: ""
                )
            }

            printer.flush()
            printer.close()
            writer.close()

            logger.info("Successfully exported accounts to CSV")
            true
        } catch (e: Exception) {
            logger.error("Failed to export accounts to CSV", e)
            false
        }
    }

    /**
     * 将注册结果导出为 CSV 文件
     */
    fun exportResultsToCsv(results: List<RegistrationResult>, filePath: String): Boolean {
        logger.info("Exporting ${results.size} results to CSV: $filePath")

        return try {
            val file = File(filePath)
            val format = CSVFormat.DEFAULT
                .withHeader("email", "status", "errorMessage", "timestamp", "duration")

            val writer = FileWriter(file)
            val printer = CSVPrinter(writer, format)

            for (result in results) {
                printer.printRecord(
                    result.email,
                    result.status.name,
                    result.errorMessage ?: "",
                    result.timestamp,
                    result.duration
                )
            }

            printer.flush()
            printer.close()
            writer.close()

            logger.info("Successfully exported results to CSV")
            true
        } catch (e: Exception) {
            logger.error("Failed to export results to CSV", e)
            false
        }
    }

    // ==================== Excel 操作 ====================

    /**
     * 从 Excel 文件导入账户列表
     */
    fun importAccountsFromExcel(filePath: String): List<TwitterAccount> {
        logger.info("Importing accounts from Excel: $filePath")

        return try {
            val file = File(filePath)
            if (!file.exists()) {
                logger.warn("File not found: $filePath")
                return emptyList()
            }

            val workbook = WorkbookFactory.create(file)
            val sheet = workbook.getSheetAt(0)
            val accounts = mutableListOf<TwitterAccount>()

            // 获取表头
            val headerRow = sheet.getRow(0)
            val emailIndex = headerRow?.getCell(0)?.stringCellValue?.let { 0 } ?: 0
            val nameIndex = 1
            val passwordIndex = 2
            val dobIndex = 3

            // 遍历数据行
            for (i in 1 until sheet.physicalNumberOfRows) {
                try {
                    val row = sheet.getRow(i) ?: continue
                    val account = TwitterAccount(
                        email = row.getCell(0)?.stringCellValue ?: "",
                        name = row.getCell(1)?.stringCellValue ?: "",
                        password = row.getCell(2)?.stringCellValue ?: "",
                        dateOfBirth = parseDate(row.getCell(3)?.stringCellValue ?: "")
                    )
                    accounts.add(account)
                } catch (e: Exception) {
                    logger.warn("Skipping invalid row: ${e.message}")
                }
            }

            workbook.close()
            logger.info("Imported ${accounts.size} accounts from Excel")
            accounts
        } catch (e: Exception) {
            logger.error("Failed to import accounts from Excel", e)
            emptyList()
        }
    }

    /**
     * 将账户列表导出为 Excel 文件
     */
    fun exportAccountsToExcel(accounts: List<TwitterAccount>, filePath: String): Boolean {
        logger.info("Exporting ${accounts.size} accounts to Excel: $filePath")

        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Accounts")

            // 创建表头
            val headerRow = sheet.createRow(0)
            val headers = listOf("email", "name", "password", "dateOfBirth", "username", "phone")
            for ((index, header) in headers.withIndex()) {
                headerRow.createCell(index).setCellValue(header)
            }

            // 创建数据行
            for ((rowIndex, account) in accounts.withIndex()) {
                val row = sheet.createRow(rowIndex + 1)
                row.createCell(0).setCellValue(account.email)
                row.createCell(1).setCellValue(account.name)
                row.createCell(2).setCellValue(account.password)
                row.createCell(3).setCellValue(account.dateOfBirth.format(dateFormatter))
                row.createCell(4).setCellValue(account.username)
                row.createCell(5).setCellValue(account.phone ?: "")
            }

            // 自动调整列宽
            for (i in headers.indices) {
                sheet.autoSizeColumn(i)
            }

            val file = File(filePath)
            workbook.write(file.outputStream())
            workbook.close()

            logger.info("Successfully exported accounts to Excel")
            true
        } catch (e: Exception) {
            logger.error("Failed to export accounts to Excel", e)
            false
        }
    }

    /**
     * 将注册结果导出为 Excel 文件
     */
    fun exportResultsToExcel(results: List<RegistrationResult>, filePath: String): Boolean {
        logger.info("Exporting ${results.size} results to Excel: $filePath")

        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Results")

            // 创建表头
            val headerRow = sheet.createRow(0)
            val headers = listOf("email", "status", "errorMessage", "timestamp", "duration")
            for ((index, header) in headers.withIndex()) {
                headerRow.createCell(index).setCellValue(header)
            }

            // 创建数据行
            for ((rowIndex, result) in results.withIndex()) {
                val row = sheet.createRow(rowIndex + 1)
                row.createCell(0).setCellValue(result.email)
                row.createCell(1).setCellValue(result.status.name)
                row.createCell(2).setCellValue(result.errorMessage ?: "")
                row.createCell(3).setCellValue(result.timestamp.toString())
                row.createCell(4).setCellValue(result.duration.toString())
            }

            // 自动调整列宽
            for (i in headers.indices) {
                sheet.autoSizeColumn(i)
            }

            val file = File(filePath)
            workbook.write(file.outputStream())
            workbook.close()

            logger.info("Successfully exported results to Excel")
            true
        } catch (e: Exception) {
            logger.error("Failed to export results to Excel", e)
            false
        }
    }

    /**
     * 解析日期字符串
     */
    private fun parseDate(dateStr: String): LocalDate {
        return try {
            LocalDate.parse(dateStr, dateFormatter)
        } catch (e: Exception) {
            logger.warn("Failed to parse date: $dateStr, using default")
            LocalDate.now()
        }
    }
}

/**
 * LocalDate 类型适配器（用于 Gson）
 */
class LocalDateTypeAdapter : com.google.gson.JsonSerializer<LocalDate>,
    com.google.gson.JsonDeserializer<LocalDate> {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun serialize(src: LocalDate, typeOfSrc: java.lang.reflect.Type, context: com.google.gson.JsonSerializationContext): com.google.gson.JsonElement {
        return com.google.gson.JsonPrimitive(src.format(formatter))
    }

    override fun deserialize(json: com.google.gson.JsonElement, typeOfT: java.lang.reflect.Type, context: com.google.gson.JsonDeserializationContext): LocalDate {
        return LocalDate.parse(json.asString, formatter)
    }
}
