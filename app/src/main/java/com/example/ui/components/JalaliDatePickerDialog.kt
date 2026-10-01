package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.util.JalaliCalendar
import com.example.util.JalaliDate

@Composable
fun JalaliDatePickerDialog(
    initialDate: JalaliDate = JalaliCalendar.now(),
    title: String = "انتخاب تاریخ شمسی",
    onDismissRequest: () -> Unit,
    onDateSelected: (JalaliDate) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(initialDate.year) }
    var selectedMonth by remember { mutableIntStateOf(initialDate.month) }
    var selectedDay by remember { mutableIntStateOf(initialDate.day) }

    val daysInMonth = JalaliCalendar.getDaysInMonth(selectedYear, selectedMonth)
    if (selectedDay > daysInMonth) {
        selectedDay = daysInMonth
    }

    val currentSelection = JalaliDate(selectedYear, selectedMonth, selectedDay)
    var showYearPicker by remember { mutableStateOf(false) }
    val now = JalaliCalendar.now()
    val minYear = 1340
    val maxYear = (now.year + 10).coerceAtLeast(1415)
    val allYears = remember(minYear, maxYear) {
        (maxYear downTo minYear).toList()
    }
    var selectedDecadeStart by remember { mutableStateOf<Int?>(null) }
    val filteredYears = remember(allYears, selectedDecadeStart) {
        val decade = selectedDecadeStart
        if (decade == null) {
            allYears
        } else {
            allYears.filter { it in decade..(decade + 9) }
        }
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
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Selected Date Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "تاریخ انتخاب شده",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = currentSelection.format(includeDayName = true),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (showYearPicker) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                modifier = Modifier.clickable {
                                    showYearPicker = !showYearPicker
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "سال ${JalaliCalendar.toPersianDigits(selectedYear)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (showYearPicker) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        }
                                    )
                                    Icon(
                                        imageVector = if (showYearPicker) {
                                            Icons.Default.KeyboardArrowUp
                                        } else {
                                            Icons.Default.KeyboardArrowDown
                                        },
                                        contentDescription = "انتخاب سال",
                                        tint = if (showYearPicker) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Quick Preset Chips
                    Text(
                        text = "میانبرهای زمانی:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        item {
                            PresetChip("امروز") {
                                selectedYear = now.year
                                selectedMonth = now.month
                                selectedDay = now.day
                                showYearPicker = false
                            }
                        }
                        item {
                            PresetChip("۱ ماه بعد") {
                                val next = JalaliCalendar.addMonths(now, 1)
                                selectedYear = next.year
                                selectedMonth = next.month
                                selectedDay = next.day
                                showYearPicker = false
                            }
                        }
                        item {
                            PresetChip("۳ ماه بعد (فصلی)") {
                                val next = JalaliCalendar.addMonths(now, 3)
                                selectedYear = next.year
                                selectedMonth = next.month
                                selectedDay = next.day
                                showYearPicker = false
                            }
                        }
                        item {
                            PresetChip("۶ ماه بعد (نیم‌سال)") {
                                val next = JalaliCalendar.addMonths(now, 6)
                                selectedYear = next.year
                                selectedMonth = next.month
                                selectedDay = next.day
                                showYearPicker = false
                            }
                        }
                        item {
                            PresetChip("۱ سال بعد (سالانه)") {
                                val next = JalaliCalendar.addMonths(now, 12)
                                selectedYear = next.year
                                selectedMonth = next.month
                                selectedDay = next.day
                                showYearPicker = false
                            }
                        }
                    }

                    // Month & Year Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Month
                        IconButton(
                            onClick = {
                                if (selectedMonth > 1) {
                                    selectedMonth--
                                } else if (selectedYear > minYear) {
                                    selectedMonth = 12
                                    selectedYear--
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ماه قبل"
                            )
                        }

                        // Month Name
                        Text(
                            text = JalaliCalendar.getMonthName(selectedMonth),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Year Selector Pill (Toggles Year Grid 1340..maxYear)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (showYearPicker) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = if (showYearPicker) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { showYearPicker = !showYearPicker }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "سال: ${JalaliCalendar.toPersianDigits(selectedYear)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showYearPicker) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Icon(
                                    imageVector = if (showYearPicker) {
                                        Icons.Default.KeyboardArrowUp
                                    } else {
                                        Icons.Default.KeyboardArrowDown
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (showYearPicker) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        // Next Month
                        IconButton(
                            onClick = {
                                if (selectedMonth < 12) {
                                    selectedMonth++
                                } else if (selectedYear < maxYear) {
                                    selectedMonth = 1
                                    selectedYear++
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "ماه بعد"
                            )
                        }
                    }

                    if (showYearPicker) {
                        // Decade Filter Row (1340 to 1410+)
                        val decades = remember {
                            listOf(1400, 1390, 1380, 1370, 1360, 1350, 1340)
                        }
                        Text(
                            text = "انتخاب سال (از ۱۳۴۰ تا ${JalaliCalendar.toPersianDigits(maxYear)}):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedDecadeStart == null,
                                    onClick = { selectedDecadeStart = null },
                                    label = {
                                        Text(
                                            text = "همه سال‌ها",
                                            fontSize = 11.5.sp,
                                            fontWeight = if (selectedDecadeStart == null) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                            items(decades) { decade ->
                                val isSelected = selectedDecadeStart == decade
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedDecadeStart = if (isSelected) null else decade
                                    },
                                    label = {
                                        Text(
                                            text = "دهه ${JalaliCalendar.toPersianDigits(decade)}",
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }

                        val yearGridState = rememberLazyGridState()
                        LaunchedEffect(selectedDecadeStart) {
                            val idx = filteredYears.indexOf(selectedYear)
                            if (idx >= 0) {
                                yearGridState.scrollToItem((idx - 4).coerceAtLeast(0))
                            }
                        }

                        LazyVerticalGrid(
                            state = yearGridState,
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredYears, key = { it }) { y ->
                                val isSelected = y == selectedYear
                                val isCurrentYear = y == now.year
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isCurrentYear -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedYear = y
                                            showYearPicker = false
                                        }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = JalaliCalendar.toPersianDigits(y),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected || isCurrentYear) FontWeight.Bold else FontWeight.Medium,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                isCurrentYear -> MaterialTheme.colorScheme.onSecondaryContainer
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Month Names Selector Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(12) { index ->
                                val m = index + 1
                                val isSelected = m == selectedMonth
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedMonth = m },
                                    label = {
                                        Text(
                                            text = JalaliCalendar.getMonthName(m),
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        // Day Grid
                        val maxDays = JalaliCalendar.getDaysInMonth(selectedYear, selectedMonth)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(maxDays) { index ->
                                val dayNum = index + 1
                                val isSelected = dayNum == selectedDay
                                val isToday = (selectedYear == now.year && selectedMonth == now.month && dayNum == now.day)

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.secondaryContainer
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isToday && !isSelected) 1.dp else 0.dp,
                                            color = if (isToday && !isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            selectedDay = dayNum
                                        }
                                ) {
                                    Text(
                                        text = JalaliCalendar.toPersianDigits(dayNum),
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDateSelected(currentSelection)
                        onDismissRequest()
                    },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
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
                    AutoResizedButtonText("تأیید تاریخ", maxFontSize = 12.5.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismissRequest,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    AutoResizedButtonText("انصراف", maxFontSize = 12.5.sp)
                }
            }
        )
    }
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}
