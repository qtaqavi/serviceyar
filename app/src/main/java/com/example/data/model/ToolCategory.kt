package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FireExtinguisher
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hvac
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(
    val titlePersian: String,
    val icon: ImageVector,
    val colorHex: Long,
    val description: String
) {
    VEHICLE(
        titlePersian = "خودرو و وسایل نقلیه",
        icon = Icons.Default.DirectionsCar,
        colorHex = 0xFF0284C7, // Blue
        description = "خودرو سواری، کامیون، کامیونت، تریلی، موتورسیکلت، ون، وانت"
    ),
    HOME_APPLIANCE(
        titlePersian = "لوازم خانگی و آشپزخانه",
        icon = Icons.Default.Home,
        colorHex = 0xFF0D9488, // Teal
        description = "یخچال، ماشین لباسشویی، پکیج، تصفیه آب، جاروبرقی"
    ),
    FACILITY_HVAC(
        titlePersian = "تأسیسات، سرمایش و گرمایش",
        icon = Icons.Default.Hvac,
        colorHex = 0xFFF59E0B, // Amber
        description = "پکیج دیواری، کولر گازی، موتورخانه، آبگرمکن، پمپ آب"
    ),
    WORKSHOP_TOOLS(
        titlePersian = "ابزارآلات و کارگاهی",
        icon = Icons.Default.Build,
        colorHex = 0xFF8B5CF6, // Purple
        description = "دریل، فرز، پمپ باد، ژنراتور، دستگاه جوش، اره برقی"
    ),
    DIGITAL_OFFICE(
        titlePersian = "تجهیزات دیجیتال و اداری",
        icon = Icons.Default.Computer,
        colorHex = 0xFF3B82F6, // Indigo
        description = "لپ‌تاپ، پرینتر، یو پی اس (UPS)، سرور، اسکنر"
    ),
    GARDEN_AGRI(
        titlePersian = "باغ، حیاط و کشاورزی",
        icon = Icons.Default.Grass,
        colorHex = 0xFF10B981, // Emerald green
        description = "علف‌تراش، چمن‌زن، سمپاش، پمپ چاه، اره موتوری"
    ),
    SAFETY_FIRE(
        titlePersian = "ایمنی و آتش‌نشانی",
        icon = Icons.Default.FireExtinguisher,
        colorHex = 0xFFEF4444, // Red
        description = "کپسول آتش‌نشانی، سنسور گاز و دود، سیستم اعلام حریق"
    ),
    OTHER(
        titlePersian = "متفرقه و سایر موارد",
        icon = Icons.Default.Inventory2,
        colorHex = 0xFF64748B, // Slate
        description = "سایر وسایل نیازمند سرویس و نگهداری"
    );

    fun toUiModel(): CategoryUiModel = CategoryUiModel(
        key = name,
        titlePersian = titlePersian,
        icon = icon,
        colorHex = colorHex,
        description = description,
        isCustom = false,
        isVehicleType = (this == VEHICLE)
    )

    companion object {
        val vehicleSubtypes: List<String> = listOf(
            "خودرو سواری",
            "کامیون",
            "کامیونت",
            "تریلی",
            "موتورسیکلت",
            "ون",
            "وانت",
            "اتوبوس",
            "مینی‌بوس",
            "تراکتور"
        )

        private val nonVehicleKeywords: List<String> = listOf(
            "کولر", "اسپلیت", "تصفیه آب", "آب تصفیه", "تصفیه‌آب", "پکیج", "یخچال", "فریزر",
            "لباسشویی", "ظرفشویی", "جاروبرقی", "آبگرمکن", "موتورخانه", "موتور برق", "موتور کولر",
            "موتور پمپ", "پمپ آب", "اره موتوری", "دریل", "فرز", "لپ‌تاپ", "لپ تاپ", "کامپیوتر",
            "پرینتر", "کپسول", "چمن‌زن", "علف‌تراش", "هود", "اجاق", "مایکروویو", "تلویزیون",
            "چیلر", "فن‌کویل", "رادیاتور", "تردمیل", "ماشین اصلاح", "ماشین حساب", "چرخ خیاطی"
        )

        private val strictVehicleKeywords: List<String> = listOf(
            "خودرو", "وسایل نقلیه", "وسیله نقلیه", "سواری", "کامیون", "کامیونت", "تریلی",
            "کشنده", "موتورسیکلت", "موتور سیکلت", "وانت", "اتوبوس", "مینی‌بوس", "مینی بوس",
            "خاور", "ایسوزو", "تراکتور", "لیفتراک", "کمباین", "پژو", "پراید", "سمند", "دنا",
            "تارا", "شاهین", "کوییک", "ساینا", "تیبا", "رانا", "پیکان", "نیسان", "ولوو",
            "اسکانیا", "بنز", "تویوتا", "هیوندای", "کیا", "مزدا", "سراتو", "سانتافه", "توسان",
            "اپتیما", "سوناتا", "تیگو", "آریزو", "هایما", "فیدلیتی", "دیگنیتی", "لاماری", "جک"
        )

        fun normalizePersian(text: String): String {
            return text.trim().lowercase()
                .replace('ي', 'ی')
                .replace('ك', 'ک')
                .replace('\u200C', ' ')
        }

        fun isExplicitlyNonVehicleText(text: String?): Boolean {
            if (text.isNullOrBlank()) return false
            val norm = normalizePersian(text)
            return nonVehicleKeywords.any { kw -> norm.contains(normalizePersian(kw)) }
        }

        fun isVehicleText(text: String?): Boolean {
            if (text.isNullOrBlank()) return false
            if (isExplicitlyNonVehicleText(text)) return false
            val norm = normalizePersian(text)
            if (strictVehicleKeywords.any { kw -> norm.contains(normalizePersian(kw)) }) {
                return true
            }
            val words = norm.split(Regex("[\\s،,()-]+")).filter { it.isNotBlank() }
            if (words.contains("ون") || words.contains("موتور") || words.contains("ماشین")) {
                return true
            }
            return false
        }

        fun fromName(name: String?): ToolCategory {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}

data class CustomToolCategory(
    val key: String,
    val titlePersian: String,
    val iconKey: String = "CATEGORY",
    val colorHex: Long = 0xFF0284C7L,
    val description: String = "دسته‌بندی سفارشی",
    val isVehicleType: Boolean = false
) {
    fun toUiModel(): CategoryUiModel = CategoryUiModel(
        key = key,
        titlePersian = titlePersian,
        icon = CategoryIconCatalog.getIconByKey(iconKey),
        colorHex = colorHex,
        description = description,
        isCustom = true,
        isVehicleType = isVehicleType || ToolCategory.isVehicleText(titlePersian)
    )
}

data class CategoryUiModel(
    val key: String,
    val titlePersian: String,
    val icon: ImageVector,
    val colorHex: Long,
    val description: String = "",
    val isCustom: Boolean = false,
    val isVehicleType: Boolean = false
)

object CategoryIconCatalog {
    val availableIcons: List<Pair<String, ImageVector>> = listOf(
        "CAR" to Icons.Default.DirectionsCar,
        "BIKE" to Icons.Default.TwoWheeler,
        "HOME" to Icons.Default.Home,
        "KITCHEN" to Icons.Default.Kitchen,
        "HVAC" to Icons.Default.Hvac,
        "PLUMBING" to Icons.Default.Plumbing,
        "TOOLS" to Icons.Default.Build,
        "CONSTRUCTION" to Icons.Default.Construction,
        "FACTORY" to Icons.Default.PrecisionManufacturing,
        "ELECTRIC" to Icons.Default.Bolt,
        "DIGITAL" to Icons.Default.Computer,
        "GARDEN" to Icons.Default.Grass,
        "SAFETY" to Icons.Default.FireExtinguisher,
        "SECURITY" to Icons.Default.Security,
        "MEDICAL" to Icons.Default.LocalHospital,
        "SPORT" to Icons.Default.FitnessCenter,
        "ENGINEERING" to Icons.Default.Engineering,
        "CATEGORY" to Icons.Default.Category,
        "BOX" to Icons.Default.Inventory2
    )

    val availableColors: List<Long> = listOf(
        0xFF0284C7L, // Sky Blue
        0xFF0D9488L, // Teal
        0xFF059669L, // Emerald
        0xFF16A34AL, // Green
        0xFFD97706L, // Amber
        0xFFEA580CL, // Orange
        0xFFDC2626L, // Red
        0xFFDB2777L, // Pink
        0xFF9333EAL, // Purple
        0xFF6366F1L, // Indigo
        0xFF475569L, // Slate
        0xFF78350FL  // Brown
    )

    fun getIconByKey(key: String?): ImageVector {
        if (key.isNullOrBlank()) return Icons.Default.Category
        return availableIcons.firstOrNull { it.first.equals(key, ignoreCase = true) }?.second
            ?: Icons.Default.Category
    }
}

object CustomCategoryRegistry {
    @Volatile
    var customCategories: List<CustomToolCategory> = emptyList()

    fun getAllCategoryModels(customList: List<CustomToolCategory> = customCategories): List<CategoryUiModel> {
        val builtIn = ToolCategory.entries.map { it.toUiModel() }
        val custom = customList.map { it.toUiModel() }
        return builtIn + custom
    }

    fun resolve(categoryKeyOrName: String?, customList: List<CustomToolCategory> = customCategories): CategoryUiModel {
        if (categoryKeyOrName.isNullOrBlank()) return ToolCategory.OTHER.toUiModel()
        // 1. Check built-in enum
        val builtIn = ToolCategory.entries.firstOrNull { it.name.equals(categoryKeyOrName, ignoreCase = true) }
        if (builtIn != null) return builtIn.toUiModel()

        // 2. Check custom categories by key or title
        val custom = customList.firstOrNull {
            it.key.equals(categoryKeyOrName, ignoreCase = true) ||
                it.titlePersian.equals(categoryKeyOrName.trim(), ignoreCase = true)
        }
        if (custom != null) return custom.toUiModel()

        // 3. Fallback if a custom category name was saved directly
        return CategoryUiModel(
            key = categoryKeyOrName,
            titlePersian = categoryKeyOrName,
            icon = Icons.Default.Category,
            colorHex = 0xFF0284C7L,
            description = "دسته‌بندی سفارشی",
            isCustom = true,
            isVehicleType = ToolCategory.isVehicleText(categoryKeyOrName)
        )
    }
}

