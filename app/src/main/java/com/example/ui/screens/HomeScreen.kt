package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomCategoryRegistry
import com.example.data.model.CustomToolCategory
import com.example.data.model.ServiceScheduleEntity
import com.example.data.model.ServiceStatus
import com.example.data.model.ToolEntity
import com.example.data.model.ToolWithServices
import com.example.ui.components.AutoResizedButtonText
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusDueSoonAmber
import com.example.ui.theme.StatusDueSoonAmberContainer
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusOverdueRedContainer
import com.example.ui.theme.StatusUpToDateGreen
import com.example.ui.theme.StatusUpToDateGreenContainer
import com.example.util.JalaliCalendar

private data class HomeAlertItem(
    val tool: ToolEntity,
    val schedule: ServiceScheduleEntity,
    val status: ServiceStatus,
    val daysUntilNext: Int
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    toolsWithServices: List<ToolWithServices>,
    allSchedules: List<ServiceScheduleEntity>,
    customCategories: List<CustomToolCategory> = CustomCategoryRegistry.customCategories,
    onToolClick: (ToolEntity) -> Unit,
    onAddNewTool: () -> Unit,
    onAddNewService: (ToolEntity) -> Unit,
    onMarkServiceDone: (ServiceScheduleEntity) -> Unit,
    onEditService: (ToolEntity, ServiceScheduleEntity) -> Unit,
    onTriggerTestNotification: () -> Unit,
    onLoadSampleData: () -> Unit,
    onOpenCategoryManagement: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenReports: () -> Unit = {},
    onOpenBackupRestore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<ServiceStatus?>(null) }
    var selectedCategoryKeyFilter by remember { mutableStateOf<String?>(null) }

    val allCategoryModels = remember(customCategories) {
        CustomCategoryRegistry.getAllCategoryModels(customCategories)
    }

    val now = JalaliCalendar.now()
    val totalToolsCount = toolsWithServices.size

    // Build active alerts list (Overdue, Due Soon, Expired Warranty)
    val activeAlerts = remember(toolsWithServices, now) {
        val list = mutableListOf<HomeAlertItem>()
        toolsWithServices.forEach { tws ->
            val isVeh = tws.tool.isVehicle()
            val odo = if (isVeh) tws.tool.currentOdometerKm else 0
            tws.schedules.forEach { sched ->
                val st = sched.computeStatus(now, odo, isVeh)
                if (st == ServiceStatus.OVERDUE || st == ServiceStatus.DUE_SOON || st == ServiceStatus.EXPIRED_WARRANTY) {
                    list.add(
                        HomeAlertItem(
                            tool = tws.tool,
                            schedule = sched,
                            status = st,
                            daysUntilNext = sched.getDaysUntilNext(now, isVeh)
                        )
                    )
                }
            }
        }
        list.sortedWith(
            compareBy<HomeAlertItem> {
                when (it.status) {
                    ServiceStatus.OVERDUE -> 0
                    ServiceStatus.EXPIRED_WARRANTY -> 1
                    ServiceStatus.DUE_SOON -> 2
                    else -> 3
                }
            }.thenBy { it.daysUntilNext }
        )
    }

    val overdueCount = activeAlerts.count { it.status == ServiceStatus.OVERDUE || it.status == ServiceStatus.EXPIRED_WARRANTY }
    val dueSoonCount = activeAlerts.count { it.status == ServiceStatus.DUE_SOON }

    // Filter tools based on search and selected chips
    val filteredTools = remember(toolsWithServices, searchQuery, selectedStatusFilter, selectedCategoryKeyFilter, customCategories) {
        val q = JalaliCalendar.toEnglishDigits(searchQuery.trim().lowercase())
            .replace('ي', 'ی')
            .replace('ك', 'ک')

        toolsWithServices.filter { toolWithServices ->
            val tool = toolWithServices.tool
            val catUi = CustomCategoryRegistry.resolve(tool.categoryName, customCategories)

            if (selectedCategoryKeyFilter != null && !catUi.key.equals(selectedCategoryKeyFilter, ignoreCase = true)) {
                return@filter false
            }

            if (selectedStatusFilter != null && toolWithServices.getOverallStatus(now) != selectedStatusFilter) {
                return@filter false
            }

            if (q.isNotEmpty()) {
                val toolNameNorm = tool.name.lowercase().replace('ي', 'ی').replace('ك', 'ک')
                val catNorm = catUi.titlePersian.lowercase().replace('ي', 'ی').replace('ك', 'ک')
                val servicesMatch = toolWithServices.schedules.any {
                    it.title.lowercase().replace('ي', 'ی').replace('ك', 'ک').contains(q)
                }
                toolNameNorm.contains(q) || catNorm.contains(q) || servicesMatch
            } else {
                true
            }
        }
    }

    // Categories that actually have tools (or are custom / currently selected) to eliminate empty category clutter
    val visibleCategories = remember(allCategoryModels, toolsWithServices, customCategories, selectedCategoryKeyFilter) {
        val populated = allCategoryModels.filter { cat ->
            val count = toolsWithServices.count {
                CustomCategoryRegistry.resolve(it.tool.categoryName, customCategories).key.equals(cat.key, ignoreCase = true)
            }
            count > 0 || cat.isCustom || cat.key.equals(selectedCategoryKeyFilter, ignoreCase = true)
        }
        populated.ifEmpty { allCategoryModels }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // FIXED STICKY TOP HEADER: "مدیریت سرویس" + Action Icons (Never scrolls when scrolling down or up)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "مدیریت سرویس",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "امروز: ${now.format(includeDayName = true)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Fixed Top Bar Icons: Reports, Cloud/Backup, Settings, Alert Bell
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        IconButton(
                            onClick = onOpenReports,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f))
                                .testTag("open_reports_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "گزارش‌گیری",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenBackupRestore,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f))
                                .testTag("open_cloud_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "ذخیره در فضای ابری و پشتیبان‌گیری",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                .testTag("open_settings_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "تنظیمات برنامه",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (overdueCount > 0) StatusOverdueRedContainer else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { onTriggerTestNotification() }
                                .testTag("alert_bell_top_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "زنگوله هشدار",
                                    tint = if (overdueCount > 0) StatusOverdueRed else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Quick Action Bar for Cloud Save, Reports & Backup
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenReports() }
                            .testTag("home_reports_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            AutoResizedButtonText(
                                text = "گزارش‌گیری",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxFontSize = 11.sp,
                                minFontSize = 8.sp,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenBackupRestore() }
                            .testTag("home_backup_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            AutoResizedButtonText(
                                text = "پشتیبان‌گیری",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxFontSize = 11.sp,
                                minFontSize = 8.sp,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenBackupRestore() }
                            .testTag("home_cloud_save_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            AutoResizedButtonText(
                                text = "فضای ابری",
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                maxFontSize = 11.sp,
                                minFontSize = 8.sp,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                }
            }
        }

        // Scrollable Body: Search, Alerts Section, Compact Device Categories, and Clean Full-Title Tool List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "جستجوی نام ابزار یا تجهیز...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "جستجو",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // 1. ALERTS SECTION (هشدارها)
            item {
                AlertsSummarySection(
                    activeAlerts = activeAlerts,
                    onAlertClick = { alertItem -> onToolClick(alertItem.tool) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 2. DEVICE CATEGORIES SECTION (دسته‌بندی دستگاه‌ها - بدون فضای خالی)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = "دسته‌بندی دستگاه‌ها",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onOpenCategoryManagement() }
                                    .testTag("manage_categories_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderSpecial,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    AutoResizedButtonText(
                                        text = "مدیریت دسته‌ها",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxFontSize = 11.5.sp,
                                        minFontSize = 8.5.sp
                                    )
                                }
                            }
                        }

                        // Tight FlowRow with custom compact pills (eliminates empty spaces)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val allSelected = selectedStatusFilter == null && selectedCategoryKeyFilter == null
                            CompactCategoryFilterPill(
                                label = "همه (${JalaliCalendar.toPersianDigits(totalToolsCount)})",
                                isSelected = allSelected,
                                activeColor = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    selectedStatusFilter = null
                                    selectedCategoryKeyFilter = null
                                }
                            )

                            if (overdueCount > 0) {
                                val isOverdueSelected = selectedStatusFilter == ServiceStatus.OVERDUE
                                CompactCategoryFilterPill(
                                    label = "هشدار فوری (${JalaliCalendar.toPersianDigits(overdueCount)})",
                                    isSelected = isOverdueSelected,
                                    activeColor = StatusOverdueRed,
                                    icon = Icons.Default.Error,
                                    onClick = {
                                        selectedStatusFilter = if (isOverdueSelected) null else ServiceStatus.OVERDUE
                                    }
                                )
                            }

                            if (dueSoonCount > 0) {
                                val isDueSoonSelected = selectedStatusFilter == ServiceStatus.DUE_SOON
                                CompactCategoryFilterPill(
                                    label = "نزدیک موعد (${JalaliCalendar.toPersianDigits(dueSoonCount)})",
                                    isSelected = isDueSoonSelected,
                                    activeColor = StatusDueSoonAmber,
                                    icon = Icons.Default.Warning,
                                    onClick = {
                                        selectedStatusFilter = if (isDueSoonSelected) null else ServiceStatus.DUE_SOON
                                    }
                                )
                            }

                            visibleCategories.forEach { category ->
                                val isSelected = category.key.equals(selectedCategoryKeyFilter, ignoreCase = true)
                                val categoryColor = Color(category.colorHex)
                                val countInCat = toolsWithServices.count {
                                    CustomCategoryRegistry.resolve(it.tool.categoryName, customCategories)
                                        .key.equals(category.key, ignoreCase = true)
                                }
                                CompactCategoryFilterPill(
                                    label = if (countInCat > 0) {
                                        "${category.titlePersian} (${JalaliCalendar.toPersianDigits(countInCat)})"
                                    } else {
                                        category.titlePersian
                                    },
                                    isSelected = isSelected,
                                    activeColor = categoryColor,
                                    icon = category.icon,
                                    onClick = {
                                        selectedCategoryKeyFilter = if (isSelected) null else category.key
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. TOOLS & EQUIPMENT LIST HEADER
            item {
                SectionHeader(
                    title = "ابزارها و تجهیزات",
                    count = filteredTools.size,
                    actionText = "افزودن وسیله +",
                    onActionClick = onAddNewTool
                )
            }

            // 4. TOOLS & EQUIPMENT ITEMS (Clean Full-Title Cards + Alert Status Only)
            if (filteredTools.isEmpty()) {
                item {
                    EmptyStateView(
                        title = if (toolsWithServices.isEmpty()) "هنوز هیچ وسیله‌ای اضافه نشده است" else "هیچ وسیله‌ای با این فیلتر یافت نشد",
                        description = if (toolsWithServices.isEmpty()) "اولین وسیله یا تجهیز خود را ثبت کنید یا وسایل نمونه را بارگذاری نمایید." else "فیلترها یا عبارت جستجو را تغییر دهید.",
                        icon = Icons.Default.Build
                    )
                    if (toolsWithServices.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onAddNewTool,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                AutoResizedButtonText(
                                    text = "افزودن وسیله جدید",
                                    maxFontSize = 12.sp,
                                    minFontSize = 8.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                            OutlinedButton(
                                onClick = onLoadSampleData,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                AutoResizedButtonText(
                                    text = "بارگذاری وسایل نمونه",
                                    maxFontSize = 12.sp,
                                    minFontSize = 8.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }
                }
            } else if (selectedCategoryKeyFilter == null) {
                val groupedByCat = filteredTools.groupBy {
                    CustomCategoryRegistry.resolve(it.tool.categoryName, customCategories).key
                }
                groupedByCat.forEach { (catKey, toolsInGroup) ->
                    val catUi = CustomCategoryRegistry.resolve(catKey, customCategories)
                    val catColor = Color(catUi.colorHex)
                    item(key = "cat_group_$catKey") {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, catColor.copy(alpha = 0.28f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Compact Category Header without empty space
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(catColor.copy(alpha = 0.10f))
                                        .clickable { selectedCategoryKeyFilter = catKey }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = catColor,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = catUi.icon,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = catUi.titlePersian,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = catColor.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = "${JalaliCalendar.toPersianDigits(toolsInGroup.size)} مورد",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = catColor,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Tools inside this category
                                toolsInGroup.forEachIndexed { index, toolWithServices ->
                                    if (index > 0) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                    }
                                    CompactToolTitleRow(
                                        toolWithServices = toolWithServices,
                                        onClick = { onToolClick(toolWithServices.tool) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredTools, key = { it.tool.id }) { toolWithServices ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        CompactToolTitleRow(
                            toolWithServices = toolWithServices,
                            onClick = { onToolClick(toolWithServices.tool) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactCategoryFilterPill(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(
            width = 0.8.dp,
            color = if (isSelected) activeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else activeColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AlertsSummarySection(
    activeAlerts: List<HomeAlertItem>,
    onAlertClick: (HomeAlertItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeAlerts.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = StatusUpToDateGreenContainer.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, StatusUpToDateGreen.copy(alpha = 0.35f)),
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = StatusUpToDateGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "وضعیت هشدارها: تمامی ابزارها و تجهیزات به‌روز و بدون هشدار فوری هستند.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = StatusUpToDateGreen
                )
            }
        }
    } else {
        val hasOverdue = activeAlerts.any { it.status == ServiceStatus.OVERDUE || it.status == ServiceStatus.EXPIRED_WARRANTY }
        val borderColor = if (hasOverdue) StatusOverdueRed.copy(alpha = 0.45f) else StatusDueSoonAmber.copy(alpha = 0.45f)
        val headerBg = if (hasOverdue) StatusOverdueRedContainer.copy(alpha = 0.65f) else StatusDueSoonAmberContainer.copy(alpha = 0.65f)
        val accentColor = if (hasOverdue) StatusOverdueRed else StatusDueSoonAmber

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = "هشدارهای سرویس و نگهداری (${JalaliCalendar.toPersianDigits(activeAlerts.size)})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "مشاهده",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                activeAlerts.forEachIndexed { index, alert ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    val isOverdue = alert.status == ServiceStatus.OVERDUE || alert.status == ServiceStatus.EXPIRED_WARRANTY
                    val itemAccent = if (isOverdue) StatusOverdueRed else StatusDueSoonAmber

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAlertClick(alert) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(itemAccent)
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = alert.tool.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "هشدار: ${alert.schedule.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = itemAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(status = alert.status, daysDiff = alert.daysUntilNext)
                    }
                }
            }
        }
    }
}

/**
 * Clean, uncluttered row displaying ONLY the full tool title and its alert status.
 * Clicking navigates the user to the tool's dedicated detail screen (`ToolDetailScreen`).
 */
@Composable
private fun CompactToolTitleRow(
    toolWithServices: ToolWithServices,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tool = toolWithServices.tool
    val category = tool.getCategoryUi()
    val categoryColor = Color(category.colorHex)
    val now = JalaliCalendar.now()
    val isVeh = tool.isVehicle()
    val overallStatus = toolWithServices.getOverallStatus(now)
    val nearestSchedule = toolWithServices.getNearestUpcomingSchedule(now)
    val daysDiff = nearestSchedule?.getDaysUntilNext(now, isVeh)

    // Find any active alert titles for this tool
    val alertSchedules = remember(toolWithServices, now) {
        val odo = if (isVeh) tool.currentOdometerKm else 0
        toolWithServices.schedules.filter {
            val st = it.computeStatus(now, odo, isVeh)
            st == ServiceStatus.OVERDUE || st == ServiceStatus.DUE_SOON || st == ServiceStatus.EXPIRED_WARRANTY
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Category Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(categoryColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.titlePersian,
                tint = categoryColor,
                modifier = Modifier.size(21.dp)
            )
        }

        // Full Tool Title + Alert Indicator (if any)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = tool.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (alertSchedules.isNotEmpty()) {
                val alertSummary = alertSchedules.joinToString("، ") { it.title }
                Text(
                    text = "هشدار سرویس: $alertSummary",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (overallStatus == ServiceStatus.OVERDUE) StatusOverdueRed else StatusDueSoonAmber,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Alert Status Badge + Navigation Arrow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatusBadge(status = overallStatus, daysDiff = daysDiff)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "مشاهده صفحه اختصاصی وسیله",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
