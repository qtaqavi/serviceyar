package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CustomToolCategory
import com.example.data.model.ServiceLogEntity
import com.example.data.model.ServiceScheduleEntity
import com.example.data.model.ToolCategory
import com.example.data.model.ToolEntity
import com.example.data.model.ToolWithServices
import com.example.data.repository.ToolRepository
import com.example.notifications.ReminderNotificationHelper
import android.content.Context
import android.net.Uri
import com.example.ui.theme.AppFont
import com.example.ui.theme.ThemeMode
import com.example.util.AppPreferences
import com.example.util.BackupPayload
import com.example.util.BackupRestoreManager
import com.example.util.BackupSettingsData
import com.example.util.LocalBackupFileInfo
import com.example.util.SmsHelper
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ToolViewModel(
    application: Application,
    private val repository: ToolRepository
) : AndroidViewModel(application) {

    private val preferences = AppPreferences.getInstance(application)

    val themeMode: StateFlow<ThemeMode> = preferences.themeMode
    val customThemeColorHex: StateFlow<Long> = preferences.customThemeColorHex
    val appFont: StateFlow<AppFont> = preferences.appFont
    val fontScale: StateFlow<Float> = preferences.fontScale
    val smsAlertPhone: StateFlow<String> = preferences.smsAlertPhone
    val isSmsAlertEnabled: StateFlow<Boolean> = preferences.isSmsAlertEnabled
    val customCategories: StateFlow<List<CustomToolCategory>> = preferences.customCategories

    val toolsWithServices: StateFlow<List<ToolWithServices>> =
        repository.allToolsWithServices.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSchedules: StateFlow<List<ServiceScheduleEntity>> =
        repository.allSchedules.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLogs: StateFlow<List<ServiceLogEntity>> =
        repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _localBackups = MutableStateFlow<List<LocalBackupFileInfo>>(emptyList())
    val localBackups: StateFlow<List<LocalBackupFileInfo>> = _localBackups.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialDataIfEmpty()
            refreshLocalBackups()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        preferences.setThemeMode(mode)
    }

    fun setCustomThemeColorHex(colorHex: Long) {
        preferences.setCustomThemeColorHex(colorHex)
    }

    fun setAppFont(font: AppFont) {
        preferences.setAppFont(font)
    }

    fun setFontScale(scale: Float) {
        preferences.setFontScale(scale)
    }

    fun setSmsAlertPhone(phone: String) {
        preferences.setSmsAlertPhone(phone)
    }

    fun setSmsAlertEnabled(enabled: Boolean) {
        preferences.setSmsAlertEnabled(enabled)
    }

    fun addOrUpdateCustomCategory(category: CustomToolCategory) {
        preferences.addOrUpdateCustomCategory(category)
    }

    fun deleteCustomCategory(categoryKey: String, reassignToCategoryKey: String = ToolCategory.OTHER.name) {
        preferences.deleteCustomCategory(categoryKey)
        viewModelScope.launch {
            val affectedTools = toolsWithServices.value.map { it.tool }.filter {
                it.categoryName.equals(categoryKey, ignoreCase = true)
            }
            affectedTools.forEach { tool ->
                repository.updateTool(tool.copy(categoryName = reassignToCategoryKey))
            }
        }
    }

    fun assignToolToCategory(tool: ToolEntity, newCategoryKey: String) {
        viewModelScope.launch {
            repository.updateTool(tool.copy(categoryName = newCategoryKey))
        }
    }

    fun sendTestSms(context: Context): Boolean {
        val phone = smsAlertPhone.value
        if (phone.isBlank()) return false
        val testMessage = "سرویس‌یار: پیامک آزمایشی هشدار سرویس با موفقیت متصل شد. هشدارهای دوره‌ای به این شماره ارسال خواهد شد."
        return SmsHelper.sendServiceAlertSms(context, phone, testMessage)
    }

    fun sendServiceReminderSms(
        context: Context,
        toolName: String,
        serviceTitle: String,
        nextDateJalali: String,
        nextKm: Int = 0
    ): Boolean {
        val phone = smsAlertPhone.value
        if (phone.isBlank()) return false
        val message = SmsHelper.generateServiceReminderText(toolName, serviceTitle, nextDateJalali, nextKm)
        return SmsHelper.sendServiceAlertSms(context, phone, message)
    }

    fun updateVehicleOdometer(toolId: Long, newOdometerKm: Int) {
        viewModelScope.launch {
            repository.updateVehicleOdometer(toolId, newOdometerKm)
        }
    }

    fun insertTool(tool: ToolEntity, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.insertTool(tool)
            onComplete?.invoke(id)
        }
    }

    fun updateTool(tool: ToolEntity) {
        viewModelScope.launch {
            repository.updateTool(tool)
        }
    }

    fun deleteTool(tool: ToolEntity) {
        viewModelScope.launch {
            repository.deleteTool(tool)
        }
    }

    fun insertSchedule(schedule: ServiceScheduleEntity, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.insertSchedule(schedule)
            onComplete?.invoke(id)
        }
    }

    fun updateSchedule(schedule: ServiceScheduleEntity) {
        viewModelScope.launch {
            repository.updateSchedule(schedule)
        }
    }

    fun deleteSchedule(schedule: ServiceScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }

    fun markServiceDone(
        scheduleId: Long,
        performedDateJalali: String,
        actualCost: Long,
        technicianOrShop: String,
        invoiceNumber: String,
        partsReplaced: String,
        notes: String,
        performedOdometerKm: Int = 0
    ) {
        viewModelScope.launch {
            repository.markServiceAsDone(
                scheduleId = scheduleId,
                performedDateJalali = performedDateJalali,
                actualCost = actualCost,
                technicianOrShop = technicianOrShop,
                invoiceNumber = invoiceNumber,
                partsReplaced = partsReplaced,
                notes = notes,
                performedOdometerKm = performedOdometerKm
            )
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.resetToSampleData()
        }
    }

    fun triggerTestNotification(customMessage: String? = null) {
        ReminderNotificationHelper.showTestNotification(getApplication(), customMessage)
    }

    fun refreshLocalBackups() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = BackupRestoreManager.listLocalBackups(getApplication())
            _localBackups.value = list
        }
    }

    private suspend fun buildCurrentBackupPayload(): BackupPayload {
        val (tools, schedules, logs) = repository.getBackupSnapshot()
        val settings = BackupSettingsData(
            themeModeName = themeMode.value.name,
            customThemeColorHex = customThemeColorHex.value,
            appFontName = appFont.value.name,
            fontScale = fontScale.value,
            smsAlertPhone = smsAlertPhone.value,
            isSmsAlertEnabled = isSmsAlertEnabled.value,
            customCategories = customCategories.value
        )
        return BackupPayload(
            tools = tools,
            schedules = schedules,
            logs = logs,
            settings = settings
        )
    }

    private fun applyRestoredSettingsIfPresent(settings: BackupSettingsData?) {
        if (settings == null) return
        try {
            val mode = ThemeMode.valueOf(settings.themeModeName)
            preferences.setThemeMode(mode)
        } catch (_: Exception) {
        }
        preferences.setCustomThemeColorHex(settings.customThemeColorHex)
        val font = AppFont.fromName(settings.appFontName)
        preferences.setAppFont(font)
        preferences.setFontScale(settings.fontScale)
        if (settings.smsAlertPhone.isNotBlank()) {
            preferences.setSmsAlertPhone(settings.smsAlertPhone)
        }
        preferences.setSmsAlertEnabled(settings.isSmsAlertEnabled)
        if (settings.customCategories.isNotEmpty()) {
            preferences.setAllCustomCategories(settings.customCategories)
        }
    }

    fun exportBackupToUri(
        context: Context,
        uri: Uri,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val payload = buildCurrentBackupPayload()
                // Also save a local copy automatically
                BackupRestoreManager.saveLocalBackupFile(context, payload)
                BackupRestoreManager.writeBackupToUri(context, uri, payload)
            }
            refreshLocalBackups()
            result.fold(
                onSuccess = {
                    onResult(true, "فایل پشتیبان با موفقیت در مسیر انتخابی ذخیره شد.")
                },
                onFailure = { e ->
                    onResult(false, "خطا در ذخیره فایل پشتیبان: ${e.localizedMessage ?: "نامشخص"}")
                }
            )
        }
    }

    fun createLocalBackupSnapshot(
        context: Context,
        onResult: (Boolean, String, File?) -> Unit
    ) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val payload = buildCurrentBackupPayload()
                BackupRestoreManager.saveLocalBackupFile(context, payload)
            }
            refreshLocalBackups()
            result.fold(
                onSuccess = { file ->
                    onResult(true, "نسخه پشتیبان در حافظه داخلی ذخیره شد (${file.name}).", file)
                },
                onFailure = { e ->
                    onResult(false, "خطا در ایجاد پشتیبان داخلی: ${e.localizedMessage ?: "نامشخص"}", null)
                }
            )
        }
    }

    fun exportAndShareToCloudDrive(
        context: Context,
        preferGoogleDrive: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val payload = buildCurrentBackupPayload()
                BackupRestoreManager.saveLocalBackupFile(context, payload)
            }
            refreshLocalBackups()
            result.fold(
                onSuccess = { file ->
                    val launched = BackupRestoreManager.shareBackupFileToCloudOrDrive(
                        context = context,
                        file = file,
                        preferGoogleDrive = preferGoogleDrive
                    )
                    if (launched) {
                        onResult(true, "فایل پشتیبان آماده ذخیره در گوگل درایو / فضای ابری شد.")
                    } else {
                        onResult(false, "خطا در باز کردن اشتراک‌گذاری ابری.")
                    }
                },
                onFailure = { e ->
                    onResult(false, "خطا در آماده‌سازی فایل پشتیبان: ${e.localizedMessage ?: "نامشخص"}")
                }
            )
        }
    }

    fun importBackupFromUri(
        context: Context,
        uri: Uri,
        replaceExisting: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val parseResult = withContext(Dispatchers.IO) {
                BackupRestoreManager.readBackupFromUri(context, uri)
            }
            parseResult.fold(
                onSuccess = { payload ->
                    withContext(Dispatchers.IO) {
                        repository.restoreBackupData(
                            tools = payload.tools,
                            schedules = payload.schedules,
                            logs = payload.logs,
                            replaceExisting = replaceExisting
                        )
                    }
                    applyRestoredSettingsIfPresent(payload.settings)
                    onResult(
                        true,
                        "بازگردانی موفق: ${payload.tools.size} وسیله، ${payload.schedules.size} برنامه سرویس و ${payload.logs.size} سابقه بازیابی شد."
                    )
                },
                onFailure = { e ->
                    onResult(false, "خطا در خواندن فایل پشتیبان: ${e.localizedMessage ?: "فایل نامعتبر است"}")
                }
            )
        }
    }

    fun restoreFromLocalBackupFile(
        file: File,
        replaceExisting: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val parseResult = withContext(Dispatchers.IO) {
                BackupRestoreManager.readLocalBackupFile(file)
            }
            parseResult.fold(
                onSuccess = { payload ->
                    withContext(Dispatchers.IO) {
                        repository.restoreBackupData(
                            tools = payload.tools,
                            schedules = payload.schedules,
                            logs = payload.logs,
                            replaceExisting = replaceExisting
                        )
                    }
                    applyRestoredSettingsIfPresent(payload.settings)
                    onResult(
                        true,
                        "نسخه پشتیبان (${payload.tools.size} وسیله و ${payload.schedules.size} برنامه سرویس) با موفقیت بازگردانی شد."
                    )
                },
                onFailure = { e ->
                    onResult(false, "خطا در بازگردانی فایل: ${e.localizedMessage ?: "نامشخص"}")
                }
            )
        }
    }

    fun deleteLocalBackupFile(file: File, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (file.exists()) {
                file.delete()
            }
            val list = BackupRestoreManager.listLocalBackups(getApplication())
            _localBackups.value = list
            withContext(Dispatchers.Main) {
                onResult("فایل پشتیبان حذف شد.")
            }
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
                    val database = AppDatabase.getInstance(application, appScope)
                    val repository = ToolRepository(database.toolDao())
                    return ToolViewModel(application, repository) as T
                }
            }
    }
}
