package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceLogEntity
import com.example.data.model.ServiceScheduleEntity
import com.example.data.model.ServiceStatus
import com.example.data.model.ToolWithServices
import com.example.ui.components.AutoResizedButtonText
import com.example.ui.components.IranianPlateBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusDueSoonAmber
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusUpToDateGreen
import com.example.util.JalaliCalendar
import com.example.util.ReportScope
import com.example.util.ServiceReportGenerator
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceReportScreen(
    toolsWithServices: List<ToolWithServices>,
    initialToolId: Long? = null,
    onBackClick: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val now = JalaliCalendar.now()

    var selectedToolId by remember { mutableStateOf(initialToolId) }
    var selectedScope by remember { mutableStateOf(ReportScope.ALL) }
    var selectedStatus by remember { mutableStateOf<ServiceStatus?>(null) }
    var showRawTextPreview by remember { mutableStateOf(false) }

    val filteredTools = remember(toolsWithServices, selectedToolId) {
        if (selectedToolId != null) {
            toolsWithServices.filter { it.tool.id == selectedToolId }
        } else {
            toolsWithServices
        }
    }

    val matchingSchedules = remember(filteredTools, selectedStatus, now) {
        filteredTools.flatMap { tws ->
            tws.schedules.filter { s ->
                selectedStatus == null || s.computeStatus(now, tws.tool.currentOdometerKm) == selectedStatus
            }
        }
    }

    val matchingLogs = remember(filteredTools) {
        filteredTools.flatMap { it.logs }.sortedByDescending { it.timestamp }
    }

    val overdueCount = remember(matchingSchedules, filteredTools, now) {
        matchingSchedules.count { s ->
            val km = filteredTools.firstOrNull { it.tool.id == s.toolId }?.tool?.currentOdometerKm ?: 0
            s.computeStatus(now, km) == ServiceStatus.OVERDUE
        }
    }
    val dueSoonCount = remember(matchingSchedules, filteredTools, now) {
        matchingSchedules.count { s ->
            val km = filteredTools.firstOrNull { it.tool.id == s.toolId }?.tool?.currentOdometerKm ?: 0
            s.computeStatus(now, km) == ServiceStatus.DUE_SOON
        }
    }
    val upToDateCount = remember(matchingSchedules, filteredTools, now) {
        matchingSchedules.count { s ->
            val km = filteredTools.firstOrNull { it.tool.id == s.toolId }?.tool?.currentOdometerKm ?: 0
            s.computeStatus(now, km) == ServiceStatus.UP_TO_DATE
        }
    }

    val totalEstimatedCost = remember(matchingSchedules) {
        matchingSchedules.sumOf { it.estimatedCost }
    }
    val totalActualCost = remember(matchingLogs) {
        matchingLogs.sumOf { it.actualCost }
    }

    val textReportContent = remember(toolsWithServices, selectedToolId, selectedScope, selectedStatus) {
        ServiceReportGenerator.generateTextReport(
            toolsWithServices = toolsWithServices,
            selectedToolId = selectedToolId,
            reportScope = selectedScope,
            selectedStatus = selectedStatus
        )
    }

    // SAF Launcher for saving PDF Report to Local Storage or Google Drive
    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = withContext(Dispatchers.IO) {
                    ServiceReportGenerator.savePdfToUri(
                        context = context,
                        uri = uri,
                        toolsWithServices = toolsWithServices,
                        selectedToolId = selectedToolId,
                        reportScope = selectedScope,
                        selectedStatus = selectedStatus
                    )
                }
                res.fold(
                    onSuccess = { onShowMessage("فایل PDF گزارش با موفقیت در مسیر انتخابی ذخیره شد.") },
                    onFailure = { e -> onShowMessage("خطا در ذخیره PDF: ${e.localizedMessage ?: "نامشخص"}") }
                )
            }
        }
    }

    // SAF Launcher for saving CSV/Excel Report to Local Storage or Google Drive
    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = withContext(Dispatchers.IO) {
                    val csv = ServiceReportGenerator.generateCsvReport(
                        toolsWithServices = toolsWithServices,
                        selectedToolId = selectedToolId,
                        reportScope = selectedScope,
                        selectedStatus = selectedStatus
                    )
                    ServiceReportGenerator.saveTextOrCsvToUri(context, uri, csv)
                }
                res.fold(
                    onSuccess = { onShowMessage("فایل اکسل (CSV) گزارش با موفقیت ذخیره شد.") },
                    onFailure = { e -> onShowMessage("خطا در ذخیره CSV: ${e.localizedMessage ?: "نامشخص"}") }
                )
            }
        }
    }

    // SAF Launcher for saving TXT Report
    val createTxtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = withContext(Dispatchers.IO) {
                    ServiceReportGenerator.saveTextOrCsvToUri(context, uri, textReportContent)
                }
                res.fold(
                    onSuccess = { onShowMessage("فایل متنی گزارش با موفقیت ذخیره شد.") },
                    onFailure = { e -> onShowMessage("خطا در ذخیره فایل متنی: ${e.localizedMessage ?: "نامشخص"}") }
                )
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Column {
                        Text(
                            text = "گزارش جامع سرویس‌ها و هزینه‌ها",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "امروز: ${now.format(includeDayName = true)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("گزارش سرویس‌یار", textReportContent))
                            Toast.makeText(context, "متن گزارش در حافظه کپی شد", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "کپی متن گزارش")
                    }
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "گزارش سرویس‌یار - ${now.format()}")
                                putExtra(Intent.EXTRA_TEXT, textReportContent)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "ارسال گزارش متنی"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "اشتراک‌گذاری متنی")
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Filters Card (Tool, Scope, Status)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "فیلترهای گزارش‌گیری:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Tool Filter Row
                        Text(
                            text = "انتخاب وسیله:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedToolId == null,
                                    onClick = { selectedToolId = null },
                                    label = {
                                        Text(
                                            text = "همه وسایل (${JalaliCalendar.toPersianDigits(toolsWithServices.size)})",
                                            fontWeight = if (selectedToolId == null) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            items(toolsWithServices, key = { it.tool.id }) { tws ->
                                val isSel = selectedToolId == tws.tool.id
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedToolId = if (isSel) null else tws.tool.id },
                                    label = {
                                        Text(
                                            text = tws.tool.name,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Report Scope Filter Row
                        Text(
                            text = "بخش‌های گزارش:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ReportScope.entries) { scopeItem ->
                                val isSel = selectedScope == scopeItem
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedScope = scopeItem },
                                    label = {
                                        Text(
                                            text = scopeItem.titlePersian,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                            }
                        }

                        // Status Filter Row
                        if (selectedScope != ReportScope.SERVICE_LOGS) {
                            Text(
                                text = "فیلتر وضعیت سرویس:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = selectedStatus == null,
                                        onClick = { selectedStatus = null },
                                        label = { Text("همه وضعیت‌ها") }
                                    )
                                }
                                val statuses = listOf(
                                    ServiceStatus.OVERDUE,
                                    ServiceStatus.DUE_SOON,
                                    ServiceStatus.UP_TO_DATE
                                )
                                items(statuses) { st ->
                                    val isSel = selectedStatus == st
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedStatus = if (isSel) null else st },
                                        label = { Text(st.titlePersian) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Export & Download Actions Card (PDF / Excel CSV / TXT / Cloud Drive)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "دریافت خروجی و ذخیره فایل گزارش",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "قابل ذخیره در حافظه داخلی (لوکال) یا گوگل درایو (Google Drive)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Save PDF via SAF (Local / Google Drive)
                            Button(
                                onClick = {
                                    createPdfLauncher.launch(ServiceReportGenerator.generateDefaultReportFileName("pdf"))
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_pdf_button")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                AutoResizedButtonText(
                                    text = "ذخیره فایل PDF",
                                    maxFontSize = 12.sp,
                                    minFontSize = 8.5.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }

                            // Save Excel CSV via SAF (Local / Google Drive)
                            Button(
                                onClick = {
                                    createCsvLauncher.launch(ServiceReportGenerator.generateDefaultReportFileName("csv"))
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF059669)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_csv_button")
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                AutoResizedButtonText(
                                    text = "خروجی اکسل CSV",
                                    maxFontSize = 12.sp,
                                    minFontSize = 8.5.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Direct Share PDF to Google Drive / Telegram
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val fileName = ServiceReportGenerator.generateDefaultReportFileName("pdf")
                                        val ok = withContext(Dispatchers.IO) {
                                            ServiceReportGenerator.createAndShareReportFile(
                                                context = context,
                                                fileName = fileName,
                                                mimeType = "application/pdf"
                                            ) { fos ->
                                                ServiceReportGenerator.writePdfReportToStream(
                                                    outputStream = fos,
                                                    toolsWithServices = toolsWithServices,
                                                    selectedToolId = selectedToolId,
                                                    reportScope = selectedScope,
                                                    selectedStatus = selectedStatus
                                                )
                                            }
                                        }
                                        if (!ok) {
                                            onShowMessage("خطا در آماده‌سازی فایل PDF برای اشتراک‌گذاری")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                AutoResizedButtonText(
                                    text = "ارسال PDF به درایو/ابری",
                                    maxFontSize = 11.5.sp,
                                    minFontSize = 8.5.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }

                            // Save TXT Report
                            OutlinedButton(
                                onClick = {
                                    createTxtLauncher.launch(ServiceReportGenerator.generateDefaultReportFileName("txt"))
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                AutoResizedButtonText(
                                    text = "ذخیره فایل متنی TXT",
                                    maxFontSize = 11.5.sp,
                                    minFontSize = 8.5.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Executive Financial & Operational Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "خلاصه آماری و مالی گزارش",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            OutlinedButton(
                                onClick = { showRawTextPreview = !showRawTextPreview },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                AutoResizedButtonText(
                                    text = if (showRawTextPreview) "نمایش کارتی" else "پیش‌نمایش متنی",
                                    maxFontSize = 11.5.sp,
                                    minFontSize = 8.5.sp
                                )
                            }
                        }

                        // 3 Stat Tiles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReportMiniStatTile(
                                label = "تعداد وسایل",
                                value = "${JalaliCalendar.toPersianDigits(filteredTools.size)} دستگاه",
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                valueColor = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            ReportMiniStatTile(
                                label = "برنامه‌های سرویس",
                                value = "${JalaliCalendar.toPersianDigits(matchingSchedules.size)} برنامه",
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                valueColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            ReportMiniStatTile(
                                label = "سرویس‌های انجام‌شده",
                                value = "${JalaliCalendar.toPersianDigits(matchingLogs.size)} نوبت",
                                containerColor = Color(0xFFDCFCE7),
                                valueColor = Color(0xFF059669),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Status counts row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReportMiniStatTile(
                                label = "گذشته از موعد (فوری)",
                                value = "${JalaliCalendar.toPersianDigits(overdueCount)} مورد",
                                containerColor = StatusOverdueRed.copy(alpha = 0.12f),
                                valueColor = StatusOverdueRed,
                                modifier = Modifier.weight(1f)
                            )
                            ReportMiniStatTile(
                                label = "نزدیک به موعد",
                                value = "${JalaliCalendar.toPersianDigits(dueSoonCount)} مورد",
                                containerColor = StatusDueSoonAmber.copy(alpha = 0.14f),
                                valueColor = StatusDueSoonAmber,
                                modifier = Modifier.weight(1f)
                            )
                            ReportMiniStatTile(
                                label = "سرویس شده و معتبر",
                                value = "${JalaliCalendar.toPersianDigits(upToDateCount)} مورد",
                                containerColor = StatusUpToDateGreen.copy(alpha = 0.12f),
                                valueColor = StatusUpToDateGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Financial Totals
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مجموع هزینه واقعی ثبت‌شده:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = JalaliCalendar.formatPrice(totalActualCost),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusUpToDateGreen,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مجموع هزینه تخمینی پیش‌رو:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = JalaliCalendar.formatPrice(totalEstimatedCost),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            if (showRawTextPreview) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = textReportContent,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            } else {
                // Detailed Visual Report Sections
                if (selectedScope == ReportScope.ALL || selectedScope == ReportScope.ACTIVE_SCHEDULES) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "گزارش برنامه‌های سرویس دوره‌ای (${JalaliCalendar.toPersianDigits(matchingSchedules.size)} مورد)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    if (matchingSchedules.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "هیچ برنامه سرویسی با فیلترهای انتخابی یافت نشد.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    } else {
                        items(matchingSchedules, key = { "sched_${it.id}" }) { schedule ->
                            val tool = filteredTools.firstOrNull { it.tool.id == schedule.toolId }?.tool
                            ReportScheduleItemCard(
                                schedule = schedule,
                                toolName = tool?.name ?: schedule.toolName,
                                isVehicle = tool?.isVehicle() == true,
                                serialOrPlate = tool?.serialNumber ?: "",
                                currentOdometerKm = tool?.currentOdometerKm ?: 0
                            )
                        }
                    }
                }

                if (selectedScope == ReportScope.ALL || selectedScope == ReportScope.SERVICE_LOGS) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = StatusUpToDateGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "گزارش سوابق و تاریخچه انجام سرویس (${JalaliCalendar.toPersianDigits(matchingLogs.size)} نوبت)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    if (matchingLogs.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "هنوز هیچ سابقه انجام سرویسی برای این بخش ثبت نشده است.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    } else {
                        items(matchingLogs, key = { "log_${it.id}" }) { log ->
                            val isVeh = filteredTools.firstOrNull { it.tool.id == log.toolId }?.tool?.isVehicle() == true
                            ReportLogItemCard(log = log, isVehicle = isVeh)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportMiniStatTile(
    label: String,
    value: String,
    containerColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReportScheduleItemCard(
    schedule: ServiceScheduleEntity,
    toolName: String,
    isVehicle: Boolean,
    serialOrPlate: String,
    currentOdometerKm: Int
) {
    val now = JalaliCalendar.now()
    val status = schedule.computeStatus(now, currentOdometerKm)
    val nextFormatted = JalaliCalendar.parse(schedule.nextServiceDateJalali)?.format()
        ?: JalaliCalendar.toPersianDigits(schedule.nextServiceDateJalali.ifBlank { "نامشخص" })
    val lastFormatted = JalaliCalendar.parse(schedule.lastServiceDateJalali)?.format()
        ?: JalaliCalendar.toPersianDigits(schedule.lastServiceDateJalali.ifBlank { "ثبت نشده" })

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
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
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = schedule.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "وسیله: $toolName • ${schedule.getServiceType().titlePersian}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = status)
            }

            if (isVehicle && serialOrPlate.isNotBlank()) {
                IranianPlateBadge(plateString = serialOrPlate, height = 38)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "آخرین سرویس: $lastFormatted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "موعد بعدی: $nextFormatted",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (isVehicle && schedule.intervalKilometers > 0) {
                val remKm = schedule.getRemainingKilometers(currentOdometerKm, now) ?: 0
                val dailyText = if (schedule.dailyKilometers > 0) {
                    " • پیمایش روزانه: ${JalaliCalendar.toPersianDigits(schedule.dailyKilometers)} کیلومتر"
                } else ""
                Text(
                    text = "دوره کارکرد: هر ${JalaliCalendar.toPersianDigits("%,d".format(Locale.US, schedule.intervalKilometers))} کیلومتر$dailyText • مانده: ${
                        JalaliCalendar.toPersianDigits("%,d".format(Locale.US, remKm))
                    } کیلومتر",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (remKm <= 0) StatusOverdueRed else MaterialTheme.colorScheme.onSurface
                )
            }

            if (schedule.estimatedCost > 0 || schedule.technicianName.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "هزینه تخمینی: ${JalaliCalendar.formatPrice(schedule.estimatedCost)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (schedule.technicianName.isNotBlank()) {
                        Text(
                            text = "مسئول/مرکز: ${schedule.technicianName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportLogItemCard(
    log: ServiceLogEntity,
    isVehicle: Boolean = true
) {
    val dateFormatted = JalaliCalendar.parse(log.performedDateJalali)?.format()
        ?: JalaliCalendar.toPersianDigits(log.performedDateJalali)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, StatusUpToDateGreen.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.serviceTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "وسیله: ${log.toolName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StatusUpToDateGreen.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = JalaliCalendar.formatPrice(log.actualCost),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = StatusUpToDateGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "تاریخ انجام: $dateFormatted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isVehicle && log.performedOdometerKm > 0) {
                    Text(
                        text = "کیلومتر: ${JalaliCalendar.toPersianDigits("%,d".format(Locale.US, log.performedOdometerKm))}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (log.technicianOrShop.isNotBlank() || log.invoiceNumber.isNotBlank()) {
                Text(
                    text = "تعمیرگاه/تکنسین: ${log.technicianOrShop.ifBlank { "-" }} • شماره فاکتور: ${JalaliCalendar.toPersianDigits(log.invoiceNumber.ifBlank { "-" })}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (log.partsReplaced.isNotBlank()) {
                Text(
                    text = "قطعات تعویض شده: ${log.partsReplaced}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
