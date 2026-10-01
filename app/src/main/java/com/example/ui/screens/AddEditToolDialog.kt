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
import androidx.compose.material.icons.filled.DirectionsCar
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CategoryIconCatalog
import com.example.data.model.CustomCategoryRegistry
import com.example.data.model.CustomToolCategory
import com.example.data.model.ToolCategory
import com.example.data.model.ToolEntity
import com.example.ui.components.AutoResizedButtonText
import com.example.ui.components.IranianPlate
import com.example.ui.components.IranianPlateEditor
import com.example.ui.components.JalaliDatePickerDialog
import com.example.util.JalaliCalendar
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditToolDialog(
    toolToEdit: ToolEntity? = null,
    customCategories: List<CustomToolCategory> = CustomCategoryRegistry.customCategories,
    onAddCustomCategory: ((CustomToolCategory) -> Unit)? = null,
    onDismissRequest: () -> Unit,
    onSaveTool: (ToolEntity) -> Unit
) {
    var name by remember { mutableStateOf(toolToEdit?.name ?: "") }
    var selectedCategoryKey by remember {
        mutableStateOf(toolToEdit?.categoryName ?: ToolCategory.HOME_APPLIANCE.name)
    }
    var userManuallyPickedCategory by remember { mutableStateOf(toolToEdit != null) }
    val allCategories = remember(customCategories) {
        CustomCategoryRegistry.getAllCategoryModels(customCategories)
    }
    val selectedCategoryUi = remember(selectedCategoryKey, customCategories) {
        CustomCategoryRegistry.resolve(selectedCategoryKey, customCategories)
    }

    var showInlineNewCategory by remember { mutableStateOf(false) }
    var newCategoryTitle by remember { mutableStateOf("") }
    var newCategoryIconKey by remember { mutableStateOf("CATEGORY") }
    var newCategoryColorHex by remember { mutableLongStateOf(CategoryIconCatalog.availableColors.first()) }

    var modelOrBrand by remember { mutableStateOf(toolToEdit?.modelOrBrand ?: "") }
    var location by remember { mutableStateOf(toolToEdit?.location ?: "") }
    var serialNumber by remember { mutableStateOf(toolToEdit?.serialNumber ?: "") }
    var currentOdometerKmStr by remember {
        mutableStateOf(if ((toolToEdit?.currentOdometerKm ?: 0) > 0) toolToEdit!!.currentOdometerKm.toString() else "")
    }
    var iranianPlate by remember {
        mutableStateOf(IranianPlate.parse(toolToEdit?.serialNumber))
    }
    var purchaseDateJalali by remember {
        mutableStateOf(toolToEdit?.purchaseDateJalali ?: JalaliCalendar.now().toStandardString())
    }
    var purchasePriceStr by remember {
        mutableStateOf(if ((toolToEdit?.purchasePrice ?: 0L) > 0) toolToEdit!!.purchasePrice.toString() else "")
    }
    var notes by remember { mutableStateOf(toolToEdit?.notes ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    // Determine whether this tool is a vehicle (خودرو، کامیون، کامیونت، تریلی، موتورسیکلت، ون و ...)
    val isVehicleTool = remember(selectedCategoryUi, name, modelOrBrand) {
        if (ToolCategory.isExplicitlyNonVehicleText(name) || ToolCategory.isExplicitlyNonVehicleText(modelOrBrand)) {
            false
        } else {
            selectedCategoryUi.isVehicleType || ToolCategory.isVehicleText(name) || ToolCategory.isVehicleText(modelOrBrand)
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tool Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (nameError && it.isNotBlank()) nameError = false
                            if (!userManuallyPickedCategory) {
                                when {
                                    ToolCategory.isVehicleText(it) -> {
                                        selectedCategoryKey = ToolCategory.VEHICLE.name
                                    }
                                    it.contains("کولر") || it.contains("اسپلیت") || it.contains("پکیج") ||
                                        it.contains("موتورخانه") || it.contains("آبگرمکن") -> {
                                        selectedCategoryKey = ToolCategory.FACILITY_HVAC.name
                                    }
                                    it.contains("تصفیه آب") || it.contains("آب تصفیه") || it.contains("یخچال") ||
                                        it.contains("لباسشویی") || it.contains("ظرفشویی") || it.contains("جاروبرقی") -> {
                                        selectedCategoryKey = ToolCategory.HOME_APPLIANCE.name
                                    }
                                }
                            }
                        },
                        label = { Text("نام وسیله یا تجهیز (الزامی) *") },
                        placeholder = { Text("مثال: خودرو پژو، کامیونت، موتورسیکلت، کولر گازی، تصفیه آب") },
                        isError = nameError,
                        supportingText = if (nameError) {
                            { Text("لطفاً نام وسیله را وارد کنید", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "دسته‌بندی وسیله:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        if (onAddCustomCategory != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clickable {
                                    showInlineNewCategory = !showInlineNewCategory
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    AutoResizedButtonText(
                                        text = "دسته‌بندی جدید",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxFontSize = 11.sp,
                                        minFontSize = 8.sp
                                    )
                                }
                            }
                        }
                    }

                    if (showInlineNewCategory && onAddCustomCategory != null) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    RoundedCornerShape(14.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "افزودن دسته‌بندی سفارشی جدید",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                OutlinedTextField(
                                    value = newCategoryTitle,
                                    onValueChange = { newCategoryTitle = it },
                                    label = { Text("نام دسته‌بندی جدید") },
                                    placeholder = { Text("مثال: ناوگان حمل و نقل / تجهیزات کارگاهی") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(CategoryIconCatalog.availableIcons) { (key, iconVec) ->
                                        val isIconSel = key == newCategoryIconKey
                                        val c = Color(newCategoryColorHex)
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isIconSel) c.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .border(
                                                    width = if (isIconSel) 1.5.dp else 0.5.dp,
                                                    color = if (isIconSel) c else MaterialTheme.colorScheme.outlineVariant,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable { newCategoryIconKey = key }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = iconVec,
                                                    contentDescription = null,
                                                    tint = if (isIconSel) c else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(CategoryIconCatalog.availableColors) { hex ->
                                        val isColSel = hex == newCategoryColorHex
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(Color(hex))
                                                .border(
                                                    width = if (isColSel) 2.dp else 0.5.dp,
                                                    color = if (isColSel) MaterialTheme.colorScheme.onSurface else Color.White,
                                                    shape = CircleShape
                                                )
                                                .clickable { newCategoryColorHex = hex }
                                        ) {
                                            if (isColSel) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (newCategoryTitle.isNotBlank()) {
                                                val newKey = "CUSTOM_${UUID.randomUUID().toString().take(8).uppercase()}"
                                                val isVehCat = ToolCategory.isVehicleText(newCategoryTitle) ||
                                                    newCategoryIconKey == "CAR" || newCategoryIconKey == "BIKE"
                                                val created = CustomToolCategory(
                                                    key = newKey,
                                                    titlePersian = newCategoryTitle.trim(),
                                                    iconKey = newCategoryIconKey,
                                                    colorHex = newCategoryColorHex,
                                                    description = "دسته‌بندی سفارشی",
                                                    isVehicleType = isVehCat
                                                )
                                                onAddCustomCategory(created)
                                                selectedCategoryKey = newKey
                                                userManuallyPickedCategory = true
                                                newCategoryTitle = ""
                                                showInlineNewCategory = false
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        AutoResizedButtonText("ثبت و انتخاب دسته", maxFontSize = 11.5.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { showInlineNewCategory = false },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        AutoResizedButtonText("بستن", maxFontSize = 11.5.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Compact FlowRow for categories without wasted empty space
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allCategories.forEach { category ->
                            val isSelected = category.key.equals(selectedCategoryKey, ignoreCase = true)
                            val categoryColor = Color(category.colorHex)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) categoryColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedCategoryKey = category.key
                                        userManuallyPickedCategory = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else categoryColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = category.titlePersian,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Quick Vehicle Type Sub-selector when Vehicle category is active
                    if (isVehicleTool) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "نوع وسیله نقلیه (دارای پیمایش کیلومتری):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(ToolCategory.vehicleSubtypes) { vType ->
                                    val isTypeChosen = name.contains(vType) || (vType == "خودرو سواری" && name.contains("خودرو"))
                                    FilterChip(
                                        selected = isTypeChosen,
                                        onClick = {
                                            if (name.isBlank() || ToolCategory.vehicleSubtypes.any { it == name.trim() }) {
                                                name = vType
                                                nameError = false
                                            } else if (!name.contains(vType)) {
                                                name = "$vType $name".trim()
                                            }
                                            selectedCategoryKey = ToolCategory.VEHICLE.name
                                        },
                                        label = {
                                            Text(
                                                text = vType,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isTypeChosen) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Model and Brand
                    OutlinedTextField(
                        value = modelOrBrand,
                        onValueChange = { modelOrBrand = it },
                        label = { Text("مدل یا برند") },
                        placeholder = {
                            Text(
                                if (isVehicleTool) "مثال: پژو ۲۰۷ / کامیونت ایسوزو ۶ تن / هوندا ۱۲۵"
                                else "مثال: کولر گازی گری ۲۴ هزار / تصفیه آب سافت واتر"
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Location
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("محل نگهداری / قرارگیری") },
                        placeholder = { Text("مثال: پارکینگ، آشپزخانه، موتورخانه، کارگاه") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Vehicle specific ONLY: Iranian License Plate and Current Odometer (Kilometer Mileage)
                    if (isVehicleTool) {
                        IranianPlateEditor(
                            plate = iranianPlate,
                            onPlateChange = { iranianPlate = it }
                        )

                        OutlinedTextField(
                            value = currentOdometerKmStr,
                            onValueChange = {
                                currentOdometerKmStr = JalaliCalendar.toEnglishDigits(it).filter { ch -> ch.isDigit() }
                            },
                            label = { Text("پیمایش / کارکرد فعلی وسیله نقلیه (کیلومتر)") },
                            placeholder = { Text("مثال: 78500") },
                            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Serial Number / Asset Tag for non-vehicle tools (NO mileage!)
                        OutlinedTextField(
                            value = serialNumber,
                            onValueChange = { serialNumber = it },
                            label = { Text("شماره سریال یا کد اموال (اختیاری)") },
                            placeholder = { Text("مثال: SN-984022-X") },
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

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
                        val finalCategoryKey = if (isVehicleTool && !selectedCategoryUi.isVehicleType) {
                            ToolCategory.VEHICLE.name
                        } else {
                            selectedCategoryKey
                        }
                        val finalSerial = if (isVehicleTool) {
                            iranianPlate.toRawString()
                        } else {
                            serialNumber.trim()
                        }
                        val finalOdometer = if (isVehicleTool) {
                            currentOdometerKmStr.toIntOrNull() ?: 0
                        } else {
                            0
                        }
                        val newTool = (toolToEdit ?: ToolEntity(name = name.trim())).copy(
                            name = name.trim(),
                            categoryName = finalCategoryKey,
                            modelOrBrand = modelOrBrand.trim(),
                            location = location.trim(),
                            serialNumber = finalSerial,
                            currentOdometerKm = finalOdometer,
                            purchaseDateJalali = purchaseDateJalali,
                            purchasePrice = price,
                            notes = notes.trim()
                        )
                        onSaveTool(newTool)
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
                    com.example.ui.components.AutoResizedButtonText("ذخیره وسیله", maxFontSize = 12.5.sp)
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
