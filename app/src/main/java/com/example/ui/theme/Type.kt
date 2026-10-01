package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

@OptIn(ExperimentalTextApi::class)
private fun buildCompleteFontFamily(
    regularResId: Int,
    boldResId: Int = regularResId,
    isVariableFont: Boolean = false
): FontFamily {
    val weights = listOf(
        FontWeight.Thin,
        FontWeight.ExtraLight,
        FontWeight.Light,
        FontWeight.Normal,
        FontWeight.Medium,
        FontWeight.SemiBold,
        FontWeight.Bold,
        FontWeight.ExtraBold,
        FontWeight.Black
    )
    return FontFamily(
        weights.map { weight ->
            val resId = if (weight.weight >= FontWeight.Bold.weight) boldResId else regularResId
            if (isVariableFont) {
                Font(
                    resId = resId,
                    weight = weight,
                    variationSettings = FontVariation.Settings(
                        FontVariation.weight(weight.weight.coerceIn(100, 900))
                    )
                )
            } else {
                Font(
                    resId = resId,
                    weight = weight
                )
            }
        }
    )
}

// Custom Persian & Arabic Font Families bundled in res/font with full weight coverage
val VazirmatnFontFamily = buildCompleteFontFamily(
    regularResId = R.font.vazirmatn,
    boldResId = R.font.vazirmatn_bold,
    isVariableFont = true
)

val LalezarFontFamily = buildCompleteFontFamily(
    regularResId = R.font.lalezar,
    boldResId = R.font.lalezar,
    isVariableFont = false
)

val ChangaFontFamily = buildCompleteFontFamily(
    regularResId = R.font.changa,
    boldResId = R.font.changa,
    isVariableFont = true
)

val ElMessiriFontFamily = buildCompleteFontFamily(
    regularResId = R.font.el_messiri,
    boldResId = R.font.el_messiri,
    isVariableFont = true
)

val KatibehFontFamily = buildCompleteFontFamily(
    regularResId = R.font.katibeh,
    boldResId = R.font.katibeh,
    isVariableFont = false
)

val AmiriFontFamily = buildCompleteFontFamily(
    regularResId = R.font.amiri,
    boldResId = R.font.amiri,
    isVariableFont = false
)

val MarkaziTextFontFamily = buildCompleteFontFamily(
    regularResId = R.font.markazi_text,
    boldResId = R.font.markazi_text_bold,
    isVariableFont = true
)

val NotoSansArabicFontFamily = buildCompleteFontFamily(
    regularResId = R.font.noto_sans_arabic,
    boldResId = R.font.noto_sans_arabic_bold,
    isVariableFont = true
)

val LocalAppFontFamily = compositionLocalOf<FontFamily> { VazirmatnFontFamily }
val LocalAppFontScale = compositionLocalOf { 1.0f }

enum class AppFont(
    val titlePersian: String,
    val subtitlePersian: String,
    val fontFamily: FontFamily,
    val intrinsicScale: Float = 1.0f
) {
    VAZIR(
        titlePersian = "وزیرمتن استاندارد (Vazirmatn)",
        subtitlePersian = "فونت رسمی، مدرن و بسیار خوانا برای رابط کاربری فارسی",
        fontFamily = VazirmatnFontFamily,
        intrinsicScale = 1.0f
    ),
    KOUDAK(
        titlePersian = "لاله‌زار تیتری و ضخیم (Lalezar)",
        subtitlePersian = "فونت فانتزی، پررنگ و درشت با جلوه گرافیکی متمایز",
        fontFamily = LalezarFontFamily,
        intrinsicScale = 0.90f
    ),
    YEKAN(
        titlePersian = "چنگا مدرن و هندسی (Changa)",
        subtitlePersian = "فونت چهارگوش، صنعتی و منظم با حروف کشیده",
        fontFamily = ChangaFontFamily,
        intrinsicScale = 0.93f
    ),
    EL_MESSIRI(
        titlePersian = "المسیری خوش‌نگار (El Messiri)",
        subtitlePersian = "طراحی منحنی، هنری و چشم‌نواز برای متون فارسی",
        fontFamily = ElMessiriFontFamily,
        intrinsicScale = 0.94f
    ),
    KATIBEH(
        titlePersian = "کتیبه دست‌نویس و سنتی (Katibeh)",
        subtitlePersian = "سبک خوشنویسی و کلاسیک ایرانی با قوس‌های اصیل",
        fontFamily = KatibehFontFamily,
        intrinsicScale = 0.94f
    ),
    AMIRI(
        titlePersian = "امیری نسخ چاپی (Amiri)",
        subtitlePersian = "فونت اصیل کتابی و مطبوعاتی با سبک نسخ کلاسیک",
        fontFamily = AmiriFontFamily,
        intrinsicScale = 0.95f
    ),
    TAHOMA(
        titlePersian = "مرکزی کلاسیک (Markazi)",
        subtitlePersian = "سبک رسمی و اداری با خوانایی بالا در متون طولانی",
        fontFamily = MarkaziTextFontFamily,
        intrinsicScale = 0.98f
    ),
    SYSTEM(
        titlePersian = "پیش‌فرض سیستم (Noto Sans)",
        subtitlePersian = "فونت استاندارد و پایه سیستم‌عامل اندروید",
        fontFamily = FontFamily.Default,
        intrinsicScale = 0.96f
    );

    companion object {
        fun fromName(name: String?): AppFont {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: VAZIR
        }
    }
}

