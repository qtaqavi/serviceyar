package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarRental
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FireExtinguisher
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hvac
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.Color
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
        description = "خودرو سواری، موتورسیکلت، وانت، دوچرخه"
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

    companion object {
        fun fromName(name: String?): ToolCategory {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}
