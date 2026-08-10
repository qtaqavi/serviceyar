package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.IntervalType
import com.example.data.model.ServiceScheduleEntity
import com.example.ui.components.JalaliDatePickerDialog
import com.example.util.JalaliCalendar
import com.example.util.JalaliDate

@Composable
fun RecordServiceDoneDialog(
    schedule: ServiceScheduleEntity,
    onDismissRequest: () -> Unit,
    onConfirmDone: (performedDateJalali: String, actualCost: Long, technician: String, invoiceNo: String, partsReplaced: String, notes: String) -> Unit
) {
    val now = JalaliCalendar.now()
    var performedDateJalali by remember { mutableStateOf(now.toStandardString()) }
    var actualCostStr by remember {
        mutableStateOf(if (schedule.estimatedCost > 0) schedule.estimatedCost.toString() else "")
    }
    var technician by remember { mutableStateOf(schedule.technicianName) }
    var invoiceNumber by remember { mutableStateOf("") }
    var partsReplaced by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }

    val performedJalali = JalaliCalendar.parse(performedDateJalali) ?: now
    val nextEstimatedJalali = when (schedule.getIntervalType()) {
        IntervalType.MONTHLY -> JalaliCalendar.addMonths(performedJalali, 1)
        IntervalType.QUARTERLY -> JalaliCalendar.addMonths(performedJalali, 3)
        IntervalType.BIANNUAL -> JalaliCalendar.addMonths(performedJalali, 6)
        IntervalType.ANNUAL -> JalaliCalendar.addMonths(performedJalali, 12)
        IntervalType.BIENNIAL -> JalaliCalendar.addMonths(performedJalali, 24)
        IntervalType.CUSTOM_DAYS -> JalaliCalendar.addDays(performedJalali, schedule.customIntervalDays.coerceAtLeast(1))
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
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "ثبت انجام سرویس و تمدید موعد",
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
                    // Service Title Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = schedule.toolName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = schedule.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "دوره سرویس: ${schedule.getIntervalType().titlePersian}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // Performed Date (Jalali)
                    OutlinedTextField(
                        value = JalaliCalendar.toPersianDigits(performedDateJalali),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("تاریخ انجام سرویس (هجری شمسی) *") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "انتخاب تاریخ")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    )

                    // Next Date Auto-Advance Information Banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "محاسبه خودکار موعد بعدی:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "موعد سرویس بعدی به «${nextEstimatedJalali.format(includeDayName = false)}» تغییر خواهد یافت.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // Actual Cost Paid
                    OutlinedTextField(
                        value = actualCostStr,
                        onValueChange = { actualCostStr = JalaliCalendar.toEnglishDigits(it).filter { ch -> ch.isDigit() } },
                        label = { Text("هزینه پرداختی واقعی (تومان)") },
                        placeholder = { Text("مثال: 450000") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        supportingText = {
                            val amount = actualCostStr.toLongOrNull() ?: 0L
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

                    // Technician or Shop Name
                    OutlinedTextField(
                        value = technician,
                        onValueChange = { technician = it },
                        label = { Text("تعمیرکار، تکنسین یا نام نمایندگی") },
                        placeholder = { Text("مثال: اتوسرویس بهرام / مهندس اکبری") },
                        leadingIcon = { Icon(Icons.Default.Engineering, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Invoice Number
                    OutlinedTextField(
                        value = invoiceNumber,
                        onValueChange = { invoiceNumber = it },
                        label = { Text("شماره فاکتور، رسید یا کارتکس") },
                        placeholder = { Text("مثال: INV-98402") },
                        leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Parts Replaced
                    OutlinedTextField(
                        value = partsReplaced,
                        onValueChange = { partsReplaced = it },
                        label = { Text("قطعات مصرفی و تعویض شده") },
                        placeholder = { Text("مثال: روغن موتور ۴ لیتری، فیلتر روغن، اورینگ") },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Service Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("توضیحات و گزارش انجام کار") },
                        placeholder = { Text("نکات بررسی شده، وضعیت فشار یا کیلومتر فعلی...") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cost = actualCostStr.toLongOrNull() ?: 0L
                        onConfirmDone(
                            performedDateJalali,
                            cost,
                            technician.trim(),
                            invoiceNumber.trim(),
                            partsReplaced.trim(),
                            notes.trim()
                        )
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ثبت در سوابق و تمدید موعد", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("انصراف")
                }
            }
        )

        if (showDatePicker) {
            JalaliDatePickerDialog(
                initialDate = performedJalali,
                title = "انتخاب تاریخ انجام سرویس",
                onDismissRequest = { showDatePicker = false },
                onDateSelected = { selectedDate ->
                    performedDateJalali = selectedDate.toStandardString()
                    showDatePicker = false
                }
            )
        }
    }
}
