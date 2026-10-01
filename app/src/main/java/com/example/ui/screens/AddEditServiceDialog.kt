package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.IntervalType
import com.example.data.model.ServicePriority
import com.example.data.model.ServiceScheduleEntity
import com.example.data.model.ServiceType
import com.example.data.model.ToolEntity
import com.example.ui.components.JalaliDatePickerDialog
import com.example.util.JalaliCalendar
import com.example.util.JalaliDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditServiceDialog(
    tool: ToolEntity,
    scheduleToEdit: ServiceScheduleEntity? = null,
    allTools: List<ToolEntity> = emptyList(),
    onDismissRequest: () -> Unit,
    onSaveSchedule: (ServiceScheduleEntity) -> Unit
) {
    var selectedTool by remember { mutableStateOf(tool) }
    var title by remember { mutableStateOf(scheduleToEdit?.title ?: "") }
    var selectedServiceType by remember {
        mutableStateOf(scheduleToEdit?.getServiceType() ?: ServiceType.PERIODIC_GENERAL)
    }
    var selectedIntervalType by remember {
        mutableStateOf(scheduleToEdit?.getIntervalType() ?: IntervalType.ANNUAL)
    }
    var customDaysStr by remember {
        mutableStateOf((scheduleToEdit?.customIntervalDays ?: 30).toString())
    }

    val isVehicle = selectedTool.isVehicle()
    var intervalKmStr by remember {
        mutableStateOf(
            if ((scheduleToEdit?.intervalKilometers ?: 0) > 0) {
                scheduleToEdit!!.intervalKilometers.toString()
            } else if (isVehicle) "5000" else ""
        )
    }
    var dailyKmStr by remember {
        mutableStateOf(
            if ((scheduleToEdit?.dailyKilometers ?: 0) > 0) {
                scheduleToEdit!!.dailyKilometers.toString()
            } else if (isVehicle) "50" else ""
        )
    }
    var lastKmStr by remember {
        mutableStateOf(
            if ((scheduleToEdit?.lastServiceOdometerKm ?: 0) > 0) {
                scheduleToEdit!!.lastServiceOdometerKm.toString()
            } else if (isVehicle && selectedTool.currentOdometerKm > 0) {
                selectedTool.currentOdometerKm.toString()
            } else ""
        )
    }
    var nextKmStr by remember {
        mutableStateOf(
            if ((scheduleToEdit?.nextServiceOdometerKm ?: 0) > 0) {
                scheduleToEdit!!.nextServiceOdometerKm.toString()
            } else ""
        )
    }

    val now = JalaliCalendar.now()
    var lastServiceDateJalali by remember {
        mutableStateOf(scheduleToEdit?.lastServiceDateJalali ?: now.toStandardString())
    }
    var nextServiceDateJalali by remember {
        val initialNext = scheduleToEdit?.nextServiceDateJalali ?: run {
            val initIntervalKm = intervalKmStr.toIntOrNull() ?: 0
            val initDailyKm = dailyKmStr.toIntOrNull() ?: 0
            if (isVehicle && initIntervalKm > 0 && initDailyKm > 0) {
                val cycleDays = ((initIntervalKm + initDailyKm - 1) / initDailyKm).coerceAtLeast(1)
                JalaliCalendar.addDays(now, cycleDays).toStandardString()
            } else {
                JalaliCalendar.addMonths(now, 12).toStandardString()
            }
        }
        mutableStateOf(initialNext)
    }
    var expiryDateJalali by remember {
        mutableStateOf(scheduleToEdit?.expiryDateJalali ?: "")
    }

    var selectedPriority by remember {
        mutableStateOf(scheduleToEdit?.getPriority() ?: ServicePriority.MEDIUM)
    }
    var estimatedCostStr by remember {
        mutableStateOf(if ((scheduleToEdit?.estimatedCost ?: 0L) > 0) scheduleToEdit!!.estimatedCost.toString() else "")
    }
    var technicianName by remember { mutableStateOf(scheduleToEdit?.technicianName ?: "") }
    var technicianPhone by remember { mutableStateOf(scheduleToEdit?.technicianPhone ?: "") }
    var reminderDaysBefore by remember { mutableIntStateOf(scheduleToEdit?.reminderDaysBefore ?: 7) }
    var notes by remember { mutableStateOf(scheduleToEdit?.notes ?: "") }

    var toolDropdownExpanded by remember { mutableStateOf(false) }
    var priorityDropdownExpanded by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf<DatePickerTarget?>(null) }
    var titleError by remember { mutableStateOf(false) }

    fun syncMileageToNextDate(newIntervalKmStr: String = intervalKmStr, newDailyKmStr: String = dailyKmStr) {
        val intervalKm = newIntervalKmStr.toIntOrNull() ?: 0
        val dailyKm = newDailyKmStr.toIntOrNull() ?: 0
        if (intervalKm > 0 && dailyKm > 0) {
            val cycleDays = ((intervalKm + dailyKm - 1) / dailyKm).coerceAtLeast(1)
            val lastDate = JalaliCalendar.parse(lastServiceDateJalali) ?: now
            selectedIntervalType = IntervalType.CUSTOM_DAYS
            customDaysStr = cycleDays.toString()
            nextServiceDateJalali = JalaliCalendar.addDays(lastDate, cycleDays).toStandardString()
        }
    }

    fun calculateNextDateAutomatically() {
        val lastDate = JalaliCalendar.parse(lastServiceDateJalali) ?: now
        val intervalKm = intervalKmStr.toIntOrNull() ?: 0
        val dailyKm = dailyKmStr.toIntOrNull() ?: 0
        if (isVehicle && intervalKm > 0 && dailyKm > 0 && selectedIntervalType == IntervalType.CUSTOM_DAYS) {
            val cycleDays = ((intervalKm + dailyKm - 1) / dailyKm).coerceAtLeast(1)
            customDaysStr = cycleDays.toString()
            nextServiceDateJalali = JalaliCalendar.addDays(lastDate, cycleDays).toStandardString()
            return
        }
        val next = when (selectedIntervalType) {
            IntervalType.MONTHLY -> JalaliCalendar.addMonths(lastDate, 1)
            IntervalType.QUARTERLY -> JalaliCalendar.addMonths(lastDate, 3)
            IntervalType.BIANNUAL -> JalaliCalendar.addMonths(lastDate, 6)
            IntervalType.ANNUAL -> JalaliCalendar.addMonths(lastDate, 12)
            IntervalType.BIENNIAL -> JalaliCalendar.addMonths(lastDate, 24)
            IntervalType.CUSTOM_DAYS -> {
                val days = customDaysStr.toIntOrNull() ?: 30
                JalaliCalendar.addDays(lastDate, days)
            }
        }
        nextServiceDateJalali = next.toStandardString()
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            shape = RoundedCornerShape(24.dp),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.95f),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (scheduleToEdit == null) Icons.Default.Add else Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (scheduleToEdit == null) "تعریف برنامه سرویس و نگهداری" else "ویرایش برنامه سرویس",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tool Selection (if multiple tools available)
                    if (allTools.size > 1) {
                        ExposedDropdownMenuBox(
                            expanded = toolDropdownExpanded,
                            onExpandedChange = { toolDropdownExpanded = !toolDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedTool.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("وسیله یا تجهیز مربوطه") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = toolDropdownExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = toolDropdownExpanded,
                                onDismissRequest = { toolDropdownExpanded = false }
                            ) {
                                allTools.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t.name) },
                                        onClick = {
                                            selectedTool = t
                                            if (!t.isVehicle()) {
                                                intervalKmStr = ""
                                                dailyKmStr = ""
                                                lastKmStr = ""
                                                nextKmStr = ""
                                            } else if (intervalKmStr.isBlank()) {
                                                intervalKmStr = "5000"
                                                dailyKmStr = "50"
                                                if (t.currentOdometerKm > 0) {
                                                    lastKmStr = t.currentOdometerKm.toString()
                                                    nextKmStr = (t.currentOdometerKm + 5000).toString()
                                                }
                                            }
                                            toolDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "وسیله: ${selectedTool.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Service Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (titleError && it.isNotBlank()) titleError = false
                        },
                        label = { Text("عنوان سرویس (الزامی) *") },
                        placeholder = { Text("مثال: تعویض روغن موتور، سرویس سالانه پکیج، تعویض فیلتر") },
                        isError = titleError,
                        supportingText = {
                            if (titleError) Text("لطفاً عنوان سرویس را وارد کنید", color = MaterialTheme.colorScheme.error)
                        },
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Service Type Chips
                    Text(
                        text = "نوع عملیات سرویس:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(ServiceType.entries) { st ->
                            val isSelected = st == selectedServiceType
                            val badgeColor = Color(st.badgeColorHex)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedServiceType = st },
                                leadingIcon = {
                                    Icon(
                                        imageVector = st.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else badgeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = st.titlePersian,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = badgeColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Interval Selection
                    Text(
                        text = "دوره زمانی تکرار سرویس:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(IntervalType.entries) { interval ->
                            val isSelected = interval == selectedIntervalType
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedIntervalType = interval
                                    calculateNextDateAutomatically()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = interval.titlePersian,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            )
                        }
                    }

                    if (selectedIntervalType == IntervalType.CUSTOM_DAYS) {
                        OutlinedTextField(
                            value = customDaysStr,
                            onValueChange = {
                                customDaysStr = it.filter { ch -> ch.isDigit() }
                                calculateNextDateAutomatically()
                            },
                            label = { Text("تعداد روزهای دوره تکرار") },
                            placeholder = { Text("مثال: 45 روز") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Last Service Date (Jalali)
                    OutlinedTextField(
                        value = JalaliCalendar.toPersianDigits(lastServiceDateJalali),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("تاریخ آخرین سرویس انجام شده (شمسی)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { datePickerTarget = DatePickerTarget.LAST_SERVICE }) {
                                Icon(Icons.Default.Edit, contentDescription = "انتخاب تاریخ")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerTarget = DatePickerTarget.LAST_SERVICE }
                    )

                    // Next Service Date (Jalali)
                    OutlinedTextField(
                        value = JalaliCalendar.toPersianDigits(nextServiceDateJalali),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("موعد سرویس بعدی (هجری شمسی) *") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { calculateNextDateAutomatically() }) {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = "محاسبه خودکار",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { datePickerTarget = DatePickerTarget.NEXT_SERVICE }) {
                                    Icon(Icons.Default.Edit, contentDescription = "انتخاب دستی تاریخ")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerTarget = DatePickerTarget.NEXT_SERVICE }
                    )

                    // Expiry / Warranty Date (Optional)
                    OutlinedTextField(
                        value = if (expiryDateJalali.isBlank()) "تعیین نشده" else JalaliCalendar.toPersianDigits(expiryDateJalali),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("تاریخ انقضا / اتمام گارانتی و بیمه (اختیاری)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        },
                        trailingIcon = {
                            Row {
                                if (expiryDateJalali.isNotBlank()) {
                                    IconButton(onClick = { expiryDateJalali = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "حذف تاریخ انقضا")
                                    }
                                }
                                IconButton(onClick = { datePickerTarget = DatePickerTarget.EXPIRY }) {
                                    Icon(Icons.Default.Edit, contentDescription = "انتخاب تاریخ")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerTarget = DatePickerTarget.EXPIRY }
                    )

                    // Vehicle Mileage / Kilometer Tracking (without cluttering preset buttons)
                    if (isVehicle) {
                        Text(
                            text = "دوره تکرار کیلومتری و پیمایش روزانه خودرو:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = intervalKmStr,
                            onValueChange = {
                                val cleaned = JalaliCalendar.toEnglishDigits(it).filter { ch -> ch.isDigit() }
                                intervalKmStr = cleaned
                                val interval = cleaned.toIntOrNull() ?: 0
                                val lastKm = lastKmStr.toIntOrNull() ?: selectedTool.currentOdometerKm
                                if (interval > 0) {
                                    nextKmStr = (lastKm + interval).toString()
                                }
                                syncMileageToNextDate(newIntervalKmStr = cleaned, newDailyKmStr = dailyKmStr)
                            },
                            label = { Text("دوره کارکرد کیلومتری سرویس (کیلومتر)") },
                            placeholder = { Text("مثال: 5000") },
                            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Daily Mileage Input (پیمایش روزانه خودرو)
                        OutlinedTextField(
                            value = dailyKmStr,
                            onValueChange = {
                                val cleaned = JalaliCalendar.toEnglishDigits(it).filter { ch -> ch.isDigit() }
                                dailyKmStr = cleaned
                                syncMileageToNextDate(newIntervalKmStr = intervalKmStr, newDailyKmStr = cleaned)
                            },
                            label = { Text("پیمایش روزانه خودرو برای این سرویس (کیلومتر در روز)") },
                            placeholder = { Text("مثال: 50 کیلومتر در روز") },
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                            supportingText = {
                                val intervalKmVal = intervalKmStr.toIntOrNull() ?: 0
                                val dailyKmVal = dailyKmStr.toIntOrNull() ?: 0
                                if (intervalKmVal > 0 && dailyKmVal > 0) {
                                    val totalDays = ((intervalKmVal + dailyKmVal - 1) / dailyKmVal).coerceAtLeast(1)
                                    Text(
                                        text = "روزانه ${JalaliCalendar.toPersianDigits(dailyKmVal)} ک‌م از دوره ${JalaliCalendar.toPersianDigits(intervalKmVal)} ک‌م کسر شده و پس از ${JalaliCalendar.toPersianDigits(totalDays)} روز (موعد: ${JalaliCalendar.toPersianDigits(nextServiceDateJalali)}) به حد نصاب کارکرد می‌رسد.",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = lastKmStr,
                                onValueChange = {
                                    val cleaned = JalaliCalendar.toEnglishDigits(it).filter { ch -> ch.isDigit() }
                                    lastKmStr = cleaned
                                    val last = cleaned.toIntOrNull() ?: 0
                                    val interval = intervalKmStr.toIntOrNull() ?: 0
                                    if (interval > 0 && last > 0) {
                                        nextKmStr = (last + interval).toString()
                                    }
                                },
                                label = { Text("کیلومتر آخرین سرویس") },
                                placeholder = { Text("مثال: 74000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = nextKmStr,
                                onValueChange = {
                                    nextKmStr = JalaliCalendar.toEnglishDigits(it).filter { ch -> ch.isDigit() }
                                },
                                label = { Text("کیلومتر موعد بعدی") },
                                placeholder = { Text("مثال: 79000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Priority Dropdown Menu (منوی پایین‌افتادنی میزان اهمیت و فوریت سرویس)
                    ExposedDropdownMenuBox(
                        expanded = priorityDropdownExpanded,
                        onExpandedChange = { priorityDropdownExpanded = !priorityDropdownExpanded }
                    ) {
                        val selectedPrioColor = Color(selectedPriority.colorHex)
                        OutlinedTextField(
                            value = selectedPriority.titlePersian,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("میزان اهمیت و فوریت سرویس") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PriorityHigh,
                                    contentDescription = null,
                                    tint = selectedPrioColor
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityDropdownExpanded)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = priorityDropdownExpanded,
                            onDismissRequest = { priorityDropdownExpanded = false }
                        ) {
                            ServicePriority.entries.forEach { prio ->
                                val prioColor = Color(prio.colorHex)
                                val isSelected = prio == selectedPriority
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = prio.titlePersian,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) prioColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PriorityHigh,
                                            contentDescription = null,
                                            tint = prioColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = prioColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        selectedPriority = prio
                                        priorityDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Estimated Cost
                    OutlinedTextField(
                        value = estimatedCostStr,
                        onValueChange = { estimatedCostStr = JalaliCalendar.toEnglishDigits(it).filter { ch -> ch.isDigit() } },
                        label = { Text("هزینه تقریبی سرویس (تومان)") },
                        placeholder = { Text("مثال: 500000") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        supportingText = {
                            val amount = estimatedCostStr.toLongOrNull() ?: 0L
                            if (amount > 0) {
                                Text(
                                    text = JalaliCalendar.formatPrice(amount),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Technician info
                    OutlinedTextField(
                        value = technicianName,
                        onValueChange = { technicianName = it },
                        label = { Text("نام سرویس‌کار یا مرکز تخصصی") },
                        placeholder = { Text("مثال: مهندس اکبری (نمایندگی بوتان)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = technicianPhone,
                        onValueChange = { technicianPhone = it },
                        label = { Text("شماره تماس سرویس‌کار") },
                        placeholder = { Text("مثال: 09123456789") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Reminder Days Before
                    Text(
                        text = "زمان ارسال یادآوری:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        val reminderOptions = listOf(0 to "همان روز", 1 to "۱ روز قبل", 3 to "۳ روز قبل", 7 to "۱ هفته قبل", 15 to "۱۵ روز قبل", 30 to "۱ ماه قبل")
                        items(reminderOptions) { (days, label) ->
                            val isSelected = days == reminderDaysBefore
                            FilterChip(
                                selected = isSelected,
                                onClick = { reminderDaysBefore = days },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            )
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("توضیحات و نکات تکمیلی") },
                        placeholder = { Text("شماره قطعات، استانداردها، گریس مخصوص یا توصیه‌های فنی...") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            titleError = true
                            return@Button
                        }
                        val cost = estimatedCostStr.toLongOrNull() ?: 0L
                        val days = customDaysStr.toIntOrNull() ?: 30
                        val intervalKm = if (isVehicle) (intervalKmStr.toIntOrNull() ?: 0) else 0
                        val dailyKm = if (isVehicle) (dailyKmStr.toIntOrNull() ?: 0) else 0
                        val lastKm = if (isVehicle) (lastKmStr.toIntOrNull() ?: 0) else 0
                        val nextKm = if (isVehicle) (nextKmStr.toIntOrNull() ?: 0) else 0

                        val newSchedule = (scheduleToEdit ?: ServiceScheduleEntity(
                            toolId = selectedTool.id,
                            toolName = selectedTool.name,
                            title = title.trim()
                        )).copy(
                            toolId = selectedTool.id,
                            toolName = selectedTool.name,
                            title = title.trim(),
                            serviceTypeName = selectedServiceType.name,
                            intervalTypeName = selectedIntervalType.name,
                            customIntervalDays = days,
                            intervalKilometers = intervalKm,
                            dailyKilometers = dailyKm,
                            lastServiceOdometerKm = lastKm,
                            nextServiceOdometerKm = nextKm,
                            lastServiceDateJalali = lastServiceDateJalali,
                            nextServiceDateJalali = nextServiceDateJalali,
                            expiryDateJalali = expiryDateJalali,
                            priorityName = selectedPriority.name,
                            estimatedCost = cost,
                            technicianName = technicianName.trim(),
                            technicianPhone = technicianPhone.trim(),
                            reminderDaysBefore = reminderDaysBefore,
                            notes = notes.trim()
                        )
                        onSaveSchedule(newSchedule)
                        onDismissRequest()
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    com.example.ui.components.AutoResizedButtonText("ذخیره برنامه سرویس", maxFontSize = 12.5.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismissRequest,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    com.example.ui.components.AutoResizedButtonText("انصراف", maxFontSize = 12.5.sp)
                }
            }
        )

        // Date Picker Modal Dialog
        datePickerTarget?.let { target ->
            val initial = when (target) {
                DatePickerTarget.LAST_SERVICE -> JalaliCalendar.parse(lastServiceDateJalali) ?: now
                DatePickerTarget.NEXT_SERVICE -> JalaliCalendar.parse(nextServiceDateJalali) ?: now
                DatePickerTarget.EXPIRY -> JalaliCalendar.parse(expiryDateJalali) ?: JalaliCalendar.addMonths(now, 12)
            }
            val pickerTitle = when (target) {
                DatePickerTarget.LAST_SERVICE -> "انتخاب تاریخ آخرین سرویس"
                DatePickerTarget.NEXT_SERVICE -> "انتخاب موعد سرویس بعدی"
                DatePickerTarget.EXPIRY -> "انتخاب تاریخ انقضا یا گارانتی"
            }

            JalaliDatePickerDialog(
                initialDate = initial,
                title = pickerTitle,
                onDismissRequest = { datePickerTarget = null },
                onDateSelected = { selectedDate ->
                    when (target) {
                        DatePickerTarget.LAST_SERVICE -> {
                            lastServiceDateJalali = selectedDate.toStandardString()
                            calculateNextDateAutomatically()
                        }
                        DatePickerTarget.NEXT_SERVICE -> {
                            nextServiceDateJalali = selectedDate.toStandardString()
                        }
                        DatePickerTarget.EXPIRY -> {
                            expiryDateJalali = selectedDate.toStandardString()
                        }
                    }
                    datePickerTarget = null
                }
            )
        }
    }
}

private enum class DatePickerTarget {
    LAST_SERVICE,
    NEXT_SERVICE,
    EXPIRY
}
