package com.example.util

import java.util.Calendar
import java.util.Locale

/**
 * Robust Solar Hijri (Jalali / تقویم هجری شمسی) Calendar utility class.
 * Handles bidirectional conversion between Gregorian (Miladi) and Jalali dates,
 * Persian formatting, day-of-week calculation, leap years, date arithmetic,
 * and relative time strings in Persian.
 */
data class JalaliDate(
    val year: Int,
    val month: Int, // 1 to 12
    val day: Int    // 1 to 29/30/31
) : Comparable<JalaliDate> {

    fun format(includeDayName: Boolean = false): String {
        val monthName = JalaliCalendar.getMonthName(month)
        val formatted = "${JalaliCalendar.toPersianDigits(day)} $monthName ${JalaliCalendar.toPersianDigits(year)}"
        return if (includeDayName) {
            val dayName = JalaliCalendar.getDayOfWeekName(this)
            "$dayName، $formatted"
        } else {
            formatted
        }
    }

    fun toStandardString(): String {
        return "%04d/%02d/%02d".format(Locale.US, year, month, day)
    }

    fun toPersianStandardString(): String {
        return JalaliCalendar.toPersianDigits(toStandardString())
    }

    override fun compareTo(other: JalaliDate): Int {
        if (this.year != other.year) return this.year.compareTo(other.year)
        if (this.month != other.month) return this.month.compareTo(other.month)
        return this.day.compareTo(other.day)
    }

    fun isBefore(other: JalaliDate): Boolean = this < other
    fun isAfter(other: JalaliDate): Boolean = this > other
    fun isSame(other: JalaliDate): Boolean = this == other
}

object JalaliCalendar {

