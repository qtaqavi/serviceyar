package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.CustomToolCategory
import com.example.data.model.IntervalType
import com.example.data.model.ServiceLogEntity
import com.example.data.model.ServicePriority
import com.example.data.model.ServiceScheduleEntity
import com.example.data.model.ServiceType
import com.example.data.model.ToolCategory
import com.example.data.model.ToolEntity
import com.example.ui.theme.AppFont
import com.example.ui.theme.ThemeMode
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupSettingsData(
    val themeModeName: String = ThemeMode.LIGHT.name,
    val customThemeColorHex: Long = 0xFF005AC1L,
    val appFontName: String = AppFont.VAZIR.name,
    val fontScale: Float = 1.0f,
    val smsAlertPhone: String = "",
    val isSmsAlertEnabled: Boolean = false,
    val customCategories: List<CustomToolCategory> = emptyList()
)

data class BackupPayload(
    val backupVersion: Int = 2,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val createdAtJalali: String = JalaliCalendar.now().format(includeDayName = true),
    val tools: List<ToolEntity>,
    val schedules: List<ServiceScheduleEntity>,
    val logs: List<ServiceLogEntity>,
    val settings: BackupSettingsData? = null
)

data class LocalBackupFileInfo(
    val file: File,
    val fileName: String,
    val createdAtJalali: String,
    val createdAtTime: String,
    val fileSizeKb: Long,
    val toolsCount: Int,
    val schedulesCount: Int,
    val logsCount: Int
)

object BackupRestoreManager {

    private const val BACKUP_DIR_NAME = "backups"
    private const val GOOGLE_DRIVE_PACKAGE = "com.google.android.apps.docs"

