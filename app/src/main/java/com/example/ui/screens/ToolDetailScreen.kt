package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceLogEntity
import com.example.data.model.ServiceScheduleEntity
import com.example.data.model.ServiceStatus
import com.example.data.model.ToolEntity
import com.example.data.model.ToolWithServices
import com.example.ui.components.AutoResizedButtonText
import com.example.ui.components.CategoryChip
import com.example.ui.components.EmptyStateView
import com.example.ui.components.IranianPlateBadge
import com.example.ui.components.PriorityBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusDueSoonAmber
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusUpToDateGreen
import com.example.util.JalaliCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolDetailScreen(
    toolWithServices: ToolWithServices,
    onBackClick: () -> Unit,
    onEditTool: (ToolEntity) -> Unit,
    onDeleteTool: (ToolEntity) -> Unit,
    onAddNewService: (ToolEntity) -> Unit,
    onEditService: (ServiceScheduleEntity) -> Unit,
    onDeleteService: (ServiceScheduleEntity) -> Unit,
    onMarkServiceDone: (ServiceScheduleEntity) -> Unit,
    onOpenToolReport: (ToolEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val tool = toolWithServices.tool
    val schedules = toolWithServices.schedules
    val logs = toolWithServices.logs
    val now = JalaliCalendar.now()
    val overallStatus = toolWithServices.getOverallStatus(now)

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        text = tool.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onOpenToolReport(tool) }) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "گزارش سرویس‌های این وسیله",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onEditTool(tool) }) {
                        Icon(Icons.Default.Edit, contentDescription = "ویرایش وسیله")
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف وسیله",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header Info Card (Sleek 2-column grid layout with zero wasted vertical column space)
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryChip(categoryUi = tool.getCategoryUi())
                            val nearest = toolWithServices.getNearestUpcomingSchedule(now)
                            StatusBadge(status = overallStatus, daysDiff = nearest?.getDaysUntilNext(now))
                        }

                        Text(
                            text = tool.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Vehicle License Plate & Current Odometer Full-Width Bar
                        if (tool.isVehicle() && (tool.serialNumber.isNotBlank() || tool.currentOdometerKm > 0)) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (tool.serialNumber.isNotBlank()) {
                                        IranianPlateBadge(plateString = tool.serialNumber, height = 44)
                                    }
                                    if (tool.currentOdometerKm > 0) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "کارکرد فعلی خودرو",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsCar,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "${JalaliCalendar.toPersianDigits(tool.currentOdometerKm)} کیلومتر",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2-Column Metadata Grid (eliminates empty vertical column space)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailSpecTile(
                                icon = Icons.Default.Category,
                                label = "مدل / برند",
                                value = tool.modelOrBrand.ifBlank { "ثبت نشده" },
                                modifier = Modifier.weight(1f)
                            )
                            DetailSpecTile(
                                icon = Icons.Default.LocationOn,
                                label = "محل نگهداری",
                                value = tool.location.ifBlank { "ثبت نشده" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (tool.purchaseDateJalali.isNotBlank() || tool.purchasePrice > 0 || (!tool.isVehicle() && tool.serialNumber.isNotBlank())) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (tool.purchaseDateJalali.isNotBlank()) {
                                    DetailSpecTile(
                                        icon = Icons.Default.CalendarMonth,
                                        label = "تاریخ خرید",
                                        value = JalaliCalendar.toPersianDigits(tool.purchaseDateJalali),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (tool.purchasePrice > 0) {
                                    DetailSpecTile(
                                        icon = Icons.Default.AttachMoney,
                                        label = "قیمت خرید",
                                        value = JalaliCalendar.formatPrice(tool.purchasePrice),
                                        valueColor = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else if (!tool.isVehicle() && tool.serialNumber.isNotBlank()) {
                                    DetailSpecTile(
                                        icon = Icons.Default.Pin,
                                        label = "شماره سریال",
                                        value = tool.serialNumber,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        if (tool.notes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = tool.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                        // Dedicated Tool Action Buttons: Add Service, Edit Tool, Delete Tool
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onAddNewService(tool) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 9.dp),
                                modifier = Modifier.weight(1.35f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AutoResizedButtonText(
                                    text = "افزودن سرویس",
                                    maxFontSize = 11.5.sp,
                                    minFontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { onEditTool(tool) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 9.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AutoResizedButtonText(
                                    text = "ویرایش",
                                    color = MaterialTheme.colorScheme.primary,
                                    maxFontSize = 11.5.sp,
                                    minFontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { showDeleteConfirmDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 9.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AutoResizedButtonText(
                                    text = "حذف",
                                    color = MaterialTheme.colorScheme.error,
                                    maxFontSize = 11.5.sp,
                                    minFontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Tab Selector: برنامه‌های سرویس (Schedules) vs سوابق انجام شده (History Logs)
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp)
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            AutoResizedButtonText(
                                text = "برنامه‌های سرویس (${JalaliCalendar.toPersianDigits(schedules.size)})",
                                maxFontSize = 12.sp,
                                minFontSize = 8.5.sp,
                                fontWeight = if (selectedTabIndex == 0) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (selectedTabIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            AutoResizedButtonText(
                                text = "سوابق انجام شده (${JalaliCalendar.toPersianDigits(logs.size)})",
                                maxFontSize = 12.sp,
                                minFontSize = 8.5.sp,
                                fontWeight = if (selectedTabIndex == 1) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (selectedTabIndex == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            if (selectedTabIndex == 0) {
                // Tab 0: Service Schedules
                item {
                    SectionHeader(
                        title = "برنامه‌ها و موعدهای سرویس",
                        count = schedules.size,
                        actionText = "+ سرویس جدید",
                        onActionClick = { onAddNewService(tool) }
                    )
                }

                if (schedules.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "برنامه سرویسی برای این وسیله ثبت نشده است",
                            description = if (tool.isVehicle()) {
                                "سرویس‌های ماهانه، فصلی، سالانه یا کیلومتری را اضافه کنید تا هشدارهای لازم ارسال شود."
                            } else {
                                "سرویس‌های دوره‌ای ماهانه، فصلی یا سالانه این دستگاه را اضافه کنید تا هشدارهای لازم ارسال شود."
                            },
                            icon = Icons.Default.Build
                        )
                    }
                } else {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleDetailCard(
                            schedule = schedule,
                            isVehicleTool = tool.isVehicle(),
                            currentToolOdometer = if (tool.isVehicle()) tool.currentOdometerKm else 0,
                            onMarkDone = { onMarkServiceDone(schedule) },
                            onEdit = { onEditService(schedule) },
                            onDelete = { onDeleteService(schedule) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                // Tab 1: Service History Logs
                item {
                    SectionHeader(
                        title = "تاریخچه سرویس‌های انجام شده",
                        count = logs.size
                    )
                }

                if (logs.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "هنوز هیچ سابقه‌ای برای این وسیله ثبت نشده است",
                            description = "با انجام هر سرویس، روی دکمه «ثبت انجام» کلیک کنید تا سابقه و هزینه آن در اینجا آرشیو شود.",
                            icon = Icons.Default.History
                        )
                    }
                } else {
                    items(logs, key = { it.id }) { log ->
                        ServiceLogCard(
                            log = log,
                            isVehicleTool = tool.isVehicle(),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("حذف وسیله", fontWeight = FontWeight.Bold) },
                text = {
                    Text("آیا از حذف «${tool.name}» و تمام برنامه‌ها و سوابق سرویس آن مطمئن هستید؟ این عمل غیرقابل بازگشت است.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteTool(tool)
                            showDeleteConfirmDialog = false
                            onBackClick()
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        AutoResizedButtonText("بله، حذف شود", maxFontSize = 12.5.sp)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = false },
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
private fun DetailSpecTile(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ScheduleDetailCard(
    schedule: ServiceScheduleEntity,
    isVehicleTool: Boolean = true,
    currentToolOdometer: Int = 0,
    onMarkDone: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now = JalaliCalendar.now()
    val status = schedule.computeStatus(now, currentToolOdometer, isVehicleTool)
    val serviceType = schedule.getServiceType()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = when (status) {
                ServiceStatus.OVERDUE -> StatusOverdueRed.copy(alpha = 0.4f)
                ServiceStatus.DUE_SOON -> StatusDueSoonAmber.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Service Icon + Full-Width Title & Compact Single-Line Metadata/Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(serviceType.badgeColorHex).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = serviceType.icon,
                        contentDescription = null,
                        tint = Color(serviceType.badgeColorHex),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = schedule.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusBadge(status = status, daysDiff = schedule.getDaysUntilNext(now))
                        PriorityBadge(priority = schedule.getPriority())
                        Text(
                            text = "${serviceType.titlePersian} • ${schedule.getIntervalType().titlePersian}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2x2 Specification Grid (eliminates unused empty vertical column space!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailSpecTile(
                    icon = Icons.Default.CalendarMonth,
                    label = "موعد سرویس بعدی",
                    value = JalaliCalendar.toPersianDigits(schedule.nextServiceDateJalali.ifBlank { "تعیین نشده" }),
                    valueColor = when (status) {
                        ServiceStatus.OVERDUE -> StatusOverdueRed
                        ServiceStatus.DUE_SOON -> StatusDueSoonAmber
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.weight(1f)
                )
                DetailSpecTile(
                    icon = Icons.Default.History,
                    label = "آخرین سرویس",
                    value = JalaliCalendar.toPersianDigits(schedule.lastServiceDateJalali.ifBlank { "ثبت نشده" }),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailSpecTile(
                    icon = Icons.Default.AttachMoney,
                    label = "هزینه تخمینی",
                    value = if (schedule.estimatedCost > 0) JalaliCalendar.formatPrice(schedule.estimatedCost) else "تعیین نشده",
                    valueColor = if (schedule.estimatedCost > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                DetailSpecTile(
                    icon = Icons.Default.Alarm,
                    label = "یادآوری هوشمند",
                    value = "${JalaliCalendar.toPersianDigits(schedule.reminderDaysBefore)} روز قبل از موعد",
                    modifier = Modifier.weight(1f)
                )
            }

            // Vehicle Kilometer & Daily Mileage Deduction Box (ONLY for vehicles)
            if (isVehicleTool && schedule.intervalKilometers > 0) {
                val traveledKm = schedule.getTraveledKilometers(currentToolOdometer, now)
                val remainingKm = schedule.getRemainingKilometers(currentToolOdometer, now) ?: schedule.intervalKilometers
                val progress = (traveledKm.toFloat() / schedule.intervalKilometers.toFloat()).coerceIn(0f, 1f)

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
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
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "دوره کارکرد: ${JalaliCalendar.toPersianDigits(schedule.intervalKilometers)} ک‌م",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (schedule.dailyKilometers > 0) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "روزانه: ${JalaliCalendar.toPersianDigits(schedule.dailyKilometers)} ک‌م",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            color = if (remainingKm <= 0) StatusOverdueRed else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "طی شده: ${JalaliCalendar.toPersianDigits(traveledKm)} ک‌م",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (remainingKm >= 0) {
                                    "مانده دوره: ${JalaliCalendar.toPersianDigits(remainingKm)} ک‌م"
                                } else {
                                    "عبور از حد: ${JalaliCalendar.toPersianDigits(-remainingKm)} ک‌م"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (remainingKm <= 0) StatusOverdueRed else MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        if (schedule.lastServiceOdometerKm > 0 || schedule.nextServiceOdometerKm > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (schedule.lastServiceOdometerKm > 0) {
                                    Text(
                                        text = "قبلی: ${JalaliCalendar.toPersianDigits(schedule.lastServiceOdometerKm)} ک‌م",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (schedule.nextServiceOdometerKm > 0) {
                                    Text(
                                        text = "موعد بعدی: ${JalaliCalendar.toPersianDigits(schedule.nextServiceOdometerKm)} ک‌م",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Technician & Warranty Row (balanced)
            if (schedule.technicianName.isNotBlank() || schedule.technicianPhone.isNotBlank() || schedule.expiryDateJalali.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (schedule.technicianName.isNotBlank() || schedule.technicianPhone.isNotBlank()) {
                        DetailSpecTile(
                            icon = Icons.Default.Person,
                            label = "سرویس‌کار / مرکز",
                            value = listOfNotNull(
                                schedule.technicianName.takeIf { it.isNotBlank() },
                                schedule.technicianPhone.takeIf { it.isNotBlank() }
                            ).joinToString(" • "),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (schedule.expiryDateJalali.isNotBlank()) {
                        DetailSpecTile(
                            icon = Icons.Default.Schedule,
                            label = "انقضای گارانتی / بیمه",
                            value = JalaliCalendar.toPersianDigits(schedule.expiryDateJalali),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (schedule.notes.isNotBlank()) {
                Text(
                    text = schedule.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Action Buttons Footer (Unbroken "ثبت انجام" button on left, Edit/Delete on right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clickable(onClick = onEdit)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "ویرایش برنامه",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "ویرایش",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        modifier = Modifier.clickable(onClick = onDelete)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف برنامه",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "حذف",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Unbroken "ثبت انجام" button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (status == ServiceStatus.OVERDUE) StatusOverdueRed else MaterialTheme.colorScheme.primary,
                    shadowElevation = 2.dp,
                    modifier = Modifier.clickable(onClick = onMarkDone)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "ثبت انجام",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceLogCard(
    log: ServiceLogEntity,
    isVehicleTool: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusUpToDateGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = log.serviceTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = JalaliCalendar.toPersianDigits(log.performedDateJalali),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // 2-column log info grid
            if (log.actualCost > 0 || (isVehicleTool && log.performedOdometerKm > 0)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (log.actualCost > 0) {
                        DetailSpecTile(
                            icon = Icons.Default.AttachMoney,
                            label = "هزینه پرداختی",
                            value = JalaliCalendar.formatPrice(log.actualCost),
                            valueColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (isVehicleTool && log.performedOdometerKm > 0) {
                        DetailSpecTile(
                            icon = Icons.Default.DirectionsCar,
                            label = "کارکرد هنگام سرویس",
                            value = "${JalaliCalendar.toPersianDigits(log.performedOdometerKm)} ک‌م",
                            valueColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (log.technicianOrShop.isNotBlank() || log.invoiceNumber.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (log.technicianOrShop.isNotBlank()) {
                        DetailSpecTile(
                            icon = Icons.Default.Person,
                            label = "سرویس‌کار",
                            value = log.technicianOrShop,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (log.invoiceNumber.isNotBlank()) {
                        DetailSpecTile(
                            icon = Icons.Default.Pin,
                            label = "شماره فاکتور",
                            value = log.invoiceNumber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (log.partsReplaced.isNotBlank()) {
                Text(
                    text = "قطعات تعویضی: ${log.partsReplaced}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (log.notes.isNotBlank()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Text(
                    text = log.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