    val MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val WEEKDAY_NAMES = listOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
    )

    fun getMonthName(month: Int): String {
        return if (month in 1..12) MONTH_NAMES[month - 1] else ""
    }

    fun isLeapJalaliYear(year: Int): Boolean {
        val jy = year - 979
        val l1 = (jy / 33) * 8 + ((jy % 33 + 3) / 4)
        val nextJy = jy + 1
        val l2 = (nextJy / 33) * 8 + ((nextJy % 33 + 3) / 4)
        return (l2 - l1) == 1
    }

    fun isLeapYear(year: Int): Boolean = isLeapJalaliYear(year)

    fun getDaysInMonth(year: Int, month: Int): Int {
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            month == 12 -> if (isLeapJalaliYear(year)) 30 else 29
            else -> 30
        }
    }

    /**
     * Converts a Gregorian date (year, month 1-12, day 1-31) to JalaliDate.
     */
    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val isGLeap = (gYear % 4 == 0 && gYear % 100 != 0) || (gYear % 400 == 0)
        if (isGLeap) gDaysInMonth[2] = 29

        var gy = gYear - 1600
        var gm = gMonth - 1
        var gd = gDay - 1

        var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i + 1]
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        var jd = 0
        if (jDayNo < 186) {
            jm = 1 + jDayNo / 31
            jd = 1 + (jDayNo % 31)
        } else {
            jm = 7 + (jDayNo - 186) / 30
            jd = 1 + ((jDayNo - 186) % 30)
        }

        return JalaliDate(jy, jm, jd)
    }

    /**
     * Converts Jalali date to Gregorian (year, month 1-12, day 1-31)
     */
    fun jalaliToGregorian(jYear: Int, jMonth: Int, jDay: Int): Triple<Int, Int, Int> {
        val jy = jYear - 979
        val jm = jMonth - 1
        val jd = jDay - 1

        var jDayNo = 365 * jy + (jy / 33) * 8 + ((jy % 33 + 3) / 4)
        for (i in 0 until jm) {
            jDayNo += if (i < 6) 31 else 30
        }
        jDayNo += jd

        var gDayNo = jDayNo + 79

        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524

            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        val gDaysInMonth = intArrayOf(0, 31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        var i = 1
        while (i <= 12) {
            if (gDayNo < gDaysInMonth[i]) {
                gm = i
                break
            }
            gDayNo -= gDaysInMonth[i]
            i++
        }
        val gd = gDayNo + 1
        return Triple(gy, gm, gd)
    }

    fun now(): JalaliDate {
        val cal = Calendar.getInstance()
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun parse(dateStr: String): JalaliDate? {
        if (dateStr.isBlank()) return null
        val normalized = dateStr.replace(" ", "").replace("-", "/").replace("٫", "/")
        val parts = normalized.split("/")
        if (parts.size != 3) return null
        return try {
            val y = toEnglishDigits(parts[0]).toInt()
            val m = toEnglishDigits(parts[1]).toInt()
            val d = toEnglishDigits(parts[2]).toInt()
            if (y in 1300..1500 && m in 1..12 && d in 1..31) {
                JalaliDate(y, m, d)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun toMillis(jalaliDate: JalaliDate): Long {
        val (gy, gm, gd) = jalaliToGregorian(jalaliDate.year, jalaliDate.month, jalaliDate.day)
        val cal = Calendar.getInstance()
        cal.set(gy, gm - 1, gd, 12, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun fromMillis(millis: Long): JalaliDate {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun getDayOfWeekName(date: JalaliDate): String {
        val (gy, gm, gd) = jalaliToGregorian(date.year, date.month, date.day)
        val cal = Calendar.getInstance()
        cal.set(gy, gm - 1, gd)
        // Calendar.SATURDAY = 7, SUNDAY = 1, ...
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "شنبه"
            Calendar.SUNDAY -> "یکشنبه"
            Calendar.MONDAY -> "دوشنبه"
            Calendar.TUESDAY -> "سه‌شنبه"
            Calendar.WEDNESDAY -> "چهارشنبه"
            Calendar.THURSDAY -> "پنج‌شنبه"
            Calendar.FRIDAY -> "جمعه"
            else -> ""
        }
    }

    /**
     * Adds months to a Jalali date safely handling day overflows.
     */
    fun addMonths(date: JalaliDate, monthsToAdd: Int): JalaliDate {
        var totalMonths = (date.year * 12 + (date.month - 1)) + monthsToAdd
        val newYear = totalMonths / 12
        val newMonth = (totalMonths % 12) + 1
        val maxDays = getDaysInMonth(newYear, newMonth)
        val newDay = date.day.coerceAtMost(maxDays)
        return JalaliDate(newYear, newMonth, newDay)
    }

    /**
     * Adds days to a Jalali date using epoch millis.
     */
    fun addDays(date: JalaliDate, daysToAdd: Int): JalaliDate {
        val millis = toMillis(date) + (daysToAdd.toLong() * 24 * 60 * 60 * 1000L)
        return fromMillis(millis)
    }

    /**
     * Calculates the difference in days between two Jalali dates (d2 - d1).
     */
    fun daysBetween(d1: JalaliDate, d2: JalaliDate): Int {
        val m1 = toMillis(d1)
        val m2 = toMillis(d2)
        return ((m2 - m1) / (24 * 60 * 60 * 1000L)).toInt()
    }

    /**
     * Returns a human-friendly Persian relative date description compared to today.
     * e.g., «امروز»، «فردا»، «۳ روز دیگر»، «۵ روز تاخیر (گذشته)»، «۲ ماه دیگر»
     */
    fun getRelativeTimeString(targetDate: JalaliDate, referenceDate: JalaliDate = now()): String {
        val diffDays = daysBetween(referenceDate, targetDate)
        return when {
            diffDays == 0 -> "امروز"
            diffDays == 1 -> "فردا"
            diffDays == -1 -> "دیروز"
            diffDays in 2..7 -> "${toPersianDigits(diffDays)} روز دیگر"
            diffDays in -7..-2 -> "${toPersianDigits(-diffDays)} روز گذشته"
            diffDays in 8..30 -> "${toPersianDigits(diffDays)} روز دیگر"
            diffDays in -30..-8 -> "${toPersianDigits(-diffDays)} روز تاخیر (گذشته)"
            diffDays in 31..60 -> "حدود ۱ ماه دیگر"
            diffDays in 61..90 -> "حدود ۲ ماه دیگر"
            diffDays in 91..180 -> "حدود ${toPersianDigits(diffDays / 30)} ماه دیگر"
            diffDays in 181..365 -> "حدود ${toPersianDigits(diffDays / 30)} ماه دیگر"
            diffDays > 365 -> "بیش از ۱ سال دیگر"
            diffDays < -30 -> "${toPersianDigits(-diffDays / 30)} ماه گذشته از موعد"
            else -> "${toPersianDigits(diffDays)} روز"
        }
    }

    fun toPersianDigits(text: Any?): String {
        if (text == null) return ""
        val str = text.toString()
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder()
        for (ch in str) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toEnglishDigits(text: String): String {
        val persianToEnglish = mapOf(
            '۰' to '0', '۱' to '1', '۲' to '2', '۳' to '3', '۴' to '4',
            '۵' to '5', '۶' to '6', '۷' to '7', '۸' to '8', '۹' to '9',
            '٠' to '0', '١' to '1', '٢' to '2', '٣' to '3', '٤' to '4',
            '٥' to '5', '٦' to '6', '٧' to '7', '٨' to '8', '٩' to '9'
        )
        val sb = StringBuilder()
        for (ch in text) {
            sb.append(persianToEnglish[ch] ?: ch)
        }
        return sb.toString()
    }

    fun formatPrice(amount: Long): String {
        if (amount <= 0) return "رایگان / نامشخص"
        val formatted = "%,d".format(Locale.US, amount)
        return "${toPersianDigits(formatted)} تومان"
    }
}