    fun generateDefaultBackupFileName(): String {
        val nowJalali = JalaliCalendar.now()
        val timeStr = SimpleDateFormat("HHmm", Locale.US).format(Date())
        return "ServiceYar_Backup_${nowJalali.year}_${
            "%02d".format(Locale.US, nowJalali.month)
        }_${"%02d".format(Locale.US, nowJalali.day)}_$timeStr.json"
    }

    fun toJsonString(payload: BackupPayload): String {
        val root = JSONObject()
        root.put("backupSignature", "SERVICE_YAR_BACKUP")
        root.put("backupVersion", payload.backupVersion)
        root.put("createdAtTimestamp", payload.createdAtTimestamp)
        root.put("createdAtJalali", payload.createdAtJalali)
        root.put("toolsCount", payload.tools.size)
        root.put("schedulesCount", payload.schedules.size)
        root.put("logsCount", payload.logs.size)

        payload.settings?.let { s ->
            val settingsObj = JSONObject()
            settingsObj.put("themeModeName", s.themeModeName)
            settingsObj.put("customThemeColorHex", s.customThemeColorHex)
            settingsObj.put("appFontName", s.appFontName)
            settingsObj.put("fontScale", s.fontScale.toDouble())
            settingsObj.put("smsAlertPhone", s.smsAlertPhone)
            settingsObj.put("isSmsAlertEnabled", s.isSmsAlertEnabled)
            val customCatsArr = JSONArray()
            s.customCategories.forEach { cat ->
                val catObj = JSONObject()
                catObj.put("key", cat.key)
                catObj.put("titlePersian", cat.titlePersian)
                catObj.put("iconKey", cat.iconKey)
                catObj.put("colorHex", cat.colorHex)
                catObj.put("description", cat.description)
                catObj.put("isVehicleType", cat.isVehicleType)
                customCatsArr.put(catObj)
            }
            settingsObj.put("customCategories", customCatsArr)
            root.put("settings", settingsObj)
        }

        val toolsArray = JSONArray()
        payload.tools.forEach { tool ->
            val obj = JSONObject()
            obj.put("id", tool.id)
            obj.put("name", tool.name)
            obj.put("categoryName", tool.categoryName)
            obj.put("modelOrBrand", tool.modelOrBrand)
            obj.put("location", tool.location)
            obj.put("serialNumber", tool.serialNumber)
            obj.put("currentOdometerKm", tool.currentOdometerKm)
            obj.put("purchaseDateJalali", tool.purchaseDateJalali)
            obj.put("purchasePrice", tool.purchasePrice)
            obj.put("notes", tool.notes)
            obj.put("iconName", tool.iconName)
            obj.put("isArchived", tool.isArchived)
            obj.put("createdAt", tool.createdAt)
            toolsArray.put(obj)
        }
        root.put("tools", toolsArray)

        val schedulesArray = JSONArray()
        payload.schedules.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("toolId", s.toolId)
            obj.put("toolName", s.toolName)
            obj.put("title", s.title)
            obj.put("serviceTypeName", s.serviceTypeName)
            obj.put("intervalTypeName", s.intervalTypeName)
            obj.put("customIntervalDays", s.customIntervalDays)
            obj.put("intervalKilometers", s.intervalKilometers)
            obj.put("dailyKilometers", s.dailyKilometers)
            obj.put("lastServiceOdometerKm", s.lastServiceOdometerKm)
            obj.put("nextServiceOdometerKm", s.nextServiceOdometerKm)
            obj.put("lastServiceDateJalali", s.lastServiceDateJalali)
            obj.put("nextServiceDateJalali", s.nextServiceDateJalali)
            obj.put("expiryDateJalali", s.expiryDateJalali)
            obj.put("priorityName", s.priorityName)
            obj.put("estimatedCost", s.estimatedCost)
            obj.put("technicianName", s.technicianName)
            obj.put("technicianPhone", s.technicianPhone)
            obj.put("reminderDaysBefore", s.reminderDaysBefore)
            obj.put("notes", s.notes)
            obj.put("isCompleted", s.isCompleted)
            obj.put("lastCompletedTimestamp", s.lastCompletedTimestamp)
            schedulesArray.put(obj)
        }
        root.put("schedules", schedulesArray)

        val logsArray = JSONArray()
        payload.logs.forEach { l ->
            val obj = JSONObject()
            obj.put("id", l.id)
            obj.put("toolId", l.toolId)
            obj.put("toolName", l.toolName)
            obj.put("serviceScheduleId", l.serviceScheduleId)
            obj.put("serviceTitle", l.serviceTitle)
            obj.put("performedDateJalali", l.performedDateJalali)
            obj.put("performedOdometerKm", l.performedOdometerKm)
            obj.put("actualCost", l.actualCost)
            obj.put("technicianOrShop", l.technicianOrShop)
            obj.put("invoiceNumber", l.invoiceNumber)
            obj.put("partsReplaced", l.partsReplaced)
            obj.put("notes", l.notes)
            obj.put("timestamp", l.timestamp)
            logsArray.put(obj)
        }
        root.put("logs", logsArray)

        return root.toString(2)
    }

    fun fromJsonString(jsonString: String): Result<BackupPayload> {
        return try {
            val cleanJson = jsonString.trim().removePrefix("\uFEFF")
            val root = JSONObject(cleanJson)

            if (!root.has("tools") && !root.has("schedules")) {
                return Result.failure(IllegalArgumentException("فرمت فایل پشتیبان معتبر نیست."))
            }

            val backupVersion = root.optInt("backupVersion", 1)
            val createdAtTimestamp = root.optLong("createdAtTimestamp", System.currentTimeMillis())
            val createdAtJalali = root.optString("createdAtJalali", "")

            val settingsObj = root.optJSONObject("settings")
            val settings = if (settingsObj != null) {
                val customCatsList = mutableListOf<CustomToolCategory>()
                val customCatsArr = settingsObj.optJSONArray("customCategories")
                if (customCatsArr != null) {
                    for (i in 0 until customCatsArr.length()) {
                        val cObj = customCatsArr.optJSONObject(i) ?: continue
                        val key = cObj.optString("key").takeIf { it.isNotBlank() } ?: continue
                        val title = cObj.optString("titlePersian").takeIf { it.isNotBlank() } ?: continue
                        customCatsList.add(
                            CustomToolCategory(
                                key = key,
                                titlePersian = title,
                                iconKey = cObj.optString("iconKey", "CATEGORY"),
                                colorHex = cObj.optLong("colorHex", 0xFF0284C7L),
                                description = cObj.optString("description", "دسته‌بندی سفارشی"),
                                isVehicleType = cObj.optBoolean("isVehicleType", false)
                            )
                        )
                    }
                }
                BackupSettingsData(
                    themeModeName = settingsObj.optString("themeModeName", ThemeMode.LIGHT.name),
                    customThemeColorHex = settingsObj.optLong("customThemeColorHex", 0xFF005AC1L),
                    appFontName = settingsObj.optString("appFontName", AppFont.VAZIR.name),
                    fontScale = settingsObj.optDouble("fontScale", 1.0).toFloat().coerceIn(0.80f, 1.35f),
                    smsAlertPhone = settingsObj.optString("smsAlertPhone", ""),
                    isSmsAlertEnabled = settingsObj.optBoolean("isSmsAlertEnabled", false),
                    customCategories = customCatsList
                )
            } else null

            val toolsList = mutableListOf<ToolEntity>()
            val toolsArray = root.optJSONArray("tools") ?: JSONArray()
            for (i in 0 until toolsArray.length()) {
                val obj = toolsArray.getJSONObject(i)
                toolsList.add(
                    ToolEntity(
                        id = obj.optLong("id", 0L),
                        name = obj.optString("name", "وسیله"),
                        categoryName = obj.optString("categoryName", ToolCategory.HOME_APPLIANCE.name),
                        modelOrBrand = obj.optString("modelOrBrand", ""),
                        location = obj.optString("location", ""),
                        serialNumber = obj.optString("serialNumber", ""),
                        currentOdometerKm = obj.optInt("currentOdometerKm", 0),
                        purchaseDateJalali = obj.optString("purchaseDateJalali", ""),
                        purchasePrice = obj.optLong("purchasePrice", 0L),
                        notes = obj.optString("notes", ""),
                        iconName = obj.optString("iconName", ""),
                        isArchived = obj.optBoolean("isArchived", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val schedulesList = mutableListOf<ServiceScheduleEntity>()
            val schedulesArray = root.optJSONArray("schedules") ?: JSONArray()
            for (i in 0 until schedulesArray.length()) {
                val obj = schedulesArray.getJSONObject(i)
                schedulesList.add(
                    ServiceScheduleEntity(
                        id = obj.optLong("id", 0L),
                        toolId = obj.optLong("toolId", 0L),
                        toolName = obj.optString("toolName", ""),
                        title = obj.optString("title", "سرویس"),
                        serviceTypeName = obj.optString("serviceTypeName", ServiceType.PERIODIC_GENERAL.name),
                        intervalTypeName = obj.optString("intervalTypeName", IntervalType.ANNUAL.name),
                        customIntervalDays = obj.optInt("customIntervalDays", 30),
                        intervalKilometers = obj.optInt("intervalKilometers", 0),
                        dailyKilometers = obj.optInt("dailyKilometers", 0),
                        lastServiceOdometerKm = obj.optInt("lastServiceOdometerKm", 0),
                        nextServiceOdometerKm = obj.optInt("nextServiceOdometerKm", 0),
                        lastServiceDateJalali = obj.optString("lastServiceDateJalali", ""),
                        nextServiceDateJalali = obj.optString("nextServiceDateJalali", ""),
                        expiryDateJalali = obj.optString("expiryDateJalali", ""),
                        priorityName = obj.optString("priorityName", ServicePriority.MEDIUM.name),
                        estimatedCost = obj.optLong("estimatedCost", 0L),
                        technicianName = obj.optString("technicianName", ""),
                        technicianPhone = obj.optString("technicianPhone", ""),
                        reminderDaysBefore = obj.optInt("reminderDaysBefore", 3),
                        notes = obj.optString("notes", ""),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        lastCompletedTimestamp = obj.optLong("lastCompletedTimestamp", 0L)
                    )
                )
            }

            val logsList = mutableListOf<ServiceLogEntity>()
            val logsArray = root.optJSONArray("logs") ?: JSONArray()
            for (i in 0 until logsArray.length()) {
                val obj = logsArray.getJSONObject(i)
                logsList.add(
                    ServiceLogEntity(
                        id = obj.optLong("id", 0L),
                        toolId = obj.optLong("toolId", 0L),
                        toolName = obj.optString("toolName", ""),
                        serviceScheduleId = obj.optLong("serviceScheduleId", 0L),
                        serviceTitle = obj.optString("serviceTitle", ""),
                        performedDateJalali = obj.optString("performedDateJalali", ""),
                        performedOdometerKm = obj.optInt("performedOdometerKm", 0),
                        actualCost = obj.optLong("actualCost", 0L),
                        technicianOrShop = obj.optString("technicianOrShop", ""),
                        invoiceNumber = obj.optString("invoiceNumber", ""),
                        partsReplaced = obj.optString("partsReplaced", ""),
                        notes = obj.optString("notes", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }

            Result.success(
                BackupPayload(
                    backupVersion = backupVersion,
                    createdAtTimestamp = createdAtTimestamp,
                    createdAtJalali = createdAtJalali,
                    tools = toolsList,
                    schedules = schedulesList,
                    logs = logsList,
                    settings = settings
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun writeBackupToUri(context: Context, uri: Uri, payload: BackupPayload): Result<Unit> {
        return try {
            val json = toJsonString(payload)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            } ?: return Result.failure(IllegalStateException("امکان باز کردن مسیر ذخیره‌سازی وجود ندارد."))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun readBackupFromUri(context: Context, uri: Uri): Result<BackupPayload> {
        return try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).readText()
            } ?: return Result.failure(IllegalStateException("امکان خواندن فایل انتخابی وجود ندارد."))
            fromJsonString(json)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLocalBackupsDir(context: Context): File {
        val dir = File(context.filesDir, BACKUP_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun saveLocalBackupFile(context: Context, payload: BackupPayload): Result<File> {
        return try {
            val dir = getLocalBackupsDir(context)
            val fileName = generateDefaultBackupFileName()
            val file = File(dir, fileName)
            val json = toJsonString(payload)
            file.writeText(json, Charsets.UTF_8)
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listLocalBackups(context: Context): List<LocalBackupFileInfo> {
        val dir = getLocalBackupsDir(context)
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(".json", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

        return files.mapNotNull { file ->
            try {
                val text = file.readText(Charsets.UTF_8)
                val root = JSONObject(text.trim().removePrefix("\uFEFF"))
                val timestamp = root.optLong("createdAtTimestamp", file.lastModified())
                val jalaliDate = JalaliCalendar.fromMillis(timestamp).format(includeDayName = false)
                val timeStr = JalaliCalendar.toPersianDigits(
                    SimpleDateFormat("HH:mm", Locale.US).format(Date(timestamp))
                )
                val toolsCount = root.optInt("toolsCount", root.optJSONArray("tools")?.length() ?: 0)
                val schedulesCount = root.optInt("schedulesCount", root.optJSONArray("schedules")?.length() ?: 0)
                val logsCount = root.optInt("logsCount", root.optJSONArray("logs")?.length() ?: 0)

                LocalBackupFileInfo(
                    file = file,
                    fileName = file.name,
                    createdAtJalali = jalaliDate,
                    createdAtTime = timeStr,
                    fileSizeKb = (file.length() / 1024L).coerceAtLeast(1L),
                    toolsCount = toolsCount,
                    schedulesCount = schedulesCount,
                    logsCount = logsCount
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun readLocalBackupFile(file: File): Result<BackupPayload> {
        return try {
            val json = file.readText(Charsets.UTF_8)
            fromJsonString(json)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareBackupFileToCloudOrDrive(
        context: Context,
        file: File,
        preferGoogleDrive: Boolean = false
    ): Boolean {
        return try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            if (preferGoogleDrive) {
                val driveIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "فایل پشتیبان سرویس‌یار (${file.name})")
                    setPackage(GOOGLE_DRIVE_PACKAGE)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                if (driveIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(driveIntent)
                    return true
                }
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "فایل پشتیبان سرویس‌یار (${file.name})")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "فایل پشتیبان برنامه سرویس‌یار - قابل ذخیره‌سازی در گوگل درایو (Google Drive) یا حافظه ابری"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "ذخیره و ارسال فایل پشتیبان (گوگل درایو / ابری)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            false
        }
    }
}