fun createTypographyForFamily(
    family: FontFamily,
    fontScale: Float = 1.0f,
    intrinsicScale: Float = 1.0f
): Typography {
    val clampedUserScale = fontScale.coerceIn(0.85f, 1.20f)
    // Content/body text scales with the user setting, adjusted for the font's intrinsic glyph width
    val contentScale = (clampedUserScale * intrinsicScale).coerceIn(0.80f, 1.18f)
    // Buttons, chips, badges, and compact headers scale more gently so they never overflow mobile screen width
    val uiControlScale = ((1f + (clampedUserScale - 1f) * 0.40f) * intrinsicScale).coerceIn(0.84f, 1.06f)

    return Typography(
        displayLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (28f * uiControlScale).sp,
            lineHeight = (36f * uiControlScale).sp,
            letterSpacing = 0.sp
        ),
        displayMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (24f * uiControlScale).sp,
            lineHeight = (32f * uiControlScale).sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (21f * uiControlScale).sp,
            lineHeight = (28f * uiControlScale).sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (20f * uiControlScale).sp,
            lineHeight = (28f * uiControlScale).sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (18f * uiControlScale).sp,
            lineHeight = (25f * uiControlScale).sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16.5f * uiControlScale).sp,
            lineHeight = (23f * uiControlScale).sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (16.5f * contentScale).sp,
            lineHeight = (24f * contentScale).sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (14.5f * contentScale).sp,
            lineHeight = (21f * contentScale).sp,
            letterSpacing = 0.sp
        ),
        titleSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = (13f * contentScale).sp,
            lineHeight = (19f * contentScale).sp,
            letterSpacing = 0.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = (14.5f * contentScale).sp,
            lineHeight = (21f * contentScale).sp,
            letterSpacing = 0.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = (13f * contentScale).sp,
            lineHeight = (19f * contentScale).sp,
            letterSpacing = 0.sp
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = (11.5f * contentScale).sp,
            lineHeight = (17f * contentScale).sp,
            letterSpacing = 0.sp
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = (12.5f * uiControlScale).sp,
            lineHeight = (17f * uiControlScale).sp,
            letterSpacing = 0.sp
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = (11f * uiControlScale).sp,
            lineHeight = (15f * uiControlScale).sp,
            letterSpacing = 0.sp
        ),
        labelSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = (10f * uiControlScale).sp,
            lineHeight = (14f * uiControlScale).sp,
            letterSpacing = 0.sp
        )
    )
}

fun createAppTypography(appFont: AppFont? = null, fontScale: Float = 1.0f): Typography {
    val family = appFont?.fontFamily ?: VazirmatnFontFamily
    val intrinsic = appFont?.intrinsicScale ?: 1.0f
    return createTypographyForFamily(family, fontScale, intrinsic)
}

val Typography = createTypographyForFamily(VazirmatnFontFamily, 1.0f)


