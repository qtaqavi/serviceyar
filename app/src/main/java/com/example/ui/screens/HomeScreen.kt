package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceScheduleEntity
import com.example.data.model.ServiceStatus
import com.example.data.model.ToolCategory
import com.example.data.model.ToolEntity
import com.example.data.model.ToolWithServices
import com.example.ui.components.CategoryChip
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainerLight
import com.example.ui.theme.SleekOnPrimaryContainerLight
import com.example.ui.theme.StatusDueSoonAmber
import com.example.ui.theme.StatusDueSoonAmberContainer
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusOverdueRedContainer
import com.example.ui.theme.StatusUpToDateGreen
import com.example.ui.theme.StatusUpToDateGreenContainer
import com.example.util.JalaliCalendar
import com.example.util.JalaliDate

@Composable
fun HomeScreen(
    toolsWithServices: List<ToolWithServices>,
    allSchedules: List<ServiceScheduleEntity>,
    onToolClick: (ToolEntity) -> Unit,
    onAddNewTool: () -> Unit,
    onAddNewService: (ToolEntity) -> Unit,
    onMarkServiceDone: (ServiceScheduleEntity) -> Unit,
    onEditService: (ToolEntity, ServiceScheduleEntity) -> Unit,
    onTriggerTestNotification: () -> Unit,
    onLoadSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<ServiceStatus?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<ToolCategory?>(null) }

    val now = JalaliCalendar.now()

    // Aggregate statistics
    val totalToolsCount = toolsWithServices.size
    val overdueCount = allSchedules.count { it.computeStatus(now) == ServiceStatus.OVERDUE }
    val dueSoonCount = allSchedules.count { it.computeStatus(now) == ServiceStatus.DUE_SOON }
    val upToDateCount = allSchedules.count { it.computeStatus(now) == ServiceStatus.UP_TO_DATE }
    val totalEstimatedCost = allSchedules.sumOf { it.estimatedCost }

    // Find the most urgent or upcoming service for the Hero Banner
    val heroUpcomingSchedule = remember(allSchedules, toolsWithServices) {
        allSchedules
            .sortedWith(
                compareBy<ServiceScheduleEntity> {
                    when (it.computeStatus(now)) {
                        ServiceStatus.OVERDUE -> 0
                        ServiceStatus.DUE_SOON -> 1
                        ServiceStatus.UP_TO_DATE -> 2
                        ServiceStatus.EXPIRED_WARRANTY -> 3
                        ServiceStatus.NO_SCHEDULE -> 4
                    }
                }.thenBy {
                    it.getDaysUntilNext(now) ?: 9999
                }
            ).firstOrNull()
    }

    // Filter tools based on search and selected chips
    val filteredTools = remember(toolsWithServices, searchQuery, selectedStatusFilter, selectedCategoryFilter) {
        val q = JalaliCalendar.toEnglishDigits(searchQuery.trim().lowercase())
            .replace('ي', 'ی')
            .replace('ك', 'ک')

        toolsWithServices.filter { toolWithServices ->
            val tool = toolWithServices.tool
            val category = tool.getCategory()

            // Category filter
            if (selectedCategoryFilter != null && category != selectedCategoryFilter) {
                return@filter false
            }

            // Status filter
            if (selectedStatusFilter != null && toolWithServices.getOverallStatus(now) != selectedStatusFilter) {
                return@filter false
            }

            // Search query filter
            if (q.isNotEmpty()) {
                val toolNameNorm = tool.name.lowercase().replace('ي', 'ی').replace('ك', 'ک')
                val modelNorm = tool.modelOrBrand.lowercase().replace('ي', 'ی').replace('ك', 'ک')
                val locNorm = tool.location.lowercase().replace('ي', 'ی').replace('ك', 'ک')
                val servicesMatch = toolWithServices.schedules.any {
                    it.title.lowercase().replace('ي', 'ی').replace('ك', 'ک').contains(q)
                }

                toolNameNorm.contains(q) || modelNorm.contains(q) || locNorm.contains(q) || servicesMatch
            } else {
                true
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Sleek Header (مدیریت سرویس + Date + User Avatar Action)
        item {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "مدیریت سرویس",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "امروز: ${now.format(includeDayName = true)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Sleek Avatar / Quick Action Pill
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onTriggerTestNotification() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "پروفایل و یادآوری‌ها",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Hero Card (Sleek Royal Blue #005AC1 with rounded 28dp corners & glowing accents)
        if (heroUpcomingSchedule != null) {
            item {
                val heroTool = toolsWithServices.firstOrNull { it.tool.id == heroUpcomingSchedule.toolId }?.tool
                val heroDaysDiff = heroUpcomingSchedule.getDaysUntilNext(now)
                val heroStatus = heroUpcomingSchedule.computeStatus(now)

                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clickable {
                            if (heroTool != null) onToolClick(heroTool)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                // Draw glowing decorative radial circles in the background
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                                        center = Offset(size.width * 0.15f, size.height * 0.9f),
                                        radius = size.width * 0.45f
                                    )
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                                        center = Offset(size.width * 0.9f, size.height * 0.1f),
                                        radius = size.width * 0.35f
                                    )
                                )
                            }
                            .padding(22.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (heroStatus == ServiceStatus.OVERDUE) "⚠️ سرویس فوری و معوقه" else "سرویس نزدیک",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = heroUpcomingSchedule.getIntervalType().titlePersian,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = "${heroTool?.name ?: heroUpcomingSchedule.toolName} (${heroUpcomingSchedule.title})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (heroStatus == ServiceStatus.OVERDUE) Color(0xFFFFDAD6) else Color.White.copy(alpha = 0.22f)
                                ) {
                                    val daysText = when {
                                        heroDaysDiff >= 9999 -> "موعد سرویس"
                                        heroDaysDiff < 0 -> "${JalaliCalendar.toPersianDigits(-heroDaysDiff)} روز تاخیر"
                                        heroDaysDiff == 0 -> "امروز"
                                        else -> "${JalaliCalendar.toPersianDigits(heroDaysDiff)} روز مانده"
                                    }
                                    Text(
                                        text = daysText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (heroStatus == ServiceStatus.OVERDUE) Color(0xFFBA1A1A) else Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = heroUpcomingSchedule.getServiceType().titlePersian,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    modifier = Modifier.clickable {
                                        onMarkServiceDone(heroUpcomingSchedule)
                                    }
                                ) {
                                    Text(
                                        text = "ثبت انجام",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SleekPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sleek Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "جستجو در نام ابزار، مدل، برند یا نوع سرویس...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "جستجو",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
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
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Overdue Alert Banner (if any)
        if (overdueCount > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusOverdueRedContainer),
                    border = BorderStroke(1.dp, StatusOverdueRed.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(StatusOverdueRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "هشدار: ${JalaliCalendar.toPersianDigits(overdueCount)} مورد نیازمند سرویس فوری!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusOverdueRed
                            )
                            Text(
                                text = "موعد سرویس دوره‌ای سپری شده است. جهت جلوگیری از استهلاک اقدام نمایید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                selectedStatusFilter = ServiceStatus.OVERDUE
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusOverdueRed),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("مشاهده", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Summary Statistics Cards
        item {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StatCard(
                            title = "کل ابزار و وسایل",
                            value = JalaliCalendar.toPersianDigits(totalToolsCount),
                            subtitle = "تجهیزات ثبت‌شده",
                            icon = Icons.Default.Build,
                            iconColor = SleekPrimary,
                            modifier = Modifier.width(145.dp)
                        )
                    }
                    item {
                        StatCard(
                            title = "نیازمند اقدام فوری",
                            value = JalaliCalendar.toPersianDigits(overdueCount),
                            subtitle = "موعد گذشته",
                            icon = Icons.Default.Error,
                            iconColor = StatusOverdueRed,
                            containerColor = if (overdueCount > 0) StatusOverdueRedContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.width(145.dp)
                        )
                    }
                    item {
                        StatCard(
                            title = "نزدیک به موعد",
                            value = JalaliCalendar.toPersianDigits(dueSoonCount),
                            subtitle = "طی ۳۰ روز آینده",
                            icon = Icons.Default.Alarm,
                            iconColor = StatusDueSoonAmber,
                            modifier = Modifier.width(145.dp)
                        )
                    }
                    item {
                        StatCard(
                            title = "سرویس‌شده و سالم",
                            value = JalaliCalendar.toPersianDigits(upToDateCount),
                            subtitle = "وضعیت مطلوب",
                            icon = Icons.Default.CheckCircle,
                            iconColor = StatusUpToDateGreen,
                            modifier = Modifier.width(145.dp)
                        )
                    }
                    item {
                        StatCard(
                            title = "برآورد هزینه‌ها",
                            value = JalaliCalendar.formatPrice(totalEstimatedCost),
                            subtitle = "هزینه تخمینی دوره",
                            icon = Icons.Default.AttachMoney,
                            iconColor = Color(0xFF6750A4),
                            modifier = Modifier.width(175.dp)
                        )
                    }
                }
            }
        }

        // Filter Chips Row
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedStatusFilter == null && selectedCategoryFilter == null,
                            onClick = {
                                selectedStatusFilter = null
                                selectedCategoryFilter = null
                            },
                            shape = CircleShape,
                            label = { Text("همه (${JalaliCalendar.toPersianDigits(totalToolsCount)})", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedStatusFilter == ServiceStatus.OVERDUE,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == ServiceStatus.OVERDUE) null else ServiceStatus.OVERDUE
                            },
                            shape = CircleShape,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (selectedStatusFilter == ServiceStatus.OVERDUE) Color.White else StatusOverdueRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("فوری (${JalaliCalendar.toPersianDigits(overdueCount)})", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusOverdueRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedStatusFilter == ServiceStatus.DUE_SOON,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == ServiceStatus.DUE_SOON) null else ServiceStatus.DUE_SOON
                            },
                            shape = CircleShape,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (selectedStatusFilter == ServiceStatus.DUE_SOON) Color.White else StatusDueSoonAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("نزدیک موعد (${JalaliCalendar.toPersianDigits(dueSoonCount)})", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusDueSoonAmber,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(ToolCategory.entries) { category ->
                        val isSelected = category == selectedCategoryFilter
                        val categoryColor = Color(category.colorHex)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategoryFilter = if (isSelected) null else category
                            },
                            shape = CircleShape,
                            leadingIcon = {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else categoryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            label = { Text(category.titlePersian) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = categoryColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Section Title: «لیست ابزارها»
        item {
            SectionHeader(
                title = "لیست ابزارها",
                count = filteredTools.size,
                actionText = "افزودن وسیله +",
                onActionClick = onAddNewTool
            )
        }

        // Empty state
        if (filteredTools.isEmpty()) {
            item {
                EmptyStateView(
                    title = if (toolsWithServices.isEmpty()) "هنوز هیچ وسیله‌ای اضافه نشده است" else "هیچ وسیله‌ای با این فیلتر یافت نشد",
                    description = if (toolsWithServices.isEmpty()) "شما می‌توانید اولین وسیله، خودرو، پکیج یا ابزار کارگاهی خود را ثبت کنید یا داده‌های نمونه فارسی را بارگذاری نمایید." else "فیلترها یا عبارت جستجو را تغییر دهید.",
                    icon = Icons.Default.Build
                )
                if (toolsWithServices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onAddNewTool,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("افزودن وسیله جدید", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onLoadSampleData,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("بارگذاری وسایل نمونه")
                            }
                        }
                    }
                }
            }
        } else {
            // Sleek Tool Items List (Cards with 24dp rounded corners, Category Boxes, Badges & nested schedules)
            items(filteredTools, key = { it.tool.id }) { toolWithServices ->
                SleekToolServiceCard(
                    toolWithServices = toolWithServices,
                    onToolClick = { onToolClick(toolWithServices.tool) },
                    onAddNewService = { onAddNewService(toolWithServices.tool) },
                    onMarkServiceDone = onMarkServiceDone,
                    onEditService = { schedule -> onEditService(toolWithServices.tool, schedule) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
fun SleekToolServiceCard(
    toolWithServices: ToolWithServices,
    onToolClick: () -> Unit,
    onAddNewService: () -> Unit,
    onMarkServiceDone: (ServiceScheduleEntity) -> Unit,
    onEditService: (ServiceScheduleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val tool = toolWithServices.tool
    val category = tool.getCategory()
    val categoryColor = Color(category.colorHex)
    val now = JalaliCalendar.now()
    val overallStatus = toolWithServices.getOverallStatus(now)

    val nearestSchedule = toolWithServices.getNearestUpcomingSchedule(now)
    val daysDiff = nearestSchedule?.getDaysUntilNext(now)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToolClick)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main Tool Header Row (Category box 48x48 + Details + Status Badge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Sleek Category Box (w-12 h-12 rounded-2xl with category color)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(categoryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.titlePersian,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Tool Title & Category / Location details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tool.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (tool.modelOrBrand.isNotBlank()) {
                            Text(
                                text = tool.modelOrBrand,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (tool.location.isNotBlank()) {
                            Text(
                                text = "• ${tool.location}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Status Badge Pill
                StatusBadge(status = overallStatus, daysDiff = daysDiff)
            }

            // Schedules sub-list container
            if (toolWithServices.schedules.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    toolWithServices.schedules.forEach { schedule ->
                        SleekScheduleItemRow(
                            schedule = schedule,
                            onMarkDone = { onMarkServiceDone(schedule) },
                            onEdit = { onEditService(schedule) }
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "بدون برنامه سرویس",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "+ تعریف سرویس",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(onClick = onAddNewService)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SleekScheduleItemRow(
    schedule: ServiceScheduleEntity,
    onMarkDone: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now = JalaliCalendar.now()
    val status = schedule.computeStatus(now)
    val nextDate = JalaliCalendar.parse(schedule.nextServiceDateJalali)
    val daysUntilNext = schedule.getDaysUntilNext(now)
    val relativeTime = if (nextDate != null) JalaliCalendar.getRelativeTimeString(nextDate, now) else "نامشخص"
    val serviceType = schedule.getServiceType()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = when (status) {
            ServiceStatus.OVERDUE -> StatusOverdueRedContainer.copy(alpha = 0.65f)
            ServiceStatus.DUE_SOON -> Color(0xFFCCE8E8).copy(alpha = 0.55f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        border = when (status) {
            ServiceStatus.OVERDUE -> BorderStroke(1.dp, StatusOverdueRed.copy(alpha = 0.3f))
            ServiceStatus.DUE_SOON -> BorderStroke(0.5.dp, Color(0xFF006A6A).copy(alpha = 0.25f))
            else -> null
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(serviceType.badgeColorHex).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = serviceType.icon,
                            contentDescription = null,
                            tint = Color(serviceType.badgeColorHex),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = schedule.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${serviceType.titlePersian} • ${schedule.getIntervalType().titlePersian}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Countdown Relative Time Pill
                Surface(
                    shape = CircleShape,
                    color = when (status) {
                        ServiceStatus.OVERDUE -> StatusOverdueRed
                        ServiceStatus.DUE_SOON -> Color(0xFF006A6A)
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Text(
                        text = relativeTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (status) {
                            ServiceStatus.OVERDUE, ServiceStatus.DUE_SOON -> Color.White
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            // Next Service Date & Quick Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "موعد: ${JalaliCalendar.toPersianDigits(schedule.nextServiceDateJalali)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (schedule.estimatedCost > 0) {
                        Text(
                            text = "• ${JalaliCalendar.formatPrice(schedule.estimatedCost)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "ویرایش",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onEdit)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )

                    Button(
                        onClick = onMarkDone,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (status == ServiceStatus.OVERDUE) StatusOverdueRed else MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ثبت انجام", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
