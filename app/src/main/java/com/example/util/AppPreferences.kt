package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CustomCategoryRegistry
import com.example.data.model.CustomToolCategory
import com.example.ui.theme.AppFont
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _customThemeColorHex = MutableStateFlow(loadCustomThemeColorHex())
    val customThemeColorHex: StateFlow<Long> = _customThemeColorHex.asStateFlow()

    private val _appFont = MutableStateFlow(loadAppFont())
    val appFont: StateFlow<AppFont> = _appFont.asStateFlow()

    private val _fontScale = MutableStateFlow(loadFontScale())
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _smsAlertPhone = MutableStateFlow(loadSmsAlertPhone())
    val smsAlertPhone: StateFlow<String> = _smsAlertPhone.asStateFlow()

    private val _isSmsAlertEnabled = MutableStateFlow(loadIsSmsAlertEnabled())
    val isSmsAlertEnabled: StateFlow<Boolean> = _isSmsAlertEnabled.asStateFlow()

    private val _customCategories = MutableStateFlow(loadCustomCategories())
    val customCategories: StateFlow<List<CustomToolCategory>> = _customCategories.asStateFlow()

    init {
        CustomCategoryRegistry.customCategories = _customCategories.value
    }

    private fun loadThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.LIGHT.name)
        return try {
            ThemeMode.valueOf(name ?: ThemeMode.LIGHT.name)
        } catch (e: Exception) {
            ThemeMode.LIGHT
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    private fun loadCustomThemeColorHex(): Long {
        return prefs.getLong(KEY_CUSTOM_THEME_COLOR, 0xFF005AC1L)
    }

    fun setCustomThemeColorHex(colorHex: Long) {
        val normalized = colorHex or 0xFF000000L
        prefs.edit().putLong(KEY_CUSTOM_THEME_COLOR, normalized).apply()
        _customThemeColorHex.value = normalized
    }

    private fun loadAppFont(): AppFont {
        val name = prefs.getString(KEY_APP_FONT, AppFont.VAZIR.name)
        return AppFont.fromName(name)
    }

    fun setAppFont(font: AppFont) {
        prefs.edit().putString(KEY_APP_FONT, font.name).apply()
        _appFont.value = font
    }

    private fun loadFontScale(): Float {
        return prefs.getFloat(KEY_FONT_SCALE, 1.0f).coerceIn(0.85f, 1.20f)
    }

    fun setFontScale(scale: Float) {
        val clamped = scale.coerceIn(0.85f, 1.20f)
        prefs.edit().putFloat(KEY_FONT_SCALE, clamped).apply()
        _fontScale.value = clamped
    }

    private fun loadSmsAlertPhone(): String {
        return prefs.getString(KEY_SMS_PHONE, "") ?: ""
    }

    fun setSmsAlertPhone(phone: String) {
        val clean = JalaliCalendar.toEnglishDigits(phone).trim()
        prefs.edit().putString(KEY_SMS_PHONE, clean).apply()
        _smsAlertPhone.value = clean
    }

    private fun loadIsSmsAlertEnabled(): Boolean {
        return prefs.getBoolean(KEY_SMS_ENABLED, false)
    }

    fun setSmsAlertEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SMS_ENABLED, enabled).apply()
        _isSmsAlertEnabled.value = enabled
    }

    private fun loadCustomCategories(): List<CustomToolCategory> {
        val raw = prefs.getString(KEY_CUSTOM_CATEGORIES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<CustomToolCategory>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val key = obj.optString("key").takeIf { it.isNotBlank() } ?: continue
                val title = obj.optString("titlePersian").takeIf { it.isNotBlank() } ?: continue
                val iconKey = obj.optString("iconKey", "CATEGORY")
                val colorHex = obj.optLong("colorHex", 0xFF0284C7L)
                val desc = obj.optString("description", "دسته‌بندی سفارشی")
                val isVehicle = obj.optBoolean("isVehicleType", false)
                list.add(
                    CustomToolCategory(
                        key = key,
                        titlePersian = title,
                        iconKey = iconKey,
                        colorHex = colorHex,
                        description = desc,
                        isVehicleType = isVehicle
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCustomCategories(list: List<CustomToolCategory>) {
        val arr = JSONArray()
        list.forEach { cat ->
            val obj = JSONObject()
            obj.put("key", cat.key)
            obj.put("titlePersian", cat.titlePersian)
            obj.put("iconKey", cat.iconKey)
            obj.put("colorHex", cat.colorHex)
            obj.put("description", cat.description)
            obj.put("isVehicleType", cat.isVehicleType)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOM_CATEGORIES, arr.toString()).apply()
        CustomCategoryRegistry.customCategories = list
        _customCategories.value = list
    }

    fun addOrUpdateCustomCategory(category: CustomToolCategory) {
        val current = _customCategories.value.toMutableList()
        val existingIdx = current.indexOfFirst { it.key.equals(category.key, ignoreCase = true) }
        if (existingIdx >= 0) {
            current[existingIdx] = category
        } else {
            current.add(category)
        }
        saveCustomCategories(current)
    }

    fun deleteCustomCategory(categoryKey: String) {
        val updated = _customCategories.value.filterNot { it.key.equals(categoryKey, ignoreCase = true) }
        saveCustomCategories(updated)
    }

    fun setAllCustomCategories(categories: List<CustomToolCategory>) {
        saveCustomCategories(categories)
    }

    companion object {
        private const val PREFS_NAME = "service_yar_user_settings"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_CUSTOM_THEME_COLOR = "key_custom_theme_color"
        private const val KEY_APP_FONT = "key_app_font"
        private const val KEY_FONT_SCALE = "key_font_scale"
        private const val KEY_SMS_PHONE = "key_sms_phone"
        private const val KEY_SMS_ENABLED = "key_sms_enabled"
        private const val KEY_CUSTOM_CATEGORIES = "key_custom_categories"

        @Volatile
        private var INSTANCE: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = AppPreferences(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

