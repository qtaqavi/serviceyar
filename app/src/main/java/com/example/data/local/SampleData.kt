package com.example.data.local

import com.example.data.model.IntervalType
import com.example.data.model.ServiceLogEntity
import com.example.data.model.ServicePriority
import com.example.data.model.ServiceScheduleEntity
import com.example.data.model.ServiceType
import com.example.data.model.ToolCategory
import com.example.data.model.ToolEntity
import com.example.util.JalaliCalendar
import com.example.util.JalaliDate

object SampleData {

    fun generateInitialData(): Triple<List<ToolEntity>, List<ServiceScheduleEntity>, List<ServiceLogEntity>> {
        val now = JalaliCalendar.now()
        val currentYear = now.year

        val tools = listOf(
            ToolEntity(
                id = 1L,
                name = "پکیج شوفاژ دیواری بوتان (Perla Pro)",
                categoryName = ToolCategory.FACILITY_HVAC.name,
                modelOrBrand = "بوتان مدل پرلا پرو ۲۴ هزار",
                location = "آشپزخانه / بالکن شمالی",
                serialNumber = "BTN-9824-H7",
                purchaseDateJalali = "${currentYear - 2}/۰۷/۱۵",
                purchasePrice = 28000000L,
                notes = "فشار بار همیشه باید بین ۱.۲ تا ۱.۵ بار باشد. برای فصل زمستان حتماً رسوب‌گیری انجام شود."
            ),
            ToolEntity(
                id = 2L,
                name = "خودرو سواری پژو پارس TU5",
                categoryName = ToolCategory.VEHICLE.name,
                modelOrBrand = "ایران خودرو - مدل ۱۴۰۱",
                location = "پارکینگ شماره ۳",
                serialNumber = "IR-68-912-ج-33",
                purchaseDateJalali = "${currentYear - 2}/۰۳/۱۰",
                purchasePrice = 650000000L,
                notes = "روغن ۱۰W-40 نیمه‌سنتتیک استفاده شود. لنت‌های ترمز در هر سرویس چک شوند."
            ),
            ToolEntity(
                id = 3L,
                name = "دستگاه تصفیه آب خانگی ۶ مرحله‌ای CCK",
                categoryName = ToolCategory.HOME_APPLIANCE.name,
                modelOrBrand = "سی‌سی‌کا اصل تایوان مدل RO-75G",
                location = "زیر سینک ظرفشویی",
                serialNumber = "CCK-TW-2023",
                purchaseDateJalali = "${currentYear - 1}/۰۱/۲۰",
                purchasePrice = 8500000L,
                notes = "فیلتر مرحله ۱ (الیافی) باید هر ۳ تا ۴ ماه چک شود در صورت تغییر رنگ سریع‌تر تعویض گردد."
            ),
            ToolEntity(
                id = 4L,
                name = "کولر گازی اسپلیت ۲۴ هزار گری (Gree)",
                categoryName = ToolCategory.FACILITY_HVAC.name,
                modelOrBrand = "گری اینورتر پلاس مدل آی‌سیو",
                location = "سالن پذیرایی",
                serialNumber = "GR-24K-INV-88",
                purchaseDateJalali = "${currentYear - 3}/۰۲/۱۵",
                purchasePrice = 45000000L,
                notes = "قبل از شروع فصل گرما شستشوی پنل و تست فشار گاز R410 انجام شود."
            ),
            ToolEntity(
                id = 5L,
                name = "دریل بتن‌کن و چکش تخریب بوش (Bosch)",
                categoryName = ToolCategory.WORKSHOP_TOOLS.name,
                modelOrBrand = "بوش GBH 2-26 DRE اصل",
                location = "کارگاه / قفسه ابزارآلات برقی",
                serialNumber = "BSH-88219-GER",
                purchaseDateJalali = "${currentYear - 2}/۱۱/۰۵",
                purchasePrice = 12000000L,
                notes = "گریس‌کاری محفظه قلم‌گیر با گریس نسوز مخصوص در هر ۵۰ ساعت کارکرد الزامی است."
            ),
            ToolEntity(
                id = 6L,
                name = "کپسول آتش‌نشانی پودر و گاز ۶ کیلویی بایا",
                categoryName = ToolCategory.SAFETY_FIRE.name,
                modelOrBrand = "بایا سیلندر استاندارد ملی",
                location = "راهروی ورودی واحد - کنار تابلو برق",
                serialNumber = "BY-FIRE-06K-22",
                purchaseDateJalali = "${currentYear - 1}/۰۵/۰۱",
                purchasePrice = 1500000L,
                notes = "عقربه مانومتر باید در منطقه سبز باشد. سالانه یک‌بار در تیر/مرداد باید شارژ مجدد شود."
            )
        )

        // Calculate dynamic dates relative to now so schedules are immediately interactive
        val past10Days = JalaliCalendar.addDays(now, -10).toStandardString()
        val in5Days = JalaliCalendar.addDays(now, 5).toStandardString()
        val in20Days = JalaliCalendar.addDays(now, 20).toStandardString()
        val in45Days = JalaliCalendar.addDays(now, 45).toStandardString()
        val in90Days = JalaliCalendar.addDays(now, 90).toStandardString()
        val in6Months = JalaliCalendar.addMonths(now, 6).toStandardString()
        val in1Year = JalaliCalendar.addMonths(now, 12).toStandardString()

        val lastMonth = JalaliCalendar.addMonths(now, -1).toStandardString()
        val last3Months = JalaliCalendar.addMonths(now, -3).toStandardString()
        val last6Months = JalaliCalendar.addMonths(now, -6).toStandardString()

        val schedules = listOf(
            // Tool 1: پکیج
            ServiceScheduleEntity(
                id = 1L,
                toolId = 1L,
                toolName = "پکیج شوفاژ دیواری بوتان (Perla Pro)",
                title = "سرویس سالانه و اسیدشویی مبدل حرارتی",
                serviceTypeName = ServiceType.CLEANING_DESCALING.name,
                intervalTypeName = IntervalType.ANNUAL.name,
                lastServiceDateJalali = last6Months,
                nextServiceDateJalali = in20Days,
                expiryDateJalali = "${currentYear + 1}/۰۷/۱۵",
                priorityName = ServicePriority.HIGH.name,
                estimatedCost = 1200000L,
                technicianName = "مهندس اکبری (نمایندگی بوتان)",
                technicianPhone = "09123456789",
                reminderDaysBefore = 7,
                notes = "شامل رسوب‌زدایی مبدل ثانویه، تنظیم شعله و بررسی سنسور NTC"
            ),
            ServiceScheduleEntity(
                id = 2L,
                toolId = 1L,
                toolName = "پکیج شوفاژ دیواری بوتان (Perla Pro)",
                title = "تعویض فیلتر پلی‌فسفات و مغناطیسی ورودی آب",
                serviceTypeName = ServiceType.REPLACEMENT.name,
                intervalTypeName = IntervalType.BIANNUAL.name,
                lastServiceDateJalali = last3Months,
                nextServiceDateJalali = in45Days,
                expiryDateJalali = "",
                priorityName = ServicePriority.MEDIUM.name,
                estimatedCost = 450000L,
                technicianName = "سرویس‌کار تاسیسات",
                technicianPhone = "09351234567",
                reminderDaysBefore = 3,
                notes = "کارتریج فیلتر پلی‌فسفات تعویض گردد تا پکیج رسوب نگیرد."
            ),

            // Tool 2: پژو پارس
            ServiceScheduleEntity(
                id = 3L,
                toolId = 2L,
                toolName = "خودرو سواری پژو پارس TU5",
                title = "تعویض روغن موتور، فیلتر روغن و فیلتر هوا",
                serviceTypeName = ServiceType.LUBRICATION_OIL.name,
                intervalTypeName = IntervalType.QUARTERLY.name,
                lastServiceDateJalali = last3Months,
                nextServiceDateJalali = in5Days, // Due soon!
                expiryDateJalali = "",
                priorityName = ServicePriority.HIGH.name,
                estimatedCost = 1800000L,
                technicianName = "اتوسرویس بهرام",
                technicianPhone = "09121112233",
                reminderDaysBefore = 7,
                notes = "روغن توتال یا بهران رانا ۱۰W۴۰ ریخته شود. فیلتر کابین نیز تمیز شود."
            ),
            ServiceScheduleEntity(
                id = 4L,
                toolId = 2L,
                toolName = "خودرو سواری پژو پارس TU5",
                title = "تمدید بیمه شخص ثالث و حوادث راننده",
                serviceTypeName = ServiceType.WARRANTY_INSURANCE.name,
                intervalTypeName = IntervalType.ANNUAL.name,
                lastServiceDateJalali = JalaliCalendar.addMonths(now, -11).toStandardString(),
                nextServiceDateJalali = in20Days,
                expiryDateJalali = in20Days,
                priorityName = ServicePriority.HIGH.name,
                estimatedCost = 7500000L,
                technicianName = "نمایندگی بیمه ایران (کد ۴۴۱۰)",
                technicianPhone = "02188776655",
                reminderDaysBefore = 10,
                notes = "با تخفیف عدم خسارت ۴ ساله تمدید شود."
            ),

            // Tool 3: تصفیه آب
            ServiceScheduleEntity(
                id = 5L,
                toolId = 3L,
                toolName = "دستگاه تصفیه آب خانگی ۶ مرحله‌ای CCK",
                title = "تعویض فیلترهای پیش‌تصفیه (مراحل ۱، ۲ و ۳)",
                serviceTypeName = ServiceType.REPLACEMENT.name,
                intervalTypeName = IntervalType.BIANNUAL.name,
                lastServiceDateJalali = last6Months,
                nextServiceDateJalali = past10Days, // Overdue! Action required!
                expiryDateJalali = "",
                priorityName = ServicePriority.HIGH.name,
                estimatedCost = 380000L,
                technicianName = "تکنسین تصفیه آب (آقای رضایی)",
                technicianPhone = "09194445566",
                reminderDaysBefore = 5,
                notes = "فیلتر مرحله ۱ کدر شده است. هر سه کارتریج با برند مرغوب تعویض شوند."
            ),

            // Tool 4: کولر گازی
            ServiceScheduleEntity(
                id = 6L,
                toolId = 4L,
                toolName = "کولر گازی اسپلیت ۲۴ هزار گری (Gree)",
                title = "سرویس فصلی و شستشوی رادیاتور و فیلترها",
                serviceTypeName = ServiceType.CLEANING_DESCALING.name,
                intervalTypeName = IntervalType.ANNUAL.name,
                lastServiceDateJalali = JalaliCalendar.addMonths(now, -4).toStandardString(),
                nextServiceDateJalali = in6Months,
                expiryDateJalali = "",
                priorityName = ServicePriority.LOW.name,
                estimatedCost = 800000L,
                technicianName = "مرکز تخصصی تهویه مطبوع البرز",
                technicianPhone = "02122334455",
                reminderDaysBefore = 7,
                notes = "شستشوی فیلتر آنتی‌باکتریال با آب ولرم و تست نشتی لوله‌های مسی"
            ),

            // Tool 5: دریل بوش
            ServiceScheduleEntity(
                id = 7L,
                toolId = 5L,
                toolName = "دریل بتن‌کن و چکش تخریب بوش (Bosch)",
                title = "بررسی زغال موتور و روانکاری گیربکس",
                serviceTypeName = ServiceType.LUBRICATION_OIL.name,
                intervalTypeName = IntervalType.BIANNUAL.name,
                lastServiceDateJalali = last6Months,
                nextServiceDateJalali = in90Days,
                expiryDateJalali = "",
                priorityName = ServicePriority.MEDIUM.name,
                estimatedCost = 350000L,
                technicianName = "تعمیرگاه ابزار صنعت",
                technicianPhone = "09127778899",
                reminderDaysBefore = 3,
                notes = "زغال فابریک بوش تهیه شود."
            ),

            // Tool 6: کپسول آتش‌نشانی
            ServiceScheduleEntity(
                id = 8L,
                toolId = 6L,
                toolName = "کپسول آتش‌نشانی پودر و گاز ۶ کیلویی بایا",
                title = "شارژ سالانه کپسول و پلمپ استاندارد آتش‌نشانی",
                serviceTypeName = ServiceType.INSPECTION_CHECK.name,
                intervalTypeName = IntervalType.ANNUAL.name,
                lastServiceDateJalali = JalaliCalendar.addMonths(now, -11).toStandardString(),
                nextServiceDateJalali = in5Days, // Due soon!
                expiryDateJalali = in5Days,
                priorityName = ServicePriority.HIGH.name,
                estimatedCost = 250000L,
                technicianName = "مرکز مجاز شارژ کپسول آتش‌نشانی ایمن‌گستر",
                technicianPhone = "02166554433",
                reminderDaysBefore = 7,
                notes = "کارتکس تاییدیه استاندارد و کارت بازرسی سالانه دریافت شود."
            )
        )

        val logs = listOf(
            ServiceLogEntity(
                id = 1L,
                toolId = 2L,
                toolName = "خودرو سواری پژو پارس TU5",
                serviceScheduleId = 3L,
                serviceTitle = "تعویض روغن موتور و فیلترها",
                performedDateJalali = last3Months,
                actualCost = 1650000L,
                technicianOrShop = "اتوسرویس بهرام",
                invoiceNumber = "INV-78401",
                partsReplaced = "روغن توتال ۴ لیتری، فیلتر سرکان، فیلتر کابین",
                notes = "کیلومتر کارکرد خودرو: ۴۲,۵۰۰ کیلومتر"
            ),
            ServiceLogEntity(
                id = 2L,
                toolId = 1L,
                toolName = "پکیج شوفاژ دیواری بوتان (Perla Pro)",
                serviceScheduleId = 1L,
                serviceTitle = "سرویس سالانه و اسیدشویی مبدل",
                performedDateJalali = last6Months,
                actualCost = 950000L,
                technicianOrShop = "مهندس اکبری",
                invoiceNumber = "BTN-REC-102",
                partsReplaced = "اورینگ‌های مبدل، سنسور دما",
                notes = "مبدل ثانویه کاملاً رسوب‌زدایی شد و فشار تست شد."
            ),
            ServiceLogEntity(
                id = 3L,
                toolId = 3L,
                toolName = "دستگاه تصفیه آب خانگی ۶ مرحله‌ای CCK",
                serviceScheduleId = 5L,
                serviceTitle = "تعویض فیلترهای ۱ و ۲ و ۳",
                performedDateJalali = last6Months,
                actualCost = 320000L,
                technicianOrShop = "فروشگاه تصفیه آب زلال",
                invoiceNumber = "ZLL-552",
                partsReplaced = "کارتریج PP ۵ میکرون، کربن اکتیو UDF، کربن بلاک CTO",
                notes = "سختی آب خروجی (TDS) روی ۴۵ میلی‌گرم بر لیتر تنظیم گردید."
            )
        )

        return Triple(tools, schedules, logs)
    }
}
