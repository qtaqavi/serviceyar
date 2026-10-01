package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat

enum class ThemeMode(val titlePersian: String) {
    LIGHT("روشن"),
    DARK("تاریک"),
    SYSTEM("خودکار (هماهنگ با سیستم)")
}

data class ThemeColorPreset(
    val namePersian: String,
    val colorHex: Long
)

val ThemeColorPresets: List<ThemeColorPreset> = listOf(
    ThemeColorPreset("آبی سلطنتی (پیش‌فرض)", 0xFF005AC1L),
    ThemeColorPreset("فیروزه‌ای اقیانوسی", 0xFF00796BL),
    ThemeColorPreset("سبز زمردی", 0xFF059669L),
    ThemeColorPreset("بنفش اصیل", 0xFF6750A4L),
    ThemeColorPreset("نیلی مدرن", 0xFF3949ABL),
    ThemeColorPreset("نارنجی کهربایی", 0xFFD97706L),
    ThemeColorPreset("زرشکی یاقوتی", 0xFFBE123CL),
    ThemeColorPreset("صورتی مرجانی", 0xFFDB2777L),
    ThemeColorPreset("آبی آسمانی", 0xFF0284C7L),
    ThemeColorPreset("سبز جنگلی", 0xFF15803DL),
    ThemeColorPreset("قهوه‌ای کلاسیک", 0xFF78350FL),
    ThemeColorPreset("سرمه‌ای صنعتی", 0xFF334155L)
)

fun buildCustomColorScheme(seedColorHex: Long, darkTheme: Boolean): ColorScheme {
    val baseArgb = (seedColorHex or 0xFF000000L).toInt()
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(baseArgb, hsl)
    val hue = hsl[0]
    val sat = hsl[1].coerceIn(0.25f, 0.95f)

    fun hslColor(h: Float, s: Float, l: Float): Color {
        val normalizedH = ((h % 360f) + 360f) % 360f
        val argb = ColorUtils.HSLToColor(
            floatArrayOf(
                normalizedH,
                s.coerceIn(0f, 1f),
                l.coerceIn(0f, 1f)
            )
        )
        return Color(argb)
    }

    return if (!darkTheme) {
        val primaryLightness = hsl[2].coerceIn(0.26f, 0.46f)
        val primary = hslColor(hue, sat, primaryLightness)
        val primaryContainer = hslColor(hue, (sat * 0.75f).coerceIn(0.30f, 0.85f), 0.91f)
        val onPrimaryContainer = hslColor(hue, sat.coerceAtLeast(0.45f), 0.14f)

        val secHue = (hue + 24f) % 360f
        val secondary = hslColor(secHue, (sat * 0.65f).coerceIn(0.25f, 0.70f), 0.35f)
        val secondaryContainer = hslColor(secHue, (sat * 0.60f).coerceIn(0.25f, 0.75f), 0.90f)
        val onSecondaryContainer = hslColor(secHue, sat.coerceAtLeast(0.4f), 0.15f)

        val tertHue = (hue + 55f) % 360f
        val tertiary = hslColor(tertHue, (sat * 0.70f).coerceIn(0.30f, 0.80f), 0.38f)
        val tertiaryContainer = hslColor(tertHue, (sat * 0.60f).coerceIn(0.25f, 0.75f), 0.92f)
        val onTertiaryContainer = hslColor(tertHue, sat.coerceAtLeast(0.4f), 0.16f)

        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = Color.White,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = SleekBackgroundLight,
            onBackground = SleekTextPrimaryLight,
            surface = SleekSurfaceLight,
            onSurface = SleekTextPrimaryLight,
            surfaceVariant = SleekSurfaceVariantLight,
            onSurfaceVariant = SleekTextSecondaryLight,
            outline = SleekOutlineLight,
            outlineVariant = SleekOutlineVariantLight,
            error = StatusOverdueRed,
            errorContainer = StatusOverdueRedContainer,
            onError = Color.White
        )
    } else {
        val primary = hslColor(hue, (sat * 0.85f).coerceIn(0.45f, 0.95f), 0.75f)
        val onPrimary = hslColor(hue, sat.coerceAtLeast(0.5f), 0.16f)
        val primaryContainer = hslColor(hue, (sat * 0.75f).coerceIn(0.35f, 0.85f), 0.27f)
        val onPrimaryContainer = hslColor(hue, (sat * 0.90f).coerceIn(0.50f, 1f), 0.90f)

        val secHue = (hue + 24f) % 360f
        val secondary = hslColor(secHue, (sat * 0.65f).coerceIn(0.35f, 0.80f), 0.74f)
        val onSecondary = hslColor(secHue, sat, 0.16f)
        val secondaryContainer = hslColor(secHue, (sat * 0.60f).coerceIn(0.30f, 0.75f), 0.26f)
        val onSecondaryContainer = hslColor(secHue, (sat * 0.80f).coerceIn(0.45f, 0.95f), 0.90f)

        val tertHue = (hue + 55f) % 360f
        val tertiary = hslColor(tertHue, (sat * 0.70f).coerceIn(0.40f, 0.85f), 0.76f)
        val onTertiary = hslColor(tertHue, sat, 0.18f)
        val tertiaryContainer = hslColor(tertHue, (sat * 0.65f).coerceIn(0.35f, 0.80f), 0.28f)
        val onTertiaryContainer = hslColor(tertHue, (sat * 0.85f).coerceIn(0.45f, 0.95f), 0.91f)

        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = SleekBackgroundDark,
            onBackground = SleekTextPrimaryDark,
            surface = SleekSurfaceDark,
            onSurface = SleekTextPrimaryDark,
            surfaceVariant = SleekSurfaceVariantDark,
            onSurfaceVariant = SleekTextSecondaryDark,
            outline = SleekOutlineDark,
            outlineVariant = SleekOutlineVariantDark,
            error = StatusOverdueRed,
            errorContainer = Color(0xFF93000A),
            onError = Color.White
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    appFont: AppFont = AppFont.VAZIR,
    fontScale: Float = 1.0f,
    customColorHex: Long = 0xFF005AC1L,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> buildCustomColorScheme(customColorHex, darkTheme)
    }

    val clampedScale = fontScale.coerceIn(0.85f, 1.20f)
    val typography = createAppTypography(appFont, clampedScale)
    val activeFontFamily = appFont.fontFamily
    val baseDensity = androidx.compose.ui.platform.LocalDensity.current
    val normalizedDensity = androidx.compose.ui.unit.Density(
        density = baseDensity.density,
        fontScale = 1.0f
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalDensity provides normalizedDensity,
        LocalAppFontFamily provides activeFontFamily,
        LocalAppFontScale provides clampedScale
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography
        ) {
            androidx.compose.material3.ProvideTextStyle(
                value = typography.bodyMedium,
                content = content
            )
        }
    }
}


