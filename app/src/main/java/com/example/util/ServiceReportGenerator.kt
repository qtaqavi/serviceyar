package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.ServiceLogEntity
import com.example.data.model.ServiceScheduleEntity
import com.example.data.model.ServiceStatus
import com.example.data.model.ToolWithServices
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Locale

enum class ReportScope(val titlePersian: String) {
    ALL("گزارش جامع (برنامه‌ها و سوابق)"),
    ACTIVE_SCHEDULES("فقط برنامه‌ها و موعدهای سرویس"),
    SERVICE_LOGS("فقط تاریخچه و هزینه‌های انجام‌شده")
}

object ServiceReportGenerator {

    private const val REPORTS_DIR_NAME = "reports"

    fun generateDefaultReportFileName(extension: String): String {
        val now = JalaliCalendar.now()
        return "ServiceYar_Report_${now.year}_${
            "%02d".format(Locale.US, now.month)
        }_${"%02d".format(Locale.US, now.day)}.$extension"
    }

    fun generateTextReport(
        toolsWithServices: List<ToolWithServices>,
        selectedToolId: Long?,
        reportScope: ReportScope,
        selectedStatus: ServiceStatus?
    ): String {
        val now = JalaliCalendar.now()
        val filteredTools = if (selectedToolId != null) {
            toolsWithServices.filter { it.tool.id == selectedToolId }
        } else {
            toolsWithServices
        }

        val allMatchingSchedules = filteredTools.flatMap { tws ->
            tws.schedules.filter { s ->
                selectedStatus == null || s.computeStatus(now, tws.tool.currentOdometerKm) == selectedStatus
            }
        }
        val allMatchingLogs = filteredTools.flatMap { it.logs }
            .sortedByDescending { it.timestamp }

        val overdueCount = allMatchingSchedules.count { s ->
            val toolKm = filteredTools.firstOrNull { it.tool.id == s.toolId }?.tool?.currentOdometerKm ?: 0
            s.computeStatus(now, toolKm) == ServiceStatus.OVERDUE
        }
        val dueSoonCount = allMatchingSchedules.count { s ->
            val toolKm = filteredTools.firstOrNull { it.tool.id == s.toolId }?.tool?.currentOdometerKm ?: 0
            s.computeStatus(now, toolKm) == ServiceStatus.DUE_SOON
        }
        val upToDateCount = allMatchingSchedules.count { s ->
            val toolKm = filteredTools.firstOrNull { it.tool.id == s.toolId }?.tool?.currentOdometerKm ?: 0
            s.computeStatus(now, toolKm) == ServiceStatus.UP_TO_DATE
        }

        val totalEstimatedCost = allMatchingSchedules.sumOf { it.estimatedCost }
        val totalActualSpentCost = allMatchingLogs.sumOf { it.actualCost }

        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("📊 گزارش جامع مدیریت سرویس و نگهداری (سرویس‌یار)")
        sb.appendLine("تاریخ صدور گزارش: ${now.format(includeDayName = true)}")
        sb.appendLine("نوع گزارش: ${reportScope.titlePersian}")
        if (selectedToolId != null) {
            val toolName = filteredTools.firstOrNull()?.tool?.name ?: "-"
            sb.appendLine("وسیله انتخابی: $toolName")
        } else {
            sb.appendLine("دامنه وسایل: همه وسایل (${JalaliCalendar.toPersianDigits(filteredTools.size)} مورد)")
        }
        if (selectedStatus != null) {
            sb.appendLine("فیلتر وضعیت: ${selectedStatus.titlePersian}")
        }
        sb.appendLine("==========================================")
        sb.appendLine()

        // Summary Block
        sb.appendLine("📌 خلاصه آماری و مالی:")
        sb.appendLine("• تعداد وسایل در گزارش: ${JalaliCalendar.toPersianDigits(filteredTools.size)} دستگاه")
        sb.appendLine("• تعداد برنامه‌های سرویس فعال: ${JalaliCalendar.toPersianDigits(allMatchingSchedules.size)} مورد")
        sb.appendLine("  - نیازمند اقدام فوری (گذشته از موعد): ${JalaliCalendar.toPersianDigits(overdueCount)} مورد")
        sb.appendLine("  - نزدیک به موعد سرویس: ${JalaliCalendar.toPersianDigits(dueSoonCount)} مورد")
        sb.appendLine("  - سرویس شده و معتبر: ${JalaliCalendar.toPersianDigits(upToDateCount)} مورد")
        sb.appendLine("• تعداد سوابق سرویس ثبت‌شده: ${JalaliCalendar.toPersianDigits(allMatchingLogs.size)} نوبت")
        sb.appendLine("• مجموع هزینه تخمینی سرویس‌های آتی: ${JalaliCalendar.formatPrice(totalEstimatedCost)}")
        sb.appendLine("• مجموع هزینه واقعی انجام‌شده تاکنون: ${JalaliCalendar.formatPrice(totalActualSpentCost)}")
        sb.appendLine("------------------------------------------")
        sb.appendLine()

        if (reportScope == ReportScope.ALL || reportScope == ReportScope.ACTIVE_SCHEDULES) {
            sb.appendLine("🛠 بخش اول: وضعیت وسایل و برنامه‌های سرویس دوره‌ای")
            sb.appendLine()
            if (filteredTools.isEmpty() || allMatchingSchedules.isEmpty()) {
                sb.appendLine("موردی برای نمایش در برنامه‌های سرویس یافت نشد.")
                sb.appendLine()
            } else {
                filteredTools.forEachIndexed { index, tws ->
                    val tool = tws.tool
                    val schedules = tws.schedules.filter { s ->
                        selectedStatus == null || s.computeStatus(now, tool.currentOdometerKm) == selectedStatus
                    }
                    if (schedules.isNotEmpty() || selectedStatus == null) {
                        sb.appendLine("${JalaliCalendar.toPersianDigits(index + 1)}. وسیله: ${tool.name} (${tool.getCategoryUi().titlePersian})")
                        if (tool.modelOrBrand.isNotBlank()) {
                            sb.appendLine("   مدل/برند: ${tool.modelOrBrand} | محل استقرار: ${tool.location.ifBlank { "-" }}")
                        }
                        if (tool.serialNumber.isNotBlank()) {
                            val label = if (tool.isVehicle()) "پلاک انتظامی" else "شماره سریال"
                            sb.appendLine("   $label: ${JalaliCalendar.toPersianDigits(tool.serialNumber)}")
                        }
                        if (tool.isVehicle() && tool.currentOdometerKm > 0) {
                            sb.appendLine("   کیلومتر فعلی خودرو: ${JalaliCalendar.toPersianDigits("%,d".format(Locale.US, tool.currentOdometerKm))} کیلومتر")
                        }
                        if (schedules.isEmpty()) {
                            sb.appendLine("   * بدون برنامه سرویس فعال")
                        } else {
                            schedules.forEach { s ->
                                val status = s.computeStatus(now, tool.currentOdometerKm)
                                val nextDateFormatted = JalaliCalendar.parse(s.nextServiceDateJalali)?.format() ?: s.nextServiceDateJalali.ifBlank { "نامشخص" }
                                val lastDateFormatted = JalaliCalendar.parse(s.lastServiceDateJalali)?.format() ?: s.lastServiceDateJalali.ifBlank { "ثبت نشده" }
                                sb.appendLine("   🔹 سرویس: ${s.title} [${status.titlePersian}]")
                                sb.appendLine("      - آخرین انجام: $lastDateFormatted | موعد بعدی: $nextDateFormatted")
                                if (s.intervalKilometers > 0) {
                                    val remKm = s.getRemainingKilometers(tool.currentOdometerKm, now) ?: 0
                                    val dailyInfo = if (s.dailyKilometers > 0) " (پیمایش روزانه: ${JalaliCalendar.toPersianDigits(s.dailyKilometers)} کیلومتر)" else ""
                                    sb.appendLine(
                                        "      - دوره کارکرد: هر ${JalaliCalendar.toPersianDigits("%,d".format(Locale.US, s.intervalKilometers))} کیلومتر$dailyInfo | مانده تا سرویس: ${
                                            JalaliCalendar.toPersianDigits("%,d".format(Locale.US, remKm))
                                        } کیلومتر"
                                    )
                                }
                                if (s.estimatedCost > 0) {
                                    sb.appendLine("      - هزینه تخمینی: ${JalaliCalendar.formatPrice(s.estimatedCost)} | تکنسین/مرکز: ${s.technicianName.ifBlank { "-" }}")
                                }
                            }
                        }
                        sb.appendLine()
                    }
                }
            }
            sb.appendLine("------------------------------------------")
            sb.appendLine()
        }

        if (reportScope == ReportScope.ALL || reportScope == ReportScope.SERVICE_LOGS) {
            sb.appendLine("📜 بخش دوم: تاریخچه و سوابق سرویس‌های انجام‌شده")
            sb.appendLine()
            if (allMatchingLogs.isEmpty()) {
                sb.appendLine("هیچ سابقه سرویسی در این بخش ثبت نشده است.")
            } else {
                allMatchingLogs.forEachIndexed { idx, log ->
                    val dateStr = JalaliCalendar.parse(log.performedDateJalali)?.format() ?: JalaliCalendar.toPersianDigits(log.performedDateJalali)
                    sb.appendLine("${JalaliCalendar.toPersianDigits(idx + 1)}. ${log.serviceTitle} - وسیله: ${log.toolName}")
                    sb.appendLine("   تاریخ انجام: $dateStr | هزینه واقعی: ${JalaliCalendar.formatPrice(log.actualCost)}")
                    if (log.performedOdometerKm > 0) {
                        sb.appendLine("   کیلومتر زمان سرویس: ${JalaliCalendar.toPersianDigits("%,d".format(Locale.US, log.performedOdometerKm))} کیلومتر")
                    }
                    if (log.technicianOrShop.isNotBlank() || log.invoiceNumber.isNotBlank()) {
                        sb.appendLine("   تعمیرگاه/مسئول: ${log.technicianOrShop.ifBlank { "-" }} | شماره فاکتور: ${JalaliCalendar.toPersianDigits(log.invoiceNumber.ifBlank { "-" })}")
                    }
                    if (log.partsReplaced.isNotBlank()) {
                        sb.appendLine("   قطعات تعویضی: ${log.partsReplaced}")
                    }
                    if (log.notes.isNotBlank()) {
                        sb.appendLine("   توضیحات: ${log.notes}")
                    }
                    sb.appendLine()
                }
            }
        }

        return sb.toString()
    }

    fun generateCsvReport(
        toolsWithServices: List<ToolWithServices>,
        selectedToolId: Long?,
        reportScope: ReportScope,
        selectedStatus: ServiceStatus?
    ): String {
        val now = JalaliCalendar.now()
        val filteredTools = if (selectedToolId != null) {
            toolsWithServices.filter { it.tool.id == selectedToolId }
        } else {
            toolsWithServices
        }

        val sb = StringBuilder()
        // UTF-8 BOM so Microsoft Excel opens Persian characters cleanly
        sb.append("\uFEFF")

        if (reportScope == ReportScope.ALL || reportScope == ReportScope.ACTIVE_SCHEDULES) {
            sb.appendLine("جدول برنامه‌های سرویس و نگهداری دوره‌ای")
            sb.appendLine("نام وسیله,دسته‌بندی,مدل یا برند,پلاک یا سریال,عنوان سرویس,وضعیت فعلی,آخرین سرویس,موعد سرویس بعدی,دوره کیلومتری,پیمایش روزانه (کیلومتر),کیلومتر باقی‌مانده,هزینه تخمینی (تومان),تکنسین یا مرکز سرویس,اولویت")
            filteredTools.forEach { tws ->
                val tool = tws.tool
                tws.schedules.forEach { s ->
                    val status = s.computeStatus(now, tool.currentOdometerKm)
                    if (selectedStatus == null || status == selectedStatus) {
                        val remKm = s.getRemainingKilometers(tool.currentOdometerKm, now) ?: 0
                        sb.appendLine(
                            listOf(
                                escapeCsv(tool.name),
                                escapeCsv(tool.getCategoryUi().titlePersian),
                                escapeCsv(tool.modelOrBrand),
                                escapeCsv(tool.serialNumber),
                                escapeCsv(s.title),
                                escapeCsv(status.titlePersian),
                                escapeCsv(s.lastServiceDateJalali),
                                escapeCsv(s.nextServiceDateJalali),
                                s.intervalKilometers.toString(),
                                s.dailyKilometers.toString(),
                                remKm.toString(),
                                s.estimatedCost.toString(),
                                escapeCsv(s.technicianName),
                                escapeCsv(s.getPriority().titlePersian)
                            ).joinToString(",")
                        )
                    }
                }
            }
            sb.appendLine()
        }

        if (reportScope == ReportScope.ALL || reportScope == ReportScope.SERVICE_LOGS) {
            sb.appendLine("جدول سوابق و تاریخچه سرویس‌های انجام‌شده")
            sb.appendLine("نام وسیله,عنوان سرویس انجام‌شده,تاریخ انجام (شمسی),کیلومتر زمان سرویس,هزینه واقعی (تومان),تعمیرگاه یا تکنسین,شماره فاکتور,قطعات تعویض شده,یادداشت")
            val logs = filteredTools.flatMap { it.logs }.sortedByDescending { it.timestamp }
            logs.forEach { log ->
                sb.appendLine(
                    listOf(
                        escapeCsv(log.toolName),
                        escapeCsv(log.serviceTitle),
                        escapeCsv(log.performedDateJalali),
                        log.performedOdometerKm.toString(),
                        log.actualCost.toString(),
                        escapeCsv(log.technicianOrShop),
                        escapeCsv(log.invoiceNumber),
                        escapeCsv(log.partsReplaced),
                        escapeCsv(log.notes)
                    ).joinToString(",")
                )
            }
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val cleaned = value.replace("\"", "\"\"").replace("\n", " ").trim()
        return "\"$cleaned\""
    }

    fun writePdfReportToStream(
        outputStream: OutputStream,
        toolsWithServices: List<ToolWithServices>,
        selectedToolId: Long?,
        reportScope: ReportScope,
        selectedStatus: ServiceStatus?
    ): Result<Unit> {
        val pdfDocument = PdfDocument()
        return try {
            val now = JalaliCalendar.now()
            val filteredTools = if (selectedToolId != null) {
                toolsWithServices.filter { it.tool.id == selectedToolId }
            } else {
                toolsWithServices
            }

            val allMatchingSchedules = filteredTools.flatMap { tws ->
                tws.schedules.filter { s ->
                    selectedStatus == null || s.computeStatus(now, tws.tool.currentOdometerKm) == selectedStatus
                }
            }
            val allMatchingLogs = filteredTools.flatMap { it.logs }.sortedByDescending { it.timestamp }

            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points
            val margin = 36f
            val rightEdge = pageWidth - margin
            val leftEdge = margin

            var pageNumber = 1
            var currentPage = pdfDocument.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            )
            var canvas: Canvas = currentPage.canvas
            var yPos = margin

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E0F2FE")
                textSize = 10.5f
                textAlign = Paint.Align.RIGHT
            }
            val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0284C7")
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F172A")
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#334155")
                textSize = 9.5f
                textAlign = Paint.Align.RIGHT
            }
            val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG)

            fun startNewPageIfNeeded(requiredHeight: Float) {
                if (yPos + requiredHeight > pageHeight - margin) {
                    pdfDocument.finishPage(currentPage)
                    pageNumber++
                    currentPage = pdfDocument.startPage(
                        PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    )
                    canvas = currentPage.canvas
                    yPos = margin
                }
            }

            // Header Banner
            boxPaint.color = Color.parseColor("#0284C7")
            canvas.drawRoundRect(RectF(leftEdge, yPos, rightEdge, yPos + 68f), 12f, 12f, boxPaint)
            canvas.drawText("گزارش جامع مدیریت سرویس و نگهداری (سرویس‌یار)", rightEdge - 16f, yPos + 28f, titlePaint)
            canvas.drawText(
                "تاریخ گزارش: ${now.format(includeDayName = true)}  |  نوع: ${reportScope.titlePersian}",
                rightEdge - 16f,
                yPos + 50f,
                subHeaderPaint
            )
            yPos += 84f

            // Summary Box
            val totalEst = allMatchingSchedules.sumOf { it.estimatedCost }
            val totalActual = allMatchingLogs.sumOf { it.actualCost }
            boxPaint.color = Color.parseColor("#F1F5F9")
            canvas.drawRoundRect(RectF(leftEdge, yPos, rightEdge, yPos + 64f), 10f, 10f, boxPaint)
            canvas.drawText(
                "تعداد وسایل: ${JalaliCalendar.toPersianDigits(filteredTools.size)} دستگاه   |   برنامه‌های سرویس: ${JalaliCalendar.toPersianDigits(allMatchingSchedules.size)} مورد   |   سوابق انجام‌شده: ${JalaliCalendar.toPersianDigits(allMatchingLogs.size)} نوبت",
                rightEdge - 14f,
                yPos + 25f,
                bodyBoldPaint
            )
            canvas.drawText(
                "مجموع هزینه تخمینی سرویس‌های آتی: ${JalaliCalendar.formatPrice(totalEst)}   |   مجموع هزینه واقعی انجام‌شده: ${JalaliCalendar.formatPrice(totalActual)}",
                rightEdge - 14f,
                yPos + 47f,
                bodyPaint
            )
            yPos += 80f

            // Section 1: Active Schedules
            if (reportScope == ReportScope.ALL || reportScope == ReportScope.ACTIVE_SCHEDULES) {
                startNewPageIfNeeded(40f)
                canvas.drawText("۱. فهرست برنامه‌های سرویس و وضعیت موعدها", rightEdge, yPos + 14f, sectionPaint)
                yPos += 24f

                allMatchingSchedules.forEachIndexed { index, s ->
                    startNewPageIfNeeded(62f)
                    val tool = filteredTools.firstOrNull { it.tool.id == s.toolId }?.tool
                    val status = s.computeStatus(now, tool?.currentOdometerKm ?: 0)

                    boxPaint.color = Color.parseColor("#F8FAFC")
                    canvas.drawRoundRect(RectF(leftEdge, yPos, rightEdge, yPos + 54f), 8f, 8f, boxPaint)

                    val nextFormatted = JalaliCalendar.parse(s.nextServiceDateJalali)?.format() ?: s.nextServiceDateJalali
                    canvas.drawText(
                        "${JalaliCalendar.toPersianDigits(index + 1)}. ${s.title} (${s.toolName.ifBlank { tool?.name ?: "" }}) - وضعیت: ${status.titlePersian}",
                        rightEdge - 10f,
                        yPos + 20f,
                        bodyBoldPaint
                    )

                    val kmDetail = if (s.intervalKilometers > 0) {
                        val rem = s.getRemainingKilometers(tool?.currentOdometerKm ?: 0, now) ?: 0
                        " | مانده کارکرد: ${JalaliCalendar.toPersianDigits("%,d".format(Locale.US, rem))} کیلومتر"
                    } else ""

                    canvas.drawText(
                        "موعد بعدی: $nextFormatted$kmDetail | هزینه تخمینی: ${JalaliCalendar.formatPrice(s.estimatedCost)}",
                        rightEdge - 10f,
                        yPos + 40f,
                        bodyPaint
                    )
                    yPos += 62f
                }
                yPos += 12f
            }

            // Section 2: Service History Logs
            if (reportScope == ReportScope.ALL || reportScope == ReportScope.SERVICE_LOGS) {
                startNewPageIfNeeded(40f)
                canvas.drawText("۲. سوابق و تاریخچه سرویس‌های انجام‌شده", rightEdge, yPos + 14f, sectionPaint)
                yPos += 24f

                if (allMatchingLogs.isEmpty()) {
                    startNewPageIfNeeded(30f)
                    canvas.drawText("هیچ سابقه سرویسی ثبت نشده است.", rightEdge - 10f, yPos + 16f, bodyPaint)
                    yPos += 30f
                } else {
                    allMatchingLogs.forEachIndexed { idx, log ->
                        startNewPageIfNeeded(62f)
                        boxPaint.color = Color.parseColor("#F0FDF4")
                        canvas.drawRoundRect(RectF(leftEdge, yPos, rightEdge, yPos + 54f), 8f, 8f, boxPaint)

                        val dateStr = JalaliCalendar.parse(log.performedDateJalali)?.format() ?: JalaliCalendar.toPersianDigits(log.performedDateJalali)
                        canvas.drawText(
                            "${JalaliCalendar.toPersianDigits(idx + 1)}. ${log.serviceTitle} (${log.toolName}) - تاریخ انجام: $dateStr",
                            rightEdge - 10f,
                            yPos + 20f,
                            bodyBoldPaint
                        )
                        val kmText = if (log.performedOdometerKm > 0) " | کیلومتر: ${JalaliCalendar.toPersianDigits("%,d".format(Locale.US, log.performedOdometerKm))}" else ""
                        val partsText = if (log.partsReplaced.isNotBlank()) " | قطعات: ${log.partsReplaced}" else ""
                        canvas.drawText(
                            "هزینه واقعی: ${JalaliCalendar.formatPrice(log.actualCost)} | مرکز/تکنسین: ${log.technicianOrShop.ifBlank { "-" }}$kmText$partsText",
                            rightEdge - 10f,
                            yPos + 40f,
                            bodyPaint
                        )
                        yPos += 62f
                    }
                }
            }

            pdfDocument.finishPage(currentPage)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            pdfDocument.close()
        }
    }

    fun getReportsDir(context: Context): File {
        val dir = File(context.filesDir, REPORTS_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun saveTextOrCsvToUri(context: Context, uri: Uri, content: String): Result<Unit> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: return Result.failure(IllegalStateException("امکان باز کردن فایل مقصد وجود ندارد."))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun savePdfToUri(
        context: Context,
        uri: Uri,
        toolsWithServices: List<ToolWithServices>,
        selectedToolId: Long?,
        reportScope: ReportScope,
        selectedStatus: ServiceStatus?
    ): Result<Unit> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                writePdfReportToStream(out, toolsWithServices, selectedToolId, reportScope, selectedStatus)
                    .getOrThrow()
            } ?: return Result.failure(IllegalStateException("امکان باز کردن فایل PDF مقصد وجود ندارد."))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createAndShareReportFile(
        context: Context,
        fileName: String,
        mimeType: String,
        writeAction: (FileOutputStream) -> Unit
    ): Boolean {
        return try {
            val file = File(getReportsDir(context), fileName)
            FileOutputStream(file).use { fos ->
                writeAction(fos)
            }
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "گزارش سرویس‌یار ($fileName)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "اشتراک‌گذاری یا ذخیره گزارش (گوگل درایو / تلگرام / ...)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            false
        }
    }
}
