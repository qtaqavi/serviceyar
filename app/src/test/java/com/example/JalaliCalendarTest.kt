package com.example

import com.example.util.JalaliCalendar
import com.example.util.JalaliDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JalaliCalendarTest {

    @Test
    fun testPersianDigitConversion() {
        assertEquals("۱۲۳۴۵۶۷۸۹۰", JalaliCalendar.toPersianDigits("1234567890"))
        assertEquals("1234567890", JalaliCalendar.toEnglishDigits("۱۲۳۴۵۶۷۸۹۰"))
    }

    @Test
    fun testLeapYearCalculation() {
        // Known Jalali leap years
        assertTrue(JalaliCalendar.isLeapYear(1403))
        assertFalse(JalaliCalendar.isLeapYear(1402))
        assertFalse(JalaliCalendar.isLeapYear(1404))
    }

    @Test
    fun testDaysInMonth() {
        // First 6 months have 31 days
        assertEquals(31, JalaliCalendar.getDaysInMonth(1403, 1))
        assertEquals(31, JalaliCalendar.getDaysInMonth(1403, 6))

        // Next 5 months have 30 days
        assertEquals(30, JalaliCalendar.getDaysInMonth(1403, 7))
        assertEquals(30, JalaliCalendar.getDaysInMonth(1403, 11))

        // Month 12 has 30 days in leap year 1403, 29 in non-leap year 1402
        assertEquals(30, JalaliCalendar.getDaysInMonth(1403, 12))
        assertEquals(29, JalaliCalendar.getDaysInMonth(1402, 12))
    }

    @Test
    fun testDateArithmetic() {
        val start = JalaliDate(1403, 1, 15)

        // Adding 1 month
        val nextMonth = JalaliCalendar.addMonths(start, 1)
        assertEquals(1403, nextMonth.year)
        assertEquals(2, nextMonth.month)
        assertEquals(15, nextMonth.day)

        // Adding 12 months (annual)
        val nextYear = JalaliCalendar.addMonths(start, 12)
        assertEquals(1404, nextYear.year)
        assertEquals(1, nextYear.month)
        assertEquals(15, nextYear.day)

        // Adding 30 days
        val plus30Days = JalaliCalendar.addDays(start, 30)
        assertEquals(1403, plus30Days.year)
        assertEquals(2, plus30Days.month)
        assertEquals(14, plus30Days.day)
    }

    @Test
    fun testRelativeTimeString() {
        val now = JalaliDate(1403, 5, 18)
        val today = JalaliDate(1403, 5, 18)
        val tomorrow = JalaliDate(1403, 5, 19)
        val yesterday = JalaliDate(1403, 5, 17)
        val in10Days = JalaliDate(1403, 5, 28)
        val overdue5Days = JalaliDate(1403, 5, 13)

        assertEquals("امروز", JalaliCalendar.getRelativeTimeString(today, now))
        assertEquals("فردا", JalaliCalendar.getRelativeTimeString(tomorrow, now))
        assertEquals("دیروز", JalaliCalendar.getRelativeTimeString(yesterday, now))
        assertEquals("۱۰ روز دیگر", JalaliCalendar.getRelativeTimeString(in10Days, now))
        assertEquals("۵ روز گذشته", JalaliCalendar.getRelativeTimeString(overdue5Days, now))
    }

    @Test
    fun testPriceFormatting() {
        assertEquals("۱۵۰,۰۰۰ تومان", JalaliCalendar.formatPrice(150000))
        assertEquals("۲,۵۰۰,۰۰۰ تومان", JalaliCalendar.formatPrice(2500000))
    }
}
