package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.ColorUtils
import com.example.ui.components.AutoResizedButtonText
import com.example.ui.theme.AppFont
import com.example.ui.theme.ThemeColorPresets
import com.example.ui.theme.ThemeMode
import com.example.util.JalaliCalendar
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    currentThemeMode: ThemeMode,
    currentCustomColorHex: Long = 0xFF005AC1L,
    currentAppFont: AppFont,
    currentFontScale: Float = 1.0f,
    smsPhone: String,
    isSmsEnabled: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onCustomColorChange: (Long) -> Unit = {},
    onAppFontChange: (AppFont) -> Unit,
    onFontScaleChange: (Float) -> Unit = {},
    onSmsPhoneChange: (String) -> Unit,
    onSmsEnabledChange: (Boolean) -> Unit,
    onSendTestSms: () -> Boolean,
    onResetSampleData: () -> Unit,
    onOpenCategoryManagement: () -> Unit = {},
    onOpenBackupRestore: () -> Unit = {},
    onOpenReports: () -> Unit = {},
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var phoneInput by remember { mutableStateOf(smsPhone) }
    var showResetConfirm by remember { mutableStateOf(false) }

    val clampedScale = currentFontScale.coerceIn(0.85f, 1.20f)
    val scalePercent = (clampedScale * 100).roundToInt()
    val scaleLabel = when {
        scalePercent < 93 -> "کوچک"
        scalePercent in 93..105 -> "معمولی (پیش‌فرض)"
        scalePercent in 106..114 -> "بزرگ"
        else -> "خیلی بزرگ"
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            shape = RoundedCornerShape(24.dp),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(0.96f),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "تنظیمات برنامه",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Theme Mode (Light / Dark / System)
                    Text(
                        text = "پوسته و حالت نمایش:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeModeOption(
                            title = "روشن",
                            icon = Icons.Default.LightMode,
                            isSelected = currentThemeMode == ThemeMode.LIGHT,
                            onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeModeOption(
                            title = "تاریک",
                            icon = Icons.Default.DarkMode,
                            isSelected = currentThemeMode == ThemeMode.DARK,
                            onClick = { onThemeModeChange(ThemeMode.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeModeOption(
                            title = "سیستم",
                            icon = Icons.Default.BrightnessAuto,
                            isSelected = currentThemeMode == ThemeMode.SYSTEM,
                            onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 1.5 Custom Theme Color Selection (انتخاب رنگ سفارشی برای تم برنامه)
                    var showCustomColorSliders by remember { mutableStateOf(false) }
                    val initialHsl = remember(currentCustomColorHex) {
                        val out = FloatArray(3)
                        ColorUtils.colorToHSL((currentCustomColorHex or 0xFF000000L).toInt(), out)
                        out
                    }
                    var customHue by remember(currentCustomColorHex) { mutableFloatStateOf(initialHsl[0]) }
                    var customSat by remember(currentCustomColorHex) { mutableFloatStateOf(initialHsl[1].coerceIn(0.3f, 1f)) }
                    var customLight by remember(currentCustomColorHex) { mutableFloatStateOf(initialHsl[2].coerceIn(0.25f, 0.55f)) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "رنگ سفارشی تم برنامه:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }

                        val activePresetName = ThemeColorPresets.firstOrNull {
                            (it.colorHex and 0xFFFFFFL) == (currentCustomColorHex and 0xFFFFFFL)
                        }?.namePersian ?: "رنگ دلخواه سفارشی"

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(currentCustomColorHex or 0xFF000000L))
                                )
                                Text(
                                    text = activePresetName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "انتخاب سریع از پالت رنگ‌های استاندارد یا ساخت رنگ دلخواه:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ThemeColorPresets.forEach { preset ->
                                    val isSelected = (preset.colorHex and 0xFFFFFFL) == (currentCustomColorHex and 0xFFFFFFL)
                                    val swatchColor = Color(preset.colorHex or 0xFF000000L)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) swatchColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) swatchColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                        ),
                                        modifier = Modifier.clickable {
                                            onCustomColorChange(preset.colorHex)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(swatchColor)
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = preset.namePersian,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = { showCustomColorSliders = !showCustomColorSliders },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ColorLens,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                AutoResizedButtonText(
                                    text = if (showCustomColorSliders) "بستن ترکیب‌کننده رنگ دلخواه" else "انتخاب رنگ کاملاً سفارشی از طیف رنگی",
                                    maxFontSize = 12.sp,
                                    minFontSize = 8.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }

                            if (showCustomColorSliders) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val liveArgb = ColorUtils.HSLToColor(
                                            floatArrayOf(
                                                customHue.coerceIn(0f, 360f),
                                                customSat.coerceIn(0.25f, 1f),
                                                customLight.coerceIn(0.20f, 0.60f)
                                            )
                                        )
                                        val liveHexLong = (liveArgb.toLong() and 0xFFFFFFFFL) or 0xFF000000L
                                        val hexString = String.format(Locale.US, "#%06X", (liveArgb and 0xFFFFFF))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "تنظیم دقیق طیف رنگ تم:",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = hexString,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(liveArgb))
                                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                                )
                                            }
                                        }

                                        // Rainbow Hue bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(14.dp)
                                                .clip(RoundedCornerShape(7.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = listOf(
                                                            Color(0xFFFF0000),
                                                            Color(0xFFFFFF00),
                                                            Color(0xFF00FF00),
                                                            Color(0xFF00FFFF),
                                                            Color(0xFF0000FF),
                                                            Color(0xFFFF00FF),
                                                            Color(0xFFFF0000)
                                                        )
                                                    )
                                                )
                                        )

                                        Slider(
                                            value = customHue,
                                            onValueChange = {
                                                customHue = it
                                                val updated = ColorUtils.HSLToColor(
                                                    floatArrayOf(customHue, customSat, customLight)
                                                )
                                                onCustomColorChange((updated.toLong() and 0xFFFFFFFFL) or 0xFF000000L)
                                            },
                                            valueRange = 0f..360f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(liveArgb),
                                                activeTrackColor = Color(liveArgb)
                                            )
                                        )

                                        Text(
                                            text = "غلظت و روشنایی رنگ:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Slider(
                                            value = customLight,
                                            onValueChange = {
                                                customLight = it
                                                val updated = ColorUtils.HSLToColor(
                                                    floatArrayOf(customHue, customSat, customLight)
                                                )
                                                onCustomColorChange((updated.toLong() and 0xFFFFFFFFL) or 0xFF000000L)
                                            },
                                            valueRange = 0.22f..0.55f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(liveArgb),
                                                activeTrackColor = Color(liveArgb)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 2. Font Size Scaling (بزرگ و کوچک کردن اندازه فونت نوشته‌ها)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "اندازه فونت نوشته‌ها:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${JalaliCalendar.toPersianDigits(scalePercent)}٪ ($scaleLabel)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // - / Slider / + Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable(enabled = clampedScale > 0.85f) {
                                            val next = (clampedScale - 0.05f).coerceIn(0.85f, 1.20f)
                                            onFontScaleChange(next)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "کوچک کردن فونت",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        AutoResizedButtonText(
                                            text = "کوچک‌تر",
                                            color = MaterialTheme.colorScheme.primary,
                                            maxFontSize = 11.sp,
                                            minFontSize = 8.sp
                                        )
                                    }
                                }

                                Slider(
                                    value = clampedScale,
                                    onValueChange = { onFontScaleChange(it.coerceIn(0.85f, 1.20f)) },
                                    valueRange = 0.85f..1.20f,
                                    steps = 6,
                                    modifier = Modifier.weight(1f)
                                )

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable(enabled = clampedScale < 1.20f) {
                                            val next = (clampedScale + 0.05f).coerceIn(0.85f, 1.20f)
                                            onFontScaleChange(next)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "بزرگ کردن فونت",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        AutoResizedButtonText(
                                            text = "بزرگ‌تر",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            maxFontSize = 11.sp,
                                            minFontSize = 8.sp
                                        )
                                    }
                                }
                            }

                            // Preset size buttons (compact equal-width pills that never overflow)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val presets = listOf(
                                    0.85f to "کوچک",
                                    1.00f to "معمولی",
                                    1.10f to "بزرگ",
                                    1.20f to "خیلی بزرگ"
                                )
                                presets.forEach { (presetScale, label) ->
                                    val isSelected = kotlin.math.abs(clampedScale - presetScale) < 0.03f
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onFontScaleChange(presetScale) }
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 7.dp)
                                        ) {
                                            AutoResizedButtonText(
                                                text = label,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                maxFontSize = 11.sp,
                                                minFontSize = 8.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Live Preview Box
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "پیش‌نمایش اندازه نوشته: تعویض روغن موتور خودرو",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "این متن نمونه‌ای از اندازه فونت انتخابی شما در کارت‌ها و برنامه‌های سرویس است.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 3. Persian Font Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "انتخاب فونت فارسی نوشته‌های برنامه:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = currentAppFont.titlePersian.substringBefore(" ("),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Live Font Preview Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "پیش‌نمایش زنده فونت فعال (${currentAppFont.titlePersian}):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "مدیریت سرویس و نگهداری هوشمند تجهیزات (۱۲۳۴۵۶۷۸۹۰)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تمام عنوان‌ها، توضیحات و دکمه‌های برنامه با فونت انتخابی شما نمایش داده می‌شوند.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AppFont.entries.forEach { font ->
                            val isSelected = currentAppFont == font
                            val previewScale = (clampedScale * font.intrinsicScale).coerceIn(0.85f, 1.08f)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAppFontChange(font) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = font.titlePersian,
                                            style = TextStyle(
                                                fontFamily = font.fontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = (14f * previewScale).sp
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${font.subtitlePersian} • نمونه: سرویس دوره‌ای ۱۴۰۴",
                                            style = TextStyle(
                                                fontFamily = font.fontFamily,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = (11.5f * previewScale).sp
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 4. SMS Alerts Settings
                    Text(
                        text = "تنظیمات هشدارهای پیامکی (SMS):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "فعال‌سازی هشدار پیامکی",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "ارسال پیامک یادآوری موعد به شماره ثبت‌شده",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isSmsEnabled,
                                    onCheckedChange = { onSmsEnabledChange(it) }
                                )
                            }

                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = {
                                    phoneInput = it
                                    onSmsPhoneChange(it)
                                },
                                label = { Text("شماره موبایل دریافت هشدار") },
                                placeholder = { Text("مثال: 09121234567") },
                                leadingIcon = {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (phoneInput.isBlank()) {
                                        Toast.makeText(context, "لطفاً ابتدا شماره موبایل را وارد نمایید", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val sent = onSendTestSms()
                                        if (sent) {
                                            Toast.makeText(context, "پیامک آزمایشی ارسال یا برنامه پیام‌رسان باز شد", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                AutoResizedButtonText(
                                    text = "ارسال پیامک آزمایشی برای تست",
                                    maxFontSize = 12.5.sp,
                                    minFontSize = 8.5.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 5. Data Management (Categories, Backup/Restore, Reports, Reset Sample Data)
                    Text(
                        text = "دسته‌بندی‌ها، گزارش‌گیری و پشتیبان‌گیری:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedButton(
                        onClick = {
                            onDismissRequest()
                            onOpenCategoryManagement()
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        AutoResizedButtonText(
                            text = "مدیریت دسته‌بندی دستگاه‌ها و وسایل",
                            maxFontSize = 12.5.sp,
                            minFontSize = 8.5.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    Button(
                        onClick = {
                            onDismissRequest()
                            onOpenBackupRestore()
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        AutoResizedButtonText(
                            text = "پشتیبان‌گیری و بازیابی (درایو لوکال / گوگل درایو)",
                            maxFontSize = 12.5.sp,
                            minFontSize = 8.5.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onDismissRequest()
                            onOpenReports()
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        AutoResizedButtonText(
                            text = "دریافت گزارش جامع سرویس‌ها (PDF / اکسل)",
                            maxFontSize = 12.5.sp,
                            minFontSize = 8.5.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        AutoResizedButtonText(
                            text = "بازنشانی اطلاعات به حالت نمونه اولیه",
                            maxFontSize = 12.5.sp,
                            minFontSize = 8.5.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissRequest,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AutoResizedButtonText("ذخیره و بازگشت", maxFontSize = 13.sp, minFontSize = 9.sp)
                }
            }
        )

        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text("بازنشانی اطلاعات") },
                text = { Text("آیا مطمئن هستید؟ تمام دستگاه‌ها و سرویس‌ها با داده‌های نمونه جایگزین خواهند شد.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetConfirm = false
                            onResetSampleData()
                            Toast.makeText(context, "اطلاعات با موفقیت بازنشانی شد", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        AutoResizedButtonText("بله، بازنشانی شود", maxFontSize = 12.5.sp)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showResetConfirm = false },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        AutoResizedButtonText("انصراف", maxFontSize = 12.5.sp)
                    }
                }
            )
        }
    }
}

@Composable
private fun ThemeModeOption(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
