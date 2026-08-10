package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.ui.graphics.vector.ImageVector

enum class ServiceType(
    val titlePersian: String,
    val icon: ImageVector,
    val defaultIntervalMonths: Int,
    val badgeColorHex: Long
) {
    REPLACEMENT(
        titlePersian = "تعویض قطعه و مصرفی",
        icon = Icons.Default.Autorenew,
        defaultIntervalMonths = 6,
        badgeColorHex = 0xFF0284C7 // Blue
    ),
    LUBRICATION_OIL(
        titlePersian = "روغن‌کاری و روانکاری",
        icon = Icons.Default.OilBarrel,
        defaultIntervalMonths = 3,
        badgeColorHex = 0xFFD97706 // Amber
    ),
    INSPECTION_CHECK(
        titlePersian = "بازرسی فنی و تست ایمنی",
        icon = Icons.Default.HealthAndSafety,
        defaultIntervalMonths = 12,
        badgeColorHex = 0xFF059669 // Green
    ),
    CLEANING_DESCALING(
        titlePersian = "تمیزکاری و رسوب‌زدایی",
        icon = Icons.Default.CleaningServices,
        defaultIntervalMonths = 6,
        badgeColorHex = 0xFF0D9488 // Teal
    ),
    WARRANTY_INSURANCE(
        titlePersian = "تمدید گارانتی، بیمه یا مجوز",
        icon = Icons.Default.VerifiedUser,
        defaultIntervalMonths = 12,
        badgeColorHex = 0xFF7C3AED // Purple
    ),
    CALIBRATION_TUNE(
        titlePersian = "کالیبراسیون و تنظیم دقیق",
        icon = Icons.Default.Tune,
        defaultIntervalMonths = 6,
        badgeColorHex = 0xFFEA580C // Orange
    ),
    PERIODIC_GENERAL(
        titlePersian = "سرویس جامع دوره‌ای",
        icon = Icons.Default.Engineering,
        defaultIntervalMonths = 12,
        badgeColorHex = 0xFF2563EB // Royal Blue
    );

    companion object {
        fun fromName(name: String?): ServiceType {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: PERIODIC_GENERAL
        }
    }
}

enum class IntervalType(
    val titlePersian: String,
    val approximateMonths: Int,
    val approximateDays: Int
) {
    MONTHLY(
        titlePersian = "ماهانه (هر ۱ ماه)",
        approximateMonths = 1,
        approximateDays = 30
    ),
    QUARTERLY(
        titlePersian = "فصلی (هر ۳ ماه)",
        approximateMonths = 3,
        approximateDays = 90
    ),
    BIANNUAL(
        titlePersian = "نیم‌سال (هر ۶ ماه)",
        approximateMonths = 6,
        approximateDays = 180
    ),
    ANNUAL(
        titlePersian = "سالانه (هر ۱ سال)",
        approximateMonths = 12,
        approximateDays = 365
    ),
    BIENNIAL(
        titlePersian = "دو سال یک‌بار (هر ۲ سال)",
        approximateMonths = 24,
        approximateDays = 730
    ),
    CUSTOM_DAYS(
        titlePersian = "سفارشی بر حسب روز یا کارکرد",
        approximateMonths = 0,
        approximateDays = 30
    );

    companion object {
        fun fromName(name: String?): IntervalType {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: ANNUAL
        }
    }
}

enum class ServicePriority(
    val titlePersian: String,
    val colorHex: Long
) {
    HIGH(
        titlePersian = "فوری و ضروری (حیاتی)",
        colorHex = 0xFFEF4444 // Red
    ),
    MEDIUM(
        titlePersian = "متوسط (معمولی)",
        colorHex = 0xFFF59E0B // Amber
    ),
    LOW(
        titlePersian = "پایین (اختیاری / عمومی)",
        colorHex = 0xFF10B981 // Emerald Green
    );

    companion object {
        fun fromName(name: String?): ServicePriority {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: MEDIUM
        }
    }
}

enum class ServiceStatus(
    val titlePersian: String,
    val colorHex: Long,
    val iconDesc: String
) {
    OVERDUE(
        titlePersian = "نیازمند اقدام فوری (گذشته از موعد)",
        colorHex = 0xFFEF4444, // Red
        iconDesc = "منقضی یا معوقه"
    ),
    DUE_SOON(
        titlePersian = "نزدیک به موعد سرویس",
        colorHex = 0xFFF59E0B, // Amber
        iconDesc = "تا ۳۰ روز آینده"
    ),
    UP_TO_DATE(
        titlePersian = "سرویس شده و معتبر",
        colorHex = 0xFF10B981, // Emerald Green
        iconDesc = "وضعیت عالی"
    ),
    EXPIRED_WARRANTY(
        titlePersian = "اتمام گارانتی یا انقضا",
        colorHex = 0xFF9333EA, // Purple
        iconDesc = "پایان مهلت"
    ),
    NO_SCHEDULE(
        titlePersian = "بدون برنامه فعال",
        colorHex = 0xFF64748B, // Slate
        iconDesc = "تنظیم نشده"
    )
}
