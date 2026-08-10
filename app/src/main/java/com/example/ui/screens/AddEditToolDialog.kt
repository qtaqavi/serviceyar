package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ToolCategory
import com.example.data.model.ToolEntity
import com.example.ui.components.JalaliDatePickerDialog
import com.example.util.JalaliCalendar
import com.example.util.JalaliDate

@Composable
fun AddEditToolDialog(
    toolToEdit: ToolEntity? = null,
    onDismissRequest: () -> Unit,
    onSaveTool: (ToolEntity) -> Unit
) {
    var name by remember { mutableStateOf(toolToEdit?.name ?: "") }
    var selectedCategory by remember {
        mutableStateOf(toolToEdit?.getCategory() ?: ToolCategory.HOME_APPLIANCE)
    }
    var modelOrBrand by remember { mutableStateOf(toolToEdit?.modelOrBrand ?: "") }
    var location by remember { mutableStateOf(toolToEdit?.location ?: "") }
    var serialNumber by remember { mutableStateOf(toolToEdit?.serialNumber ?: "") }
    var purchaseDateJalali by remember {
        mutableStateOf(toolToEdit?.purchaseDateJalali ?: JalaliCalendar.now().toStandardString())
    }
    var purchasePriceStr by remember {
        mutableStateOf(if ((toolToEdit?.purchasePrice ?: 0L) > 0) toolToEdit!!.purchasePrice.toString() else "")
    }
    var notes by remember { mutableStateOf(toolToEdit?.notes ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

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
                        imageVector = if (toolToEdit == null) Icons.Default.Add else Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (toolToEdit == null) "افزودن وسیله یا تجهیز جدید" else "ویرایش مشخصات وسیله",
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
                    // Tool Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (nameError && it.isNotBlank()) nameError = false
                        },
                        label = { Text("نام وسیله یا تجهیز (الزامی) *") },
                        placeholder = { Text("مثال: پکیج دیواری، خودرو پژو، دریل بتن‌کن") },
                        isError = nameError,
                        supportingText = {
                            if (nameError) Text("لطفاً نام وسیله را وارد کنید", color = MaterialTheme.colorScheme.error)
                        },
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Selector
                    Text(
                        text = "دسته‌بندی وسیله:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(ToolCategory.entries) { category ->
                            val isSelected = category == selectedCategory
                            val categoryColor = Color(category.colorHex)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                leadingIcon = {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else categoryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = category.titlePersian,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = categoryColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Model and Brand
                    OutlinedTextField(
                        value = modelOrBrand,
                        onValueChange = { modelOrBrand = it },
                        label = { Text("مدل یا برند") },
                        placeholder = { Text("مثال: بوتان پرلا پرو ۲۴ هزار / بوش اصل") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Location
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("محل نگهداری / قرارگیری") },
                        placeholder = { Text("مثال: آشپزخانه، پارکینگ، قفسه ابزار کارگاه") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Serial Number / Asset Tag
                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("شماره سریال یا کد اموال") },
                        placeholder = { Text("مثال: SN-984022-X") },
                        leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Purchase Date (Jalali)
                    OutlinedTextField(
                        value = JalaliCalendar.toPersianDigits(purchaseDateJalali),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("تاریخ خرید (هجری شمسی)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "انتخاب تاریخ",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    )

                    // Purchase Price
                    OutlinedTextField(
                        value = purchasePriceStr,
                        onValueChange = { purchasePriceStr = JalaliCalendar.toEnglishDigits(it).filter { ch -> ch.isDigit() } },
                        label = { Text("قیمت خرید (تومان - اختیاری)") },
                        placeholder = { Text("مثال: 15000000") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        supportingText = {
                            val amount = purchasePriceStr.toLongOrNull() ?: 0L
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

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("یادداشت‌ها و نکات فنی") },
                        placeholder = { Text("توضیحات، شرایط گارانتی یا نکات مهم نگهداری...") },
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
                        if (name.isBlank()) {
                            nameError = true
                            return@Button
                        }
                        val price = purchasePriceStr.toLongOrNull() ?: 0L
                        val newTool = (toolToEdit ?: ToolEntity(name = name.trim())).copy(
                            name = name.trim(),
                            categoryName = selectedCategory.name,
                            modelOrBrand = modelOrBrand.trim(),
                            location = location.trim(),
                            serialNumber = serialNumber.trim(),
                            purchaseDateJalali = purchaseDateJalali,
                            purchasePrice = price,
                            notes = notes.trim()
                        )
                        onSaveTool(newTool)
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ذخیره وسیله", fontWeight = FontWeight.Bold)
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
            val initialDate = JalaliCalendar.parse(purchaseDateJalali) ?: JalaliCalendar.now()
            JalaliDatePickerDialog(
                initialDate = initialDate,
                title = "انتخاب تاریخ خرید شمسی",
                onDismissRequest = { showDatePicker = false },
                onDateSelected = { selectedDate ->
                    purchaseDateJalali = selectedDate.toStandardString()
                    showDatePicker = false
                }
            )
        }
    }
}
