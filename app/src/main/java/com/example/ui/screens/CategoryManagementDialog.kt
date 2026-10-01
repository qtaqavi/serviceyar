package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CategoryIconCatalog
import com.example.data.model.CustomCategoryRegistry
import com.example.data.model.CustomToolCategory
import com.example.data.model.ToolEntity
import com.example.ui.components.AutoResizedButtonText
import com.example.ui.components.CategoryChip
import com.example.util.JalaliCalendar
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryManagementDialog(
    customCategories: List<CustomToolCategory>,
    allTools: List<ToolEntity>,
    onSaveCustomCategory: (CustomToolCategory) -> Unit,
    onDeleteCustomCategory: (String) -> Unit,
    onAssignToolCategory: (ToolEntity, String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val allCategories = remember(customCategories) {
        CustomCategoryRegistry.getAllCategoryModels(customCategories)
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: دسته‌بندی‌ها و افزودن, 1: دسته‌بندی سریع وسایل
    var editingKey by remember { mutableStateOf<String?>(null) }
    var titlePersian by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedIconKey by remember { mutableStateOf("CATEGORY") }
    var selectedColorHex by remember { mutableLongStateOf(CategoryIconCatalog.availableColors.first()) }
    var isVehicleType by remember { mutableStateOf(false) }
    var showForm by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    var toolForCategoryChange by remember { mutableStateOf<ToolEntity?>(null) }

    fun resetForm() {
        editingKey = null
        titlePersian = ""
        description = ""
        selectedIconKey = "CATEGORY"
        selectedColorHex = CategoryIconCatalog.availableColors.first()
        isVehicleType = false
        titleError = false
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            shape = RoundedCornerShape(24.dp),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(0.95f),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "مدیریت دسته‌بندی دستگاه‌ها و وسایل",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${JalaliCalendar.toPersianDigits(allCategories.size)} دسته‌بندی فعال (${JalaliCalendar.toPersianDigits(customCategories.size)} سفارشی)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tab Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = {
                                AutoResizedButtonText(
                                    text = "لیست و ایجاد دسته‌بندی",
                                    maxFontSize = 12.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FolderSpecial,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = {
                                AutoResizedButtonText(
                                    text = "دسته‌بندی وسایل (${JalaliCalendar.toPersianDigits(allTools.size)})",
                                    maxFontSize = 12.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (selectedTab == 0) {
                        if (!showForm) {
                            Button(
                                onClick = {
                                    resetForm()
                                    showForm = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                AutoResizedButtonText("افزودن دسته‌بندی جدید", maxFontSize = 13.sp)
                            }
                        } else {
                            // Add / Edit Custom Category Card
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = if (editingKey == null) "تعریف دسته‌بندی سفارشی جدید" else "ویرایش دسته‌بندی سفارشی",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    OutlinedTextField(
                                        value = titlePersian,
                                        onValueChange = {
                                            titlePersian = it
                                            if (it.isNotBlank()) titleError = false
                                        },
                                        label = { Text("نام دسته‌بندی *") },
                                        placeholder = { Text("مثال: تجهیزات پزشکی، ماشین‌آلات، صوتی و تصویری") },
                                        isError = titleError,
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    if (titleError) {
                                        Text(
                                            text = "لطفاً نام دسته‌بندی را وارد کنید",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    OutlinedTextField(
                                        value = description,
                                        onValueChange = { description = it },
                                        label = { Text("توضیحات یا نمونه وسایل (اختیاری)") },
                                        placeholder = { Text("مثال: تلویزیون، سینمای خانگی، کنسول بازی") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Icon Picker
                                    Text(
                                        text = "انتخاب آیکون دسته‌بندی:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(horizontal = 2.dp)
                                    ) {
                                        items(CategoryIconCatalog.availableIcons) { (iconKey, iconVector) ->
                                            val isSelected = iconKey == selectedIconKey
                                            val activeColor = Color(selectedColorHex)
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) activeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .border(
                                                        width = if (isSelected) 2.dp else 1.dp,
                                                        color = if (isSelected) activeColor else MaterialTheme.colorScheme.outlineVariant,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { selectedIconKey = iconKey }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = iconVector,
                                                        contentDescription = null,
                                                        tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Color Picker
                                    Text(
                                        text = "انتخاب رنگ دسته‌بندی:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(horizontal = 2.dp)
                                    ) {
                                        items(CategoryIconCatalog.availableColors) { colorHex ->
                                            val isSelected = colorHex == selectedColorHex
                                            val c = Color(colorHex)
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(c)
                                                    .border(
                                                        width = if (isSelected) 2.5.dp else 1.dp,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.5f),
                                                        shape = CircleShape
                                                    )
                                                    .clickable { selectedColorHex = colorHex }
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Vehicle odometer option
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsCar,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = "پشتیبانی از کیلومترشمار",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "مناسب وسایل نقلیه و ماشین‌آلات دارای کارکرد کیلومتری",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Switch(
                                                checked = isVehicleType,
                                                onCheckedChange = { isVehicleType = it }
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (titlePersian.isBlank()) {
                                                    titleError = true
                                                    return@Button
                                                }
                                                val key = editingKey ?: "CUSTOM_${UUID.randomUUID().toString().take(8).uppercase()}"
                                                val newCat = CustomToolCategory(
                                                    key = key,
                                                    titlePersian = titlePersian.trim(),
                                                    iconKey = selectedIconKey,
                                                    colorHex = selectedColorHex,
                                                    description = description.trim().ifBlank { "دسته‌بندی سفارشی" },
                                                    isVehicleType = isVehicleType
                                                )
                                                onSaveCustomCategory(newCat)
                                                resetForm()
                                                showForm = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            AutoResizedButtonText("ذخیره دسته‌بندی", maxFontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                resetForm()
                                                showForm = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            AutoResizedButtonText("انصراف", maxFontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider()

                        // Custom categories list if any
                        if (customCategories.isNotEmpty()) {
                            Text(
                                text = "دسته‌بندی‌های سفارشی شما:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            customCategories.forEach { customCat ->
                                val uiModel = customCat.toUiModel()
                                val count = allTools.count { it.categoryName.equals(customCat.key, ignoreCase = true) }
                                val catColor = Color(customCat.colorHex)
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = catColor.copy(alpha = 0.08f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, catColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = catColor.copy(alpha = 0.2f),
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = uiModel.icon,
                                                        contentDescription = null,
                                                        tint = catColor,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = customCat.titlePersian,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = catColor.copy(alpha = 0.16f)
                                                    ) {
                                                        Text(
                                                            text = "${JalaliCalendar.toPersianDigits(count)} وسیله",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = catColor,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = customCat.description,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    editingKey = customCat.key
                                                    titlePersian = customCat.titlePersian
                                                    description = customCat.description
                                                    selectedIconKey = customCat.iconKey
                                                    selectedColorHex = customCat.colorHex
                                                    isVehicleType = customCat.isVehicleType
                                                    showForm = true
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "ویرایش",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    onDeleteCustomCategory(customCat.key)
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "حذف",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Built-in categories list
                        Text(
                            text = "دسته‌بندی‌های استاندارد برنامه:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        allCategories.filter { !it.isCustom }.forEach { cat ->
                            val count = allTools.count { it.categoryName.equals(cat.key, ignoreCase = true) }
                            val catColor = Color(cat.colorHex)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = catColor.copy(alpha = 0.16f),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = cat.icon,
                                                    contentDescription = null,
                                                    tint = catColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = cat.titlePersian,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = cat.description,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = catColor.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = "${JalaliCalendar.toPersianDigits(count)} وسیله",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = catColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Tab 1: Quick Categorize Existing Tools
                        Text(
                            text = "برای تغییر دسته‌بندی هر وسیله، روی دکمه تغییر دسته بزنید:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (allTools.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "هنوز هیچ وسیله‌ای ثبت نشده است.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            allTools.forEach { tool ->
                                val catUi = CustomCategoryRegistry.resolve(tool.categoryName, customCategories)
                                val isExpanded = toolForCategoryChange?.id == tool.id
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (isExpanded) 1.5.dp else 0.5.dp,
                                            color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = tool.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                CategoryChip(categoryUi = catUi)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    toolForCategoryChange = if (isExpanded) null else tool
                                                },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SwapHoriz,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                AutoResizedButtonText(
                                                    text = if (isExpanded) "بستن" else "تغییر دسته",
                                                    maxFontSize = 11.5.sp
                                                )
                                            }
                                        }

                                        if (isExpanded) {
                                            HorizontalDivider()
                                            Text(
                                                text = "انتخاب دسته‌بندی جدید برای «${tool.name}»:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            FlowRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                allCategories.forEach { targetCat ->
                                                    val isCurrent = tool.categoryName.equals(targetCat.key, ignoreCase = true)
                                                    val cColor = Color(targetCat.colorHex)
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = if (isCurrent) cColor else MaterialTheme.colorScheme.surface,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .border(
                                                                width = 1.dp,
                                                                color = if (isCurrent) cColor else MaterialTheme.colorScheme.outlineVariant,
                                                                shape = RoundedCornerShape(10.dp)
                                                            )
                                                            .clickable {
                                                                onAssignToolCategory(tool, targetCat.key)
                                                                toolForCategoryChange = null
                                                            }
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = targetCat.icon,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(14.dp),
                                                                tint = if (isCurrent) Color.White else cColor
                                                            )
                                                            Text(
                                                                text = targetCat.titlePersian,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissRequest,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    AutoResizedButtonText("بستن", maxFontSize = 12.5.sp)
                }
            }
        )
    }
}
